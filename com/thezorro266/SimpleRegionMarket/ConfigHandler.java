package com.thezorro266.SimpleRegionMarket;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.config.Configuration;

public class ConfigHandler {
	private static File agents;
	private static File config;

	public ConfigHandler(String path) {
		agents = new File(path + "agents.yml");
		config = new File(path + "config.yml");
	}

	public boolean load() {
		Configuration confighandle;
		try {
			confighandle = new Configuration(config);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		confighandle.load();
		AgentManager.MAX_REGIONS = confighandle.getInt("maxregions", 0);
		
		try {
			confighandle = new Configuration(agents);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		confighandle.load();
		boolean ret = false;
		String path;
		for (String world : confighandle.getKeys()) {
			path = world;
			for (String region : confighandle.getKeys(path)) {
				path = world + "." + region;
				for (String signnr : confighandle.getKeys(path)) {
					path = world + "." + region + "." + signnr;
					String besitzer = confighandle.getString(path + ".Owner");
					double preis = confighandle.getDouble(path + ".Price", 0);
					if (preis >= 0) {
						ret = true;
						SimpleRegionMarket.getAgentManager().getAgentList().add(new RegionAgent(region,
								new Location(
										Bukkit.getWorld(world),
										confighandle.getDouble(path + ".X", 0),
										confighandle.getDouble(path + ".Y", 0),
										confighandle.getDouble(path + ".Z", 0)),
								besitzer, preis));
					}
				}
			}
		}
		return ret;
	}

	public boolean save() {
		Configuration confighandle;
		try {
			confighandle = new Configuration(config);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		confighandle.setProperty("maxregions", AgentManager.MAX_REGIONS);
		confighandle.save();
		
		try {
			confighandle = new Configuration(agents);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		boolean ret = false;
		if (confighandle != null) {
			int i = 0;
			String path;
			for (RegionAgent obj : SimpleRegionMarket.getAgentManager().getAgentList()) {
				Location loc = obj.getLocation();
				path = loc.getWorld().getName() + "." + obj.getRegion() + "." + Integer.toString(i);
				confighandle.setProperty(path + ".X", loc.getX());
				confighandle.setProperty(path + ".Y", loc.getY());
				confighandle.setProperty(path + ".Z", loc.getZ());
				confighandle.setProperty(path + ".Owner", obj.getOwner());
				confighandle.setProperty(path + ".Price", obj.getPrice());
				i++;
			}
			confighandle.save();
			ret = true;
		}
		return ret;
	}
}
