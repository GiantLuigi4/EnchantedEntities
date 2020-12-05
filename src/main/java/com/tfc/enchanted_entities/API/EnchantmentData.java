package com.tfc.enchanted_entities.API;

import com.tfc.enchanted_entities.TriFunction;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LootingLevelEvent;

import java.util.ArrayList;
import java.util.function.BiConsumer;

public class EnchantmentData {
	public final int maxLevel;
	public final String registryName;
	public final int xp;
	public BiConsumer<LivingEntity, EntityEnchantment> onTick = null;
	public BiConsumer<CreeperEntity, EntityEnchantment> onCreeperExplode = null;
	public TriFunction<LivingEntity, EntityEnchantment, Float, Float> onAttack = null;
	public TriFunction<LivingEntity, EntityEnchantment, LivingHurtEvent, Float> onAttack2 = null;
	public TriFunction<LivingEntity, EntityEnchantment, Float, Float> onHit = null;
	public TriFunction<LivingEntity, EntityEnchantment, LivingHurtEvent, Float> onHit2 = null;
	public BiConsumer<LootingLevelEvent, EntityEnchantment> onLoot = null;
	public BiConsumer<LivingDeathEvent, EntityEnchantment> onDeath = null;
	
	public EnchantmentData(int maxLevel, String registryName, int xp) {
		this.maxLevel = maxLevel;
		this.registryName = registryName;
		this.xp = xp;
	}
	
	@Deprecated
	public EnchantmentData(int maxLevel, String registryName, int xp, BiConsumer<LivingEntity, EntityEnchantment> onTick, BiConsumer<CreeperEntity, EntityEnchantment> onCreeperExplode, TriFunction<LivingEntity, EntityEnchantment, Float, Float> onAttack, TriFunction<LivingEntity, EntityEnchantment, Float, Float> onHit) {
		this.maxLevel = maxLevel;
		this.registryName = registryName;
		this.xp = xp;
		setOnTick(onTick).setOnCreeperExplode(onCreeperExplode).setOnAttack2(onAttack).setOnDamaged2(onHit);
	}
	
	@Deprecated
	public EnchantmentData(int maxLevel, String registryName, int xp, BiConsumer<LivingEntity, EntityEnchantment> onTick, BiConsumer<CreeperEntity, EntityEnchantment> onCreeperExplode, TriFunction<LivingEntity, EntityEnchantment, LivingHurtEvent, Float> onAttack2, TriFunction<LivingEntity, EntityEnchantment, LivingHurtEvent, Float> onHit2, int a) {
		this.maxLevel = maxLevel;
		this.registryName = registryName;
		this.xp = xp;
		setOnTick(onTick).setOnCreeperExplode(onCreeperExplode).setOnAttack(onAttack2).setOnDamaged(onHit2);
	}
	
	public EnchantmentData setOnTick(BiConsumer<LivingEntity, EntityEnchantment> onTick) {
		this.onTick = onTick;
		return this;
	}
	
	public EnchantmentData setOnCreeperExplode(BiConsumer<CreeperEntity, EntityEnchantment> onCreeperExplode) {
		this.onCreeperExplode = onCreeperExplode;
		return this;
	}
	
	@Deprecated
	public EnchantmentData setOnAttack2(TriFunction<LivingEntity, EntityEnchantment, Float, Float> onAttack) {
		this.onAttack = onAttack;
		return this;
	}
	
	public EnchantmentData setOnAttack(TriFunction<LivingEntity, EntityEnchantment, LivingHurtEvent, Float> onAttack2) {
		this.onAttack2 = onAttack2;
		return this;
	}
	
	@Deprecated
	public EnchantmentData setOnDamaged2(TriFunction<LivingEntity, EntityEnchantment, Float, Float> onHit) {
		this.onHit = onHit;
		return this;
	}
	
	public EnchantmentData setOnDamaged(TriFunction<LivingEntity, EntityEnchantment, LivingHurtEvent, Float> onHit2) {
		this.onHit2 = onHit2;
		return this;
	}
	
	public EnchantmentData setOnLoot(BiConsumer<LootingLevelEvent, EntityEnchantment> onLoot) {
		this.onLoot = onLoot;
		return this;
	}
	
	public EnchantmentData setOnDeath(BiConsumer<LivingDeathEvent, EntityEnchantment> onDeath) {
		this.onDeath = onDeath;
		return this;
	}
	
	public int getXp(LivingEntity entity, ArrayList<EntityEnchantment> enchantments) {
		return xp;
	}
	
	public ITextComponent getDisplayName(LivingEntity entity) {
		return new TranslationTextComponent("enchanted_entities.enchant."+registryName.replace(":","."));
	}
	
	public void tickEntity(LivingEntity entity, EntityEnchantment enchantment) {
		onTick.accept(entity,enchantment);
	}
	
	public void onCreeperGoBoom(CreeperEntity entity, EntityEnchantment enchantment) {
		onCreeperExplode.accept(entity,enchantment);
	}
	
	public void onDamaged(LivingEntity entity, LivingHurtEvent event, EntityEnchantment enchantment) {
		if (onHit != null) event.setAmount(onHit.apply(entity,enchantment,event.getAmount()));
		else if (onHit2 != null) event.setAmount(onHit2.apply(entity,enchantment,event));
	}
	
	public void onAttack(LivingEntity entity, LivingHurtEvent event, EntityEnchantment enchantment) {
		if (onAttack != null) event.setAmount(onAttack.apply(entity,enchantment,event.getAmount()));
		else if (onAttack2 != null) event.setAmount(onAttack2.apply(entity,enchantment,event));
	}
	
	public void onLooting(LootingLevelEvent event, EntityEnchantment enchantment) {
		if (onLoot != null) onLoot.accept(event,enchantment);
	}
	
	public void onDeath(LivingDeathEvent event, EntityEnchantment enchantment) {
		if (onDeath != null) onDeath.accept(event,enchantment);
	}
}
