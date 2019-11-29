package xyz.dogboy.surfacelightning;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

public class EventHandler {
    private static final Logger logger = LogManager.getLogger(SurfaceLightning.class);

    private final Random random = new Random();
    private Map<Integer, Map<Long, Long>> lastWave = new HashMap<>();

    private Map<Long, Long> getChunkWaveMap(World world) {
        return this.lastWave.computeIfAbsent(world.provider.getDimension(), dimId -> new HashMap<>());
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.side != Side.SERVER || event.phase != TickEvent.Phase.END) {
            return;
        }

        if (!(event.world instanceof WorldServer) || !(event.world.provider instanceof WorldProviderSurface)) {
            return;
        }

        WorldServer world = (WorldServer) event.world;
        Map<Long, Long> chunkWaveMap = this.getChunkWaveMap(world);

        for (Chunk chunk : world.getChunkProvider().getLoadedChunks()) {
            long hash = ChunkPos.asLong(chunk.x, chunk.z);
            long lastWaveInChunk = chunkWaveMap.computeIfAbsent(hash, chunkHash -> world.getTotalWorldTime() - this.random.nextInt(SLConfig.tickInterval));

            if ((world.getTotalWorldTime() - lastWaveInChunk) >= SLConfig.tickInterval) {
                chunkWaveMap.put(hash, world.getTotalWorldTime());

                if (this.withRandomChance(SLConfig.chunkLightningChance)) {
                    EventHandler.logger.debug("Creating lightning at ({} {}, DIM{})", chunk.x, chunk.z, world.provider.getDimension());
                    this.createLightning(chunk);
                }
            }
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        this.lastWave.remove(event.getWorld().provider.getDimension());
    }

    @SubscribeEvent
    public void onChunkUnload(ChunkEvent.Unload event) {
        this.getChunkWaveMap(event.getWorld()).remove(ChunkPos.asLong(event.getChunk().x, event.getChunk().z));
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(Reference.modid)) {
            ConfigManager.sync(Reference.modid, Config.Type.INSTANCE);
        }
    }

    @SubscribeEvent
    public void onLivingDamage(LivingDamageEvent event) {
        if (event.getSource() == DamageSource.LIGHTNING_BOLT && SLConfig.lightningInstaKill) {
            event.setAmount(Float.MAX_VALUE);
        }
    }

    private boolean withRandomChance(double chance) {
        return chance > 0 && this.random.nextDouble() <= chance;
    }

    private void createLightning(Chunk chunk) {
        BlockPos hitPos = new BlockPos(chunk.x * 16 + this.random.nextInt(16), 0, chunk.z * 16 + this.random.nextInt(16));
        hitPos = chunk.getWorld().getTopSolidOrLiquidBlock(hitPos);

        if (this.withRandomChance(SLConfig.playerAttractionChance)) {
            List<EntityPlayer> playersInRange = chunk.getWorld().getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(hitPos).grow(SLConfig.playerAttractionSearchRadius), this::filterPlayer);
            if (!playersInRange.isEmpty()) {
                EntityPlayer attractedTo = playersInRange.get(this.random.nextInt(playersInRange.size()));
                if (chunk.getWorld().canSeeSky(attractedTo.getPosition())) {
                    EventHandler.logger.info("Lightning strike at ({} {} {}, DIM{}) is being attracted to player {}",
                            hitPos.getX(), hitPos.getY(), hitPos.getZ(), chunk.getWorld().provider.getDimension(), attractedTo.getGameProfile().getName());

                    hitPos = attractedTo.getPosition();
                }
            }
        }

        chunk.getWorld().addWeatherEffect(new EntityLightningBolt(chunk.getWorld(), hitPos.getX(), hitPos.getY(), hitPos.getZ(), false));
    }

    private boolean filterPlayer(EntityPlayer player) {
        if (!(player instanceof EntityPlayerMP)) {
            return false;
        }

        EntityPlayerMP playerMP = (EntityPlayerMP) player;
        return playerMP.interactionManager.survivalOrAdventure();
    }

}
