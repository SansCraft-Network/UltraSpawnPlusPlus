# UltraSpawnPlusPlus

A lightweight, easy-to-configure Spigot plugin that adds a robust delayed teleport to spawn. Players get a visible countdown, and the teleport is automatically cancelled if they move or receive damage during the delay.

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

- 5-second countdown with customizable messages  
- Automatic cancellation on player movement (block-level)  
- Automatic cancellation on damage  
- Fully configurable spawn coordinates and countdown length  
- Clean, dependency-free codebase  

---

## Requirements

- Java 8 or higher  
- Spigot API 1.13+ (compatible with Paper)  
- Maven or Gradle (for building)
- Spigot from 1.16.5 upto 1.21.7 is supported  

---

## Installation

1. Download the latest `UltraSpawnPlusPlus.jar` from the [Releases](https://github.com/yourusername/UltraSpawnPlusPlus/releases).  
2. Drop the JAR into your server’s `plugins/` folder.  
3. Start or reload your server.  

---

## Configuration

All configuration is done in `plugins/UltraSpawnPlusPlus/config.yml`. On first run, a default config will be generated.  

```yaml
spawn:
  world: world
  x: 0.39
  y: 198.0
  z: 0.62
  yaw: 0.47
  pitch: 33.57
delay: 5      # countdown in seconds
messages:
  countdown: "✨ Teleporting in %seconds% second(s)... ✨"
  cancel_move: "Teleport cancelled: you moved!"
  cancel_damage: "Teleport cancelled: you took damage!"
  start: "✨ Teleporting in %delay% seconds... ✨"
  finish: "✅ Teleporting now!"
```

You can adjust spawn coordinates, countdown length, and all in-game messages.

---

## Usage

Run the command:  
```
/spawn
```

Players without the required permission see the configured `permission-message`.

---

## Permissions

- `spawn.setspawn`  
  Allows use of `/spawn`.  

---

## Development

```bash
# Clone the repo
git clone https://github.com/yourusername/UltraSpawnPlusPlus.git

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

- Report bugs and open feature requests in [Issues](https://github.com/yourusername/UltraSpawnPlusPlus/issues).  
- Submit pull requests with clear descriptions of changes.  
- Follow existing code style and include Javadoc where appropriate.  

---

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
