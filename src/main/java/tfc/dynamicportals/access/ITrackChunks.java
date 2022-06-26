package tfc.dynamicportals.access;

import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;

public interface ITrackChunks {
	ArrayList<ChunkPos> trackedChunks();
	
	ArrayList<ChunkPos> oldTrackedChunks();
	
	void tickTracking();
	
	boolean setDoUpdate(boolean val);
}
