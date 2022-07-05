package tfc.dynamicportals.mixin.client.quality;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.dynamicportals.access.ExtendedView;
import tfc.dynamicportals.opt.VecMap;

@Mixin(ViewArea.class)
public abstract class ViewAreaMixin implements ExtendedView {
	@Shadow
	public ChunkRenderDispatcher.RenderChunk[] chunks;
	@Shadow
	protected int chunkGridSizeX;
	@Shadow
	protected int chunkGridSizeY;
	@Shadow
	protected int chunkGridSizeZ;
	@Shadow
	@Final
	protected Level level;
	@Unique
	VecMap<ChunkRenderDispatcher.RenderChunk> chunksMap = new VecMap<>(2);
	@Unique
	VecMap<ChunkRenderDispatcher.RenderChunk> absChunksMap = new VecMap<>(2);
	ChunkRenderDispatcher chunkFactory;
	int centerX;
	int centerZ;
	
	@Shadow
	protected abstract int getChunkIndex(int pX, int pY, int pZ);
	
	@Inject(at = @At("TAIL"), method = "<init>")
	public void postInit(ChunkRenderDispatcher pChunkRenderDispatcher, Level pLevel, int pViewDistance, LevelRenderer pLevelRenderer, CallbackInfo ci) {
	}
	
	@Inject(at = @At("HEAD"), method = "createChunks", cancellable = true)
	public void preCreateChunks(ChunkRenderDispatcher pRenderChunkFactory, CallbackInfo ci) {
		if (!Minecraft.getInstance().isSameThread()) {
			throw new IllegalStateException("createChunks called from wrong thread: " + Thread.currentThread().getName());
		} else {
			this.chunkFactory = pRenderChunkFactory;
			int i = this.chunkGridSizeX * this.chunkGridSizeY * this.chunkGridSizeZ;
			this.chunks = new ChunkRenderDispatcher.RenderChunk[i];
			
			for (int x = 0; x < this.chunkGridSizeX; ++x) {
				for (int y = 0; y < this.chunkGridSizeY; ++y) {
					for (int z = 0; z < this.chunkGridSizeZ; ++z) {
//						int index = this.getChunkIndex(x, y, z);
//						this.chunksMap.put(new Vec3i(x, y, z), pRenderChunkFactory.new RenderChunk(index, x * 16, y * 16, z * 16));
						makeChunk(new Vec3i(x, y, z), true);
					}
				}
			}
		}
		ci.cancel();
	}

	@Inject(at = @At("HEAD"), method = "releaseAllBuffers", cancellable = true)
	public void preReleaseBuffers(CallbackInfo ci) {
		for (ChunkRenderDispatcher.RenderChunk value : chunksMap.values()) value.releaseBuffers();
		ci.cancel();
	}
	
	@Inject(at = @At("HEAD"), method = "repositionCamera", cancellable = true)
	public void prePosition(double pViewEntityX, double pViewEntityZ, CallbackInfo ci) {
		int x = Mth.ceil(pViewEntityX);
		int z = Mth.ceil(pViewEntityZ);
		centerX = x;
		centerZ = z;
		
		int gridSizeX = this.chunkGridSizeX * 16;
		int gridSizeZ = this.chunkGridSizeZ * 16;
		for (Vec3i vec3i : chunksMap.keySet()) {
			int iterX = vec3i.getX();
			int iterY = vec3i.getY();
			int iterZ = vec3i.getZ();
			int offsetPosX = x - 8 - gridSizeX / 2;
			int gridPosX = offsetPosX + Math.floorMod(iterX * 16 - offsetPosX, gridSizeX);
			int offsetPosZ = z - 8 - gridSizeZ / 2;
			int gridPosZ = offsetPosZ + Math.floorMod(iterZ * 16 - offsetPosZ, gridSizeZ);
			int l2 = this.level.getMinBuildHeight() + iterY * 16;
			ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk = getChunk(new Vec3i(iterX, iterY, iterZ), true);
			BlockPos blockpos = chunkrenderdispatcher$renderchunk.getOrigin();
			if (gridPosX != blockpos.getX() || l2 != blockpos.getY() || gridPosZ != blockpos.getZ()) {
				chunkrenderdispatcher$renderchunk.setOrigin(gridPosX, l2, gridPosZ);
			}
		}
		ci.cancel();
	}
	
	@Inject(at = @At("HEAD"), method = "setDirty", cancellable = true)
	public void preSetDirty(int pSectionX, int pSectionY, int pSectionZ, boolean pReRenderOnMainThread, CallbackInfo ci) {
		int x = Math.floorMod(pSectionX, this.chunkGridSizeX);
		int y = Math.floorMod(pSectionY - this.level.getMinSection(), this.chunkGridSizeY);
		int z = Math.floorMod(pSectionZ, this.chunkGridSizeZ);
//		ChunkRenderDispatcher.RenderChunk chunk = chunksMap.get(new Vec3i(x, y, z));
		ChunkRenderDispatcher.RenderChunk chunk = getChunk(new Vec3i(x, y, z), true);
		chunk.setDirty(pReRenderOnMainThread);
		ci.cancel();
	}
	
	@Inject(at = @At("HEAD"), method = "getRenderChunkAt", cancellable = true)
	public void preGetRenderChunk(BlockPos pPos, CallbackInfoReturnable<ChunkRenderDispatcher.RenderChunk> cir) {
		int x = Mth.intFloorDiv(pPos.getX(), 16);
		int y = Mth.intFloorDiv(pPos.getY() - this.level.getMinBuildHeight(), 16);
		int z = Mth.intFloorDiv(pPos.getZ(), 16);
		if (y >= 0 && y < this.chunkGridSizeY) {
			x = Mth.positiveModulo(x, this.chunkGridSizeX);
			z = Mth.positiveModulo(z, this.chunkGridSizeZ);
			ChunkRenderDispatcher.RenderChunk chunk = getChunk(new Vec3i(x, y, z), true);
			cir.setReturnValue(chunk);
		} else cir.setReturnValue(null);
	}
	
	@Override
	public ChunkRenderDispatcher.RenderChunk makeChunk(Vec3i vec, boolean relative) {
		if (!relative) {
			Vec3i relativePos = new Vec3i(vec.getX() - centerX, vec.getY(), vec.getZ() - centerZ);
			ChunkRenderDispatcher.RenderChunk renderChunk = get(relativePos);
			if (renderChunk != null) {
				return renderChunk;
			}
			int x = vec.getX();
			int y = vec.getY();
			int z = vec.getZ();
			int index = this.getChunkIndex(x, y, z);
			ChunkRenderDispatcher.RenderChunk chunk = chunkFactory.new RenderChunk(index, x * 16, y * 16, z * 16);
			this.absChunksMap.put(new Vec3i(x, y, z), chunk);
			return chunk;
		}
		{
			Vec3i absPos = new Vec3i(vec.getX() + centerX, vec.getY(), vec.getZ() + centerZ);
			int x = absPos.getX();
			int y = absPos.getY();
			int z = absPos.getZ();
			ChunkRenderDispatcher.RenderChunk renderChunk = this.absChunksMap.get(new Vec3i(x, y, z));
			if (renderChunk != null) return renderChunk;
		}
		int x = vec.getX();
		int y = vec.getY();
		int z = vec.getZ();
		int index = this.getChunkIndex(x, y, z);
		ChunkRenderDispatcher.RenderChunk chunk = chunkFactory.new RenderChunk(index, x * 16, y * 16, z * 16);
		this.chunksMap.put(new Vec3i(x, y, z), chunk);
		return chunk;
	}
	
	private ChunkRenderDispatcher.RenderChunk get(Vec3i vec) {
		int x = vec.getX();
		int y = vec.getY();
		int z = vec.getZ();
		ChunkRenderDispatcher.RenderChunk chunk = chunksMap.get(new Vec3i(x, y, z));
		return chunk;
	}
	
	@Override
	public ChunkRenderDispatcher.RenderChunk getChunk(Vec3i vec, boolean relative) {
		if (!relative) {
			Vec3i relativePos = new Vec3i(vec.getX() - centerX, vec.getY(), vec.getZ() - centerZ);
			ChunkRenderDispatcher.RenderChunk renderChunk = get(relativePos);
			if (renderChunk != null) return renderChunk;
			int x = vec.getX();
			int y = vec.getY();
			int z = vec.getZ();
			ChunkRenderDispatcher.RenderChunk chunk = this.absChunksMap.get(new Vec3i(x, y, z));
			return chunk;
		}
		{
			Vec3i absPos = new Vec3i(vec.getX() + centerX, vec.getY(), vec.getZ() + centerZ);
			int x = absPos.getX();
			int y = absPos.getY();
			int z = absPos.getZ();
			ChunkRenderDispatcher.RenderChunk renderChunk = this.absChunksMap.get(new Vec3i(x, y, z));
			if (renderChunk != null) return renderChunk;
		}
		return chunksMap.get(vec);
	}
	
	@Override
	public VecMap<ChunkRenderDispatcher.RenderChunk> extraView() {
		return absChunksMap;
	}
}
