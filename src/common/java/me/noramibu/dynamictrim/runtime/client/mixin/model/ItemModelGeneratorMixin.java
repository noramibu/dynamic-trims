package me.noramibu.dynamictrim.runtime.client.mixin.model;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import java.util.ArrayList;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;

@Mixin(value = ItemModelGenerator.class)
public abstract class ItemModelGeneratorMixin {
    @ModifyExpressionValue(
            method = "<clinit>",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/google/common/collect/Lists;newArrayList([Ljava/lang/Object;)Ljava/util/ArrayList;",
                    remap = false
            )
    )
    private static ArrayList<String> increaseLayerCount(ArrayList<String> original) {
        for (int i = 5; i < 20; i++) { // should cover all possible armour / trim layers
            if (original.contains("layer" + i)) {
                continue;
            }
            original.add("layer" + i);
        }
        return original;
    }
}