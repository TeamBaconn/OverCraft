package com.Tuong.Heros;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.Tuong.Arena.Arena;
import com.Tuong.OverCraftCore.Core;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_9_R2.EnumParticle;

public class Soldier76 implements Listener{
	private Player player;
	private Arena arena;
	private int ammo,maxhealth;
	private float ultimate_charge;
	private final double damageshoot = 6;
	private boolean start,msg,reloading,ult;
	public Soldier76(Player player, Arena arena){
		player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
		player.getInventory().clear();
		player.getInventory().setHeldItemSlot(8);
		player.getInventory().setItemInMainHand(getItemName());
		if(arena.team.get(player).equals("BLUE")) player.getInventory().setChestplate(arena.getBlueChestplate());
		else player.getInventory().setChestplate(arena.getRedChestplate());
		if(Core.t) player.playSound(player.getLocation(), Sound.ENTITY_SHEEP_STEP, 1, 1);
		this.player = player;
		this.arena = arena;
		this.start = true;
		this.msg = false;
		this.ammo = 25;
		this.maxhealth = 15;
		this.reloading = false;
		this.ult = false;
		this.ultimate_charge = 0;
		player.setMaxHealth(maxhealth);
		player.setHealth(player.getMaxHealth());
		Bukkit.getPluginManager().registerEvents(this, Core.plugin);
	}
	
	private ItemStack getItemName(){
        ItemStack pistol = new ItemStack(Material.IRON_PICKAXE, 1);
        ItemMeta meta = pistol.getItemMeta();
        pistol.setItemMeta(meta);
        meta.spigot().setUnbreakable(true);
        meta.setDisplayName(ChatColor.BOLD + "" + ChatColor.GRAY + "Heavy Pulse Rifle");
        meta.setLore(Arrays.asList(ChatColor.GOLD + "Type: " + ChatColor.GRAY + "" + ChatColor.ITALIC + "Rapid Fire Hitscan",
                ChatColor.GOLD + "Damage: " + ChatColor.GRAY + "" + ChatColor.ITALIC + "5-17",
                ChatColor.GOLD + "Falloff range: " + ChatColor.GRAY + "" + ChatColor.ITALIC + "35 to 55 meters",
                ChatColor.GOLD + "Rate of fire: " + ChatColor.GRAY + "" + ChatColor.ITALIC + "10 rounds per second",
                ChatColor.GOLD + "Ammo: " + ChatColor.GRAY + "" + ChatColor.ITALIC + "25",
                ChatColor.GOLD + "Reload Time: " + ChatColor.GRAY + "" + ChatColor.ITALIC + "1.5 Second",
                ChatColor.LIGHT_PURPLE + "Description: " + ChatColor.RED + "" + ChatColor.ITALIC + "Soldier: 76’s rifle remains particularly steady while unloading fully-automatic pulse fire."));
        pistol.setItemMeta(meta);
        return pistol;
    }
	
	@EventHandler
	public void respawn(PlayerRespawnEvent e){
		if(!e.getPlayer().equals(player)) return;
		player.setMaxHealth(maxhealth);
		player.setHealth(player.getMaxHealth());
	}
	
	public Player getPlayer(){
		return this.player;
	}
	
	@EventHandler
	public void fall(EntityDamageEvent e){
		if(e.getEntity().equals(player) && e.getCause() == DamageCause.FALL) e.setCancelled(true);
	}
	
	@EventHandler
	public void click(InventoryClickEvent e){
		if(e.getWhoClicked().equals(player)) e.setCancelled(true);
	}
	public static ArrayList<Location> circle(Location center, double radius, int amount)
    {
        World world = center.getWorld();
        double increment = (2 * Math.PI) / amount;
        ArrayList<Location> locations = new ArrayList<Location>();
        for(int i = 0;i < amount; i++)
        {
            double angle = i * increment;
            double x = center.getX() + (radius * Math.cos(angle));
            double z = center.getZ() + (radius * Math.sin(angle));
            locations.add(new Location(world, x, center.getY(), z));
        }
        return locations;
    }
	@EventHandler
	public void kill(EntityDeathEvent e){
		if(e.getEntity().getKiller() != null && e.getEntity().getKiller().equals(player) && !e.getEntity().equals(player)) {
			if(ultimate_charge + 0.4 >= 1){
				if(start)player.setLevel(ammo);
				ultimate_charge = 1;
				if(msg == false){
					if(Core.t) player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_DEATH, 1, 1);
					Core.sendTitle(player, 5, 51, 1, ChatColor.GOLD+"ULTIMATE", ChatColor.GREEN+"Ready to launch");
					msg = true;
				}
			}else ultimate_charge += 0.4;
			if(start)player.setExp(ultimate_charge);
		}
	}
	
	@EventHandler
	public void pick(PlayerPickupArrowEvent e){
		if(e.getPlayer().equals(player))e.setCancelled(true);
	}
	@EventHandler
	public void pick(PlayerPickupItemEvent e){
		if(e.getPlayer().equals(player))e.setCancelled(true);
	}
	@SuppressWarnings("deprecation")
	@EventHandler
	public void rekall(PlayerItemHeldEvent e){
		if(e.getPlayer().equals(player) && arena.death.contains(player)){
			e.setCancelled(true);
			return;
		}
		if(e.getPlayer().equals(player)){
			if(!player.hasPotionEffect(PotionEffectType.LUCK) && player.isOnGround()){
				if(Core.t) player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SHEEP_AMBIENT, 1, 1);
				player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 160, 1));
				Location loc = e.getPlayer().getLocation().clone().add(0,0.15,0);
				new BukkitRunnable() {
					int t = 0;
					@Override
					public void run() {
						t++;
						Core.playParticle(EnumParticle.HEART, loc, 4);
						for(Location l : circle(loc, 3, 30)) Core.playParticle(EnumParticle.DRIP_LAVA, l, 2);
						if(t % 2 == 0) for(Player p : arena.playerList.keySet()) if((arena.isAlly(p, player) || p.equals(player)) && loc.distance(p.getLocation()) <= 3) player.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 20, 1));
						if(t == 8) this.cancel();
					}
				}.runTaskTimer(Core.plugin, 0, 10);
			}
		}
	}
	  int TRACKING_RANGE = 50;
	  final double DONT_HIDE_RANGE = 4; 
	  final double VECTOR_LENGTH = 0.8; 
	  final double HIDE_DEGREES = 70;
	  private boolean checkLookable(Player player, Entity target) {
	      Location loc = player.getLocation().add(0, 1.625, 0); //1.625
	      Location target_loc = target.getLocation().add(0, 1, 0); // 1
	      if(!loc.getChunk().isLoaded())return false;               //check loaded
	      if(!target_loc.getChunk().isLoaded())return false;        //check loaded
	      if(loc.getWorld() != target_loc.getWorld()) return false; //check again
	              double distance = loc.distance(target_loc);
	              double checked_distance = 0;

	              // too far, force hide
	              if(distance > TRACKING_RANGE){
	                  return false;
	              }

	              // too near, force show
	              if (distance < DONT_HIDE_RANGE * 2) {
	                  return true;
	              }

	              Vector vector1 = target_loc.subtract(loc).toVector();
	              vector1.multiply(1 / vector1.length());

	              Vector A = vector1.clone();
	              Vector B = loc.getDirection();
	              double degrees = Math.toDegrees(Math.acos(A.dot(B) / A.length() * B.length()));
	              if (degrees > HIDE_DEGREES) {
	                  return false;
	              }

	              // don't check too near block
	              checked_distance += DONT_HIDE_RANGE;
	              loc.add(vector1.clone().multiply(DONT_HIDE_RANGE / VECTOR_LENGTH));

	              vector1.multiply(VECTOR_LENGTH);
	              distance -= DONT_HIDE_RANGE; // don't check if too near target
	              while (checked_distance < distance) {
	                  //player.sendBlockChange(loc, Material.GLASS, (byte) 0);
	                  if(!loc.getChunk().isLoaded())return false; //check loaded
	                  if (isOccluding(loc.getBlock().getType())) {
	                      return false;
	                  }
	                  checked_distance += VECTOR_LENGTH;
	                  loc.add(vector1);
	              }
	              return true;
	  }
	  public boolean isOccluding(Material m) {
	      if (!m.isBlock()) {
	          return false;
	      }
	      switch (m) {
	          case STONE:
	          case GRASS:
	          case DIRT:
	          case COBBLESTONE:
	          case WOOD:
	          case BEDROCK:
	          case SAND:
	          case GRAVEL:
	          case GOLD_ORE:
	          case IRON_ORE:
	          case COAL_ORE:
	          case LOG:
	          case SPONGE:
	          case LAPIS_ORE:
	          case LAPIS_BLOCK:
	          case DISPENSER:
	          case SANDSTONE:
	          case NOTE_BLOCK:
	          case WOOL:
	          case GOLD_BLOCK:
	          case IRON_BLOCK:
	          case DOUBLE_STEP:
	          case BRICK:
	          case BOOKSHELF:
	          case MOSSY_COBBLESTONE:
	          case OBSIDIAN:
	          case MOB_SPAWNER:
	          case DIAMOND_ORE:
	          case DIAMOND_BLOCK:
	          case WORKBENCH:
	          case FURNACE:
	          case BURNING_FURNACE:
	          case REDSTONE_ORE:
	          case GLOWING_REDSTONE_ORE:
	          case SNOW_BLOCK:
	          case CLAY:
	          case JUKEBOX:
	          case PUMPKIN:
	          case NETHERRACK:
	          case SOUL_SAND:
	          case JACK_O_LANTERN:
	          case MONSTER_EGGS:
	          case SMOOTH_BRICK:
	          case HUGE_MUSHROOM_1:
	          case HUGE_MUSHROOM_2:
	          case MELON_BLOCK:
	          case MYCEL:
	          case NETHER_BRICK:
	          case ENDER_STONE:
	          case REDSTONE_LAMP_OFF:
	          case REDSTONE_LAMP_ON:
	          case WOOD_DOUBLE_STEP:
	          case EMERALD_ORE:
	          case EMERALD_BLOCK:
	          case COMMAND:
	          case QUARTZ_ORE:
	          case QUARTZ_BLOCK:
	          case DROPPER:
	          case STAINED_CLAY:
	          case HAY_BLOCK:
	          case HARD_CLAY:
	          case COAL_BLOCK:
	          case LOG_2:
	          case PACKED_ICE:
	          case SLIME_BLOCK:
	          case PRISMARINE:
	          case RED_SANDSTONE:
	          case DOUBLE_STONE_SLAB2:
	          case PURPUR_BLOCK:
	          case PURPUR_PILLAR:
	          case PURPUR_DOUBLE_SLAB:
	          case END_BRICKS:
	          case STRUCTURE_BLOCK:
	          case COMMAND_REPEATING:
	          case COMMAND_CHAIN:
	              return true;
	          default:
	              return false;
	      }
	  }
	@EventHandler
	public void shoot(PlayerInteractEvent e){
		if(e.getPlayer().equals(player) && (arena.death.contains(player) || reloading)) {
			e.setCancelled(true);
			return;
		}
		if(e.getPlayer().equals(player) && (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR)){
			if(ammo > 0 && start){
				if(ult){
					boolean c = true;
					for(Player p : arena.playerList.keySet()) if(!arena.isAlly(p, player) || !p.equals(player)){
						if(checkLookable(player, p)) {
							Location loc = lookAt(player.getLocation(), p.getLocation());
							Vector direction = loc.getDirection().normalize();
							double t = 0;
							while(true){
								t += 1.8;
								double x = direction.getX() * t;
								double y = direction.getY() * t + 1.5;
								double z = direction.getZ() * t;
								loc.add(x,y,z);
								boolean b = true;
								if(loc.getBlock().getType() != Material.AIR && loc.getBlock().getType() != Material.GRASS && loc.getBlock().getType() != Material.LONG_GRASS) break;
								if(b){
									Core.playParticle(EnumParticle.FIREWORKS_SPARK, loc, 1);
									loc.subtract(x,y,z);
									if (t > 50)break;
								}
							}
							c = false;
							p.damage(damageshoot,player);
						}
						break;
					}
					if(c){
						Location loc = player.getLocation();
						Vector direction = loc.getDirection().normalize();
						Player p2 = player;
						double t = 0;
						while(true){
							t += 1.8;
							double x = direction.getX() * t;
							double y = direction.getY() * t + 1.5;
							double z = direction.getZ() * t;
							loc.add(x,y,z);
							boolean b = true;
							if(loc.getBlock().getType() != Material.AIR && loc.getBlock().getType() != Material.GRASS && loc.getBlock().getType() != Material.LONG_GRASS) break;
							for(Player p : arena.getPlayerList().keySet()){
								if(p.getEyeLocation().distance(loc) <= 1.5 || p.getLocation().distance(loc) <= 1.5){
									if(arena.isAlly(p, p2) == false) {
										if(arena.playerList.get(p) instanceof Genji && ((Genji)arena.playerList.get(p)).isReflect()){
											t = 0;
											p2 = p;
											loc = p.getLocation();
											direction = loc.getDirection().normalize();
											b = false;
										}else if(p2 != null && p2 instanceof Player) p.damage(damageshoot,p2);
										break;
									}
								}
							}
							if(b){
								Core.playParticle(EnumParticle.FIREWORKS_SPARK, loc, 1);
								loc.subtract(x,y,z);
								if (t > 50)break;
							}
						}
					}
				}else{
					Location loc = player.getLocation();
					Vector direction = loc.getDirection().normalize();
					Player p2 = player;
					double t = 0;
					while(true){
						t += 1.8;
						double x = direction.getX() * t;
						double y = direction.getY() * t + 1.5;
						double z = direction.getZ() * t;
						loc.add(x,y,z);
						boolean b = true;
						if(loc.getBlock().getType() != Material.AIR && loc.getBlock().getType() != Material.GRASS && loc.getBlock().getType() != Material.LONG_GRASS) break;
						for(Player p : arena.getPlayerList().keySet()){
							if(p.getEyeLocation().distance(loc) <= 1.5 || p.getLocation().distance(loc) <= 1.5){
								if(arena.isAlly(p, p2) == false) {
									if(arena.playerList.get(p) instanceof Genji && ((Genji)arena.playerList.get(p)).isReflect()){
										t = 0;
										p2 = p;
										loc = p.getLocation();
										direction = loc.getDirection().normalize();
										b = false;
									}else if(p2 != null && p2 instanceof Player) p.damage(damageshoot,p2);
									break;
								}
							}
						}
						if(b){
							Core.playParticle(EnumParticle.FIREWORKS_SPARK, loc, 1);
							loc.subtract(x,y,z);
							if (t > 50)break;
						}
					}
				}
				ammo-=1;
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_BREAK, 1, 1);
				if(start)player.setLevel(ammo);
			}else{
				e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_OFF, 1, 1);
				if(reloading) return;
				reloading = true;
				new BukkitRunnable() {
					@Override
					public void run() {
						e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
						ammo = 25;
						if(start)player.setLevel(ammo);
						reloading = false;
					}
				}.runTaskLater(Core.plugin, 20);
			}
		}else if(e.getPlayer().equals(player) && (e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_AIR) && !e.getPlayer().hasPotionEffect(PotionEffectType.UNLUCK)){
			Fireball fire = player.getWorld().spawn(player.getEyeLocation().clone().add(player.getLocation().getDirection()), Fireball.class);
			player.addPotionEffect(new PotionEffect(PotionEffectType.UNLUCK, 160, 1));
			Vector v = player.getLocation().getDirection().multiply(4);;
			fire.setMetadata(player.getName(), new FixedMetadataValue(Core.plugin, "Soldier76"));
			if(ult){
				for(Player p : arena.playerList.keySet()) if(!arena.isAlly(p, player) || !p.equals(player)) if(checkLookable(player, p)) {
					v = lookAt(player.getLocation(),p.getLocation()).getDirection().multiply(4);
				}
			}
			fire.setShooter(player);
			fire.setIsIncendiary(false);
			fire.setVelocity(v);
		}
	}
	public Location lookAt(Location loc, Location lookat) {
        //Clone the loc to prevent applied changes to the input loc
        loc = loc.clone();

        // Values of change in distance (make it relative)
        double dx = lookat.getX() - loc.getX();
        double dy = lookat.getY() - loc.getY();
        double dz = lookat.getZ() - loc.getZ();

        // Set yaw
        if (dx != 0) {
            // Set yaw start value based on dx
            if (dx < 0) {
                loc.setYaw((float) (1.5 * Math.PI));
            } else {
                loc.setYaw((float) (0.5 * Math.PI));
            }
            loc.setYaw((float) loc.getYaw() - (float) Math.atan(dz / dx));
        } else if (dz < 0) {
            loc.setYaw((float) Math.PI);
        }

        // Get the distance from dx/dz
        double dxz = Math.sqrt(Math.pow(dx, 2) + Math.pow(dz, 2));

        // Set pitch
        loc.setPitch((float) -Math.atan(dy / dxz));

        // Set values, convert to degrees (invert the yaw since Bukkit uses a different yaw dimension format)
        loc.setYaw(-loc.getYaw() * 180f / (float) Math.PI);
        loc.setPitch(loc.getPitch() * 180f / (float) Math.PI);

        return loc;
    }
	@EventHandler
	public void switchItem(PlayerSwapHandItemsEvent e){
		e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_OFF, 1, 1);
		if(reloading) return;
		reloading = true;
		new BukkitRunnable() {
			@Override
			public void run() {
				e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				ammo = 25;
				if(start)player.setLevel(ammo);
				reloading = false;
			}
		}.runTaskLater(Core.plugin, 20);
	}
	@EventHandler
	public void bomb(PlayerDropItemEvent e){
		if(e.getPlayer().equals(player)){
			e.setCancelled(true);
			if(e.getPlayer().equals(player) && arena.death.contains(player)) return;
			if (this.ultimate_charge == 1.0F) {
				if(Core.t) player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SHEEP_SHEAR, 1, 1);
		        this.ultimate_charge = 0.0F;
		        ult = true;
		        player.getInventory().setHelmet(new ItemStack(Material.PUMPKIN));
		        new BukkitRunnable() {
					@Override
					public void run() {
		                ult = false;
		                player.getInventory().setHelmet(null);
					}
				}.runTaskLater(Core.plugin, 120);
		    }
		}
	}
	
	@EventHandler
	public void explode(EntityExplodeEvent e){
		if(e.getEntity().hasMetadata(player.getName())){
			e.blockList().clear();
		}
	}
	
	@EventHandler
	public void nodamage(EntityDamageByEntityEvent e){
		if(e.getDamager().equals(player) && e.getEntity() instanceof Player) if(arena.isAlly(player, (Player)e.getEntity())) e.setCancelled(true);
		if(e.getDamager().hasMetadata(player.getName()) && e.getEntity() instanceof Player) if(arena.isAlly(player, (Player)e.getEntity())) e.setCancelled(true);
	}
	
	public void stop(){
		for(PotionEffect pe :  player.getActivePotionEffects()) player.removePotionEffect(pe.getType());
		start = false;
		HandlerList.unregisterAll(this);
		player.setWalkSpeed(0.2F);
		player.setLevel(0); player.setExp(0);
		player.setLevel((int)arena.expStore.get(player)[0]);
		player.setExp(arena.expStore.get(player)[1]);
		for(Entity en : player.getWorld().getEntities()) if(en.hasMetadata(player.getName())) en.remove();
	}
}
