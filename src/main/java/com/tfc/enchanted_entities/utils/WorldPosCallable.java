package com.tfc.enchanted_entities.utils;

import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.function.BiFunction;

public class WorldPosCallable implements IWorldPosCallable {
	public final World world;
	public final BlockPos pos;
	
	public WorldPosCallable(World world, BlockPos pos) {
		this.world = world;
		this.pos = pos;
	}
	
	@Override
	public <T> Optional<T> apply(BiFunction<World, BlockPos, T> p_221484_1_) {
		return Optional.of(p_221484_1_.apply(world, pos));
	}
	
	@Override
	public String toString() {
		return "WorldPosCallable{" +
				"world=" + world +
				", pos=" + pos +
				'}';
	}
}
