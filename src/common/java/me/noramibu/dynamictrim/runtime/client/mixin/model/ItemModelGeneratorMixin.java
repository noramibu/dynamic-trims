package me.noramibu.dynamictrim.runtime.client.mixin.model;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import java.util.ArrayList;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;

@Mixin(value = ItemModelGenerator.class)
public abstract class ItemModelGeneratorMixin {
    @ModifyExpressionValue(
            method = "<clinit>",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;of(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/util/List;",
                    remap = false
            )
    )
    private static List<String> increaseLayerCount(List<String> original) {
        ArrayList<String> layers = new ArrayList<>(original);
        for (int i = 5; i < 20; i++) { // should cover all possible armour / trim layers
            if (layers.contains("layer" + i)) {
                continue;
            }
            layers.add("layer" + i);
        }
        return layers;
    }
}
