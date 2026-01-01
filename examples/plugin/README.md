# Custom Plugin

This example demonstrates a custom plugin for a Minecraft server using the PaperMC API. The plugin listens for the new `COMMANDS` lifecycle event to register a command, `/foo`. The command then simply sends some messages to console as well as a few to the executor of the command. For more details, see the comments inside `index.bsk`.