# MiningTycoon Plugin

A complete Minecraft mining progression plugin with a leveling system, prestige, pets, custom armor, and progressive zones.

## 🎮 Main Features

### Progression System
- **Levels**: Up to 500 levels with exponential progression
- **Prestige**: Prestige system with portals (Basic & Elite)
- **Experience**: XP and money earned from mining with multipliers

### Equipment
- **Tools**: 13 custom pickaxes with progressive bonuses
- **Armor**: 30+ armor sets with XP and money bonuses
- **Pets**: 25 equippable pets with passive bonuses

### Zones & Restrictions
- **18 zones** with level requirements (5 to 500)
- Fast teleportation system between zones
- Anti-intrusion protection with pushback

### Special Systems
- **AFK Zone**: Passive XP and money gains
- **Community Generator**: Community generator with drops Rare
- **Global Boosts**: Server boosts for XP, money, or both
- **Zentium**: Rare premium currency

### Admin Interface
- Complete administration GUI
- Level, prestige, and item management
- Debug and monitoring system

## 📋 Commands

### Players
- `/level` - Display your statistics
- `/prestige [confirm]` - Prestige system
- `/afk` - Teleport to AFK zone
- `/lobby` - Return to lobby
- `/hub` - Teleport to hub
- `/droptoggle` - Enable/disable drop messages
- `/booststatus` - Global boost status
- `/index` - Command list

### Teleportation
- `/fasttp zone <1-18>` - Fast teleport to zones

### Administration
- `/admin` - GUI Administration
- `/leveladmin <set/reset> <player> [level]` - Level management
- `/prestigeadmin <set/reset> <player> [prestige]` - Prestige management
- `/giveitem <item> <player>` - Give a tool
- `/givearmor <armor> <player>` - Give armor
- `/mode` - Toggle staff mode (no rewards)

## 🔧 Installation

1. Download `MiningTycoon.jar`

2. Place in the `plugins/` folder

3. Restart the server
4. Configure `config.yml` if necessary

## 📊 Dependencies

- **Paper/Spigot**: 1.21.4+
- **Java**: 21+
- **Recommended Plugins**:

- Vault (economy)

- WorldGuard (regions) 
- PlaceholderAPI (placeholders)

## 🎯 Permissions

- `miningtycoon.admin` - Full admin access
- `miningtycoon.prestige.admin` - Prestige management
- `miningtycoon.level.admin` - Level management
- `antiblock.bypass` - Bypass block protection

## 🏆 Rarity System

### Tools
- **Basic**: Wooden, Stone Pickaxe
- **Common**: Reinforced, Rockshredder, Stone Crusher
- **Rare**: Iron, Tempered Edge, Ore Splitter, Iron Storm
- **Epic**: Diamond, Crystal Cutter, Shardpiercer, GemReaper, AetherPick

### Armor
- **Basic**: Miner Set, Prospector Set
- **Common**: Quarry Set → Saltstone Scraper Set
- **Rare**: Gold Hunter Set → Emerald Harvester Set
- **Epic**: Mythril Splitter Set → Starlight Guardian Set
- **Legendary**: Void Monarch → Eternal Warden Set

### Pets
- **Basic**: Rocky Mole → Pebble Slime
- **Common**: Iron Snail → Glow Worm
- **Rare**: Silver Griffin → Emerald Fox
- **Epic**: Drill Core → Lava Serpent
- **Legendary**: Titanium Dragon → Galactic Golem

## 📈 Progression

1. **Start**: Starter Pickaxe + basic equipment
2. **Levels 1-100**: Unlock zones 1-9
3. **Level 120**: First Prestige available
4. **Level 150+**: Elite Prestige and advanced zones
5. **Level 500**: Maximum level, focus on prestige

## 🎁 Rewards

- **Prestige Basic**: 1 Diamond, 10,000 coins, 1 Zentium
- **Prestige Elite**: 1 Diamond, 10,000 coins
- **Community Generator**: Rare drops, global boosts, armor

## 🔄 Global Boosts

- **EXP Boost**: x1.5 to x3.5 (variable duration)
- **Coin Boost**: x2.0 (variable duration)
- **Combined**: x1.5 XP + x2.0 coins

## 💾 Storage

All data is saved in `plugins/MiningTycoon/playerdata/`

## 📜 License

© 2026 SkyXNetwork - All rights reserved
Developed with ❤️ XPaladiumyX

---

