# VillagerTradeReset 💎

## 📥 Available on Modrinth
<p align="left">
  <a href="https://modrinth.com/plugin/villagertradereset">
    <img src="https://img.shields.io/badge/Modrinth-Download-00af5c?style=for-the-badge&logo=modrinth&logoColor=white" alt="Modrinth">
  </a>
</p>
A lightweight Spigot/Paper plugin that allows players to quickly reset Villager professions and trades. Perfect for survival servers to reroll villager trades without the hassle of constantly breaking and replacing workstations.

## Overview

The VillagerTradeReset plugin enhances the villager trading experience by adding fast mechanics to reset trades. You can configure who can perform the reset and choose between text commands or direct in-game interactions.

## 📹 Demonstration
![Demostration](https://cdn.modrinth.com/data/2G0dafBL/images/1c6ab6094c5bb2bbab09c0870c1a363b4fed103a.gif)

## Features

* **Crouch & Click Interaction:** Reset any villager's trades instantly by crouching (sneaking) and right-clicking them.
* **Target Command Reset:** Use the main command while looking directly at a villager to wipe and reset their trades.
* **Admin Controls:** Easily toggle the plugin status or reload configurations via a dedicated admin command.

## Languages

The plugin automatically generates language files and supports multiple languages out of the box. You can easily switch between them by changing the `language` option in your `config.yml` file:

# Available languages:
# English -> en_US
# Portuguese (Brasil) -> pt_BR
# Portuguese (Portugal) -> pt_PT
# Spanish (Español) -> es_ES
# French (Français) -> fr_FR
# German (Deutsch) -> de_DE
# Italian (Italiano) -> it_IT
# Russian (Русский) -> ru_RU
# Chinese (简体中文) -> zh_CN
# Japanese (日本語) -> ja_JP
# Korean (한국어) -> ko_KR
language: en_US

After changing the language code, just run /vtr reload to apply the changes instantly in-game!

## Usage

### Commands

* `/resettrades` : Resets the trades of the villager you are looking at.
* `/vtr on|off|reload` : Administrative command to manage the plugin status and reload configs.

### Permissions

* `villagertradereset` : Allows players to use the `/resettrades` command and the sneak+right-click mechanic.
* `villagertradereset.admin` : Allows administrators to use the `/vtr` command.

## Support

If you encounter any bugs or have suggestions to improve the plugin, please open an issue on the tracker.
