package com.tfc.enchanted_entities.events;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class RenderDragonEvent<T extends EnderDragonEntity,V extends EnderDragonRenderer> extends Event {
	private final LivingEntity entity;
	private final EnderDragonRenderer renderer;
	private final float partialRenderTick;
	private final MatrixStack matrixStack;
	private final IRenderTypeBuffer buffers;
	private final int light;
	
	public RenderDragonEvent(LivingEntity entity, EnderDragonRenderer renderer, float partialRenderTick, MatrixStack matrixStack,
							 IRenderTypeBuffer buffers, int light)
	{
		this.entity = entity;
		this.renderer = renderer;
		this.partialRenderTick = partialRenderTick;
		this.matrixStack = matrixStack;
		this.buffers = buffers;
		this.light = light;
	}
	
	@Override
	public boolean isCancelable() {
		return true;
	}
	
	public LivingEntity getEntity() {
		return entity;
	}
	
	public EnderDragonRenderer getRenderer() {
		return renderer;
	}
	
	public float getPartialRenderTick() {
		return partialRenderTick;
	}
	
	public MatrixStack getMatrixStack() {
		return matrixStack;
	}
	
	public IRenderTypeBuffer getBuffers() {
		return buffers;
	}
	
	public int getLight() {
		return light;
	}
}
