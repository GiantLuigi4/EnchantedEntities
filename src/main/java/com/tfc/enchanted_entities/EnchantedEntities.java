package com.tfc.enchanted_entities;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.tfc.enchanted_entities.API.EnchantmentData;
import com.tfc.enchanted_entities.API.EnchantmentManager;
import com.tfc.enchanted_entities.API.EntityEnchantment;
import com.tfc.enchanted_entities.events.RenderDragonEvent;
import com.tfc.enchanted_entities.network.EnchantmentDataPacket;
import com.tfc.enchanted_entities.network.EnchantmentRequestPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.enchantment.FireAspectEnchantment;
import net.minecraft.enchantment.ThornsEnchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
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
	
	public EnchantedEntities() {
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.addListener(EnchantedEntities::livingTickEvent);
		MinecraftForge.EVENT_BUS.addListener(EnchantedEntities::onEntityHurt);
		MinecraftForge.EVENT_BUS.addListener(EnchantedEntities::onServerStarting);
		MinecraftForge.EVENT_BUS.addListener(EnchantedEntities::poolLoot);

		if (FMLEnvironment.dist.isClient()) {
			MinecraftForge.EVENT_BUS.addListener(EnchantedEntities::renderLiving);
			MinecraftForge.EVENT_BUS.addListener(EnchantedEntities::renderDragon);
		}
		
		INSTANCE.registerMessage(0, EnchantmentRequestPacket.class, EnchantmentRequestPacket::writePacketData, EnchantmentRequestPacket::new, (packet, contex) -> {
			INSTANCE.send(
					PacketDistributor.PLAYER.with(() -> contex.get().getSender()),
					new EnchantmentDataPacket(
							EnchantmentManager.isEnchanted((LivingEntity) contex.get().getSender().world.getEntityByID(packet.id)),
							packet.id
					)
			);
		});
		INSTANCE.registerMessage(1, EnchantmentDataPacket.class, EnchantmentDataPacket::writePacketData, EnchantmentDataPacket::new, (packet, contex) -> {
		});
	}
	
	public static void onServerStarting(FMLServerStartingEvent event1) {
		dataRegistry.clear();
		
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
								(entity, enchantment, event) -> event.getAmount() + ((event.getSource().getImmediateSource() == null || (event.getSource().getTrueSource() != null && event.getSource().getImmediateSource().equals(event.getSource().getTrueSource()))) ? (enchantment.level / 2f):0),
								(entity, enchantment, event) -> event.getAmount(),
								0
						),
						new EnchantmentData(
								5, "minecraft:power", 1,
								(BiConsumer<LivingEntity, EntityEnchantment>) emptyConsumer,
								(BiConsumer<CreeperEntity, EntityEnchantment>) emptyConsumer,
								(entity, enchantment, event) -> event.getAmount() + (!(event.getSource().getImmediateSource() == null || (event.getSource().getTrueSource() != null && event.getSource().getImmediateSource().equals(event.getSource().getTrueSource()))) ? (enchantment.level / 2f):0),
								(entity, enchantment, event) -> event.getAmount(),
								0
						),
						new EnchantmentData(
								1, "minecraft:flame", 1,
								(BiConsumer<LivingEntity, EntityEnchantment>) emptyConsumer,
								(BiConsumer<CreeperEntity, EntityEnchantment>) emptyConsumer,
								(entity, enchantment, event) -> {
									if (!((event.getSource().getImmediateSource() == null || (event.getSource().getTrueSource() != null && event.getSource().getImmediateSource().equals(event.getSource().getTrueSource()))))) {
										event.getEntityLiving().setFireTimer((enchantment.level*50));
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
										event.getEntityLiving().setFireTimer((enchantment.level*80));
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
												event.getEntityLiving().addVelocity(vec3d.x,vec3d.y,vec3d.z);
											}
										}
										else if (event.getSource().getImmediateSource() != null) {
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
												event.getEntityLiving().addVelocity(vec3d.x,vec3d.y,vec3d.z);
											}
										}
										else if (event.getSource().getImmediateSource() != null) {
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
								3, "minecraft:thorns", 1,
								(BiConsumer<LivingEntity, EntityEnchantment>) emptyConsumer,
								(BiConsumer<CreeperEntity, EntityEnchantment>) emptyConsumer,
								(entity, enchantment, event) -> event.getAmount(),
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
								},
								0
						),
						new EnchantmentData(
								3, "minecraft:looting", 1,
								(BiConsumer<LivingEntity, EntityEnchantment>) emptyConsumer,
								(BiConsumer<CreeperEntity, EntityEnchantment>) emptyConsumer,
								(entity, enchantment, amt) -> amt,
								(entity, enchantment, amt) -> amt
						).onLoot((lootingEvent,enchantment)-> lootingEvent.setLootingLevel(lootingEvent.getLootingLevel()+enchantment.level))
				)
		);
		
		EnchantEntityCommand.register(event1.getCommandDispatcher());
	}
	
	private static boolean isRendering = false;

//	private static void registerCapabilities() {
//		CapabilityManager.INSTANCE.register(
//				EnchantmentCapability.class,
//				new EnchantmentCapability(),
//				EnchantmentCapability::new
//		);
//	}
	
	public static void onEntityHurt(LivingHurtEvent event) {
		if (event.getSource().getTrueSource() != null) {
			if (event.getSource().getTrueSource() instanceof LivingEntity) {
				for (EntityEnchantment enchantment : EnchantmentManager.getEnchantmentsForEntity((LivingEntity) event.getSource().getTrueSource()))
					enchantment.data.onHitEntity((LivingEntity) event.getSource().getTrueSource(), event, enchantment);
			} else if (event.getSource().getImmediateSource() != null) {
				for (EntityEnchantment enchantment : EnchantmentManager.getEnchantmentsForEntity((LivingEntity) event.getSource().getImmediateSource()))
					enchantment.data.onHitEntity((LivingEntity) event.getSource().getImmediateSource(), event, enchantment);
			}
		}
		for (EntityEnchantment enchantment : EnchantmentManager.getEnchantmentsForEntity(event.getEntityLiving()))
			enchantment.data.onEntityHit(event.getEntityLiving(), event, enchantment);
	}
	
	private static void poolLoot(LootingLevelEvent event) {
		if (event.getDamageSource().getTrueSource() != null) {
			for (EntityEnchantment enchantment : EnchantmentManager.getEnchantmentsForEntity((LivingEntity) event.getDamageSource().getTrueSource()))
				enchantment.data.onLooting(event, enchantment);
		}
//		event.setLootingLevel(1000);
	}
	
	private static void livingTickEvent(LivingEvent.LivingUpdateEvent event) {
		if (event.getEntityLiving().world.isRemote) {
			if (!event.getEntity().getPersistentData().contains("EnchantedEntities")) {
				INSTANCE.sendToServer(new EnchantmentRequestPacket(event.getEntity().getEntityId()));
			}
		} else {
			if (!event.getEntity().getPersistentData().contains("EnchantedEntities")) {
				EnchantmentManager.enchantEntity(event.getEntityLiving(), null);
				if (event.getEntity().world.rand.nextDouble() >= 0.9)
					for (int i = 0; i < event.getEntity().world.rand.nextInt(4); i++) {
						EnchantmentData data = dataRegistry.get(event.getEntity().world.rand.nextInt(dataRegistry.size()));
						EnchantmentManager.enchantEntity(
								event.getEntityLiving(),
								new EntityEnchantment(
										new Random(data.maxLevel * event.getEntity().world.getGameTime()).nextInt(data.maxLevel) + 1,
										data
								)
						);
					}
				INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(event::getEntityLiving),new EnchantmentDataPacket(EnchantmentManager.isEnchanted(event.getEntityLiving()),event.getEntityLiving().getEntityId()));
			} else {
				if (event.getEntityLiving().ticksExisted%100==0) {
					INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(event::getEntityLiving),new EnchantmentDataPacket(EnchantmentManager.isEnchanted(event.getEntityLiving()),event.getEntityLiving().getEntityId()));
				}
			}
		}
//		EnchantmentManager.unenchantEntity(
//				event.getEntityLiving(),
//				null
//		);
		
//		event.getEntityLiving().hurtResistantTime=0;
//		if (event.getEntityLiving() instanceof PlayerEntity)
//		event.getEntityLiving().attackEntityFrom(DamageSource.causeMobDamage(event.getEntityLiving()),-1);
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
