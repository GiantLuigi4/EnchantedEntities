package com.tfc.enchanted_entities.gui;

import com.tfc.enchanted_entities.API.EnchantmentData;
import com.tfc.enchanted_entities.API.EnchantmentManager;
import com.tfc.enchanted_entities.API.EntityEnchantment;
import com.tfc.enchanted_entities.EnchantedEntities;
import com.tfc.enchanted_entities.utils.WorldPosCallable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.gui.screen.DemoScreen;
import net.minecraft.client.gui.screen.inventory.AnvilScreen;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.stats.Stats;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static net.minecraft.inventory.container.RepairContainer.getNewRepairCost;

public class Container extends net.minecraft.inventory.container.Container implements INamedContainerProvider {
	protected final IInventory thisInventory = new Inventory(37);
	
	public static ContainerType<Container> TYPE;
	
	private final IntReferenceHolder xpSeed = IntReferenceHolder.create(new int[]{new Random().nextInt()},0);
	public final int[] enchantLevels = new int[3];
	public final int[] enchantClue = new int[]{-1, -1, -1};
	public final int[] worldClue = new int[]{-1, -1, -1};
	private WorldPosCallable worldPosCallable;
	
	private final Random rand = new Random();
	
	@Override
	public ITextComponent getDisplayName() {
		return new TranslationTextComponent("enchanted_entities.container.enchanter");
	}
	
	@Nullable
	@Override
	public net.minecraft.inventory.container.Container createMenu(int p_createMenu_1_, PlayerInventory p_createMenu_2_, PlayerEntity p_createMenu_3_) {
		return this;
	}
	
	public Container(ContainerType<?> containerTypeIn, int id, PlayerInventory playerInventoryIn, WorldPosCallable pos) {
		super(containerTypeIn,id);
		
		populateContainer(playerInventoryIn);
		
		this.worldPosCallable = pos;
	}
	
	private void populateContainer(PlayerInventory playerInventoryIn) {
		int i = -(18*1);
		
		int slot = 0;
		
		for (int i1 = 0; i1 < 9; ++i1) {
			this.addSlot(new Slot(playerInventoryIn, slot, 8 + i1 * 18, 161 + i));
			slot++;
		}
		
		for (int l = 0; l < 3; ++l) {
			for (int j1 = 0; j1 < 9; ++j1) {
				this.addSlot(new Slot(playerInventoryIn, slot, 8 + j1 * 18, 103 + l * 18 + i));
				slot++;
			}
		}
		
		int x=(128/4);
		int y=(int)(18*2.8);
		for (int a=0;a<1;a++) {
			this.addSlot(new Slot(thisInventory,slot+a,x,y));
			x+=(18*1.5);
		}
		
		this.trackInt(IntReferenceHolder.create(this.enchantLevels, 0));
		this.trackInt(IntReferenceHolder.create(this.enchantLevels, 1));
		this.trackInt(IntReferenceHolder.create(this.enchantLevels, 2));
		this.trackInt(IntReferenceHolder.create(this.enchantClue, 0));
		this.trackInt(IntReferenceHolder.create(this.enchantClue, 1));
		this.trackInt(IntReferenceHolder.create(this.enchantClue, 2));
		this.trackInt(IntReferenceHolder.create(this.worldClue, 0));
		this.trackInt(IntReferenceHolder.create(this.worldClue, 1));
		this.trackInt(IntReferenceHolder.create(this.worldClue, 2));
	}
	
	public Container(int id, PlayerInventory playerInventoryIn, WorldPosCallable pos) {
		super(TYPE,id);
		
		populateContainer(playerInventoryIn);
		
		this.worldPosCallable = pos;
	}
	
	public Container(int id, PlayerInventory playerInventoryIn) {
		super(TYPE,id);
		
		populateContainer(playerInventoryIn);
		
		this.worldPosCallable = null;
	}
	
	@Override
	public boolean canInteractWith(PlayerEntity playerIn) {
		return true;
	}
	
	@Override
	public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
		ItemStack stack = super.slotClick(slotId, dragType, clickTypeIn, player);
		
		onCraftMatrixChanged(thisInventory);
		
		return stack;
	}
	
	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);
		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			if (index<=8) {
				if (!this.mergeItemStack(itemstack1, 9, 36, false)) {
					return ItemStack.EMPTY;
				}
				
				slot.onSlotChange(itemstack1, itemstack);
			} else if (!this.mergeItemStack(itemstack1, 0, 8, false)) {
				return ItemStack.EMPTY;
			}
			
			if (itemstack1.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}
			
			if (itemstack1.getCount() == itemstack.getCount()) {
				return ItemStack.EMPTY;
			}
			
			slot.onTake(playerIn, itemstack1);
		}
		
		onCraftMatrixChanged(thisInventory);
		
		return itemstack;
	}
	
	@Override
	public void onCraftMatrixChanged(IInventory inventoryIn) {
		super.onCraftMatrixChanged(inventoryIn);
		
		if (worldPosCallable != null) {
			if (inventoryIn == this.thisInventory) {
				ItemStack itemstack = inventoryIn.getStackInSlot(36);
				if (!itemstack.isEmpty() && itemstack.getItem().equals(Items.LAPIS_LAZULI)) {
					this.worldPosCallable.consume((p_217002_2_, p_217002_3_) -> {
						float power = 0;
						
						for(int k = -1; k <= 1; ++k) {
							for(int l = -1; l <= 1; ++l) {
								if ((k != 0 || l != 0) && p_217002_2_.isAirBlock(p_217002_3_.add(l, 0, k)) && p_217002_2_.isAirBlock(p_217002_3_.add(l, 1, k))) {
									power += getPower(p_217002_2_, p_217002_3_.add(l * 2, 0, k * 2));
									power += getPower(p_217002_2_, p_217002_3_.add(l * 2, 1, k * 2));
									
									if (l != 0 && k != 0) {
										power += getPower(p_217002_2_, p_217002_3_.add(l * 2, 0, k));
										power += getPower(p_217002_2_, p_217002_3_.add(l * 2, 1, k));
										power += getPower(p_217002_2_, p_217002_3_.add(l, 0, k * 2));
										power += getPower(p_217002_2_, p_217002_3_.add(l, 1, k * 2));
									}
								}
							}
						}
						
						xpSeed.set(new Random().nextInt());
						this.rand.setSeed((long)this.xpSeed.get());
						
						for(int i1 = 0; i1 < 3; ++i1) {
							this.enchantLevels[i1] = (int)(power*(i1/2f))+1;
							this.enchantClue[i1] = -1;
							this.worldClue[i1] = -1;
							if (this.enchantLevels[i1] < i1 + 1) {
								this.enchantLevels[i1] = 0;
							}
							this.enchantLevels[i1] = net.minecraftforge.event.ForgeEventFactory.onEnchantmentLevelSet(p_217002_2_, p_217002_3_, i1, (int)power, itemstack, enchantLevels[i1]);
						}
						
						for(int j1 = 0; j1 < 3; ++j1) {
							if (this.enchantLevels[j1] > 0) {
								List<EntityEnchantment> list = this.getEnchantmentList(itemstack, j1, this.enchantLevels[j1]*j1);
								if (list != null && !list.isEmpty()) {
									EntityEnchantment enchantmentdata = list.get(this.rand.nextInt(list.size()));
									this.enchantClue[j1] = EnchantedEntities.dataRegistry.indexOf(enchantmentdata.data);
//									this.worldClue[j1] = Math.max(enchantmentdata.level%enchantmentdata.data.maxLevel,1);
									this.worldClue[j1] = (int)MathHelper.clamp(enchantmentdata.level,2,enchantmentdata.data.maxLevel);
								}
							}
						}
						
						this.detectAndSendChanges();
					});
				} else {
					for(int i = 0; i < 3; ++i) {
						this.enchantLevels[i] = 0;
						this.enchantClue[i] = -1;
						this.worldClue[i] = -1;
					}
					xpSeed.set(new Random().nextInt());
				}
			}
		}
	}
	
	private float getPower(net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos) {
		return world.getBlockState(pos).getEnchantPowerBonus(world, pos);
	}
	
	private List<EntityEnchantment> getEnchantmentList(ItemStack stack, int enchantSlot, int level) {
		this.rand.setSeed((long)(this.xpSeed.get() + enchantSlot));
		List<EnchantmentData> list = (List<EnchantmentData>) EnchantedEntities.dataRegistry.clone();
		
		ArrayList<EntityEnchantment> entityEnchantments=new ArrayList<>();
		
//		int amt = rand.nextInt(Math.max(level/5,1))+1;
//
//		int i=1;
		
		level = Math.max(1,level);
		
//		while (entityEnchantments.size()<=amt && !list.isEmpty()) {
//			EnchantmentData data = list.remove(this.rand.nextInt(list.size()));
//			float lvl = rand.nextFloat()/(1/(1-(1f/level)));
//			System.out.println(lvl);
//			entityEnchantments.add(new EntityEnchantment((int)(lvl*data.maxLevel),data));
//			i++;
//		}

		if (entityEnchantments.isEmpty()) {
			EnchantmentData data = list.remove(this.rand.nextInt(list.size()));
			float lvl = rand.nextFloat()/(1/(1-(1f/level)));
			entityEnchantments.add(new EntityEnchantment((int)(lvl*data.maxLevel),data));
//			i++;
		}
		
		return entityEnchantments;
	}
	
	@Override
	public void onContainerClosed(PlayerEntity playerIn) {
		super.onContainerClosed(playerIn);
		playerIn.dropItem(thisInventory.getStackInSlot(36),false);
	}
	
	@Override
	public boolean enchantItem(PlayerEntity playerIn, int id) {
		if (playerIn.world.isRemote) return true;
		if (id >=0 && id <=2) {
			ItemStack stack = thisInventory.getStackInSlot(36);
			if (stack.getCount() >= id+1) {
				if (((Container)playerIn.openContainer).worldPosCallable != null) {
					if (((Container)playerIn.openContainer).worldPosCallable.apply((world,pos)-> {
						List<LivingEntity> entities = world.getLoadedEntitiesWithinAABB(LivingEntity.class,
								new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1),
								LivingEntity::isAlive
						);
						entities.add(playerIn);
						for (LivingEntity entityIn : entities) {
							if (!EnchantmentManager.isEnchanted(entityIn)) {
								EnchantmentManager.enchantEntity(entityIn, new EntityEnchantment(worldClue[id], EnchantedEntities.dataRegistry.get(enchantClue[id])));
								if (id == 2) {
									while (rand.nextFloat() >= 0.5) {
										EntityEnchantment entityEnchantment = getEnchantmentList(new ItemStack(Items.LAPIS_LAZULI, 64), 2, 10).get(0);
										if (!EnchantmentManager.isEnchantedWith(entityIn, entityEnchantment.data)) {
											EnchantmentManager.enchantEntity(entityIn, entityEnchantment);
										}
										xpSeed.set(new Random().nextInt());
										rand.setSeed(new Random().nextLong());
									}
								}
								world.playSound(null, pos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F, world.rand.nextFloat() * 0.1F + 0.9F);
								
								playerIn.experienceLevel -= id;
								
								for (int i = 0; i < 3; i++) {
									this.enchantLevels[i] = 0;
									this.enchantClue[i] = -1;
									this.worldClue[i] = -1;
								}
								xpSeed.set(new Random().nextInt());
								
								onCraftMatrixChanged(thisInventory);
								return "success";
							}
						}
						return "fail";
					}).orElse("fail").equals("success")) {
						stack.setCount(stack.getCount()-(id+1));
					}
				}
			}
			return true;
		}
		
		if (id == 4) {
			ItemStack stack = thisInventory.getStackInSlot(36);
			int k2 = stack.getRepairCost();
			
			k2 = getNewRepairCost(k2);
			if (stack.getItem() instanceof EnchantedBookItem && k2 <=playerIn.experienceLevel) {
				List<LivingEntity> entities = worldPosCallable.world.getLoadedEntitiesWithinAABB(LivingEntity.class,
						new AxisAlignedBB(worldPosCallable.pos.getX(), worldPosCallable.pos.getY(), worldPosCallable.pos.getZ(), worldPosCallable.pos.getX() + 1, worldPosCallable.pos.getY() + 1, worldPosCallable.pos.getZ() + 1),
						LivingEntity::isAlive
				);
				entities.add(playerIn);
				for (LivingEntity entityIn : entities) {
					EnchantedBookItem.getEnchantments(stack).forEach(enchantment ->
							EnchantmentManager.enchantEntity(entityIn,
									new EntityEnchantment(
											((CompoundNBT) enchantment).getInt("lvl"),
											EnchantmentManager.getEnchantmentByID(
													((CompoundNBT) enchantment).getString("id")
											))));
					worldPosCallable.world.playSound(null, worldPosCallable.pos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F, worldPosCallable.world.rand.nextFloat() * 0.1F + 0.9F);
					
					for (int i = 0; i < 3; i++) {
						this.enchantLevels[i] = 0;
						this.enchantClue[i] = -1;
						this.worldClue[i] = -1;
					}
					xpSeed.set(new Random().nextInt());
					
					playerIn.experienceLevel += (-k2);
					
					thisInventory.setInventorySlotContents(36,new ItemStack(Items.BOOK));
					
					onCraftMatrixChanged(thisInventory);
				}
			}
		}
		
		return super.enchantItem(playerIn,id);
	}
}
