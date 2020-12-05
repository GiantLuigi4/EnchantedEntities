package com.tfc.enchanted_entities.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.tfc.enchanted_entities.API.EnchantmentData;
import com.tfc.enchanted_entities.API.EntityEnchantment;
import com.tfc.enchanted_entities.EnchantedEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.EnchantmentContainer;
import net.minecraft.util.EnchantmentNameParts;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class ContainerScreen extends net.minecraft.client.gui.screen.inventory.ContainerScreen<Container> {
	public ContainerScreen(Container screenContainer, PlayerInventory inv, ITextComponent titleIn) {
		super(screenContainer, inv, titleIn);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		assert this.minecraft != null;
		
		this.renderBackground();
		
		this.minecraft.getTextureManager().bindTexture(new ResourceLocation("enchanted_entities:textures/gui/gui.png"));
		blit(this.guiLeft,this.guiTop,0,0,176,166);
		this.container.inventorySlots.forEach((slot)->{
			blit(
					(slot.xPos-1)+this.guiLeft,
					(slot.yPos-1)+this.guiTop,
					176,0,
					18,18
			);
		});
		MatrixStack matrixstack = new MatrixStack();
		IRenderTypeBuffer.Impl irendertypebuffer$impl = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
		matrixstack.translate(0.0D, 0.0D, (double)this.itemRenderer.zLevel);
		Matrix4f matrix4f = matrixstack.getLast().getMatrix();
		
		this.getMinecraft().fontRenderer.renderString(
				container.getDisplayName().getFormattedText(),
				this.guiLeft+4,this.guiTop+4,
				4210752,false,
				matrix4f,
				irendertypebuffer$impl,true,0,
				LightTexture.packLight(15,15)
		);
		
		irendertypebuffer$impl.finish();
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
		
		final ResourceLocation ENCHANTMENT_TABLE_GUI_TEXTURE = new ResourceLocation("textures/gui/container/enchanting_table.png");
		
		assert this.minecraft.player != null;
//		int i = this.container.getLapisAmount();
		
		{
			EnchantmentNameParts.getInstance().reseedRandomGenerator(0);
			
			int l = this.container.inventorySlots.get(36).getStack().getCount();
			
//			int i = (this.width - this.xSize) / 2;
//			int j = (this.height - this.ySize) / 2;
			int i=0;
			int j=0;
			
			for(int i1 = 0; i1 < 3; ++i1) {
				int j1 = i + 60;
				int k1 = j1 + 20;
				this.setBlitOffset(0);
				this.minecraft.getTextureManager().bindTexture(ENCHANTMENT_TABLE_GUI_TEXTURE);
				int l1 = (this.container).enchantLevels[i1];
				RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
				if (l1 == 0) {
					this.blit(j1, j + 14 + 19 * i1, 0, 185, 108, 19);
				} else {
					String s = "" + l1;
					int i2 = 86 - this.font.getStringWidth(s);
					String s1 = EnchantmentNameParts.getInstance().generateNewRandomName(this.font, i2);
					FontRenderer fontrenderer = this.minecraft.getFontResourceManager().getFontRenderer(Minecraft.standardGalacticFontRenderer);
					int j2 = 6839882;
					if (((l < i1 + 1 || this.minecraft.player.experienceLevel < l1) && !this.minecraft.player.abilities.isCreativeMode) || this.container.enchantClue[i1] == -1) { // Forge: render buttons as disabled when enchantable but enchantability not met on lower levels
						this.blit(j1, j + 14 + 19 * i1, 0, 185, 108, 19);
						this.blit(j1 + 1, j + 15 + 19 * i1, 16 * i1, 239, 16, 16);
						fontrenderer.drawSplitString(s1, k1, j + 16 + 19 * i1, i2, (j2 & 16711422) >> 1);
						j2 = 4226832;
					} else {
						int k2 = mouseX-this.guiLeft - (i + 60);
						int l2 = mouseY-this.guiTop - (j + 14 + 19 * i1);
						if (k2 >= 0 && l2 >= 0 && k2 < 108 && l2 < 19) {
							this.blit(j1, j + 14 + 19 * i1, 0, 204, 108, 19);
							j2 = 16777088;
						} else {
							this.blit(j1, j + 14 + 19 * i1, 0, 166, 108, 19);
						}
						
						this.blit(j1 + 1, j + 15 + 19 * i1, 16 * i1, 223, 16, 16);
						fontrenderer.drawSplitString(s1, k1, j + 16 + 19 * i1, i2, j2);
						j2 = 8453920;
					}
					
					fontrenderer = this.minecraft.fontRenderer;
					fontrenderer.drawStringWithShadow(s, (float)(k1 + 86 - fontrenderer.getStringWidth(s)), (float)(j + 16 + 19 * i1 + 7), j2);
				}
			}
		}
		
		//Enchantment tooltips
		{
			int i = this.container.inventorySlots.get(36).getStack().getCount();
			boolean flag = this.minecraft.player.abilities.isCreativeMode;
			for (int j = 0; j < 3; ++j) {
				int k = (this.container).enchantLevels[j];
				if (container.enchantClue[j] != -1) {
					EnchantmentData enchantment = EnchantedEntities.dataRegistry.get(container.enchantClue[j]);
					int i1 = j + 1;
					if (this.isPointInRegion(60, 14 + 19 * j, 108, 17, mouseX, mouseY) && k > 0) {
						List<String> list = Lists.newArrayList();
						list.add("" + TextFormatting.WHITE + TextFormatting.ITALIC + I18n.format("container.enchant.clue", enchantment == null ? "" : enchantment.getDisplayName(null).getFormattedText() + " " + container.worldClue[j]));
						if (enchantment == null) {
							java.util.Collections.addAll(list, "", TextFormatting.RED + I18n.format("forge.container.enchant.limitedEnchantability"));
						} else if (!flag) {
							list.add("");
							if (this.minecraft.player.experienceLevel < k) {
								list.add(TextFormatting.RED + I18n.format("container.enchant.level.requirement", (this.container).enchantLevels[j]));
							} else {
								String s;
								if (i1 == 1) {
									s = I18n.format("container.enchant.lapis.one");
								} else {
									s = I18n.format("container.enchant.lapis.many", i1);
								}
								
								TextFormatting textformatting = i >= i1 ? TextFormatting.GRAY : TextFormatting.RED;
								list.add(textformatting + "" + s);
								if (i1 == 1) {
									s = I18n.format("container.enchant.level.one");
								} else {
									s = I18n.format("container.enchant.level.many", i1);
								}
								
								list.add(TextFormatting.GRAY + "" + s);
							}
						}
						
						this.renderTooltip(list, mouseX - this.guiLeft, mouseY - this.guiTop);
						break;
					}
				}
			}
		}
		
		this.renderHoveredToolTip(mouseX-this.guiLeft, mouseY-this.guiTop);
	}
}
