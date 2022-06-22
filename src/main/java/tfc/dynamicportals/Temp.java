package tfc.dynamicportals;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import tfc.dynamicportals.api.BasicPortal;

import java.util.ArrayList;

public class Temp {
	private static BasicPortal[] portals;
	
	static {
		ArrayList<BasicPortal> portals = new ArrayList<>();
		
		double width = Math.sqrt(Math.pow(2, 2) * 2);

//		{
//			Portal portal = new Portal();
//			portal.size = new Vector2d(500, 5000);
////			portal.position = new Vector3d(camX, 10, camZ - portal.size.y / 2);
//			portal.position = new Vector3d(0, 0, 0);
//			portal.rotation = new Vector2d(Math.toRadians(22.5), Math.toRadians(0));
////			portal.computeNormal();
//			portals.add(portal);
//		}

//		double time = 0;
//		if (Minecraft.getInstance().level != null) {
//			time = Minecraft.getInstance().level.getGameTime() + Minecraft.getInstance().getFrameTime();
//		}
//		double time = 12;
//		double rotation = time;
		double rotation = 0;
		int count = 2;
		for (int i = 0; i < count; i++) {
			rotation += 360 / (count * 2d);
			double c = Math.cos(Math.toRadians(rotation));
			double s = Math.sin(Math.toRadians(rotation));
			double s1 = Math.sin(Math.toRadians((rotation / 3.) + 180));
			double c1 = Math.sin(Math.toRadians((rotation / 8.) + 180));
			BasicPortal other = new BasicPortal()
					.setSize(2, 3)
					.setPosition(s * -7 - c1 * 10, 5, c * -7 - c1 * 10)
//					.setPosition(0, 5, -2)
					.setRotation(Math.toRadians(22), Math.toRadians(0)); //22=>rotation
//					.setRotation(Math.toRadians(0), 0);
			other.computeNormal();
			portals.add(other);
			{
				BasicPortal portal = new BasicPortal()
						.setSize(2, 3)
						.setPosition(s * 7 + s1 * 10, 5, c * 7 + s1 * 10)
//						.setPosition(0, 5, 2)
						.setRotation(Math.toRadians(70), Math.toRadians(0)); //30=>rotation+180
//						.setRotation(Math.toRadians(180), 0);
				portal.computeNormal();
				portals.add(portal);
				other.target = portal;
				portal.target = other;
			}
		}
		
		Temp.portals = portals.toArray(new BasicPortal[0]);
	}
	
	public static BasicPortal[] getPortals(Level level) {
//		portals[0].setPosition(
//				Minecraft.getInstance().player.position().x - 5,
//				Minecraft.getInstance().player.position().y,
//				Minecraft.getInstance().player.position().z
//		);
		return Temp.portals;
	}
}
