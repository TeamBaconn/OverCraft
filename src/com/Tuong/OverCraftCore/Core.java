package com.Tuong.OverCraftCore;


import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Wool;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import com.Tuong.Arena.Arena;
import com.Tuong.Arena.ArenaManager;
import com.Tuong.Heros.Genji;
import com.Tuong.Heros.Hanzo;
import com.Tuong.Heros.Lucio;
import com.Tuong.Heros.Mei;
import com.Tuong.Heros.Roadhog;
import com.Tuong.Heros.Tracer;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_9_R2.AxisAlignedBB;
import net.minecraft.server.v1_9_R2.EnumParticle;
import net.minecraft.server.v1_9_R2.IChatBaseComponent;
import net.minecraft.server.v1_9_R2.PacketPlayOutTitle;
import net.minecraft.server.v1_9_R2.PacketPlayOutWorldParticles;
import net.minecraft.server.v1_9_R2.PlayerConnection;

public class Core extends JavaPlugin implements Listener{
	public static Plugin plugin;
	public static ArenaManager arenaManager;
	public static boolean t;
	public static int maxsecond,maxpoint; 
	public static final String prefix = ChatColor.WHITE+"["+ChatColor.GRAY+""+ChatColor.BOLD+"Over"+ChatColor.GOLD+""+ChatColor.BOLD+"Craft"+ChatColor.WHITE+"] ";
	private String[] instructor = {"Now, set blue team spawn by left click the block",
			"Then set lower regoin of blue team spawn",
			"And set upper regoin of blue team spawn",
			"Then set red team spawn",
			"And set lower regoin of red team spawn",
			"And set upper regoin of red team spawn",
			"Then set lobby location where players will teleport there while waiting for players",
			"And set return point where players will teleport after the game end",
			"Set lower region for capture point 1"};
	public void onEnable(){
		getConfig().options().copyDefaults(true);
		getConfig().addDefault("Sound.Enable", true);
		getConfig().addDefault("DefaultSecondsToCapture", 300);
		getConfig().addDefault("DefaultPointsToCapture", 200);
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
			Location[] locationInfo = new Location[cap*2+8];
			for(int i = 0; i < cap*2+8; i++){
				Location loc = new Location(Bukkit.getWorld(getConfig().getString("Arena."+st+"."+i+".World")),getConfig().getDouble("Arena."+st+"."+i+".x"),getConfig().getDouble("Arena."+st+"."+i+".y"),getConfig().getDouble("Arena."+st+"."+i+".z"));
				locationInfo[i] = loc;
			}
			arenaManager.createArena(name, locationInfo, numberic);
		}
		saveConfig();
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
								Location[] locationInfo = new Location[Integer.valueOf(args[4].toString())*2+8];
								int i = -2;
								for(String st : p.getInventory().getItemInMainHand().getItemMeta().getLore()){
									i++;
									if(i < 0) continue;
									String[] s = ChatColor.stripColor(st).split(" ");
									locationInfo[i] = new Location(p.getWorld(),Integer.valueOf(s[0]),Integer.valueOf(s[1]),Integer.valueOf(s[2]));
								}
								if(arenaManager.createArena(args[1].toString(), locationInfo, numbericInfo)) p.sendMessage(prefix+ChatColor.GREEN+"Create arena "+ChatColor.GOLD+args[1].toString() + ChatColor.GREEN+" done!");
								else p.sendMessage(prefix+ChatColor.RED+"Create arena "+ChatColor.GOLD+args[1].toString()+ChatColor.RED+" failed!");
							}else p.sendMessage(prefix+ChatColor.RED+"You need to hold the wand /oc wand to get a wand and instruction");
							return false;
						}else if(!args[0].equals("list") && !args[0].equals("leave") && !args[0].equals("autojoin")  && !args[0].equals("join") && !args[0].equals("class")) {
							p.sendMessage(prefix+ChatColor.RED+"Command not found use /oc to get all the command");
							return false;
						}
					}else{
						p.sendMessage(ChatColor.BLUE+""+ChatColor.BOLD+"�����������"+ChatColor.WHITE+""+ChatColor.BOLD+"Over"+ChatColor.GOLD+""+ChatColor.BOLD+"Craft"+ChatColor.BLUE+""+ChatColor.BOLD+"�����������");
						p.sendMessage(ChatColor.YELLOW+""+ChatColor.BOLD+"� "+ChatColor.GRAY+"/oc "+ChatColor.GOLD+"create "+ChatColor.WHITE+" <arena_name> <min_player_to_start> <max_player> <number_of_capture_points>" +ChatColor.GRAY+" - This command require a wand with a set of locations to get the wand use command below and permission "+ChatColor.LIGHT_PURPLE +"oc.admin");
						p.sendMessage(ChatColor.YELLOW+""+ChatColor.BOLD+"� "+ChatColor.GRAY+"/oc "+ChatColor.GOLD+"wand"+ChatColor.GRAY+" - Give you a wand with instruction, left click block to save location and right click block to remove location, require"+ChatColor.LIGHT_PURPLE+" oc.admin"+ChatColor.GRAY+" permission");
						p.sendMessage(ChatColor.YELLOW+""+ChatColor.BOLD+"� "+ChatColor.GRAY+"/oc "+ChatColor.GOLD+"remove "+ChatColor.WHITE+" <arena_name_to_remove>"+ChatColor.GRAY+" - Remove an arena require"+ChatColor.LIGHT_PURPLE+" oc.admin"+ChatColor.GRAY+" permission");
						p.sendMessage(ChatColor.YELLOW+""+ChatColor.BOLD+"� "+ChatColor.GRAY+"/oc "+ChatColor.GOLD+"list"+ChatColor.GRAY+" - Open a list of arenas require permission"+ChatColor.LIGHT_PURPLE+" oc.list");
						p.sendMessage(ChatColor.YELLOW+""+ChatColor.BOLD+"� "+ChatColor.GRAY+"/oc "+ChatColor.GOLD+"autojoin"+ChatColor.GRAY+" - Auto join an arena best use for sorting player require"+ChatColor.LIGHT_PURPLE+" oc.autojoin"+ChatColor.GRAY+" permission");
						p.sendMessage(ChatColor.YELLOW+""+ChatColor.BOLD+"� "+ChatColor.GRAY+"/oc "+ChatColor.GOLD+"join "+ChatColor.WHITE+"<arene_name>"+ChatColor.GRAY+" - Join arena require"+ChatColor.LIGHT_PURPLE+" oc.player"+ChatColor.GRAY+" permission");
						p.sendMessage(ChatColor.YELLOW+""+ChatColor.BOLD+"� "+ChatColor.GRAY+"/oc "+ChatColor.GOLD+"leave"+ChatColor.GRAY+" - Leave arena require"+ChatColor.LIGHT_PURPLE+" oc.player"+ChatColor.GRAY+" permission");
						p.sendMessage(ChatColor.YELLOW+""+ChatColor.BOLD+"� "+ChatColor.GRAY+"/oc "+ChatColor.GOLD+"class "+ChatColor.WHITE+"<class_name>"+ChatColor.GRAY+" - Choose hero class require"+ChatColor.LIGHT_PURPLE+" oc.player"+ChatColor.GRAY+" permission");
						p.sendMessage(ChatColor.BLUE+""+ChatColor.BOLD+"�������������������������������");
						return false;
					}
				}if(args.length >= 1) if(p.hasPermission("oc.list") && args[0].equals("list")){
						p.sendMessage(prefix+ChatColor.GRAY+"Arenas list:");
						for(Arena arena : arenaManager.getArrayList()) p.sendMessage(ChatColor.GRAY+"+ "+ChatColor.GOLD+arena.getArenaName() +ChatColor.GREEN+ " (Min players: "+ChatColor.RED+arena.getNumbericInfo()[0] +ChatColor.GREEN+", Max players: "+ChatColor.RED+arena.getNumbericInfo()[1] + ChatColor.GREEN+", Number of capture points: "+ChatColor.RED+arena.getNumbericInfo()[2]+ChatColor.GREEN+")");
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
											p.sendMessage(prefix + ChatColor.RED+"You don't have permission to use "+ChatColor.LIGHT_PURPLE+args[1].toLowerCase()+ChatColor.RED+" class");
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
											p.sendMessage(prefix + ChatColor.RED+"You don't have permission to use "+ChatColor.LIGHT_PURPLE+args[1].toLowerCase()+ChatColor.RED+" class");
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
											p.sendMessage(prefix + ChatColor.RED+"You don't have permission to use "+ChatColor.LIGHT_PURPLE+args[1].toLowerCase()+ChatColor.RED+" class");
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
											p.sendMessage(prefix + ChatColor.RED+"You don't have permission to use "+ChatColor.LIGHT_PURPLE+args[1].toLowerCase()+ChatColor.RED+" class");
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
											p.sendMessage(prefix + ChatColor.RED+"You don't have permission to use "+ChatColor.LIGHT_PURPLE+args[1].toLowerCase()+ChatColor.RED+" class");
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
											p.sendMessage(prefix + ChatColor.RED+"You don't have permission to use "+ChatColor.LIGHT_PURPLE+args[1].toLowerCase()+ChatColor.RED+" class");
											break;
										}
									}
									arena.classRemove(p);
									arena.playerList.remove(p);
									arena.playerList.put(p, new Lucio(p, arena));
									break;
								default:
									p.sendMessage(prefix+ChatColor.RED+"Invalid hero");
									break;
								}
								arena.update(p, 1);
								}else p.sendMessage(prefix+ChatColor.RED+"You must be in your spawn area to do that");
							}else p.sendMessage(prefix+ChatColor.RED+"You're not in an arena or arena not started yet");
						}else p.sendMessage(prefix+ChatColor.RED+"Use /oc class <hero>");
					}else if(args[0].equals("autojoin") && p.hasPermission("oc.autojoin")){
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
					}else if(args[0].equals("join") && p.hasPermission("oc.player")){
						if(args.length >= 2){
							String arenaName = args[1].toString();
							Arena arena = arenaManager.getArenaByName(arenaName);
							if(arena != null && arena.getNumbericInfo()[1] > arena.getPlayerList().size() && !arena.started()) arena.playerJoin(p);
							else p.sendMessage(prefix+ChatColor.RED+"Arena not found or full or ingame");
						}else p.sendMessage(prefix+ChatColor.RED+"Wrong! Use /oc join <arenaName>");
					}else p.sendMessage(prefix+ChatColor.RED+"Invalid command or don't have permission for this");
			}else getLogger().info("You can just use this command in-game");
		}
	    return false;
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
					}
				}
			}
		}
		if(e.getPlayer().getInventory().getItemInMainHand().hasItemMeta() && e.getPlayer().getInventory().getItemInMainHand().getItemMeta().hasDisplayName() && e.getPlayer().getInventory().getItemInMainHand().getItemMeta().getDisplayName().equals(getWand().getItemMeta().getDisplayName()) && e.getPlayer().getInventory().getItemInMainHand().getItemMeta().hasLore()){
		if(e.getPlayer().isOp()){
				if(e.getAction() == Action.LEFT_CLICK_BLOCK) {
					ItemMeta mt = e.getPlayer().getInventory().getItemInMainHand().getItemMeta();
					List<String> lore = mt.getLore();
					if(lore.size() <= 8) e.getPlayer().sendMessage(prefix+ChatColor.GREEN+instructor[lore.size()]);
					else if((lore.size() - 8) % 2 == 0){
						if(((lore.size()-8)/2+1) > 1) e.getPlayer().sendMessage(prefix+ChatColor.GREEN+"Set lower region for capture point "+((lore.size()-8)/2+1)+". If "+((lore.size()-8)/2)+" capture point(s) is enough for you please use command /oc create {your_arena_name} {min_players} {max_players} "+((lore.size()-8)/2));
						else e.getPlayer().sendMessage(prefix+ChatColor.GREEN+"Set lower region for capture point "+((lore.size()-8)/2+1));
					}else {
						e.getPlayer().sendMessage(prefix+ChatColor.GREEN+"Set upper region for capture point "+((lore.size()-8)/2+1));
					}
					lore.add(ChatColor.GOLD+""+e.getClickedBlock().getX() + " " + e.getClickedBlock().getY() + " " + e.getClickedBlock().getZ());
					mt.setLore(lore);
					e.getPlayer().getInventory().getItemInMainHand().setItemMeta(mt);
				} else if((e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) && e.getPlayer().getInventory().getItemInMainHand().getItemMeta().getLore().size() > 1) {
					ItemMeta mt = e.getPlayer().getInventory().getItemInMainHand().getItemMeta();
					List<String> lore = mt.getLore();
					lore.remove(lore.size()-1);
					mt.setLore(lore);
					e.getPlayer().getInventory().getItemInMainHand().setItemMeta(mt);
					if(lore.size() <= 8) e.getPlayer().sendMessage(prefix+ChatColor.GREEN+instructor[lore.size()-1]);
					else if((lore.size() - 9) % 2 == 0){
						if(((lore.size()-8+1)/2) > 1) e.getPlayer().sendMessage(prefix+ChatColor.GREEN+"Set lower region for capture point "+((lore.size()-8)/2+1)+". If "+((lore.size()-8)/2)+" capture point(s) is enough for you please use command /oc create {your_arena_name} {min_players} {max_players} "+((lore.size()-8)/2));
						else e.getPlayer().sendMessage(prefix+ChatColor.GREEN+"Set lower region for capture point "+((lore.size()-8)/2+1));
					}else {
						e.getPlayer().sendMessage(prefix+ChatColor.GREEN+"Set upper region for capture point "+((lore.size()-8)/2));
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