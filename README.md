# ParagonBot

A self-hosted Discord community bot for the **Paragon Network** Minecraft server, built on
[JDA 6](https://jda.wiki/) with SQLite persistence and a YAML configuration file.

It handles the day-to-day operation of a single guild: tickets, moderation with timed
punishments, automod, invite tracking, a counting game, a media/partner programme, staff
lists, logging and a set of community auto-responses.

---

## Features

| Area | What it does |
| --- | --- |
| **Ticket system** | `/ticket-setup` panel, button-driven ticket creation, claim, type selection and closing, `/rename-ticket`, staff ping guard, ticket blacklisting (`/blacklist-ticket`, `/remove-blacklist-ticket`) and an auto-close bot: silent tickets are announced and auto-closed after 6 h of inactivity, unanswered tickets ping staff every 6 h (`!autoClose` toggles it per ticket) |
| **Moderation** | `/ban-set`, `/change-ban`, `/unban`, `/unban-force`, `/mute-set`, `/unmute`. Punishments with a duration are stored in SQLite and released automatically by background jobs (`autoUnban`, `autoUnmute`, `autoUnblacklistTicket`) |
| **Automod** | Swear filter with obfuscation-resistant regex (`a@4`, `s$5`, `t7`, …), caps filter, link filter, spam detection (rate + duplicate messages), attachment-only channels, blocklisted-word auto-delete, plus channel/category/role bypass rules |
| **Counting game** | Per-channel counting with persisted state, `!count` admin commands, and a DB-backed channel registry |
| **Invite tracking** | Caches guild invites every 10 s, attributes joins to an invite, logs them to the invite log channel, and answers `!invites` / `!invitedby` (staff only) from the `invites` table |
| **Media programme** | `/media <link>` posts a YouTube/TikTok video to the media channel, `/media-rank` grants a timed content-creator/partner rank, `/media-blacklist`, `/media-unblacklist`, expired ranks are stripped automatically every 10 min and logged |
| **Logging** | Message delete/edit logging and member join/leave logging into the configured log channels |
| **Staff list** | `/stafflist-create` builds a staff embed from configured staff roles; refreshed automatically every 24 h |
| **Community** | Welcome embed on join, `/vote` + `!vote` vote links, `/guide` (Components V2 guide), `/embed-info`, daily vote reminder, bi-daily partner shoutout, rotating presence |
| **Auto-responses** | `!ip` / `ip?`, `!store`, `!setAvatar` (owner, attached image), `!reload` (hot-reload config) |
| **Maintenance** | Hourly SQLite backups DM'd to the bot owner, `!reload` for config, startup validation of every configured channel/role ID |

---

## Tech stack

* **Java** (Gradle 8.13 wrapper, Shadow plugin → fat JAR)
* **[JDA 6.2.0](https://jda.wiki/)** — Discord gateway, slash commands, Components V2
* **SQLite** (`sqlite-jdbc`) — three database files, schema created on startup
* **SnakeYAML** — `config.yml` parsing
* **Gson** (declared, not referenced yet) / **org.json** — JSON payloads
* **JUnit 5** — test harness (no tests are checked in yet)

Gateway intents enabled: `GUILD_MEMBERS`, `MESSAGE_CONTENT`, `GUILD_MESSAGES`,
`GUILD_VOICE_STATES`, `GUILD_INVITES`.

---

## Requirements

* **JDK 21** — the code uses `List.getFirst()` (Java 21+), and Gradle 8.13 cannot run on
  some newer JDKs (verified broken on JDK 25 with `Type T not present`)
* A Discord application + bot token
* The bot invited to **one guild**, with the intents above enabled in the Developer Portal

---

## Getting started

```bash
# 1. Build the fat JAR (produces build/libs/ParagonBot.jar)
./gradlew shadowJar          # on Windows: gradlew.bat shadowJar

# 2. Run it from an empty working directory — config.yml and the DBs are created there
java -jar build/libs/ParagonBot.jar
```

On first run the bot writes a default `config.yml` with example values — replace them with
your token, guild/role/channel IDs and database paths before starting.

### Configuration (`config.yml`)

The file is git-ignored and lives in the working directory. IDs are resolved once at
startup; missing values are reported as `[CONFIG ERROR] …` on the console.

```yaml
bot:
  id: "your-bot-token"

Guild:
  guildID: "123456789012345678"

dreggcake:
  id: "123456789012345678"      # bot owner — receives DB backups via DM

database:
  main: "database.db"
  counting: "counting.db"
  media: "media.db"

ChannelsID:
  welcomeID: "..."
  punishmentLogID: "..."
  memberLogID: "..."
  messagesLogID: "..."
  inviteLogID: "..."
  ticketLogID: "..."
  mediaLogID: "..."
  ticketID: "..."
  staffListChannelID: "..."
  supportChannelID: "..."
  doNotDeleteID: "..."
  voteReminderID: "..."

CategoryID:
  ticketCategoryID: "..."
  staffLogsId: "..."

autoModBypassChannelsID:
  countingID: "..."
  spammingID: "..."

staffRoleID:        # owner, dev, manager, srAdmin, jrAdmin, srMod, jrMod,
                    # support, staff, bossDesigner, builder, mediaManager
  ownerRoleID: "..."
  # ...

punishRoleID:
  mutedRoleID: "..."
  ticketBlacklistRoleID: "..."

gamemodeRoleID:     # survival, lifesteal, earth, prison, cobblemon, minigames
  survivalRoleID: "..."
  # ...

media:
  partnerRoleID: "..."
  cc1RoleID: "..."               # cc1 … cc4 content-creator roles
  channels:
    mediaUploadID: "..."

Partner:
  queenChannel: "..."

messages:
  ip: "play.paragonnetwork.us"
  store: "https://www.paragonnetwork.us/"
  voteLinks:
    - "https://example.com/vote"
```

`!reload` (restricted via `PermissionUtility`) re-reads `config.yml` at runtime.

> **Note:** `swear/en.json` is read from the working directory (`./swear/en.json`),
> not from the JAR. Copy it out of `src/main/resources/swear/en.json` next to the JAR
> if the swear filter should load.

---

## Commands

### Slash commands

Registered per guild on startup by `CommandManager`.

| Command | Description |
| --- | --- |
| `/ticket-setup` | Post the ticket panel |
| `/blacklist-ticket` · `/remove-blacklist-ticket` · `/rename-ticket` | Ticket moderation |
| `/ban-set` · `/change-ban` · `/unban` · `/unban-force` | Bans |
| `/mute-set` · `/unmute` | Timed mutes |
| `/media` · `/media-rank` · `/media-blacklist` · `/media-unblacklist` | Media programme |
| `/stafflist-create` | Build the staff-list embed |
| `/embed-info` | Post the server info embed |
| `/guide` | Interactive server guide |
| `/vote` | Vote links + buttons |

### Text commands

| Trigger | Effect |
| --- | --- |
| `!vote` | Vote embed (text variant of `/vote`) |
| `!ip` / `ip?` | Server IP auto-response |
| `!store` | Store link |
| `!setAvatar` | Sets the bot avatar from an attached image (owner/guild owner only) |
| `!reload` | Hot-reload `config.yml` (staff only) |
| `!count enable` / `!count disable` / `!count set <n>` | Counting-channel administration (`MANAGE_CHANNEL`) |
| `!invites [user]` · `!invitedby [user]` | Invite stats / inviter lookup (staff only) |
| `!autoClose` | Toggle ticket auto-close for the current ticket |

---

## Database

Three SQLite files (paths configurable under `database.*`), schema created idempotently
on startup by `DatabaseBootstrap`:

| File | Tables |
| --- | --- |
| `database.db` (main) | `mutes`, `bans`, `blacklistTicket`, `tickets`, `invites`, `staff_list`, `ticket_autoclose` |
| `counting.db` | `counting_channels` |
| `media.db` | `media_ranks`, `media_blacklists` |

`*.db` files are git-ignored. Hourly backups of `database.db` and `counting.db` are DM'd
to the configured owner.

---

## Project layout

```
src/main/java/com/dreggcake/src/
├── Main.java                 # entry point: builds JDA, registers listeners/commands
├── BotStartupListener.java   # onReady: DB init, resolve roles/channels, start schedulers
├── RecurringTasks.java       # all background jobs (backups, auto-unban, refreshers, …)
├── CommandManager.java       # slash command registry + dispatch
├── AutoModService/           # swear, caps, link, spam, attachments, bypass rules
├── Counting/                 # counting game
├── Database/                 # DAO, schemas, bootstrap, backups
├── Embeds/                   # /embed-info
├── InviteTracker/            # invite cache, join attribution, logs
├── Logging/                  # message + member logging
├── Media/                    # media ranks, blacklists, uploads
├── Miscellaneous/            # welcome, vote, partner, guide, !ip/!store
├── Moderation/               # bans, mutes, timed releases
├── StaffList/                # staff embed build + daily refresh
├── TicketSystem/             # panel, listeners, auto-close bot, blacklisting
├── Config/                   # YAML config load/save/reload
└── Utility/                  # Channels, Roles, GuildContext, permissions, config resolver
src/main/resources/
├── swear/en.json             # swear word list
└── serverLogo.png            # bundled image asset
```

---

## Building & running tests

```bash
./gradlew build          # compiles + runs tests (JUnit 5)
./gradlew shadowJar      # build/libs/ParagonBot.jar
```

If `./gradlew` is not executable in your checkout, run it as `bash gradlew …`.
