package com.tfc.enchanted_entities.data;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.client.resources.ReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Loader extends ReloadListener<Map<ResourceLocation, JsonObject>> {
	private final Gson gson = new Gson();
	protected Map<ResourceLocation, EntityStats> dataMap;
	
	public static final Loader dataLoader = new Loader();

	@Override
	protected Map<ResourceLocation, JsonObject> prepare(IResourceManager resourceManagerIn, IProfiler profilerIn) {
		dataMap = new HashMap<>();
		HashMap<ResourceLocation, JsonObject> locationJsonObjectHashMap = new HashMap<>();
		resourceManagerIn.getAllResourceLocations("enchantment_data",(name)->name.endsWith(".json")).forEach((location)->{
			try {
				InputStream stream = resourceManagerIn.getResource(location).getInputStream();
				byte[] bytes = new byte[stream.available()];
				stream.read(bytes);
				stream.close();
				String text = new String(bytes);
				locationJsonObjectHashMap.put(location,gson.fromJson(text,JsonObject.class));
			} catch (Throwable ignored) {
			}
		});
		return locationJsonObjectHashMap;
	}
	
	@Override
	protected void apply(Map<ResourceLocation, JsonObject> objectIn, IResourceManager resourceManagerIn, IProfiler profilerIn) {
		dataMap = new HashMap<>();
		objectIn.forEach((name,object)->{
			object.entrySet().forEach((entry)->{
				ArrayList<String> blacklistedEnchants = new ArrayList<>();
				entry.getValue().getAsJsonObject().getAsJsonArray("black_listed_enchants").iterator().forEachRemaining((element)->{
					blacklistedEnchants.add(element.getAsString());
				});
				dataMap.put(new ResourceLocation(entry.getKey()),new EntityStats(
						entry.getValue().getAsJsonObject().get("enchantment_weight").getAsFloat(),
						blacklistedEnchants
				));
			});
		});
	}
	
	public EntityStats getStatsForEntity(ResourceLocation registryName) {
		return dataMap.getOrDefault(registryName,dataMap.getOrDefault(new ResourceLocation("minecraft:default"),new EntityStats(0.25f,new ArrayList<>())));
	}
}
