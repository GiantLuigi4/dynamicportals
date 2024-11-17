package tfc.dynamicportals.cmd;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.math.Quaternion;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.WorldCoordinates;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import tfc.dynamicportals.api.registry.BasicPortalTypes;
import tfc.dynamicportals.api.registry.PortalType;
import tfc.dynamicportals.cmd.cmdr.CmdRBridgeNode;
import tfc.dynamicportals.cmd.cmdr.CmdRContext;
import tfc.dynamicportals.cmd.cmdr.CommandNodeAccessor;
import tfc.dynamicportals.cmd.cmdr.DataHolderNode;
import tfc.dynamicportals.cmd.cmdr.exception.DypoException;
import tfc.dynamicportals.cmd.cmdr.nodes.BrigadierNode;
import tfc.dynamicportals.cmd.cmdr.nodes.ChoiceNode;
import tfc.dynamicportals.cmd.cmdr.nodes.CmdRNode;
import tfc.dynamicportals.cmd.cmdr.nodes.args.OrientationArgument;
import tfc.dynamicportals.cmd.cmdr.nodes.args.OrientationData;
import tfc.dynamicportals.cmd.cmdr.util.ContextHelper;

import java.util.function.BiConsumer;

public class CommandRegistry {
    public static void register(RegisterCommandsEvent event) {
        CmdRNode root = BrigadierNode.literal("dynamic_portals");
        {
            CmdRNode network = BrigadierNode.literal("network");

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

            BasicPortalTypes.forEach((k, v) -> {
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
        event.getDispatcher().getRoot().addChild(
                new CmdRBridgeNode<>(
                        (CommandDispatcher) event.getDispatcher(),
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
                    Quaternion quaternion = data.asQuaternion((CommandSourceStack) ctx.getSource());
                    nbt.putIntArray("orientation", new int[]{
                            Float.floatToIntBits(quaternion.i()),
                            Float.floatToIntBits(quaternion.j()),
                            Float.floatToIntBits(quaternion.k()),
                            Float.floatToIntBits(quaternion.r())
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
                Vec2 position = ctx.getArgument("size", Coordinates.class).getRotation(
                        (CommandSourceStack) ctx.getSource()
                );

                nbt.putLongArray(
                        "size",
                        new long[]{
                                Double.doubleToLongBits(position.x),
                                Double.doubleToLongBits(position.y),
                        }
                );

                return nbt;
            });
            sizeRoot.addArg(sizeArg);
            sizeArg.addArg(repeat);
            repeat.requireArg(sizeRoot);
        }
    }
}
