package org.betterx.bclib.client.models;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.interfaces.ItemModelProvider;
import org.betterx.bclib.interfaces.RuntimeBlockModelProvider;
import org.betterx.bclib.models.RecordItemModelProvider;

import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.multipart.MultiPart;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomModelBakery {
    private record StateModelPair(BlockState state, UnbakedModel model) {
    }

    private final Map<ResourceLocation, UnbakedModel> models = Maps.newConcurrentMap();
    private final Map<Block, List<StateModelPair>> blockModels = Maps.newConcurrentMap();

    public UnbakedModel getBlockModel(ResourceLocation location) {
        return models.get(location);
    }

    public UnbakedModel getItemModel(ResourceLocation location) {
        return models.get(location);
    }

    public void loadCustomModels(ResourceManager resourceManager) {
        BuiltInRegistries.BLOCK.stream()
                               .filter(block -> block instanceof RuntimeBlockModelProvider)
                               .forEach(block -> {
                                   ResourceLocation blockID = BuiltInRegistries.BLOCK.getKey(block);
                                   if (blockID == null) {
                                       BCLib.LOGGER.warn("Skip runtime block model: missing registry key for {}", block);
                                       return;
                                   }
                                   try {
                                       ResourceLocation storageID = ResourceLocation.fromNamespaceAndPath(
                                               blockID.getNamespace(),
                                               "blockstates/" + blockID.getPath() + ".json"
                                       );
                                       if (resourceManager.getResource(storageID).isEmpty()) {
                                           addBlockModel(blockID, block);
                                       }
                                       storageID = ResourceLocation.fromNamespaceAndPath(
                                               blockID.getNamespace(),
                                               "models/item/" + blockID.getPath() + ".json"
                                       );
                                       if (resourceManager.getResource(storageID).isEmpty()) {
                                           addItemModel(blockID, (ItemModelProvider) block);
                                       }
                                   } catch (RuntimeException ex) {
                                       BCLib.LOGGER.error("Failed to build runtime block model for {}", blockID, ex);
                                   }
                               });

        BuiltInRegistries.ITEM.stream()
                              .filter(item -> item instanceof ItemModelProvider || RecordItemModelProvider.has(item))
                              .forEach(item -> {
                                  ResourceLocation registryID = BuiltInRegistries.ITEM.getKey(item);
                                  if (registryID == null) {
                                      BCLib.LOGGER.warn("Skip runtime item model: missing registry key for {}", item);
                                      return;
                                  }
                                  try {
                                      ResourceLocation storageID = ResourceLocation.fromNamespaceAndPath(
                                              registryID.getNamespace(),
                                              "models/item/" + registryID.getPath() + ".json"
                                      );
                                      final ItemModelProvider provider = (item instanceof ItemModelProvider)
                                              ? (ItemModelProvider) item
                                              : RecordItemModelProvider.get(item);
                                      if (provider == null) {
                                          BCLib.LOGGER.warn("Skip runtime item model: missing provider for {}", registryID);
                                          return;
                                      }

                                      if (resourceManager.getResource(storageID).isEmpty()) {
                                          addItemModel(registryID, provider);
                                      }
                                  } catch (RuntimeException ex) {
                                      BCLib.LOGGER.error("Failed to build runtime item model for {}", registryID, ex);
                                  }
                              });
    }

    private void addBlockModel(ResourceLocation blockID, Block block) {
        RuntimeBlockModelProvider provider = (RuntimeBlockModelProvider) block;
        ImmutableList<BlockState> states = block.getStateDefinition().getPossibleStates();
        BlockState defaultState = block.defaultBlockState();

        ModelResourceLocation defaultStateID = BlockModelShaper.stateToModelLocation(blockID, defaultState);
        UnbakedModel defaultModel = provider.getModelVariant(defaultStateID, defaultState, models);
        if (defaultModel == null) {
            BCLib.LOGGER.warn("Skip runtime block model: missing default model for {}", blockID);
            return;
        }

        List<StateModelPair> stateModels = new ArrayList<>(states.size());
        if (defaultModel instanceof MultiPart) {
            states.forEach(blockState -> {
                ModelResourceLocation stateID = BlockModelShaper.stateToModelLocation(blockID, blockState);
                models.put(stateID.id(), defaultModel);
                stateModels.add(new StateModelPair(blockState, defaultModel));
            });
        } else {
            states.forEach(blockState -> {
                ModelResourceLocation stateID = BlockModelShaper.stateToModelLocation(blockID, blockState);
                UnbakedModel model = stateID.equals(defaultStateID)
                        ? defaultModel
                        : provider.getModelVariant(stateID, blockState, models);
                if (model == null) {
                    BCLib.LOGGER.warn("Skip runtime block model: missing model for {}", stateID);
                    model = defaultModel;
                }
                models.put(stateID.id(), model);
                stateModels.add(new StateModelPair(blockState, model));
            });
        }
        blockModels.put(block, stateModels);
    }

    private void addItemModel(ResourceLocation itemID, ItemModelProvider provider) {
        ModelResourceLocation modelLocation = new ModelResourceLocation(
                itemID,
                "inventory"
        );

        ResourceLocation modelKey = modelLocation.id();
        if (!models.containsKey(modelKey)) {
            ResourceLocation itemModelLocation = itemID.withPrefix("item/");
            BlockModel model = provider.getItemModel(modelKey);
            if (model == null) {
                BCLib.LOGGER.warn("Skip runtime item model: missing model for {}", itemID);
                return;
            }
            models.put(modelKey, model);
            models.put(itemModelLocation, model);
        }
    }
}
