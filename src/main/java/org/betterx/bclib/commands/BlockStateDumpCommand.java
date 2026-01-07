package org.betterx.bclib.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
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
import org.betterx.bclib.mixin.common.IdMapperAccessor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BlockStateDumpCommand {
    private static final int DEFAULT_SAMPLE = 10;
    private static final int MAX_SAMPLE = 200;
    private static final int MAX_DUMP_RANGE = 500;
    private static final String DEFAULT_PREFIX = "blockstate-dump-";
    private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final long FNV_OFFSET = 0xcbf29ce484222325L;
    private static final long FNV_PRIME = 0x100000001b3L;

    public static LiteralArgumentBuilder<CommandSourceStack> register(
            LiteralArgumentBuilder<CommandSourceStack> bnContext
    ) {
        return bnContext
                .then(Commands.literal("blockstate_dump")
                              .requires(source -> source.hasPermission(Commands.LEVEL_OWNERS))
                              .executes(ctx -> dump(ctx, DEFAULT_SAMPLE))
                              .then(Commands.argument("count", IntegerArgumentType.integer(1, MAX_SAMPLE))
                                            .executes(ctx -> dump(ctx, IntegerArgumentType.getInteger(ctx, "count")))
                              )
                )
                .then(Commands.literal("blockstate_dump_range")
                              .requires(source -> source.hasPermission(Commands.LEVEL_OWNERS))
                              .then(Commands.argument("start", IntegerArgumentType.integer(0))
                                            .then(Commands.argument("count", IntegerArgumentType.integer(1, MAX_DUMP_RANGE))
                                                          .executes(ctx -> dumpRange(
                                                                  ctx,
                                                                  IntegerArgumentType.getInteger(ctx, "start"),
                                                                  IntegerArgumentType.getInteger(ctx, "count")
                                                          ))
                                            )
                              )
                )
                .then(Commands.literal("blockstate_hash")
                              .requires(source -> source.hasPermission(Commands.LEVEL_OWNERS))
                              .then(Commands.argument("start", IntegerArgumentType.integer(0))
                                            .then(Commands.argument("count", IntegerArgumentType.integer(1))
                                                          .executes(ctx -> hashRangeCommand(
                                                                  ctx,
                                                                  IntegerArgumentType.getInteger(ctx, "start"),
                                                                  IntegerArgumentType.getInteger(ctx, "count")
                                                          ))
                                            )
                              )
                )
                .then(Commands.literal("blockstate_dump_file")
                              .requires(source -> source.hasPermission(Commands.LEVEL_OWNERS))
                              .executes(ctx -> dumpFile(ctx, null))
                              .then(Commands.argument("name", StringArgumentType.word())
                                            .executes(ctx -> dumpFile(
                                                    ctx,
                                                    StringArgumentType.getString(ctx, "name")
                                            ))
                              )
                );
    }

    private static int dump(CommandContext<CommandSourceStack> ctx, int sample) {
        int size = Block.BLOCK_STATE_REGISTRY.size();
        int canonicalMaxId = getCanonicalMaxId();
        int span = getSpan();
        int maxId = span > 0 ? span - 1 : -1;
        long hash = hashRange(0, span);
        CommandSourceStack source = ctx.getSource();

        source.sendSuccess(() -> Component.literal("BlockState registry size: " + size), false);
        source.sendSuccess(() -> Component.literal("BlockState registry canonical max id: " + canonicalMaxId), false);
        source.sendSuccess(() -> Component.literal("BlockState registry max id: " + maxId), false);
        source.sendSuccess(() -> Component.literal("BlockState registry span: " + span), false);
        source.sendSuccess(() -> Component.literal("BlockState registry hash: " + Long.toUnsignedString(hash, 16)), false);

        if (span == 0) {
            return Command.SINGLE_SUCCESS;
        }

        int aliasCount = countAliasIds(span);
        if (aliasCount > 0) {
            source.sendSuccess(() -> Component.literal("BlockState registry alias ids: " + aliasCount), false);
        }

        int head = Math.min(sample, span);
        source.sendSuccess(() -> Component.literal("First " + head + ":"), false);
        for (int i = 0; i < head; i++) {
            int id = i;
            BlockState state = Block.stateById(id);
            MutableComponent line = formatLine(id, state);
            source.sendSuccess(() -> line, false);
        }

        if (span > head) {
            int tailStart = Math.max(0, span - head);
            source.sendSuccess(() -> Component.literal("Last " + head + ":"), false);
            for (int i = tailStart; i < span; i++) {
                int id = i;
                BlockState state = Block.stateById(id);
                MutableComponent line = formatLine(id, state);
                source.sendSuccess(() -> line, false);
            }
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int dumpRange(CommandContext<CommandSourceStack> ctx, int start, int count) {
        int span = getSpan();
        int maxId = span > 0 ? span - 1 : -1;
        CommandSourceStack source = ctx.getSource();
        if (span == 0) {
            source.sendSuccess(() -> Component.literal("BlockState registry size: 0"), false);
            return Command.SINGLE_SUCCESS;
        }
        if (start >= span) {
            source.sendFailure(Component.literal("Start index " + start + " is out of range (max id " + maxId + ")"));
            return 0;
        }

        int end = Math.min(span, start + count);
        source.sendSuccess(() -> Component.literal("Range " + start + ".." + (end - 1) + ":"), false);
        for (int i = start; i < end; i++) {
            int id = i;
            BlockState state = Block.stateById(id);
            MutableComponent line = formatLine(id, state);
            source.sendSuccess(() -> line, false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int hashRangeCommand(CommandContext<CommandSourceStack> ctx, int start, int count) {
        int span = getSpan();
        int maxId = span > 0 ? span - 1 : -1;
        CommandSourceStack source = ctx.getSource();
        if (span == 0) {
            source.sendSuccess(() -> Component.literal("BlockState registry size: 0"), false);
            return Command.SINGLE_SUCCESS;
        }
        if (start >= span) {
            source.sendFailure(Component.literal("Start index " + start + " is out of range (max id " + maxId + ")"));
            return 0;
        }

        int end = Math.min(span, start + count);
        long hash = hashRange(start, end);
        MutableComponent message = Component.literal(
                "BlockState hash " + start + ".." + (end - 1) + ": " + Long.toUnsignedString(hash, 16)
        );
        source.sendSuccess(() -> message, false);
        return Command.SINGLE_SUCCESS;
    }

    private static int dumpFile(CommandContext<CommandSourceStack> ctx, String name) {
        int size = Block.BLOCK_STATE_REGISTRY.size();
        int canonicalMaxId = getCanonicalMaxId();
        int span = getSpan();
        int maxId = span > 0 ? span - 1 : -1;
        CommandSourceStack source = ctx.getSource();

        String filename = resolveDumpFilename(name);
        Path outDir = Paths.get("bclib");
        Path outFile = outDir.resolve(filename);

        try {
            Files.createDirectories(outDir);
        } catch (IOException e) {
            source.sendFailure(Component.literal("Failed to create output directory: " + outDir));
            return 0;
        }

        long hash = FNV_OFFSET;
        int aliasCount = 0;
        try (BufferedWriter writer = Files.newBufferedWriter(outFile, StandardCharsets.US_ASCII)) {
            writer.write("# BlockState registry dump");
            writer.newLine();
            writer.write("# size: " + size);
            writer.newLine();
            writer.write("# canonical_max_id: " + canonicalMaxId);
            writer.newLine();
            writer.write("# max_id: " + maxId);
            writer.newLine();
            writer.write("# span: " + span);
            writer.newLine();

            for (int id = 0; id < span; id++) {
                BlockState state = Block.stateById(id);
                String stateText = state == null ? "null" : formatState(state);
                if (state != null && Block.BLOCK_STATE_REGISTRY.getId(state) != id) {
                    aliasCount++;
                }
                hash = fnv1a(hash, stateText);
                hash = fnv1a(hash, "\n");
                writer.write(id + "\t" + stateText);
                writer.newLine();
            }

            writer.write("# alias_ids: " + aliasCount);
            writer.newLine();
            writer.write("# hash: " + Long.toUnsignedString(hash, 16));
            writer.newLine();
        } catch (IOException e) {
            source.sendFailure(Component.literal("Failed to write dump: " + outFile));
            return 0;
        }

        String resultPath = outDir.resolve(filename).toString();
        source.sendSuccess(() -> Component.literal("Wrote blockstate dump to " + resultPath), false);
        return Command.SINGLE_SUCCESS;
    }

    private static MutableComponent formatLine(int id, BlockState state) {
        String display = state == null ? "null" : formatState(state);
        MutableComponent result = Component.literal(id + ": ")
                                           .setStyle(Style.EMPTY.withColor(ChatFormatting.BLUE));
        result.append(Component.literal(display)
                               .setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));
        return result;
    }

    private static String formatState(BlockState state) {
        String blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
        List<Property<?>> props = new ArrayList<>(state.getProperties());
        if (props.isEmpty()) {
            return blockId;
        }

        props.sort(Comparator.comparing(Property::getName));
        StringBuilder sb = new StringBuilder();
        for (Property<?> property : props) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(property.getName()).append("=").append(state.getValue(property));
        }

        return blockId + "[" + sb + "]";
    }

    private static long hashRange(int start, int end) {
        long hash = FNV_OFFSET;
        for (int id = start; id < end; id++) {
            BlockState state = Block.stateById(id);
            hash = fnv1a(hash, state == null ? "null" : formatState(state));
            hash = fnv1a(hash, "\n");
        }
        return hash;
    }

    private static int getMaxId() {
        int maxId = -1;
        for (BlockState state : Block.BLOCK_STATE_REGISTRY) {
            int id = Block.BLOCK_STATE_REGISTRY.getId(state);
            if (id > maxId) {
                maxId = id;
            }
        }
        return maxId;
    }

    private static int getCanonicalMaxId() {
        return getMaxId();
    }

    private static int getSpan() {
        if (Block.BLOCK_STATE_REGISTRY instanceof IdMapperAccessor accessor) {
            return accessor.bclib_getIdToT().size();
        }
        int maxId = getCanonicalMaxId();
        return maxId >= 0 ? maxId + 1 : 0;
    }

    private static int countAliasIds(int span) {
        int count = 0;
        for (int id = 0; id < span; id++) {
            BlockState state = Block.stateById(id);
            if (state != null && Block.BLOCK_STATE_REGISTRY.getId(state) != id) {
                count++;
            }
        }
        return count;
    }

    private static String resolveDumpFilename(String name) {
        if (name == null || name.isBlank()) {
            return DEFAULT_PREFIX + LocalDateTime.now().format(FILE_TS) + ".txt";
        }
        String trimmed = name.trim();
        if (!trimmed.endsWith(".txt")) {
            trimmed = trimmed + ".txt";
        }
        return trimmed;
    }

    private static long fnv1a(long hash, String value) {
        for (int i = 0; i < value.length(); i++) {
            hash ^= value.charAt(i);
            hash *= FNV_PRIME;
        }
        return hash;
    }
}
