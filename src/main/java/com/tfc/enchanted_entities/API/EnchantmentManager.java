package com.tfc.enchanted_entities.API;

import com.tfc.enchanted_entities.EnchantedEntities;
import com.tfc.enchanted_entities.network.EnchantmentDataPacket;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fml.network.PacketDistributor;

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
//		EnchantedEntities.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(()->entity), new EnchantmentDataPacket(EnchantmentManager.isEnchanted(entity), entity.getEntityId(), 0));
	}
	
	public static boolean isEnchanted(LivingEntity entity) {
		if (!entity.getPersistentData().contains("EnchantedEntities"))
			return false;
		return !(entity.getPersistentData().getCompound("EnchantedEntities").isEmpty());
	}
	
	public static void unenchantEntity(LivingEntity entity, String registryName) {
		if (!entity.getPersistentData().contains("EnchantedEntities") || registryName == null)
			entity.getPersistentData().put("EnchantedEntities", new CompoundNBT());
		if (registryName != null && entity.getPersistentData().getCompound("EnchantedEntities").contains(registryName))
			entity.getPersistentData().getCompound("EnchantedEntities")
					.remove(registryName);
		
		EnchantedEntities.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(()->entity), new EnchantmentDataPacket(EnchantmentManager.isVisiblyEnchanted(entity), entity.getEntityId(), 0));
	}
	
	public static boolean isVisiblyEnchanted(LivingEntity entity) {
		return
				EnchantmentManager.isEnchanted(entity) &&
						!EnchantmentManager.isEnchantedWith(
								entity,
								EnchantmentManager.getEnchantmentByID(
										"enchanted_entities:hide_glint"
								)
						);
	}
	
	public static boolean isEnchantedWith(LivingEntity entity, EnchantmentData data) {
		if (!entity.getPersistentData().contains("EnchantedEntities"))
			return false;
		for (EntityEnchantment entityEnchantment : getEnchantmentsForEntity(entity))
			if (entityEnchantment.data.equals(data)) return true;
		return false;
	}
	
	public static EnchantmentData getEnchantmentByID(String registryName) {
		for (EnchantmentData data : EnchantedEntities.dataRegistry) {
			if (data.registryName.equals(registryName)) {
				return data;
			}
		}
		return new EnchantmentData(0,registryName,0);
	}
}
