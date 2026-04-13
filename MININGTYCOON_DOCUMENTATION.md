# MiningTycoon Plugin Documentation

**Version:** 2.0.5  
**Author:** XPaladiumyX  
**Website:** skyxnetwork.net  
**API Version:** 1.21

---

## 1. Overview

### Purpose

MiningTycoon is a complete Minecraft mining progression plugin that transforms the traditional mining experience into a full-fledged RPG-style game mode. Players progress through levels, earn currency, acquire powerful equipment (pickaxes, armor, pets), and can prestige to reset their progress for additional bonuses.

### Main Features

| Feature | Description |
|---------|-------------|
| **Level System** | 500 max levels with progressive XP requirements (10% increase per level) |
| **Currency System** | Earn coins while mining with support for Vault or custom commands |
| **Custom Equipment** | 17+ pickaxes, 12+ armor sets, 25+ pets with unique bonuses |
| **Prestige System** | Reset progress for permanent bonuses (Basic @ Lv.120, Elite @ Lv.150) |
| **Global Boosts** | Randomly activated XP/Coins multipliers with boss bar display |
| **AFK Rewards** | Passive income system in designated AFK zones |
| **Zone Restrictions** | Level-gated mining zones with WorldGuard support |
| **PlaceholderAPI** | Full placeholder support for scoreboards and GUIs |

---

## 2. Architecture Breakdown

### Package Structure

```
skyxnetwork.miningTycoon/
├── commands/          # Command executors and tab completion
├── data/              # Player data and storage management
├── gui/               # Inventory-based GUIs
├── listeners/         # Event handlers
├── managers/          # Business logic components
├── placeholders/      # PlaceholderAPI integration
├── tasks/             # Scheduled tasks
└── utils/             # Utility classes
```

### Class Overview

#### Main Plugin Class
- **MiningTycoon.java** (`skyxnetwork.miningTycoon.MiningTycoon`)
  - Singleton pattern for global access
  - Initializes all managers, listeners, commands, and tasks
  - Handles plugin enable/disable lifecycle
  - Provides getter methods for all managers

#### Data Layer
| Class | Role |
|-------|------|
| `PlayerData` | POJO holding player stats (level, XP, prestige, mode, AFK status) |
| `DataStorage` | YAML-based file storage in `playerdata/` folder |

#### Managers
| Class | Purpose |
|-------|---------|
| `PlayerDataManager` | Manages in-memory player data cache, XP adding, level-up checking |
| `ItemManager` | Loads and manages custom items (pickaxes, armor, pets) from YAML configs |
| `EconomyManager` | Handles money integration (Vault API or custom commands) |
| `BoostManager` | Manages global boost timers, multipliers, boss bar display |
| `ZoneManager` | Stores zone level requirements for access control |
| `PrestigeManager` | Handles prestige logic, rewards, display formatting |
| `PrestigePortalManager` | Manages prestige portal definitions and configurations |
| `WorldGuardManager` | Optional WorldGuard region integration for zone detection |

#### Listeners (Events)
| Listener | Events Handled |
|----------|---------------|
| `BlockBreakListener` | Block breaking, XP/coin calculation, tool/pet/armor bonuses |
| `PlayerJoinListener` | Player login/quit, data loading/saving |
| `PlayerMoveListener` | Zone access enforcement, player pushing |
| `InventoryClickListener` | GUI interactions |
| `DropListener` | (Not fully detailed - likely block drop handling) |
| `AFKListener` | AFK zone detection framework |
| `BlockPlaceListener` | Block placement restrictions |
| `PrestigePortalListener` | Prestige portal entry detection |
| `PortalListener` | General portal events |
| `AdminGUINew` | Admin GUI event handling |

#### Tasks (Scheduled Jobs)
| Task | Interval | Purpose |
|------|----------|---------|
| `LevelCheckTask` | 1 second | Check and process level-ups |
| `AFKRewardTask` | 1 tick | Grant AFK rewards in AFK zone |
| `NightVisionTask` | 5 seconds | Apply night vision to players |

#### GUI Classes
| Class | Purpose |
|-------|---------|
| `AdminGUINew` | Admin management interface |
| `PrestigePortalGUI` | Prestige selection interface |

#### Utilities
| Class | Description |
|-------|-------------|
| `ConfigUtil` | Configuration loading helpers |
| `ItemBuilder` | Fluent API for creating custom items with HDB support |
| `ColorUtil` | Color code translation (`&` to §) |
| `NumberFormatter` | Number formatting (large numbers display) |
| `ActionBarUtil` | Send action bar messages to players |

---

## 3. Complete Feature Documentation

### 3.1 Level System

- **Max Level:** 500 (configurable in `config.yml`)
- **XP Formula:** Base 100 XP, multiplied by 1.1 per level
- **Block Rewards:** Each block type has base XP/money based on level requirement
- **Level Display:** Various color-coded formats based on level ranges

### 3.2 Item System

#### Pickaxes (`items/pickaxes.yml`)

| ID | Material | Rarity | Exp Bonus | Money Bonus | Special |
|----|-----------|--------|------------|-------------|---------|
| `wooden_pickaxe` | Wood | Basic | 0 | 0 | - |
| `stone_pickaxe` | Stone | Basic | 3 | 1 | - |
| `reinforced_pickaxe` | Stone | Common | 7 | 3 | Efficiency I |
| `rockshredder_pickaxe` | Stone | Common | 15 | 8 | Efficiency II |
| `stone_crusher` | Stone | Rare | 40 | 23 | Efficiency III, Lucky Miner I |
| `iron_pickaxe` | Iron | Rare | 92 | 53 | Lucky Miner II |
| `tempered_edge` | Iron | Rare | 128 | 86 | Efficiency I, Lucky Miner II |
| `ore_splitter` | Iron | Rare | 376 | 191 | Efficiency II, Lucky Miner II |
| `iron_storm` | Iron | Rare | 720 | 346 | Efficiency III, Haste II, Lucky Miner II |
| `diamond_pickaxe` | Diamond | Epic | 1656 | 742 | Efficiency I, Haste III, Lucky Miner III |
| `crystal_cutter` | Diamond | Epic | 3704 | 1506 | Efficiency II, Haste III, Lucky Miner III |
| `shardpiercer` | Diamond | Epic | 7159 | 3458 | Efficiency III, Haste III, Lucky Miner III |
| `gemreaper` | Diamond | Epic | 12406 | 5846 | Efficiency IV, Haste III, Lucky Miner III |
| `aetherpick` | Diamond | Epic | 19750 | 14987 | Efficiency IV, Haste III, Lucky Miner III |

#### Armor (`items/armor.yml`)

| Set | Rarity | Exp Bonus | Money Bonus |
|-----|--------|-----------|-------------|
| Miner (3 pieces) | Basic | 5-11 | 1-6 |
| Prospector (4 pieces) | Basic | 7-30 | 3-18 |
| Quarry (4 pieces) | Common | 12-64 | 8-45 |
| Eternal Warden (4 pieces) | Legendary | 33333 | 33333 |

#### Pets (`items/pets.yml`)
Pets are worn as helmets and provide passive bonuses. 25+ pets ranging from Basic to Legendary rarity with bonuses up to 20,000 XP and 17,900 coins.

### 3.3 Prestige System

**Basic Prestige:**
- Requirement: Level 120
- Rewards: 1 Diamond, 10,000 coins, 1 Zentium

**Elite Prestige:**
- Requirement: Level 150
- Rewards: 1 Diamond, 10,000 coins

### 3.4 Global Boosts

| Type | Multiplier | Duration |
|------|------------|----------|
| EXP | 1.0-3.0x (3.5x super rare) | 60-300s (3600s super rare) |
| Coins | 2.0x | 60-300s |
| Both | 1.5x EXP, 2.0x Coins | 60-300s |

- Broadcasts to all players
- Displays boss bar with countdown
- Super rare: 0.5% chance for 3.5x multiplier

### 3.5 Commands

#### Player Commands

| Command | Usage | Description |
|---------|-------|-------------|
| `/level` | `/level` | Display current level and XP |
| `/prestige` | `/prestige [confirm]` | Initiate prestige (requires level 120+) |
| `/afk` | `/afk` | Teleport to AFK zone |
| `/fasttp` | `/fasttp zone <1-18>` | Fast teleport to zones |
| `/droptoggle` | `/droptoggle` | Toggle drop messages |
| `/booststatus` | `/booststatus` | Check global boost status |
| `/mode` | `/mode` | Toggle staff mode (no rewards) |
| `/index` | `/index` | Show command list |
| `/lobby` | `/lobby` | Return to lobby |

#### Admin Commands

| Command | Usage | Permission |
|---------|-------|------------|
| `/admin` | `/admin` | `miningtycoon.admin` |
| `/giveitem` | `/giveitem <pickaxe_id> <player>` | `miningtycoon.admin` |
| `/givearmor` | `/givearmor <armor_id> <player>` | `miningtycoon.admin` |
| `/givepet` | `/givepet <pet_id> <player>` | `miningtycoon.admin` |
| `/leveladmin` | `/leveladmin <set/reset> <player> [level]` | `miningtycoon.level.admin` |
| `/prestigeadmin` | `/prestigeadmin <set/reset> <player> [prestige]` | `miningtycoon.prestige.admin` |
| `/permconfig` | `/permconfig <add/remove/check> <player> <perm>` | `miningtycoon.permhandle` |
| `/miningtycoon` | `/miningtycoon <reload/save/help/version>` | `miningtycoon.admin` |

### 3.6 Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `miningtycoon.admin` | Full admin access | OP |
| `miningtycoon.level.admin` | Level management | OP |
| `miningtycoon.prestige.admin` | Prestige management | OP |
| `miningtycoon.permhandle` | Permission handling | OP |
| `antiblock.bypass` | Bypass block protection | OP |

### 3.7 Configuration Options

#### General Settings (`config.yml`)

```yaml
settings:
  world-name: "mining_tycoon"      # Mining world name
  max-level: 500                   # Maximum player level
  level-up-multiplier: 1.1        # XP requirement multiplier per level
```

#### Economy Settings

```yaml
economy:
  use-commands: true              # Use command-based economy
  command: "coins give %player% %amount%"  # Currency command
  currency-symbol: "⛁"           # Currency display symbol
```

#### AFK Zone Settings

```yaml
afk:
  enabled: true
  location:
    x: 9
    y: 125
    z: 19
  rewards:
    exp-per-tick: 1
    money-per-tick: 1
```

#### Zone Requirements

```yaml
zones:
  push-delay: 5
  max-pushes: 3
  requirements:
    2: 5
    3: 10
    4: 26
    # ... up to zone 18 requiring level 500
```

#### Database Settings

```yaml
database:
  type: "yaml"  # or "mysql"
  mysql:
    host: "localhost"
    port: 3306
    database: "miningtycoon"
    username: "root"
    password: "password"
```

---

## 4. Technical Details

### API Usage

| API | Version | Purpose |
|-----|---------|---------|
| **Paper API** | 1.21.4 | Core server API |
| **Adventure** | (via Paper) | Message styling |
| **PlaceholderAPI** | 2.11.5 | Placeholder expansion |
| **WorldGuard** | 7.0.9 | Region protection |
| **Vault** | 1.7 | Economy abstraction |
| **HeadDatabase API** | 1.0 | Custom skull textures |

### Data Storage

- **Primary:** YAML files in `plugins/MiningTycoon/playerdata/`
- **Format:** `{uuid}.yml` with keys: `level`, `experience`, `experienceNeeded`, `prestige`, `dropMessagesEnabled`, `playerMode`, `afkTime`
- **MySQL:** Optional, configured in `config.yml`

### Performance Considerations

1. **Lazy Loading:** Player data loaded on join only
2. **Item Caching:** All custom items cached in memory by ItemManager
3. **Scheduled Tasks:** Efficient tick-based reward system
4. **Boss Bar:** Single shared BossBar for boosts (not per-player)

### Error Handling

- File I/O errors logged with stack traces
- Invalid item configs logged and skipped
- Economy failures logged as warnings
- Default values used for missing config options

---

## 5. Installation & Setup

### Requirements

- **Server:** Paper 1.21.4+ (or Spigot with Adventure API)
- **Java:** JDK 21
- **Optional Plugins:** PlaceholderAPI, Vault, WorldGuard, HeadDatabase

### Installation Steps

1. **Build the plugin:**
   ```bash
   mvn clean package
   ```

2. **Install the JAR:**
   ```
   Copy target/MiningTycoon-2.0.5.jar to plugins/
   ```

3. **Start server** - Config files will be generated

4. **Configure** - Edit `config.yml` with your settings

5. **Add items** - Edit `items/pickaxes.yml`, `items/armor.yml`, `items/pets.yml`

6. **Restart** or use `/miningtycoon reload`

### World Setup

1. Create world: `mining_tycoon` (or configure in config.yml)
2. Set up WorldGuard regions for zones (optional)
3. Configure AFK zone coordinates in config

---

## 6. Support & Compatibility

### Supported Versions

| Version | Support |
|---------|---------|
| 1.21.4 | Full |
| 1.21.3 | Full |
| 1.21.2 | Full |
| 1.21.1 | Full |
| 1.21 | Full |

### Supported Server Types

- **Paper** (Recommended)
- **Spigot** (Requires Adventure API)
- **Purpur** (Fully compatible)

### Known Limitations

1. No GUI for item creation (manual YAML editing required)
2. Zone system requires WorldGuard for full functionality
3. AFK rewards only in designated coordinates (8-11, 108, 18-21)
4. Currency command requires compatible economy plugin

### Future Improvements

- GUI-based item editor
- More configurable reward formulas
- Database optimization for large player bases
- Additional prestige tiers

---

## 7. Developer Notes

### Public API (via MiningTycoon.getInstance())

```java
// Player Data Access
getPlayerDataManager().getPlayerData(player)
getPlayerDataManager().addExperience(player, amount)

// Item System
getItemManager().getPickaxe(id)
getItemManager().getArmor(id)
getItemManager().getPet(id)

// Economy
getEconomyManager().giveMoney(player, amount)
getEconomyManager().getBalance(player)
getEconomyManager().isEnabled()

// Boosts
getBoostManager().isBoostActive()
getBoostManager().getExpMultiplier()
getBoostManager().getCoinsMultiplier()

// Prestige
getPrestigeManager().canPrestige(player, zone)
getPrestigeManager().performPrestige(player, zone)
```

### PlaceholderAPI Integration

```
%miningtycoon_level_current%      - Current level
%miningtycoon_exp_display%        - XP display (e.g., "150/500✦")
%miningtycoon_exp_progress%       - Progress bar
%miningtycoon_exp_percent%       - XP percentage
%miningtycoon_prestige_current%   - Prestige level
%miningtycoon_prestige_display%   - Prestige display
%miningtycoon_level_display%     - Level display with color
```

### Hooking Into the Plugin

Developers can access all public methods through the singleton instance:

```java
import skyxnetwork.miningTycoon.MiningTycoon;

MiningTycoon plugin = MiningTycoon.getInstance();
// Access managers, items, data, etc.
```

---

## 8. File Structure

```
MiningTycoon/
├── pom.xml                    # Maven build configuration
├── src/main/
│   ├── java/skyxnetwork/miningTycoon/
│   │   ├── MiningTycoon.java          # Main class
│   │   ├── commands/                  # Command executors (15 files)
│   │   ├── data/                       # Data classes
│   │   ├── gui/                        # GUI handlers
│   │   ├── listeners/                 # Event listeners (10 files)
│   │   ├── managers/                  # Business logic (8 files)
│   │   ├── placeholders/              # PlaceholderAPI
│   │   ├── tasks/                     # Scheduled tasks
│   │   └── utils/                     # Utilities
│   └── resources/
│       ├── plugin.yml                  # Plugin metadata
│       ├── config.yml                  # Main configuration
│       └── items/
│           ├── pickaxes.yml           # Pickaxe definitions
│           ├── armor.yml              # Armor definitions
│           └── pets.yml               # Pet definitions
└── target/
    └── MiningTycoon-2.0.5.jar         # Compiled plugin
```

---

*Generated for MiningTycoon v2.0.5*