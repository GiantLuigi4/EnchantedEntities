package com.tfc.enchanted_entities;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.tfc.enchanted_entities.API.EnchantmentData;
import com.tfc.enchanted_entities.API.EnchantmentManager;
import com.tfc.enchanted_entities.API.EntityEnchantment;
import com.tfc.enchanted_entities.gui.Container;
import com.tfc.enchanted_entities.utils.WorldPosCallable;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.BiFunction;

//https://cdn.discordapp.com/attachments/197165501741400064/784273590999973908/brigadier_basics_pt2.txt
public class EnchantEntityCommand {
	public static LiteralArgumentBuilder construct() {
		LiteralArgumentBuilder<CommandSource> builder = Commands.literal("enchant_entity").requires(commandSource ->
				{
					System.out.println(commandSource.hasPermissionLevel(2));
					return commandSource.hasPermissionLevel(2);
				}
		)
				.then(Commands.literal("disenchant")
						.then(Commands.argument("entity", EntityArgument.entities())
								.executes(context -> {
									int countEnchanted = 0;
									
									try {
										for (Entity e : EntityArgument.getEntities(context, "entity")) {
											if (e instanceof LivingEntity) {
												EnchantmentManager.unenchantEntity((LivingEntity) e, null);
												countEnchanted++;
											}
										}
									} catch (Throwable err) {
										err.printStackTrace();
									}
									
									context.getSource().sendFeedback(new StringTextComponent("Successfully disenchanted " + countEnchanted + " entities."), false);
									return countEnchanted;
								})));
		
		for (EnchantmentData data : EnchantedEntities.dataRegistry) {
			builder.then(Commands.literal(data.registryName)
					.then(Commands.argument("level", IntegerArgumentType.integer())
							.then(Commands.argument("entity", EntityArgument.entities())
									.executes(context -> {
										int countEnchanted = 0;
										
										try {
											for (Entity e : EntityArgument.getEntities(context, "entity")) {
												if (e instanceof LivingEntity) {
													EnchantmentManager.enchantEntity((LivingEntity) e, new EntityEnchantment(IntegerArgumentType.getInteger(context, "level"), data));
													countEnchanted++;
												}
											}
										} catch (Throwable err) {
											err.printStackTrace();
										}
										
										context.getSource().sendFeedback(new StringTextComponent("Successfully enchanted " + countEnchanted + " entities with " + data.registryName + " " + IntegerArgumentType.getInteger(context, "level")), false);
										return countEnchanted;
									})))
					.then(Commands.literal("enchant")
							.then(Commands.argument("level", IntegerArgumentType.integer())
									.then(Commands.argument("entity", EntityArgument.entities())
											.executes(context -> {
												int countEnchanted = 0;
												
												try {
													for (Entity e : EntityArgument.getEntities(context, "entity")) {
														if (e instanceof LivingEntity) {
															EnchantmentManager.enchantEntity((LivingEntity) e, new EntityEnchantment(IntegerArgumentType.getInteger(context, "level"), data));
															countEnchanted++;
														}
													}
												} catch (Throwable err) {
													err.printStackTrace();
												}
												
												context.getSource().sendFeedback(new StringTextComponent("Successfully enchanted " + countEnchanted + " entities with " + data.registryName + " " + IntegerArgumentType.getInteger(context, "level")), false);
												return countEnchanted;
											}))))
					.then(Commands.literal("disenchant")
							.then(Commands.argument("entity", EntityArgument.entities())
									.executes(context -> {
										int countEnchanted = 0;
										
										try {
											for (Entity e : EntityArgument.getEntities(context, "entity")) {
												if (e instanceof LivingEntity) {
													EnchantmentManager.unenchantEntity((LivingEntity) e, data.registryName);
													countEnchanted++;
												}
											}
										} catch (Throwable err) {
											err.printStackTrace();
										}
										
										context.getSource().sendFeedback(new StringTextComponent("Successfully removed " + data.registryName + " from " + countEnchanted + "entities"), false);
										return countEnchanted;
									}))));
		}
		
		return builder
				.then(Commands.literal("help")
						.executes(context -> {
							context.getSource().sendFeedback(new StringTextComponent("" +
									"Enchants an entity.\n" +
									"Usage:\n" +
									"/enchant_entity enchantment:registry_name level entity_selector\n" +
									"/enchant_entity enchantment:registry_name enchant level entity_selector\n" +
									"/enchant_entity enchantment:registry_name disenchant entity_selector\n" +
									"/enchant_entity disenchant entity_selector" +
									""), false);
							return 0;
						}))
				.then(Commands.literal("open_container")
						.executes(context -> {
							((ServerPlayerEntity) context.getSource().getEntity()).openContainer(new Container(Container.TYPE, 0, ((ServerPlayerEntity) context.getSource().getEntity()).inventory, new WorldPosCallable(context.getSource().getWorld(),context.getSource().getEntity().getPosition())));
							return 0;
						}));
	}
	
	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(construct());
	}
}
