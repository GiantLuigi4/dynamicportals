package tfc.dynamicportals.api.implementation.data;

import com.mojang.math.Vector3d;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.phys.Vec3;
import tfc.dynamicportals.util.Vec2d;

import java.util.Optional;

public class PortalDataSerializers {
	public static final EntityDataSerializer<Vector3d> VECTOR_3D = new EntityDataSerializer<Vector3d>() {
		@Override
		public void write(FriendlyByteBuf p_135140_, Vector3d p_135141_) {
			p_135140_.writeDouble(p_135141_.x);
			p_135140_.writeDouble(p_135141_.y);
			p_135140_.writeDouble(p_135141_.z);
		}
		
		@Override
		public Vector3d read(FriendlyByteBuf p_135143_) {
			return new Vector3d(p_135143_.readDouble(), p_135143_.readDouble(), p_135143_.readDouble());
		}
		
		@Override
		public Vector3d copy(Vector3d p_135133_) {
			return new Vector3d(p_135133_.x, p_135133_.y, p_135133_.z);
		}
	};
	
	public static final EntityDataSerializer<Vec3> VEC3 = new EntityDataSerializer<Vec3>() {
		@Override
		public void write(FriendlyByteBuf p_135140_, Vec3 p_135141_) {
			p_135140_.writeDouble(p_135141_.x);
			p_135140_.writeDouble(p_135141_.y);
			p_135140_.writeDouble(p_135141_.z);
		}
		
		@Override
		public Vec3 read(FriendlyByteBuf p_135143_) {
			return new Vec3(p_135143_.readDouble(), p_135143_.readDouble(), p_135143_.readDouble());
		}
		
		@Override
		public Vec3 copy(Vec3 p_135133_) {
			return p_135133_;
		}
	};
	
	public static final EntityDataSerializer<Optional<Vec3>> OPTIONAL_VEC3 = new EntityDataSerializer<Optional<Vec3>>() {
		@Override
		public void write(FriendlyByteBuf p_135140_, Optional<Vec3> p_135141_) {
			p_135140_.writeBoolean(p_135141_.isPresent());
			if (p_135141_.isPresent()) {
				Vec3 p = p_135141_.get();
				p_135140_.writeDouble(p.x);
				p_135140_.writeDouble(p.y);
				p_135140_.writeDouble(p.z);
			}
		}
		
		@Override
		public Optional<Vec3> read(FriendlyByteBuf p_135143_) {
			if (p_135143_.readBoolean())
				return Optional.of(new Vec3(p_135143_.readDouble(), p_135143_.readDouble(), p_135143_.readDouble()));
			else return Optional.empty();
		}
		
		@Override
		public Optional<Vec3> copy(Optional<Vec3> p_135133_) {
			return p_135133_;
		}
	};
	
	public static final EntityDataSerializer<Vec2d> VEC2D = new EntityDataSerializer<Vec2d>() {
		@Override
		public void write(FriendlyByteBuf p_135140_, Vec2d p_135141_) {
			p_135140_.writeDouble(p_135141_.x);
			p_135140_.writeDouble(p_135141_.y);
		}
		
		@Override
		public Vec2d read(FriendlyByteBuf p_135143_) {
			return new Vec2d(p_135143_.readDouble(), p_135143_.readDouble());
		}
		
		@Override
		public Vec2d copy(Vec2d p_135133_) {
			return new Vec2d(p_135133_.x, p_135133_.y);
		}
	};
}
