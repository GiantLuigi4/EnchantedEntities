package com.tfc.enchanted_entities.API;

import com.tfc.enchanted_entities.TriFunction;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LootingLevelEvent;

import java.util.ArrayList;
import java.util.function.BiConsumer;

public class EnchantmentData {
	public final int maxLevel;
	public final String registryName;
	public final int xp;
	public final BiConsumer<LivingEntity, EntityEnchantment> onTick;
	public final BiConsumer<CreeperEntity, EntityEnchantment> onCreeperExplode;
	public final TriFunction<LivingEntity, EntityEnchantment, Float, Float> onAttack;
	public final TriFunction<LivingEntity, EntityEnchantment, LivingHurtEvent, Float> onAttack2;
	public final TriFunction<LivingEntity, EntityEnchantment, Float, Float> onHit;
	public final TriFunction<LivingEntity, EntityEnchantment, LivingHurtEvent, Float> onHit2;
	public BiConsumer<LootingLevelEvent, EntityEnchantment> onLoot = null;
	
	public EnchantmentData(int maxLevel, String registryName, int xp, BiConsumer<LivingEntity, EntityEnchantment> onTick, BiConsumer<CreeperEntity, EntityEnchantment> onCreeperExplode, TriFunction<LivingEntity, EntityEnchantment, Float, Float> onAttack, TriFunction<LivingEntity, EntityEnchantment, Float, Float> onHit) {
		this.maxLevel = maxLevel;
		this.registryName = registryName;
		this.xp = xp;
		this.onTick = onTick;
		this.onCreeperExplode = onCreeperExplode;
		this.onAttack = onAttack;
		this.onAttack2 = null;
		this.onHit = onHit;
		this.onHit2 = null;
	}
	
	public EnchantmentData(int maxLevel, String registryName, int xp, BiConsumer<LivingEntity, EntityEnchantment> onTick, BiConsumer<CreeperEntity, EntityEnchantment> onCreeperExplode, TriFunction<LivingEntity, EntityEnchantment, LivingHurtEvent, Float> onAttack2, TriFunction<LivingEntity, EntityEnchantment, LivingHurtEvent, Float> onHit2, int a) {
		this.maxLevel = maxLevel;
		this.registryName = registryName;
		this.xp = xp;
		this.onTick = onTick;
		this.onCreeperExplode = onCreeperExplode;
		this.onAttack = null;
		this.onAttack2 = onAttack2;
		this.onHit = null;
		this.onHit2 = onHit2;
	}
	
	public EnchantmentData onLoot(BiConsumer<LootingLevelEvent, EntityEnchantment> onLoot) {
		this.onLoot = onLoot;
		return this;
	}
	
	public int getXp(LivingEntity entity, ArrayList<EntityEnchantment> enchantments) {
		return xp;
	}
	
	public ITextComponent getDisplayName(LivingEntity entity) {
		return new TranslationTextComponent(registryName.replace(":","."));
	}
	
	public void tickEntity(LivingEntity entity, EntityEnchantment enchantment) {
		onTick.accept(entity,enchantment);
	}
	
	public void onCreeperGoBoom(CreeperEntity entity, EntityEnchantment enchantment) {
		onCreeperExplode.accept(entity,enchantment);
	}
	
	public void onEntityHit(LivingEntity entity, LivingHurtEvent event, EntityEnchantment enchantment) {
		if (onHit != null) event.setAmount(onHit.apply(entity,enchantment,event.getAmount()));
		else event.setAmount(onHit2.apply(entity,enchantment,event));
	}
	
	public void onHitEntity(LivingEntity entity, LivingHurtEvent event, EntityEnchantment enchantment) {
		if (onAttack != null) event.setAmount(onAttack.apply(entity,enchantment,event.getAmount()));
		else event.setAmount(onAttack2.apply(entity,enchantment,event));
	}
	
	public void onLooting(LootingLevelEvent event, EntityEnchantment enchantment) {
		if (onLoot != null) onLoot.accept(event,enchantment);
	}
}
