package org.betterx.bclib.mixin.common.boat;

import org.betterx.bclib.items.boat.BoatTypeOverride;
import org.betterx.bclib.items.boat.CustomBoatTypeOverride;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.boat.AbstractChestBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AbstractBoat.class)
public abstract class BoatMixin implements CustomBoatTypeOverride {
    @Unique
    private static final EntityDataAccessor<Integer> BCL_CUSTOM_TYPE = SynchedEntityData.defineId(
            AbstractBoat.class,
            EntityDataSerializers.INT
    );
    @Unique
    private static final int BCL_NO_CUSTOM_TYPE = -1;

    @Unique
    private BoatTypeOverride bcl_type = null;

    @Inject(method = "defineSynchedData", at = @At("TAIL"), remap = false)
    private void bclib_defineSynchedData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(BCL_CUSTOM_TYPE, BCL_NO_CUSTOM_TYPE);
    }

    @Override
    public void bcl_setCustomType(BoatTypeOverride type) {
        bcl_type = type;
        this.bclib_entityData().set(BCL_CUSTOM_TYPE, type == null ? BCL_NO_CUSTOM_TYPE : type.ordinal());
    }

    @Override
    public BoatTypeOverride bcl_getCustomType() {
        int id = this.bclib_entityData().get(BCL_CUSTOM_TYPE);
        if (id == BCL_NO_CUSTOM_TYPE) {
            bcl_type = null;
            return null;
        }

        bcl_type = BoatTypeOverride.byId(id);
        return bcl_type;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"), remap = false)
    private void bclib_addAdditionalSaveData(ValueOutput valueOutput, CallbackInfo ci) {
        BoatTypeOverride type = this.bcl_getCustomType();
        if (type != null) {
            valueOutput.putString("cType", type.name());
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"), remap = false)
    private void bclib_readAdditionalSaveData(ValueInput valueInput, CallbackInfo ci) {
        this.bcl_setCustomType(valueInput.getString("cType").map(BoatTypeOverride::byName).orElse(null));
    }

    @Inject(method = "getDropItem", at = @At("HEAD"), cancellable = true, remap = false)
    private void bclib_getDropItem(CallbackInfoReturnable<Item> cir) {
        BoatTypeOverride type = this.bcl_getCustomType();
        if (type == null) {
            return;
        }

        Item customItem = (Object) this instanceof AbstractChestBoat ? type.getChestBoatItem() : type.getBoatItem();
        if (customItem != null) {
            cir.setReturnValue(customItem);
        }
    }

    @Inject(method = "getPickResult", at = @At("HEAD"), cancellable = true, remap = false)
    private void bclib_getPickResult(CallbackInfoReturnable<ItemStack> cir) {
        BoatTypeOverride type = this.bcl_getCustomType();
        if (type == null) {
            return;
        }

        Item customItem = (Object) this instanceof AbstractChestBoat ? type.getChestBoatItem() : type.getBoatItem();
        if (customItem != null) {
            cir.setReturnValue(new ItemStack(customItem));
        }
    }

    @Unique
    private SynchedEntityData bclib_entityData() {
        return ((AbstractBoat) (Object) this).getEntityData();
    }
}
