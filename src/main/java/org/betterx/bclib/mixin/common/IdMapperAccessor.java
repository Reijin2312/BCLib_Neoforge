package org.betterx.bclib.mixin.common;

import net.minecraft.core.IdMapper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(value = IdMapper.class)
public interface IdMapperAccessor {
    @Accessor(value = "idToT")
    List<Object> bclib_getIdToT();
}
