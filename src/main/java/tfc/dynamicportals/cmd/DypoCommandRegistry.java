package tfc.dynamicportals.cmd;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.WorldCoordinates;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import org.joml.Quaterniond;
import org.objectweb.asm.tree.ClassNode;
import tfc.dynamicportals.api.registry.PortalType;
import tfc.dynamicportals.api.registry.PortalTypes;
import tfc.dynamicportals.cmd.cmdr.CmdRBridgeNode;
import tfc.dynamicportals.cmd.cmdr.CmdRContext;
import tfc.dynamicportals.cmd.cmdr.CommandNodeAccessor;
import tfc.dynamicportals.cmd.cmdr.exception.DypoException;
import tfc.dynamicportals.cmd.cmdr.nodes.BrigadierNode;
import tfc.dynamicportals.cmd.cmdr.nodes.ChoiceNode;
import tfc.dynamicportals.cmd.cmdr.nodes.CmdRNode;
import tfc.dynamicportals.cmd.cmdr.nodes.args.OrientationArgument;
import tfc.dynamicportals.cmd.cmdr.nodes.args.OrientationData;
import tfc.dynamicportals.cmd.cmdr.util.ContextHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.BiConsumer;

public class DypoCommandRegistry {
    // mojang goobered up my command system
    // therefore I goober their system
    public static void forceRegister(CommandDispatcher<SharedSuggestionProvider> dispatcher) {
        CommandNode[] nd = new CommandNode[1];
        dispatcher.getRoot().getChildren().forEach(chld -> {
            if (chld.getName().equals("dynamic_portals"))
                nd[0] = chld;
        });
        if (nd[0] == null) return;
        ;

        dispatcher.getRoot().getChildren().remove(nd[0]);
        try {
            Field f = CommandNode.class.getDeclaredField("literals");
            f.setAccessible(true);
            Map mp = (Map) f.get(dispatcher.getRoot());
            mp.remove(nd[0].getName());
        } catch (Throwable err) {
            throw new RuntimeException("wth");
        }
        register(dispatcher, true);
    }

    public static void register(CommandDispatcher<SharedSuggestionProvider> dispatcher, boolean client) {
        CmdRNode root = BrigadierNode.literal("dynamic_portals");

        root.postAction((contex, nil) -> {
            CommandContext<CommandSourceStack> ctx = (CommandContext<CommandSourceStack>) contex;
            ctx.getSource().sendSuccess(
                    () -> Component.translatable(
                            "dynamicportals.command.bread.help"
                    ),
                    true
            );
            return 1;
        });

        {
            CmdRNode network = BrigadierNode.literal("network");
            network.postAction((contex, nil) -> {
                CommandContext<CommandSourceStack> ctx = (CommandContext<CommandSourceStack>) contex;
                ctx.getSource().sendSuccess(
                        () -> Component.translatable(
                                "dynamicportals.command.bread.help.network"
                        ),
                        true
                );
                return 1;
            });

            CmdRNode create = BrigadierNode.literal("create").postAction((t, a) -> DypoCommand.createNetwork((CommandContext<?>) t));
            CmdRNode delete = BrigadierNode.literal("delete").setAction((a) -> {
                CommandNodeAccessor.throwUnchecked(new DypoException("NYI"));
                throw new RuntimeException("wth");
            });
            network.addArg(create);
            network.addArg(delete);
            CmdRNode networkName = BrigadierNode.stringArg("network");
            create.addArg(networkName);

            root.addArg(network);
        }
        {
            CmdRNode portal = BrigadierNode.literal("portal");
            portal.postAction((contex, nil) -> {
                CommandContext<CommandSourceStack> ctx = (CommandContext<CommandSourceStack>) contex;
                ctx.getSource().sendSuccess(
                        () -> Component.translatable(
                                "dynamicportals.command.bread.help.portal"
                        ),
                        true
                );
                return 1;
            });

            CmdRNode create = BrigadierNode.literal("create")
                    .setAction((a) -> new CompoundTag())
                    .postAction((ctx, tag) -> DypoCommand.createPortal((CommandContext<?>) ctx, (CompoundTag) tag));
            CmdRNode networkName = BrigadierNode.stringArg("network");
            create.addArg(networkName);

            CmdRNode modify = BrigadierNode.literal("modify").setAction((a) -> {
                CommandNodeAccessor.throwUnchecked(new DypoException("NYI"));
                throw new RuntimeException("wth");
            });

            CmdRNode delete = BrigadierNode.literal("delete").setAction((a) -> {
                CommandNodeAccessor.throwUnchecked(new DypoException("NYI"));
                throw new RuntimeException("wth");
            });

            PortalTypes.forEach((k, v) -> {
                if (v.supportsCommand()) {
                    CmdRNode branchCreate = BrigadierNode.literal(k.toString()).setAction((nbt) -> {
                        ((CompoundTag) nbt).putString("type", k.toString());
                        return nbt;
                    });
                    CmdRNode branchModif = BrigadierNode.literal(k.toString()).setAction((a) -> {
                        CommandNodeAccessor.throwUnchecked(new DypoException("NYI"));
                        throw new RuntimeException("wth");
                    });

                    v.fillCommand(branchCreate, branchModif);

                    networkName.addArg(branchCreate);
                    modify.addArg(branchModif);
                }
            });

            portal.addArg(create);
            portal.addArg(modify);
            portal.addArg(delete);
            root.addArg(portal);
        }

        //noinspection RedundantCast
        dispatcher.getRoot().addChild(
                new CmdRBridgeNode<>(
                        (CommandDispatcher) dispatcher,
                        client,
                        "dynamic_portals",
                        root,
                        (o) -> {
                            if (o instanceof CommandSourceStack stk) {
                                return stk.hasPermission(4);
                            } else if (o instanceof ClientSuggestionProvider provider) {
                                return provider.hasPermission(4);
                            }
                            throw new RuntimeException("what");
                        },
                        null, null,
                        false
                )
        );
    }

    public static void registerC(RegisterClientCommandsEvent event) {
        register((CommandDispatcher<SharedSuggestionProvider>) (Object) event.getDispatcher(), true);
    }

    public static void registerS(RegisterCommandsEvent event) {
        register((CommandDispatcher<SharedSuggestionProvider>) (Object) event.getDispatcher(), false);
    }

    public static <T extends CommandContext<V>, V> ChoiceNode<T, CompoundTag, CompoundTag> fillBase(
            PortalType<?> type,
            BiConsumer<T, CompoundTag> defaults,
            CmdRNode<T, CompoundTag, CompoundTag> create,
            CmdRNode<T, CompoundTag, CompoundTag> modify
    ) {
        create.setAction((t, a) -> {
            a.putString("type", type.getRegistryName().toString());

            CommandSourceStack src = ((CommandSourceStack) t.getSource());
            Vec3 position = src.getPosition();

            a.put("level", ContextHelper.getLevelTag(t));
            a.putLongArray(
                    "coords",
                    new long[]{
                            Double.doubleToLongBits(position.x),
                            Double.doubleToLongBits(position.y),
                            Double.doubleToLongBits(position.z)
                    }
            );

            defaults.accept(t, a);

            return a;
        });

        // fill create
        {
            ChoiceNode<T, CompoundTag, CompoundTag> repeat = new ChoiceNode<>(true);
            {
                CmdRNode<T, CompoundTag, CompoundTag> positionRoot = BrigadierNode.literal("position");
                CmdRNode<T, CompoundTag, CompoundTag> posArg = BrigadierNode.positionArg("position");
                posArg.setAction((ctx, nbt) -> {
                    Vec3 position = ctx.getArgument("position", WorldCoordinates.class).getPosition(
                            (CommandSourceStack) ctx.getSource()
                    );

                    nbt.putLongArray(
                            "coords",
                            new long[]{
                                    Double.doubleToLongBits(position.x),
                                    Double.doubleToLongBits(position.y),
                                    Double.doubleToLongBits(position.z)
                            }
                    );

                    return nbt;
                });
                positionRoot.addArg(posArg);
                posArg.addArg(repeat);
                repeat.addArg(positionRoot);
            }
            {
                CmdRNode<T, CompoundTag, CompoundTag> rotationRoot = BrigadierNode.literal("rotation");
                CmdRNode<T, CompoundTag, CompoundTag> rotationArg = new OrientationArgument<>();
                rotationArg.setAction((ctx, nbt) -> {
                    CmdRContext context = CommandNodeAccessor.getCmdRCtx(ctx);
                    OrientationData data = context.getExecData();
                    Quaterniond quaternion = data.asQuaternion((CommandSourceStack) ctx.getSource());
                    nbt.putLongArray("orientation", new long[]{
                            Double.doubleToLongBits(quaternion.x()),
                            Double.doubleToLongBits(quaternion.y()),
                            Double.doubleToLongBits(quaternion.z()),
                            Double.doubleToLongBits(quaternion.w())
                    });
                    return nbt;
                });
                rotationRoot.addArg(rotationArg);
                rotationArg.addArg(repeat);
                repeat.addArg(rotationRoot);
            }
            create.addArg(repeat);
            modify.addArg(repeat);

            return repeat;
        }
    }

    public static <T extends CommandContext<V>, V> void fillBasic(
            PortalType<?> type,
            CmdRNode<T, CompoundTag, CompoundTag> create,
            CmdRNode<T, CompoundTag, CompoundTag> modify
    ) {
        ChoiceNode<T, CompoundTag, CompoundTag> repeat = fillBase(type, (ctx, nbt) -> {
        }, create, modify);

        {
            CmdRNode<T, CompoundTag, CompoundTag> sizeRoot = BrigadierNode.literal("size");
            CmdRNode<T, CompoundTag, CompoundTag> sizeArg = BrigadierNode.vec2Arg("size");
            sizeArg.setAction((ctx, nbt) -> {
                Vec3 position = ctx.getArgument("size", Coordinates.class).getPosition(
                        (CommandSourceStack) ctx.getSource()
                );

                nbt.putLongArray(
                        "size",
                        new long[]{
                                Double.doubleToLongBits(position.x()),
                                Double.doubleToLongBits(position.z()),
                        }
                );

                return nbt;
            });
            sizeRoot.addArg(sizeArg);
            sizeArg.addArg(repeat);
            repeat.requireArg(sizeRoot);
        }
        {
            CmdRNode<T, CompoundTag, CompoundTag> doubleSidedRoot = BrigadierNode.literal("frontonly");
            CmdRNode<T, CompoundTag, CompoundTag> dbsArg = BrigadierNode.booleanArg("frontonly");
            dbsArg.setAction((ctx, nbt) -> {
                Boolean valuer = ctx.getArgument("frontonly", Boolean.class);
                nbt.putBoolean("double_sided", valuer);
                return nbt;
            });
            doubleSidedRoot.addArg(dbsArg);
            dbsArg.addArg(repeat);
            repeat.requireArg(doubleSidedRoot);
        }
    }
}
