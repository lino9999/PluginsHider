# PluginsHider - Agent Guidelines & Architecture Manual

## Overview
PluginsHider is an advanced command shielding, tab-completion privacy, server brand spoofing, and channel leak prevention plugin created by Lino for modern Spigot, Paper, Purpur, and Folia servers (compatible with Minecraft 1.21.11 to 26.3+).

## Hard Constraints
- **Author & Ownership**: Exclusively authored by Lino. Do not import, copy, or reference legacy naming, old package names, or original plugin branding.
- **Clean Source Code**: All Java source files must contain **zero comments** (no block or line comments).
- **Language**: All user-facing strings, console log entries, and documentation must be in **English**.
- **Java Target**: Java 21 bytecode (`maven.compiler.release = 21`).
- **Dependencies**: Public Spigot / Paper API (`spigot-api`) and PacketEvents 2.x (`packetevents-spigot`). Optional soft-depend on LuckPerms (`net.luckperms:api`).
- **Security**: No DRM, phone-home verification, backdoors, or arbitrary remote execution code.

## Efficient Workflow & Token Preservation
- **Never scan or read**: `Plugin originale/`, `.work/`, `target/`, or `dist/`. These are excluded via `.ignore`, `.geminiignore`, and `.gitignore`.
- **Targeted reads**: Use specific line ranges when inspecting code. Do not dump entire packages into context.
- **Build Verification**: Run with JDK 21 using:
  ```powershell
  $env:JAVA_HOME = "C:\Users\lucci\AppData\Roaming\PrismLauncher\java\java-runtime-delta"; .\mvnw.cmd -B clean package
  ```

## Codebase Map
Relative to `src/main/java/io/github/lino9999/pluginshider/`:

| Component | Primary File(s) | Role |
| :--- | :--- | :--- |
| **Plugin Entry Point** | `PluginsHiderPlugin.java` | Extends `JavaPlugin`, coordinates lifecycle, PacketEvents registration, service hooks, Folia scheduler compatibility. |
| **Configuration Model** | `config/PluginConfig.java`, `config/GroupConfig.java`, `config/RateLimitConfig.java` | Strongly typed configuration manager, loads YAML settings, compiles regex patterns, parses custom command lists. |
| **Group & ACL Engine** | `acl/AccessGroup.java`, `acl/GroupManager.java`, `acl/RuleDecision.java` | Evaluates permission hierarchies, inheritance, command allowlists/denylists, regex filters, and namespace matching. |
| **Command Interception** | `listener/CommandExecutionListener.java`, `listener/TabCompletionListener.java` | Blocks disallowed commands at runtime, normalizes commands, enforces custom blocked messages, and cleans tab-complete suggestions. |
| **PacketEvents Interceptor** | `packet/PacketInterceptor.java`, `packet/BrandPacketModifier.java`, `packet/ChannelPacketModifier.java`, `packet/DeclareCommandsModifier.java` | Low-level packet manipulation for `DECLARE_COMMANDS`, server brand spoofing in F3, and blocking channel leak queries. |
| **Operator Protection** | `security/OperatorGuard.java`, `listener/OperatorSecurityListener.java` | Enforces authorized operator list, prevents rogue `/op` execution, and runs periodic security sweeps. |
| **Command Rate Limiter** | `security/CommandRateLimiter.java` | Sliding-window / token-bucket rate limiter stopping command spam and DoS attempts. |
| **Plugin Commands** | `command/PluginsHiderCommand.java` | Handles `/pluginshider` (`/phider`, `/ph`, `/plh`) administrative commands (`reload`, `check`, `sync`, `brand`, `help`). |
| **Integrations** | `integration/LuckPermsHook.java` | Listens to LuckPerms event bus for dynamic permission node updates to automatically sync client command trees. |

## Configuration & Permissions Overview
- **Command**: `/pluginshider` (aliases: `/phider`, `/ph`, `/plh`)
- **Main Admin Permission**: `pluginshider.admin`
- **Bypass Permissions**:
  - `pluginshider.bypass.commands`
  - `pluginshider.bypass.tab`
  - `pluginshider.bypass.plugins`
  - `pluginshider.bypass.channels`
  - `pluginshider.bypass.spam`
- **Group Assignment**: `pluginshider.group.<name>`
