package com.tfc.enchanted_entities.network;

import net.minecraft.network.INetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;

public class EnchantmentRequestPacket implements IPacket {
	public int id;
	
	public EnchantmentRequestPacket(PacketBuffer buf) {
		readPacketData(buf);
	}
	
	public EnchantmentRequestPacket(int id) {
		this.id = id;
	}
	
	@Override
	public void readPacketData(PacketBuffer buf) {
		id = buf.readInt();
	}
	
	@Override
	public void writePacketData(PacketBuffer buf) {
		buf.writeInt(id);
	}
	
	@Override
	public void processPacket(INetHandler handler) {
	
	}
}
