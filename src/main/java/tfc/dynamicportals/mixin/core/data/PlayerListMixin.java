package tfc.dynamicportals.mixin.core.data;

import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
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

    @Shadow private int viewDistance;

    @Shadow private int simulationDistance;
    
    @Shadow @Final private LayeredRegistryAccess<RegistryLayer> registries;
    
    @Inject(at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;m_9829_(Lnet/minecraft/network/protocol/Packet;)V", ordinal = 0), method = "placeNewPlayer")
    public void postJoin(Connection pConnection, ServerPlayer pPlayer, CallbackInfo ci) {
        DypoNetworkRegistry.send(new SyncLevelsPacket(viewDistance, simulationDistance, registries, server), PacketDistributor.PLAYER.with(() -> pPlayer));
    }

    @Inject(at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;m_9829_(Lnet/minecraft/network/protocol/Packet;)V", ordinal = 1), method = "respawn")
    public void postRespawn(ServerPlayer pPlayer, boolean pKeepEverything, CallbackInfoReturnable<ServerPlayer> cir) {
        DypoNetworkRegistry.send(new SyncLevelsPacket(viewDistance, simulationDistance, registries, server), PacketDistributor.PLAYER.with(() -> pPlayer));
    }
}
