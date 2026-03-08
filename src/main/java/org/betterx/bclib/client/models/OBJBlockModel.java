package org.betterx.bclib.client.models;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.util.BlocksHelper;
import org.betterx.bclib.util.MHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.client.resources.model.UnbakedGeometry;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import net.neoforged.neoforge.client.RenderTypeGroup;
import net.neoforged.neoforge.client.model.AbstractUnbakedModel;
import net.neoforged.neoforge.client.model.StandardModelParameters;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.joml.Vector3f;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OBJBlockModel extends AbstractUnbakedModel {
    private static final Vector3f[] POSITIONS = new Vector3f[]{new Vector3f(), new Vector3f(), new Vector3f()};

    private final Map<Direction, List<UnbakedQuad>> quadsUnbakedMap = Maps.newEnumMap(Direction.class);
    private final List<UnbakedQuad> quadsUnbaked = Lists.newArrayList();
    private final ObjGeometry geometry;

    public OBJBlockModel(
            Identifier location,
            Vector3f offset,
            boolean useCulling,
            boolean useShading,
            byte particleIndex,
            Identifier... textureIDs
    ) {
        super(new StandardModelParameters(
                null,
                createTextureSlots(textureIDs, particleIndex),
                ItemTransforms.NO_TRANSFORMS,
                Boolean.TRUE,
                net.minecraft.client.resources.model.UnbakedModel.GuiLight.SIDE,
                null,
                RenderTypeGroup.EMPTY,
                Map.of()
        ));

        for (Direction dir : BlocksHelper.DIRECTIONS) {
            quadsUnbakedMap.put(dir, Lists.newArrayList());
        }

        loadModel(location, new Vector3f(offset), useCulling, useShading, (byte) (textureIDs.length - 1));
        this.geometry = new ObjGeometry(textureIDs.length);
    }

    @Override
    public UnbakedGeometry geometry() {
        return geometry;
    }

    private static TextureSlots.Data createTextureSlots(Identifier[] textureIDs, int particleIndex) {
        TextureSlots.Data.Builder builder = new TextureSlots.Data.Builder();
        for (int i = 0; i < textureIDs.length; i++) {
            builder.addTexture(Integer.toString(i), new Material(net.minecraft.client.resources.model.ModelManager.BLOCK_OR_ITEM, textureIDs[i]));
        }
        if (textureIDs.length > 0) {
            int safeParticleIndex = Math.max(0, Math.min(textureIDs.length - 1, particleIndex));
            builder.addReference("particle", Integer.toString(safeParticleIndex));
        }
        return builder.build();
    }

    private static Resource getResource(ResourceManager resourceManager, Identifier location) {
        return resourceManager.getResource(location).orElse(null);
    }

    private void loadModel(Identifier location, Vector3f offset, boolean useCulling, boolean useShading, byte maxIndex) {
        Resource resource = getResource(Minecraft.getInstance().getResourceManager(), location);
        if (resource == null) {
            return;
        }

        try (InputStream input = resource.open();
             InputStreamReader streamReader = new InputStreamReader(input);
             BufferedReader reader = new BufferedReader(streamReader)) {

            List<Float> vertices = new ArrayList<>(12);
            List<Float> uvs = new ArrayList<>(8);
            List<Integer> vertexIndex = new ArrayList<>(4);
            List<Integer> uvIndex = new ArrayList<>(4);

            byte materialIndex = -1;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("usemtl")) {
                    materialIndex++;
                    if (materialIndex > maxIndex) {
                        materialIndex = maxIndex;
                    }
                } else if (line.startsWith("vt")) {
                    String[] uv = line.split(" ");
                    uvs.add(Float.parseFloat(uv[1]));
                    uvs.add(Float.parseFloat(uv[2]));
                } else if (line.startsWith("v ")) {
                    String[] vert = line.split(" ");
                    for (int i = 1; i < 4; i++) {
                        vertices.add(Float.parseFloat(vert[i]));
                    }
                } else if (line.startsWith("f")) {
                    String[] members = line.split(" ");
                    if (members.length != 5) {
                        BCLib.LOGGER.warn("Only quad faces are supported in OBJ model {}", location);
                        continue;
                    }
                    vertexIndex.clear();
                    uvIndex.clear();

                    for (int i = 1; i < members.length; i++) {
                        String member = members[i];
                        if (member.contains("/")) {
                            String[] sub = member.split("/");
                            vertexIndex.add(Integer.parseInt(sub[0]) - 1);
                            if (sub.length > 1 && !sub[1].isEmpty()) {
                                uvIndex.add(Integer.parseInt(sub[1]) - 1);
                            }
                        } else {
                            vertexIndex.add(Integer.parseInt(member) - 1);
                        }
                    }

                    boolean hasUV = !uvIndex.isEmpty();
                    UnbakedQuad quad = new UnbakedQuad();
                    for (int i = 0; i < 4; i++) {
                        int index = vertexIndex.get(i) * 3;
                        int quadIndex = i * 5;
                        quad.addData(quadIndex++, vertices.get(index++) + offset.x());
                        quad.addData(quadIndex++, vertices.get(index++) + offset.y());
                        quad.addData(quadIndex++, vertices.get(index) + offset.z());
                        if (hasUV) {
                            index = uvIndex.get(i) * 2;
                            quad.addData(quadIndex++, uvs.get(index++) * 16F);
                            quad.addData(quadIndex, (1 - uvs.get(index)) * 16F);
                        }
                    }
                    quad.setSpriteIndex(materialIndex);
                    if (useShading) {
                        quad.setDirection(getNormalDirection(quad));
                        quad.setShading(true);
                    }
                    if (useCulling) {
                        Direction dir = getCullingDirection(quad);
                        if (dir == null) {
                            quadsUnbaked.add(quad);
                        } else {
                            quadsUnbakedMap.get(dir).add(quad);
                        }
                    } else {
                        quadsUnbaked.add(quad);
                    }
                }
            }

            if (materialIndex < 0) {
                quadsUnbaked.forEach(quad -> quad.setSpriteIndex(0));
                quadsUnbakedMap.values().forEach(list -> list.forEach(quad -> quad.setSpriteIndex(0)));
            }
        } catch (IOException e) {
            BCLib.LOGGER.error("Unable to load OBJ model {}", location, e);
        }
    }

    private Direction getNormalDirection(UnbakedQuad quad) {
        Vector3f pos = quad.getPos(0, POSITIONS[0]);
        Vector3f dirA = quad.getPos(1, POSITIONS[1]);
        Vector3f dirB = quad.getPos(2, POSITIONS[2]);
        dirA.sub(pos);
        dirB.sub(pos);
        pos = MHelper.cross(dirA, dirB);
        return Direction.getApproximateNearest(pos.x(), pos.y(), pos.z());
    }

    private @Nullable Direction getCullingDirection(UnbakedQuad quad) {
        Direction dir = null;
        for (int i = 0; i < 4; i++) {
            Vector3f pos = quad.getPos(i, POSITIONS[0]);
            if (pos.x() < 1 && pos.x() > 0 && pos.y() < 1 && pos.y() > 0 && pos.z() < 1 && pos.z() > 0) {
                return null;
            }
            Direction newDir = Direction.getApproximateNearest(pos.x() - 0.5F, pos.y() - 0.5F, pos.z() - 0.5F);
            if (dir == null) {
                dir = newDir;
            } else if (newDir != dir) {
                return null;
            }
        }
        return dir;
    }

    private class ObjGeometry implements UnbakedGeometry {
        private final int textureCount;

        private ObjGeometry(int textureCount) {
            this.textureCount = textureCount;
        }

        @Override
        public QuadCollection bake(TextureSlots slots, ModelBaker baker, ModelState state, ModelDebugName debugName) {
            TextureAtlasSprite[] sprites = new TextureAtlasSprite[Math.max(1, textureCount)];
            for (int i = 0; i < sprites.length; i++) {
                sprites[i] = baker.sprites().resolveSlot(slots, Integer.toString(i), debugName);
            }

            QuadCollection.Builder builder = new QuadCollection.Builder();
            quadsUnbaked.forEach(quad -> builder.addUnculledFace(quad.bake(sprites, state)));
            for (Map.Entry<Direction, List<UnbakedQuad>> entry : quadsUnbakedMap.entrySet()) {
                Direction cullDir = entry.getKey();
                entry.getValue().forEach(quad -> builder.addCulledFace(cullDir, quad.bake(sprites, state)));
            }
            return builder.build();
        }
    }
}
