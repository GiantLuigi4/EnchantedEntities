package com.tfc.enchanted_entities.block;

import com.tfc.enchanted_entities.gui.Container;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.function.BiFunction;

public class EntityEnchatnerBlock extends Block {
	public EntityEnchatnerBlock(Properties properties) {
		super(properties);
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		if (!worldIn.isRemote) {
			player.openContainer(new Container(0, player.inventory, new IWorldPosCallable() {
				@Override
				public <T> Optional<T> apply(BiFunction<World, BlockPos, T> p_221484_1_) {
					return Optional.of(p_221484_1_.apply(worldIn,pos));
				}
			}));
		}
		return ActionResultType.SUCCESS;
	}
}
