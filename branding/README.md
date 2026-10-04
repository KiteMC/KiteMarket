# KiteMarket product icon

Original artwork: a white market-stall silhouette and exchange arrows on a
rounded violet-gradient tile. The rounded corners, clear symbol and generous
spacing belong to the same visual family as the ArcPass and VerifyMC icons.
No lettering, third-party artwork or theme assets.

- Editable source: `kitemarket-icon.svg` (512 × 512 vector canvas).
- RGBA exports: `kitemarket-icon-{1024,512,128}.png`, with transparent outer
  corners and antialiased symbol edges.
- Re-export: `python branding/export-icon.py` with Python 3.9+ and Pillow.
  The exporter reads the SVG's gradient, rounded rectangle and absolute paths;
  edits must retain the limited M/L/H/V/Q/Z vocabulary and round strokes.
- Use the 128 export for small cards; use SVG or the larger exports for websites,
  product pages, and repository branding. Do not stretch into a different ratio.
- Copyright © 2026 KiteMC. The product icon may be used to identify and link to
  KiteMarket. It is separate from the MIT API and example code licenses.
