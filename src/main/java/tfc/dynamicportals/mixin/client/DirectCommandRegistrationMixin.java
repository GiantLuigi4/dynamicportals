package tfc.dynamicportals.mixin.client;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraftforge.client.ClientCommandHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.dynamicportals.cmd.DypoCommandRegistry;

@Mixin(value = ClientCommandHandler.class, remap = false)
public class DirectCommandRegistrationMixin {
    @Inject(at= @At("TAIL"), method = "mergeServerCommands")
    private static void postMerge(CommandDispatcher<SharedSuggestionProvider> serverCommands, CommandBuildContext buildContext, CallbackInfoReturnable<CommandDispatcher<SharedSuggestionProvider>> cir) {
        CommandDispatcher<SharedSuggestionProvider> dispatcher = cir.getReturnValue();
        DypoCommandRegistry.forceRegister(dispatcher);
    }
}
