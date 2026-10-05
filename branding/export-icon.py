"""Export the editable KiteMarket SVG geometry as antialiased PNGs.

Run: python branding/export-icon.py

The SVG remains the editable source. Pillow draws the same deliberately
simple geometry so the repository can ship deterministic 1024/512/128 PNG
exports without requiring a native SVG renderer.
"""

from pathlib import Path

from PIL import Image, ImageColor, ImageDraw

ROOT = Path(__file__).resolve().parent
CANVAS = 512
SCALE = 4


def lerp(a: tuple[int, int, int], b: tuple[int, int, int], ratio: float):
    return tuple(round(x + (y - x) * ratio) for x, y in zip(a, b))


def diagonal_gradient(size: int):
    start = ImageColor.getrgb("#C4B5FD")
    middle = ImageColor.getrgb("#7C3AED")
    end = ImageColor.getrgb("#3730A3")
    image = Image.new("RGBA", (size, size))
    pixels = image.load()
    for y in range(size):
        for x in range(size):
            ratio = (x + y) / (2 * (size - 1))
            color = lerp(start, middle, ratio / 0.42) if ratio < 0.42 else lerp(
                middle, end, (ratio - 0.42) / 0.58
            )
            pixels[x, y] = (*color, 255)
    return image


def point(value: float):
    return round(value * SCALE)


def line(draw, coordinates, fill, width):
    draw.line(
        [(point(x), point(y)) for x, y in coordinates],
        fill=fill,
        width=point(width),
        joint="curve",
    )


def draw_icon():
    size = CANVAS * SCALE
    image = diagonal_gradient(size)
    draw = ImageDraw.Draw(image, "RGBA")
    white = (255, 255, 255, 255)
    violet = (124, 58, 237, 255)
    gold = (245, 158, 11, 255)

    # Soft kite silhouette.
    kite = [(256, 56), (328, 126), (256, 198), (184, 126)]
    line(draw, kite + [kite[0]], (255, 255, 255, 82), 14)
    line(draw, [(256, 198), (258, 233), (300, 260)], (255, 255, 255, 82), 10)
    line(draw, [(300, 260), (316, 262)], (255, 255, 255, 82), 10)
    line(draw, [(316, 262), (308, 276)], (255, 255, 255, 82), 10)

    # Canopy.
    line(draw, [(86, 236), (116, 146), (396, 146), (426, 236)], white, 14)
    draw.polygon(
        [(144 * SCALE, 146 * SCALE), (196 * SCALE, 146 * SCALE),
         (170 * SCALE, 236 * SCALE), (118 * SCALE, 236 * SCALE)],
        fill=violet,
    )
    draw.polygon(
        [(248 * SCALE, 146 * SCALE), (300 * SCALE, 146 * SCALE),
         (326 * SCALE, 236 * SCALE), (274 * SCALE, 236 * SCALE)],
        fill=violet,
    )
    line(draw, [(86, 236), (426, 236)], white, 14)
    for center in (138, 218, 298, 378):
        draw.arc(
            (
                point(center - 52),
                point(184),
                point(center + 52),
                point(288),
            ),
            0,
            180,
            fill=white,
            width=point(14),
        )
    line(draw, [(122, 312), (122, 414), (390, 414), (390, 312)], white, 18)
    line(draw, [(96, 414), (416, 414)], white, 18)

    # Exchange coin.
    draw.ellipse(
        (point(198), point(296), point(314), point(412)),
        fill=gold,
        outline=white,
        width=point(12),
    )
    line(draw, [(224, 342), (288, 342)], white, 11)
    line(draw, [(272, 326), (288, 342), (272, 358)], white, 11)
    line(draw, [(288, 366), (224, 366)], white, 11)
    line(draw, [(240, 350), (224, 366), (240, 382)], white, 11)

    return image


def export():
    image = draw_icon()
    for size in (1024, 512, 128):
        destination = ROOT / f"kitemarket-icon-{size}.png"
        image.resize((size, size), Image.Resampling.LANCZOS).save(destination)
        print(destination)


if __name__ == "__main__":
    export()
