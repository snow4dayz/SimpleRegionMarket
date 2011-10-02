package com.thezorro266.SimpleRegionMarket;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;

import com.iCo6.system.Accounts;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

class PListener extends PlayerListener {

	@Override
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			Block b = event.getClickedBlock();
			if ((b.getTypeId() == 63) || (b.getTypeId() == 68)) {
				Sign sign = (Sign) b.getState();
				if (sign.getLine(0).equals("[AGENT]")) {
					RegionAgent agent = SimpleRegionMarket.getAgentManager().getAgent(b.getLocation());

					if (agent == null)
						return;

					Player p = event.getPlayer();
					ProtectedRegion region = SimpleRegionMarket.getWorldGuard().getRegionManager(b.getLocation().getWorld()).getRegion(agent.getRegion());
					
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
						Accounts acc = new Accounts();
						if (!acc.exists(p.getName())) {
							acc.create(p.getName());
						}
						if (acc.exists(p.getName())) {
							double price = agent.getPrice();
							if (acc.get(p.getName()).getHoldings().hasEnough(price)) {
								if(owner.isEmpty()) {
									acc.get(p.getName()).getHoldings().subtract(price);
									SimpleRegionMarket.sellRegion(region, p);
									SimpleRegionMarket.outputDebug(p, "You successfully buyed the region " + region.getId() + ".");
								} else {
									if (!acc.exists(owner)) {
										acc.create(owner);
									}
									if (acc.exists(owner)) {
										acc.get(p.getName()).getHoldings().subtract(price);
										acc.get(owner).getHoldings().add(price);
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
				}
			}
		}
	}
}