package com.Tuong.OverCraftCore;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Wool;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.Tuong.Arena.Arena;
import com.Tuong.Arena.ArenaManager;
import com.Tuong.Database.Database;
import com.Tuong.Heros.Genji;
import com.Tuong.Heros.Hanzo;
import com.Tuong.Heros.Lucio;
import com.Tuong.Heros.Mei;
import com.Tuong.Heros.Roadhog;
import com.Tuong.Heros.Soldier76;
import com.Tuong.Heros.Tracer;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;
import net.minecraft.server.v1_10_R1.AxisAlignedBB;
import net.minecraft.server.v1_10_R1.EnumParticle;
import net.minecraft.server.v1_10_R1.IChatBaseComponent;
import net.minecraft.server.v1_10_R1.PacketPlayOutCustomSoundEffect;
import net.minecraft.server.v1_10_R1.PacketPlayOutTitle;
import net.minecraft.server.v1_10_R1.PacketPlayOutWorldParticles;
import net.minecraft.server.v1_10_R1.PlayerConnection;
import net.minecraft.server.v1_10_R1.SoundCategory;

public class Core extends JavaPlugin implements Listener{
	public static Plugin plugin;
	public static ArenaManager arenaManager;
	public static boolean t;
	public static int maxsecond,maxpoint; 
	public static final String prefix = ChatColor.WHITE+"["+ChatColor.GRAY+""+ChatColor.BOLD+"Over"+ChatColor.GOLD+""+ChatColor.BOLD+"Craft"+ChatColor.WHITE+"] ";
	public static Database database;
	public static ArrayList<Player> kick;
	private String[] instructor = {"Now, set blue team spawn by left click the block",
			"Then set lower region of blue team spawn",
			"And set upper region of blue team spawn",
			"Then set red team spawn",
			"And set lower region of red team spawn",
			"And set upper region of red team spawn",
			"Then set lobby location where players will teleport there while waiting for players",
			"And set return point where players will teleport after the game end",
			"Then, set lower arena region",
			"And set upper arena region",
			"Set lower region for capture point 1"};
	public static Economy econ;
	public void onEnable(){
		getConfig().options().copyDefaults(true);
		getConfig().addDefault("Vault", false);
		getConfig().addDefault("RequestTexture", true);
		getConfig().addDefault("BungeeCord", false);
		getConfig().addDefault("FallBackServer", "lobby");
		getConfig().addDefault("Database", false);
		getConfig().addDefault("Username", "user");
		getConfig().addDefault("Password", "pass");
		getConfig().addDefault("DatabaseName", "yournamehere");
		getConfig().addDefault("Sound.Enable", true);
		getConfig().addDefault("DefaultSecondsToCapture", 300);
		getConfig().addDefault("DefaultPointsToCapture", 200);
		getConfig().addDefault("DefaultCountDownSeconds", 30);
		getConfig().addDefault("DefaultCoins", 300);
		getConfig().addDefault("CoinsWin", 30);
		getConfig().addDefault("CoinsLose", 10);
		getConfig().addDefault("Message.1", "&cYou are recovering from the death!");
		getConfig().addDefault("Message.2", "&7You joined team &9BLUE");
		getConfig().addDefault("Message.3", "&7You joined team &cRED");
		getConfig().addDefault("Message.4", "&cYou can't join that team");
		getConfig().addDefault("Message.5", "&cPlayer &6%PLAYER% &cleft the game");
		getConfig().addDefault("Message.6", "&aJoin arena &6%ARENA% &aon &cRED &ateam");
		getConfig().addDefault("Message.7", "&aJoin arena &6%ARENA% &aon &9BLUE &ateam");
		getConfig().addDefault("Message.8", "&aCount down started....");
		getConfig().addDefault("Message.9", "&cCount down stop because not enough players");
		getConfig().addDefault("Message.10", "&aGame about to start in &6%TIME% &a second(s)");
		getConfig().addDefault("Message.11", "&6!Lets the game started!");
		getConfig().addDefault("Message.12", "&aTeleported to arena.");
		getConfig().addDefault("Message.13", "&aYou won the game on arena &6%ARENA%");
		getConfig().addDefault("Message.14", "&7You lost the game on arena &6%ARENA%");
		getConfig().addDefault("Message.15", "&6You won");
		getConfig().addDefault("Message.16", "&7You lost");
		getConfig().addDefault("Message.17", "&9Defend objective");
		getConfig().addDefault("Message.18", "&cAttack objective");
		getConfig().addDefault("Message.19", "&7Leave arena &6%ARENA%");
		getConfig().addDefault("Message.20", "&cYou denied resource pack download which you may not have full experience of the game");
		getConfig().addDefault("Message.21", "&aDownloading resource pack...");
		getConfig().addDefault("Message.22", "&6Downloading resource pack done!");
		getConfig().addDefault("Message.23", "&cDownloading resource pack failed!");
		getConfig().addDefault("Message.24", "&7&n&lHow to use hero's abilities?");
		getConfig().addDefault("Message.25", "&aPASSIVE &7| &fSome heroes don't have it. No key required to press it will active when you at some special certain stage.");
		getConfig().addDefault("Message.26", "&aLEFT_SHIFT &7| &fToggle/Use shift skill");
		getConfig().addDefault("Message.27", "&aE_KEY &7| &fToggle/Use E skill");
		getConfig().addDefault("Message.28", "&aQ_KEY &7| &fUse your ultimate when the exp bar charge is full");
		getConfig().addDefault("Message.29", "&aF_KEY &7| &fReload your weapon");
		getConfig().addDefault("Message.30", "&cYou must be in your spawn area to do that");
		getConfig().addDefault("Message.31", "&cYou're not in an arena or arena not started yet");
		getConfig().addDefault("Message.32", "&cYou don't have permission to use &d%CLASS% class");
		saveConfig();
		t = getConfig().getBoolean("Sound.Enable");
		maxsecond = getConfig().getInt("DefaultSecondsToCapture");
		maxpoint = getConfig().getInt("DefaultPointsToCapture");
		plugin = this;
		arenaManager = new ArenaManager();
		Bukkit.getPluginManager().registerEvents(this, this);
		if(getConfig().contains("Arena")) for(String st : getConfig().getConfigurationSection("Arena").getKeys(false)){
			String name = st;
			int min = getConfig().getInt("Arena."+st+".minPlayers"),max = getConfig().getInt("Arena."+st+".maxPlayers"),cap = getConfig().getInt("Arena."+st+".capturePoints");
			int[] numberic = {min,max,cap};
			Location[] locationInfo = new Location[cap*2+10];
			for(int i = 0; i < cap*2+10; i++){
				Location loc = new Location(Bukkit.getWorld(getConfig().getString("Arena."+st+"."+i+".World")),getConfig().getDouble("Arena."+st+"."+i+".x"),getConfig().getDouble("Arena."+st+"."+i+".y"),getConfig().getDouble("Arena."+st+"."+i+".z"));
				locationInfo[i] = loc;
			}
			arenaManager.createArena(name, locationInfo, numberic);
		}
		saveConfig();
		if(getConfig().getBoolean("Database")) {
			database = new Database(getConfig().getString("Username"), getConfig().getString("Password"), getConfig().getString("DatabaseName"));
			if(database.connected) database.createTable("overcraft", "UUID varchar(255), RANK INTEGER, WIN INTEGER, RANK_WIN INTEGER, KILLS INTEGER, KILL_STREAK INTEGER, COIN INTEGER, HERO varchar(255)");
		}
		if(getConfig().getBoolean("BungeeCord")) {
			Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
			new BukkitRunnable() {
				@Override
				public void run() {
					//Run queue
					for(Player player : kick) if(!player.isOnline()) kick.remove(player);
					if(kick.size() > 0){
						ByteArrayDataOutput out = ByteStreams.newDataOutput();
						out.writeUTF("Connect");
						out.writeUTF(plugin.getConfig().getString("FallBackServer"));
						kick.get(0).sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
					}
					for(Player player : Bukkit.getOnlinePlayers()) if(arenaManager.inArena(player) == null) {
						
					}
				}
			}.runTaskTimer(Core.plugin, 0, 10);
		}
		if(Core.plugin.getConfig().getBoolean("Vault")) setupEconomy();
	}
	private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
    
	public static void playSound(Player p, String sound){
		PacketPlayOutCustomSoundEffect packet = new PacketPlayOutCustomSoundEffect(sound, SoundCategory.VOICE, p.getEyeLocation().getBlockX(), p.getEyeLocation().getBlockY(), p.getEyeLocation().getBlockZ(), 1, 1);
		((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
	}
	@EventHandler
	public void signCreate(SignChangeEvent e)
	{
	    if (ChatColor.stripColor(e.getLine(0)).toLowerCase().replace(" ", "").equals("[overcraft]"))
	    {
	    	if(!e.getPlayer().hasPermission("oc.admin")) {
	    		e.getPlayer().sendMessage(prefix+ChatColor.RED+"You don't have permission to create sign");
	    		e.setCancelled(true);
	    		return;
	    	}
	        e.setLine(0, prefix);
	        e.setLine(1, ChatColor.RED+e.getLine(1));
	        e.setLine(2, ChatColor.BLUE+""+ChatColor.BOLD+e.getLine(2));
	        e.getBlock().getState().update();
	    }
	}
	public void onDisable(){
		for(Arena arena : arenaManager.getArrayList()) {
			arena.refresh();
			//save
			if(getConfig().contains("Arena."+arena.getArenaName())) continue;
			getConfig().set("Arena."+arena.getArenaName()+".minPlayers", arena.getNumbericInfo()[0]);
			getConfig().set("Arena."+arena.getArenaName()+".maxPlayers", arena.getNumbericInfo()[1]);
			getConfig().set("Arena."+arena.getArenaName()+".capturePoints", arena.getNumbericInfo()[2]);
			for(int i = 0; i < arena.getLocationInfo().length; i++){
				getConfig().set("Arena."+arena.getArenaName()+"."+i+".World", arena.getLocationInfo()[i].getWorld().getName());
				getConfig().set("Arena."+arena.getArenaName()+"."+i+".x", arena.getLocationInfo()[i].getX());
				getConfig().set("Arena."+arena.getArenaName()+"."+i+".y", arena.getLocationInfo()[i].getY());
				getConfig().set("Arena."+arena.getArenaName()+"."+i+".z", arena.getLocationInfo()[i].getZ());
			}
		}
		saveConfig();
		if(database != null)database.close();
	}
	@EventHandler
	  public void onInventoryClick(InventoryClickEvent event)
	  {
	    Inventory inv = event.getInventory();
	    if (!inv.getTitle().equals("Select Hero")) {
	      return;
	    }
	    if (!(event.getWhoClicked() instanceof Player)) {
	      return;
	    }
	    Player player = (Player)event.getWhoClicked();
	    ItemStack item = event.getCurrentItem();
	    if (item.getType() == Material.SUGAR)
	    {
	      player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 5.0F, 10.0F);
	      player.chat("/oc class genji");
	    }
	    if (item.getType() == Material.PRISMARINE_CRYSTALS)
	    {
	      player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 5.0F, 10.0F);
	      player.chat("/oc class soldier76");
	    }
	    if (item.getType() == Material.GOLD_NUGGET)
	    {
	      player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 5.0F, 10.0F);
	      player.chat("/oc class tracer");
	    }
	    if (item.getType() == Material.PRISMARINE_SHARD)
	    {
	      player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 5.0F, 10.0F);
	      player.chat("/oc class hanzo");
	    }
	    if (item.getType() == Material.CLAY_BALL)
	    {
	      player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 5.0F, 10.0F);
	      player.chat("/oc class mei");
	    }
	    if (item.getType() == Material.CHORUS_FRUIT)
	    {
	      player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 5.0F, 10.0F);
	      player.chat("/oc class roadhog");
	    }
	    if (item.getType() == Material.QUARTZ)
	    {
	      player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 5.0F, 10.0F);
	      player.chat("/oc class lucio");
	    }
	    event.setCancelled(true);
	    player.closeInventory();
	}
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(cmd.getName().equalsIgnoreCase("oc")){
			if(sender instanceof Player){
				Player p = (Player)sender;
				if(p.hasPermission("oc.admin")){
					if(args.length > 0){
						if(args[0].equals("reload")) {
							reloadConfig();
							p.sendMessage(prefix+ChatColor.GREEN+"Reload config - Plugin created by Tuong");
							return false;
						}if(args[0].equals("wand")) {
							p.getInventory().addItem(getWand());
							p.sendMessage(prefix+ChatColor.GREEN+instructor[0]);
							return false;
						}if(args[0].equals("remove") && args.length >= 2) {
							Arena arena = arenaManager.getArenaByName(args[1].toString());
							if(arena != null) {
								arena.refresh();
								p.sendMessage(prefix+ChatColor.GREEN+" Remove arena "+ChatColor.GOLD+arena.getArenaName()+ChatColor.GREEN+" done!");
								getConfig().set("Arena."+arena.getArenaName(), null);
								saveConfig();
								arenaManager.removeArena(arena);
							}else p.sendMessage(prefix+ChatColor.GREEN+" Can't find arena "+ChatColor.GOLD+args[1]+ChatColor.GREEN+"!");
							return false;
						}else if(args[0].equals("create") && args.length >= 5){
							if(p.getInventory().getItemInMainHand().hasItemMeta() && p.getInventory().getItemInMainHand().getItemMeta().hasDisplayName() && p.getInventory().getItemInMainHand().getItemMeta().getDisplayName().equals(getWand().getItemMeta().getDisplayName()) && p.getInventory().getItemInMainHand().getItemMeta().hasLore()){
								int[] numbericInfo = {Integer.valueOf(args[2].toString()),Integer.valueOf(args[3].toString()),Integer.valueOf(args[4].toString())};
								Location[] locationInfo = new Location[Integer.valueOf(args[4].toString())*2+10];
								int i = -2;
								for(String st : p.getInventory().getItemInMainHand().getItemMeta().getLore()){
									i++;
									if(i < 0) continue;
									String[] s = ChatColor.stripColor(st).split(" ");
									locationInfo[i] = new Location(Bukkit.getWorld(s[3]),Integer.valueOf(s[0]),Integer.valueOf(s[1]),Integer.valueOf(s[2]));
								}
								if(arenaManager.createArena(args[1].toString(), locationInfo, numbericInfo)) p.sendMessage(prefix+ChatColor.GREEN+"Create arena "+ChatColor.GOLD+args[1].toString() + ChatColor.GREEN+" done!");
								else p.sendMessage(prefix+ChatColor.RED+"Create arena "+ChatColor.GOLD+args[1].toString()+ChatColor.RED+" failed!");
							}else p.sendMessage(prefix+ChatColor.RED+"You need to hold the wand /oc wand to get a wand and instruction");
							return false;
						}else if(!args[0].equals("list") && !args[0].equals("leave") && !args[0].equals("autojoin")  && !args[0].equals("join") && !args[0].equals("class") && !args[0].equals("hero")) {
							p.sendMessage(prefix+ChatColor.RED+"Command not found use /oc to get all the command");
							return false;
						}
					}else{
						p.sendMessage(ChatColor.BLUE+""+ChatColor.BOLD+"==========="+ChatColor.WHITE+""+ChatColor.BOLD+"Over"+ChatColor.GOLD+""+ChatColor.BOLD+"Craft"+ChatColor.BLUE+""+ChatColor.BOLD+"===========");
						p.sendMessage(ChatColor.YELLOW+""+ChatColor.BOLD+"> "+ChatColor.GRAY+"/oc "+ChatColor.GOLD+"create "+ChatColor.WHITE+" <arena_name> <min_player_to_start> <max_player> <number_of_capture_points>" +ChatColor.GRAY+" - This command require a wand with a set of locations to get the wand use command below and permission "+ChatColor.LIGHT_PURPLE +"oc.admin");
						p.sendMessage(ChatColor.YELLOW+""+ChatColor.BOLD+"> "+ChatColor.GRAY+"/oc "+ChatColor.GOLD+"wand"+ChatColor.GRAY+" - Give you a wand with instruction, left click block to save location and right click block to remove location, require"+ChatColor.LIGHT_PURPLE+" oc.admin"+ChatColor.GRAY+" permission");
						p.sendMessage(ChatColor.YELLOW+""+ChatColor.BOLD+"> "+ChatColor.GRAY+"/oc "+ChatColor.GOLD+"remove "+ChatColor.WHITE+" <arena_name_to_remove>"+ChatColor.GRAY+" - Remove an arena require"+ChatColor.LIGHT_PURPLE+" oc.admin"+ChatColor.GRAY+" permission");
						p.sendMessage(ChatColor.YELLOW+""+ChatColor.BOLD+"> "+ChatColor.GRAY+"/oc "+ChatColor.GOLD+"list"+ChatColor.GRAY+" - Open a list of arenas require permission"+ChatColor.LIGHT_PURPLE+" oc.list");
						p.sendMessage(ChatColor.YELLOW+""+ChatColor.BOLD+"> "+ChatColor.GRAY+"/oc "+ChatColor.GOLD+"autojoin"+ChatColor.GRAY+" - Auto join an arena best use for sorting player require"+ChatColor.LIGHT_PURPLE+" oc.autojoin"+ChatColor.GRAY+" permission");
						p.sendMessage(ChatColor.YELLOW+""+ChatColor.BOLD+"> "+ChatColor.GRAY+"/oc "+ChatColor.GOLD+"join "+ChatColor.WHITE+"<arene_name>"+ChatColor.GRAY+" - Join arena require"+ChatColor.LIGHT_PURPLE+" oc.player"+ChatColor.GRAY+" permission");
						p.sendMessage(ChatColor.YELLOW+""+ChatColor.BOLD+"> "+ChatColor.GRAY+"/oc "+ChatColor.GOLD+"leave"+ChatColor.GRAY+" - Leave arena require"+ChatColor.LIGHT_PURPLE+" oc.player"+ChatColor.GRAY+" permission");
						p.sendMessage(ChatColor.YELLOW+""+ChatColor.BOLD+"> "+ChatColor.GRAY+"/oc "+ChatColor.GOLD+"class "+ChatColor.WHITE+"<class_name>"+ChatColor.GRAY+" - Choose hero class require"+ChatColor.LIGHT_PURPLE+" oc.player"+ChatColor.GRAY+" permission");
						p.sendMessage(ChatColor.YELLOW+""+ChatColor.BOLD+"> "+ChatColor.GRAY+"/oc "+ChatColor.GOLD+"hero "+ChatColor.GRAY+" - Open hero GUI require"+ChatColor.LIGHT_PURPLE+" oc.player"+ChatColor.GRAY+" permission");
						p.sendMessage(ChatColor.BLUE+""+ChatColor.BOLD+"===============================");
						return false;
					}
				}if(args.length >= 1) if(p.hasPermission("oc.list") && args[0].equals("list")){
						p.sendMessage(prefix+ChatColor.GRAY+"Arenas list:");
						for(Arena arena : arenaManager.getArrayList()) p.sendMessage(ChatColor.GRAY+"+ "+ChatColor.GOLD+arena.getArenaName() +ChatColor.GREEN+ " (Min players: "+ChatColor.RED+arena.getNumbericInfo()[0] +ChatColor.GREEN+", Max players: "+ChatColor.RED+arena.getNumbericInfo()[1] + ChatColor.GREEN+", Number of capture points: "+ChatColor.RED+arena.getNumbericInfo()[2]+ChatColor.GREEN+")");
					}else if(args[0].equals("hero") && p.hasPermission("oc.player")){
						Arena arena = arenaManager.inArena(p);
						if(arena != null && arena.started() == true){
							if(arena.getTeam().get(p).equals("BLUE") && arena.spawnBlueRegion.contains(p.getLocation()) || arena.getTeam().get(p).equals("RED") && arena.spawnRedRegion.contains(p.getLocation())){
						Inventory inv = Bukkit.createInventory(null, 9, "Select Hero");
					    
					    ItemStack genji = nameItem(new ItemStack(Material.SUGAR), ChatColor.BOLD+ "" + ChatColor.GRAY + "Genji");
					    inv.setItem(1, genji);
					    
					    ItemStack soldier76 = nameItem(new ItemStack(Material.PRISMARINE_CRYSTALS), ChatColor.BOLD+"" + ChatColor.GRAY + "Soldier: 76");
					    inv.setItem(2, soldier76);
					    
					    ItemStack tracer = nameItem(new ItemStack(Material.GOLD_NUGGET), ChatColor.BOLD +""+ ChatColor.GRAY + "Tracer");
					    inv.setItem(3, tracer);
					    
					    ItemStack hanzo = nameItem(new ItemStack(Material.PRISMARINE_SHARD), ChatColor.BOLD +""+ ChatColor.GRAY + "Hanzo");
					    inv.setItem(4, hanzo);
					    
					    ItemStack mei = nameItem(new ItemStack(Material.CLAY_BALL), ChatColor.BOLD +""+ ChatColor.GRAY + "Mei");
					    inv.setItem(5, mei);
					    
					    ItemStack roadhog = nameItem(new ItemStack(Material.CHORUS_FRUIT), ChatColor.BOLD +""+ ChatColor.GRAY + "Roadhog");
					    inv.setItem(6, roadhog);
					    
					    ItemStack lucio = nameItem(new ItemStack(Material.QUARTZ), ChatColor.BOLD +""+ ChatColor.GRAY + "Lucio");
					    inv.setItem(7, lucio);
					    
					    p.openInventory(inv);
							}else p.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', getConfig().getString("Message.30")));
						}else p.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', getConfig().getString("Message.31")));
					}else if(args[0].equals("leave") && p.hasPermission("oc.player")){
						Arena arena = arenaManager.inArena(p);
						if(arena != null){
							arena.playerLeave(p);
						}else p.sendMessage(prefix+ChatColor.RED+"You're not in any arena");
					}else if(args[0].equals("class") && p.hasPermission("oc.player")){
						if(args.length >= 2){
							Arena arena = arenaManager.inArena(p);
							if(arena != null && arena.started() == true){
								if(arena.getTeam().get(p).equals("BLUE") && arena.spawnBlueRegion.contains(p.getLocation()) || arena.getTeam().get(p).equals("RED") && arena.spawnRedRegion.contains(p.getLocation())){
								switch(args[1].toLowerCase()){
								case "genji":
									if(!p.hasPermission("oc.class."+args[1].toLowerCase())) {
										if(!p.hasPermission("oc.class.all")){
											p.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', getConfig().getString("Message.32")).replace("%CLASS%",ChatColor.LIGHT_PURPLE+args[1].toLowerCase()+ChatColor.RED));
											break;
										}
									}
									arena.classRemove(p);
									arena.playerList.remove(p);
									arena.playerList.put(p, new Genji(p, arena));
									break;
								case "hanzo":
									if(!p.hasPermission("oc.class."+args[1].toLowerCase())) {
										if(!p.hasPermission("oc.class.all")){
											p.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', getConfig().getString("Message.32")).replace("%CLASS%",ChatColor.LIGHT_PURPLE+args[1].toLowerCase()+ChatColor.RED));
											break;
										}
									}
									arena.classRemove(p);
									arena.playerList.remove(p);
									arena.playerList.put(p, new Hanzo(p, arena));
									break;
								case "mei":
									if(!p.hasPermission("oc.class."+args[1].toLowerCase())) {
										if(!p.hasPermission("oc.class.all")){
											p.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', getConfig().getString("Message.32")).replace("%CLASS%",ChatColor.LIGHT_PURPLE+args[1].toLowerCase()+ChatColor.RED));
											break;
										}
									}
									arena.classRemove(p);
									arena.playerList.remove(p);
									arena.playerList.put(p, new Mei(p, arena));
									break;
								case "tracer":
									if(!p.hasPermission("oc.class."+args[1].toLowerCase())) {
										if(!p.hasPermission("oc.class.all")){
											p.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', getConfig().getString("Message.32")).replace("%CLASS%",ChatColor.LIGHT_PURPLE+args[1].toLowerCase()+ChatColor.RED));
											break;
										}
									}
									arena.classRemove(p);
									arena.playerList.remove(p);
									arena.playerList.put(p, new Tracer(p, arena));
									break;
								case "roadhog":
									if(!p.hasPermission("oc.class."+args[1].toLowerCase())) {
										if(!p.hasPermission("oc.class.all")){
											p.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', getConfig().getString("Message.32")).replace("%CLASS%",ChatColor.LIGHT_PURPLE+args[1].toLowerCase()+ChatColor.RED));
											break;
										}
									}
									arena.classRemove(p);
									arena.playerList.remove(p);
									arena.playerList.put(p, new Roadhog(p, arena));
									break;
								case "lucio":
									if(!p.hasPermission("oc.class."+args[1].toLowerCase())) {
										if(!p.hasPermission("oc.class.all")){
											p.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', getConfig().getString("Message.32")).replace("%CLASS%",ChatColor.LIGHT_PURPLE+args[1].toLowerCase()+ChatColor.RED));
											break;
										}
									}
									arena.classRemove(p);
									arena.playerList.remove(p);
									arena.playerList.put(p, new Lucio(p, arena));
									break;
								case "soldier76":
									if(!p.hasPermission("oc.class."+args[1].toLowerCase())) {
										if(!p.hasPermission("oc.class.all")){
											p.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', getConfig().getString("Message.32")).replace("%CLASS%",ChatColor.LIGHT_PURPLE+args[1].toLowerCase()+ChatColor.RED));
											break;
										}
									}
									arena.classRemove(p);
									arena.playerList.remove(p);
									arena.playerList.put(p, new Soldier76(p, arena));
									break;
								default:
									p.sendMessage(prefix+ChatColor.RED+"Invalid hero");
									break;
								}
								arena.update(p, 1);
								}else p.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', getConfig().getString("Message.30")));
							}else p.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', getConfig().getString("Message.31")));
						}else p.sendMessage(prefix+ChatColor.RED+"Use /oc class <hero>");
					}else if(args[0].equals("autojoin") && p.hasPermission("oc.autojoin")){
						if(!getConfig().getBoolean("BungeeCord")){
						Arena an = null;
						int curplayer = 0;
						if(arenaManager.getArrayList().size() >= 1 && !arenaManager.getArrayList().get(0).started() && arenaManager.getArrayList().get(0).getNumbericInfo()[1] > arenaManager.getArrayList().get(0).getPlayerList().size()) {
							an = arenaManager.getArrayList().get(0);
							curplayer = an.getPlayerList().keySet().size();
						}
						for(Arena arena : arenaManager.getArrayList()) if(!arena.started() && arena.getNumbericInfo()[1] > arena.getPlayerList().size()) {
							if(arena.getPlayerList().keySet().size() > curplayer){
								an = arena;
								curplayer = an.getPlayerList().keySet().size();
							}
						}
						if(an == null) p.sendMessage(prefix + ChatColor.RED+"No arena left to join");
						else an.playerJoin(p);
						}else p.sendMessage(prefix + ChatColor.RED + "You in the leave queue, when you leave join game again");
					}else if(args[0].equals("join") && p.hasPermission("oc.player")){
						if(!getConfig().getBoolean("BungeeCord")){
						if(args.length >= 2){
							String arenaName = args[1].toString();
							Arena arena = arenaManager.getArenaByName(arenaName);
							if(arena != null && arena.getNumbericInfo()[1] > arena.getPlayerList().size() && !arena.started()) arena.playerJoin(p);
							else p.sendMessage(prefix+ChatColor.RED+"Arena not found or full or ingame");
						}else p.sendMessage(prefix+ChatColor.RED+"Wrong! Use /oc join <arenaName>");
						}else p.sendMessage(prefix + ChatColor.RED + "You in the leave queue, when you leave join game again");
					}else p.sendMessage(prefix+ChatColor.RED+"Invalid command or don't have permission for this");
			}else getLogger().info("You can just use this command in-game");
		}
	    return false;
	}
	@EventHandler
	public void texturePack(PlayerResourcePackStatusEvent e){
			if(e.getStatus() == Status.DECLINED) sendMessage(e.getPlayer(), 20);
			if(e.getStatus() == Status.ACCEPTED) sendMessage(e.getPlayer(), 21);
			if(e.getStatus() == Status.SUCCESSFULLY_LOADED) sendMessage(e.getPlayer(), 22);
			if(e.getStatus() == Status.FAILED_DOWNLOAD) sendMessage(e.getPlayer(), 23);
		
	}
	public void sendMessage(Player p, int num){
		p.sendMessage(Core.prefix+ChatColor.translateAlternateColorCodes('&', Core.plugin.getConfig().getString("Message."+num).replace("%PLAYER%", p.getName())));
	}
	@EventHandler
	public void join(PlayerJoinEvent e){
		if(getConfig().getBoolean("RequestTexture")) e.getPlayer().setResourcePack("https://drive.google.com/uc?export=download&id=0Bwjo6ZpU7OWTQ1pxV24xdmpBSXM");
		if(e.getPlayer().hasPermission("oc.admin") && getConfig().getBoolean("Database") && database != null && !database.connected) e.getPlayer().sendMessage(prefix + ChatColor.RED+ "Database is down, check it now!!!!");
		if(getConfig().getBoolean("BungeeCord")){
			e.getPlayer().setGameMode(GameMode.ADVENTURE);
			if(arenaManager.getArrayList().size() > 0){
				Arena arena = arenaManager.getArrayList().get(0);
				e.getPlayer().teleport(arena.getLocationInfo()[6]);
				if(!arena.started() && arena.getNumbericInfo()[1] > arena.getPlayerList().size()){
					arena.playerJoin(e.getPlayer());
				}else sendBackServer(e.getPlayer());
			}else sendBackServer(e.getPlayer());
		}      
		if(getConfig().getBoolean("Database") && database != null && database.connected){
			database.reward(e.getPlayer());
		}
	}
	@EventHandler
	public void leave(PlayerQuitEvent e){
		if(kick.contains(e.getPlayer())) kick.remove(e.getPlayer());
	}
	
	public static void sendBackServer(Player p){
		kick.add(p);
	}
	
	private ItemStack nameItem(ItemStack item, String name)
	  {
	    ItemMeta meta = item.getItemMeta();
	    meta.setDisplayName(name);
	    item.setItemMeta(meta);
	    return item;
	  }
	public static void sendTitle(Player player, Integer fadeIn, Integer stay, Integer fadeOut, String title, String subtitle)
    {
        PlayerConnection connection = ((CraftPlayer)player).getHandle().playerConnection;
     
        PacketPlayOutTitle packetPlayOutTimes = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TIMES, null, fadeIn.intValue(), stay.intValue(), fadeOut.intValue());
        connection.sendPacket(packetPlayOutTimes);
        if (subtitle != null)
        {
            subtitle = ChatColor.translateAlternateColorCodes('&', subtitle);
            IChatBaseComponent titleSub = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + subtitle + "\"}");
            PacketPlayOutTitle packetPlayOutSubTitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, titleSub);
            connection.sendPacket(packetPlayOutSubTitle);
        }
        if (title != null)
        {
            title = ChatColor.translateAlternateColorCodes('&', title);
            IChatBaseComponent titleMain = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + title + "\"}");
            PacketPlayOutTitle packetPlayOutTitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, titleMain);
            connection.sendPacket(packetPlayOutTitle);
        }
    }
	public static Entity getNearestEntityInSight(Player player, int range) {
	    List<Entity> entities = player.getNearbyEntities(range, range, range); //Get the entities within range
	    Iterator<Entity> iterator = entities.iterator(); //Create an iterator
	    while (iterator.hasNext()) {
	        Entity next = iterator.next(); //Get the next entity in the iterator
	        if (!(next instanceof LivingEntity) || next == player) { //If the entity is not a living entity or the player itself, remove it from the list
	            iterator.remove();
	        } 
	    }
		List<Block> sight = player.getLineOfSight((Set<Material>) null, range); //Get the blocks in the player's line of sight (the Set is null to not ignore any blocks)
	    for (Block block : sight) { //For each block in the list
	        if (block.getType() != Material.AIR) { //If the block is not air -> obstruction reached, exit loop/seach
	            break;
	        }
	        Location low = block.getLocation(); //Lower corner of the block
	        Location high = low.clone().add(1, 1, 1); //Higher corner of the block
	        AxisAlignedBB blockBoundingBox = new AxisAlignedBB(low.getX(), low.getY(), low.getZ(), high.getX(), high.getY(), high.getZ()); //The bounding or collision box of the block
	        for (Entity entity : entities) { //For every living entity in the player's range
	            //If the entity is truly close enough and the bounding box of the block (1x1x1 box) intersects with the entity's bounding box, return it
	            if (entity.getLocation().distance(player.getEyeLocation()) <= range && ((CraftEntity) entity).getHandle().getBoundingBox().b(blockBoundingBox)) {
	                return entity;
	            }
	        }
	    }
	    return null; //Return null/nothing if no entity was found
	}
	@EventHandler
	public void useWand(PlayerInteractEvent e){
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (e.getClickedBlock().getType() == Material.WALL_SIGN || e.getClickedBlock().getType() == Material.SIGN_POST) {
				Sign s = (Sign) e.getClickedBlock().getState();
				if (ChatColor.stripColor(s.getLine(0)).equalsIgnoreCase(ChatColor.stripColor(prefix))){
					switch(ChatColor.stripColor(s.getLine(1)).toLowerCase()){
					case "join":
						if(s.getLine(2) == null) return;
						e.getPlayer().chat("/oc join "+ChatColor.stripColor(s.getLine(2)));
						break;
					case "class":
						if(s.getLine(2) == null) return;
						e.getPlayer().chat("/oc class "+ChatColor.stripColor(s.getLine(2)).toLowerCase());
						break;
					case "autojoin":
						e.getPlayer().chat("/oc autojoin");
						break;
					case "leave":
						e.getPlayer().chat("/oc leave");
						break;
					case "hero":
						e.getPlayer().chat("/oc hero");
					}
				}
			}
		}
		if(e.getPlayer().getInventory().getItemInMainHand().hasItemMeta() && e.getPlayer().getInventory().getItemInMainHand().getItemMeta().hasDisplayName() && e.getPlayer().getInventory().getItemInMainHand().getItemMeta().getDisplayName().equals(getWand().getItemMeta().getDisplayName()) && e.getPlayer().getInventory().getItemInMainHand().getItemMeta().hasLore()){
		if(e.getPlayer().isOp()){
				if(e.getAction() == Action.LEFT_CLICK_BLOCK) {
					ItemMeta mt = e.getPlayer().getInventory().getItemInMainHand().getItemMeta();
					List<String> lore = mt.getLore();
					if(lore.size() <= 10) e.getPlayer().sendMessage(prefix+ChatColor.GREEN+instructor[lore.size()]);
					else if((lore.size() - 10) % 2 == 0){
						if(((lore.size()-10)/2+1) > 1) e.getPlayer().sendMessage(prefix+ChatColor.GREEN+"Set lower region for capture point "+((lore.size()-10)/2+1)+". If "+((lore.size()-10)/2)+" capture point(s) is enough for you please use command /oc create {your_arena_name} {min_players} {max_players} "+((lore.size()-10)/2));
						else e.getPlayer().sendMessage(prefix+ChatColor.GREEN+"Set lower region for capture point "+((lore.size()-8)/2+1));
					}else {
						e.getPlayer().sendMessage(prefix+ChatColor.GREEN+"Set upper region for capture point "+((lore.size()-10)/2+1));
					}
					lore.add(ChatColor.GOLD+""+e.getClickedBlock().getX() + " " + e.getClickedBlock().getY() + " " + e.getClickedBlock().getZ() + " " + e.getClickedBlock().getWorld().getName());
					mt.setLore(lore);
					e.getPlayer().getInventory().getItemInMainHand().setItemMeta(mt);
				} else if((e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) && e.getPlayer().getInventory().getItemInMainHand().getItemMeta().getLore().size() > 1) {
					ItemMeta mt = e.getPlayer().getInventory().getItemInMainHand().getItemMeta();
					List<String> lore = mt.getLore();
					lore.remove(lore.size()-1);
					mt.setLore(lore);
					e.getPlayer().getInventory().getItemInMainHand().setItemMeta(mt);
					if(lore.size() <= 10) e.getPlayer().sendMessage(prefix+ChatColor.GREEN+instructor[lore.size()-1]);
					else if((lore.size() - 10) % 2 == 0){
						if(((lore.size()-10+1)/2) > 1) e.getPlayer().sendMessage(prefix+ChatColor.GREEN+"Set lower region for capture point "+((lore.size()-10)/2+1)+". If "+((lore.size()-10)/2)+" capture point(s) is enough for you please use command /oc create {your_arena_name} {min_players} {max_players} "+((lore.size()-10)/2));
						else e.getPlayer().sendMessage(prefix+ChatColor.GREEN+"Set lower region for capture point "+((lore.size()-10)/2+1));
					}else {
						e.getPlayer().sendMessage(prefix+ChatColor.GREEN+"Set upper region for capture point "+((lore.size()-10)/2));
					}
				}
				e.setCancelled(true);
			}else {
				e.getPlayer().getInventory().setItemInMainHand(null);
			}
		}
	}
	
	public ItemStack getWand(){
		ItemStack item = new ItemStack(Material.BLAZE_ROD);
		ItemMeta mt = item.getItemMeta();
		mt.setDisplayName(ChatColor.DARK_PURPLE+""+ChatColor.BOLD+"OCWand");
		mt.setLore(Arrays.asList(ChatColor.GREEN+"Left click to remove location, right click on block to add location"));
		item.setItemMeta(mt);
		return item;
	}
	public static ItemStack getTeamCompass(){
		ItemStack item = new ItemStack(Material.COMPASS);
		ItemMeta mt = item.getItemMeta();
		mt.setDisplayName(ChatColor.GREEN+""+ChatColor.BOLD+"Click to choose team");
		item.setItemMeta(mt);
		return item;
	}
	public static Inventory getTeamInventory(){
		Inventory inv = Bukkit.createInventory(null, 9, ChatColor.DARK_GREEN+"Choose team");
		Wool blue = new Wool(DyeColor.BLUE);
		ItemStack item = blue.toItemStack();
		ItemMeta mt = item.getItemMeta();
		mt.setDisplayName(ChatColor.BLUE+"Blue team");
		mt.setLore(Arrays.asList(ChatColor.GREEN+"Click to choose team blue"));
		item.setItemMeta(mt);
		inv.setItem(3, item);
		blue = new Wool(DyeColor.RED);
		item = blue.toItemStack();
		mt = item.getItemMeta();
		mt.setDisplayName(ChatColor.RED+"Red team");
		mt.setLore(Arrays.asList(ChatColor.GREEN+"Click to choose team blue"));
		item.setItemMeta(mt);
		inv.setItem(5, item);
		return inv;
	}
	
	public static Vector getRandomVelocity(Boolean bool) {
	double i = 0.3;
	if(bool) i = 0;
	Random random = new Random();
	final double power = 0.5D;
	double rix = random.nextBoolean() ? -power : power;
	double riz = random.nextBoolean() ? -power : power;
	double x = random.nextBoolean() ? (rix * (1D + (random.nextInt(30) / 50)))
			: 0.0D;
		double y =  i + (random.nextInt(10) / 45D);
		double z = random.nextBoolean() ? (riz * (1D + (random.nextInt(30) / 50)))
			: 0.0D;
		Vector velocity = new Vector(x, y, z);
	 
		return velocity;
	}
	public static void pullEntityToLocation(Entity e, Location loc)
	{
	    Location entityLoc = e.getLocation();
	   
	    entityLoc.setY(entityLoc.getY() + 0.5D);
	    e.teleport(entityLoc);
	   
	    double g = -0.08D;
	    double d = loc.distance(entityLoc);
	    double t = d;
	 
	    double v_x = (1.0D + 0.07000000000000001D * t) * (loc.getX() - entityLoc.getX()) / t;
	    double v_y = (1.0D + 0.03D * t) * (loc.getY() - entityLoc.getY()) / t - 0.5D * g * t;
	    double v_z = (1.0D + 0.07000000000000001D * t) * (loc.getZ() - entityLoc.getZ()) / t;
	   
	    Vector v = e.getVelocity();
	 
	    v.setX(v_x);
	    v.setY(v_y);
	    v.setZ(v_z);
	 
	    e.setVelocity(v);
	}
	public static void playParticle(EnumParticle type,Location l,int number){
		PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(type,true,(float)l.getX(),(float)l.getY(),(float)l.getZ(),0,0,0,number,0);
		for(Player p : l.getWorld().getPlayers()) ((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet);
	}
}
