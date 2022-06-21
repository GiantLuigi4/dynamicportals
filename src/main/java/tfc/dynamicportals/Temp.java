package tfc.dynamicportals;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import tfc.dynamicportals.api.BasicPortal;

import java.util.ArrayList;

public class Temp {
	public static BasicPortal[] getPortals(Level level) {
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
//		double rotation = time;
		double rotation = 0;
		for (int i = 0; i < 2; i++) {
			rotation += 360 / 4d;
			double c = Math.cos(Math.toRadians(rotation));
			double s = Math.sin(Math.toRadians(rotation));
			BasicPortal other = new BasicPortal()
					.setSize(width, 2)
					.setPosition(s * -7, 5, c * -7)
//					.setPosition(0, 5, -2)
					.setRotation(Math.toRadians(rotation), Math.toRadians(0));
//					.setRotation(Math.toRadians(0), 0);
			other.computeNormal();
			portals.add(other);
			{
				BasicPortal portal = new BasicPortal()
						.setSize(width, 2)
						.setPosition(s * 7, 5, c * 7)
//						.setPosition(0, 5, 2)
						.setRotation(Math.toRadians(rotation + 180), Math.toRadians(0));
//						.setRotation(Math.toRadians(180), 0);
				portal.computeNormal();
				portals.add(portal);
				other.target = portal;
				portal.target = other;
			}
		}
		
		return portals.toArray(new BasicPortal[0]);
	}
}
