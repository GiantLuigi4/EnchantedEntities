package com.tfc.enchanted_entities;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;

import java.util.ArrayList;

public class RedirectingBuffer implements IRenderTypeBuffer {
	private final IVertexBuilder builder;
	private final IRenderTypeBuffer buffer;
	
	protected final ArrayList<String> buffers = new ArrayList<>();
	
	public RedirectingBuffer(IRenderTypeBuffer parent, RenderType type) {
		builder = parent.getBuffer(type);
		buffer=parent;
	}
	
	@Override
	public IVertexBuilder getBuffer(RenderType p_getBuffer_1_) {
		buffers.add(p_getBuffer_1_.toString());
		if (p_getBuffer_1_.equals(RenderType.getGlint()))
			return buffer.getBuffer(p_getBuffer_1_);
		else if (p_getBuffer_1_.equals(RenderType.getEntityGlint()))
			return buffer.getBuffer(p_getBuffer_1_);
		return builder;
	}
}

