package com.thezorro266.SimpleRegionMarket;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class AgentManager {

	private ArrayList<RegionAgent> agents = new ArrayList<RegionAgent>();
	
	public static int MAX_REGIONS = 0;

	public boolean addAgent(ProtectedRegion region, Location loc, Player p, double price) {
		if (region != null) {
			if (loc != null) {
				if (price >= 0) {
					String pname = "";
					if(p != null) {
						pname = p.getName();
					}
					return getAgentList().add(new RegionAgent(region.getId(), loc, pname, price));
				}
			}
		}
		return false;
	}

	public void checkAgents() {
		Iterator<RegionAgent> itr = getAgentList().iterator(); 
		while(itr.hasNext()) {
			RegionAgent obj = (RegionAgent) itr.next(); 
			if (obj.getWorldWorld() == null) {
				SimpleRegionMarket.outputConsole("Check: World " + obj.getWorld() + " doesn't exist anymore. Deleted Sign.");
			} else if(obj.getProtectedRegion() == null) {
				obj.destroyAgent(false);
				itr.remove();
				SimpleRegionMarket.outputConsole("Check: Region " + obj.getRegion() + " doesn't exist anymore. Deleted Sign.");
			}
		}
	}

	public int countAgents(ProtectedRegion region) {
		checkAgents();
		int count = 0;
		if (region != null) {
			for (RegionAgent obj : getAgentList()) {
				if (obj.getProtectedRegion() == region) {
					count++;
				}
			}
		}
		return count;
	}

	public RegionAgent getAgent(Location loc) {
		checkAgents();
		if (loc != null) {
			for (RegionAgent obj : getAgentList()) {
				if (loc.equals(obj.getLocation()))
					return obj;
			}
		}
		return null;
	}

	public boolean removeAgent(RegionAgent agent) {
		boolean ret = false;
		if(agent != null) {
			agent.destroyAgent(false);
			getAgentList().remove(agent);
			ret = true;
		}
		return ret;
	}
	
	public int removeAgentsFromRegion(ProtectedRegion region) {
		int count = 0;
		if(region != null) {
			Iterator<RegionAgent> itr = getAgentList().iterator(); 
			while(itr.hasNext()) {
				RegionAgent obj = itr.next(); 
				if(obj.getProtectedRegion() == region) {
					obj.destroyAgent(false);
					itr.remove();
					count++;
				}
			}
		}
		return count;
	}

	public ArrayList<RegionAgent> getAgentList() {
		return agents;
	}

	public ProtectedRegion getRegion(Location loc) {
		if (loc != null) {
			Vector vec = new Vector(loc.getX(), loc.getY(), loc.getZ());
			ArrayList<ProtectedRegion> regions = new ArrayList<ProtectedRegion>();
			int highestPrior = 0;
			for(ProtectedRegion region: SimpleRegionMarket.getWorldGuard().getRegionManager(loc.getWorld()).getApplicableRegions(vec)) {
				regions.add(region);
				if(region.getPriority() > highestPrior) {
					highestPrior = region.getPriority();
				}
			}
			
			if(regions.size() == 1) {
				return regions.get(0);
			}
		}
		return null;
	}
	
	public double getRegionPrice(ProtectedRegion region, Player p) {
		if (region != null) {
			ArrayList<Double> prices = new ArrayList<Double>();
			for (RegionAgent obj : getAgentList()) {
				if (obj.getProtectedRegion() == region) {
					prices.add(obj.getPrice());
				}
			}
			if (prices.size() > 0) {
				double old = prices.get(0);
				for (int i = 0; i < prices.size(); i++) {
					if (prices.get(i) != old) {
						if(p != null) {
							SimpleRegionMarket.outputError(p, "There were found some signs, which don't have the same price.");
							SimpleRegionMarket.outputError(p, "Region " + region.getId() + ", price " + SimpleRegionMarket.getEconomicManager().format(old) + " and " + SimpleRegionMarket.getEconomicManager().format(prices.get(i)));
						}
						return -1;
					}
				}
				return old;
			}
		}
		return -1;
	}

	public boolean isOwner(Player player, ProtectedRegion region) {
		if (region != null) {
			if (player != null)
				return region.getOwners().contains(SimpleRegionMarket.getWorldGuard().wrapPlayer(player));
		}
		return false;
	}
}