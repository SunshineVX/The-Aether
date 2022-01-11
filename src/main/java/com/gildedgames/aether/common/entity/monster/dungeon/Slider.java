package com.gildedgames.aether.common.entity.monster.dungeon;

import com.gildedgames.aether.Aether;
import com.gildedgames.aether.common.entity.ai.navigator.SlideNavigation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;


public class Slider extends Mob implements Enemy {

    protected static final EntityDimensions DIMENSIONS = EntityDimensions.fixed(4F, 4F);

    public Slider(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
        this.setRot(0, 0);
        this.moveControl = new SliderMoveControl(this);
    }

    public static AttributeSupplier.Builder createMobAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.ATTACK_DAMAGE, 0.1D)
                .add(Attributes.FOLLOW_RANGE, 64.0D);
    }

    @Override
    protected void registerGoals() {
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 5, true, false, null));
        this.goalSelector.addGoal(2, new SliderAttackGoal(this));
        this.goalSelector.addGoal(3, new SliderAwakeGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
    }

    public EntityDimensions getDimensions(Pose pPose) {
        return DIMENSIONS;
    }

    /**
     * Freeze Y rotation
     */
    @Override
    public void setYRot(float pYRot) {
        super.setYRot(0);
    }

    @Override
    public boolean shouldDiscardFriction() {
        return true;
    }

    @Override
    protected boolean isAffectedByFluids() {
        return false;
    }

    @Override
    protected int calculateFallDamage(float pDistance, float pDamageMultiplier) {
        return 0;
    }

    // Max amount of time the Slider will rest between slides
    public int getMovementPause() {
        return 30;
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public PathNavigation getNavigation() {
        return super.getNavigation();
    }

    protected PathNavigation createNavigation(Level pLevel) {
        return new SlideNavigation(this, pLevel);
    }

    public boolean canAttack(LivingEntity pTarget) {
        return pTarget.canBeSeenAsEnemy();
    }

    public static class SliderAttackGoal extends Goal {
        private final Slider slime;

        public SliderAttackGoal(Slider p_33648_) {
            this.slime = p_33648_;
            this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        /**
         * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
         * method as well.
         */
        public boolean canUse() {
            LivingEntity livingentity = this.slime.getTarget();
            if (livingentity == null) {
                return false;
            } else {
                return this.slime.canAttack(livingentity) && this.slime.getMoveControl() instanceof SliderMoveControl;
            }
        }

        /**
         * Execute a one shot task or start executing a continuous task
         */
        public void start() {
            super.start();
        }

        /**
         * Returns whether an in-progress EntityAIBase should continue executing
         */
        public boolean canContinueToUse() {
            return true;
        }

        public boolean requiresUpdateEveryTick() {
            return true;
        }

        /**
         * Keep ticking a continuous task that has already been started
         */
        public void tick() {
            LivingEntity livingentity = this.slime.getTarget();

//            Aether.LOGGER.info(this.slime.getNavigation().getPath());

            if (this.slime.getNavigation().isDone() && livingentity != null) {
                boolean pathCreated = this.slime.getNavigation().moveTo(livingentity, 1);
                Path path = this.slime.getNavigation().getPath();

                if (pathCreated && path != null && this.slime.getTarget() instanceof ServerPlayer player) {
                    Node last = null;

                    for (int i = 0; i < path.getNodeCount(); i++) {
                        Node node = path.getNode(i);

                        if (last != null) {

                            if (node.x != last.x) {
                                Node first = last.x < node.x ? last : node;
                                Node second = last.x > node.x ? last : node;

                                for (int x = first.x; x <= second.x; x++) {
                                    this.slime.level.setBlock(new BlockPos(x, first.y, first.z), Blocks.DANDELION.defaultBlockState(), 3);
                                }
                            }

                            if (node.z != last.z) {
                                Node first = last.z < node.z ? last : node;
                                Node second = last.z > node.z ? last : node;

                                for (int z = first.z; z <= second.z; z++) {
                                    this.slime.level.setBlock(new BlockPos(first.x, first.y, z), Blocks.POPPY.defaultBlockState(), 3);
                                }
                            }

                            if (node.y != last.y) {
                                Node first = last.y < node.y ? last : node;
                                Node second = last.y > node.y ? last : node;

                                for (int y = first.y; y < second.y; y++) {
                                    this.slime.level.setBlock(new BlockPos(first.x, y, first.z), Blocks.TRIPWIRE.defaultBlockState(), 3);
                                }
                            }
                        }

                        last = node;
                    }
                }
            }
            //
//            ((SliderEntity.SliderMoveControl) this.slime.getMoveControl()).setDirection(this.slime.getYRot(), true);
        }
    }

    public static class SliderAwakeGoal extends Goal {
        private final Slider slime;

        public SliderAwakeGoal(Slider p_33660_) {
            this.slime = p_33660_;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        /**
         * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
         * method as well.
         */
        public boolean canUse() {
            return true;
        }

        /**
         * Keep ticking a continuous task that has already been started
         */
        public void tick() {
            ((Slider.SliderMoveControl) this.slime.getMoveControl()).setWantedMovement(1.0D);
        }
    }


    public static class SliderMoveControl extends MoveControl {
        private int moveDelay;
        private int energyUsed;
        private final Slider slider;
        private boolean isAggressive;

        public SliderMoveControl(Slider slider) {
            super(slider);
            this.slider = slider;
        }

        public void setWantedMovement(double pSpeed) {
            this.speedModifier = pSpeed;
        }

        public void recharge() {
            this.moveDelay = Math.min(this.energyUsed, this.slider.getMovementPause());
            this.operation = Operation.WAIT;
        }

        @Override
        public void setWantedPosition(double pX, double pY, double pZ, double pSpeed) {
            this.wantedX = pX;
            this.wantedY = pY;
            this.wantedZ = pZ;
            this.speedModifier = pSpeed;
        }

        public float getAxisMovement(double cur, double target) {
            double dist = Math.abs(cur - target);

            if (dist > 0) {
                if (dist < this.mob.getSpeed()) {
                    return (float) (target - cur);
                }

                return cur < target ? this.mob.getSpeed() : -this.mob.getSpeed();
            }

            return 0;
        }

        public void tick() {
            Aether.LOGGER.info(moveDelay + " " + this.operation);

            if (this.operation != MoveControl.Operation.MOVE_TO) {
                this.mob.setSpeed(0.0F);
                this.mob.setDeltaMovement(Vec3.ZERO);

                if (this.moveDelay > 0) {
                    --this.moveDelay;
                } else {
                    this.operation = MoveControl.Operation.MOVE_TO;
                }
            } else {
                this.mob.setSpeed((float) (this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
                this.energyUsed++;

                Vec3 motion = new Vec3(
                        getAxisMovement(this.mob.getX(), this.getWantedX()),
                        getAxisMovement(this.mob.getY(), this.getWantedY()),
                        getAxisMovement(this.mob.getZ(), this.getWantedZ()));

                this.mob.setDeltaMovement(motion);
            }
        }
    }
}
