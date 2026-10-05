# KiteMarket product icon

Original artwork: a bold white price-tag silhouette with a gold exchange coin
on a violet-to-pink gradient tile. The simple tag, coin and bidirectional arrows
stay readable at small sizes and share the rounded, high-contrast visual family
of the ArcPass and VerifyMC icons. No lettering, third-party artwork or theme
assets.

- Editable source: `kitemarket-icon.svg` (512 × 512 vector canvas).
- RGBA exports: `kitemarket-icon-{1024,512,128}.png`, with transparent outer
  corners and antialiased symbol edges.
- Re-export: `python branding/export-icon.py` with Python 3.9+ and Pillow. The
  SVG is the editable source and the exporter mirrors its intentionally simple
  geometry for deterministic raster output.
- Use the 128 export for small cards; use SVG or the larger exports for websites,
  product pages, and repository branding. Do not stretch into a different ratio.
- Copyright © 2026 KiteMC. The product icon may be used to identify and link to
  KiteMarket. It is separate from the MIT API and example code licenses.
