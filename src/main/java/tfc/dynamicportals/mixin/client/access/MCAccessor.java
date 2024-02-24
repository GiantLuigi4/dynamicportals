package tfc.dynamicportals.mixin.client.access;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import tfc.dynamicportals.itf.MinecraftAccess;

@Mixin(Minecraft.class)
public class MCAccessor implements MinecraftAccess {
	@Shadow @Final @Mutable
	public LevelRenderer levelRenderer;
	
	@Override
	public void dynamic_portals$setLevelRenderer(LevelRenderer instance) {
		levelRenderer = instance;
	}
}
