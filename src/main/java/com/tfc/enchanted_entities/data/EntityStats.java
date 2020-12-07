package com.tfc.enchanted_entities.data;

import java.util.ArrayList;

public class EntityStats {
	public final float enchantmentWeight;
	public final ArrayList<String> blackListedEnchants;
	
	public EntityStats(float enchantmentWeight, ArrayList<String> blackListedEnchants) {
		this.enchantmentWeight = enchantmentWeight;
		this.blackListedEnchants = blackListedEnchants;
	}
}
