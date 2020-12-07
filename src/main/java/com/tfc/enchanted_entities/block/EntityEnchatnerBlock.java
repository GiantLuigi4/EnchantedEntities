package com.tfc.enchanted_entities.block;

import com.tfc.enchanted_entities.API.EnchantmentManager;
import com.tfc.enchanted_entities.gui.Container;
import com.tfc.enchanted_entities.utils.WorldPosCallable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.Random;
import java.util.function.BiFunction;

public class EntityEnchatnerBlock extends Block {
	public EntityEnchatnerBlock(Properties properties) {
		super(properties);
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		if (!worldIn.isRemote) {
			player.openContainer(new Container(0, player.inventory, new WorldPosCallable(worldIn, pos)));
		}
		return ActionResultType.SUCCESS;
	}
	
	private static final VoxelShape shape = VoxelShapes.or(
			VoxelShapes.create(0, 0, 0, 1, 1f / 16f, 1),
			VoxelShapes.create(2f / 16, 0, 2f / 16, 14f / 16, 4f / 16f, 14f / 16)
	);
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return shape;
	}
	
	@Override
	public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		super.animateTick(stateIn, worldIn, pos, rand);
		
		for (int i = -2; i <= 2; ++i) {
			for (int j = -2; j <= 2; ++j) {
				if (i > -2 && i < 2 && j == -1) {
					j = 2;
				}
				
				if (rand.nextInt(16) == 0) {
					for (int k = 0; k <= 1; ++k) {
						BlockPos blockpos = pos.add(i, k, j);
						if (worldIn.getBlockState(blockpos).getEnchantPowerBonus(worldIn, pos) > 0) {
							if (!worldIn.isAirBlock(pos.add(i / 2, 0, j / 2))) {
								break;
							}
							
							worldIn.addParticle(
									ParticleTypes.ENCHANT,
									(double) pos.getX() + 0.5D,
									(double) pos.getY() + 1.5D,
									(double) pos.getZ() + 0.5D,
									(double) ((float) i + rand.nextFloat()) - 0.5D,
									((float) k - rand.nextFloat() - 0.5F),
									(double) ((float) j + rand.nextFloat()) - 0.5D
							);
						}
					}
				}
			}
		}
	}
	
	@Override
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
		super.onEntityCollision(state, worldIn, pos, entityIn);
		if (entityIn instanceof LivingEntity) {
			if (!EnchantmentManager.isEnchanted((LivingEntity) entityIn)) {
				if (!(entityIn instanceof PlayerEntity)) {
					if (worldIn.getRedstonePowerFromNeighbors(pos) != 0) {
						entityIn.setMotion(0, 0, 0);
						((LivingEntity) entityIn).setJumping(false);
						((LivingEntity) entityIn).onGround = (false);
						entityIn.setPosition(pos.getX() + 0.5f, pos.getY() + (4f / 16f), pos.getZ() + 0.5f);
						entityIn.fallDistance = 0;
					}
				}
			}
		}
	}
}
