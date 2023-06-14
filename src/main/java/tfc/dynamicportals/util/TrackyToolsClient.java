package tfc.dynamicportals.util;

import com.google.common.collect.ImmutableList;
import com.tracky.Tracky;
import com.tracky.TrackyAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import tfc.dynamicportals.api.implementation.BasicPortal;

import java.util.*;
import java.util.function.Supplier;

// TODO: deal with memory leaks that are guaranteed to happen due to this
public class TrackyToolsClient {
	private static final UUID trackyToolsUUID = Tracky.getDefaultUUID("dynamic_portals", "client");
	
	private static final HashMap<Level, HashMap<UUID, Set<SectionPos>>> forcedChunks = new HashMap<>();
	
	private static boolean isDirty = false;
	
	public static void init(Level lvl) {
		synchronized (forcedChunks) {
			Map<UUID, Supplier<Collection<SectionPos>>> map0 = TrackyAccessor.getRenderedChunks(lvl);
			if (!forcedChunks.containsKey(lvl) || !map0.containsKey(trackyToolsUUID)) {
				Map<UUID, Set<SectionPos>> map = addIfAbsent(forcedChunks, lvl, HashMap::new);
				final Collection<SectionPos>[] output = new Collection[]{new HashSet<>()};
				Supplier<Collection<SectionPos>> function = () -> {
					Collection<SectionPos> currentFrameOutput = output[0];
					synchronized (map) {
						if (isDirty) {
							currentFrameOutput = new HashSet<>();
							for (Set<SectionPos> value : map.values())
								currentFrameOutput.addAll(value);
							
							currentFrameOutput.remove(null);
							currentFrameOutput = ImmutableList.copyOf(currentFrameOutput);
							output[0] = currentFrameOutput;
							isDirty = false;
						}
					}
					if (Minecraft.getInstance().screen != null)
						if (Minecraft.getInstance().screen instanceof ReceivingLevelScreen)
							return Collections.emptySet();
					return currentFrameOutput;
				};
				map0.put(trackyToolsUUID, function);
			}
		}
	}
	
	public static Set<SectionPos> getChunksForPortal(Level lvl, BasicPortal portal) {
		synchronized (forcedChunks) {
			HashMap<UUID, Set<SectionPos>> map = addIfAbsent(forcedChunks, lvl, HashMap::new);
			init(lvl);
			return addIfAbsent(map, portal.uuid, HashSet::new);
		}
	}
	
	// compute if absent takes a supplier
	// I just want a consumer
	public static <K, T> T addIfAbsent(HashMap<K, T> map, K key, Supplier<T> value) {
		synchronized (map) {
			if (!map.containsKey(key))
				map.put(key, value.get());
			return map.get(key);
		}
	}
	
	public static void markDirty(Level lvl) {
		isDirty = true;
		TrackyAccessor.markForRerender(lvl);
	}
	
	public static void removePortal(Level lvl, BasicPortal portal) {
		synchronized (forcedChunks) {
			Map<UUID, Set<SectionPos>> map = addIfAbsent(forcedChunks, lvl, HashMap::new);
			init(lvl);
			map.remove(portal.uuid);
		}
	}
}
