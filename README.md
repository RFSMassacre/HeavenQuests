![HeavenQuests](HeavenQuests.png)

### Auto-generate eleven daily quests based on the Paper platform.
# Installation
## Dependencies
Ensure that both of these plugins are installed with HeavenQuests for it to work.
- [HeavenLibrary](https://modrinth.com/plugin/heavenlibrary)
- [CoinsEngine](https://hangar.papermc.io/NightExpress/CoinsEngine)

## Steps
- Download [HeavenQuests](https://modrinth.com/plugin/heavenquests) and the other dependencies.
- Shut down your server.
- Upload all three jars to the plugins folder of your server.
- Start the server and let the files generate.
- Go into the `config.yml` file and make sure the following is updated to the currencies you want to use.
```YAML
currency:
  prize: <currency>
  reroll: <currency>
```
- Edit the rest of the `config.yml` and `locale.yml` to your liking.
- Run the command `/questadmin reload` and use `/questadmin reroll <player>` to update the quests of that player. (Using no argument will do it to yourself.)

# Objectives
Objects are what dictate the action of the quest. Every player gets one random quest of each type every day.
- **KILL_MONSTER**
- **KILL_ANIMAL**
- **BREAK_BLOCK**
- **HARVEST**
- **SMELT**
- **COOK**
- **CRAFT**
- **TAME**
- **VISIT_BIOME**
- **FISH**
- **ENCHANT**

# Commands & Permissions
## Player Commands
- `/quest` **|** `heavenquests.quest`

Opens the daily quest menu.

## Admin Commands
- `/questadmin reload` **|** `heavenquests.questadmin.reload`

Reload the configuration files without having to reboot the server.

- `/questadmin reroll [player]` **|** `heavenquests.questadmin.reload`

Resets all of that player's daily quests.

# Configurations
- **config.yml**
```YAML
# In seconds.
daily-interval: 86400
completed-interval: 43200

# In ticks.
title-duration:
  fade-in: 20
  stay: 100
  fade-out: 20

objectives:
  kill_animal:
    min: 10
    max: 15
    prize: 45.0
  kill_monster:
    min: 16
    max: 32
    prize: 60.0
  break_block:
    min: 32
    max: 64
    prize: 16.0
  enchant:
    min: 3
    max: 9
    prize: 60.0
  fish:
    min: 6
    max: 10
    prize: 100.0
  visit_biome:
    min: 1
    max: 1
    prize: 750.0
  craft:
    min: 16
    max: 32
    prize: 10.0
  smelt:
    min: 32
    max: 64
    prize: 10.0
  cook:
    min: 32
    max: 64
    prize: 16.0
  tame:
    min: 8
    max: 16
    prize: 40.0
  harvest:
    min: 64
    max: 128
    prize: 10.0

multipliers:
  materials:
    ancient_debris: 6.0
    diamond: 7.0
    iron: 3.0
    gold: 5.0
    lapis_lazuli: 2.0
    wheat: 1.2
    chorus_fruit: 1.5
    carrot: 0.8
    axe: 3.0
    sword: 2.0
    helmet: 5.0
    chestplate: 8.0
    leggings: 7.0
    boots: 4.0
    book: 3.0
    quartz: 1.5
    chainmail: 4.0
  entities:
    vindicator: 2.0
    iron_golem: 5.0
    ravager: 5.0
    chicken: 0.5

# This blacklists based on if the material or entity contains a part of or all of the string in the blacklist.
blacklist:
  materials:
    - glazed
    - anvil
    - template
    - bedrock
    - stripped
    - barrier
    - structure
    - sign
    - rail
    - armor_stand
    - button
    - door
    - pressure_plate
    - glass_pane
    - spawn_egg
    - cartography_table
    - netherite
    - diamond
    - clock
    - command_block
    - crafter
    - stairs
    - trapdoor
    - crying_obsidian
    - hoe
    - sword
    - axe
    - leggings
    - shovel
    - disc
    - elytra
    - banner_pattern
    - ghast_tear
    - pickaxe
    - hay_block
    - hopper
    - jukebox
    - jigsaw
    - knowledge_book
    - lava
    - lead
    - shulker
    - ominous_trial_key
    - froglight
    - rail
    - armor_trim
    - void_air
    - vault
    - wet_sponge
    - bundle
    - dried_kelp
    - sniffer_egg
    - cracked
  entities:
    - elder_guardian
    - ender_dragon
    - wither
    - warden
    - creaking
    - breeze_wind_charge
    - ominous_item_spawner
    - breeze
    - end_crystal
    - evoker_fangs
    - eye_of_ender
    - giant
    - interaction
    - leash_knot
    - llama_spit
    - marker
    - minecart
    - player
    - shulker_bullet
    - stray
    - wither_skull
    - painting
    - item
    - fishing_bobber
    - experience_orb
    - area_effect_cloud
    - glow_item_frame
    - falling_block
    - boat
    - mule
    - donkey
    - skeleton_horse
    - zombie_horse
  custom-fishing:
    - book
    - stick
    - rod
    - diamond
    - fishfinder
    - flint
    - kelp
    - nether_brick
    - obsidian
    - shoes
    - stone
    - water_effect
    - lava_effect
    - bait
    - vanilla
    - seagrass
    - gold_ingot
    - gold_nugget
    - golden_star
    - silver_star
    - netherrack
    - hook
  biomes:
    - advanced
    - cave/
    - minecraft
    - nullscape

reroll-price: 200
single-price: 25

# Make sure these currency names are valid in CoinsEngine!
currency:
  prize: silver
  reroll: gold
```
- **locale.yml**
```YAML
prefix: "&d&lQuests &7&l|&r "

no-perm: "&cYou do not have permission for this command!"
no-data: "&cYour data could not be found!"
success: "&aYou have completed &f{quest}&a! You were rewarded &f{prize}&a!"
expired: "&7See the &dTaskmaster &7in your capital to see your new quests!"
reloaded: "&aThe plugin has been reloaded!"
player-not-found: "&f{player}&c was not found! Ensure the username is spelled correctly."
not-enough: "&cYou need &f{price}&c but only have &f{balance}&c!"

notify:
  progress:
    title: "&r"
    subtitle: "&7{quest}"
  complete:
    title: "&eDaily Complete!"
    subtitle: "&7You were rewarded &f{prize}&7!"

rerolled:
  menu: "&aYou have rerolled your daily quest for &f{price}&a! Now you have &f{balance}&a."
  single: "&aYou have rerolled &e{old}&a for &e{new}&a for &f{price}&a! Now you have
    &f{balance}&a."
  self: "&f{player}&a has rerolled their daily quests."
  target: "&eYour daily quests have been rerolled."
set:
  self: "&7You have set a new quest for &f{player}&7! &8(&e{quest}&8)"
  target: "&7You have a new quest! &8(&e{quest}&8)"
```