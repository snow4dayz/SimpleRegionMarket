package com.thezorro266.SimpleRegionMarket;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.nijikokun.register.payment.*;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class SimpleRegionMarket extends JavaPlugin {
	private static Server server;
	private static ConfigHandler configuration;
	private static AgentManager agentmanager;
	
	private boolean ERROR = false;

	public static int ERR_NAME = 0;
	public static int ERR_NOOWN = 1;
	public static int ERR_MONEY = 2;
	public static int ERR_OWN = 3;

	public static boolean saveAll() {
		return configuration.save();
	}
	
	public static AgentManager getAgentManager() {
		return agentmanager;
	}

	public static WorldGuardPlugin getWorldGuard() {
		Plugin plugin = server.getPluginManager().getPlugin("WorldGuard");

		if (plugin == null || !(plugin instanceof WorldGuardPlugin))
			return null;

		return (WorldGuardPlugin) plugin;
	}
	
	public static Method getEconomicManager() {
		if(Methods.hasMethod()) {
			return Methods.getMethod();
		} else {
			outputConsole("Error: Economic System was not found.");
			return null;
		}
	}

	public static boolean canBuy(Player player) {
		return (player.hasPermission("simpleregionmarket.buy") || canSell(player) || isAdmin(player));
	}

	public static boolean canSell(Player player) {
		return (player.hasPermission("simpleregionmarket.sell") || isAdmin(player));
	}

	public static boolean isAdmin(Player player) {
		return player.hasPermission("simpleregionmarket.admin");
	}
	
	public static void sellRegion(ProtectedRegion region, Player p) {
		for (String player : region.getOwners().getPlayers()) {
			Player powner;
			powner = Bukkit.getPlayerExact(player);
			if (powner != null) {
				outputDebug(powner, "The region " + region.getId() + " was sold to " + p.getName() + ".");
			}
			region.getOwners().removePlayer(player);
		}
		getAgentManager().removeAgentsFromRegion(region);
		region.getOwners().addPlayer(getWorldGuard().wrapPlayer(p));
		saveAll();
	}

	public static void outputConsole(String output) {
		System.out.println("[SimpleRegionMarket] " + output);
	}

	public static void outputDebug(Player p, String s) {
		p.sendMessage(ChatColor.AQUA + "[SimpleRegionMarket] " + ChatColor.YELLOW + s);
	}

	public static void outputError(Player p, String s) {
		p.sendMessage(ChatColor.AQUA + "[SimpleRegionMarket] " + ChatColor.RED + s);
	}

	public static void outputError(Player p, int errorID) {
		if (errorID == ERR_NAME)
			outputError(p, "There is no region with this name.");
		if (errorID == ERR_NOOWN)
			outputError(p, "You don't own this region.");
		if (errorID == ERR_MONEY)
			outputError(p, "You don't have enough money.");
		if (errorID == ERR_OWN)
			outputError(p, "You own this region.");
	}

	private BListener blockListener = new BListener();

	private PListener playerListener = new PListener();

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {

		if (!(sender instanceof Player))
			return false;

		Player p = (Player) sender;

		if (args.length < 1)
			return false;

		if (args[0].equalsIgnoreCase("?")) {
			if(canBuy(p)) {
				if(canSell(p)) {
					outputDebug(p, "To sell a region you need to place a sign on it.");
					outputDebug(p, "In the first line of the sign should be '[AGENT]',");
					if(isAdmin(p)) {
						outputDebug(p, "in the second line the name of the region (if there is more than one region at that location),");
						outputDebug(p, "in the third line there should be the price (if there is not already a sign with a given price)");
						outputDebug(p, "and in the last line there can be a 'none' that the money does not go to you, but to the server.");
					} else {
						outputDebug(p, "in the second line the name of the region (if there is more than one region at that location)");
						outputDebug(p, "and in the third line there should be the price (if there is not already a sign with a given price).");
					}
					outputDebug(p, "After successfully creating the sign the last line will be filled with the size of the region.");
				}
				outputDebug(p, "If you want to buy a region, just right-click on the sign.");
			} else {
				outputError(p, "You cannot buy or sell a region.");
			}
		} else if (args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("l")) {
			ArrayList<RegionAgent> list = new ArrayList<RegionAgent>();
			getAgentManager().checkAgents();
			for (RegionAgent agent : getAgentManager().getAgentList()) {
				if (agent.getWorldWorld() == p.getWorld()) {
					boolean add = true;
					for (RegionAgent tmp : list) {
						if (tmp.getProtectedRegion() == agent.getProtectedRegion()) {
							add = false;
							break;
						}
					}
					if (add) {
						list.add(agent);
					}
				}
			}

			String string = "";
			boolean first = true;
			for (RegionAgent agent : list) {
				if (!first) {
					string += " | ";
				}
				string += agent.getRegion() + " - " + getEconomicManager().format(agent.getPrice());
				if (string.length() > 80) {
					outputDebug(p, string);
					first = true;
				} else {
					first = false;
				}
			}
			outputDebug(p, string);
		} else {
			return false;
		}
		return true;
	}

	@Override
	public void onDisable() {
		if(ERROR) {
			outputConsole("Plugin unloaded. There was an error with the other plugins.");
		} else {
			if (saveAll()) {
				outputConsole("Config saved successfully.");
			} else {
				outputConsole("Error: Config wasn't saved successfully.");
			}
			outputConsole("Plugin successfully unloaded.");
		}
	}

	@Override
	public void onEnable() {
		server = getServer();
		
		if (getWorldGuard() == null) {
			ERROR = true;
			outputConsole("Error: WorldGuard was not found.");
			server.getPluginManager().disablePlugin(this);
			return;
		}

		outputConsole("OK: All plugins found, SimpleRegionMarket is loading..");

		server.getPluginManager().registerEvent(Event.Type.BLOCK_BREAK, blockListener, Event.Priority.Normal, this);
		server.getPluginManager().registerEvent(Event.Type.SIGN_CHANGE, blockListener, Event.Priority.Normal, this);
		server.getPluginManager().registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Normal, this);

		agentmanager = new AgentManager();

		configuration = new ConfigHandler(getDataFolder() + File.separator + "agents.yml");

		if (!configuration.load()) {
			outputConsole("I did not found any agents in the configuration.");
		}

		outputConsole("Successfully v" + getDescription().getVersion() + " loaded, updated by theZorro266");
	}
}