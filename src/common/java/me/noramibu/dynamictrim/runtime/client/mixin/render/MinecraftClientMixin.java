package me.noramibu.dynamictrim.runtime.client.mixin.render;

import me.noramibu.dynamictrim.runtime.client.RuntimeTrimsClient;
import me.noramibu.dynamictrim.runtime.client.palette.TrimPalette;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {
    @Shadow public ClientLevel level;

    @Inject(
            method = "runTick",
            at = @At("TAIL")
    )
    private void cycleAnimatedTrims(boolean renderLevel, CallbackInfo ci) {
        if(RuntimeTrimsClient.animate && level != null) {
            RuntimeTrimsClient.getTrimPalettes().forEach(TrimPalette::cycleAnimatedColours);
        }
    }
}
