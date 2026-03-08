package org.betterx.bclib.blockentities;

import org.betterx.bclib.registry.BaseBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.List;

public class BaseBarrelBlockEntity extends RandomizableContainerBlockEntity {
    private NonNullList<ItemStack> inventory;
    private ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {

        @Override
        protected void onOpen(Level level, BlockPos blockPos, BlockState blockState) {
            BaseBarrelBlockEntity.this.playSound(blockState, SoundEvents.BARREL_OPEN);
            BaseBarrelBlockEntity.this.updateBlockState(blockState, true);
        }

        @Override
        protected void onClose(Level level, BlockPos blockPos, BlockState blockState) {
            BaseBarrelBlockEntity.this.playSound(blockState, SoundEvents.BARREL_CLOSE);
            BaseBarrelBlockEntity.this.updateBlockState(blockState, false);
        }

        @Override
        protected void openerCountChanged(Level level, BlockPos blockPos, BlockState blockState, int i, int j) {
        }

        @Override
        public boolean isOwnContainer(Player player) {
            if (player.containerMenu instanceof ChestMenu) {
                Container container = ((ChestMenu) player.containerMenu).getContainer();
                return container == BaseBarrelBlockEntity.this;
            }
            return false;
        }
    };

    private BaseBarrelBlockEntity(BlockEntityType<?> type, BlockPos blockPos, BlockState blockState) {
        super(type, blockPos, blockState);
        this.inventory = NonNullList.withSize(27, ItemStack.EMPTY);
    }

    public BaseBarrelBlockEntity(BlockPos blockPos, BlockState blockState) {
        this(BaseBlockEntities.BARREL, blockPos, blockState);
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        if (!this.trySaveLootTable(tag)) {
            ContainerHelper.saveAllItems(tag, this.inventory);
        }
    }

    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        this.inventory = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(tag)) {
            ContainerHelper.loadAllItems(tag, this.inventory);
        }
    }

    @Override
    public int getContainerSize() {
        return 27;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.inventory;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> list) {
        this.inventory = list;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.barrel");
    }

    @Override
    protected AbstractContainerMenu createMenu(int syncId, Inventory playerInventory) {
        return ChestMenu.threeRows(syncId, playerInventory, this);
    }

    public void startOpen(ContainerUser user) {
        if (!this.remove && !user.getLivingEntity().isSpectator()) {
            this.openersCounter.incrementOpeners(
                    user.getLivingEntity(),
                    this.getLevel(),
                    this.getBlockPos(),
                    this.getBlockState(),
                    user.getContainerInteractionRange()
            );
        }
//        if (!player.isSpectator()) {
//            if (viewerCount < 0) {
//                viewerCount = 0;
//            }
//
//            ++viewerCount;
//            BlockState blockState = this.getBlockState();
//            if (!blockState.getValue(BarrelBlock.OPEN)) {
//                playSound(blockState, SoundEvents.BARREL_OPEN);
//                updateBlockState(blockState, true);
//            }
//
//            if (level != null) {
//                scheduleUpdate();
//            }
//        }
    }

    private void scheduleUpdate() {
        level.scheduleTick(getBlockPos(), getBlockState().getBlock(), 5);
    }

    public void tick() {
//        if (level != null) {
//            viewerCount = ChestBlockEntity.getOpenCount(level, worldPosition);
//            if (viewerCount > 0) {
//                scheduleUpdate();
//            } else {
//                BlockState blockState = getBlockState();
//                if (!(blockState.getBlock() instanceof BaseBarrelBlock)) {
//                    setRemoved();
//                    return;
//                }
//                if (blockState.getValue(BarrelBlock.OPEN)) {
//                    playSound(blockState, SoundEvents.BARREL_CLOSE);
//                    updateBlockState(blockState, false);
//                }
//            }
//        }
    }

    @Override
    public void stopOpen(ContainerUser user) {
        if (!this.remove && !user.getLivingEntity().isSpectator()) {
            this.openersCounter.decrementOpeners(user.getLivingEntity(), this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    @Override
    public List<ContainerUser> getEntitiesWithContainerOpen() {
        return this.openersCounter.getEntitiesWithContainerOpen(this.getLevel(), this.getBlockPos());
    }

    public void recheckOpen() {
        if (!this.remove) {
            this.openersCounter.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    private void updateBlockState(BlockState state, boolean open) {
        if (level != null) {
            level.setBlock(this.getBlockPos(), state.setValue(BarrelBlock.OPEN, open), 3);
        }
    }

    private void playSound(BlockState blockState, SoundEvent soundEvent) {
        if (level != null) {
            Vec3i facingDir = blockState.getValue(BarrelBlock.FACING).getUnitVec3i();
            double x = this.worldPosition.getX() + 0.5D + facingDir.getX() / 2.0D;
            double y = this.worldPosition.getY() + 0.5D + facingDir.getY() / 2.0D;
            double z = this.worldPosition.getZ() + 0.5D + facingDir.getZ() / 2.0D;
            level.playSound(
                    null, x, y, z,
                    soundEvent, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F
            );
        }
    }
}
