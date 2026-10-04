"""Rebuild the freely reusable minimal ItemsAdder example images (MIT License).

Draws a plain white frame and a small book button, not official Market Stall artwork.
Requires Pillow; writes exclusively to this example's registered texture paths.
"""
import argparse
from pathlib import Path
from PIL import Image, ImageDraw

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument("--icons-only", action="store_true", help="Keep the existing font background.")
arguments = parser.parse_args()
textures = Path(__file__).resolve().parent / "themes" / "example-ia" / "itemsadder" / "textures"
if not arguments.icons_only:
    target = textures / "gui" / "market.png"
    target.parent.mkdir(parents=True, exist_ok=True)
    image = Image.new("RGBA", (176, 222), (0, 0, 0, 0))
    ImageDraw.Draw(image).rectangle((0, 0, 175, 221), outline=(255, 255, 255, 255), width=2)
    image.save(target, compress_level=9)
    print(f"Written minimal community example: {target}")

target = textures / "items" / "book_button.png"
target.parent.mkdir(parents=True, exist_ok=True)
image = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
draw = ImageDraw.Draw(image)
draw.rectangle((3, 2, 12, 13), fill="#3e4149")
draw.rectangle((4, 3, 11, 11), fill="#f5edd8")
draw.rectangle((4, 12, 11, 12), fill="#b29a75")
draw.rectangle((3, 3, 4, 12), fill="#7c956c")
draw.rectangle((6, 5, 10, 5), fill="#b4aa96")
draw.rectangle((6, 7, 10, 7), fill="#b4aa96")
draw.rectangle((6, 9, 9, 9), fill="#b4aa96")
image.save(target, compress_level=9)
print(f"Written MIT community item icon: {target}")
