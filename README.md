> [!Warning]
> This repository is a stub. While ByteCraft works, it only includes
> minimal syntax for interacting with the Bukkit server.

# ByteCraft

ByteCraft is a [ByteSkript](https://github.com/Moderocky/ByteSkript) library that provides [Paper](https://papermc.io/software/paper/) server integration capabilities.

## Prerequisites

- Java 21 or higher
- ByteSkript compiler
- Paper server

## Installation

1. Download [the latest bootstrap](http://maven.lhf.blue/api/maven/latest/file/snapshots/blue/lhf/bytecraft/bytecraft-bootstrap).
2. Move the bootstrap JAR to your ByteSkript project's `libraries/` directory.
3. Place your scripts in the project's `skript/` directory. You may use this as an example:
   ```applescript
    plugin:
        name: Test Plugin
        version: 1.0.0
    on load:
        trigger:
            print "Hello from Test!"
            print "The server is on version " + getBukkitVersion() of the server
    ```
4. Run ByteSkript as normal: `java -jar ByteSkript-<VERSION>.jar jar` (a total of three `jar`s)
5. Get your plugin from `compiled/CompiledScripts.jar` and place it in your server's `plugins/` folder
6. Restart your server
7. Enjoy!

## Build Instructions

You must have [git](https://git-scm.com/install/) and [Java 21](https://adoptium.net/temurin/releases/?version=21) or higher installed.

> [!Warning]
> Currently (2026-01-07), compilation using these steps does not work
> because ByteCraft depends on features which are yet unreleased
> in ByteSkript. To build using these instrcutions, you must first
> build and install Mirror 5.0.5 and ByteSkript 1.0.41 by compiling
> [my fork of Mirror](https://github.com/bluelhf/Mirror) and [my fork of ByteSkript](https://github.com/bluelhf/ByteSkript)
> using `mvn source:jar install`

1. Open the Terminal.
2. Clone the repository using `git clone https://github.com/bluelhf/bytecraft/`
3. Move to it using `cd bytecraft`
4. Build using `./mvnw package`
5. The Bytecraft JAR is now available in `bytecraft-bootstrap/target/` :tada:

## Project Structure

ByteCraft is split into three Maven submodules; an interface, library, and bootstrap.
- The library (`bytecraft-library/`) contains practically all useful code, and there is little
reason to look at any of the other directories.
- The bootstrap (`bytecraft-bootstrap/`) handles
loading ByteCraft's dependencies and ByteCraft itself onto the classpath when ByteSkript loads.
- The interface (`bytecraft-interface/`) is only there so that the library and bootstrap have a common
ground to use as the basis for service provider communication. At run-time, it is included as part of
the bootstrap.
