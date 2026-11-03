# Ruisu's Gui Mod

Client-side Fabric mod for Minecraft 26.2 that shows a **Toggle Sprint: ON/OFF** box on your HUD.

- Set **Options → Controls → Sprint** to **Toggle** so the box shows your toggle state.
- Press **Right Shift** (rebindable under Controls → Key Binds → Miscellaneous) to open the editor, drag the box anywhere, and scroll over it (or use **−** / **+**) to change its size from 50% to 400%. Press **Done** or Esc when finished. The position and size are saved to `config/ruisusgui.json`.
- With [Mod Menu](https://modrinth.com/mod/modmenu) installed, the mod's config button opens the same editor.

## Building

```
./gradlew build
```

The mod jar ends up in `build/libs/`. It needs Fabric Loader 0.20.1+ and Fabric API.
