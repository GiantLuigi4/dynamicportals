package tfc.dynamicportals.util;

import com.google.common.collect.ImmutableSet;
import com.tracky.Tracky;
import com.tracky.TrackyAccessor;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import tfc.dynamicportals.api.implementation.BasicPortal;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

// TODO: deal with memory leaks that are guaranteed to happen due to this
public class TrackyTools {
	private static final UUID trackyToolsUUID = Tracky.getDefaultUUID("dynamic_portals", "server");
	
	private static final HashMap<Level, HashMap<UUID, HashMap<UUID, ArrayList<SectionPos>>>> forcedChunks = new HashMap<>();
	private static final HashMap<Level, ArrayList<Player>> players = new HashMap<>();
	
	public static void init(Level lvl) {
		synchronized (forcedChunks) {
			Map<UUID, Function<Player, Collection<SectionPos>>> map0 = TrackyAccessor.getForcedChunks(lvl);
			if (!forcedChunks.containsKey(lvl) || !map0.containsKey(trackyToolsUUID)) {
				HashMap<UUID, HashMap<UUID, ArrayList<SectionPos>>> map = addIfAbsent(forcedChunks, lvl, HashMap::new);
				Function<Player, Collection<SectionPos>> function = (player) -> {
					HashMap<UUID, ArrayList<SectionPos>> map1 = map.get(player.getUUID());
					if (map1 == null) return ImmutableSet.of();
					ArrayList<SectionPos> output = new ArrayList<>();
					for (ArrayList<SectionPos> value : map1.values())
						output.addAll(value);
					return output;
				};
				map0.put(trackyToolsUUID, function);
				TrackyAccessor.getPlayersLoadingChunks(lvl).put(trackyToolsUUID, players.get(lvl));
			}
		}
	}
	
	public static ArrayList<SectionPos> getChunksForPortal(Level lvl, Player player, BasicPortal portal) {
		synchronized (forcedChunks) {
			HashMap<UUID, HashMap<UUID, ArrayList<SectionPos>>> map = addIfAbsent(forcedChunks, lvl, HashMap::new);
			HashMap<UUID, ArrayList<SectionPos>> forPlayer = addIfAbsent(map, player.getUUID(), HashMap::new);
			if (!player.level.isClientSide)
				addIfAbsent(players, lvl, ArrayList::new).add(player);
			init(lvl);
			return addIfAbsent(forPlayer, portal.uuid, ArrayList::new);
		}
	}
	
	// compute if absent takes a supplier
	// I just want a consumer
	public static <K, T> T addIfAbsent(HashMap<K, T> map, K key, Supplier<T> value) {
		if (!map.containsKey(key))
			map.put(key, value.get());
		return map.get(key);
	}
}
