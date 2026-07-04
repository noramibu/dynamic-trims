package me.noramibu.dynamictrim.neoforge;

import me.noramibu.dynamictrim.DynamicTrim;
import me.noramibu.dynamictrim.neoforge.client.DynamicTrimNeoForgeClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(DynamicTrim.MOD_ID)
public final class DynamicTrimNeoForge {
    public DynamicTrimNeoForge() {
        DynamicTrim.init();
        if (FMLEnvironment.dist == Dist.CLIENT) {
            DynamicTrimNeoForgeClient.init();
        }
    }
}
