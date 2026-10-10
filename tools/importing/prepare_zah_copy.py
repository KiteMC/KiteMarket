"""Create a checked, stopped zAuctionHouse source copy without writing to its data directory."""
from __future__ import annotations

import argparse
from contextlib import closing
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import sqlite3
import uuid

MAX_FILE = 256 * 1024 * 1024
FORMATS = {"v3-json": "V3_JSON", "v3-sqlite": "V3_SQLITE", "v4-sqlite": "V4_SQLITE"}


def no_links_in_path(path: Path) -> None:
    for component in (path, *path.parents):
        try:
            details = component.lstat()
        except FileNotFoundError:
            continue
        if component.is_symlink() or getattr(details, "st_file_attributes", 0) & 0x400:
            raise ValueError(f"Links/reparse points are not accepted: {component.name}")


def regular(path: Path, *, directory: bool = False) -> None:
    no_links_in_path(path)
    details = path.lstat()
    if path.is_symlink() or getattr(details, "st_file_attributes", 0) & 0x400:
        raise ValueError(f"Links/reparse points are not accepted: {path.name}")
    if directory and not path.is_dir() or not directory and not path.is_file():
        raise ValueError(f"Unexpected source file type: {path.name}")
    if not directory and details.st_size > MAX_FILE:
        raise ValueError(f"Source file exceeds 256 MiB: {path.name}")


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(65536), b""):
            digest.update(chunk)
    return digest.hexdigest()


def read_identity(path: Path, source: Path, major: str) -> str:
    """A durable origin file prevents a fresh copy/hash from becoming a new import namespace."""
    expected = {"schema": 1, "sourceDirectory": str(source), "sourceMajor": major}
    if path.exists():
        regular(path)
        stored = json.loads(path.read_text(encoding="utf-8"))
        if any(stored.get(key) != value for key, value in expected.items()):
            raise ValueError("Origin file belongs to a different installation/directory/generation")
        return str(uuid.UUID(stored["sourceInstance"]))
    instance = str(uuid.uuid4())
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("x", encoding="utf-8", newline="\n") as handle:
        json.dump({**expected, "sourceInstance": instance}, handle, indent=2)
        handle.write("\n")
    return instance


def source_files(source: Path, kind: str, database: str | None) -> list[Path]:
    if kind == "V3_JSON":
        combined = source / "data.json"
        split = [source / name for name in ("items.json", "buying_items.json", "expired_items.json")]
        present = [path for path in split if path.exists()]
        if combined.exists() and present:
            raise ValueError("Combined data.json and split JSON files cannot be mixed")
        files = [combined] if combined.exists() else present
        if not files:
            raise ValueError("No supported V3 JSON item files were found")
        history = source / "transactions.json"
        if history.exists():
            files.append(history)
        return files
    if not database or not re.fullmatch(r"[A-Za-z0-9_.-]{1,128}", database):
        raise ValueError("SQLite requires --database-file with a simple file name")
    path = source / database
    if Path(str(path) + "-wal").exists() or Path(str(path) + "-shm").exists():
        raise ValueError("Use a stopped, checkpointed SQLite database/backup without WAL/SHM")
    return [path]


def observed_economies(files: list[Path], kind: str, prefix: str) -> list[str]:
    economies: set[str] = set()
    if kind == "V3_JSON":
        for path in files:
            if path.name == "transactions.json":
                continue
            data = json.loads(path.read_text(encoding="utf-8"))
            records = data if isinstance(data, list) else [
                record for key in ("items", "buyingItems", "expiredItems")
                for record in data.get(key, [])]
            for record in records:
                value = record.get("economy") if isinstance(record, dict) else None
                if isinstance(value, str) and 0 < len(value) <= 255:
                    economies.add(value)
    else:
        uri = files[0].as_uri() + "?mode=ro&immutable=1"
        with closing(sqlite3.connect(uri, uri=True)) as connection:
            connection.execute("PRAGMA query_only=ON")
            column = "economy" if kind == "V3_SQLITE" else "economy_name"
            for (value,) in connection.execute(
                    f'SELECT DISTINCT "{column}" FROM "{prefix}items"'):
                if isinstance(value, str) and 0 < len(value) <= 255:
                    economies.add(value)
    return sorted(economies)


def prepare(args: argparse.Namespace) -> dict:
    if not args.source_stopped:
        raise ValueError("--source-stopped is required after all source nodes have stopped normally")
    source = Path(args.source_dir).absolute()
    regular(source, directory=True)
    source = source.resolve()
    destination = Path(args.copy_dir).absolute()
    no_links_in_path(destination)
    destination = destination.resolve()
    if destination.exists():
        raise ValueError("Copy directory already exists; inspect it instead of overwriting")
    if source == destination or source in destination.parents:
        raise ValueError("The copy must be outside the foreign source directory")
    kind = FORMATS[args.format]
    if not args.game_version or len(args.game_version) > 64 or not all(
            character.isascii() and (character.isalnum() or character in "._+-")
            for character in args.game_version) or not args.game_version[0].isdigit():
        raise ValueError("Invalid --game-version")
    if len(args.table_prefix) > 64 or any(
            not character.isascii() or not (character.isalnum() or character == "_")
            for character in args.table_prefix):
        raise ValueError("Invalid --table-prefix")
    origin = Path(args.origin_file).absolute()
    no_links_in_path(origin)
    origin = origin.resolve()
    if source == origin or source in origin.parents or destination in origin.parents:
        raise ValueError("Keep the reusable origin file outside both source and copy directories")
    files = source_files(source, kind, args.database_file)
    before = {}
    for path in files:
        regular(path)
        before[path.name] = sha256(path)
    # An invalid source/query is rejected before creating a destination.
    economies = observed_economies(files, kind, args.table_prefix)
    identity = read_identity(origin, source, "V3" if kind.startswith("V3") else "V4")
    destination.parent.mkdir(parents=True, exist_ok=True)
    destination.mkdir()
    generated: list[Path] = []
    try:
        for path in files:
            output = destination / path.name
            with path.open("rb") as original, output.open("xb") as copied:
                generated.append(output)
                shutil.copyfileobj(original, copied, length=65536)
                copied.flush()
                os.fsync(copied.fileno())
            regular(path)
            if sha256(path) != before[path.name] or sha256(output) != before[path.name]:
                raise ValueError("Source changed while copying; no consistent snapshot was produced")
        namespace = f'zauctionhouse-{"v3" if kind.startswith("V3") else "v4"}:{identity}'
        manifest = {
            "schema": 1, "sourceInstance": identity, "gameVersion": args.game_version,
            "format": kind, "tablePrefix": args.table_prefix,
            "copiedFromStoppedSource": True, "files": before,
        }
        if args.database_file:
            manifest["databaseFile"] = args.database_file
        if args.timestamp_zone:
            manifest["timestampZone"] = args.timestamp_zone
        mapping = {"schema": 1, "sourceNamespace": namespace,
                   "gameVersion": args.game_version, "currencies": {}}
        if kind.startswith("V3"):
            # V3's fixed schema has no creation timestamp. Preserve unknown=0, never fabricate one.
            mapping["missingCreatedAt"] = "PRESERVE_UNKNOWN"
        for name, document in (("source-copy.json", manifest), ("mapping.json", mapping)):
            output = destination / name
            with output.open("x", encoding="utf-8", newline="\n") as handle:
                generated.append(output)
                json.dump(document, handle, ensure_ascii=False, indent=2)
                handle.write("\n")
        return {"copyDirectory": str(destination), "originFile": str(origin),
                "sourceNamespace": namespace, "sourceCopySha256": sha256(destination / "source-copy.json"),
                "observedEconomies": economies,
                "next": "Review mapping.json, then run console: km import dry-run <copy-directory-name>"}
    except BaseException:
        # Delete only exact paths created by this invocation, within its new destination.
        for output in reversed(generated):
            if output.parent != destination:
                raise RuntimeError("Refusing cleanup outside the owned copy directory")
            output.unlink(missing_ok=True)
        destination.rmdir()
        raise


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--source-dir", required=True)
    parser.add_argument("--copy-dir", required=True)
    parser.add_argument("--origin-file", required=True,
                        help="Keep and reuse this generated identity file for the same installation")
    parser.add_argument("--format", required=True, choices=FORMATS)
    parser.add_argument("--game-version", required=True)
    parser.add_argument("--database-file")
    parser.add_argument("--table-prefix", default="zauctionhouse_")
    parser.add_argument("--timestamp-zone",
                        help="Original source JVM time zone for offset-free V4 SQL timestamp text")
    parser.add_argument("--source-stopped", action="store_true")
    args = parser.parse_args()
    print(json.dumps(prepare(args), ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
