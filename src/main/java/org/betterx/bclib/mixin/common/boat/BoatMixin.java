package org.betterx.bclib.mixin.common.boat;

import org.betterx.bclib.items.boat.BoatTypeOverride;
import org.betterx.bclib.items.boat.CustomBoatTypeOverride;
import org.betterx.bclib.util.BCLAttachments;

import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.boat.AbstractChestBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;

import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AbstractBoat.class)
public abstract class BoatMixin implements CustomBoatTypeOverride {
    @Unique
    private BoatTypeOverride bcl_type = null;

    @Override
    public void bcl_setCustomType(BoatTypeOverride type) {
        bcl_type = type;
        this.bclib_setTypeNameInAttachment(type == null ? null : type.name());
    }

    @Override
    public BoatTypeOverride bcl_getCustomType() {
        String typeName = this.bclib_getTypeNameFromAttachment();
        if (typeName == null) {
            bcl_type = null;
            return null;
        }

        if (bcl_type != null && typeName.equals(bcl_type.name())) {
            return bcl_type;
        }

        bcl_type = BoatTypeOverride.byName(typeName);
        return bcl_type;
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"), remap = false)
    private void bclib_readAdditionalSaveData(ValueInput valueInput, CallbackInfo ci) {
        if (this.bclib_getTypeNameFromAttachment() != null) {
            return;
        }
        valueInput.getString("cType").ifPresent(typeName -> this.bcl_setCustomType(BoatTypeOverride.byName(typeName)));
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
    private String bclib_getTypeNameFromAttachment() {
        if (BCLAttachments.BOAT_CUSTOM_TYPE == null) {
            return bcl_type == null ? null : bcl_type.name();
        }

        IAttachmentHolder holder = (IAttachmentHolder) (Object) this;
        String typeName = holder.getExistingDataOrNull(BCLAttachments.BOAT_CUSTOM_TYPE);
        return typeName == null || typeName.isEmpty() ? null : typeName;
    }

    @Unique
    private void bclib_setTypeNameInAttachment(String typeName) {
        if (BCLAttachments.BOAT_CUSTOM_TYPE == null) {
            return;
        }

        IAttachmentHolder holder = (IAttachmentHolder) (Object) this;
        if (typeName == null || typeName.isEmpty()) {
            holder.removeData(BCLAttachments.BOAT_CUSTOM_TYPE);
        } else {
            holder.setData(BCLAttachments.BOAT_CUSTOM_TYPE, typeName);
        }
    }
}
