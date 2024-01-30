# SHPD Toolkit

This repository is a fork of [Shattered Pixel Dungeon](https://github.com/00-Evan/shattered-pixel-dungeon) and contains code from [SHPD Seedfinder](https://github.com/Elektrochecker/shpd-seed-finder) which is a fork of [Alessiomarotta's SHPD Seedfinder](https://github.com/alessiomarotta/shpd-seed-finder).

# Installation
This application can be installed on Android by downloading and installing the appropriate `.apk` file from the [releases](https://github.com/Elektrochecker/shpd-toolkit/releases) tab. It can also be used on desktop computers by downloading the `.jar` executable from the [releases](https://github.com/Elektrochecker/shpd-toolkit/releases) tab. In order to run the application on desktop, Java must be installed. If you intend to use this application on a desktop computer, consider using [the original seedfinder CLI tool](https://github.com/Elektrochecker/shpd-seed-finder) instead. It is faster, more powerful and will receive updates sooner.

As different versions of Shattered Pixel Dungeon (SHPD) generate different dungeons, a release of SHPD Toolkit will only work with certain versions of SHPD. Versions that only differ by the last digit in the version code (for example v2.3.0 and v2.3.1) are likeley, but not guaranteed to share the same level generation.

# Usage

### scouting mode
Scouting mode can be used to gain information about a given dungeon seed. The maximum depth of the search can be changed in the seedfinder settings. By changing the logging options, the item categories to be shown can be changed. On smaller devices it may be necessary to disable some of them in order to make the appearing window fit on the screen. The item log window is intentionally "sticky": in order to close it, press back on mobile or escape on desktop. Daily run scouting works similarly to regular seed scouting.

### seedfinding
Seedfinding mode is used to generate seeds to fit user specified criteria. When pressing on "Find Seed", the user will be prompted to enter a list of items for the seed to contain. This list needs to fulfill the same conditions as the item list from the original seedfinder:

- item names are the same as read in-game, including upgrade level
- all items must be written in lowercase letters
- each item must go on a new line

The following two examples are valid inputs and functionally equivalent:

```
ring of sharpshooting +2
alchemist's toolkit
```
```
sharpshooting +2
toolkit
```
The seedfinder can run in two different modes which can be changed in the settings:
- ANY mode: find seeds that contain any one of the specified items
- ALL mode: find seeds that contain all of the specified items

The max. depth slider in the settings controls until which floor the condition has to be met. Setting the slider to 4 and the mode to ALL will generate seeds that contain all of the specified items before floor 5.

Upon starting the seedfinder the app will loop through different seeds until it finds a fitting one. The app will appear frozen until a seed is found. When entering an invalid, impossible or sufficiently unlikeley combination of items, the app will lock up and has to be forcefully closed.

### Item catalog
The item catalog can be used to check/confirm the names of different items. After finding or scouting a seed, the consumables in the catalog will have the types of the ones in this seed.

### Challenges
Some challenges such as "forbidden runes" change level generation. The challenges the seedfinder uses can be changed in the settings.

# Building
SHPD Toolkit can be compiled exactly like [Shattered Pixel Dungeon](https://github.com/00-Evan/shattered-pixel-dungeon).