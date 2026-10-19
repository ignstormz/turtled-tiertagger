# Turtle TierTagger — StormDK (Minecraft 1.21.11)

This is a fork of **[mctiers-dev/TierTagger](https://github.com/mctiers-dev/TierTagger)**, built from the upstream **`1.21.11` branch**. Original mod by uku and netiyiy. Distributed under the original **Mozilla Public License 2.0**; see `LICENSE`. Retain this attribution and make the modified source available when distributing the compiled mod.

Maintained by [StormDK](https://modrinth.com/user/StormDK). The original TierTagger attribution and MPL-2.0 licensing remain in this source distribution.

The upstream UI, nametag, player-list, player-search, player-profile, configuration screen, and gamemode cycling are preserved, with TurtleTiers-specific changes.

## TurtleTiers integration

- Data: `https://turtletiers-api.onrender.com/api/players` (JSON with `players[]`, `uuid`, `name`, `rankings`, `overallElo`).
- Website: https://turtletiers-api.onrender.com/rankings/overall
- Modes: Vanilla, UHC, Pot, Neth OP, SMP, Sword, Axe, Mace, Spear Mace, Cart, Creeper, Diamond SMP.
- Supports **all 15 tiers**: HT1/MT1/LT1 through HT5/MT5/LT5. Highest-tier fallback respects H > M > L within each number.
- Colour configuration is ordered HT1, MT1, LT1, then HT2, MT2, LT2, through HT5, MT5, LT5, with the original TurtleTiers palette preserved.
- Player profiles omit region and retired status and show `ELO Placement: <placement>` without an estimated suffix.
- The uploaded `3rmheCJ.png` turtle art is used as the mod icon and the TurtleTiers picker icon.
- Uses the **exact website tab icons**, converted from their public emoji URLs to PNG and bundled as Minecraft bitmap font resources.
- Downloads one public rankings snapshot at startup, refreshes every five minutes, and keeps the last successful snapshot when the API is unavailable. Player nametags do not make network requests.
- Overall Elo is used in the player profile; placement is calculated by sorting returned overall Elo values, not guaranteed identical to the website's full ranking rules.
- Player profile placement is labelled `ELO Placement: #N`. Hovering a gamemode rank shows only `Gamemode: <name>` and `ELO: <value>` from that player's API `elos` map (or `N/A` when absent).
- The Mod Menu Website button links to StormDK's Modrinth profile.
- The mod is client-side only. Minecraft Java **1.21.11**, Fabric Loader, and the listed Ukulib dependency are required.

## Build

1. Install Java 21.
2. In this project folder, run `./gradlew build` (`gradlew.bat build` on Windows).
3. Put the built **TurtleTiersTagger** jar from `build/libs` in your Minecraft `mods` folder alongside the compatible Fabric Loader, Fabric API (as applicable), and Ukulib.

This source package has **not been compiled or in-game tested** in the current environment. It is a source handoff, not an installable mod JAR yet.

## Notes

- The previous original TierTagger MCTiers/SubTiers tierlist artwork is **excluded** from this fork, in line with the upstream project notice that those icons were licensed only for the original mod.
- The TurtleTiers icons are sourced from the site's public `KIT_ICONS` table. Confirm redistribution rights for the Discord-hosted emoji assets before public release.
- The original mod's Modrinth update prompt was removed: it would otherwise point users to a different mod build.
- Mod id remains `tier-tagger` to preserve upstream integration and settings internals. The archive, display name, and target API identify this as a TurtleTiers fork.
