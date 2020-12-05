package com.tfc.enchanted_entities.mixins;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.tfc.enchanted_entities.events.RenderDragonEvent;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderDragonRenderer.class)
public class DragonRendererMixin {
	@Inject(at = @At("HEAD"), method = "render(Lnet/minecraft/entity/boss/dragon/EnderDragonEntity;FFLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;I)V")
	public void render(EnderDragonEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, CallbackInfo ci) {
		net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new RenderDragonEvent<EnderDragonEntity, EnderDragonRenderer>(entityIn, (EnderDragonRenderer)(Object)this, partialTicks, matrixStackIn, bufferIn, packedLightIn));
	}
}
