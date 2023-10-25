package tfc.dynamicportals.mixin.core.debug;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.dynamicportals.api.PortalNet;
import tfc.dynamicportals.api.implementation.BasicPortal;
import tfc.dynamicportals.itf.NetworkHolder;

import java.util.UUID;

@Mixin(MinecraftServer.class)
public class ServerMixin {
	@Inject(at = @At("HEAD"), method = "runServer")
	public void preRun(CallbackInfo ci) {
		PortalNet net = new PortalNet(new UUID(
				98423, 23912
		));
		BasicPortal bap0 = new BasicPortal();
		bap0.position = new Vec3(32, 64, 0);
		net.link(bap0);
		BasicPortal bap1 = new BasicPortal();
		bap1.position = new Vec3(-32, 64, 0);
		net.link(bap1);
		((NetworkHolder) this).getPortalNetworks().add(net);
	}
}
