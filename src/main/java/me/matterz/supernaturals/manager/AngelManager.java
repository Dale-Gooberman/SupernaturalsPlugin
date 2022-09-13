package me.matterz.supernaturals.manager;

import me.matterz.supernaturals.SuperNPlayer;
import me.matterz.supernaturals.SupernaturalsPlugin;
import me.matterz.supernaturals.io.SNConfigHandler;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Boat;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class AngelManager extends ClassManager {

	public AngelManager() {
		super();
	}

	@Override
	public double damagerEvent(EntityDamageByEntityEvent event, double damage) {
		Player player = (Player) event.getEntity();
		SuperNPlayer snplayer = SuperNManager.get(player);
		if (event.getEntity() instanceof Animals) {
			if (player.getInventory().getItemInMainHand().getType().equals(Material.DIAMOND_SWORD)) {
				SuperNManager.sendMessage(snplayer, "Angels cannot use diamond swords on animals!");
				event.setCancelled(true);
				return 0;
			}
		}
		return damage;
	}

	@Override
	public void spellEvent(EntityDamageByEntityEvent event, Player target) {
		Player player = (Player) event.getDamager();
		SuperNPlayer snplayer = SuperNManager.get(player);
		SuperNPlayer sntarget = SuperNManager.get(target);

		boolean cancelled = false;

		if (player.getInventory().getItemInMainHand().getType().equals(Material.FEATHER)) {
			if (snplayer.getPower() > SNConfigHandler.angelHealPowerCost) {
				target.setHealth(target.getHealth()
						+ SNConfigHandler.angelHealHealthGain);
				SuperNManager.alterPower(snplayer, -SNConfigHandler.angelHealPowerCost, "Healing "
						+ target.getName());
				target.sendMessage(ChatColor.RED + "Healed by "
						+ player.getName());
			} else {
				SuperNManager.sendMessage(snplayer, "Not enough power to heal!");
			}
		}
		if (player.getInventory().getItemInMainHand().getType().equals(Material.PAPER)) {
			if (snplayer.getPower() > SNConfigHandler.angelCurePowerCost) {
				if (sntarget.isSuper()) {
					SuperNManager.cure(sntarget);
					target.sendMessage(ChatColor.RED + "Cured by "
							+ player.getName());
					SuperNManager.alterPower(snplayer, -SNConfigHandler.angelCurePowerCost, "Cured "
							+ target.getName());
				} else {
					SuperNManager.sendMessage(snplayer, "Player is not a supernatural!");
				}
			} else {
				SuperNManager.sendMessage(snplayer, "Not enough power!");
			}
		}
		if (player.getInventory().getItemInMainHand().toString().equalsIgnoreCase(SNConfigHandler.priestSpellGuardianAngel)) {
			if (SNConfigHandler.debugMode) {
				SupernaturalsPlugin.log(snplayer.getName()
						+ " is attempting to cast guardian angel...");
			}
			if (SNConfigHandler.debugMode) {
				SupernaturalsPlugin.log(target.getName()
						+ " is targeted by guardian angel.");
			}
			cancelled = guardianAngel(player, target);
			if (!event.isCancelled()) {
				event.setCancelled(cancelled);
			}
			return;
		}
	}

	@Override
	public boolean playerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Action action = event.getAction();
		Material itemInHandMaterial = player.getInventory().getItemInMainHand().getType();
		Material itemInOffHandMaterial = player.getInventory().getItemInOffHand().getType();
		SuperNPlayer snplayer = SuperNManager.get(player);
		if (action.equals(Action.LEFT_CLICK_AIR)
				|| action.equals(Action.LEFT_CLICK_BLOCK)) {
			if (itemInHandMaterial.equals(Material.DANDELION)) {
				if (snplayer.getPower() > SNConfigHandler.angelJumpPowerCost) {
					jump(player, SNConfigHandler.angelJumpDeltaSpeed);
				} else {
					SuperNManager.sendMessage(snplayer, "Not enough power to jump!");
				}
			}
		}
		if (action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK)) {
			if (itemInOffHandMaterial.equals(Material.FEATHER)) {
				if (snplayer.getPower() > SNConfigHandler.angelHealPowerCost) {
					player.setHealth(player.getHealth()
						+ SNConfigHandler.angelHealHealthGain);
					SuperNManager.alterPower(snplayer, -SNConfigHandler.angelHealPowerCost, "You heal your wounds!");
				} else {
				SuperNManager.sendMessage(snplayer, "Not enough power to heal!");
				}
			}
		}
		if (action.equals(Action.LEFT_CLICK_BLOCK)) {
			Block targetBlock = player.getTargetBlock((Set<Material>) null, 20);
			Location targetBlockLocation = targetBlock.getLocation();
			ItemStack item = player.getInventory().getItemInMainHand();
			if (itemInHandMaterial.equals(Material.BEEF)
					|| itemInHandMaterial.equals(Material.BONE)
					|| itemInHandMaterial.equals(Material.PORKCHOP)) {

				if (snplayer.getPower() > SNConfigHandler.angelSummonPowerCost) {
					if (itemInHandMaterial.equals(Material.BEEF)) {
						player.getWorld().spawnEntity(targetBlockLocation, EntityType.COW);
						event.setCancelled(true);
						SuperNManager.alterPower(snplayer, -SNConfigHandler.angelSummonPowerCost, "Summoned cow.");
						if (item.getAmount() == 1) {
							player.getInventory().setItemInMainHand(null);
						} else {
							item.setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
						}
						return true;
					}
					else if (itemInHandMaterial.equals(Material.BONE)) {
						Wolf spawnedWolf = (Wolf) player.getWorld().spawnEntity(targetBlockLocation, EntityType.WOLF);
						spawnedWolf.setTamed(true);
						spawnedWolf.setOwner(player);
						spawnedWolf.setHealth(20);
						event.setCancelled(true);
						SuperNManager.alterPower(snplayer, -SNConfigHandler.angelSummonPowerCost, "Summoned wolf.");
						if (item.getAmount() == 1) {
							player.getInventory().setItemInMainHand(null);
						} else {
							item.setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
						}
						return true;
					}
					else {
						player.getWorld().spawnEntity(targetBlockLocation, EntityType.PIG);
						event.setCancelled(true);
						SuperNManager.alterPower(snplayer, -SNConfigHandler.angelSummonPowerCost, "Summoned pig.");
						if (item.getAmount() == 1) {
							player.getInventory().setItemInMainHand(null);
						} else {
							item.setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
						}
						return true;
					}

				}
			}

		}
		return false;
	}

	@Override
	public double victimEvent(EntityDamageEvent event, double damage) {
		if (event.getCause().equals(DamageCause.FALL)) {
			event.setCancelled(true);
			return 0;
		}
		return damage;
	}

	public static boolean jump(Player player, double deltaSpeed) {
		SuperNPlayer snplayer = SuperNManager.get(player);
		if (snplayer.getPower() < SNConfigHandler.angelJumpPowerCost) {
			SuperNManager.sendMessage(snplayer, "Not enough Power to jump.");
			return false;
		} else {
			SuperNManager.alterPower(snplayer, -SNConfigHandler.angelJumpPowerCost, "SuperJump!");
			if (SNConfigHandler.debugMode) {
				SupernaturalsPlugin.log(snplayer.getName() + " used jump!");
			}
		}

		Vector vjadd;
		vjadd = new Vector(0, 1, 0);
		vjadd.multiply(deltaSpeed);

		player.setVelocity(player.getVelocity().add(vjadd));
		return true;
	}

	@Override
	public void armorCheck(Player player) {
		PlayerInventory inv = player.getInventory();
		ItemStack helmet = inv.getHelmet();
		ItemStack chest = inv.getChestplate();
		ItemStack leggings = inv.getLeggings();
		ItemStack boots = inv.getBoots();

		if (helmet != null) {
			if (helmet.getType().equals(Material.NETHERITE_HELMET)
					|| helmet.getType().equals(Material.GOLDEN_HELMET)) {
				inv.setHelmet(null);
				dropItem(player, helmet);
			}
		}
		if (chest != null) {
			if (chest.getType().equals(Material.NETHERITE_CHESTPLATE)
					|| chest.getType().equals(Material.GOLDEN_CHESTPLATE)) {
				inv.setChestplate(null);
				dropItem(player, chest);
			}
		}
		if (leggings != null) {
			if (leggings.getType().equals(Material.NETHERITE_LEGGINGS)
					|| leggings.getType().equals(Material.GOLDEN_LEGGINGS)) {
				inv.setLeggings(null);
				dropItem(player, leggings);
			}
		}
		if (boots != null) {
			if (boots.getType().equals(Material.NETHERITE_BOOTS)
					|| boots.getType().equals(Material.GOLDEN_BOOTS)) {
				inv.setBoots(null);
				dropItem(player, boots);
			}
		}
	}

	public void waterAdvanceTime(Player player) {
		if (player.isDead()) {
			return;
		}

		if (player.isInsideVehicle()) {
			if (player.getVehicle() instanceof Boat) {
				return;
			}
		}

		SuperNPlayer snplayer = SuperNManager.get(player);

		Material material = player.getLocation().getBlock().getType();

		if (material == Material.WATER) {
			SuperNManager.alterPower(snplayer, SNConfigHandler.angelSwimPowerGain, "Swimming in water");
		}
	}
	public boolean guardianAngel(Player player, Player victim) {
		SuperNPlayer angel = SuperNManager.get(player);
		SuperNPlayer snvictim = SuperNManager.get(victim);

		if (angel.getPower() > SNConfigHandler.priestPowerGuardianAngel) {
			if (!snvictim.isSuper()) {
				if (SupernaturalsPlugin.instance.getDataHandler().hasAngel(angel)) {
					SuperNManager.sendMessage(angel, "Removed Guardian Angel from "
							+ ChatColor.WHITE
							+ SupernaturalsPlugin.instance.getDataHandler().getAngelPlayer(angel).getName());
					SuperNManager.sendMessage(SupernaturalsPlugin.instance.getDataHandler().getAngelPlayer(angel), "Guardian Angel removed!");
					SupernaturalsPlugin.instance.getDataHandler().removeAngel(angel);
				}
				SuperNManager.sendMessage(snvictim, "You now have a Guardian Angel!");
				SuperNManager.alterPower(angel, -SNConfigHandler.priestPowerGuardianAngel, "Guardian Angel on "
						+ ChatColor.WHITE
						+ snvictim.getName()
						+ ChatColor.RED
						+ "!");
				SupernaturalsPlugin.instance.getDataHandler().addAngel(angel, snvictim);

				ItemStack item = player.getInventory().getItemInMainHand();
				if (item.getAmount() == 1) {
					player.getInventory().setItemInMainHand(null);
				} else {
					item.setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
				}
				return true;
			}
			SuperNManager.sendMessage(angel, "You cannot set a Guardian Angel on a Supernatural player.");
			return false;
		} else {
			SuperNManager.sendMessage(angel, "Not enough power to cast Guardian Angel.");
			return false;
		}
	}
}