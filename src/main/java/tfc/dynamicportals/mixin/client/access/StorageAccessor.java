package tfc.dynamicportals.mixin.client.access;

import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.concurrent.atomic.AtomicReferenceArray;

@Mixin(ClientChunkCache.Storage.class)
public interface StorageAccessor {
	@Accessor("chunks")
	AtomicReferenceArray<LevelChunk> chunks();
}

