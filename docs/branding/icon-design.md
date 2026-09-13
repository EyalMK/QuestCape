# QuestCape icon

QuestCape uses an original gold quest map with a blue route arrow. The compact silhouette and bold arrow remain visible on RuneLite's dark sidebar. The design prompt below uses the current plugin name.

- Runtime asset: `src/main/resources/quest-route-icon.png` (32 x 32 RGBA PNG, displayed by RuneLite at 16 x 16).
- Plugin Hub listing asset: root `icon.png` (the same 32 x 32 RGBA export, within the 48 x 72 pixel limit).
- Generated master: `docs/branding/quest-route-master.png`.
- Preview: `docs/branding/quest-route-preview.png` (256 x 256).
- Generation mode: built-in imagegen, new image, no reference images.
- Export: high-quality bicubic downscaling with System.Drawing, preserving alpha. The master is outside runtime resources.

## Design prompt

```text
Use case: logo-brand
Asset type: transparent PNG sidebar icon for the QuestCape RuneLite plugin.
Primary request: Create one original, polished fantasy-game UI icon: a compact golden folded quest map with a bold blue upward route arrow across its face, communicating a guided path through quests.
Style: clean hand-crafted game inventory icon, extremely simple flat shapes, restrained two-tone shading, crisp silhouette and thick dark brown contour. Designed to remain recognizable when reduced to 16 by 16 pixels.
Composition: one centered square icon, filling about 90 percent of the canvas with modest transparent padding. Map has just two broad folds. One thick blue route bends once and terminates in a prominent upward arrowhead. Strong hierarchy: golden map silhouette first, blue arrow second.
Palette: warm parchment gold and amber, light cream highlights, bright sky blue arrow, dark brown outlines. High contrast against a dark charcoal sidebar.
Background: genuinely transparent alpha outside the icon, including corners. No painted checkerboard, no background tile.
Constraints: single icon only; no text, letters, numbers, logos, watermark, border frame, micro-details, dotted trails, extra symbols, gradients, glow, cast shadow, photorealism or 3D perspective. Keep the route arrow bold enough to survive reduction to 16 pixels.
```
