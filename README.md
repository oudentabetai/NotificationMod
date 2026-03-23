# NotificationMod

A Minecraft 1.12.2 Forge client-side mod that sends **Windows notifications** (system-tray balloon alerts) for multiplayer events.

## Features

| Event | Notification |
|---|---|
| You connect to a server | "Connected to server" |
| You disconnect from a server | "Disconnected from server" |
| Another player joins the server | "Player Joined – `<name>` joined the server" |
| Another player leaves the server | "Player Left – `<name>` left the server" |
| A chat message is received | "Minecraft Chat – `<message>`" |

## Requirements

- Minecraft **1.12.2**
- Forge **14.23.5.2860** or later
- **Windows** (notifications use the Java AWT SystemTray API; other platforms will log a warning and skip notifications)

## Build

```
./gradlew build
```

The compiled jar is placed in `build/libs/`.

## Install

Copy the jar from `build/libs/` into your Minecraft `mods/` folder.

