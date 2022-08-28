package tfc.dynamicportals.util;

import com.google.common.collect.ImmutableSet;
import com.tracky.Tracky;
import com.tracky.TrackyAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import tfc.dynamicportals.api.implementation.BasicPortal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

// TODO: deal with memory leaks that are guaranteed to happen due to this
public class TrackyTools {
	private static final UUID trackyToolsUUID = Tracky.getDefaultUUID();
	
	private static final HashMap<Level, HashMap<UUID, HashMap<UUID, ArrayList<ChunkPos>>>> forcedChunks = new HashMap<>();
	private static final HashMap<Level, ArrayList<Player>> players = new HashMap<>();
	
	public static void init(Level lvl) {
		synchronized (forcedChunks) {
			HashMap<UUID, HashMap<UUID, ArrayList<ChunkPos>>> map = addIfAbsent(forcedChunks, lvl, HashMap::new);
			Function<Player, Collection<ChunkPos>> function = (player) -> {
				HashMap<UUID, ArrayList<ChunkPos>> map1 = map.get(player.getUUID());
				if (map1 == null) return ImmutableSet.of();
				ArrayList<ChunkPos> output = new ArrayList<>();
				for (ArrayList<ChunkPos> value : map1.values())
					output.addAll(value);
				return output;
			};
			TrackyAccessor.getForcedChunks(lvl).put(trackyToolsUUID, function);
			TrackyAccessor.getPlayersLoadingChunks(lvl).put(trackyToolsUUID, players.get(lvl));
		}
	}
	
	public static ArrayList<ChunkPos> getChunksForPortal(Level lvl, Player player, BasicPortal portal) {
		synchronized (forcedChunks) {
			init(lvl);
			HashMap<UUID, HashMap<UUID, ArrayList<ChunkPos>>> map = addIfAbsent(forcedChunks, lvl, HashMap::new);
			HashMap<UUID, ArrayList<ChunkPos>> forPlayer = addIfAbsent(map, player.getUUID(), HashMap::new);
			addIfAbsent(players, lvl, ArrayList::new).add(player);
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
