package com.thezorro266.SimpleRegionMarket;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

public class LanguageHandler {
	private static Configuration languagefile;

	public static boolean setLang(String lang) {
		if(lang == null) {
			lang = "en";
		}
		File default_lang = new File(SimpleRegionMarket.PLUGIN_DIR + "en.yml");
		if(!default_lang.exists()) {
			try {
				default_lang.createNewFile();
				FileWriter fwstream = null;
				fwstream = new FileWriter(SimpleRegionMarket.PLUGIN_DIR + "en.yml");
				String nl = System.getProperty("line.separator");
				fwstream.write("ERR_NO_WORLDGUARD: 'Error: WorldGuard was not found.'" + nl +
						"ERR_NO_REGISTER: 'Error: Register was not found.'" + nl +
						"ERR_NO_ECO: 'Error: Economic System was not found.'" + nl +
						"ERR_ECO_TRANSFER: 'There was a problem with transfering the money.'" + nl +
						"ERR_NO_ECO_USER: 'The economic system was not found, please tell the server owner.'" + nl +
						"ERR_CREATE_ECO_ACCOUNT: 'Error: Could not create economy account \"$0\".'" + nl +
						"PLUGIN_LOADING: 'OK: All plugins found, SimpleRegionMarket is loading..'" + nl +
						"CONFIG_SAVED: 'Config saved successfully.'" + nl +
						"ERR_CONFIG_NOT_SAVED: 'Error: Config was not saved successfully.'" + nl +
						"PLUGIN_UNLOAD: 'Plugin successfully unloaded.'" + nl +
						"ERR_PLUGIN_UNLOAD: 'Error: Plugin unloaded. There was an error with the other plugins.'" + nl +
						"ERR_NO_MONEY: 'You do not have enough money.'" + nl +
						"ERR_NO_PRICE: 'Price not found.'" + nl +
						"ERR_PRICE_UNDER_ZERO: 'The price cannot be lower than zero.'" + nl +
						"ERR_NO_PERM: 'You are not allowed to do that.'" + nl +
						"ERR_NO_PERM_BUY: 'You do not have the permission to buy a region.'" + nl +
						"ERR_NO_PERM_BUY_SELL: 'You cannot buy or sell a region.'" + nl +
						"ERR_PLACE_AGENT: 'The agent could not be created.'" + nl +
						"AGENT_PLACED: 'You have got now $0 agent(s) placed for this region.'" + nl +
						"AGENT_DELETE: 'Successfully deleted the agent.'" + nl +
						"AGENT_YOURS: 'This is your agent.'" + nl +
						"ERR_REGION_NAME: 'There is no region with this name.'" + nl +
						"ERR_REGION_NO_OWNER: 'You do not own this region.'" + nl +
						"ERR_REGION_OWNER: 'You own this region.'" + nl +
						"ERR_REGION_PRICE: 'There were found some signs, which do not have the same price.'" + nl +
						"ERR_REGION_PRICE_SHOW: 'Region $0, price $1 and $2.'" + nl +
						"ERR_REGION_DELETE: 'Check: Region $0 does not exist anymore. Deleted Sign.'" + nl +
						"ERR_REGION_LIMIT: 'You have reached your limit of regions on this world.'" + nl +
						"ERR_REGION_BUY_YOURS: 'You cannot buy this region, because its yours.'" + nl +
						"REGION_SOLD: 'The region $0 was sold to $1.'" + nl +
						"REGION_BUYED_NONE: 'You successfully bought the region $0.'" + nl +
						"REGION_BUYED_USER: 'You successfully bought the region $0 from $1.'" + nl +
						"REGION_OFFER_NONE: 'You offer the region for sale by the server.'" + nl +
						"REGION_OFFER_USER: 'You offer your region for sale.'" + nl +
						"HELP_01: 'To sell a region you need to place a sign on it.'" + nl +
						"HELP_02: 'In the first line of the sign should be \"[AGENT]\"'" + nl +
						"HELP_03: 'in the second line the name of the region (if there is more than one region at that location)'" + nl +
						"HELP_04: 'and in the third line there should be the price (if there is not already a sign with a given price).'" + nl +
						"HELP_ADM_01: 'in the third line there should be the price (if there is not already a sign with a given price)'" + nl +
						"HELP_ADM_02: 'and in the last line there can be a \"none\" that the money does not go to you, but to the server.'" + nl +
						"HELP_05: 'After successfully creating the sign the last line will be filled with the size of the region.'" + nl +
						"HELP_BUY: 'If you want to buy a region, just right-click on the sign.'" + nl +
						"CMD_MAXREGIONS_NO_ARG: 'Max Regions: $0 - Use /rm maxregions [VALUE] to set Max Regions for all players.'" + nl +
						"CMD_MAXREGIONS_WRONG_ARG: 'Use /rm maxregions [VALUE] to set Max Regions for all players.'" + nl +
						"CMD_MAXREGIONS: 'Max Regions for all players set to $0.'" + nl +
						"CMD_LANG_NO_ARG: 'Language: \"$0\" - Use /rm lang [LANGUAGE] to set the language.'" + nl +
						"CMD_LANG_SWITCHED: 'Successfully switched to English translated by <SERVERNAME HERE> :D'" + nl +
						"CMD_LANG_NO_LANG: 'Language not found.'");
				fwstream.close();
				System.out.println("[SimpleRegionMarket] Loaded default Language \"en\".");
			} catch (IOException e) {
				System.out.println("[SimpleRegionMarket] Error: Loading default Language \"en\" failed.");
				e.printStackTrace();
			}
		}

		File choosen_lang = new File(SimpleRegionMarket.PLUGIN_DIR + lang + ".yml");
		if(choosen_lang.exists()) {
			try {
				languagefile = new Configuration(choosen_lang);
				languagefile.load();
				return true;
			} catch (Exception e) {
				System.out.println("[SimpleRegionMarket] Error: Could not open \"" + lang + ".yml\" as configuration.");
				e.printStackTrace();
				return false;
			}
		} else {
			try {
				languagefile = new Configuration(default_lang);
				languagefile.load();
				return false;
			} catch (Exception e) {
				System.out.println("[SimpleRegionMarket] Error: Could not open \"en.yml\" as configuration.");
				e.printStackTrace();
				return false;
			}
		}
	}

	private static String defaultLanguage(String id) {
		String string = "";
		if(id != null) {
		}
		return string;
	}

	private static String parseLanguageString(String id, ArrayList<String> args) {
		String string = "";
		if(languagefile != null && languagefile.getString(id) != null) {
			string = languagefile.getString(id);
		} else {
			string = defaultLanguage(id);
		}

		for(int i = string.length()-1; i >= 0; i--) {
			if(string.charAt(i) == '$') {
				if(string.charAt(i-1) == '$') {
					string = string.substring(0, i) + string.substring(i+1, string.length());
				} else if(Character.isDigit(string.charAt(i+1))) {
					int argi;
					try {
						argi = Integer.parseInt(Character.toString(string.charAt(i+1)));
					} catch (Exception e) {
						string = string.substring(0, i) + "ERROR ARGUMENT" + string.substring(i+2, string.length());
						continue;
					}

					try {
						string = string.substring(0, i) + args.get(argi) + string.substring(i+2, string.length());
					} catch (Exception e) {
						string = string.substring(0, i) + "ERROR ARGUMENT" + string.substring(i+2, string.length());
						continue;
					}
				}
			}
		}
		return string;
	}

	public static void outputConsole(String id, ArrayList<String> args) {
		System.out.println("[SimpleRegionMarket] " + parseLanguageString(id, args));
	}

	public static void outputDebug(Player p, String id, ArrayList<String> args) {
		p.sendMessage(ChatColor.WHITE + "[" + ChatColor.DARK_BLUE + "SimpleRegionMarket" + ChatColor.WHITE + "] " + ChatColor.YELLOW + parseLanguageString(id, args));
	}

	public static void outputError(Player p, String id, ArrayList<String> args) {
		p.sendMessage(ChatColor.WHITE + "[" + ChatColor.DARK_BLUE + "SimpleRegionMarket" + ChatColor.WHITE + "] " + ChatColor.RED + parseLanguageString(id, args));
	}

	public static void outputString(Player p, String string) {
		p.sendMessage(ChatColor.WHITE + "[" + ChatColor.DARK_BLUE + "SimpleRegionMarket" + ChatColor.WHITE + "] " + ChatColor.YELLOW + string);
	}
}
