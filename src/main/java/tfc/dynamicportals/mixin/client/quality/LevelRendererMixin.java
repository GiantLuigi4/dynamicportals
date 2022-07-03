package tfc.dynamicportals.mixin.client.quality;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.dynamicportals.Renderer;
import tfc.dynamicportals.Temp;
import tfc.dynamicportals.access.ExtendedView;
import tfc.dynamicportals.api.AbstractPortal;
import tfc.dynamicportals.opt.VecMap;

import javax.annotation.Nullable;
import java.util.LinkedHashSet;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

// TODO: get this working with sodium/rubidium
@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
	@Shadow
	@Final
	public BlockingQueue<ChunkRenderDispatcher.RenderChunk> recentlyCompiledChunks;
	
	@Shadow
	@Nullable
	private ClientLevel level;
	
	@Shadow
	@Nullable
	private ViewArea viewArea;
	
	@Inject(at = @At("HEAD"), method = "prepareCullFrustum")
	public void prePrepCullFrustum(PoseStack pPoseStack, Vec3 pCameraPos, Matrix4f pProjectionMatrix, CallbackInfo ci) {
	}
	
	@ModifyVariable(at = @At("HEAD"), method = "setupRender", index = 3, argsOnly = true)
	public boolean denyRefresh(boolean src) {
		if (!recentlyCompiledChunks.isEmpty()) {
			for (AbstractPortal portal : Temp.getPortals(level)) {
				if (portal.getGraph() != null) {
					portal.getGraph().addAll(recentlyCompiledChunks);
				}
			}
		}
		if (Renderer.isStencilPresent())
			return true;
		return src;
	}
	
	@Inject(at = @At("HEAD"), method = "setupRender", cancellable = true)
	public void preSetupRender(Camera chunkrenderdispatcher$renderchunk, Frustum levelrenderer$renderchunkinfo, boolean queue, boolean levelrenderer$renderchunkstorage, CallbackInfo ci) {
		// TODO: check if this breaks compat
		if (Renderer.isStencilPresent())
			ci.cancel();
	}
	
	@Inject(at = @At("HEAD"), method = "applyFrustum", cancellable = true)
	public void preApplyFrustum(Frustum pFrustrum, CallbackInfo ci) {
		if (Renderer.isStencilPresent())
			ci.cancel();
	}
	
	@Inject(at = @At("HEAD"), method = "updateRenderChunks")
	public void preUpdateRenderChunks(LinkedHashSet<LevelRenderer.RenderChunkInfo> pChunkInfos, LevelRenderer.RenderInfoMap pInfoMap, Vec3 pViewVector, Queue<LevelRenderer.RenderChunkInfo> pInfoQueue, boolean pShouldCull, CallbackInfo ci) {
		VecMap<ChunkRenderDispatcher.RenderChunk> chunks = ((ExtendedView) viewArea).extraView();
		for (ChunkRenderDispatcher.RenderChunk value : chunks.values()) {
			if (value.isDirty()) {
				pInfoQueue.add(new LevelRenderer.RenderChunkInfo(value, (Direction) null, 0));
			}
		}
	}
	
	@Inject(at = @At("HEAD"), method = "initializeQueueForFullUpdate")
	public void preInitQueue(Camera pCamera, Queue<LevelRenderer.RenderChunkInfo> pInfoQueue, CallbackInfo ci) {
		VecMap<ChunkRenderDispatcher.RenderChunk> chunks = ((ExtendedView) viewArea).extraView();
		for (ChunkRenderDispatcher.RenderChunk value : chunks.values()) {
			pInfoQueue.add(new LevelRenderer.RenderChunkInfo(value, (Direction) null, 0));
		}
	}
}
