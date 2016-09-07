package com.Tuong.Arena;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import com.Tuong.Heros.Genji;
import com.Tuong.Heros.Hanzo;
import com.Tuong.Heros.Lucio;
import com.Tuong.Heros.Mei;
import com.Tuong.Heros.Roadhog;
import com.Tuong.Heros.Tracer;
import com.Tuong.OverCraftCore.Core;
import com.Tuong.Region.Cuboid;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_9_R2.IChatBaseComponent;
import net.minecraft.server.v1_9_R2.PacketPlayOutTitle;
import net.minecraft.server.v1_9_R2.PlayerConnection;

public class Arena implements Listener{
	/*
	 * location[0] spawnblue
	 * location[1-2] regionspawnblue
	 * location[3] spawnred
	 * location[4-5] regionspawnred 
	 * location[6] lobby
	 * location[7] return point
	 * location[8-9] capturepoint1
	 * ...
	 * location[n-1 - n] capturepointn 
	 * int[0] minplayer
	 * int[1] maxplayer
	 * int[2] numberofcapturepoint
	 */
	private Location[] locationInfo;
	private int[] numbericInfo;
	private String arenaName;
	public Cuboid spawnRedRegion,spawnBlueRegion;
	private Cuboid[] captureArea;
	private int[] capturePoint;
	private int maxsecond = 300;
	private int captureObjective,second;
	private HashMap<Player,ItemStack[]> itemStore;
	private HashMap<Player,ItemStack[]> armorStore;
	public HashMap<Player,float[]> expStore;
	public HashMap<Player,String> team;
	public HashMap<Player,Object> playerList;	
	private boolean start,iscount,overtime,msg;
	public ArrayList<Player> death;
	private ScoreboardManager manager;
	private BossBar boss;
	public ArrayList<Player> freezed;
	public Arena(String arenaName, Location[] locationInfo,int[] numbericInfo){
		manager = Bukkit.getScoreboardManager();
		this.maxsecond = Core.maxsecond;
		Bukkit.getPluginManager().registerEvents(this, Core.plugin);
		this.locationInfo = locationInfo;
		this.numbericInfo = numbericInfo;
		this.arenaName = arenaName;
		this.msg = false;
		this.start = false; this.iscount = false;
		this.spawnBlueRegion = new Cuboid(locationInfo[1], locationInfo[2]);
		this.spawnRedRegion = new Cuboid(locationInfo[4], locationInfo[5]);
		this.captureArea = new Cuboid[numbericInfo[2]];
		for(int i = 0; i < numbericInfo[2]; i++)captureArea[i] = new Cuboid(locationInfo[9+i*2], locationInfo[9+i*2-1]);
		this.capturePoint = new int[numbericInfo[2]];
		this.captureObjective = 0;
		this.playerList = new HashMap<Player,Object>();
		this.boss = Bukkit.createBossBar("Objective Score: "+capturePoint[captureObjective]+"/"+Core.maxpoint, BarColor.BLUE, BarStyle.SEGMENTED_10);
		this.boss.setProgress(0);
		boss.setTitle("Objective Score: "+capturePoint[captureObjective]+"/"+Core.maxpoint);
		boss.setProgress(Double.valueOf(Double.valueOf(capturePoint[captureObjective])/Double.valueOf(Core.maxpoint)));
		refresh();
	}
	public boolean isAlly(Player player1, Player player2){
		if(team.containsKey(player1) && team.containsKey(player2) && team.get(player1).equals(team.get(player2))) return true;
		return false;
	}
	public void update(Player p,int i){
		if(!playerList.containsKey(p)) return;
		p.getScoreboard().clearSlot(DisplaySlot.SIDEBAR);
		Scoreboard board = manager.getNewScoreboard();
		Objective time = board.registerNewObjective("timer", "dummy");
		time.setDisplaySlot(DisplaySlot.SIDEBAR);
		time.setDisplayName(ChatColor.WHITE+""+ChatColor.BOLD+"OVER"+ChatColor.GOLD+""+ChatColor.BOLD+"CRAFT");
		Score blank = time.getScore(ChatColor.GRAY+"~~~~~~~~~~~~~~~~~~");
		blank.setScore(15);
		Score player = time.getScore(ChatColor.RED+""+ChatColor.BOLD+"» "+ChatColor.YELLOW+""+ChatColor.BOLD+"Player Info:");
		player.setScore(14);
		Score player_info1 = time.getScore(ChatColor.GRAY+"Name: "+ChatColor.WHITE+p.getName());
		player_info1.setScore(12);
		ChatColor color;
		if(team.get(p).equals("BLUE")) color = ChatColor.BLUE; else color = ChatColor.RED; 
		Score player_info2 = time.getScore(ChatColor.GRAY+"Team: "+color+team.get(p));
		player_info2.setScore(11);
		Score blank3 = time.getScore(ChatColor.RESET.toString()+ChatColor.RESET.toString()+ChatColor.RESET.toString()+ChatColor.RESET.toString());
		if(i == 1) blank3.setScore(9); else blank3.setScore(10); 
		if(i == 1){
			Score player_info3 = time.getScore(ChatColor.GRAY+"Hero: "+ChatColor.WHITE+hero(p));
			player_info3.setScore(10);
			Score game = time.getScore(ChatColor.RED+""+ChatColor.BOLD+"» "+ChatColor.YELLOW+""+ChatColor.BOLD+"Game Info:");
			game.setScore(8);
			String timer = "";
			if(second/60 > 9) timer = String.valueOf(second/60); else timer = "0"+String.valueOf(second/60);
			timer += ":";
			if(second%60 > 9) timer += String.valueOf(second%60); else timer += "0"+String.valueOf(second%60);
			Score game_1 = time.getScore(ChatColor.BLUE+"BLUE"+ChatColor.GOLD+" Wins in: "+ChatColor.GREEN+timer);
			game_1.setScore(7);
			Score game_2 = time.getScore(ChatColor.GRAY+"Objective Captured: "+ChatColor.WHITE+captureObjective+ChatColor.GRAY+"/"+ChatColor.WHITE+getNumbericInfo()[2]);
			game_2.setScore(6);
			if(capturePoint[captureObjective] < 0) capturePoint[captureObjective] = 0;
			Score game_3 = time.getScore(ChatColor.GRAY+"Objective Point: "+ChatColor.WHITE+capturePoint[captureObjective]+ChatColor.GRAY+"/"+Core.maxpoint);
			game_3.setScore(5);
			Score blank2 = time.getScore(ChatColor.RESET.toString()+ChatColor.RESET.toString());
			blank2.setScore(4);
		}
		int[] t = getTeamSize();
		Score arena = time.getScore(ChatColor.RED+""+ChatColor.BOLD+"» "+ChatColor.YELLOW+""+ChatColor.BOLD+"Arena Info:");
		arena.setScore(3);
		Score arena_1 = time.getScore(ChatColor.GRAY+"Arena: "+ChatColor.WHITE+getArenaName());
		arena_1.setScore(2);
		Score arena_2 = time.getScore(ChatColor.GRAY+"Players: "+ChatColor.BLUE+t[0]+ChatColor.DARK_GRAY+"-"+ChatColor.RED+t[1]+ChatColor.DARK_GRAY+"/"+ChatColor.WHITE+getNumbericInfo()[1]);
		arena_2.setScore(1);
		Score blank2 = time.getScore(ChatColor.RESET.toString()+ChatColor.GRAY+"~~~~~~~~~~~~~~~~~~");
		blank2.setScore(0);
		p.setScoreboard(board);
	}
	public String hero(Player p){
		if(playerList.get(p) == null) return "Waiting";
		if(playerList.get(p) instanceof Mei) return "Mei";
		if(playerList.get(p) instanceof Hanzo) return "Hanzo";
		if(playerList.get(p) instanceof Tracer) return "Tracer";
		if(playerList.get(p) instanceof Roadhog) return "Roadhog";
		if(playerList.get(p) instanceof Lucio) return "Lucio";
		if(playerList.get(p) instanceof Genji) return "Genji";
		return "";
	}
	@EventHandler
	public void playerChat(AsyncPlayerChatEvent e){
		if(playerList.containsKey(e.getPlayer())){
			e.setCancelled(true);
			for(Player p:playerList.keySet()){
				if(team.get(e.getPlayer()).equals("BLUE")) p.sendMessage(ChatColor.DARK_GRAY+"["+ChatColor.GREEN+hero(e.getPlayer())+ChatColor.DARK_GRAY+"] "+ChatColor.BLUE+e.getPlayer().getName()+ChatColor.GRAY+": "+e.getMessage());
				else p.sendMessage(ChatColor.DARK_GRAY+"["+ChatColor.GREEN+hero(e.getPlayer())+ChatColor.DARK_GRAY+"] "+ChatColor.RED+e.getPlayer().getName()+ChatColor.GRAY+": "+e.getMessage());
			}
		}
	}
	@EventHandler
	public void regionMove(PlayerMoveEvent e){
		if(!playerList.containsKey(e.getPlayer())) return;
		if(death.contains(e.getPlayer()) && (e.getFrom().getBlockX() != e.getTo().getBlockX() || e.getFrom().getBlockZ() != e.getTo().getBlockZ())) {
			e.getPlayer().sendMessage(ChatColor.RED+"You are recovering from the death!");
			e.setCancelled(true);
		}
		Location to = e.getTo();
		if((spawnRedRegion.contains(to.getBlockX(), to.getBlockY(), to.getBlockZ()) && team.get(e.getPlayer()).equals("BLUE")) || (spawnBlueRegion.contains(to.getBlockX(), to.getBlockY(), to.getBlockZ())&& team.get(e.getPlayer()).equals("RED"))){
			e.setCancelled(true);
		}
	}
	@EventHandler
	public void damageInGame(EntityDamageEvent e){
		if(e.getEntity() instanceof Player){
			Player p = (Player)e.getEntity();
			if((playerList.containsKey(p) && start == false) || death.contains(p)) e.setCancelled(true);
		}
	}
	@EventHandler
	public void drop1(PlayerDropItemEvent e){
		if(playerList.containsKey(e.getPlayer())) e.setCancelled(true);
	}
	@EventHandler
	public void drop2(InventoryClickEvent e){
		if(playerList.containsKey(e.getWhoClicked())) {
			e.setCancelled(true);
			if(e.getInventory().getName().equals(ChatColor.DARK_GREEN+"Choose team")){
				if(e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta()){
				if(ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName()).contains("Blue")){
					int[] t = getTeamSize();
					if(t[0] - 1 > 0 && t[0]+3 > t[1]){
						team.remove(e.getWhoClicked());
						team.put((Player) e.getWhoClicked(), "BLUE");
						e.getWhoClicked().sendMessage(Core.prefix+ChatColor.GRAY+"You joined team "+ChatColor.BLUE +" BLUE");
						for(Player p : playerList.keySet()) update(p,0);
					}else e.getWhoClicked().sendMessage(Core.prefix+ChatColor.RED+"You can't join that team");
					e.getWhoClicked().closeInventory();
				}else if(ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName()).contains("Red")){
					int[] t = getTeamSize();
					if(t[1] - 1 > 0 && t[1]+3 > t[0]){
						team.remove(e.getWhoClicked());
						team.put((Player) e.getWhoClicked(), "RED");
						e.getWhoClicked().sendMessage(Core.prefix+ChatColor.GRAY+"You joined team "+ChatColor.RED +" RED");
						for(Player p : playerList.keySet()) update(p,0);
					}else e.getWhoClicked().sendMessage(Core.prefix+ChatColor.RED+"You can't join that team");
					e.getWhoClicked().closeInventory();
				}
				}
			}
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void playerOut(PlayerQuitEvent e){
		if(playerList.containsKey(e.getPlayer())){
			playerLeave(e.getPlayer());			
			broadcast(ChatColor.RED+"Player "+ChatColor.GOLD+e.getPlayer().getName()+ChatColor.RED+" left the game");
		}
	}
	public void broadcast(String message){
		for(Player p : playerList.keySet()) p.sendMessage(Core.prefix+message);
	}
	public void broadcast2(String message,String message2){
		for(Player p : playerList.keySet()) {
			sendTitle(p,10,15,5,message, message2);
		}
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
	public void playerJoin(Player player){
		player.setResourcePack("https://dl.dropboxusercontent.com/s/gpc6voja8vp009n/OverCraft.zip");
		player.setHealth(player.getMaxHealth());
		player.setFoodLevel(20);
		playerList.put(player, null);
		itemStore.put(player, player.getInventory().getContents());
		armorStore.put(player, player.getInventory().getArmorContents());
		Location cac = getLocationInfo()[6].clone();
		player.teleport(cac.add(0,1,0));
		player.setGameMode(GameMode.ADVENTURE);
		player.getInventory().clear();
		player.getInventory().addItem(Core.getTeamCompass());
		float[] g = new float[2];
		g[0] = player.getLevel();
		g[1] = player.getExp();
		expStore.put(player, g);
		int[] t = getTeamSize();
		if(t[0] > t[1]) team.put(player, "RED"); else team.put(player, "BLUE");
		ChatColor color;
		if(team.get(player).equals("BLUE")) color = ChatColor.BLUE; else color = ChatColor.RED; 
		player.sendMessage(Core.prefix+ChatColor.GREEN+"Join arena "+ChatColor.GOLD+getArenaName()+ChatColor.GREEN+" on "+ color+team.get(player)+ChatColor.GREEN +" team");
		if(playerList.size() >= getNumbericInfo()[0] && iscount == false) {
			iscount = true;
			countDown();
		}
		for(Player p : playerList.keySet())update(p,0);
	}
	@EventHandler
	public void texturePack(PlayerResourcePackStatusEvent e){
		if(playerList.containsKey(e.getPlayer())){
			if(e.getStatus() == Status.DECLINED) e.getPlayer().sendMessage(Core.prefix+ChatColor.RED+"You denied resource pack download which you may not have full experience of the game");			
			if(e.getStatus() == Status.ACCEPTED) e.getPlayer().sendMessage(Core.prefix+ChatColor.GREEN+"Downloading resource pack...");
			if(e.getStatus() == Status.SUCCESSFULLY_LOADED)e.getPlayer().sendMessage(Core.prefix+ChatColor.GOLD+"Downloading resource pack done!");
			if(e.getStatus() == Status.FAILED_DOWNLOAD)e.getPlayer().sendMessage(Core.prefix+ChatColor.GRAY+"Downloading resource pack fail!");
		}
	}
	public void countDown(){
		broadcast(ChatColor.GREEN+"Count down started...");
		new BukkitRunnable() {
			int t = 8;
			@Override
			public void run() {
				if(iscount == false){
					broadcast(ChatColor.RED+"Count down stop because not enough players");
					this.cancel();
				}
				if(t != 0) broadcast2(ChatColor.GREEN+"Game about to start in ",ChatColor.GOLD+""+t+" second");
				for(Player p : playerList.keySet()) p.getWorld().playSound(p.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				if(t <= 0 && start == false) {
					start = true;
					broadcast2(ChatColor.GOLD+"!Lets the game started!","");
					play();
					this.cancel();
				}
				t--;
			}
		}.runTaskTimer(Core.plugin, 1, 20);
	}
	@EventHandler
	public void playerhunger(FoodLevelChangeEvent e){
		if(playerList.containsKey(e.getEntity())) {
			e.setCancelled(true);
		}
	}
	@EventHandler
	public void playerDie(PlayerDeathEvent e){
		if(playerList.containsKey(e.getEntity())) {
			e.setKeepInventory(true);
			e.setKeepLevel(true);
		}
	}
	@EventHandler
	public void playerRespawn(PlayerRespawnEvent e){
		if(team.containsKey(e.getPlayer())) {
			if(team.get(e.getPlayer()).equals("BLUE")) e.setRespawnLocation(getLocationInfo()[0]);
			else e.setRespawnLocation(getLocationInfo()[3]);
			death.add(e.getPlayer());
			new BukkitRunnable() {
				@Override
				public void run() {
					if(death.contains(e.getPlayer())) death.remove(e.getPlayer());
				}
			}.runTaskLater(Core.plugin, 200);
		}
	}
	@EventHandler
	public void useCommand(PlayerCommandPreprocessEvent e){
		if(playerList.containsKey(e.getPlayer()) && !e.getMessage().equals("/oc leave") && !(e.getMessage().contains("/oc class") && e.getMessage().indexOf("/oc class") == 0)) e.setCancelled(true);
	}
	public void play(){
		for(Player p : team.keySet()) {
			p.getInventory().clear();
			Location cac = getLocationInfo()[0].clone();
			Location loz = getLocationInfo()[3].clone();
			if(team.get(p).equals("BLUE")) p.teleport(cac.add(0,1,0)); else p.teleport(loz.add(0,1,0));
			p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
			playerList.remove(p);
			playerList.put(p, new Tracer(p,this));
			//add default hero
			boss.addPlayer(p);
			if(team.get(p).equals("BLUE")) p.sendMessage(Core.prefix+ChatColor.BLUE+"Defend objective "+ChatColor.LIGHT_PURPLE+(captureObjective+1)+ChatColor.BLUE+" at"+ChatColor.GREEN+" x: "+ChatColor.GRAY+captureArea[captureObjective].getCenter().getBlockX() +ChatColor.GREEN+" y: "+ChatColor.GRAY+captureArea[captureObjective].getCenter().getBlockY()+ChatColor.GREEN+" z: "+ChatColor.GRAY+captureArea[captureObjective].getCenter().getBlockZ()); 
			else p.sendMessage(Core.prefix+ChatColor.RED+"Attack objective "+ChatColor.LIGHT_PURPLE+(captureObjective+1)+ChatColor.BLUE+" at"+ChatColor.GREEN+" x: "+ChatColor.GRAY+captureArea[captureObjective].getCenter().getBlockX() +ChatColor.GREEN+" y: "+ChatColor.GRAY+captureArea[captureObjective].getCenter().getBlockY()+ChatColor.GREEN+" z: "+ChatColor.GRAY+captureArea[captureObjective].getCenter().getBlockZ()); 
		}
		boss.setVisible(true);
		broadcast(ChatColor.GREEN+"Teleport to arena");
		new BukkitRunnable() {
			@Override
			public void run() {
				if(capturePoint[captureObjective] >= Core.maxpoint){
					if(captureObjective+1 >= getNumbericInfo()[2]){
						//win
						for(Player p : playerList.keySet()) {
							if(team.get(p).equals("RED")) {
								sendTitle(p, 20, 60, 25, ChatColor.GOLD+"You won!", "");
								p.sendMessage(Core.prefix +ChatColor.GREEN+ "You won the game on arena "+ChatColor.GOLD+getArenaName());
							}else{
								sendTitle(p, 20, 60, 25, ChatColor.GRAY+"You lost!", "");
								p.sendMessage(Core.prefix +ChatColor.GRAY+ "You lost the game on arena "+ChatColor.GOLD+getArenaName());
							}
						}
						refresh();
						this.cancel();
						return;
					}
					overtime = false;
					msg = false;
					second += maxsecond;
					captureObjective++;
					for(Player p : team.keySet()) if(team.get(p).equals("BLUE")) p.sendMessage(Core.prefix+ChatColor.BLUE+"Defend objective "+ChatColor.LIGHT_PURPLE+(captureObjective+1)+ChatColor.BLUE+" at"+ChatColor.GREEN+" x: "+ChatColor.GRAY+captureArea[captureObjective].getCenter().getBlockX() +ChatColor.GREEN+" y: "+ChatColor.GRAY+captureArea[captureObjective].getCenter().getBlockY()+ChatColor.GREEN+" z: "+ChatColor.GRAY+captureArea[captureObjective].getCenter().getBlockZ()); 
					else p.sendMessage(Core.prefix+ChatColor.RED+"Attack objective "+ChatColor.LIGHT_PURPLE+(captureObjective+1)+ChatColor.BLUE+" at"+ChatColor.GREEN+" x: "+ChatColor.GRAY+captureArea[captureObjective].getCenter().getBlockX() +ChatColor.GREEN+" y: "+ChatColor.GRAY+captureArea[captureObjective].getCenter().getBlockY()+ChatColor.GREEN+" z: "+ChatColor.GRAY+captureArea[captureObjective].getCenter().getBlockZ()); 
				}
				boolean b = false;
				if(capturePoint[captureObjective] < 0) capturePoint[captureObjective] = 0;
				for(Player p : team.keySet()){
					if(captureArea[captureObjective].contains(p.getLocation()))
					if(team.get(p).equals("BLUE")) capturePoint[captureObjective] -= 5;
					else {
						capturePoint[captureObjective] += 5;
						b = true;
					}
					update(p,1);
					if((spawnBlueRegion.contains(p.getLocation()) && team.get(p).equals("BLUE")) || spawnRedRegion.contains(p.getLocation()) && team.get(p).equals("RED")){
						if(p.getHealth() < p.getMaxHealth()-1) p.setHealth(p.getHealth()+1);
					}
				}
				second--;
				if((second <= 0 || overtime) && b) {
					second = 5;
					overtime = true;
					if(overtime && msg == false){
						for(Player p : playerList.keySet()) sendTitle(p,20,60,25,ChatColor.GOLD+"OVERTIME","");
						msg = true;
					}
					if(capturePoint[captureObjective] >= Core.maxpoint){
						if(captureObjective+1 >= getNumbericInfo()[2]){
							//win
							for(Player p : playerList.keySet()) {
								if(team.get(p).equals("RED")) {
									sendTitle(p, 20, 60, 25, ChatColor.GOLD+"You won!", "");
									p.sendMessage(Core.prefix +ChatColor.GREEN+ "You won the game on arena "+ChatColor.GOLD+getArenaName());
								}else{
									sendTitle(p, 20, 60, 25, ChatColor.GRAY+"You lost!", "");
									p.sendMessage(Core.prefix +ChatColor.GRAY+ "You lost the game on arena "+ChatColor.GOLD+getArenaName());
								}
							}
							refresh();
							this.cancel();
							return;
						}
						overtime = false;
						msg = false;
						second += maxsecond;
						captureObjective++;
						for(Player p : team.keySet()) if(team.get(p).equals("BLUE")) p.sendMessage(Core.prefix+ChatColor.BLUE+"Defend objective "+ChatColor.LIGHT_PURPLE+(captureObjective+1)+ChatColor.BLUE+" at"+ChatColor.GREEN+" x: "+ChatColor.GRAY+captureArea[captureObjective].getCenter().getBlockX() +ChatColor.GREEN+" y: "+ChatColor.GRAY+captureArea[captureObjective].getCenter().getBlockY()+ChatColor.GREEN+" z: "+ChatColor.GRAY+captureArea[captureObjective].getCenter().getBlockZ()); 
						else p.sendMessage(Core.prefix+ChatColor.RED+"Attack objective "+ChatColor.LIGHT_PURPLE+(captureObjective+1)+ChatColor.BLUE+" at"+ChatColor.GREEN+" x: "+ChatColor.GRAY+captureArea[captureObjective].getCenter().getBlockX() +ChatColor.GREEN+" y: "+ChatColor.GRAY+captureArea[captureObjective].getCenter().getBlockY()+ChatColor.GREEN+" z: "+ChatColor.GRAY+captureArea[captureObjective].getCenter().getBlockZ()); 
					}
				}else if(second <= 0 && b == false){
					//win
					for(Player p : playerList.keySet()) {
						if(team.get(p).equals("BLUE")) {
							sendTitle(p, 20, 60, 25, ChatColor.GOLD+"You won!", "");
							p.sendMessage(Core.prefix +ChatColor.GREEN+ "You won the game on arena "+ChatColor.GOLD+getArenaName());
						}else{
							sendTitle(p, 20, 60, 25, ChatColor.GRAY+"You lost!", "");
							p.sendMessage(Core.prefix +ChatColor.GRAY+ "You lost the game on arena "+ChatColor.GOLD+getArenaName());
						}
					}
					refresh();
					this.cancel();
					return;
				}
				if(b == false) capturePoint[captureObjective]--;
				if(capturePoint[captureObjective] < 0) capturePoint[captureObjective] = 0;
				boss.setTitle("Objective Score: "+capturePoint[captureObjective]+"/"+Core.maxpoint);
				boss.setProgress(Double.valueOf(Double.valueOf(capturePoint[captureObjective])/Double.valueOf(Core.maxpoint)));
				int[] t = getTeamSize();
				if(t[0] == 0 || t[1] == 0){
					for(Player p : playerList.keySet()) {
						sendTitle(p, 20, 60, 25, ChatColor.GOLD+"You won!", "");
						p.sendMessage(Core.prefix +ChatColor.GREEN+ "You won the game on arena "+ChatColor.GOLD+getArenaName());
					}
					//cancel
					refresh();
					this.cancel(); 
					return;
				}
				if(capturePoint[captureObjective] >= Core.maxpoint){
					if(captureObjective+1 >= getNumbericInfo()[2]){
						//win
						for(Player p : playerList.keySet()) {
							if(team.get(p).equals("RED")) {
								sendTitle(p, 20, 60, 25, ChatColor.GOLD+"You won!", "");
								p.sendMessage(Core.prefix +ChatColor.GREEN+ "You won the game on arena "+ChatColor.GOLD+getArenaName());
							}else{
								sendTitle(p, 20, 60, 25, ChatColor.GRAY+"You lost!", "");
								p.sendMessage(Core.prefix +ChatColor.GRAY+ "You lost the game on arena "+ChatColor.GOLD+getArenaName());
							}
						}
						refresh();
						this.cancel();
						return;
					}
					overtime = false;
					msg = false;
					second += maxsecond;
					captureObjective++;
				}
			}
		}.runTaskTimer(Core.plugin, 0, 20);
	}
	public HashMap<Player,String> getTeam(){
		return team;
	}
	public int[] getTeamSize(){
		int[] i = {0,0};
		for(Player p : team.keySet()) if(team.get(p).equals("BLUE")) i[0]++; else i[1]++;
		return i;
	}
	@EventHandler
	public void chooseTeam(PlayerInteractEvent e){
		if(e.getPlayer().getInventory().getItemInMainHand().equals(Core.getTeamCompass())) e.getPlayer().openInventory(Core.getTeamInventory());
	}
	public void playerLeave(Player player){
		classRemove(player);
		player.getScoreboard().clearSlot(DisplaySlot.SIDEBAR);
		player.setHealth(player.getMaxHealth());
		boss.removePlayer(player);
		player.getInventory().clear();
		player.getInventory().setArmorContents(null);
		if(itemStore.containsKey(player))player.getInventory().setContents(itemStore.get(player));
		if(armorStore.containsKey(player))player.getInventory().setArmorContents(armorStore.get(player));
		Location cac = getLocationInfo()[7].clone();
		player.teleport(cac.add(0,1,0));
		player.sendMessage(Core.prefix+ChatColor.GRAY+"Leave arena "+ChatColor.GOLD+getArenaName());
		playerList.remove(player);
		team.remove(player);
		player.setMaxHealth(20);
		player.setHealth(20);
		player.setLevel(0); player.setExp(0);
		player.setLevel((int)expStore.get(player)[0]);
		player.setExp(expStore.get(player)[1]);
		if(playerList.size() < getNumbericInfo()[0] && iscount) iscount = false;
	}
	
	@EventHandler
	public void hunger(FoodLevelChangeEvent e){
		if(playerList.containsKey((Player)e.getEntity())) e.setCancelled(true);
	}
	public ItemStack getBlueChestplate(){
		ItemStack lhelmet = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
		LeatherArmorMeta lam = (LeatherArmorMeta)lhelmet.getItemMeta();
		lam.spigot().setUnbreakable(true);
		lam.setColor(Color.BLUE);
		lhelmet.setItemMeta(lam);
		return lhelmet;
	}
	public ItemStack getRedChestplate(){
		ItemStack lhelmet = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
		LeatherArmorMeta lam = (LeatherArmorMeta)lhelmet.getItemMeta();
		lam.spigot().setUnbreakable(true);
		lam.setColor(Color.RED);
		lhelmet.setItemMeta(lam);
		return lhelmet;
	}
	@EventHandler
    public void onDamage(EntityDamageEvent event) {
    if (playerList.containsKey(event.getEntity())) {
    Player player = (Player) event.getEntity();
    if(player.getInventory().getChestplate() != null) player.getInventory().getChestplate().setDurability((short) 0);
    }
    }
	public void refresh(){
		while(!playerList.isEmpty()) for(Player p : playerList.keySet()){
			playerLeave(p);
			break;
		}
		this.boss.removeAll();
		this.freezed = new ArrayList<Player>();
		this.death= new ArrayList<Player>();
		this.expStore = new HashMap<Player,float[]>();
		this.itemStore = new HashMap<Player,ItemStack[]>();
		this.armorStore = new HashMap<Player,ItemStack[]>();
		this.playerList = new HashMap<Player,Object>();
		this.team = new HashMap<Player,String>();
		this.captureObjective = 0;
		this.capturePoint = new int[getNumbericInfo()[2]];
		this.start = false;
		this.overtime = false;
		this.second = maxsecond;
		getLocationInfo()[0].getWorld().setGameRuleValue("mobGriefing", "false");
	}
	
	public void classRemove(Player player){
		if(playerList.containsKey(player) && playerList.get(player) == null) return;
		if(playerList.get(player) instanceof Tracer) ((Tracer)playerList.get(player)).stop();
		else if(playerList.get(player) instanceof Hanzo) ((Hanzo)playerList.get(player)).stop();
		else if(playerList.get(player) instanceof Roadhog) ((Roadhog)playerList.get(player)).stop();
		else if(playerList.get(player) instanceof Lucio) ((Lucio)playerList.get(player)).stop();
		else if(playerList.get(player) instanceof Genji) ((Genji)playerList.get(player)).stop();
		else if(playerList.get(player) instanceof Mei) ((Mei)playerList.get(player)).stop();
	}
	
	public boolean inArena(Player player){
		return playerList.containsKey(player);
	}
	public String getArenaName(){
		return this.arenaName;
	}
	public HashMap<Player,Object> getPlayerList(){
		return this.playerList;
	}
	public Location[] getLocationInfo(){
		return this.locationInfo;
	}
	public int[] getNumbericInfo(){
		return this.numbericInfo;
	}
	public boolean started(){
		return this.start;
	}
}
