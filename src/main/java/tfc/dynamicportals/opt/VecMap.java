package tfc.dynamicportals.opt;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import net.minecraft.core.Vec3i;

import java.util.ArrayList;

public class VecMap<T> {
	public final int level;
	
	Int2ObjectLinkedOpenHashMap<VecMap<T>> map;
	Int2ObjectLinkedOpenHashMap<T> trueMap;
	
	public VecMap(int layers) {
		this.level = layers;
		if (layers == 0) trueMap = new Int2ObjectLinkedOpenHashMap<>();
		else map = new Int2ObjectLinkedOpenHashMap<>();
	}
	
	public T get(Vec3i vec) {
		if (level == 2) {
			VecMap<T> map = this.map.getOrDefault(vec.getX(), null);
			if (map == null) {
				map = new VecMap<>(1);
				this.map.put(vec.getX(), map);
			}
			return map.get(vec);
		}
		if (level == 1) {
			VecMap<T> map = this.map.getOrDefault(vec.getY(), null);
			if (map == null) {
				map = new VecMap<>(0);
				this.map.put(vec.getY(), map);
			}
			return map.get(vec);
		}
		return trueMap.get(vec.getZ());
	}
	
	public T put(Vec3i vec, T renderChunk) {
		if (level == 2) {
			VecMap<T> map = this.map.getOrDefault(vec.getX(), null);
			if (map == null) {
				map = new VecMap<>(1);
				this.map.put(vec.getX(), map);
			}
			return map.put(vec, renderChunk);
		}
		if (level == 1) {
			VecMap<T> map = this.map.getOrDefault(vec.getY(), null);
			if (map == null) {
				map = new VecMap<>(0);
				this.map.put(vec.getY(), map);
			}
			return map.put(vec, renderChunk);
		}
		return trueMap.put(vec.getZ(), renderChunk);
	}
	
	public Iterable<? extends Vec3i> keySet() {
		ArrayList<Vec3i> vecs = new ArrayList<>();
		for (Integer integer : map.keySet()) {
			VecMap<T> mp = map.get(integer);
			for (Integer integer1 : mp.map.keySet()) {
				VecMap<T> mp1 = mp.map.get(integer1);
				for (Integer integer2 : mp1.trueMap.keySet()) {
					vecs.add(new Vec3i(integer, integer1, integer2));
				}
			}
		}
		return vecs;
	}
	
	public Iterable<T> values() {
		if (level == 0) {
			return trueMap.values();
		}
		ArrayList<T> out = new ArrayList<>();
		for (VecMap<T> value : map.values()) {
			for (T t : value.values()) {
				out.add(t);
			}
		}
		return out;
	}
}
