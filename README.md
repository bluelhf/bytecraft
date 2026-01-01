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

1. Download [the latest bootstrap](http://maven.lhf.blue/api/maven/latest/file/snapshots/blue/lhf/bytecraft/bytecraft-bootstrap) or build it yourself using Maven `mvn package`
   - You can also build it yourself by downloading this repository as a .zip, extracting it and running `./mvnw package` in the terminal. It'll be available in `bytecraft-bootstrap/target/`.
3. Move the bootstrap JAR to your ByteSkript project's `libraries/` directory
4. Place your scripts in the project's `skript/` directory. You may use this as an example:
   ```applescript
    plugin:
        name: Test Plugin
        version: 1.0.0
    on load:
        trigger:
            print "Hello from Test!"
            print "The server is on version " + getBukkitVersion() of the server
    ```
5. Run ByteSkript as normal: `java -jar ByteSkript-<VERSION>.jar jar` (a total of three `jar`s)
6. Get your plugin from `compiled/CompiledScripts.jar` and place it in your server's `plugins/` folder
7. Restart your server
8. Enjoy!

## Project Structure

ByteCraft is split into three Maven submodules; an interface, library, and bootstrap.
- The library (`bytecraft-library/`) contains practically all useful code, and there is little
reason to look at any of the other directories.
- The bootstrap (`bytecraft-bootstrap/`) handles
loading ByteCraft's dependencies and ByteCraft itself onto the classpath when ByteSkript loads.
- The interface (`bytecraft-interface/`) is only there so that the library and bootstrap have a common
ground to use as the basis for service provider communication. At run-time, it is included as part of
the bootstrap.
