package tfc.dynamicportals.cmd.cmdr.nodes.args;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.phys.Vec2;
import tfc.dynamicportals.cmd.cmdr.nodes.util.RelAbsParser;

public abstract class OrientationData {
    public static final class QuatData extends OrientationData {
        private final RelAbsParser.RelAbsData arg0, arg1, arg2, arg3;

        public QuatData(RelAbsParser.RelAbsData arg0, RelAbsParser.RelAbsData arg1, RelAbsParser.RelAbsData arg2, RelAbsParser.RelAbsData arg3) {
            this.arg0 = arg0;
            this.arg1 = arg1;
            this.arg2 = arg2;
            this.arg3 = arg3;
        }

        @Override
        public Quaternion asQuaternion(CommandSourceStack source) {
            Vec2 vec = source.getRotation();
            Quaternion sourceRotation = new Quaternion(0, -vec.y - 90, -vec.x, true);

            return new Quaternion(
                    arg0.get(sourceRotation.i()),
                    arg1.get(sourceRotation.j()),
                    arg2.get(sourceRotation.k()),
                    arg3.get(sourceRotation.r())
            );
        }
    }

    public abstract Quaternion asQuaternion(CommandSourceStack source);
}
