package tfc.dynamicportals.api.implementation;

import com.mojang.math.Quaternion;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import tfc.dynamicportals.api.AbstractPortal;
import tfc.dynamicportals.api.registry.BasicPortalTypes;
import tfc.dynamicportals.api.registry.PortalType;

import java.io.ByteArrayOutputStream;

public class BasicPortal extends AbstractPortal {
    Vec3 position;
    Quaternion orientation;

    public BasicPortal(PortalType<?> type) {
        super(type);
    }

    public BasicPortal() {
        super(BasicPortalTypes.BASIC);
    }

    @Override
    public AABB getNetworkBox() {
        return null;
    }

    @Override
    public void write(CompoundTag tag) {

    }

    @Override
    public void load(CompoundTag tag) {
    }
}
