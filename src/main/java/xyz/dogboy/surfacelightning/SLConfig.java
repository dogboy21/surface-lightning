package xyz.dogboy.surfacelightning;

import net.minecraftforge.common.config.Config;

@Config(modid = Reference.modid, type = Config.Type.INSTANCE)
public class SLConfig {

    @Config.Name("Tick Interval")
    @Config.Comment("The Interval in Ticks between a lightning wave")
    @Config.RangeInt(min = 1)
    public static int tickInterval = 20;

    @Config.Name("Chunk Lightning Chance")
    @Config.Comment("The chance for a lightning strike to appear in a loaded chunk")
    @Config.RangeDouble(min = 0, max = 1)
    public static double chunkLightningChance = 0.2;

    @Config.Name("Lightning Player Attraction Chance")
    @Config.Comment("The chance for a lightning strike to be attracted by a player. Set to 0 to disable")
    @Config.RangeDouble(min = 0, max = 1)
    public static double playerAttractionChance = 0.3;

    @Config.Name("Lightning Player Attraction Search Radius")
    @Config.Comment("The radius around a lightning strike in which it gets attracted to a player")
    @Config.RangeInt(min = 1)
    public static int playerAttractionSearchRadius = 3;

    @Config.Name("Lightning Bypasses Armor")
    @Config.Comment("Should Lightning Strike Damage bypass armor")
    @Config.RequiresMcRestart
    public static boolean lightningBypassesArmor = false;

    @Config.Name("Lightning Instant Kill")
    @Config.Comment("Should Lightning Strikes instakill the hit target")
    public static boolean lightningInstaKill = false;

}
