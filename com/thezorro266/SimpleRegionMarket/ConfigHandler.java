package com.thezorro266.SimpleRegionMarket;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.config.Configuration;

public class ConfigHandler {
	private static File file;

	public ConfigHandler(String path) {
		file = new File(path);
	}

	public boolean load() {
		Configuration config;
		try {
			config = new Configuration(file);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		config.load();
		boolean ret = false;
		String path;
		for (String world : config.getKeys()) {
			path = world;
			for (String region : config.getKeys(path)) {
				path += "." + region;
				for (String signnr : config.getKeys(path)) {
					path += "." + signnr;
					String besitzer = config.getString(path + ".Owner");
					double preis = config.getDouble(path + ".Price", 0);
					if (preis >= 0) {
						ret = true;
						SimpleRegionMarket.getAgentManager().getAgentList().add(new RegionAgent(region,
								new Location(
										Bukkit.getWorld(world),
										config.getDouble(path + ".X", 0),
										config.getDouble(path + ".Y", 0),
										config.getDouble(path + ".Z", 0)),
								besitzer, preis));
					}
				}
			}
		}
		return ret;
	}

	public boolean save() {
		SimpleRegionMarket.getAgentManager().checkAgents();

		Configuration config;
		try {
			config = new Configuration(file);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		boolean ret = false;
		if (config != null) {
			int i = 0;
			String path;
			for (RegionAgent obj : SimpleRegionMarket.getAgentManager().getAgentList()) {
				Location loc = obj.getLocation();
				path = loc.getWorld().getName() + "." + obj.getRegion() + "." + Integer.toString(i);
				config.setProperty(path + ".X", loc.getX());
				config.setProperty(path + ".Y", loc.getY());
				config.setProperty(path + ".Z", loc.getZ());
				config.setProperty(path + ".Owner", obj.getOwner());
				config.setProperty(path + ".Price", obj.getPrice());
				i++;
			}
			config.save();
			ret = true;
		}
		return ret;
	}
}
