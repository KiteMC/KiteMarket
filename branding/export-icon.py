"""Export the editable pixel SVG as transparent PNGs without raster AI or fonts.

Requires Python 3 and Pillow. Run: python branding/export-icon.py
The deliberately small SVG vocabulary keeps the source and PNGs identical.
"""

from pathlib import Path
import re
import xml.etree.ElementTree as ET

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent


def polygon_path(data):
    """Read the M / H,V / h,v / z commands used by this icon."""
    tokens = re.findall(r"[MHVhvz]|-?\d+", data)
    points = []
    position = (0, 0)
    index = 0
    while index < len(tokens):
        command = tokens[index]
        index += 1
        if command == "M":
            if points:
                yield points
            position = (int(tokens[index]), int(tokens[index + 1]))
            points = [position]
            index += 2
        elif command == "h":
            position = (position[0] + int(tokens[index]), position[1])
            points.append(position)
            index += 1
        elif command == "v":
            position = (position[0], position[1] + int(tokens[index]))
            points.append(position)
            index += 1
        elif command == "H":
            position = (int(tokens[index]), position[1])
            points.append(position)
            index += 1
        elif command == "V":
            position = (position[0], int(tokens[index]))
            points.append(position)
            index += 1
        elif command == "z":
            yield points
            points = []
        else:
            raise ValueError(f"Unsupported path command {command}")
    if points:
        yield points


def export():
    source = ET.parse(ROOT / "kitemarket-icon.svg")
    # Draw at the intended pixel resolution; integer enlargement keeps edges crisp.
    image = Image.new("RGBA", (64, 64))
    draw = ImageDraw.Draw(image)
    for element in source.getroot():
        tag = element.tag.rsplit("}", 1)[-1]
        if tag in ("title", "desc"):
            continue
        color = element.attrib["fill"]
        if tag == "rect":
            x, y = int(element.attrib["x"]), int(element.attrib["y"])
            w, h = int(element.attrib["width"]), int(element.attrib["height"])
            draw.rectangle((x, y, x + w - 1, y + h - 1), fill=color)
        elif tag == "path":
            # Rasterize on doubled coordinates with point centers at odd values;
            # paths describe pixel boundaries, so right/bottom edges stay exclusive.
            mask = Image.new("L", (128, 128))
            painter = ImageDraw.Draw(mask)
            for points in polygon_path(element.attrib["d"]):
                painter.polygon([(x * 2, y * 2) for x, y in points], fill=255)
            mask = mask.resize((64, 64), Image.Resampling.NEAREST)
            image.paste(color, mask=mask)
        else:
            raise ValueError(f"Unsupported SVG element {tag}")
    for size in (1024, 512, 128):
        destination = ROOT / f"kitemarket-icon-{size}.png"
        image.resize((size, size), Image.Resampling.NEAREST).save(destination)
        print(destination)


if __name__ == "__main__":
    export()
