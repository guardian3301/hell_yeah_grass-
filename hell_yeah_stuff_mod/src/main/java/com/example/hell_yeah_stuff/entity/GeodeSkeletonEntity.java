package com.example.hell_yeah_stuff.entity;
import com.example.hell_yeah_stuff.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import javax.annotation.Nullable;
public class GeodeSkeletonEntity extends AbstractSkeleton implements CrossbowAttackMob {
    private static final EntityDataAccessor<Boolean> DATA_CHARGING = SynchedEntityData.defineId(GeodeSkeletonEntity.class, EntityDataSerializers.BOOLEAN);
    private RangedCrossbowAttackGoal<GeodeSkeletonEntity> crossbowGoal;
    public GeodeSkeletonEntity(EntityType<? extends GeodeSkeletonEntity> t, Level l){ super(t,l); this.reassessWeaponGoal(); }
    public static AttributeSupplier.Builder createAttributes(){ return Monster.createMonsterAttributes().add(Attributes.MOVEMENT_SPEED,0.27D).add(Attributes.MAX_HEALTH,16.0D).add(Attributes.FOLLOW_RANGE,32.0D); }
    @Override protected void registerGoals(){
        this.goalSelector.addGoal(1,new FloatGoal(this));
        this.goalSelector.addGoal(3,new AvoidEntityGoal<>(this,Wolf.class,6.0F,1.0D,1.2D));
        this.goalSelector.addGoal(5,new WaterAvoidingRandomStrollGoal(this,1.0D));
        this.goalSelector.addGoal(6,new LookAtPlayerGoal(this,Player.class,8.0F));
        this.goalSelector.addGoal(6,new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1,new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,true));
        this.targetSelector.addGoal(3,new NearestAttackableTargetGoal<>(this,IronGolem.class,true));
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){ super.defineSynchedData(b); b.define(DATA_CHARGING,false); }
    @Override protected void populateDefaultEquipmentSlots(RandomSource r,DifficultyInstance d){
        this.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(ModItems.MULTI_CROSSBOW.get()));
        this.setDropChance(EquipmentSlot.MAINHAND,0.07F);
        if(r.nextFloat()<0.22F){ this.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Blocks.AMETHYST_BLOCK)); this.setDropChance(EquipmentSlot.HEAD,0.0F); }
    }
    @Override public ItemStack getProjectile(ItemStack w){
        if(w.is(ModItems.MULTI_CROSSBOW.get())||w.getItem() instanceof ProjectileWeaponItem) return new ItemStack(Items.AMETHYST_SHARD);
        return super.getProjectile(w);
    }
    @Override public void reassessWeaponGoal(){
        if(this.level()==null) return;
        if(this.level().isClientSide) return;
        if(this.crossbowGoal==null) this.crossbowGoal=new RangedCrossbowAttackGoal<>(this, 1.0D, 8.0F);
        this.goalSelector.removeGoal(this.crossbowGoal);
        ItemStack h=this.getMainHandItem();
        boolean has=h.getItem() instanceof net.minecraft.world.item.CrossbowItem;
        if(has||this.getType()!=null) this.goalSelector.addGoal(4,this.crossbowGoal);
    }
    @Override public void setItemSlot(EquipmentSlot s,ItemStack st){ super.setItemSlot(s,st); if(this.level()!=null && !this.level().isClientSide && s==EquipmentSlot.MAINHAND) this.reassessWeaponGoal(); }
    @Override public void setChargingCrossbow(boolean c){ this.entityData.set(DATA_CHARGING,c); }
    public boolean isChargingCrossbow(){ return this.entityData.get(DATA_CHARGING); }
    @Override public void onCrossbowAttackPerformed(){ this.noActionTime=0; }
    @Override public void performRangedAttack(LivingEntity t,float v){ this.performCrossbowAttack(this,1.6F); }
    @Override public boolean canFireProjectileWeapon(ProjectileWeaponItem w){ return w instanceof net.minecraft.world.item.CrossbowItem; }
    @Override protected SoundEvent getAmbientSound(){ return SoundEvents.SKELETON_AMBIENT; }
    @Override protected SoundEvent getHurtSound(DamageSource s){ return SoundEvents.SKELETON_HURT; }
    @Override protected SoundEvent getDeathSound(){ return SoundEvents.SKELETON_DEATH; }
    @Override protected SoundEvent getStepSound(){ return SoundEvents.SKELETON_STEP; }
    @Override protected void playStepSound(BlockPos p,net.minecraft.world.level.block.state.BlockState s){ this.playSound(this.getStepSound(),0.15F,0.9F); }
    @Override public boolean isSunBurnTick(){ return false; }
    @Override public boolean isShaking(){ return false; }
    @Nullable @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor l,DifficultyInstance d,MobSpawnType r,@Nullable SpawnGroupData g){ g=super.finalizeSpawn(l,d,r,g); this.reassessWeaponGoal(); this.enchantSpawnedWeapon(l,l.getRandom(),d); return g; }
    @Override public void readAdditionalSaveData(CompoundTag t){ super.readAdditionalSaveData(t); this.reassessWeaponGoal(); }
}
