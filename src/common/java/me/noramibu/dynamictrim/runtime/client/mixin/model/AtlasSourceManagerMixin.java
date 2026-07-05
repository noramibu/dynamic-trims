package me.noramibu.dynamictrim.runtime.client.mixin.model;

import me.noramibu.dynamictrim.runtime.RuntimeTrims;
import me.noramibu.dynamictrim.runtime.client.model.item.GroupPermutationsAtlasSource;
import com.google.common.collect.BiMap;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SpriteSources.class)
public abstract class AtlasSourceManagerMixin {
    @Shadow @Final
    private static BiMap<ResourceLocation, SpriteSourceType> TYPES;

    static {
        GroupPermutationsAtlasSource.TYPE = new SpriteSourceType(GroupPermutationsAtlasSource.CODEC);
        TYPES.put(RuntimeTrims.id("group_permutations"), GroupPermutationsAtlasSource.TYPE);
    }
}
