# Ruisu's Gui Mod

Client-side Fabric mod for Minecraft 26.2 that adds movable, resizable HUD boxes.

## Widgets

- **Toggle Sprint**: shows ON/OFF. Set **Options → Controls → Sprint** to **Toggle** so it shows your toggle state.
- **Potion Effects**: each active effect's icon and time left, which turns red in the last 10 seconds. Only shows
  when you have effects. While it's on, vanilla's effect icons in the top-right corner are hidden.

## Editing the layout

Press **Right Shift** (rebindable under Controls → Key Binds → Miscellaneous), or use the config button in
[Mod Menu](https://modrinth.com/mod/modmenu).

- **Drag** a box to move it. It snaps to the screen edges and centre, and lines up with the edges and centres of
  other boxes; a blue guide line shows what it snapped to. Hold **Shift** to drag without snapping.
- **Scroll** over a box, or use **−** / **+**, to resize it from 50% to 400%.
- **Right-click** a box, or use the **Shown** button, to hide or show it.
- The buttons in the middle change the last box you clicked: **Shadow** on/off, **Text** colour, and
  **Background** from off (no box) to fully solid in 25% steps.
- **Reset Selected** puts that box's position, size and style back to the defaults.

Press **Done** or Esc when finished. Everything is saved to `config/ruisusgui.json`.

## Adding a widget

For one line of text, extend `TextHudWidget` and return the text from `text()`. For anything else
(icons, several rows), extend `HudWidget` and draw it yourself. Register it in `RuisusGuiClient`.
Add its name to `assets/ruisusgui/lang/en_us.json` as `hud.ruisusgui.<id>`.

## Building

```
./gradlew build
```

The mod jar ends up in `build/libs/`. It needs Fabric Loader 0.20.1+ and Fabric API.
