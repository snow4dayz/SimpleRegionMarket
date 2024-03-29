package com.thezorro266.SimpleRegionMarket;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;

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

	private boolean error = false;

	public static int ERR_NAME = 0;
	public static int ERR_NOOWN = 1;
	public static int ERR_MONEY = 2;
	public static int ERR_OWN = 3;
	public static int ERR_NOPERM = 4;

	public static String plugin_dir = null;
	public static String language = null;

	public static void saveAll() {
		configuration.save();
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
		if(Methods.hasMethod())
			return Methods.getMethod();
		else {
			LanguageHandler.langOutputConsole("ERR_NO_ECO", Level.SEVERE, null);
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
		return (player.hasPermission("simpleregionmarket.admin") || player.isOp());
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
					list.add(Integer.toString(AgentManager.max_regions));
					LanguageHandler.outputDebug(p, "CMD_MAXREGIONS_NO_ARG", list);
					return true;
				}
				try {
					maxregions = Integer.parseInt(args[1]);
				} catch (Exception e) {
					LanguageHandler.outputError(p, "CMD_MAXREGIONS_WRONG_ARG", null);
					return true;
				}
				AgentManager.max_regions = maxregions;
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
					list.add(language);
					LanguageHandler.outputDebug(p, "CMD_LANG_NO_ARG", list);
				} else {
					if(LanguageHandler.setLang(args[1])) {
						language = args[1];
						ArrayList<String> list = new ArrayList<String>();
						list.add(language);
						LanguageHandler.outputDebug(p, "CMD_LANG_SWITCHED", list);
					} else {
						LanguageHandler.outputError(p, "CMD_LANG_NO_LANG", null);
					}
				}
			} else {
				LanguageHandler.outputError(p, "ERR_NO_PERM", null);
			}
		} else
			return false;
		return true;
	}

	@Override
	public void onDisable() {
		if(error) {
			LanguageHandler.langOutputConsole("ERR_PLUGIN_UNLOAD", Level.SEVERE, null);
		} else {
			saveAll();
			LanguageHandler.langOutputConsole("PLUGIN_UNLOAD", Level.INFO, null);
		}
	}

	@Override
	public void onEnable() {
		server = getServer();
		agentmanager = new AgentManager();
		plugin_dir = getDataFolder() + File.separator;

		configuration = new ConfigHandler();
		configuration.load();

		LanguageHandler.setLang(language);

		if (getWorldGuard() == null) {
			error = true;
			LanguageHandler.langOutputConsole("ERR_NO_WORLDGUARD", Level.SEVERE, null);
			server.getPluginManager().disablePlugin(this);
			return;
		}

		if(server.getPluginManager().getPlugin("Register") == null) {
			error = true;
			LanguageHandler.langOutputConsole("ERR_NO_REGISTER", Level.SEVERE, null);
			server.getPluginManager().disablePlugin(this);
			return;
		}
		
		getAgentManager().checkAgents();

		server.getPluginManager().registerEvent(Event.Type.BLOCK_BREAK, blockListener, Event.Priority.Normal, this);
		server.getPluginManager().registerEvent(Event.Type.SIGN_CHANGE, blockListener, Event.Priority.Normal, this);
		server.getPluginManager().registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Normal, this);
		
		LanguageHandler.outputConsole(Level.INFO, "Version " + getDescription().getVersion() + " loaded, updated by theZorro266");
	}
}