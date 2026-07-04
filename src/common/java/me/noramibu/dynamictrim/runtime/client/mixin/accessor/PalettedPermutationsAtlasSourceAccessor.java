package me.noramibu.dynamictrim.runtime.client.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.texture.atlas.sources.PalettedPermutations;
import net.minecraft.resources.ResourceLocation;

@Mixin(PalettedPermutations.class)
public interface PalettedPermutationsAtlasSourceAccessor {
    @Accessor
    List<ResourceLocation> getTextures();

    @Accessor
    ResourceLocation getPaletteKey();

    @Accessor
    Map<String, ResourceLocation> getPermutations();
}
