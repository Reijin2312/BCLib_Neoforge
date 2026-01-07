package org.betterx.bclib.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockStateIdCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register(LiteralArgumentBuilder<CommandSourceStack> bnContext) {
        return bnContext
                .then(Commands.literal("blockstate_id")
                              .requires(source -> source.hasPermission(Commands.LEVEL_OWNERS))
                              .then(Commands.argument("id", IntegerArgumentType.integer(0))
                                            .executes(BlockStateIdCommand::printBlockState)
                              )
                );
    }

    private static int printBlockState(CommandContext<CommandSourceStack> ctx) {
        int id = IntegerArgumentType.getInteger(ctx, "id");
        BlockState state = Block.stateById(id);
        if (state == null) {
            ctx.getSource().sendFailure(Component.literal("No blockstate for id " + id));
            return 0;
        }

        String blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
        StringBuilder props = new StringBuilder();
        state.getValues().forEach((Property<?> property, Comparable<?> value) -> {
            if (props.length() > 0) {
                props.append(",");
            }
            props.append(property.getName()).append("=").append(value);
        });

        String display = props.length() > 0 ? blockId + "[" + props + "]" : blockId;
        int canonicalId = Block.BLOCK_STATE_REGISTRY.getId(state);
        if (canonicalId >= 0 && canonicalId != id) {
            display = display + " (canonical id: " + canonicalId + ")";
        }

        MutableComponent result = Component.literal("BlockState id " + id + ": ")
                                           .setStyle(Style.EMPTY.withBold(true).withColor(ChatFormatting.BLUE));
        result.append(Component.literal(display)
                               .setStyle(Style.EMPTY.withBold(false).withColor(ChatFormatting.WHITE)));
        ctx.getSource().sendSuccess(() -> result, false);
        return Command.SINGLE_SUCCESS;
    }
}
