package blue.lhf.bytecraft.runtime;

import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public enum Egocentric {
    ABOVE,
    BELOW,
    IN_FRONT_OF,
    RIGHT,
    BEHIND,
    LEFT;

    public @NotNull Vector toVector(final Vector forward) {
        return switch (this) {
            case IN_FRONT_OF -> forward;
            case BEHIND -> IN_FRONT_OF.toVector(forward).multiply(-1);
            case ABOVE -> forward.getCrossProduct(BlockFace.UP.getDirection()).getCrossProduct(forward);
            case BELOW -> ABOVE.toVector(forward).multiply(-1);
            case RIGHT -> forward.clone().rotateAroundY(-0.5 * Math.PI);
            case LEFT -> RIGHT.toVector(forward).multiply(-1);
        };
    }
}
