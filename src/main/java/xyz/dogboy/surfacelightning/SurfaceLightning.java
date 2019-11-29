package xyz.dogboy.surfacelightning;

import net.minecraft.util.DamageSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

@Mod(modid = Reference.modid, name = Reference.name, version = Reference.version, certificateFingerprint = Reference.fingerprint)
public class SurfaceLightning {

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new EventHandler());

        if (SLConfig.lightningBypassesArmor) {
            DamageSource.LIGHTNING_BOLT.setDamageBypassesArmor();
        }
    }

}
