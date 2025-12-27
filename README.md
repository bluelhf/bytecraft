> [!Warning]
> This repository is a stub. While ByteCraft works, it does not include
> any additional syntaxes for interacting with the Bukkit server.

# ByteCraft

ByteCraft is a ByteSkript library that provides Bukkit server integration capabilities.

## Prerequisites

- Java 21 or higher
- ByteSkript compiler
- Bukkit server

## Installation

1. Build the project using Maven: `mvn package`
2. Move the compiled archive from `bytecraft-bootstrap/target/bytecraft-bootstrap-<VERSION>.jar` to your ByteSkript project's `libraries/` directory
3. Place your scripts in the project's `skript/` directory. You may use this as an example:
   ```
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

## Project Structure

ByteCraft is split into three Maven submodules; an interface, library, and bootstrap.
- The library (`bytecraft-library/`) contains practically all useful code, and there is little
reason to look at any of the other directories.
- The bootstrap (`bytecraft-bootstrap/`) handles
loading ByteCraft's dependencies and ByteCraft itself onto the classpath when ByteSkript loads.
- The interface (`bytecraft-interface/`) is only there so that the library and bootstrap have a common
ground to use as the basis for service provider communication. At run-time, it is included as part of
the bootstrap.