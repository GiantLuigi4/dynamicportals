package tfc.dynamicportals.mixin.client.optimization;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.dynamicportals.Renderer;
import tfc.dynamicportals.util.async.ReusableThread;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Unique
	private static final ReusableThread[] threads = new ReusableThread[4];
	@Unique
	private final Object lock = new Object();
	@Shadow
	@Final
	private Minecraft minecraft;
	@Unique
	private Matrix4f projection;
	@Unique
	private boolean isRendering = false;
	@Unique
	private PoseStack stack;

	@Inject(at = @At("TAIL"), method = "<clinit>")
	private static void postStaticInit(CallbackInfo ci) {
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new ReusableThread(() -> {
			});
			Renderer.addThread(threads[i]);
		}
	}

	@Inject(at = @At("HEAD"), method = "renderLevel")
	public void preRenderLevel(float f1, long vector3f, PoseStack f2, CallbackInfo ci) {
		isRendering = true;
		stack = f2;
	}

	@Inject(at = @At("HEAD"), method = "renderLevel")
	public void postRenderLevel(float f1, long vector3f, PoseStack f2, CallbackInfo ci) {
		isRendering = false;
	}

	@Inject(at = @At("HEAD"), method = "resetProjectionMatrix")
	public void preSetProjMat(Matrix4f pMatrix, CallbackInfo ci) {
		// TODO: get this working
//		projection = RenderSystem.getProjectionMatrix();
//		// TODO: this can be made even less blocking
//		Matrix4f mat = stack.last().pose();
//		AbstractPortal[] portals = Temp.getPortals(minecraft.level);
//		for (AbstractPortal portal : portals) {
////			scheduleNext(() -> {
//				Renderer.updatePortal(portal, mat, pMatrix);
////			});
//		}

//		if (portals.length != 0) {
//			int[] index = new int[]{0};
//			scheduleNext(() -> {
//				update(portals, portals[index[0]], index, mat);
//			});
//		}
	}

//	@Unique
//	private void update(AbstractPortal[] portals, AbstractPortal portal, int[] index, Matrix4f mat) {
////		synchronized (lock) {
//		for (ReusableThread thread : threads) {
//			if (!thread.isRunning()) {
//				index[0] = index[0] + 1;
//				if (index[0] < portals.length) {
//					AbstractPortal portal1 = portals[index[0]];
//					thread.setAction(() -> {
//						update(portals, portal1, index, mat);
//					});
//					thread.start();
//				}
//			}
//		}
////		}
//		Renderer.updatePortal(portal, mat, projection);
//		if (threads[0].isCurrentThread()) {
//			synchronized (lock) {
//				index[0] = index[0] + 1;
//				if (index[0] < portals.length) {
//					AbstractPortal portal1 = portals[index[0]];
//					scheduleNext(() -> {
//						update(portals, portal1, index, mat);
//					});
//				}
//			}
//		}
//	}

	@Unique
	private void scheduleNext(Runnable r) {
		while (true) {
			for (ReusableThread thread : threads) {
				if (!thread.isRunning()) {
					thread.setAction(r);
					thread.start();
					return;
				}
			}
		}
	}
}
