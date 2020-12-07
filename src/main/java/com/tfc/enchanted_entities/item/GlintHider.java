package com.tfc.enchanted_entities.item;

import com.tfc.enchanted_entities.API.EnchantmentManager;
import com.tfc.enchanted_entities.API.EntityEnchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class GlintHider extends Item {
	public GlintHider(Properties properties) {
		super(properties);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		if (
				!EnchantmentManager.isEnchantedWith(
						playerIn,
						EnchantmentManager.getEnchantmentByID("enchanted_entities:hide_glint")
				)
		) {
			if (!worldIn.isRemote) {
				EnchantmentManager.enchantEntity(
						playerIn,
						new EntityEnchantment(1, EnchantmentManager.getEnchantmentByID("enchanted_entities:hide_glint"))
				);
				playerIn.getHeldItem(handIn).setCount(playerIn.getHeldItem(handIn).getCount() - 1);
			}
			return ActionResult.resultSuccess(playerIn.getHeldItem(handIn));
		}
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}
}
