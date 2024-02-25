package tfc.dynamicportals.command.arg;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.world.phys.Vec3;

public class OrientationArgument extends Vec3Argument {
	public OrientationArgument() {
		super(false);
	}
	
	public static OrientationArgument vec3() {
		return new OrientationArgument();
	}
	
	public static Vec3 getVec3(CommandContext<CommandSourceStack> pContext, String pName) {
		Coordinates crds = pContext.getArgument(pName, Coordinates.class);
		Vec3 vec = crds.getPosition(pContext.getSource());
		
		double xc;
		if (crds.isXRelative()) xc = pContext.getSource().getRotation().x;
		else xc = vec.x;
		
		double yc;
		if (crds.isXRelative()) yc = pContext.getSource().getRotation().y;
		else yc = vec.y;
		
		double zc;
		if (crds.isXRelative()) zc = 0;
		else zc = vec.z;
		
		return new Vec3(xc, yc, zc);
	}
}
