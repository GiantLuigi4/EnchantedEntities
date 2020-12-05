//package com.tfc.enchanted_entities;
//
//import net.minecraft.enchantment.Enchantment;
//import net.minecraft.entity.LivingEntity;
//import net.minecraft.nbt.CompoundNBT;
//import net.minecraft.nbt.INBT;
//import net.minecraft.network.datasync.EntityDataManager;
//import net.minecraft.util.Direction;
//import net.minecraft.util.ResourceLocation;
//import net.minecraftforge.common.capabilities.Capability;
//import net.minecraftforge.common.capabilities.CapabilityInject;
//import net.minecraftforge.common.util.INBTSerializable;
//import net.minecraftforge.common.util.Lazy;
//
//import javax.annotation.Nullable;
//import java.util.HashMap;
//
//public class EnchantmentCapability implements INBTSerializable<CompoundNBT>, Capability.IStorage<EnchantmentCapability> {
//	private static final ResourceLocation ID = new ResourceLocation("enchanted_entities:capability");
//	@CapabilityInject(EnchantmentCapability.class)
//	public static final EnchantmentCapability INSTANCE = null;
//
//	private final HashMap<String, Enchantment> enchantmentHashMap = new HashMap<>();
//
//	EntityDataManager manager;
//
//	public EnchantmentCapability() {
//	}
//
//	private final Capability<EnchantmentCapability> capability;
//
//	public EnchantmentCapability(LivingEntity entity) {
//		this.manager = new EntityDataManager(entity);
//		this.capability = this;
//	}
//
//	@Override
//	public CompoundNBT serializeNBT() {
//		INSTANCE.writeNBT(INSTANCE)
//	}
//
//	@Override
//	public void deserializeNBT(CompoundNBT nbt) {
//
//	}
//
//	@Nullable
//	@Override
//	public INBT writeNBT(Capability<EnchantmentCapability> capability, EnchantmentCapability instance, Direction side) {
////		return capability.;
//		CompoundNBT capNBT = new CompoundNBT();
//		String lastKey = "";
//		int id = 0;
//		for (EntityDataManager.DataEntry entry : manager.getAll()) {
//			if (lastKey.equals("")) {
//				capNBT.putString("enchantment"+id,entry.getValue().toString());
//				lastKey = entry.getValue().toString();
//				id++;
//			} else {
//				capNBT.putString("enchantment"+id+"_level",entry.getValue().toString());
//				lastKey = "";
//			}
//		}
//		return capNBT;
//	}
//
//	@Override
//	public void readNBT(Capability<EnchantmentCapability> capability, EnchantmentCapability instance, Direction side, INBT nbt) {
//		System.out.println(nbt);
//	}
//}
