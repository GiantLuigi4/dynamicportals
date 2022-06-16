package tfc.dynamicportals.mixin.client;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.shaders.Program;
import com.mojang.blaze3d.shaders.Uniform;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.dynamicportals.ShaderInjections;
import tfc.dynamicportals.api.Renderer;

import java.util.List;
import java.util.Map;

@Mixin(ShaderInstance.class)
public abstract class ShaderMixin {
	@Shadow
	public abstract AbstractUniform safeGetUniform(String pName);
	
	@Shadow
	@Final
	private Map<String, Uniform> uniformMap;
	@Shadow
	@Final
	private int programId;
	@Shadow
	@Final
	private Program vertexProgram;
	@Shadow
	@Final
	private List<String> samplerNames;
	@Shadow
	@Final
	private List<Integer> samplerLocations;
	@Shadow
	@Final
	private Map<String, Object> samplerMap;
	@Unique
	private boolean isInitialized = false;
	
	@Unique
	private AbstractUniform STENCIL_PRESENT;
	@Unique
	private AbstractUniform STENCIL_TEXTURE;
	@Unique
	private AbstractUniform STENCIL_DEPTH;
	@Unique
	private AbstractUniform FBO_SIZE;
	
	@Inject(at = @At("HEAD"), method = "markDirty")
	public void preMarkDirty(CallbackInfo ci) {
		if (vertexProgram != null) {
			if (!isInitialized) {
				isInitialized = true;
				getUniform("dynamicPortalsHasStencilTextureSet", "int", 1);
				getUniform("dynamicPortalsStencilTexture", "int", 1);
				getUniform("dynamicPortalsStencilDepth", "int", 1);
				getUniform("dynamicPortalsFBOSize", "float", 2);
				STENCIL_PRESENT = safeGetUniform("dynamicPortalsHasStencilTextureSet");
				STENCIL_TEXTURE = safeGetUniform("dynamicPortalsStencilTexture");
				STENCIL_DEPTH = safeGetUniform("dynamicPortalsStencilDepth");
				FBO_SIZE = safeGetUniform("dynamicPortalsFBOSize");
			}
		}
	}
	
	@Unique
	private void getUniform(String name, String typeStr, int count) {
		int type = Uniform.getTypeFromString(typeStr);
		int l = count > 1 && count <= 4 && count < 8 ? count - 1 : 0;
		int k = Uniform.glGetUniformLocation(this.programId, name);
		if (k != -1) {
			Uniform uniform = new Uniform(name, type + l, count, (ShaderInstance) (Object) this);
			uniform.setLocation(k);
			uniformMap.put(name, uniform);
		}
	}
	
	@Inject(at = @At(value = "TAIL"), method = "apply")
	public void preApply(CallbackInfo ci) {
		if (vertexProgram != null) {
			STENCIL_PRESENT.set(Renderer.isStencilPresent() ? 1 : 0);
			if (Renderer.useScreenspaceTex()) STENCIL_PRESENT.set(2);
			if (STENCIL_PRESENT instanceof Uniform) ((Uniform) STENCIL_PRESENT).upload();
			if (Renderer.isStencilPresent()) {
				ShaderInjections.setupTextures(STENCIL_TEXTURE, STENCIL_DEPTH);
				FBO_SIZE.set(Renderer.fboWidth(), Renderer.fboHeight());
				if (FBO_SIZE instanceof Uniform) ((Uniform) FBO_SIZE).upload();
			}
		}
	}
}
