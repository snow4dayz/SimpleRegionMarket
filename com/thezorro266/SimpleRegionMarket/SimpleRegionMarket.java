package com.thezorro266.SimpleRegionMarket;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.nijikokun.register.payment.Method;
import com.nijikokun.register.payment.Methods;
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
	public static int ERR_NOPERM = 4;
	
	public static String PLUGIN_DIR = null;
	public static String LANGUAGE = null;

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
			LanguageHandler.outputConsole("ERR_NO_ECO", null);
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
				ArrayList<String> list = new ArrayList<String>();
				list.add(region.getId());
				list.add(p.getName());
				LanguageHandler.outputDebug(powner, "REGION_SOLD", list);
			}
			region.getOwners().removePlayer(player);
		}
		getAgentManager().removeAgentsFromRegion(region);
		region.getOwners().addPlayer(getWorldGuard().wrapPlayer(p));
		saveAll();
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
					LanguageHandler.outputDebug(p, "HELP_01", null);
					LanguageHandler.outputDebug(p, "HELP_02", null);
					LanguageHandler.outputDebug(p, "HELP_03", null);
					if(isAdmin(p)) {
						LanguageHandler.outputDebug(p, "HELP_ADM_01", null);
						LanguageHandler.outputDebug(p, "HELP_ADM_02", null);
					} else {
						LanguageHandler.outputDebug(p, "HELP_04", null);
					}
					LanguageHandler.outputDebug(p, "HELP_05", null);
				}
				LanguageHandler.outputDebug(p, "HELP_BUY", null);
			} else {
				LanguageHandler.outputError(p, "ERR_NO_PERM_BUY_SELL", null);
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
					LanguageHandler.outputString(p, string);
					string = "";
					first = true;
				} else {
					first = false;
				}
			}
			LanguageHandler.outputString(p, string);
		} else if (args[0].equalsIgnoreCase("maxregions")) {
			if(isAdmin(p)) {
				int maxregions;
				if(args.length < 2) {
					ArrayList<String> list = new ArrayList<String>();
					list.add(Integer.toString(AgentManager.MAX_REGIONS));
					LanguageHandler.outputDebug(p, "CMD_MAXREGIONS_NO_ARG", list);
					return true;
				}
				try {
					maxregions = Integer.parseInt(args[1]);
				} catch (Exception e) {
					LanguageHandler.outputError(p, "CMD_MAXREGIONS_WRONG_ARG", null);
					return true;
				}
				AgentManager.MAX_REGIONS = maxregions;
				ArrayList<String> list = new ArrayList<String>();
				list.add(Integer.toString(maxregions));
				LanguageHandler.outputDebug(p, "CMD_MAXREGIONS", list);
			} else {
				LanguageHandler.outputError(p, "ERR_NO_PERM", null);
			}
		} else if (args[0].equalsIgnoreCase("lang")) {
			if(isAdmin(p)) {
				if(args.length < 2) {
					ArrayList<String> list = new ArrayList<String>();
					list.add(LANGUAGE);
					LanguageHandler.outputDebug(p, "CMD_LANG_NO_ARG", list);
				} else {
					if(LanguageHandler.setLang(args[1])) {
						LANGUAGE = args[1];
						ArrayList<String> list = new ArrayList<String>();
						list.add(LANGUAGE);
						LanguageHandler.outputDebug(p, "CMD_LANG_SWITCHED", list);
					} else {
						LanguageHandler.outputError(p, "CMD_LANG_NO_LANG", null);
					}
				}
			} else {
				LanguageHandler.outputError(p, "ERR_NO_PERM", null);
			}
		} else {
			return false;
		}
		return true;
	}

	@Override
	public void onDisable() {
		if(ERROR) {
			LanguageHandler.outputConsole("ERR_PLUGIN_UNLOAD", null);
		} else {
			if (saveAll()) {
				LanguageHandler.outputConsole("CONFIG_SAVED", null);
			} else {
				LanguageHandler.outputConsole("ERR_CONFIG_NOT_SAVED", null);
			}
			LanguageHandler.outputConsole("PLUGIN_UNLOAD", null);
		}
	}

	@Override
	public void onEnable() {
		server = getServer();
		PLUGIN_DIR = getDataFolder() + File.separator;

		configuration = new ConfigHandler();
		configuration.load();
		
		LanguageHandler.setLang(LANGUAGE);
		
		if (getWorldGuard() == null) {
			ERROR = true;
			LanguageHandler.outputConsole("ERR_NO_WORLDGUARD", null);
			server.getPluginManager().disablePlugin(this);
			return;
		}
		
		if(server.getPluginManager().getPlugin("Register") == null) {
			ERROR = true;
			LanguageHandler.outputConsole("ERR_NO_REGISTER", null);
			server.getPluginManager().disablePlugin(this);
			return;
		}
		
		LanguageHandler.outputConsole("PLUGIN_LOADING", null);

		server.getPluginManager().registerEvent(Event.Type.BLOCK_BREAK, blockListener, Event.Priority.Normal, this);
		server.getPluginManager().registerEvent(Event.Type.SIGN_CHANGE, blockListener, Event.Priority.Normal, this);
		server.getPluginManager().registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Normal, this);

		agentmanager = new AgentManager();

		System.out.println("SimpleRegionMarket v" + getDescription().getVersion() + " loaded, updated by theZorro266");
	}
}