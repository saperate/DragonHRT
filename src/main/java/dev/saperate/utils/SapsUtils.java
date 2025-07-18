package dev.saperate.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.potion.PotionUtil;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;

/**
 * A collection of methods that makes some redundant stuff easier to use
 */
public final class SapsUtils {

    /**
     * This checks if an entity collides with blocks. it uses a float to see how close we need to be for it
     * to count as a collision. Lower is not necessarily better since there is a chance that if you set it
     * too low it will miss the target.
     * <br><br>Recommended sensitivity is 0.1f.
     * <br><b>This includes fluids in the collision check</b>
     * <br><b>This only checks for solid blocks</b>
     * @param entity The entity that we are checking collisions for
     * @param sensitivity How close does it have to be to a block to collide
     * @return The block position of the hit or null if none is found
     * @see Entity
     * @see BlockPos
     * @see World
     */
    public static BlockPos checkBlockCollision(Entity entity, float sensitivity) {
        return checkBlockCollision(entity, sensitivity, true, true, entity.getBoundingBox());
    }

    /**
     * This checks if an entity collides with blocks. it uses a float to see how close we need to be for it
     * to count as a collision. Lower is not necessarily better since there is a chance that if you set it
     * too low it will miss the target.
     * <br><br>Recommended sensitivity is 0.1f.
     * <br><b>This only checks for solid blocks</b>
     * @param entity The entity that we are checking collisions for
     * @param sensitivity How close does it have to be to a block to collide
     * @param includeFluids Whether fluids count in the collision check
     * @return The block position of the hit or null if none is found
     * @see Entity
     * @see BlockPos
     * @see World
     */
    public static BlockPos checkBlockCollision(Entity entity, float sensitivity, boolean includeFluids) {
        return checkBlockCollision(entity, sensitivity, includeFluids, true, entity.getBoundingBox());
    }

    /**
     * This checks if an entity collides with blocks. it uses a float to see how close we need to be for it
     * to count as a collision. Lower is not necessarily better since there is a chance that if you set it
     * too low it will miss the target.
     * <br><br>Recommended sensitivity is 0.1f.
     * @param entity The entity that we are checking collisions for
     * @param sensitivity How close does it have to be to a block to collide
     * @param includeFluids Whether fluids count in the collision check
     * @param requireSolid Whether not solid blocks like grass should be included
     * @return The block position of the hit or null if none is found
     * @see Entity
     * @see BlockPos
     * @see World
     */
    public static BlockPos checkBlockCollision(Entity entity, float sensitivity, boolean includeFluids, boolean requireSolid) {
        return checkBlockCollision(entity, sensitivity, includeFluids, requireSolid, entity.getBoundingBox());
    }

    /**
     * This checks if an entity collides with blocks. it uses a float to see how close we need to be for it
     * to count as a collision. Lower is not necessarily better since there is a chance that if you set it
     * too low it will miss the target.
     * <br><br>Recommended sensitivity is 0.1f.
     * @param entity The entity that we are checking collisions for
     * @param sensitivity How close does it have to be to a block to collide
     * @param includeFluids Whether fluids count in the collision check
     * @param bounds The bounding box that we are checking
     * @return The block position of the hit or null if none is found
     * @see Entity
     * @see BlockPos
     * @see World
     * @see Box
     */
    public static BlockPos checkBlockCollision(Entity entity, float sensitivity, boolean includeFluids, boolean requireSolid, Box bounds) {
        Box box = bounds.expand(sensitivity);
        BlockPos blockPos = BlockPos.ofFloored(box.minX + 1.0E-7, box.minY + 1.0E-7, box.minZ + 1.0E-7);
        BlockPos blockPos2 = BlockPos.ofFloored(box.maxX - 1.0E-7, box.maxY - 1.0E-7, box.maxZ - 1.0E-7);

        //There is a weird bug where if you didn't move the entity yet, the bounding box doesn't add position
        //So this is here to fix that
        if (!isAboutEquals(box.minX, entity.getX(), box.maxX - box.minX)) {
            blockPos = blockPos.add(entity.getBlockPos());
            blockPos2 = blockPos2.add(entity.getBlockPos());
        }

        BlockPos.Mutable mutable = new BlockPos.Mutable();

        List<BlockPos> possibleHits = new ArrayList<>();

        for (int i = blockPos.getX(); i <= blockPos2.getX(); ++i) {
            for (int j = blockPos.getY(); j <= blockPos2.getY(); ++j) {
                for (int k = blockPos.getZ(); k <= blockPos2.getZ(); ++k) {
                    mutable.set(i, j, k);
                    BlockState blockState = entity.getWorld().getBlockState(mutable);
                    if (blockState.isAir()
                            || (!includeFluids && blockState.getBlock() instanceof FluidBlock)
                            || (requireSolid && !blockState.isSolid())
                    ) {
                        continue;
                    }

                    try {
                        blockState.onEntityCollision(entity.getWorld(), mutable, entity);
                        possibleHits.add(new BlockPos(mutable));
                    } catch (Throwable var12) {
                        CrashReport crashReport = CrashReport.create(var12, "Colliding entity with block");
                        CrashReportSection crashReportSection = crashReport.addElement("Block being collided with");
                        CrashReportSection.addBlockInfo(crashReportSection, entity.getWorld(), mutable, blockState);
                        throw new CrashException(crashReport);
                    }
                }
            }
        }


        //Get the closest block from all possible hits
        BlockPos bestHit = possibleHits.isEmpty() ? null : possibleHits.get(0);
        double bestDistance = possibleHits.isEmpty() ? -1 : entity.squaredDistanceTo(bestHit.toCenterPos());
        for (BlockPos hit : possibleHits) {
            double dist = entity.squaredDistanceTo(hit.toCenterPos());
            if (dist < bestDistance) {
                bestHit = hit;
                bestDistance = dist;
            }
        }

        return bestHit;
    }


    /**
     * Hacky way to get which blocks can be affected by an explosion
     */
    public static void getAffectedBlocks(World world, Entity entity, double x, double y, double z, float power) {
        ObjectArrayList<BlockPos> affectedBlocks = new ObjectArrayList<>();
        int l, k;
        world.emitGameEvent(entity, GameEvent.EXPLODE, new Vec3d(x, y, z));
        HashSet<BlockPos> set = Sets.newHashSet();
        for (int j = 0; j < 16; ++j) {
            for (k = 0; k < 16; ++k) {
                block2:
                for (l = 0; l < 16; ++l) {
                    if (j != 0 && j != 15 && k != 0 && k != 15 && l != 0 && l != 15) continue;
                    for (float h = power * (0.7f + world.random.nextFloat() * 0.6f); h > 0.0f; h -= 0.22500001f) {
                        BlockPos blockPos = BlockPos.ofFloored(x, y, z);
                        if (!world.isInBuildLimit(blockPos)) continue block2;
                        set.add(blockPos);
                    }
                }
            }
        }

        affectedBlocks.addAll(set);
    }


    /**
     * searches for an int inside another using some bitwise operation. Allows us to store
     * multiple value inside a single integer with the drawback that they can not be as big as
     * before, and they take a bit longer to interpret.
     * @param num The int where we will extract the bits
     * @param start The index where will start extracting
     * @param length The number of bits we are searching for
     * @return An int containing all the bits we were looking for, placed at the start of the new int
     */
    public static int extractBits(int num, int start, int length) {
        int mask = (1 << length) - 1;
        mask <<= start;
        int result = num & mask;
        result >>= start;
        return result;
    }

    //I don't have any use for this, but I spent time on it, so it is staying
    public void parseUpgradeInt(int obj) {

        int i = 0;
        while (true) {
            int len = extractBits(obj, i, 3);
            if (len == 0) {
                break;
            }
            i += 3;
            byte r = (byte) extractBits(obj, i, len);


            i += len;
        }
    }

    public static float calculatePitch(Vec3d direction) {
        double horizontalDistance = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        double pitch = Math.atan2(direction.y, horizontalDistance);
        return (float) Math.toDegrees(pitch);
    }

    public static float calculateYaw(Vec3d direction) {
        double yaw = Math.atan2(-direction.x, direction.z);
        return (float) Math.toDegrees(yaw);
    }

    public static void summonParticles(Entity entity, Random rnd, ParticleEffect type, float velocity, int density) {
        summonParticles(entity, rnd, type, velocity, density, 1);
    }


    public static void summonParticles(Entity entity, Random rnd, ParticleEffect type, float velocity, int density, float rndYForce) {
        for (int i = 0; i < density; i++) {
            entity.getWorld().addParticle(type,
                    entity.getX() - 0.5f + rnd.nextDouble(),
                    entity.getY() + rnd.nextDouble() * rndYForce,
                    entity.getZ() - 0.5f + rnd.nextDouble(),
                    rnd.nextBetween(-1, 1) * velocity, rnd.nextBetween(-1, 1) * velocity, rnd.nextBetween(-1, 1) * velocity);
        }
    }


    public static void serverSummonParticles(ServerWorld world, ParticleEffect type, Entity entity, Random rnd,
                                             double vX, double vY, double vZ, double speed, int count,
                                             float offsetX, float offsetY, float offsetZ, float vAmplitude) {
        serverSummonParticles(world,type,entity.getPos(),rnd,vX,vY,vZ,speed,count,offsetX,offsetY,offsetZ,vAmplitude);
    }

    public static void serverSummonParticles(ServerWorld world, ParticleEffect type, Vec3d pos, Random rnd,
                                             double vX, double vY, double vZ, double speed, int count,
                                             float offsetX, float offsetY, float offsetZ, float vAmplitude) {
        for (int i = 0; i < count; i++) {
            world.spawnParticles(type,
                    pos.getX() + rnd.nextDouble() - 0.5f + offsetX,
                    pos.getY() + rnd.nextDouble() + 0.5f + offsetY,
                    pos.getZ() + rnd.nextDouble() - 0.5f + offsetZ,
                    0,
                    vX + rnd.nextDouble() * vAmplitude,
                    vY + rnd.nextDouble() * vAmplitude,
                    vZ + rnd.nextDouble() * vAmplitude,
                    speed
            );
        }


    }


    public static BlockPos vec3fToBlockPos(Vector3f vec) {
        return new BlockPos(
                (int) Math.floor(vec.x),
                (int) Math.floor(vec.y),
                (int) Math.floor(vec.z)
        );
    }

    public static Vec3d getEntityLookVector(Entity e, float distance) {
        if(e == null){
            return new Vec3d(0,0,0);
        }
        double rYaw = Math.toRadians(e.getYaw() + 90);
        double rPitch = Math.toRadians(-e.getPitch());

        float x = (float) (Math.cos(rPitch) * Math.cos(rYaw));
        float y = (float) Math.sin(rPitch);
        float z = (float) (Math.cos(rPitch) * Math.sin(rYaw));

        return new Vec3d(x, y, z).multiply(distance).add(e.getEyePos());
    }

    public static HitResult raycastEntity(Entity origin, double maxDistance, Predicate<Entity> predicate) {
        Vec3d cameraPos = origin.getCameraPosVec(1.0f);
        Vec3d rot = origin.getRotationVec(1.0f);
        Vec3d context = cameraPos.add(rot.x * maxDistance, rot.y * maxDistance, rot.z * maxDistance);
        Box box = origin.getBoundingBox().stretch(rot.multiply(maxDistance)).expand(1d, 1d, 1d);
        return ProjectileUtil.raycast(origin, origin.getEyePos(), context, box, predicate.and(entity -> entity instanceof LivingEntity && !entity.isSpectator() && entity.canHit()), maxDistance * maxDistance);
    }

    public static BlockHitResult raycastBlockCustomRotation(Entity origin, float maxDistance, boolean includeFluids, Vec3d rotation) {
        Vec3d cameraPos = origin.getCameraPosVec(1);
        Vec3d context = cameraPos.add(rotation.x * maxDistance, rotation.y * maxDistance, rotation.z * maxDistance);
        return origin.getWorld().raycast(new RaycastContext(cameraPos, context, RaycastContext.ShapeType.OUTLINE, includeFluids ? RaycastContext.FluidHandling.ANY : RaycastContext.FluidHandling.NONE, origin));

    }

    //TODO maybe use a discard block list so that if we hit that and we got both and entity hit and a block hit we only keep the entity hit
    public static HitResult raycastFull(Entity origin, double maxDistance, boolean includeFluids) {
        return raycastFull(origin, maxDistance, includeFluids, Entity::isAlive);
    }

    public static HitResult raycastFull(Entity origin, double maxDistance, boolean includeFluids, Predicate<Entity> entityPredicate) {
        EntityHitResult eHit = (EntityHitResult) raycastEntity(origin, maxDistance, entityPredicate);
        BlockHitResult bHit = (BlockHitResult) origin.raycast(maxDistance, 1.0f, includeFluids);

        if (eHit == null) {
            return bHit;
        } else if (bHit == null) {
            return eHit;
        }

        if (eHit.squaredDistanceTo(origin) < bHit.squaredDistanceTo(origin)) {
            return eHit;
        } else {
            return bHit;
        }
    }

    /**
     * Checks if the hit result contains an entity.
     * If it does contain an entity we return it, if it doesn't we return null.
     * @return An entity or null
     */
    public static Entity entityFromHitResult(HitResult result) {
        if (result.getType().equals(HitResult.Type.ENTITY)) {
            return ((EntityHitResult) result).getEntity();
        } else {
            return null;
        }
    }

    public static Boolean isAboutEquals(double a, double b, double errorMargin) {
        return Math.abs(a - b) <= errorMargin;
    }
    public static Boolean isAboutEquals(Vec3d a, Vec3d b, double errorMargin) {
        return a.distanceTo(b) <= errorMargin;
    }

    /**
     * This method checks safely if an entity has a status effect.
     * It will return true if the entity has the effect <b>AND</b>
     * if the entity is not null
     * @param effect The effect we want to check
     * @param entity The entity that we check
     * @return true if the entity has the status effect
     */
    public static boolean safeHasStatusEffect(StatusEffect effect, LivingEntity entity) {
        boolean hasEffect = false;
        try {
            if (entity != null) {
                hasEffect = entity.hasStatusEffect(effect);
            } else {
                return false;
            }
        } catch (Exception ignored) {
        }
        return hasEffect;
    }


    /**
     * Searches for "<br>" in the translatable to be able to actually do line breaks in tooltips
     * @return the number of args used in the translatable
     */
    public static int addTranslatable(List<Text> tooltip, String key, Object... args) {
        String raw = Text.translatable(key, args).getString();
        if (raw.equals(key)) {
            return 0;
        }
        for (String str : raw.split("<br>")) {
            tooltip.add(Text.of(str));
        }
        return raw.split("%d").length;
    }

    /**
     * Takes in a translatable and makes a new line every N amount of spaces (defined by max)
     *
     * @param tooltip the list where we will add lines
     * @param key Where we will find the translatable
     * @param max The maximum amount of words per line (recommended: 6)
     * @param args The arguments used by the translatable
     * @return the number of args used in the translatable
     */
    public static int addTranslatableAutomaticLineBreaks(List<Text> tooltip, String key, int max, Object... args) {
        String raw = Text.translatable(key, args).getString();
        if (raw.equals(key)) {
            return 0;
        }
        StringBuilder builder = new StringBuilder();
        String[] arr = raw.split(" ");
        for (int i = 0; i < arr.length; i++) {
            builder.append(arr[i]).append(" ");
            if (i % max == 0 && i != arr.length - 1) {
                tooltip.add(Text.of(builder.toString()));
                builder = new StringBuilder();
            }
        }
        tooltip.add(Text.of(builder.toString()));
        return raw.split("%d").length;
    }

    public static void launchEntity(Entity entity, float power) {
        launchEntity(entity,power,true);
    }

    public static void launchEntity(Entity entity, float power, boolean reduceYVelocity) {
        Vector3f velocity = getEntityLookVector(entity, 1)
                .subtract(entity.getEyePos())
                .normalize().multiply(power, reduceYVelocity ? Math.sqrt(power * 0.5) : power * 0.5f, power).toVector3f();
        //returns the root vehicle or itself if there are none
        Entity vehicle = entity.getRootVehicle();

        vehicle.setVelocity(velocity.x,
                velocity.y,
                velocity.z);
        vehicle.velocityModified = true;
        vehicle.move(MovementType.PLAYER, vehicle.getVelocity());
    }

    /**
     * This is used from the server to make a sound. It is designed to be called every tick,
     * and it will play the sound every X ticks.
     * If you just want to play the sound once (or every tick), set the interval to 1.
     * @param entity The entity where we will play the sound
     * @param interval The interval at which we will play the sound
     * @param sound The sound which will be played
     */
    public static void playSoundAtEntity(Entity entity, SoundEvent sound, int interval) {
        if (entity.age % interval == 0) {
            entity.getWorld().playSound(null, entity.getBlockPos(), sound, SoundCategory.NEUTRAL, 1, (1.0f + (entity.getWorld().random.nextFloat() - entity.getWorld().random.nextFloat()) * 0.2f) * 0.7f);
        }
    }

    /**
     * Checks if the looker is looking at the observed entity.
     * Normally used with a player in order to know if the entity is within the screen or not.
     * The way this works is by making a kind of cone that shoots off from where the looker is looking.
     * @param looker The entity from which we are casting the top of the cone
     * @param observed The entity used to check if it is within that cone
     * @param maxDistance The height of the cone (put negative numbers for no limit)
     * @param angle The "radius" of the base of the cone. Usually 0.75 works best for a screen
     * @return True if the observed is within the cone made by the looker
     */
    public static boolean isLookingAt(Entity looker, Entity observed, int maxDistance, float angle){
        Vector3f pos = getEntityLookVector(looker, 3).subtract(looker.getPos()).normalize().multiply(3).toVector3f();
        Vector3f dir = looker.getPos().subtract(observed.getPos()).toVector3f();
        if (dir.length() > maxDistance && maxDistance >= 0) {
            return false;
        }
        dir = dir.normalize();
        float dot = -pos.normalize().dot(dir);
        System.out.println(Math.cos(dot));

        return (Math.cos(dot) <= angle  && dot >= 0);
    }

    /**
     * Checks what status effects we can get from the player's hand. Then removes those from the inventory.
     */
    public static List<StatusEffectInstance> getEffectsFromHands(PlayerEntity player){
        ArrayList<StatusEffectInstance> effects = new ArrayList<>();

        for (ItemStack stack : player.getHandItems()) {
            List<StatusEffectInstance> effectInstances = PotionUtil.getPotionEffects(stack);
            if(!effectInstances.isEmpty()){
                effects.addAll(effectInstances);
                player.getInventory().removeOne(stack);
                player.getInventory().insertStack(Items.GLASS_BOTTLE.getDefaultStack());
            }

        }
        return effects;
    }

    public static boolean isBeingRainedOn(Entity entity) {
        BlockPos blockPos = entity.getBlockPos();
        return entity.getWorld().hasRain(blockPos) || entity.getWorld().hasRain(BlockPos.ofFloored((double) blockPos.getX(), entity.getBoundingBox().maxY, (double) blockPos.getZ()));
    }

    /**
     * Finds entities in a given radius. This method does not take into account walls.
     * The shape of the thing is a square.
     */
    public static List<LivingEntity> getEntitiesInRadius(Vec3d origin, float radius, World world, Entity except){
        return Lists.transform(
                getEntitiesInRadius(origin,radius,world,except, entity -> entity instanceof LivingEntity),
                entity -> (LivingEntity) entity
        );
    }

    /**
     * Finds entities in a given radius. This method does not take into account walls.
     * The shape of the thing is a square.
     */
    public static List<Entity> getEntitiesInRadius(Vec3d origin, float radius, World world, Entity except, Predicate<Entity> pred){
        return world.getOtherEntities(
                except,
                new Box(origin.subtract(radius,radius,radius),origin.add(radius,radius,radius)),
                pred
        );
    }

    public static boolean isLookingForwards(Vector3f direction){
        double dot = Math.acos(new Vector3f(0,0,1).dot(direction)/direction.length());
        return dot >= 1.5;
    }

    /**
     *
     * @return True if the block was broken
     */
    public static boolean mineBlock(BlockPos blockHit, World world, int entityId, int age, int startMiningAge, float miningSpeed) {
        float progress = calcBlockBreakingDelta(world.getBlockState(blockHit), world, blockHit, miningSpeed)
                * (age - startMiningAge + 1);
        world.setBlockBreakingInfo(entityId, blockHit, (int) (progress * 10));

        if (progress >= 1) {
            world.breakBlock(blockHit, true);
            return true;
        }
        return false;
    }

    private static float calcBlockBreakingDelta(BlockState state, BlockView world, BlockPos pos, float miningSpeed) {
        float f = state.getHardness(world, pos);
        if (f == -1.0f) {
            return 0.0f;
        }
        return 1 / f / miningSpeed;
    }

}
