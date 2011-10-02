package com.thezorro266.SimpleRegionMarket;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;

import com.iCo6.iConomy;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

class BListener extends BlockListener {

	@Override
	public void onBlockBreak(BlockBreakEvent event) {
		Block b = event.getBlock();
		if (b.getTypeId() == 63 || b.getTypeId() == 68) {
			Sign sign = (Sign) b.getState();
			if (sign.getLine(0).equalsIgnoreCase("[AGENT]")) {
				RegionAgent agent = SimpleRegionMarket.getAgentManager().getAgent(b.getLocation());

				if (agent == null)
					return;

				Player p = event.getPlayer();
				ProtectedRegion region = SimpleRegionMarket.getWorldGuard().getRegionManager(b.getLocation().getWorld()).getRegion(agent.getRegion());
				if (!SimpleRegionMarket.getAgentManager().isOwner(p, region) && !SimpleRegionMarket.isAdmin(p)) {
					event.setCancelled(true);
					sign.update();
					return;
				}

				event.setCancelled(true);
				if (p != null) {
					agent.destroyAgent(true);
					SimpleRegionMarket.outputDebug(event.getPlayer(), "Successfully deleted the agent.");
				}
				SimpleRegionMarket.getAgentManager().removeAgent(agent);
				SimpleRegionMarket.saveAll();
			}
		}
	}

	@Override
	public void onSignChange(SignChangeEvent event) {
		if (event.getLine(0).equalsIgnoreCase("[AGENT]")) {
			ProtectedRegion region;
			Location signloc = event.getBlock().getLocation();
			
			RegionAgent oldagent = SimpleRegionMarket.getAgentManager().getAgent(signloc);
			if(oldagent != null) {
				SimpleRegionMarket.getAgentManager().getAgentList().remove(oldagent);
			}

			if (event.getLine(1).isEmpty()) {
				region = SimpleRegionMarket.getAgentManager().getRegion(signloc);
			} else {
				region = SimpleRegionMarket.getWorldGuard().getRegionManager(signloc.getWorld()).getRegion(event.getLine(1));
			}

			Player p = event.getPlayer();

			if (p != null) {
				if(!SimpleRegionMarket.canSell(p)) {
					event.setCancelled(true);
					event.getBlock().setType(Material.AIR);
					signloc.getWorld().dropItem(signloc, new ItemStack(Material.SIGN, 1));
					return;
				}
			}
			if (region == null) {
				if (p != null) {
					SimpleRegionMarket.outputError(p, SimpleRegionMarket.ERR_NAME);
				}
				event.setCancelled(true);
				event.getBlock().setType(Material.AIR);
				signloc.getWorld().dropItem(signloc, new ItemStack(Material.SIGN, 1));
				return;
			}

			if (p != null) {
				if (!SimpleRegionMarket.getAgentManager().isOwner(p, region) && !SimpleRegionMarket.isAdmin(p)) {
					SimpleRegionMarket.outputError(p, SimpleRegionMarket.ERR_NOOWN);
					event.setCancelled(true);
					event.getBlock().setType(Material.AIR);
					signloc.getWorld().dropItem(signloc, new ItemStack(Material.SIGN, 1));
					return;
				}
			}

			double price = 0;
			if (event.getLine(2).isEmpty()) {
				if (SimpleRegionMarket.getAgentManager().countAgents(region) > 0) {
					price = SimpleRegionMarket.getAgentManager().getRegionPrice(region, null);
				} else {
					price = -1;
				}
				if (price < 0) {
					if (p != null) {
						SimpleRegionMarket.outputError(p, "Price not found.");
					}
					event.setCancelled(true);
					event.getBlock().setType(Material.AIR);
					signloc.getWorld().dropItem(signloc, new ItemStack(Material.SIGN, 1));
					return;
				}
			} else {
				price = Double.parseDouble(event.getLine(2));
				try {
					if (price < 0) {
						if (p != null) {
							SimpleRegionMarket.outputError(p, "The price can't be under zero.");
						}
						event.setCancelled(true);
						event.getBlock().setType(Material.AIR);
						signloc.getWorld().dropItem(signloc, new ItemStack(Material.SIGN, 1));
						return;
					}
				} catch (Exception e) {
					if (p != null) {
						SimpleRegionMarket.outputError(p, "Price not found.");
					}
					event.setCancelled(true);
					event.getBlock().setType(Material.AIR);
					signloc.getWorld().dropItem(signloc, new ItemStack(Material.SIGN, 1));
					return;
				}
			}
			
			Player lp = p;
			if (p != null) {
				if (!SimpleRegionMarket.getAgentManager().isOwner(p, region) || event.getLine(3).equalsIgnoreCase("none")) {
					if(SimpleRegionMarket.isAdmin(p)) {
						lp = null;
					}
				}
			}

			if (SimpleRegionMarket.getAgentManager().addAgent(region, signloc, lp, price)) {
				if (p != null) {
					if(lp == null) {
						SimpleRegionMarket.outputDebug(p, "You offer the region for sale by the server.");
					} else {
						SimpleRegionMarket.outputDebug(p, "You offer your region for sale.");
					}
					SimpleRegionMarket.getAgentManager().getRegionPrice(region, p);
					if (SimpleRegionMarket.getAgentManager().countAgents(region) > 1) {
						SimpleRegionMarket.outputDebug(p, "You've got now " + Integer.toString(SimpleRegionMarket.getAgentManager().countAgents(region)) + " agent(s) placed for this region.");
					}
				}
				event.setLine(0, "[AGENT]");
				event.setLine(1, region.getId());
				event.setLine(2, iConomy.format(price));
				int rightX = (int) region.getMaximumPoint().getX() - (int) (region.getMinimumPoint().getX() - 1);
				if (rightX < 0) {
					rightX *= -1;
				}
				int rightY = (int) region.getMaximumPoint().getY() - (int) (region.getMinimumPoint().getY() - 1);
				if (rightY < 0) {
					rightY *= -1;
				}
				int rightZ = (int) region.getMaximumPoint().getZ() - (int) (region.getMinimumPoint().getZ() - 1);
				if (rightZ < 0) {
					rightZ *= -1;
				}
				event.setLine(3, Integer.toString(rightX) + " x " + Integer.toString(rightY) + " x " + Integer.toString(rightZ));
				SimpleRegionMarket.saveAll();
			} else {
				if (p != null) {
					SimpleRegionMarket.outputError(p, "The agent couldn't be created.");
				}
				event.setCancelled(true);
				event.getBlock().setType(Material.AIR);
				signloc.getWorld().dropItem(signloc, new ItemStack(Material.SIGN, 1));
				return;
			}
		}
	}
}