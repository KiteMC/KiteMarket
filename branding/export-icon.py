"""Export this editable SVG as antialiased PNGs using Python 3 and Pillow.

Run: python branding/export-icon.py
The limited SVG vocabulary uses a diagonal gradient, a rounded rectangle and
absolute M/L/H/V/Q/Z paths. No fonts, external artwork or native SVG runtime.
"""

from pathlib import Path
import re
import xml.etree.ElementTree as ET

from PIL import Image, ImageColor, ImageDraw

ROOT = Path(__file__).resolve().parent
CANVAS = 512
SCALE = 4


def paths(data):
    """Flatten the icon's absolute paths, preserving separate subpaths."""
    tokens = re.findall(r"[A-Za-z]|-?\d+(?:\.\d+)?", data)
    points = []
    position = (0, 0)
    index = 0
    while index < len(tokens):
        command = tokens[index]
        index += 1
        if command == "M":
            if points:
                yield points
            position = (float(tokens[index]), float(tokens[index + 1]))
            points = [position]
            index += 2
        elif command == "L":
            position = (float(tokens[index]), float(tokens[index + 1]))
            points.append(position)
            index += 2
        elif command == "H":
            position = (float(tokens[index]), position[1])
            points.append(position)
            index += 1
        elif command == "V":
            position = (position[0], float(tokens[index]))
            points.append(position)
            index += 1
        elif command == "Q":
            control = (float(tokens[index]), float(tokens[index + 1]))
            end = (float(tokens[index + 2]), float(tokens[index + 3]))
            start = position
            for step in range(1, 65):
                t = step / 64
                points.append(tuple(
                    (1 - t) ** 2 * start[axis]
                    + 2 * (1 - t) * t * control[axis]
                    + t ** 2 * end[axis]
                    for axis in (0, 1)
                ))
            position = end
            index += 4
        elif command == "Z":
            points.append(points[0])
            yield points
            points = []
        else:
            raise ValueError(f"Unsupported path command {command}")
    if points:
        yield points


def gradient_image(element, size):
    if tuple(element.get(key) for key in ("x1", "y1", "x2", "y2")) != (
        "0%", "0%", "100%", "100%"
    ):
        raise ValueError("Only a full diagonal gradient is supported")
    stops = [
        (float(stop.attrib["offset"].rstrip("%")) / 100,
         ImageColor.getrgb(stop.attrib["stop-color"]))
        for stop in element
    ]
    if not stops or stops[0][0] != 0 or stops[-1][0] != 1:
        raise ValueError("Gradient stops must span 0% to 100%")
    image = Image.new("RGBA", (size, size))
    draw = ImageDraw.Draw(image)
    for diagonal in range(2 * size - 1):
        t = diagonal / (2 * (size - 1))
        left, right = next(
            (a, b) for a, b in zip(stops, stops[1:]) if a[0] <= t <= b[0]
        )
        ratio = (t - left[0]) / (right[0] - left[0])
        color = tuple(round(a + (b - a) * ratio) for a, b in zip(left[1], right[1]))
        if diagonal < size:
            line = ((diagonal, 0), (0, diagonal))
        else:
            line = ((size - 1, diagonal - size + 1), (diagonal - size + 1, size - 1))
        draw.line(line, fill=color + (255,))
    return image


def draw_elements(image, elements, gradients, inherited=None):
    for element in elements:
        style = dict(inherited or {}) | element.attrib
        tag = element.tag.rsplit("}", 1)[-1]
        if tag in ("title", "desc", "defs"):
            continue
        if tag == "g":
            draw_elements(image, element, gradients, style)
        elif tag == "rect":
            if (element.get("width"), element.get("height")) != ("512", "512"):
                raise ValueError("The background must cover the 512px canvas")
            gradient_id = style["fill"].removeprefix("url(#").removesuffix(")")
            background = gradient_image(gradients[gradient_id], image.width)
            mask = Image.new("L", image.size)
            ImageDraw.Draw(mask).rounded_rectangle(
                (0, 0, image.width - 1, image.height - 1),
                radius=float(element.attrib["rx"]) * SCALE, fill=255,
            )
            image.paste(background, mask=mask)
        elif tag == "path":
            if style.get("fill") != "none" or any(
                style.get(key) != "round" for key in ("stroke-linecap", "stroke-linejoin")
            ):
                raise ValueError("Icon paths require round strokes and no fill")
            painter = ImageDraw.Draw(image)
            color = ImageColor.getrgb(style["stroke"]) + (255,)
            width = round(float(style["stroke-width"]) * SCALE)
            radius = width / 2
            for points in paths(element.attrib["d"]):
                scaled = [(x * SCALE, y * SCALE) for x, y in points]
                painter.line(scaled, fill=color, width=width, joint="curve")
                for x, y in scaled:
                    painter.ellipse(
                        (x - radius, y - radius, x + radius, y + radius), fill=color,
                    )
        else:
            raise ValueError(f"Unsupported SVG element {tag}")


def export():
    source = ET.parse(ROOT / "kitemarket-icon.svg").getroot()
    if source.attrib.get("viewBox") != "0 0 512 512":
        raise ValueError("Icon viewBox must be 0 0 512 512")
    gradients = {
        element.attrib["id"]: element
        for element in source.iter()
        if element.tag.rsplit("}", 1)[-1] == "linearGradient"
    }
    image = Image.new("RGBA", (CANVAS * SCALE, CANVAS * SCALE))
    draw_elements(image, source, gradients)
    for size in (1024, 512, 128):
        destination = ROOT / f"kitemarket-icon-{size}.png"
        image.resize((size, size), Image.Resampling.LANCZOS).save(destination)
        print(destination)


if __name__ == "__main__":
    export()
