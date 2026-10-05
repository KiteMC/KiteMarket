#!/usr/bin/env python3
"""Check two public Java 11 SDKs; optionally plan authenticated Maven uploads.

Does not upload, delete or overwrite packages. Credentials are read from the
workflow environment and are never included in output or cached on disk.
"""
from __future__ import annotations

import argparse
import base64
import hashlib
import io
import os
from pathlib import Path, PurePosixPath
import re
import struct
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen
import xml.etree.ElementTree as ET
import zipfile

MODULES = {
    "market-api": ("KiteMarket-API", "kitemarket-api", "com/kitemc/market/api/", "KiteMarketApi"),
    "market-ui-api": ("KiteMarket-UI-API", "kitemarket-ui-api", "com/kitemc/market/api/ui/", "KiteMarketUiApi"),
}
REGISTRY = "https://maven.pkg.github.com/kitemc/KiteMarket"
POM_NS = {"m": "http://maven.apache.org/POM/4.0.0"}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise ValueError(message)


def validate_jar(data: bytes, prefix: str, entry: str, *, sources: bool = False) -> None:
    with zipfile.ZipFile(io.BytesIO(data)) as jar:
        names = jar.namelist()
        require(len(names) == len(set(names)), "Duplicate SDK archive entries")
        require("META-INF/LICENSE" in names, "SDK MIT license is missing")
        require(b"MIT License" in jar.read("META-INF/LICENSE"), "Unexpected SDK license")
        suffix = ".java" if sources else ".class"
        require(prefix + entry + suffix in names, "Required SDK entry is missing")
        for name in names:
            path = PurePosixPath(name)
            require(not path.is_absolute() and "\\" not in name and ":" not in name
                    and ".." not in path.parts, "Unsafe SDK archive path")
            if name.endswith(".class"):
                require(not sources and name.startswith(prefix), "Non-SDK class in public package")
                raw = jar.read(name)
                require(len(raw) >= 8 and raw[:4] == b"\xca\xfe\xba\xbe"
                        and struct.unpack(">H", raw[6:8])[0] == 55,
                        "SDK must use Java 11 class files")
            if name.endswith(".java"):
                require(sources and name.startswith(prefix), "Non-SDK source in public package")
            require(not name.lower().endswith((".pem", ".key", ".yml", ".yaml")),
                    "Private or runtime resource in public SDK")


def validate_pom(data: bytes, artifact: str, version: str) -> None:
    pom = ET.fromstring(data)
    require(pom.findtext("m:groupId", namespaces=POM_NS) == "com.kitemc"
            and pom.findtext("m:artifactId", namespaces=POM_NS) == artifact
            and pom.findtext("m:version", namespaces=POM_NS) == version,
            "SDK Maven coordinates do not match the requested version")
    require(pom.findtext("m:licenses/m:license/m:name", namespaces=POM_NS) == "MIT License",
            "SDK POM must declare MIT")
    require(not pom.findall("m:dependencies/m:dependency", POM_NS),
            "Public SDK must not pull core or compile-only server dependencies")
    require(pom.findtext("m:scm/m:url", namespaces=POM_NS) == "https://github.com/KiteMC/KiteMarket",
            "SDK POM must identify the public repository")


def artifacts(root: Path, module: str, version: str) -> dict[str, bytes]:
    basename, artifact, prefix, entry = MODULES[module]
    libraries = root / module / "build/libs"
    files = {
        artifact + f"-{version}.jar": (libraries / f"{basename}-{version}.jar").read_bytes(),
        artifact + f"-{version}-sources.jar": (libraries / f"{basename}-{version}-sources.jar").read_bytes(),
        artifact + f"-{version}-javadoc.jar": (libraries / f"{basename}-{version}-javadoc.jar").read_bytes(),
        artifact + f"-{version}.pom": (root / module / "build/publications/sdk/pom-default.xml").read_bytes(),
    }
    validate_jar(files[artifact + f"-{version}.jar"], prefix, entry)
    validate_jar(files[artifact + f"-{version}-sources.jar"], prefix, entry, sources=True)
    validate_pom(files[artifact + f"-{version}.pom"], artifact, version)
    # Javadoc may include no executable or source classes, runtime resources or keys.
    with zipfile.ZipFile(io.BytesIO(files[artifact + f"-{version}-javadoc.jar"])) as docs:
        require("META-INF/LICENSE" in docs.namelist(), "Javadoc MIT license is missing")
        for name in docs.namelist():
            path = PurePosixPath(name)
            require(not path.is_absolute() and ".." not in path.parts
                    and "\\" not in name and ":" not in name,
                    "Unsafe Javadoc archive path")
            require(not name.lower().endswith((".class", ".java", ".pem", ".key", ".yml", ".yaml")),
                    "Non-documentation content in public Javadoc")
    return files


def remote_file(artifact: str, version: str, name: str, authorization: str) -> bytes | None:
    url = f"{REGISTRY}/com/kitemc/{artifact}/{version}/{name}"
    request = Request(url, headers={"Authorization": authorization})
    try:
        with urlopen(request, timeout=30) as response:
            require(response.headers.get("Content-Length") is None
                    or int(response.headers["Content-Length"]) <= 16 * 1024 * 1024,
                    "Unexpected remote SDK size")
            data = response.read(16 * 1024 * 1024 + 1)
            require(len(data) <= 16 * 1024 * 1024, "Unexpected remote SDK size")
            return data
    except HTTPError as error:
        if error.code == 404:
            return None
        raise ValueError(f"GitHub Packages returned HTTP {error.code}; check package permissions") from None
    except URLError:
        raise ValueError("GitHub Packages could not be reached; no upload planned") from None


def remote_plan(files: dict[str, bytes], artifact: str, version: str,
                authorization: str) -> str:
    remote = {name: remote_file(artifact, version, name, authorization) for name in files}
    if all(data is None for data in remote.values()):
        return "publish"
    # A partial version may be the result of an interrupted Maven upload. Stop
    # rather than overwriting it automatically; the administrator can inspect it.
    require(all(data is not None for data in remote.values()),
            f"{artifact}:{version} exists only partially; inspect the package before retrying")
    for name, data in files.items():
        require(hashlib.sha256(data).digest() == hashlib.sha256(remote[name]).digest(),
                f"{artifact}:{version} already contains different {name}; use a new version")
    return "existing"


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--version", required=True)
    parser.add_argument("--remote-plan", action="store_true")
    args = parser.parse_args()
    require(re.fullmatch(r"\d+\.\d+\.\d+", args.version) is not None, "Expected stable SDK version")
    root = Path(__file__).resolve().parents[1]
    version_match = re.search(r'\bversion\s*=\s*"([^"]+)"',
                              (root / "build.gradle.kts").read_text(encoding="utf-8"))
    require(version_match is not None and version_match[1] == args.version,
            "The tagged public build version differs from the requested package version")
    local = {module: artifacts(root, module, args.version) for module in MODULES}
    plans = {}
    if args.remote_plan:
        actor, token = os.environ.get("GITHUB_ACTOR"), os.environ.get("GITHUB_TOKEN")
        require(bool(actor) and bool(token), "Workflow GitHub Packages credentials are missing")
        authorization = "Basic " + base64.b64encode(f"{actor}:{token}".encode()).decode()
        plans = {module: remote_plan(files, MODULES[module][1], args.version, authorization)
                 for module, files in local.items()}
        output = os.environ.get("GITHUB_OUTPUT")
        require(bool(output), "GITHUB_OUTPUT is required for the upload plan")
        with open(output, "a", encoding="utf-8") as stream:
            for module, plan in plans.items():
                stream.write(f"{module.replace('-', '_')}={plan}\n")
    for module in MODULES:
        print(f"{module}: Java 11 / MIT / public-only verified"
              + (f"; {plans[module]}" if plans else ""))


if __name__ == "__main__":
    try:
        main()
    except (ValueError, OSError, zipfile.BadZipFile, ET.ParseError) as error:
        raise SystemExit(f"SDK package check failed: {error}") from None
