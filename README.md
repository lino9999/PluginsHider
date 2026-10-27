# 🛡️ PluginsHider

[![Java Version](https://img.shields.io/badge/Java-21%2B-blue.svg)](https://adoptium.net/)
[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.11%20--%2026.3%2B-brightgreen.svg)](https://papermc.io/)
[![Folia Supported](https://img.shields.io/badge/Folia-Native%20Support-blueviolet.svg)](https://github.com/PaperMC/Folia)
[![PacketEvents](https://img.shields.io/badge/PacketEvents-2.x-orange.svg)](https://github.com/retrooper/packetevents)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Author](https://img.shields.io/badge/Author-Lino-cyan.svg)](https://github.com/lino9999)

> **PluginsHider** is an advanced, ultra-lightweight Minecraft server security and privacy suite designed to protect Spigot, Paper, Purpur, and Folia servers against client-side intelligence gathering, mod snooping, command reconnaissance, and unauthorized privilege escalation.

---

## 📌 About PluginsHider

Modern Minecraft hack clients (such as Meteor, Aristois, LiquidBounce, and Wurst) automatically scan servers upon connection. They snoop on registered plugin messaging channels (`minecraft:register`), query the server brand via F3 debug packets, brute-force tab completions through `DECLARE_COMMANDS`, probe `/plugins`, `/version`, and `/about`, and attempt privilege escalation on `/op`.

**PluginsHider** eliminates these vulnerabilities at the packet level before data ever reaches the client. Completely redesigned from the ground up by **Lino**, it combines high-performance packet interception with an intuitive, extensible group access control system (ACL), reactive LuckPerms hooks, and native Folia multi-threading support.

---

## ✨ Key Features

- **🔒 Packet-Level Command Cloaking**: Filters the Brigadier `DECLARE_COMMANDS` packet so unauthorized commands do not even appear in the player's client-side suggestion list.
- **🚫 Complete Plugin Inspection Defense**: Intercepts `/plugins`, `/pl`, `/version`, `/ver`, `/about`, `/help`, `/?`, and namespace prefixes (`bukkit:*`, `minecraft:*`). Returns customizable styled responses or stays silent.
- **🏷️ F3 Server Brand Spoofing**: Overrides the outgoing `minecraft:brand` packet to display custom stealth network branding in the F3 debug screen.
- **🕵️ Anti-Recon Channel Protection**: Sanitizes outgoing `minecraft:register` packets to prevent modified clients from detecting installed mods and plugins via registered channels.
- **⚡ Command Spam Rate Limiter**: Sliding-window rate limiter prevents automated command spamming, bot attacks, and command-based server freezing.
- **👑 Strict Operator (OP) Lockdown**: Enforces a strict allowed-operators whitelist by player name and UUID. Automatically strips operator status and disconnects unauthorized players attempting privilege escalation.
- **👥 Modular Group ACL & Inheritance**: Configure priority-based groups with `WHITELIST` or `BLACKLIST` modes, group inheritance, command negations (`!warp vault`), regex pattern matching, and reusable list files.
- **🔄 Reactive LuckPerms Integration**: Listens to LuckPerms event bus to automatically update and resynchronize client command trees in real-time without requiring a reconnect.
- **🌐 Folia Native Compatibility**: Multi-threaded, asynchronous task scheduling compatible with Folia regionized threading as well as Paper and Spigot.
- **🧹 Clean Room Architecture**: Zero bloat, zero tracking, zero external telemetry, zero comments in source code, 100% clean and maintainable Java 21 codebase.

---

## 💻 Commands & Aliases

Primary command: `/pluginshider` (Aliases: `/phider`, `/ph`, `/plh`)

| Command | Permission | Description |
| :--- | :--- | :--- |
| `/phider help` | `pluginshider.admin` | Displays the help menu with all available commands and version info. |
| `/phider reload` | `pluginshider.admin` | Reloads all configuration files, ACL rules, and external list files. |
| `/phider check <player>` | `pluginshider.admin` | Inspects a player's resolved group, priority, mode, and active rules. |
| `/phider sync [player]` | `pluginshider.admin` | Forces a real-time recalculation and synchronization of client command trees. |
| `/phider brand` | `pluginshider.admin` | Displays the active server brand spoofing configuration and status. |

---

## 🔑 Permissions

| Permission Node | Default | Description |
| :--- | :--- | :--- |
| `pluginshider.admin` | `op` | Full administrative control over `/pluginshider`. |
| `pluginshider.group.<name>` | `false` | Assigns a player to a defined group (e.g. `pluginshider.group.vip`). |
| `pluginshider.bypass.commands` | `false` | Allows execution of all commands regardless of group restrictions. |
| `pluginshider.bypass.tab` | `false` | Bypasses tab completion filtering; sees all commands in autocomplete. |
| `pluginshider.bypass.plugins` | `false` | Allows executing `/plugins` to see the real installed plugins list. |
| `pluginshider.bypass.channels` | `false` | Bypasses plugin messaging channel registration filtering. |
| `pluginshider.bypass.spam` | `false` | Bypasses the command spam rate limiter. |

---

## ⚙️ Configuration (`config.yml`)

```yaml
# ==============================================================================
# PluginsHider - Comprehensive Server Security & Privacy Configuration
# Author: Lino
# Compatible with Spigot, Paper, Purpur, and Folia (1.21.11 - 26.3+)
# ==============================================================================

settings:
  locale: "en"
  debug: false
  case-insensitive-commands: true

brand-spoofing:
  enabled: true
  server-brand: "&8[&bPlugins&3Hider&8] &7Protected"

channel-protection:
  enabled: true
  allowed-channels:
    - "minecraft:brand"
    - "bungeecord:main"
    - "velocity:main"
    - "geyserutils:main"

plugin-command-shield:
  enabled: true
  blocked-commands:
    - "/plugins"
    - "/pl"
    - "/bukkit:plugins"
    - "/bukkit:pl"
    - "/version"
    - "/ver"
    - "/bukkit:version"
    - "/bukkit:ver"
    - "/about"
    - "/bukkit:about"
    - "/help"
    - "/bukkit:help"
    - "/?"
    - "/bukkit:?"
  custom-response: "&8[&bPluginsHider&8] &cCommand access denied."

rate-limiting:
  enabled: true
  window-seconds: 2
  max-commands-per-window: 30
  action: KICK # Options: KICK or BLOCK
  kick-message: "&cExcessive command activity detected. Connection terminated."

operator-security:
  enabled: false
  allowed-operators:
    - "Lino"
  auto-deop-unauthorized: true
  kick-unauthorized: true
  unauthorized-kick-message: "&cUnauthorized operator privilege detected."
  block-op-command-message: "&cYou are not authorized to assign operator privileges."
  sweep-interval-seconds: 5

groups:
  default:
    priority: 0
    mode: WHITELIST # Options: WHITELIST or BLACKLIST
    blocked-message: "&cUnknown command. Type \"/help\" for help."
    commands:
      - "spawn"
      - "tpa"
      - "tpaccept"
      - "tpdeny"
      - "home"
      - "sethome"
      - "msg"
      - "r"
      - "pay"
      - "balance"
      - "bal"
      - "warp"
      - "list:utility-commands"
    tab-complete:
      - "spawn"
      - "tpa"
      - "tpaccept"
      - "tpdeny"
      - "home"
      - "sethome"
      - "msg"
      - "r"
      - "pay"
      - "balance"
      - "bal"
      - "warp"
      - "list:utility-commands"
    inherited-groups: []

  vip:
    priority: 10
    mode: WHITELIST
    blocked-message: "&cUnknown command. Type \"/help\" for help."
    commands:
      - "fly"
      - "feed"
      - "heal"
      - "workbench"
      - "craft"
    tab-complete:
      - "fly"
      - "feed"
      - "heal"
      - "workbench"
      - "craft"
    inherited-groups:
      - "default"

  admin:
    priority: 100
    mode: BLACKLIST
    blocked-message: "&cAdministrative safety lock: This command is restricted."
    commands:
      - "stop"
      - "restart"
    tab-complete: []
    inherited-groups: []
```

---

## 🎯 Advanced Filtering Capabilities

### 1. Subcommand Control
You can allow or restrict commands down to specific arguments:
```yaml
commands:
  - "warp shop"
  - "warp pvp"
```

### 2. Rule Negation (`!`)
Carve out specific exclusions from inherited groups or wildcards:
```yaml
commands:
  - "warp"
  - "!warp admin_vault"
```

### 3. Regex Patterns (`regex:`)
Match complex command syntax using standard Java regular expressions:
```yaml
commands:
  - "regex:^msg [a-zA-Z0-9_]+ (hello|hi)$"
```

### 4. Modular External Lists (`list:<name>`)
Create reusable command libraries in `plugins/PluginsHider/lists/<name>.yml` and reference them with `list:<name>`:
```yaml
# lists/utility-commands.yml
utility-commands:
  - "ping"
  - "rules"
  - "discord"
  - "vote"
```

---

## 🛠️ Building from Source

### Prerequisites
- JDK 21 or higher
- Git

### Build Instructions
```bash
git clone https://github.com/lino9999/PluginsHider.git
cd PluginsHider
./mvnw clean package
```
The compiled, ready-to-deploy JAR will be generated in `target/PluginsHider-1.0.0.jar`.

---

## 🏷️ Repository Metadata & SEO

- **Repository About**: Ultra-performance command hiding, tab-complete privacy, F3 brand spoofing, and channel leak security suite for Minecraft 1.21.11 - 26.3+ (Spigot, Paper, Folia).
- **GitHub Topics / Tags**: `minecraft-plugin`, `plugin-hider`, `command-blocker`, `tab-complete`, `security`, `packetevents`, `folia-supported`, `spigot-plugin`, `paper-plugin`, `anti-recon`, `brand-spoofing`, `anticheat-suite`, `privacy`

---

## 📄 License & Credits

- Developed and maintained by **Lino**.
- Distributed under the [MIT License](LICENSE).
