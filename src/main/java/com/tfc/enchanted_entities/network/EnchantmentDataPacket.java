package com.tfc.enchanted_entities.network;

import com.tfc.enchanted_entities.API.EnchantmentData;
import com.tfc.enchanted_entities.API.EnchantmentManager;
import com.tfc.enchanted_entities.API.EntityEnchantment;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.INetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.io.IOException;

public class EnchantmentDataPacket implements IPacket {
	public boolean isEnchanted;
	public int id;
	public int target;
	
	public EnchantmentDataPacket(boolean isEnchanted, int id, int target) {
		this.isEnchanted = isEnchanted;
		this.id = id;
		this.target = target;
	}
	
	public EnchantmentDataPacket(PacketBuffer buf) {
		if (FMLEnvironment.dist.isClient()) readPacketData(buf);
	}
	
	private static final EnchantmentData dummyData = new EnchantmentData(0,"null:null",0,(a,e)->{},(a,e)->{},(a,e,i)->i,(a,e,i)->i);
	
	@Override
	public void readPacketData(PacketBuffer buf) {
		LivingEntity entity = (LivingEntity) Minecraft.getInstance().world.getEntityByID(buf.readInt());
		if (buf.readBoolean()) {
			if (entity == null) {
				EnchantmentManager.enchantEntity(Minecraft.getInstance().player, new EntityEnchantment(0, dummyData));
			} else {
				EnchantmentManager.enchantEntity(entity, new EntityEnchantment(0, dummyData));
			}
		} else {
			if (entity == null) {
				EnchantmentManager.unenchantEntity(Minecraft.getInstance().player, dummyData.registryName);
			} else {
				EnchantmentManager.unenchantEntity(entity, dummyData.registryName);
			}
		}
	}
	
	@Override
	public void writePacketData(PacketBuffer buf) {
		buf.writeInt(id);
		buf.writeBoolean(isEnchanted);
	}
	
	@Override
	public void processPacket(INetHandler handler) {
	
	}
}
