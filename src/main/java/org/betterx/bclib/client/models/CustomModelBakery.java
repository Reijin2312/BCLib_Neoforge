package org.betterx.bclib.client.models;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.interfaces.ItemModelProvider;
import org.betterx.bclib.interfaces.RuntimeBlockModelProvider;
import org.betterx.bclib.models.RecordItemModelProvider;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.multipart.MultiPartModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomModelBakery {
    private record StateModelPair(BlockState state, BlockStateModel.UnbakedRoot model) {
    }

    private final Map<Identifier, UnbakedModel> models = Maps.newConcurrentMap();
    private final Map<Identifier, BlockStateModel.UnbakedRoot> blockStateModels = Maps.newConcurrentMap();
    private final Map<Block, List<StateModelPair>> blockModels = Maps.newConcurrentMap();

    public UnbakedModel getBlockModel(Identifier location) {
        return models.get(location);
    }

    public BlockStateModel.UnbakedRoot getBlockStateModel(Identifier location) {
        return blockStateModels.get(location);
    }

    public UnbakedModel getItemModel(Identifier location) {
        return models.get(location);
    }

    public void loadCustomModels(ResourceManager resourceManager) {
        BuiltInRegistries.BLOCK.stream()
                               .filter(block -> block instanceof RuntimeBlockModelProvider)
                               .forEach(block -> {
                                   Identifier blockID = BuiltInRegistries.BLOCK.getKey(block);
                                   if (blockID == null) {
                                       BCLib.LOGGER.warn("Skip runtime block model: missing registry key for {}", block);
                                       return;
                                   }
                                   try {
                                       Identifier storageID = Identifier.fromNamespaceAndPath(
                                               blockID.getNamespace(),
                                               "blockstates/" + blockID.getPath() + ".json"
                                       );
                                       if (resourceManager.getResource(storageID).isEmpty()) {
                                           addBlockModel(blockID, block);
                                       }
                                       storageID = Identifier.fromNamespaceAndPath(
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
                                  Identifier registryID = BuiltInRegistries.ITEM.getKey(item);
                                  if (registryID == null) {
                                      BCLib.LOGGER.warn("Skip runtime item model: missing registry key for {}", item);
                                      return;
                                  }
                                  try {
                                      Identifier storageID = Identifier.fromNamespaceAndPath(
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

    private void addBlockModel(Identifier blockID, Block block) {
        RuntimeBlockModelProvider provider = (RuntimeBlockModelProvider) block;
        ImmutableList<BlockState> states = block.getStateDefinition().getPossibleStates();
        BlockState defaultState = block.defaultBlockState();

        Identifier defaultStateID = blockID;
        BlockStateModel.UnbakedRoot defaultModel = provider.getModelVariant(defaultStateID, defaultState, models);
        if (defaultModel == null) {
            BCLib.LOGGER.warn("Skip runtime block model: missing default model for {}", blockID);
            return;
        }

        List<StateModelPair> stateModels = new ArrayList<>(states.size());
        if (defaultModel instanceof MultiPartModel.Unbaked) {
            states.forEach(blockState -> {
                Identifier stateKey = stateKey(blockID, blockState);
                blockStateModels.put(stateKey, defaultModel);
                stateModels.add(new StateModelPair(blockState, defaultModel));
            });
        } else {
            states.forEach(blockState -> {
                Identifier stateID = blockID;
                BlockStateModel.UnbakedRoot model = blockState.equals(defaultState)
                        ? defaultModel
                        : provider.getModelVariant(stateID, blockState, models);
                if (model == null) {
                    BCLib.LOGGER.warn("Skip runtime block model: missing model for {} {}", blockID, blockState);
                    model = defaultModel;
                }
                blockStateModels.put(stateKey(blockID, blockState), model);
                stateModels.add(new StateModelPair(blockState, model));
            });
        }
        blockModels.put(block, stateModels);
    }

    private void addItemModel(Identifier itemID, ItemModelProvider provider) {
        Identifier modelKey = itemID;
        if (!models.containsKey(modelKey)) {
            Identifier itemModelLocation = itemID.withPrefix("item/");
            BlockModel model = provider.getItemModel(modelKey);
            if (model == null) {
                BCLib.LOGGER.warn("Skip runtime item model: missing model for {}", itemID);
                return;
            }
            models.put(modelKey, model);
            models.put(itemModelLocation, model);
        }
    }

    private static Identifier stateKey(Identifier blockId, BlockState blockState) {
        return Identifier.fromNamespaceAndPath(
                blockId.getNamespace(),
                "runtime_blockstates/" + blockId.getPath() + "_" + Integer.toUnsignedString(blockState.hashCode(), 16)
        );
    }
}
