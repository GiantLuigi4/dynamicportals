package tfc.dynamicportals.cmd.cmdr.nodes.args;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import tfc.dynamicportals.cmd.cmdr.CommandNodeAccessor;
import tfc.dynamicportals.cmd.cmdr.exception.DypoException;
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
        public Quaterniond asQuaternion(CommandSourceStack source) {
            Quaterniond sourceRotation = quatFrom(source);

            return new Quaterniond(
                    arg0.get(sourceRotation.x()),
                    arg1.get(sourceRotation.y()),
                    arg2.get(sourceRotation.z()),
                    arg3.get(sourceRotation.w())
            );
        }

        @Override
        public Vec3 asEuler(CommandSourceStack source) {
            CommandNodeAccessor.throwUnchecked(new DypoException(
                    "Cannot represent quaternions using euler angles"
            ));
            throw new RuntimeException("wth");
        }

        @Override
        public Vec2 asPitchYaw(CommandSourceStack source) {
            CommandNodeAccessor.throwUnchecked(new DypoException(
                    "Cannot represent quaternions using pitch/yaw"
            ));
            throw new RuntimeException("wth");
        }
    }

    public static final class EulerData extends OrientationData {
        private final RelAbsParser.RelAbsData arg0, arg1, arg2;

        public EulerData(RelAbsParser.RelAbsData arg0, RelAbsParser.RelAbsData arg1, RelAbsParser.RelAbsData arg2) {
            this.arg0 = arg0;
            this.arg1 = arg1;
            this.arg2 = arg2;
        }

        @Override
        public Quaterniond asQuaternion(CommandSourceStack source) {
            Vec2 vec = source.getRotation();
            Quaterniond sourceRotation = new Quaterniond().rotationXYZ(
                    Math.toRadians(arg2.get(0)),
                    Math.toRadians(arg0.get(-vec.y - 90)),
                    Math.toRadians(arg1.get(-vec.x))
            );
            sourceRotation.normalize();
            return sourceRotation;
        }

        // TODO: check?
        @Override
        public Vec3 asEuler(CommandSourceStack source) {
            Vec2 vec = source.getRotation();
            return new Vec3(
                    arg0.get(vec.x),
                    arg1.get(vec.y),
                    arg2.get(0)
            );
        }

        @Override
        public Vec2 asPitchYaw(CommandSourceStack source) {
            CommandNodeAccessor.throwUnchecked(new DypoException(
                    "Cannot represent euler angles using pitch/yaw"
            ));
            throw new RuntimeException("wth");
        }
    }

    public static final class PitchYawData extends OrientationData {
        private final RelAbsParser.RelAbsData arg0, arg1;

        public PitchYawData(RelAbsParser.RelAbsData arg0, RelAbsParser.RelAbsData arg1) {
            this.arg0 = arg0;
            this.arg1 = arg1;
        }

        @Override
        public Quaterniond asQuaternion(CommandSourceStack source) {
            Vec2 vec = source.getRotation();
            Quaterniond sourceRotation = new Quaterniond().rotationXYZ(
                    0,
                    Math.toRadians(arg0.get(-vec.y - 90)),
                    Math.toRadians(arg1.get(-vec.x))
            );
            sourceRotation.normalize();
            return sourceRotation;
        }

        // TODO: check?
        @Override
        public Vec3 asEuler(CommandSourceStack source) {
            Vec2 vec = source.getRotation();
            return new Vec3(
                    arg0.get(vec.x),
                    arg1.get(vec.y),
                    0
            );
        }

        // TODO: check?
        @Override
        public Vec2 asPitchYaw(CommandSourceStack source) {
            Vec2 vec = source.getRotation();
            return new Vec2(
                    arg0.get(vec.x),
                    arg1.get(vec.y)
            );
        }
    }

    public static Quaterniond quatFrom(CommandSourceStack stack) {
        Vec2 vec = stack.getRotation();
        Quaterniond sourceRotation = new Quaterniond().rotationXYZ(
                0,
                Math.toRadians(-vec.y - 90),
                Math.toRadians(-vec.x)
        );
        sourceRotation.normalize();
        return sourceRotation;
    }

    public abstract Quaterniond asQuaternion(CommandSourceStack source);

    public abstract Vec3 asEuler(CommandSourceStack source);

    public abstract Vec2 asPitchYaw(CommandSourceStack source);
}
