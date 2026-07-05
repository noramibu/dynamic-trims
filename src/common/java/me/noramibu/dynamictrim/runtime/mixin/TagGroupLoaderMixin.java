package me.noramibu.dynamictrim.runtime.mixin;

import me.noramibu.dynamictrim.runtime.RuntimeTrims;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagLoader;
import net.minecraft.world.item.Item;

@Mixin(TagLoader.class)
public abstract class TagGroupLoaderMixin {
    @Shadow @Final private String directory;

    @Inject(method = "build(Ljava/util/Map;)Ljava/util/Map;", at = @At("HEAD"))
    private void addToTagEntries(Map<Identifier, List<TagLoader.EntryWithSource>> tagEntries, CallbackInfoReturnable<Map<Identifier, Collection<?>>> cir) {
        if (!"tags/item".equals(directory)) return;

        addToTag(tagEntries, ItemTags.TRIM_MATERIALS.location(), RuntimeTrims.getTrimTagInjector().getTrimMaterials());
        addToTag(tagEntries, ItemTags.TRIMMABLE_ARMOR.location(), RuntimeTrims.getTrimTagInjector().getTrimmableArmour());
    }

    private void addToTag(Map<Identifier, List<TagLoader.EntryWithSource>> tagEntries, Identifier id, Collection<Item> items) {
        List<TagLoader.EntryWithSource> entries = tagEntries.computeIfAbsent(id, key -> new ArrayList<>());
        entries.addAll(items.stream()
                .map(BuiltInRegistries.ITEM::getKey)
                .map(itemId -> new TagLoader.EntryWithSource(TagEntry.element(itemId), RuntimeTrims.MOD_ID))
                .collect(Collectors.toSet()));
    }
}
