# RoleSystem — Hytale Server Plugin

A server-side plugin for Hytale that introduces a role-based gameplay system with custom death handling, medical items, and an admin UI for role management.

---

## Features

- **Role system** — assign `MEDIC` or `SOLDIER` roles to players, persisted across server restarts
- **Custom death screen** — replaces Hytale's native death UI with a countdown-based respawn page
- **Medical items** — role-aware healing consumables with progressive heal and revive mechanics
- **Medical bench restriction** — only MEDIC players can access the crafting bench
- **Admin UI** — in-game panel to view and manage player roles
- **In-place respawn** — players respawn at their exact death location, not the world spawn point

---

## Roles

| Role | Description |
|---|---|
| `MEDIC` | Can use all medical items, heal other players, revive dead players, and access the medical bench |
| `SOLDIER` | Can use Bandage on themselves only |
| *(none)* | Same as SOLDIER for item usage |

Roles are stored in `RoleSystem/players.json` next to the plugin JAR and persist between restarts.

---

## Items

### Bandage

Crafteable at the `medicalbench`.

| Role | Target | Heal amount | Duration |
|---|---|---|---|
| MEDIC | Aimed player or self | 20% max HP | 5 seconds |
| Non-MEDIC | Self only | 5% max HP | 10 seconds |

### MedicalKit

Crafteable at the `medicalbench`. **MEDIC only.**

| Target state | Effect | Duration |
|---|---|---|
| Alive (self or other) | Heals to 100% max HP progressively | 20 seconds |
| Dead | Revives the player — 10% HP instantly, then 65% over 10 seconds with reduced movement speed | 20 seconds |

#### General healing behavior

- If the target already has full HP, the item is returned and a warning is shown before consuming.
- Only one active heal per player at a time.
- Healing stops automatically if the target reaches 100% HP before the timer ends.
- A chat message shows who is being healed as soon as consumption starts.

---

## Death System

When a player dies, the native Hytale death screen is suppressed and replaced with a custom **DiePage**:

- A **20-second countdown** must complete before the Respawn button becomes available.
- On respawn, the player reappears at their **exact death position** — no teleport to world spawn.
- If a player disconnects while dead and reconnects, the death screen is restored automatically.

---

## Commands

### `/roles set --player <name> --role <role>`

Assigns a role to a player. The change is saved immediately to `players.json`.

```
/roles set --player Tenzinn --role MEDIC
```

### `/roles get [--role <role>]`

Opens an in-game admin panel listing all connected players with their name, status, and assigned role. Optionally filter by role.

```
/roles get
/roles get --role MEDIC
```

### `/respawn <player>`

Forces a dead player to respawn.

---

## Admin Panel

Accessible via `/roles get`. Displays all connected players with color-coded status and role indicators:

| Status | Color |
|---|---|
| ACTIVE | Green |
| UNDEFINED (no role) | Yellow |
| Inactive | Gray |

| Role | Color |
|---|---|
| MEDIC | Blue |
| SOLDIER | Red |
| None | Gray |

---

## Project Structure

```
src/
├── Tenzinn/
│   ├── RoleSystem.java               # Plugin main class
│   ├── Commands/
│   │   ├── RespawnCommand.java
│   │   └── Roles/
│   │       ├── RoleCommands.java
│   │       ├── GetRoleCommand.java
│   │       └── SetRoleCommand.java
│   ├── Events/
│   │   ├── DetectDiePlayer.java      # Custom death interception
│   │   ├── DetectRejoinDead.java     # Restore death UI on reconnect
│   │   ├── DetectPlayerReady.java
│   │   ├── DetectPlayerDisconnect.java
│   │   └── DetectHealingItemUse.java # Pre-consume validation
│   ├── Interactions/
│   │   ├── UseHealingItemInteraction.java
│   │   └── DetectBenchInteract.java
│   ├── Items/
│   │   ├── HealingItem.java          # Enum with per-role config
│   │   └── HealingStaffSystem.java   # Server-side ray cast
│   ├── JSON/
│   │   └── Roles.java                # JSON persistence
│   ├── Systems/
│   │   ├── RoleController.java
│   │   └── CurrentPositionRespawnController.java
│   └── UI/
│       ├── DiePage.java
│       ├── RoleListAdminPage.java
│       └── Events/
│           └── BasicEvent.java
```

---

## Requirements

- Hytale Server (Early Access)
- Java 25
- Gson (for JSON persistence)

---

## Configuration

Player roles are stored in:

```
<server>/mods/RoleSystem/players.json
```

Format:

```json
{
  "players": {
    "PlayerName": "MEDIC",
    "OtherPlayer": "SOLDIER"
  }
}
```

Roles can be assigned in-game via `/roles set` or edited directly in the file (requires server restart to reload).

---

## License

MIT