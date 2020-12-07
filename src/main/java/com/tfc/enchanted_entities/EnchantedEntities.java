package com.tfc.enchanted_entities;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.tfc.enchanted_entities.API.EnchantmentData;
import com.tfc.enchanted_entities.API.EnchantmentManager;
import com.tfc.enchanted_entities.API.EntityEnchantment;
import com.tfc.enchanted_entities.block.EntityEnchatnerBlock;
import com.tfc.enchanted_entities.data.EntityStats;
import com.tfc.enchanted_entities.data.Loader;
import com.tfc.enchanted_entities.events.RenderDragonEvent;
import com.tfc.enchanted_entities.gui.Container;
import com.tfc.enchanted_entities.gui.ContainerScreen;
import com.tfc.enchanted_entities.item.GlintHider;
import com.tfc.enchanted_entities.network.EnchantmentDataPacket;
import com.tfc.enchanted_entities.network.EnchantmentRequestPacket;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.BiConsumer;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("enchanted_entities")
public class EnchantedEntities {
	public static final ArrayList<EnchantmentData> dataRegistry = new ArrayList<>();
	
	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
			new ResourceLocation("enchanted_entities", "main"),
			() -> "1",
			"1"::equals,
			"1"::equals
	);
	
	// Directly reference a log4j logger.
	private static final Logger LOGGER = LogManager.getLogger();
	
	private static final ArrayList<EnchantmentDataPacket> dataPackets = new ArrayList<>();
	
	public EnchantedEntities() {
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.addListener(EnchantedEntities::livingTickEvent);
		MinecraftForge.EVENT_BUS.addListener(EnchantedEntities::onEntityHurt);
		MinecraftForge.EVENT_BUS.addListener(EnchantedEntities::onServerAboutToStart);
		MinecraftForge.EVENT_BUS.addListener(EnchantedEntities::onServerStarting);
		MinecraftForge.EVENT_BUS.addListener(EnchantedEntities::poolLoot);
		MinecraftForge.EVENT_BUS.addListener(EnchantedEntities::onDeath);
		MinecraftForge.EVENT_BUS.addListener(EnchantedEntities::worldTick);
		
		if (FMLEnvironment.dist.isClient()) {
			MinecraftForge.EVENT_BUS.addListener(EnchantedEntities::renderLiving);
			MinecraftForge.EVENT_BUS.addListener(EnchantedEntities::renderDragon);
		}
		
		INSTANCE.registerMessage(0, EnchantmentRequestPacket.class, EnchantmentRequestPacket::writePacketData, EnchantmentRequestPacket::new, (packet, contex) -> {
			dataPackets.add(new EnchantmentDataPacket(
					EnchantmentManager.isVisiblyEnchanted((LivingEntity) contex.get().getSender().world.getEntityByID(packet.id)),
					packet.id, contex.get().getSender().getEntityId()
			));
			contex.get().setPacketHandled(true);
		});
		
		INSTANCE.registerMessage(1, EnchantmentDataPacket.class, EnchantmentDataPacket::writePacketData, EnchantmentDataPacket::new, (packet, contex) -> {
			contex.get().setPacketHandled(true);
		});
		
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public static void onServerStarting(FMLServerStartingEvent event1) {
		EnchantEntityCommand.register(event1.getCommandDispatcher());
	}
	
	public static void onServerAboutToStart(FMLServerAboutToStartEvent event1) {
		dataRegistry.clear();
		
		event1.getServer().getResourceManager().addReloadListener(Loader.dataLoader);
		
		final BiConsumer<?, EntityEnchantment> emptyConsumer = (o, e) -> {
		};
		
		dataRegistry.addAll(
				Arrays.asList(
						new EnchantmentData(
								4, "minecraft:protection", 1,
								(BiConsumer<LivingEntity, EntityEnchantment>) emptyConsumer,
								(BiConsumer<CreeperEntity, EntityEnchantment>) emptyConsumer,
								(entity, enchantment, amount) -> amount,
								(entity, enchantment, amount) -> amount - (enchantment.level / 2f)
						),
						new EnchantmentData(
								5, "minecraft:sharpness", 1,
								(BiConsumer<LivingEntity, EntityEnchantment>) emptyConsumer,
								(BiConsumer<CreeperEntity, EntityEnchantment>) emptyConsumer,
								(entity, enchantment, event) -> event.getAmount() + ((event.getSource().getImmediateSource() == null || (event.getSource().getTrueSource() != null && event.getSource().getImmediateSource().equals(event.getSource().getTrueSource()))) ? (enchantment.level / 2f) : 0),
								(entity, enchantment, event) -> event.getAmount(),
								0
						),
						new EnchantmentData(
								5, "minecraft:power", 1,
								(BiConsumer<LivingEntity, EntityEnchantment>) emptyConsumer,
								(BiConsumer<CreeperEntity, EntityEnchantment>) emptyConsumer,
								(entity, enchantment, event) -> event.getAmount() + (!(event.getSource().getImmediateSource() == null || (event.getSource().getTrueSource() != null && event.getSource().getImmediateSource().equals(event.getSource().getTrueSource()))) ? (enchantment.level / 2f) : 0),
								(entity, enchantment, event) -> event.getAmount(),
								0
						),
						new EnchantmentData(
								1, "minecraft:flame", 1,
								(BiConsumer<LivingEntity, EntityEnchantment>) emptyConsumer,
								(BiConsumer<CreeperEntity, EntityEnchantment>) emptyConsumer,
								(entity, enchantment, event) -> {
									if (!((event.getSource().getImmediateSource() == null || (event.getSource().getTrueSource() != null && event.getSource().getImmediateSource().equals(event.getSource().getTrueSource()))))) {
										event.getEntityLiving().setFireTimer((enchantment.level * 50));
									}
									return event.getAmount();
								},
								(entity, enchantment, event) -> event.getAmount(),
								0
						),
						new EnchantmentData(
								2, "minecraft:fire_aspect", 1,
								(BiConsumer<LivingEntity, EntityEnchantment>) emptyConsumer,
								(BiConsumer<CreeperEntity, EntityEnchantment>) emptyConsumer,
								(entity, enchantment, event) -> {
									if (((event.getSource().getImmediateSource() == null || (event.getSource().getTrueSource() != null && event.getSource().getImmediateSource().equals(event.getSource().getTrueSource()))))) {
										event.getEntityLiving().setFireTimer((enchantment.level * 80));
									}
									return event.getAmount();
								},
								(entity, enchantment, event) -> event.getAmount(),
								0
						),
						new EnchantmentData(
								5, "minecraft:knockback", 1,
								(BiConsumer<LivingEntity, EntityEnchantment>) emptyConsumer,
								(BiConsumer<CreeperEntity, EntityEnchantment>) emptyConsumer,
								(entity, enchantment, event) -> {
									if (!event.getSource().damageType.equals("thorns")) {
										if (event.getSource().getTrueSource() != null) {
											if ((event.getSource().getImmediateSource() == null || (event.getSource().getTrueSource() != null && event.getSource().getImmediateSource().equals(event.getSource().getTrueSource())))) {
												Vec3d vec3d =
														event.getSource().getTrueSource().getPositionVec()
																.subtract(event.getEntityLiving().getPositionVec())
																.mul(1, 0, 1)
																.normalize().scale(-enchantment.level);
												event.getEntityLiving().addVelocity(vec3d.x, vec3d.y, vec3d.z);
											}
										} else if (event.getSource().getImmediateSource() != null) {
//											event.getEntityLiving().addVelocity(
//													event.getSource().getImmediateSource().getPositionVec()
//															.subtract(event.getEntityLiving().getPositionVec())
//															.mul(1,0,1)
//															.normalize().scale(-enchantment.level)
//											);
										}
									}
									return event.getAmount();
								},
								(entity, enchantment, event) -> event.getAmount(),
								0
						),
						new EnchantmentData(
								2, "minecraft:punch", 1,
								(BiConsumer<LivingEntity, EntityEnchantment>) emptyConsumer,
								(BiConsumer<CreeperEntity, EntityEnchantment>) emptyConsumer,
								(entity, enchantment, event) -> {
									if (!event.getSource().damageType.equals("thorns")) {
										if (event.getSource().getTrueSource() != null) {
											if (!(event.getSource().getImmediateSource() == null || (event.getSource().getTrueSource() != null && event.getSource().getImmediateSource().equals(event.getSource().getTrueSource())))) {
												Vec3d vec3d =
														event.getSource().getTrueSource().getPositionVec()
																.subtract(event.getEntityLiving().getPositionVec())
																.mul(1, 0, 1)
																.normalize().scale(-enchantment.level);
												event.getEntityLiving().addVelocity(vec3d.x, vec3d.y, vec3d.z);
											}
										}
									}
									return event.getAmount();
								},
								(entity, enchantment, event) -> event.getAmount(),
								0
						),
						new EnchantmentData(3, "minecraft:thorns", 1)
								.setOnDamaged(
										(entity, enchantment, event) -> {
											int level = enchantment.level;
											Random rnd = entity.getRNG();
											if (!event.getSource().damageType.equals("thorns"))
												if (event.getSource().getTrueSource() != null) {
													event.getSource().getTrueSource().attackEntityFrom(
															DamageSource.causeThornsDamage(event.getEntityLiving()),
															level > 10 ? level - 10 : 1 + rnd.nextInt(4)
													);
												} else if (event.getSource().getImmediateSource() != null) {
													event.getSource().getTrueSource().attackEntityFrom(
															DamageSource.causeThornsDamage(event.getEntityLiving()),
															level > 10 ? level - 10 : 1 + rnd.nextInt(4)
													);
												}
											return event.getAmount();
										}),
						new EnchantmentData(3, "minecraft:looting", 1)
								.setOnLoot((lootingEvent, enchantment) -> lootingEvent.setLootingLevel(lootingEvent.getLootingLevel() + enchantment.level)),
						new EnchantmentData(10, "minecraft:vanishing_curse", 1)
								.setOnDeath((deathEvent, enchantment) -> {
									if (deathEvent.getEntityLiving() instanceof PlayerEntity) {
										for (int i = 0; i <= enchantment.level; i++) {
											((PlayerEntity) deathEvent.getEntityLiving()).inventory.setInventorySlotContents(
													new Random().nextInt(((PlayerEntity) deathEvent.getEntityLiving()).inventory.getSizeInventory()),
													ItemStack.EMPTY
											);
										}
									}
								})
								.setOnLoot((lootingEvent, enchantment) -> {
									lootingEvent.setLootingLevel(0);
									lootingEvent.setCanceled(lootingEvent.isCancelable());
								}),
						new EnchantmentData(3, "minecraft:unbreaking", 1)
								.setOnDamaged((entity, enchant, event) -> {
									if (new Random().nextDouble() * 10 <= enchant.level) {
										event.setCanceled(event.isCancelable());
									}
									return event.getAmount() - (enchant.level / 10f);
								}),
						new EnchantmentData(1, "enchanted_entities:hide_glint", 0)
				)
		);
	}
	
	//https://github.com/3TUSK/SRA/blob/bleeding/src/main/java/info/tritusk/anchor/AnchorScreen.java
	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static final class ScreenRegistry {
		@SubscribeEvent
		public static void setup(FMLClientSetupEvent event) {
			ScreenManager.registerFactory(Container.TYPE, ContainerScreen::new);
		}
	}
	
	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class ContainerRegistry {
		//https://github.com/3TUSK/SRA/blob/bleeding/src/main/java/info/tritusk/anchor/AnchorScreen.java
		@SubscribeEvent
		public static void regContainerType(RegistryEvent.Register<ContainerType<?>> event) {
			event.getRegistry().register(
					(Container.TYPE = new ContainerType<>(Container::new)).setRegistryName("enchanted_entities", "container")
			);
		}
		
		private static Block entityEnchanter;
		
		@SubscribeEvent
		public static void registerBlocks(RegistryEvent.Register<Block> event) {
			entityEnchanter = new EntityEnchatnerBlock(Block.Properties.from(Blocks.ENCHANTING_TABLE).notSolid()).setRegistryName("enchanted_entities:entity_enchanter");
			event.getRegistry().register(
					entityEnchanter
			);
		}
		
		@SubscribeEvent
		public static void registerItems(RegistryEvent.Register<Item> event) {
			event.getRegistry().register(
					new GlintHider(new Item.Properties()).setRegistryName("enchanted_entities:glint_hider")
			);
		}
	}
	
	private static void onDeath(LivingDeathEvent event) {
		for (EntityEnchantment enchantment : EnchantmentManager.getEnchantmentsForEntity(event.getEntityLiving()))
			enchantment.data.onDeath(event, enchantment);
	}
	
	private static boolean isRendering = false;

	public static void onEntityHurt(LivingHurtEvent event) {
		if (event.getSource().getTrueSource() != null) {
			if (event.getSource().getTrueSource() instanceof LivingEntity) {
				for (EntityEnchantment enchantment : EnchantmentManager.getEnchantmentsForEntity((LivingEntity) event.getSource().getTrueSource()))
					enchantment.data.onAttack((LivingEntity) event.getSource().getTrueSource(), event, enchantment);
			} else if (event.getSource().getImmediateSource() != null) {
				for (EntityEnchantment enchantment : EnchantmentManager.getEnchantmentsForEntity((LivingEntity) event.getSource().getImmediateSource()))
					enchantment.data.onAttack((LivingEntity) event.getSource().getImmediateSource(), event, enchantment);
			}
		}
		for (EntityEnchantment enchantment : EnchantmentManager.getEnchantmentsForEntity(event.getEntityLiving()))
			enchantment.data.onDamaged(event.getEntityLiving(), event, enchantment);
	}
	
	private static void poolLoot(LootingLevelEvent event) {
		if (event.getDamageSource().getTrueSource() != null) {
			for (EntityEnchantment enchantment : EnchantmentManager.getEnchantmentsForEntity((LivingEntity) event.getDamageSource().getTrueSource()))
				enchantment.data.onLooting(event, enchantment);
		}
	}
	
	private static void worldTick(TickEvent.WorldTickEvent event) {
		ArrayList<Integer> toRemove = new ArrayList<>();
		int num = 0;
		
		for (EnchantmentDataPacket packet : dataPackets) {
			if (event.world.getEntityByID(packet.target) != null) {
				try {
					INSTANCE.send(
							PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.world.getEntityByID(packet.target)),
							packet
					);
				} catch (Throwable ignored) {
					ignored.printStackTrace();
				}
				toRemove.add(0, num);
			}
			num++;
		}
		
		toRemove.forEach(number -> dataPackets.remove((int) number));
	}
	
	private static void livingTickEvent(LivingEvent.LivingUpdateEvent event) {
		if (event.getEntityLiving().world.isRemote) {
			if (!event.getEntity().getPersistentData().contains("EnchantedEntities") || event.getEntityLiving() instanceof PlayerEntity) {
				INSTANCE.sendToServer(new EnchantmentRequestPacket(event.getEntity().getEntityId()));
			}
			return;
		} else {
			if (!event.getEntity().getPersistentData().contains("EnchantedEntities")) {
				EnchantmentManager.enchantEntity(event.getEntityLiving(), null);
				EntityStats stats = Loader.dataLoader.getStatsForEntity(event.getEntityLiving().getType().getRegistryName());
				if (event.getEntity().world.rand.nextDouble() <= stats.enchantmentWeight)
					for (int i = 0; i < event.getEntity().world.rand.nextInt(4); i++) {
						EnchantmentData data = dataRegistry.get(event.getEntity().world.rand.nextInt(dataRegistry.size()));
						if (stats.blackListedEnchants.contains(data.registryName)) {
							i--;
						} else {
							EnchantmentManager.enchantEntity(
									event.getEntityLiving(),
									new EntityEnchantment(
											new Random(data.maxLevel * event.getEntity().world.getGameTime()).nextInt(data.maxLevel) + 1,
											data
									)
							);
						}
					}
				INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(event::getEntityLiving), new EnchantmentDataPacket(EnchantmentManager.isVisiblyEnchanted(event.getEntityLiving()), event.getEntityLiving().getEntityId(), 0));
			} else {
				if (event.getEntityLiving().ticksExisted % 1 == 0) {
					INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(event::getEntityLiving), new EnchantmentDataPacket(EnchantmentManager.isVisiblyEnchanted(event.getEntityLiving()), event.getEntityLiving().getEntityId(),0));
				}
			}
		}
	}
	
	public static void renderLiving(RenderLivingEvent<LivingEntity, ?> event) {
		if (event instanceof RenderLivingEvent.Post && !isRendering) {
			if (EnchantmentManager.isEnchanted(event.getEntity())) {
				isRendering = true;
				RedirectingBuffer buffer = new RedirectingBuffer(event.getBuffers(), RenderType.getEntityGlint());
				MatrixStack.Entry entry = event.getMatrixStack().getLast();
				try {
					event.getRenderer().render(
							event.getEntity(),
							event.getEntity().getYaw(event.getPartialRenderTick()),
							event.getPartialRenderTick(), event.getMatrixStack(),
							buffer, event.getLight()
					);
				} catch (Throwable ignored) {
				}
				while (!(event.getMatrixStack().getLast() == entry))
					event.getMatrixStack().pop();
				isRendering = false;
			}
		}
	}
	
	public static void renderDragon(RenderDragonEvent<EnderDragonEntity, EnderDragonRenderer> event) {
		if (!isRendering) {
			if (EnchantmentManager.isEnchanted(event.getEntity())) {
				isRendering = true;
				RedirectingBuffer buffer = new RedirectingBuffer(event.getBuffers(), RenderType.getEntityGlint());
				MatrixStack.Entry entry = event.getMatrixStack().getLast();
				try {
					event.getRenderer().render(
							(EnderDragonEntity) event.getEntity(),
							event.getEntity().getYaw(event.getPartialRenderTick()),
							event.getPartialRenderTick(), event.getMatrixStack(),
							buffer, event.getLight()
					);
				} catch (Throwable ignored) {
				}
				while (!(event.getMatrixStack().getLast() == entry))
					event.getMatrixStack().pop();
				isRendering = false;
			}
		}
	}
}
