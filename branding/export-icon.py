"""Export the editable KiteMarket product icon as antialiased PNGs.

Run: python branding/export-icon.py

The SVG is the editable source. Pillow mirrors its intentionally bold
geometry so the repository can ship deterministic 1024/512/128 RGBA exports
without requiring a native SVG renderer.
"""

from __future__ import annotations

import math
from pathlib import Path

from PIL import Image, ImageColor, ImageDraw, ImageFilter

ROOT = Path(__file__).resolve().parent
BASE = 512
SCALE = 4


def lerp(a: tuple[int, int, int], b: tuple[int, int, int], ratio: float):
    ratio = max(0.0, min(1.0, ratio))
    return tuple(round(x + (y - x) * ratio) for x, y in zip(a, b))


def background(size: int) -> Image.Image:
    """Build the violet/pink radial gradient used by the SVG source."""
    image = Image.new("RGBA", (size, size))
    pixels = image.load()
    start = ImageColor.getrgb("#B9A4FF")
    middle = ImageColor.getrgb("#7C3AED")
    end = ImageColor.getrgb("#BE185D")
    cx, cy = size * 0.17, size * 0.12
    max_distance = math.hypot(size * 0.83, size * 0.88)
    for y in range(size):
        for x in range(size):
            distance = math.hypot(x - cx, y - cy) / max_distance
            if distance < 0.43:
                color = lerp(start, middle, distance / 0.43)
            else:
                color = lerp(middle, end, (distance - 0.43) / 0.57)
            pixels[x, y] = (*color, 255)

    mask = Image.new("L", (size, size), 0)
    ImageDraw.Draw(mask).rounded_rectangle(
        (0, 0, size - 1, size - 1), radius=112 * SCALE, fill=255
    )
    image.putalpha(mask)
    return image


def p(value: float) -> int:
    return round(value * SCALE)


def draw_icon() -> Image.Image:
    size = BASE * SCALE
    image = background(size)
    overlay = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(overlay, "RGBA")

    draw.rounded_rectangle(
        (p(22), p(22), p(490), p(490)),
        radius=p(94),
        outline=(255, 255, 255, 36),
        width=p(4),
    )
    draw.ellipse((p(-18), p(-14), p(282), p(170)), fill=(255, 255, 255, 20))

    symbol = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    sd = ImageDraw.Draw(symbol, "RGBA")
    white = (255, 255, 255, 255)
    deep_violet = (109, 40, 217, 255)
    inner_violet = (91, 33, 182, 255)
    dark_violet = (76, 29, 149, 255)
    gold = (251, 191, 36, 255)
    gold_light = (253, 230, 138, 255)

    outer = [
        (p(116), p(118)),
        (p(306), p(118)),
        (p(408), p(220)),
        (p(408), p(382)),
        (p(116), p(382)),
    ]
    sd.polygon(outer, fill=white)
    sd.line(outer + [outer[0]], fill=white, width=p(8), joint="curve")

    inner = [
        (p(142), p(148)),
        (p(294), p(148)),
        (p(376), p(230)),
        (p(376), p(352)),
        (p(142), p(352)),
    ]
    sd.polygon(inner, fill=deep_violet)
    sd.line(inner + [inner[0]], fill=inner_violet, width=p(4), joint="curve")

    sd.ellipse(
        (p(147), p(147), p(183), p(183)),
        fill=dark_violet,
        outline=white,
        width=p(8),
    )
    sd.ellipse((p(160), p(160), p(170), p(170)), fill=gold_light)

    sd.ellipse(
        (p(178), p(200), p(334), p(356)),
        fill=gold,
        outline=white,
        width=p(12),
    )
    sd.ellipse(
        (p(195), p(217), p(317), p(339)),
        outline=(255, 255, 255, 82),
        width=p(4),
    )

    arrow_color = inner_violet
    sd.line([(p(211), p(262)), (p(299), p(262))], fill=arrow_color, width=p(13))
    sd.line(
        [(p(277), p(240)), (p(299), p(262)), (p(277), p(284))],
        fill=arrow_color,
        width=p(13),
        joint="curve",
    )
    sd.line([(p(301), p(298)), (p(213), p(298))], fill=arrow_color, width=p(13))
    sd.line(
        [(p(235), p(276)), (p(213), p(298)), (p(235), p(320))],
        fill=arrow_color,
        width=p(13),
        joint="curve",
    )

    symbol = symbol.rotate(6, resample=Image.Resampling.BICUBIC, center=(p(256), p(256)))
    shadow_alpha = symbol.getchannel("A").filter(ImageFilter.GaussianBlur(p(10)))
    shadow_alpha = shadow_alpha.point(lambda alpha: round(alpha * 0.34))
    shadow = Image.new("RGBA", (size, size), (76, 29, 149, 0))
    shadow.putalpha(shadow_alpha)
    overlay.alpha_composite(shadow, (0, p(12)))
    overlay.alpha_composite(symbol)
    image.alpha_composite(overlay)
    return image


def export() -> None:
    image = draw_icon()
    for size in (1024, 512, 128):
        destination = ROOT / f"kitemarket-icon-{size}.png"
        image.resize((size, size), Image.Resampling.LANCZOS).save(destination)
        print(destination)


if __name__ == "__main__":
    export()
