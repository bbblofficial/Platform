# Platform Plugin v1.0 (1.8.8)

Platform plugin for **Minecraft 1.8.8** — CarbonSpigot compatible.

**Created by Muvixo**

## What's New in v1.0

- **Cosmetic Kit** — Leather Red + Iron armor, Protection III, Unbreakable
- **No Damage** — Players are invincible, no HP is ever lost
- **No Fall Damage** — Players never take fall damage
- **Infinite Food** — Players never get hungry
- **Unstable Connection** — Kicks high-ping players with full command set
- **Spawn System** — Set once, players teleport there on join / respawn
- **Safe config updates** — New options are merged into config.yml without wiping your settings

## Commands

| Command | Description |
|---------|-------------|
| `/platform help` | Show help |
| `/platform creator` | Show credits |
| `/platform kit [player]` | Give the cosmetic kit |
| `/platform setspawn` | Set the spawn point |
| `/platform reload` | Reload config |
| `/platform cc` | Connection check status |
| `/platform cc on\|off\|toggle` | Toggle connection check |
| `/platform cc bypass <player>` | Bypass a player |
| `/platform cc unbypass <player>` | Remove bypass |
| `/platform cc forceaddping <player> <ping>` | Force a ping value |
| `/platform cc ping <player> default` | Remove forced ping |

Aliases: `/pf`

## Permissions

| Permission | Default | Description |
|---|---|---|
| `platform.kit` | op | Give the cosmetic kit |
| `platform.setspawn` | op | Set the spawn point |
| `platform.connection` | op | Manage the connection check |
| `platform.reload` | op | Reload the config |

## Cosmetic Kit

| Item | Enchantment | Extra |
|---|---|---|
| Leather Helmet | Protection III | Color #FF0000, Unbreakable |
| Leather Chestplate | Protection III | Color #FF0000, Unbreakable |
| Iron Leggings | Protection III | Unbreakable |
| Iron Boots | Protection III | Unbreakable |
| Wooden Sword | Sharpness I | Unbreakable |

## Installation

1. Drop JAR into `plugins/`
2. Restart server
3. Config auto-created / safely merged
4. Run `/platform setspawn` in-game

## Building from source

The project uses Maven and GitHub Actions.

```bash
mvn clean package
```

The output JAR will be at `target/Platform.jar`.

### GitHub Actions

Push to `main` or `master` → the workflow automatically builds the plugin
and uploads `Platform-JAR` as an artifact. Tag a commit (e.g. `git tag v1.0 && git push --tags`)
to also publish a GitHub Release with the JAR attached.

## Credits

- **Muvixo** — Creator
