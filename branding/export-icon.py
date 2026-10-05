"""Render the editable SVG directly as antialiased RGBA PNGs.

Run: python branding/export-icon.py
Requires Python 3.9+, Node.js 20+ and sharp (local install or NODE_PATH).
"""

from pathlib import Path
import shutil
import subprocess
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parent

RENDER_SCRIPT = r"""
const fs = require("node:fs");
const path = require("node:path");
const sharp = require("sharp");

async function exportIcon() {
  const root = process.argv[1];
  const source = fs.readFileSync(path.join(root, "kitemarket-icon.svg"));
  const destinations = await Promise.all([1024, 512, 128].map(async size => {
    const destination = path.join(root, `kitemarket-icon-${size}.png`);
    await sharp(source, { density: 144 })
      .resize(size, size, { kernel: "lanczos3" })
      .ensureAlpha()
      .png()
      .toFile(destination);
    return destination;
  }));
  destinations.forEach(destination => console.log(destination));
}

exportIcon().catch(error => {
  console.error(error.message);
  process.exitCode = 1;
});
"""


def export():
    source = ET.parse(ROOT / "kitemarket-icon.svg").getroot()
    if source.attrib.get("viewBox") != "0 0 512 512":
        raise ValueError("Icon viewBox must be 0 0 512 512")
    node = shutil.which("node")
    if node is None:
        raise RuntimeError("Node.js 20+ is required to export the icon")
    subprocess.run([node, "-e", RENDER_SCRIPT, str(ROOT)], cwd=ROOT, check=True)


if __name__ == "__main__":
    export()
