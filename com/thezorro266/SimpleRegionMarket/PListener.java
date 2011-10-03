package com.thezorro266.SimpleRegionMarket;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

class PListener extends PlayerListener {

	@Override
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			Block b = event.getClickedBlock();
			if(b != null) {
				if ((b.getTypeId() == 63) || (b.getTypeId() == 68)) {
					Sign sign = (Sign) b.getState();
					if (sign.getLine(0).equals("[AGENT]")) {
						RegionAgent agent = SimpleRegionMarket.getAgentManager().getAgent(b.getLocation());
						
						if (agent == null)
							return;
						
						if(SimpleRegionMarket.getEconomicManager() != null) {
							Player p = event.getPlayer();
							if(AgentManager.MAX_REGIONS != 0) {
								int count = 0;
								count = SimpleRegionMarket.getWorldGuard().getRegionManager(b.getWorld()).getRegionCountOfPlayer(SimpleRegionMarket.getWorldGuard().wrapPlayer(p));
								if(count >= AgentManager.MAX_REGIONS && !SimpleRegionMarket.isAdmin(p)) {
									SimpleRegionMarket.outputError(p, "You have reached your limit of regions on this world.");
									return;
								}
							}
		
							ProtectedRegion region = SimpleRegionMarket.getWorldGuard().getRegionManager(b.getWorld()).getRegion(agent.getRegion());
							
							if(!SimpleRegionMarket.canBuy(p)) {
								SimpleRegionMarket.outputError(p, "You don't have the permission to buy a region.");
							} else if (SimpleRegionMarket.getAgentManager().isOwner(p, region)) {
								if(agent.getOwnerPlayer() != null) {
									if(p.equals(agent.getOwnerPlayer())) {
										SimpleRegionMarket.outputDebug(p, "This is your agent.");
									} else {
										SimpleRegionMarket.outputDebug(p, "You cannot buy this region, because it's yours.");
									}
								} else {
									SimpleRegionMarket.outputDebug(p, "You cannot buy this region, because it's yours.");
								}
							} else {
								String owner = agent.getOwner();
								if (!SimpleRegionMarket.getEconomicManager().hasAccount(p.getName())) {
									SimpleRegionMarket.getEconomicManager().createAccount(p.getName());
								}
								if (SimpleRegionMarket.getEconomicManager().hasAccount(p.getName())) {
									double price = agent.getPrice();
									if (SimpleRegionMarket.getEconomicManager().getAccount(p.getName()).hasEnough(price)) {
										if(owner.isEmpty()) {
											SimpleRegionMarket.getEconomicManager().getAccount(p.getName()).subtract(price);
											SimpleRegionMarket.sellRegion(region, p);
											SimpleRegionMarket.outputDebug(p, "You successfully buyed the region " + region.getId() + ".");
										} else {
											if (!SimpleRegionMarket.getEconomicManager().hasAccount(owner)) {
												SimpleRegionMarket.getEconomicManager().createAccount(owner);
											}
											if (SimpleRegionMarket.getEconomicManager().hasAccount(owner)) {
												SimpleRegionMarket.getEconomicManager().getAccount(p.getName()).subtract(price);
												SimpleRegionMarket.getEconomicManager().getAccount(owner).add(price);
												SimpleRegionMarket.sellRegion(region, p);
												SimpleRegionMarket.outputDebug(p, "You successfully buyed the region " + region.getId() + " from " + owner + ".");
											} else {
												SimpleRegionMarket.outputError(p, "There was a problem with transfering the money.");
												SimpleRegionMarket.outputConsole("Error: Couldn't create economy account '" + owner + "'.");
											}
										}
									} else {
										SimpleRegionMarket.outputError(p, SimpleRegionMarket.ERR_MONEY);
									}
								} else {
									SimpleRegionMarket.outputError(p, "There was a problem with transfering the money.");
									SimpleRegionMarket.outputConsole("Error: Couldn't create economy account '" + p.getName() + "'.");
								}
							}
						} else {
							if(event.getPlayer() != null) {
								SimpleRegionMarket.outputError(event.getPlayer(), "The economic system was not found, please tell the server owner");
							}
						}
					}
				}
			}
		}
	}
}