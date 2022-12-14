/*
 * Supernatural Players Plugin for Bukkit
 * Copyright (C) 2011  Matt Walker <mmw167@gmail.com>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package me.matterz.supernaturals;

import me.matterz.supernaturals.commands.*;
import me.matterz.supernaturals.io.*;
import me.matterz.supernaturals.listeners.*;
import me.matterz.supernaturals.manager.*;
import me.matterz.supernaturals.util.TextUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SupernaturalsPlugin extends JavaPlugin {
	public static SupernaturalsPlugin instance;

	private final SNConfigHandler snConfig = new SNConfigHandler(this);
	private SNDataHandler snData = new SNDataHandler();
	private SNWhitelistHandler snWhitelist = new SNWhitelistHandler(this);

	@SuppressWarnings("unused")
	private SNEntityListener entityListener;
	@SuppressWarnings("unused")
	private SNPlayerListener playerListener;
	@SuppressWarnings("unused")
	private SNPlayerMonitor playerMonitor;
	@SuppressWarnings("unused")
	private SNEntityMonitor entityMonitor;
	@SuppressWarnings("unused")
	private SNBlockListener blockListener;
	@SuppressWarnings("unused")
	private SNServerMonitor serverMonitor;

	private SuperNManager superManager = new SuperNManager(this);
	private HumanManager humanManager = new HumanManager(this);
	private VampireManager vampManager = new VampireManager();
	private PriestManager priestManager = new PriestManager();
	private WereManager wereManager = new WereManager();
	private GhoulManager ghoulManager = new GhoulManager();
	private HunterManager hunterManager = new HunterManager();
	private DemonManager demonManager = new DemonManager();
	private EnderBornManager enderManager = new EnderBornManager(this);
	private AngelManager angelManager = new AngelManager();

	public List<SNCommand> commands = new ArrayList<SNCommand>();

	private static File dataFolder;

	public static boolean foundPerms = false;

	public PluginManager pm;

	public SNDataHandler getDataHandler() {
		return snData;
	}

	// -------------------------------------------- //
	// Managers //
	// -------------------------------------------- //

	public SuperNManager getSuperManager() {
		return superManager;
	}

	public SNConfigHandler getConfigManager() {
		return snConfig;
	}

	public SNWhitelistHandler getWhitelistHandler() {
		return snWhitelist;
	}

	public VampireManager getVampireManager() {
		return vampManager;
	}

	public AngelManager getAngelManager() {
		return angelManager;
	}

	public PriestManager getPriestManager() {
		return priestManager;
	}

	public WereManager getWereManager() {
		return wereManager;
	}

	public GhoulManager getGhoulManager() {
		return ghoulManager;
	}

	public HunterManager getHunterManager() {
		return hunterManager;
	}

	public DemonManager getDemonManager() {
		return demonManager;
	}

	public ClassManager getClassManager(Player player) {
		SuperNPlayer snplayer = SuperNManager.get(player);
		if (snplayer.getType().equalsIgnoreCase("demon")) {
			return demonManager;
		} else if (snplayer.getType().equalsIgnoreCase("ghoul")) {
			return ghoulManager;
		} else if (snplayer.getType().equalsIgnoreCase("witchhunter")) {
			return hunterManager;
		} else if (snplayer.getType().equalsIgnoreCase("priest")) {
			return priestManager;
		} else if (snplayer.getType().equalsIgnoreCase("vampire")) {
			return vampManager;
		} else if (snplayer.getType().equalsIgnoreCase("werewolf")) {
			return wereManager;
		} else if (snplayer.getType().equalsIgnoreCase("enderborn")) {
			return enderManager;}
		else if (snplayer.getType().equalsIgnoreCase("angel")) {
			return angelManager;
		} else {
			return humanManager;
		}
	}

	// -------------------------------------------- //
	// Plugin Enable/Disable //
	// -------------------------------------------- //

	@Override
	public void onDisable() {
		SuperNManager.cancelTimer();
		snData.write();

		saveData();
		demonManager.removeAllWebs();
		PluginDescriptionFile pdfFile = getDescription();
		log(pdfFile.getName() + " version " + pdfFile.getVersion()
				+ " disabled.");

	}

	@Override
	public void onEnable() {

		SupernaturalsPlugin.instance = this;
		getDataFolder().mkdir();
		pm = getServer().getPluginManager();

		// Add the commands
		commands.add(new SNCommandHelp());
		commands.add(new SNCommandAdmin());
		commands.add(new SNCommandPower());
		commands.add(new SNCommandReload());
		commands.add(new SNCommandSave());
		commands.add(new SNCommandConvert());
		commands.add(new SNCommandCure());
		commands.add(new SNCommandList());
		commands.add(new SNCommandClasses());
		commands.add(new SNCommandSetChurch());
		commands.add(new SNCommandSetBanish());
		commands.add(new SNCommandReset());
		commands.add(new SNCommandKillList());
		commands.add(new SNCommandRmTarget());
		commands.add(new SNCommandRestartTask());
		commands.add(new SNCommandJoin());

		entityListener = new SNEntityListener(this);
		playerListener = new SNPlayerListener(this);
		playerMonitor = new SNPlayerMonitor(this);
		entityMonitor = new SNEntityMonitor(this);
		blockListener = new SNBlockListener(this);
		serverMonitor = new SNServerMonitor(this);

		PluginDescriptionFile pdfFile = getDescription();
		log(pdfFile.getName() + " version " + pdfFile.getVersion()
				+ " enabled.");

		if (!SNVersionHandler.versionFile.exists()) {
			SNVersionHandler.writeVersion();
		}

		dataFolder = getDataFolder();
		SNConfigHandler.getConfiguration();

		loadData();
		snData = SNDataHandler.read();

		SNWhitelistHandler.reloadWhitelist();

		if (snData == null) {
			snData = new SNDataHandler();
		}

		SuperNManager.startTimer();
		HunterManager.createBounties();
		setupPermissions();
	}

	// -------------------------------------------- //
	// Chat Commands //
	// -------------------------------------------- //

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (sender instanceof Player) {
			List<String> parameters = new ArrayList<String>(Arrays.asList(args));
			if (SNConfigHandler.debugMode) {
				SupernaturalsPlugin.log(((Player) sender).getName()
						+ " used command: " + commandLabel + " with args: "
						+ TextUtil.implode(parameters, ", "));
			}
			handleCommand(sender, parameters, true);
			return true;
		} else {
			List<String> parameters = new ArrayList<String>(Arrays.asList(args));
			if (SNConfigHandler.debugMode) {
				SupernaturalsPlugin.log(((Player) sender).getName()
						+ " used command: " + commandLabel + " with args: "
						+ TextUtil.implode(parameters, ", "));
			}
			handleCommand(sender, parameters, false);
			return true;
		}
	}

	public void handleCommand(CommandSender sender, List<String> parameters, boolean isPlayer) {
		if (parameters.size() == 0) {
			for (SNCommand vampcommand : commands) {
				if (vampcommand.getName().equalsIgnoreCase("help")) {
					vampcommand.execute(sender, parameters);
					return;
				}
			}
			sender.sendMessage(ChatColor.RED + "Unknown command. Try /sn help");
			return;
		}

		String command = parameters.get(0).toLowerCase();
		parameters.remove(0);

		for (SNCommand vampcommand : commands) {
			if (command.equals(vampcommand.getName())) {
				if (!isPlayer && vampcommand.senderMustBePlayer) {
					sender.sendMessage("This command, sn " + command
							+ ", is player-only");
				}
				vampcommand.execute(sender, parameters);
				return;
			}
		}

		sender.sendMessage(ChatColor.RED + "Unknown command \"" + command
				+ "\". Try /sn help");
	}

	// -------------------------------------------- //
	// Data Management //
	// -------------------------------------------- //

	public static void saveAll() {
		File file = new File(dataFolder, "data.yml");
		SNPlayerHandler.save(SuperNManager.getSupernaturals(), file);

		SNConfigHandler.saveConfig();
	}

	public static void saveData() {
		File file = new File(dataFolder, "data.yml");
		SNPlayerHandler.save(SuperNManager.getSupernaturals(), file);
	}

	public static void loadData() {
		File file = new File(dataFolder, "data.yml");
		SuperNManager.setSupernaturals(SNPlayerHandler.load(file));
	}

	public static void reConfig() {
		if (SNConfigHandler.debugMode) {
			SupernaturalsPlugin.log("Reloading config...");
		}
		SNConfigHandler.reloadConfig();
	}

	public static void reloadData() {
		File file = new File(dataFolder, "data.yml");
		SuperNManager.setSupernaturals(SNPlayerHandler.load(file));
	}

	public static void restartTask() {
		SuperNManager.cancelTimer();
		SuperNManager.startTimer();
	}

	// -------------------------------------------- //
	// Permissions //
	// -------------------------------------------- //

	private void setupPermissions() {
		if (pm.isPluginEnabled("PermissionsEx")) {
			log("Found PermissionsEx!");
			foundPerms = true;
		} else if (pm.isPluginEnabled("PermissionsBukkit")) {
			log("Found PermissionsBukkit!");
			foundPerms = true;
		} else if (pm.isPluginEnabled("bPermissions")) {
			log("Found bPermissions.");
			log(Level.WARNING, "If something goes wrong with bPermissions and this plugin, I will not help!");
			foundPerms = true;
		} else if (pm.isPluginEnabled("GroupManager")) {
			log("Found GroupManager.");
			foundPerms = true;
		}

		if (!foundPerms) {
			log("Permission system not detected, defaulting to SuperPerms");
			log("A permissions system may be detected later, just wait.");
		}
	}

	public static boolean hasPermissions(Player player, String permissions) {
		return player.hasPermission(permissions);
	}


	public boolean getPvP(Player player) {
		/*
		WorldGuardPlugin worldGuard = SupernaturalsPlugin.instance.getWorldGuard();
		if (worldGuard == null) {
			return true;
		}
		Vector pt = player.getLocation().toVector();
		RegionManager regionManager = worldGuard.getRegionManager(player.getWorld());
		ApplicableRegionSet set = regionManager.getApplicableRegions(pt);
		*/
		return true;
	}

	public boolean getSpawn(Player player) {
		/*
		WorldGuardPlugin worldGuard = SupernaturalsPlugin.instance.getWorldGuard();
		if (worldGuard == null) {
			return true;
		}
		Vector pt = player.getLocation().toVector();
		RegionManager regionManager = worldGuard.getRegionManager(player.getWorld());
		ApplicableRegionSet set = regionManager.getApplicableRegions(pt);
		*/
		return true;
	}

	// -------------------------------------------- //
	// Logging //
	// -------------------------------------------- //

	public static void log(String msg) {
		log(Level.INFO, msg);
	}

	public static void log(Level level, String msg) {
		Logger.getLogger("Minecraft").log(level, "["
				+ instance.getDescription().getFullName() + "] " + msg);
	}

}
