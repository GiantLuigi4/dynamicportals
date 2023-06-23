package tfc.dynamicportals.mixin.dev;

import net.minecraft.client.main.Main;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.Locale;
import java.util.Scanner;

@Mixin(Main.class)
public class RenderDocker {
	private static final Logger LOGGER = LoggerFactory.getLogger("DynamicPortals::DEBUG");
	
	@Inject(method = "main", at = @At("HEAD"), remap = false)
	private static void preMain(String[] pArgs, CallbackInfo ci) {
		if (FMLEnvironment.production) return;
		
		String pth = System.getProperty("java.library.path");
		String name = System.mapLibraryName("renderdoc");
		boolean rdDetected = false;
		for (String s : pth.split(";")) {
			if (new File(s + "/" + name).exists()) {
				rdDetected = true;
			}
		}
		
		if (rdDetected) {
			boolean[] doEnable = new boolean[]{false};
			
			Thread td = new Thread(() -> {
				LOGGER.warn("Renderdoc detected, would you like to load it? y/N");
				
				Scanner sc = new Scanner(System.in);
				while (true) {
					String ln = sc.nextLine().trim();
					if (ln.toLowerCase(Locale.ROOT).startsWith("y")) {
						doEnable[0] = true;
						return;
					} else if (ln.toLowerCase(Locale.ROOT).startsWith("n")) {
						return;
					}
				}
			});
			
			td.setDaemon(true);
			td.start();
			
			try {
				int tm = 0;
				while (tm <= 4000 && !doEnable[0]) {
					Thread.sleep(10);
					tm += 10;
				}
				//@formatter:off
				try { td.interrupt(); } catch (Throwable ignored) { }
				try { td.stop(); } catch (Throwable ignored) { }
				//@formatter:on
				
				if (doEnable[0]) {
					System.loadLibrary("renderdoc");
				}
			} catch (Throwable ignored) {
			}
		}
	}
}
