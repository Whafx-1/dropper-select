# Dropper Select (Fabric, Minecraft 26.3)

Hold **1–9** while looking at a dropper and it fires the item in that slot. No redstone needed.

- **Tap** a key to fire one item. **Hold** it to fire one every 4 ticks (5 items per second).
- Slots are numbered left to right, top to bottom, the same way the dropper GUI shows them:
  ```
  1 2 3
  4 5 6
  7 8 9
  ```
- If the slot is empty, the dropper just clicks and doesn't fire anything else.
- If there's a chest or hopper in front of the dropper, the item goes into it, just like a redstone pulse would send it.
- This also works on **dispensers**, so they shoot, place, or use the chosen item.
- Redstone-powered droppers still pick a random slot, as in vanilla.
- To change the keys, go to Options > Controls > Key Binds > Miscellaneous and look for "Dropper: Fire Slot N".
  The defaults are 1–9, so move your hotbar keys to something else first.
- The mod has to be installed on the client. In singleplayer that's all you need. On a server, the server needs it too.

## Build the jar

You need **Java 25** (JDK). One option is https://adoptium.net (pick "Temurin 25").

Open a terminal in this folder and run:

- Windows: `gradlew.bat build`
- Mac/Linux: `./gradlew build`

The first build takes a few minutes because it downloads Minecraft and Fabric.
When it finishes, the mod is at `build/libs/dropper-select-1.0.0.jar`.
Use that file, not the one ending in `-sources.jar`.

## Install

1. Install Fabric Loader for 26.3 (https://fabricmc.net/use/installer/).
2. Put **Fabric API** for 26.3 (https://modrinth.com/mod/fabric-api) into your `.minecraft/mods` folder.
3. Put `dropper-select-1.0.0.jar` into the same folder.
4. Launch the Fabric 26.3 profile.

## No Java? Build it on GitHub instead

Push this folder to a new GitHub repository. The included workflow (`.github/workflows/build.yml`)
builds the mod automatically. Open the **Actions** tab, select the latest run, and download **Artifacts**.
