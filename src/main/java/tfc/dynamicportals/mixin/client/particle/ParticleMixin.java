package tfc.dynamicportals.mixin.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.dynamicportals.TeleportationHandler;
import tfc.dynamicportals.access.ParticleAccessor;

@Mixin(Particle.class)
public abstract class ParticleMixin implements ParticleAccessor {
	
	@Shadow protected double x;
	@Shadow protected double y;
	@Shadow protected double z;
	@Shadow	protected double xo;
	@Shadow	protected double yo;
	@Shadow	protected double zo;
	@Shadow	protected double xd;
	@Shadow	protected double yd;
	@Shadow	protected double zd;
	
	@Shadow public abstract void move(double pX, double pY, double pZ);
	
	@Shadow public abstract void setPos(double pX, double pY, double pZ);
	
	@Shadow public abstract AABB getBoundingBox();
	
	@Override
	public Vec3 getPosition() {
		return new Vec3(x, y, z);
	}
	
	@Override
	public Vec3 getOldPosition() {
		return new Vec3(xo, yo, zo);
	}
	
	@Override
	public Vec3 getMotion() {
		return new Vec3(xd, yd, zd);
	}
	
	@Override
	public void setPosition(double nx, double ny, double nz) {
		setPos(nx, ny, nz);
	}
	
	@Override
	public void setOldPosition(double ox, double oy, double oz) {
		xo = ox;
		yo = oy;
		zo = oz;
	}
	
	@Override
	public void move(Vec3 motion) {
		move(motion.x, motion.y, motion.z);
	}
	
	@Override
	public AABB getBBox() {
		return getBoundingBox();
	}
	
	@Inject(at=@At("TAIL"), method = "tick")
	public void teleport(CallbackInfo ci) {
		Vec3 motion = new Vec3(xd, yd, zd);
		Vec3 vec = TeleportationHandler.getTeleportedMotion((Particle) (Object) this, motion);
		motion = vec == null ? motion : vec;
		this.move(motion.x, motion.y, motion.z);
	}
}
