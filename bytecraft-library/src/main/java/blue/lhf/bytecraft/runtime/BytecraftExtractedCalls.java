package blue.lhf.bytecraft.runtime;

import net.kyori.adventure.key.Key;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.byteskript.skript.error.ScriptRuntimeError;
import org.byteskript.skript.runtime.Skript;

import java.util.UUID;

public class BytecraftExtractedCalls {
    public static Block getRelativeCardinal(final Object amountObject, final Object directionObject, final Object targetObject) {
        final Integer amount = Skript.convert(amountObject, Integer.class);
        final BlockFace direction = Skript.convert(directionObject, BlockFace.class);
        final Location target = Skript.convert(targetObject, Location.class);
        return target.clone().add(direction.getDirection().multiply(amount)).getBlock();
    }

    public static Block getRelativeEgocentric(final Object amountObject, final Object directionObject, final Object targetObject) {
        final Integer amount = Skript.convert(amountObject, Integer.class);
        final Egocentric direction = Skript.convert(directionObject, Egocentric.class);
        final Location target = Skript.convert(targetObject, Location.class);
        return target.clone().add(direction.toVector(target.getDirection()).multiply(amount)).getBlock();
    }

    public static Location findLocation(final Object xObject, final Object yObject, final Object zObject, final Object worldObject) {
        final Integer x = Skript.convert(xObject, Integer.class);
        final Integer y = Skript.convert(yObject, Integer.class);
        final Integer z = Skript.convert(zObject, Integer.class);
        final World world = Skript.convert(worldObject, World.class);
        return new Location(world, x, y, z);
    }

    public static World findWorld(final Object objectCandidate) {
        Object object = Skript.convert(objectCandidate, World.class, false);
        if (object == null) object = Skript.convert(objectCandidate, Key.class, false);
        if (object == null) object = Skript.convert(objectCandidate, NamespacedKey.class, false);
        if (object == null) object = Skript.convert(objectCandidate, UUID.class, false);
        if (object == null) object = Skript.convert(objectCandidate, String.class, false);

        return switch (object) {
            case final UUID uuid -> Bukkit.getWorld(uuid);
            case final NamespacedKey key ->  Bukkit.getWorld(key);
            case final String name -> Bukkit.getWorld(name);
            case final Key adventureKey -> Bukkit.getWorld(adventureKey);
            default -> throw new ScriptRuntimeError(objectCandidate.getClass().getSimpleName() + " cannot be used to " +
                    "find a world, as it is not and cannot be converted to an UUID, NamespacedKey, String, or Key.");
        };
    }
}
