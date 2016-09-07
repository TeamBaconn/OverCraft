package com.Tuong.Heros;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.Tuong.Arena.Arena;
import com.Tuong.OverCraftCore.Core;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_9_R2.EnumParticle;

public class Lucio implements Listener{
	private Player player;
	private Arena arena;
	private int ammo,maxREGENERATIONth;
	private float ultimate_charge;
	private double shootdamage;
	private boolean start,msg,reloading,speed,shift,boost,shoot;
	public Lucio(Player player, Arena arena){
		player.getInventory().clear();
		player.getInventory().setHeldItemSlot(8);
		player.getInventory().setItemInMainHand(getItemName());
		if(arena.team.get(player).equals("BLUE")) player.getInventory().setChestplate(arena.getBlueChestplate());
		else player.getInventory().setChestplate(arena.getRedChestplate());
		if(Core.t)player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_CHICKEN_AMBIENT, 1, 1);
		this.player = player;
		this.arena = arena;
		this.start = true;
		this.reloading= false;
		this.speed = true;
		this.shift = false;
		this.boost = false;
		this.shoot = false;
		this.ammo = 20;
		this.shootdamage = 2.5;
		this.maxREGENERATIONth = 20;
		player.setMaxHealth(maxREGENERATIONth);
		player.setHealth(player.getMaxHealth());
		Bukkit.getPluginManager().registerEvents(this, Core.plugin);
		recharge();
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
	public void bomb(PlayerDropItemEvent e){
		if(e.getPlayer().equals(player)){
			e.setCancelled(true);
			if(e.getPlayer().equals(player) && arena.death.contains(player)) return;
			if(ultimate_charge == 1 && e.getPlayer().isOnGround()){
				ultimate_charge = 0;
				playWitchEffect(player.getLocation());
				player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 200, 10));
				if(Core.t) player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_CHICKEN_EGG, 1, 1);
				for(Entity en : player.getNearbyEntities(15, 15, 15)) if(en instanceof Player && arena.playerList.containsKey((Player)en) && arena.isAlly((Player)en, player)){
					((Player)en).addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 200, 10));
				}
			}
		}
	}
	public static void playWitchEffect(Location loc){
		new BukkitRunnable(){
            double t = Math.PI/4;
            public void run(){
                    t = t + 0.1*Math.PI;
                    for (double theta = 0; theta <= 2*Math.PI; theta = theta + Math.PI/32){
                            double x = t*Math.cos(theta);
                            double y = 2*Math.exp(-0.1*t) * Math.sin(t) + 1.5;
                            double z = t*Math.sin(theta);
                            loc.add(x,y,z);
                            Core.playParticle(EnumParticle.VILLAGER_HAPPY, loc, 1);
                            loc.subtract(x,y,z);
                            theta = theta + Math.PI/64;
                            x = t*Math.cos(theta);
                            y = 2*Math.exp(-0.1*t) * Math.sin(t) + 1.5;
                            z = t*Math.sin(theta);
                            loc.add(x,y,z);
                            Core.playParticle(EnumParticle.CRIT_MAGIC, loc, 1);
                            loc.subtract(x,y,z);
                    }
                    if (t > 20){
                            this.cancel();
                    }
            }    
		}.runTaskTimer(Core.plugin, 0, 1);
	}
	@EventHandler
	public void respawn(PlayerRespawnEvent e){
		if(!e.getPlayer().equals(player)) return;
		player.setMaxHealth(maxREGENERATIONth);
		player.setMaxHealth(player.getMaxHealth());
	}
	
	public void recharge(){
		new BukkitRunnable() {
			@Override
			public void run() {
				if(start == false) this.cancel();
				if(ultimate_charge + 0.01 >= 1){
					ultimate_charge = 1;
					if(msg == false){
						Core.sendTitle(player, 5, 51, 1, ChatColor.GOLD+"ULTIMATE", ChatColor.GREEN+"Ready to launch");
						msg = true;
					}
				}else ultimate_charge += 0.01;
				if(start) player.setLevel(ammo);
				if(start) player.setExp(ultimate_charge);
				int t = 0,d = 0;
				if(boost){
					boost = false;
					t = 4;
					d = 28;
				}
				if(speed) player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 52+d, 0+t));
				else player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 52+d, 0+t));
				for(Entity en : player.getNearbyEntities(15, 15, 15)) if(en instanceof Player && arena.playerList.containsKey((Player)en) && arena.isAlly((Player)en, player)){
					if(speed) {
						((Player)en).addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 52+d, 0+t));
					} else {
						((Player)en).addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 52+d, 0+t));
					}
					if(ultimate_charge + 0.002 >= 1){
						ultimate_charge = 1;
						if(msg == false){
							Core.sendTitle(player, 5, 51, 1, ChatColor.GOLD+"ULTIMATE", ChatColor.GREEN+"Ready to launch");
							msg = true;
						}
					}else ultimate_charge += 0.002;
				}
			}
		}.runTaskTimer(Core.plugin, 0, 52);
	}
	private ItemStack getItemName(){
        ItemStack soundgun = new ItemStack(Material.STONE_SPADE, 1);
        ItemMeta meta = soundgun.getItemMeta();
        soundgun.setItemMeta(meta);
        meta.spigot().setUnbreakable(true);
        meta.setDisplayName(ChatColor.BOLD + "" + ChatColor.GRAY + "Sonic Amplifier");
        meta.setLore(Arrays.asList(ChatColor.GOLD + "Type: " + ChatColor.GRAY + "" + ChatColor.ITALIC + "Linear projectile",
                ChatColor.GOLD + "Damage: " + ChatColor.GRAY + "" + ChatColor.ITALIC + "16 per projectile/4 projectiles per shot",
                ChatColor.GOLD + "Projectile speed: " + ChatColor.GRAY + "" + ChatColor.ITALIC + " 33.33 meters per second",
                ChatColor.GOLD + "Ammo usage: " + ChatColor.GRAY + "" + ChatColor.ITALIC + " 4 rounds per shot",
                ChatColor.GOLD + "Rate of fire: " + ChatColor.GRAY + "" + ChatColor.ITALIC + "1 shot per second",
                ChatColor.GOLD + "Ammo: " + ChatColor.GRAY + "" + ChatColor.ITALIC + "20",
                ChatColor.LIGHT_PURPLE + "Description: " + ChatColor.RED + "" + ChatColor.ITALIC + "Lúcio can hit his enemies with sonic projectiles!"));
        soundgun.setItemMeta(meta);
        return soundgun;
    }
	@EventHandler
	public void explode(EntityExplodeEvent e){
		if(e.getEntity().hasMetadata(player.getName())){
			e.blockList().clear();
		}
	}
	@EventHandler
    public void onPlayerMove(PlayerMoveEvent event)
    {
      if(!event.getPlayer().equals(this.player)) return;
      if(event.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR)
      if (player.getEyeLocation().getBlock().getRelative(BlockFace.NORTH)
        .getType() != Material.AIR)
      {
        if (player.getEyeLocation().getBlock()
          .getRelative(BlockFace.NORTH).getType() != Material.WATER) {
          if (player.getEyeLocation().getBlock()
            .getRelative(BlockFace.NORTH).getType() != Material.STATIONARY_WATER) {
              player.setVelocity(player.getEyeLocation()
                .getDirection().multiply(0.65D));
          }
        }
      }
      else if (player.getEyeLocation().getBlock().getRelative(BlockFace.SOUTH).getType() != Material.AIR)
      {
        if (player.getEyeLocation().getBlock()
          .getRelative(BlockFace.SOUTH).getType() != Material.WATER) {
          if (player.getEyeLocation().getBlock()
            .getRelative(BlockFace.SOUTH).getType() != Material.STATIONARY_WATER) {
              player.setVelocity(player.getEyeLocation()
                .getDirection().multiply(0.65D));
          }
        }
      }
      else if (player.getEyeLocation().getBlock().getRelative(BlockFace.EAST).getType() != Material.AIR)
      {
        if (player.getEyeLocation().getBlock().getRelative(BlockFace.EAST)
          .getType() != Material.WATER) {
          if (player.getEyeLocation().getBlock()
            .getRelative(BlockFace.EAST).getType() != Material.STATIONARY_WATER) {
              player.setVelocity(player.getEyeLocation()
                .getDirection().multiply(0.65D));
          }
        }
      } 	
      else if ((player.getEyeLocation().getBlock().getRelative(BlockFace.WEST).getType() != Material.AIR) && 
        (player.getEyeLocation().getBlock().getRelative(BlockFace.WEST)
        .getType() != Material.WATER)) {
        if (player.getEyeLocation().getBlock().getRelative(BlockFace.WEST).getType() != Material.STATIONARY_WATER) {
            player.setVelocity(player.getEyeLocation()
              .getDirection().multiply(0.65D));
          }
      }
    }
	@EventHandler
	public void kill(EntityDeathEvent e){
		if(e.getEntity().getKiller() != null && e.getEntity().getKiller().equals(player) && !e.getEntity().equals(player)) {
			if(ultimate_charge + 0.4 >= 1){
				if(start) player.setLevel(ammo);
				ultimate_charge = 1;
				if(msg == false){
					Core.sendTitle(player, 5, 51, 1, ChatColor.GOLD+"ULTIMATE", ChatColor.GREEN+"Ready to launch");
					msg = true;
				}
			}else ultimate_charge += 0.4;
			if(start) player.setExp(ultimate_charge);
		}
	}
	@EventHandler
	public void shoot(PlayerInteractEvent e){
		if(e.getPlayer().equals(player) && (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR)){
			if(e.getPlayer().equals(player) && (arena.death.contains(player) || shoot)) {
				e.setCancelled(true);
				return;
			}
			e.setCancelled(true);
			if(ammo > 0){
				shoot = true;
				new BukkitRunnable() {
					int t = 4;
					@Override
					public void run() {
						if(t == 0) {
							this.cancel();
							shoot = false;
						}else t--;
						Snowball snow = player.getWorld().spawn(player.getEyeLocation().add(0,0.5,0), Snowball.class);
						snow.setVelocity(player.getLocation().getDirection().multiply(3.5));
						snow.setShooter(player);
						snow.setMetadata(player.getName(), new FixedMetadataValue(Core.plugin, "Lucio"));
						player.playSound(player.getLocation(), Sound.BLOCK_SLIME_HIT, 1, 1);
					}
				}.runTaskTimer(Core.plugin, 0, 3);
				ammo-=4;
				if(start) player.setLevel(ammo);
			}else{
				e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_OFF, 1, 1);
				if(reloading) return;
				reloading = true;
				new BukkitRunnable() {
					@Override
					public void run() {
						e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
						ammo = 20;
						if(start) player.setLevel(ammo);
						reloading = false;
					}
				}.runTaskLater(Core.plugin, 25);
			}
		}
	}
	@EventHandler
	public void rekall(PlayerItemHeldEvent e){
		if(e.getPlayer().equals(player)){
			e.setCancelled(true);
			if(e.getPlayer().equals(player) && arena.death.contains(player)){
				e.setCancelled(true);
				return;
			}
			if(!player.hasPotionEffect(PotionEffectType.LUCK)){
				player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 240, 1));
				Core.sendTitle(player, 10, 30, 10, ChatColor.LIGHT_PURPLE+""+ChatColor.BOLD+"!UP!", "");
				boost = true;
			}
		}
	}
	
	public boolean boosted(){
		return boost;
	}
	@EventHandler
	public void changebooster(PlayerToggleSneakEvent e){
		if(e.getPlayer().equals(player)){
			if(e.getPlayer().equals(player) && arena.death.contains(player)) return;
			if(shift) {
				shift= false;
				return;
			} else shift = true;
			if(speed) {
				speed = false; 
				Core.sendTitle(player, 10, 30, 10, "", ChatColor.YELLOW+""+ChatColor.BOLD+"Heal");
				if(Core.t) player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_CHICKEN_STEP, 1, 1);
			}else {
				speed = true;
				Core.sendTitle(player, 10, 30, 10, "", ChatColor.GREEN+""+ChatColor.BOLD+"Speed");
				if(Core.t) player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_CHICKEN_HURT, 1, 1);
			}
		}
	}
	@EventHandler
	public void fall(EntityDamageEvent e){
		if(e.getEntity().equals(player) && e.getCause() == DamageCause.FALL) e.setCancelled(true);
	}
	@EventHandler
	public void nodamage(EntityDamageByEntityEvent e){
		if(e.getDamager().equals(player) && e.getEntity() instanceof Player) {
			if(arena.isAlly(player, (Player)e.getEntity())) e.setCancelled(true);
			else if(!player.hasPotionEffect(PotionEffectType.UNLUCK)){
				player.addPotionEffect(new PotionEffect(PotionEffectType.UNLUCK, 80, 1));
				e.setDamage(5);
				e.getEntity().setVelocity(e.getDamager().getLocation().getDirection().multiply(2.5));
			}
		}
		if(e.getDamager().hasMetadata(player.getName()) && e.getEntity() instanceof Player){
			if(arena.isAlly(player, (Player)e.getEntity())) e.setCancelled(true);
			else e.setDamage(shootdamage);
		}
	}
	public void stop(){
		start = false;
		HandlerList.unregisterAll(this);
		player.setLevel(0); player.setExp(0);
		player.setLevel((int)arena.expStore.get(player)[0]);
		player.setExp(arena.expStore.get(player)[1]);
		for(Entity en : player.getWorld().getEntities()) if(en.hasMetadata(player.getName())) en.remove();
	}
}
