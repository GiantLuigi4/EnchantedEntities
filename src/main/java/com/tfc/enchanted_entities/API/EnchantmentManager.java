package com.tfc.enchanted_entities.API;

import com.tfc.enchanted_entities.EnchantedEntities;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;

import java.util.ArrayList;

public class EnchantmentManager {
	public static ArrayList<EntityEnchantment> getEnchantmentsForEntity(LivingEntity entity) {
		ArrayList<EntityEnchantment> entityEnchantments = new ArrayList<>();
		if (!entity.getPersistentData().contains("EnchantedEntities"))
			return entityEnchantments;
		for (EnchantmentData data : EnchantedEntities.dataRegistry) {
			if (entity.getPersistentData().getCompound("EnchantedEntities").contains(data.registryName)) {
				EntityEnchantment entityEnchantment =
						new EntityEnchantment(
								entity.getPersistentData().getCompound("EnchantedEntities")
										.getInt(data.registryName), data
						);
				entityEnchantments.add(entityEnchantment);
			}
		}
		return entityEnchantments;
	}
	
	public static void enchantEntity(LivingEntity entity, EntityEnchantment enchantment) {
		if (!entity.getPersistentData().contains("EnchantedEntities"))
			entity.getPersistentData().put("EnchantedEntities", new CompoundNBT());
		if (enchantment != null)
			entity.getPersistentData().getCompound("EnchantedEntities")
					.putInt(enchantment.data.registryName, enchantment.level);
	}
	
	public static boolean isEnchanted(LivingEntity entity) {
		if (!entity.getPersistentData().contains("EnchantedEntities"))
			entity.getPersistentData().put("EnchantedEntities", new CompoundNBT());
		return !(entity.getPersistentData().getCompound("EnchantedEntities").isEmpty());
	}
	
	public static void unenchantEntity(LivingEntity entity, String registryName) {
		if (!entity.getPersistentData().contains("EnchantedEntities") || registryName == null)
			entity.getPersistentData().put("EnchantedEntities", new CompoundNBT());
		if (registryName != null && entity.getPersistentData().getCompound("EnchantedEntities").contains(registryName))
			entity.getPersistentData().getCompound("EnchantedEntities")
					.remove(registryName);
	}
}
