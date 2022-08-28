package tfc.dynamicportals.util;

import com.tracky.Tracky;
import com.tracky.TrackyAccessor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import tfc.dynamicportals.api.implementation.BasicPortal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Supplier;

// TODO: deal with memory leaks that are guaranteed to happen due to this
public class TrackyToolsClient {
	private static final UUID trackyToolsUUID = Tracky.getDefaultUUID();
	
	private static final HashMap<Level, HashMap<UUID, ArrayList<ChunkPos>>> forcedChunks = new HashMap<>();
	
	private static boolean isDirty = false;
	
	public static void init(Level lvl) {
		synchronized (forcedChunks) {
			HashMap<UUID, ArrayList<ChunkPos>> map = addIfAbsent(forcedChunks, lvl, HashMap::new);
			ArrayList<ChunkPos> output = new ArrayList<>();
			Supplier<Collection<ChunkPos>> function = () -> {
				if (isDirty) {
					for (ArrayList<ChunkPos> value : map.values())
						output.addAll(value);
					isDirty = false;
				}
				return output;
			};
			TrackyAccessor.getRenderedChunks(lvl).put(trackyToolsUUID, function);
		}
	}
	
	public static ArrayList<ChunkPos> getChunksForPortal(Level lvl, BasicPortal portal) {
		synchronized (forcedChunks) {
			init(lvl);
			HashMap<UUID, ArrayList<ChunkPos>> map = addIfAbsent(forcedChunks, lvl, HashMap::new);
			return addIfAbsent(map, portal.uuid, ArrayList::new);
		}
	}
	
	// compute if absent takes a supplier
	// I just want a consumer
	public static <K, T> T addIfAbsent(HashMap<K, T> map, K key, Supplier<T> value) {
		if (!map.containsKey(key))
			map.put(key, value.get());
		return map.get(key);
	}
	
	public static void markDirty() {
		isDirty = true;
	}
}
