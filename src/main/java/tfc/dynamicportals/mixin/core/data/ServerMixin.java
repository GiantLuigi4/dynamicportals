package tfc.dynamicportals.mixin.core.data;

import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.dynamicportals.api.PortalNet;
import tfc.dynamicportals.itf.NetworkHolder;
import tfc.dynamicportals.level.LevelLoader;
import tfc.dynamicportals.level.ServerLevelLoader;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

@Mixin(MinecraftServer.class)
public abstract class ServerMixin implements NetworkHolder {
    @Shadow
    @Final
    protected WorldData worldData;
    @Shadow
    @Final
    protected LevelStorageSource.LevelStorageAccess storageSource;
    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow protected abstract void endMetricsRecordingTick();

    ArrayList<PortalNet> nets = new ArrayList<>();

    ServerLevelLoader loader = new ServerLevelLoader((MinecraftServer) (Object) this);

    @Override
    public ArrayList<PortalNet> getPortalNetworks() {
        return nets;
    }

    @Override
    public LevelLoader getLoader() {
        return loader;
    }

    @Inject(at = @At("TAIL"), method = "createLevels")
    public void postLoad(ChunkProgressListener pListener, CallbackInfo ci) {
        Path pth = this.storageSource.getWorldDir();
        pth = pth.resolve("dypo_networks.dat");
        if (!Files.exists(pth))
            return; // nothing to load

        try {
            CompoundTag tag = NbtIo.readCompressed(new FileInputStream(pth.toFile()));
            ListTag list = tag.getList("nets", Tag.TAG_COMPOUND);
            for (Tag tag1 : list) {
                try {
                    nets.add(PortalNet.load(this, ((CompoundTag) tag1)));
                } catch (Throwable err) {
                    LOGGER.error("[DynamicPortals:PortalNetwork:core/data/ServerMixin] Portal Network failed to load", err);
                }
            }
        } catch (Throwable err) {
            LOGGER.error("[DynamicPortals:PortalNetwork:core/data/ServerMixin] Portal Networks failed to load", err);
        }
    }

    @Inject(at = @At("HEAD"), method = "saveAllChunks")
    public void preSave(boolean pSuppressLog, boolean pFlush, boolean pForced, CallbackInfoReturnable<Boolean> cir) {
        Path pth = this.storageSource.getWorldDir();
        pth = pth.resolve("dypo_networks.dat");
        if (nets.isEmpty() && !Files.exists(pth))
            // don't create the file if it doesn't need to exist
            return;

        try {
            CompoundTag network = new CompoundTag();
            ListTag netsList = new ListTag();
            for (PortalNet net : nets) {
                try {
                    CompoundTag me = new CompoundTag();
                    net.write(me);
                    netsList.add(me);
                } catch (Throwable err) {
                    LOGGER.error("[DynamicPortals:PortalNetwork:core/data/ServerMixin] Portal Network failed to save", err);
                }
            }
            network.put("nets", netsList);

            NbtIo.writeCompressed(
                    network, new FileOutputStream(pth.toFile())
            );
        } catch (Throwable err) {
            LOGGER.error("[DynamicPortals:PortalNetwork:core/data/ServerMixin] Portal Networks failed to save", err);
        }
    }
}
