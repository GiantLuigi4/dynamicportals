package tfc.dynamicportals.mixin.core.data;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraftforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.dynamicportals.network.DypoNetworkRegistry;
import tfc.dynamicportals.network.sync.SyncLevelsPacket;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    @Shadow
    @Final
    private MinecraftServer server;

    @Shadow @Final private RegistryAccess.Frozen registryHolder;

    @Shadow private int viewDistance;

    @Shadow private int simulationDistance;

    @Inject(at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V", ordinal = 0), method = "placeNewPlayer")
    public void postJoin(Connection pNetManager, ServerPlayer pPlayer, CallbackInfo ci) {
        DypoNetworkRegistry.NETWORK_INSTANCE.send(
                PacketDistributor.PLAYER.with(() -> pPlayer),
                new SyncLevelsPacket(viewDistance, simulationDistance, registryHolder, server)
        );
    }

    @Inject(at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V", ordinal = 1), method = "respawn")
    public void postRespawn(ServerPlayer pPlayer, boolean pKeepEverything, CallbackInfoReturnable<ServerPlayer> cir) {
        DypoNetworkRegistry.NETWORK_INSTANCE.send(
                PacketDistributor.PLAYER.with(() -> pPlayer),
                new SyncLevelsPacket(viewDistance, simulationDistance, registryHolder, server)
        );
    }
}
