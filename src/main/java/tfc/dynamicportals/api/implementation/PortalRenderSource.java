package tfc.dynamicportals.api.implementation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.tracky.api.RenderSource;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collection;

public class PortalRenderSource extends RenderSource {
	int radius;
	BasicPortal portal;
	SectionPos originSection;
	
	boolean isActive = false;
	
	public PortalRenderSource setActive(boolean active) {
		isActive = active;
		return this;
	}
	
	protected static ArrayList<SectionPos> createList(BasicPortal portal, int radius) {
		Vec3 origin = portal.target.raytraceOffset();
		SectionPos originSection = SectionPos.of(new BlockPos(
				(int) origin.x, (int) origin.y, (int) origin.z
		));
		
		ArrayList<SectionPos> sections = new ArrayList<>();
		
		for (int x = -radius; x <= radius; x++) {
			for (int z = -radius; z <= radius; z++) {
				double dist = Math.sqrt(x * x + z * z);
				if (dist > radius) continue;
				
				for (int y = -radius; y <= radius; y++) {
					sections.add(
							SectionPos.of(
									originSection.getX() + x,
									originSection.getY() + y,
									originSection.getZ() + z
							)
					);
				}
			}
		}
		
		return sections;
	}
	
	public PortalRenderSource(Collection<SectionPos> sections, int radius, BasicPortal portal) {
		super(sections);
		this.radius = radius;
		this.portal = portal;
		
		Vec3 origin = portal.target.raytraceOffset();
		this.originSection = SectionPos.of(new BlockPos(
				(int) origin.x, (int) origin.y, (int) origin.z
		));
	}
	
	public PortalRenderSource(int radius, BasicPortal portal) {
		this(createList(portal, radius), radius, portal);
	}
	
	public void tick() {
		// TODO:
	}
	
	@Override
	public boolean containsSection(SectionPos pos) {
		int yO = pos.getY() - originSection.getY();
		if (Math.abs(yO) > radius)
			return false;
		
		int xO = pos.getX() - originSection.getX();
		int zO = pos.getZ() - originSection.getZ();
		
		return Math.sqrt(xO * xO + zO * zO) <= radius;
	}
	
	@Override
	protected void updateFrustum(Camera camera, Frustum frustum) {
		if (isActive) {
			this.chunksInFrustum.clear();
			
			for (ChunkRenderDispatcher.RenderChunk renderChunk : this.chunksInSource) {
//				if (renderChunk.getCompiledChunk().hasNoRenderableLayers())
//					continue;
				
				if (frustum.isVisible(renderChunk.getBoundingBox()))
					chunksInFrustum.add(renderChunk);
			}
		}
	}
	
	@Override
	public boolean needsCulling() {
		return isActive;
	}
	
	@Override
	public void calculateChunkOffset(Vector3f vec, double camX, double camY, double camZ) {
		// TODO: ?
		super.calculateChunkOffset(vec, camX, camY, camZ);
	}
	
	@Override
	public boolean canDraw(Camera camera, Frustum frustum) {
		if (portal.target instanceof BasicPortal bap)
			return isActive && frustum.isVisible(bap.box);
		return isActive;
	}
	
	@Override
	public void draw(PoseStack stack, ViewArea area, ShaderInstance instance, RenderType type, double camX, double camY, double camZ) {
		super.draw(stack, area, instance, type, camX, camY, camZ);
	}
}
