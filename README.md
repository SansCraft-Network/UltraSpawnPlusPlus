# UltraSpawnPlusPlus

A lightweight, easy-to-configure Spigot plugin that adds a robust delayed teleport to spawn. Players get a visible countdown (chat, actionbar, or bossbar), and the teleport is automatically cancelled if they move or receive damage during the delay.

## Table of Contents

- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
- [Configuration](#configuration)
- [Usage](#usage)
- [Permissions](#permissions)
- [Development](#development)
- [Contributing](#contributing)
- [License](#license)

---

## Features

- Customizable countdown before teleporting to spawn (default 5 seconds, fully configurable)
- Visible countdown in chat, actionbar, and/or bossbar (all customizable)
- Teleport is cancelled if the player moves or takes damage
- Admins can set custom spawn location, messages, and display options
- Per-player custom cooldowns via permissions (in ticks or seconds)
- Permission to bypass the cooldown and teleport instantly
- Clean, dependency-free codebase

---

## Requirements

- Java 8 or higher
- Spigot API 1.13+ (compatible with Paper)
- Maven or Gradle (for building)
- Spigot from 1.16.5 up to 1.21.7 is supported

---

## Installation

1. Download the latest `UltraSpawnPlusPlus.jar` from the [Releases](https://github.com/SansCraft-Network/UltraSpawnPlusPlus/releases).
2. Drop the JAR into your server’s `plugins/` folder.
3. Start or reload your server.

---

## Configuration

All configuration is done in `plugins/UltraSpawnPlusPlus/config.yml`. On first run, a default config will be generated.

```yaml
#######################################################################
# UltraSpawnPlusPlus - Main Configuration
#
# Permissions (set with LuckPerms, PermissionsEx, etc):
#   ultraspawn.spawn                - Allow /spawn
#   ultraspawn.admin                - Allow config reloads/changes
#   ultraspawn.bypasscooldown       - Instantly teleport, no countdown
#   ultraspawn.customcooldown.ticks.N   - Custom cooldown in ticks (20 = 1s)
#   ultraspawn.customcooldown.seconds.N - Custom cooldown in seconds
#     (If a player has multiple, the shortest is used)
#
# Display options:
#   display.bossbar: true/false         - Show time left in bossbar
#   display.bossbar-title: <string>     - Bossbar title, %seconds% replaced
#   display.bossbar-color: <color>      - PINK, BLUE, RED, GREEN, YELLOW, PURPLE, WHITE
#   display.bossbar-style: <style>      - SOLID, SEGMENTED_6, SEGMENTED_10, SEGMENTED_12, SEGMENTED_20
#   display.actionbar: true/false       - Show time left in actionbar
#   display.actionbar-message: <string> - Actionbar message, %seconds% replaced
#
# Messages:
#   All support color codes and placeholders as shown below.
#######################################################################

spawn:
  world: world
  x: 0.39
  y: 198.0
  z: 0.62
  yaw: 0.47
  pitch: 33.57

# Default countdown in seconds (if no custom permission)
delay: 5

display:
  bossbar: false   # Show time left in bossbar
  bossbar-title: "✨ Teleporting in %seconds% second(s)... ✨"
  bossbar-color: "BLUE"   # Options: PINK, BLUE, RED, GREEN, YELLOW, PURPLE, WHITE
  bossbar-style: "SOLID"  # Options: SOLID, SEGMENTED_6, SEGMENTED_10, SEGMENTED_12, SEGMENTED_20
  actionbar: false # Show time left in actionbar
  actionbar-message: "✨ Teleporting in %seconds% second(s)... ✨"

messages:
  countdown: "✨ Teleporting in %seconds% second(s)... ✨"
  cancel_move: "Teleport cancelled: you moved!"
  cancel_damage: "Teleport cancelled: you took damage!"
  start: "✨ Teleporting in %delay% seconds... ✨"
  finish: "✅ Teleporting now!"
```

You can adjust spawn coordinates, countdown length, display options, and all in-game messages. See the comments in the config for all available options and permissions.

---

## Usage

Run the command:
```
/spawn
```

Players without the required permission see the configured `permission-message`.

---

## Permissions

- `ultraspawn.spawn`  
  Allows use of `/spawn`  
  Default: [✔] True  
- `ultraspawn.admin`  
  Allows changing of plugin configuration  
  Default: [X] False  
- `ultraspawn.bypasscooldown`  
  Instantly teleports, bypassing the countdown  
  Default: [X] False  
- `ultraspawn.customcooldown.ticks.N`  
  Set a custom cooldown in ticks (20 = 1 second). If a player has multiple, the shortest is used.  
  Default: [X] False  
- `ultraspawn.customcooldown.seconds.N`  
  Set a custom cooldown in seconds. If a player has multiple, the shortest is used.  
  Default: [X] False  

---

## Development

```bash
# Clone the repo
git clone https://github.com/SansCraft-Network/UltraSpawnPlusPlus.git

# Enter project folder
cd UltraSpawnPlusPlus

# Build with Maven
mvn clean package

# Or build with Gradle
gradle build
```

Compiled JAR will be in `target/` (Maven) or `build/libs/` (Gradle).

---

## Contributing

Contributions are welcome! Feel free to:

- Report bugs and open feature requests in [Issues](https://github.com/SansCraft-Network/UltraSpawnPlusPlus/issues).
- Submit pull requests with clear descriptions of changes.
- Follow existing code style and include Javadoc where appropriate.

---

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
