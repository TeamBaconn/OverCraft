
package com.Tuong.Heros;

import java.util.Arrays;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
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

public class Genji implements Listener{
	private Player player;
	private Arena arena;
	private int ammo;
	private float ultimate_charge;
	private double shootdamage,maxREGENERATIONth;
	private boolean start,msg,reloading,reflect,ultimate,shoot;
	public Genji(Player player, Arena arena){
		player.setAllowFlight(true);
		player.getInventory().clear();
		player.getInventory().setHeldItemSlot(8);
		player.getInventory().setItem(8,getShuriken());
		if(arena.team.get(player).equals("BLUE")) player.getInventory().setChestplate(arena.getBlueChestplate());
		else player.getInventory().setChestplate(arena.getRedChestplate());
		this.player = player;
		this.arena = arena;
		this.start = true;
		this.reloading= false;
		this.reflect = false;
		this.ultimate = false;
		this.shoot = false;
		this.ammo = 20;
		this.shootdamage = 5.6;
		this.maxREGENERATIONth = 20;
		player.setMaxHealth(maxREGENERATIONth);
		player.setHealth(player.getMaxHealth());
		Bukkit.getPluginManager().registerEvents(this, Core.plugin);
		if(Core.t) player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_GHAST_HURT, 1, 1);
		recharge();
	}
	private ItemStack getShuriken(){
        ItemStack shuriken = new ItemStack(Material.STICK, 1);
        ItemMeta meta = shuriken.getItemMeta();
        shuriken.setItemMeta(meta);
        meta.setDisplayName(ChatColor.BOLD + "" + ChatColor.GRAY + "Shuriken");
        meta.setLore(Arrays.asList(ChatColor.GOLD + "Type: " + ChatColor.GRAY + "" + ChatColor.ITALIC + "Linear Projectile",
                ChatColor.GOLD + "Damage: " + ChatColor.GRAY + "" + ChatColor.ITALIC + "28 Per Projectile | 3 Projectiles/Shot",
                ChatColor.GOLD + "Projectile speed: " + ChatColor.GRAY + "" + ChatColor.ITALIC + "47m/Second",
                ChatColor.GOLD + "Rate of fire: " + ChatColor.GRAY + "" + ChatColor.ITALIC + "1 Shot/Second",
                ChatColor.GOLD + "Ammo: " + ChatColor.GRAY + "" + ChatColor.ITALIC + "24",
                ChatColor.GOLD + "Reload time: " + ChatColor.GRAY + "" + ChatColor.ITALIC + "1 Second",
                ChatColor.LIGHT_PURPLE + "Description: " + ChatColor.RED + "" + ChatColor.ITALIC + "Genji looses three deadly throwing stars in quick succession!"));
        shuriken.setItemMeta(meta);
        return shuriken;
    }
	
	public boolean isReflect(){
		return this.reflect;
	}
	@EventHandler
	public void pick(PlayerPickupArrowEvent e){
		if(e.getPlayer().equals(player))e.setCancelled(true);
	}
	@EventHandler
	public void pick(PlayerPickupItemEvent e){
		if(e.getPlayer().equals(player))e.setCancelled(true);
	}
	@EventHandler
	public void bomb(PlayerDropItemEvent e){
		if(e.getPlayer().equals(player)){
			e.setCancelled(true);
			if(e.getPlayer().equals(player) && arena.death.contains(player) && reflect) return;
			if(ultimate_charge == 1){
				ultimate_charge = 0;
				if(Core.t) player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_GHAST_SCREAM, 1, 1);
				shootdamage += 8; ultimate = true;
				player.getInventory().clear();
				player.getInventory().setItem(8,getItemName());
				Core.playParticle(EnumParticle.VILLAGER_HAPPY, player.getEyeLocation(), 10);
				new BukkitRunnable() {
					@Override
					public void run() {
						ultimate = false;
						shootdamage -= 8;
						player.getInventory().clear();
						player.getInventory().setItem(8,getShuriken());
					}
				}.runTaskLater(Core.plugin, 160);
			}
		}
	}
	@EventHandler
	public void respawn(PlayerRespawnEvent e){
		if(!e.getPlayer().equals(player)) return;
		player.setAllowFlight(true);
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
						if(Core.t) player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_GHAST_WARN, 1, 1);
						msg = true;
					}
				}else ultimate_charge += 0.01;
				if(start) player.setLevel(ammo);
				if(start) player.setExp(ultimate_charge);
			}
		}.runTaskTimer(Core.plugin, 0, 52);
	}
	private ItemStack getItemName(){
        ItemStack blade = new ItemStack(Material.IRON_SWORD, 1);
        ItemMeta meta = blade.getItemMeta();
        blade.setItemMeta(meta);
        meta.setDisplayName(ChatColor.BOLD + "" + ChatColor.GRAY + "Dragon Blade");
        meta.setLore(Arrays.asList(ChatColor.GOLD + "Type: " + ChatColor.GRAY + "" + ChatColor.ITALIC + "Melee",
                ChatColor.GOLD + "Damage: " + ChatColor.GRAY + "" + ChatColor.ITALIC + "120/Swing",
                ChatColor.GOLD + "Movement speed: " + ChatColor.GRAY + "" + ChatColor.ITALIC + "8m/Second",
                ChatColor.GOLD + "Maximum range: " + ChatColor.GRAY + "" + ChatColor.ITALIC + "5m",
                ChatColor.GOLD + "Rate of fire: " + ChatColor.GRAY + "" + ChatColor.ITALIC + "1 Swing/Second",
                ChatColor.GOLD + "Duration: " + ChatColor.GRAY + "" + ChatColor.ITALIC + "8 Seconds",
                ChatColor.GOLD + "Charge required: " + ChatColor.GRAY + "" + ChatColor.ITALIC + "1200 Points",
                ChatColor.LIGHT_PURPLE + "Description: " + ChatColor.RED + "" + ChatColor.ITALIC + "Genji brandishes his katana for a brief period of time. Until he sheathes his sword, Genji can deliver killing strikes to any targets within his reach!"));
        blade.setItemMeta(meta);
        return blade;
	}
	@EventHandler
	public void explode(EntityExplodeEvent e){
		if(e.getEntity().hasMetadata(player.getName())){
			e.blockList().clear();
		}
	}
	@EventHandler
	public void player(PlayerMoveEvent e){
		if(!e.getPlayer().equals(player)) return;
		if (!player.getAllowFlight()) {
            Location loc = player.getLocation();
            Block block = loc.getBlock().getRelative(BlockFace.DOWN);
            if (block.getType() != Material.AIR) {
                player.setAllowFlight(true);
            }
        }
		if(e.getPlayer().equals(player) && player.getLocation().getPitch() >= -60 && player.getLocation().getPitch() <= -40){
			Block targetblock = player.getTargetBlock((Set<Material>)null, 1);
			Block targetblockunder = targetblock.getWorld().getBlockAt(targetblock.getLocation().add(0,-1,0));
			if(!targetblock.isLiquid() && targetblock.getType() != Material.AIR && !targetblockunder.isLiquid() && targetblockunder.getType() != Material.AIR){
				player.setVelocity(new Vector(0,0.5,0));
			}
		}
	}
	@EventHandler
	public void kill(EntityDeathEvent e){
		if(e.getEntity().getKiller() != null && e.getEntity().getKiller().equals(player) && !e.getEntity().equals(player)) {
			player.removePotionEffect(PotionEffectType.UNLUCK);
			if(ultimate_charge + 0.4 >= 1){
				if(start) player.setLevel(ammo);
				ultimate_charge = 1;
				if(msg == false){
					Core.sendTitle(player, 5, 51, 1, ChatColor.GOLD+"ULTIMATE", ChatColor.GREEN+"Ready to launch");
					if(Core.t) player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_GHAST_WARN, 1, 1);
					msg = true;
				}
			}else ultimate_charge += 0.4;
			if(start) player.setExp(ultimate_charge);
		}
	}
	@EventHandler
	public void shoot(PlayerInteractEvent e){
		if(e.getPlayer().equals(player) && (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR)){
			if(e.getPlayer().equals(player) && (arena.death.contains(player) || reflect || ultimate || shoot || reloading)) {
				e.setCancelled(true);
				return;
			}
			e.setCancelled(true);
			if(ammo > 0){
				shoot = true;
				new BukkitRunnable() {
					int t = 3;
					@Override
					public void run() {
						if(t == 0) {
							shoot = false;
							this.cancel(); 
						}else t--;
						Snowball snow = player.getWorld().spawn(player.getEyeLocation().add(0,0.3,0), Snowball.class);
						snow.setVelocity(player.getLocation().getDirection().multiply(3));
						snow.setShooter(player);
						snow.setMetadata(player.getName(), new FixedMetadataValue(Core.plugin, "Genji"));
					}
				}.runTaskTimer(Core.plugin, 0, 3);
				ammo-=3;
				if(start) player.setLevel(ammo);
			}else{
				e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_OFF, 1, 1);
				if(reloading) return;
				reloading = true;
				new BukkitRunnable() {
					@Override
					public void run() {
						e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
						ammo = 24;
						if(start) player.setLevel(ammo);
						reloading = false;
					}
				}.runTaskLater(Core.plugin, 25);
			}
		}else if(e.getPlayer().equals(player) && (e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_AIR)){
			if(e.getPlayer().equals(player) && (arena.death.contains(player) || reflect || ultimate || shoot || reloading)) {
				e.setCancelled(true);
				return;
			}
			if(ammo > 0){
				int[] t = {-8,0,8};
				for(int i = 0; i < 3; i++){
					Snowball snow = player.getWorld().spawn(player.getEyeLocation().add(0,0.3,0), Snowball.class);
					snow.setVelocity(getDirection(player.getLocation().getYaw()+t[i], player.getLocation().getPitch()).multiply(3.5));
					snow.setShooter(player);
					snow.setMetadata(player.getName(), new FixedMetadataValue(Core.plugin, "Genji"));
				}
				ammo-=3;
				if(start) player.setLevel(ammo);
			}else{
				e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_OFF, 1, 1);
				if(reloading) return;
				reloading = true;
				new BukkitRunnable() {
					@Override
					public void run() {
						e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
						ammo = 24;
						if(start) player.setLevel(ammo);
						reloading = false;
					}
				}.runTaskLater(Core.plugin, 25);
			}
		}
	}
	@EventHandler
	public void antiSwap(PlayerSwapHandItemsEvent e){
		e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_OFF, 1, 1);
		if(reloading) return;
		reloading = true;
		new BukkitRunnable() {
			@Override
			public void run() {
				e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
				ammo = 24;
				if(start) player.setLevel(ammo);
				reloading = false;
			}
		}.runTaskLater(Core.plugin, 25);
	}
	@EventHandler
	public void click(InventoryClickEvent e){
		if(e.getWhoClicked().equals(player)) e.setCancelled(true);
	}
	public Vector getDirection(double yaw, double pitch) {
		yaw = yaw % 360;
        Vector vector = new Vector();

        double rotX = yaw;
        double rotY = pitch;

        vector.setY(-Math.sin(Math.toRadians(rotY)));

        double xz = Math.cos(Math.toRadians(rotY));

        vector.setX(-xz * Math.sin(Math.toRadians(rotX)));
        vector.setZ(xz * Math.cos(Math.toRadians(rotX)));

        return vector;
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
				if(ultimate == false) player.getInventory().setItemInOffHand(getItemName());
				reflect = true;
				new BukkitRunnable() {
					@Override
					public void run() {
						player.getInventory().setItemInOffHand(null);
						reflect = false;
					}
				}.runTaskLater(Core.plugin, 40);
			}
		}
	}
	
	@EventHandler
	public void changebooster(PlayerToggleSneakEvent e){
		if(e.getPlayer().equals(player)){
			if(e.getPlayer().equals(player) && arena.death.contains(player)) return;
			if(!player.hasPotionEffect(PotionEffectType.UNLUCK)){
				player.addPotionEffect(new PotionEffect(PotionEffectType.UNLUCK, 160, 1));
				Entity dam = Core.getNearestEntityInSight(e.getPlayer(), 14);
				if(dam != null) ((LivingEntity) dam).addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 2));
				player.setVelocity(player.getLocation().getDirection().multiply(3));
			}
		}
	}
	@EventHandler
	public void fall(EntityDamageEvent e){
		if(e.getEntity().equals(player) && e.getCause() == DamageCause.FALL) e.setCancelled(true);
	}
	@EventHandler
	public void nodamage(EntityDamageByEntityEvent e){
		if(e.getDamager().hasMetadata(player.getName()) && e.getEntity() instanceof Player){
			if(arena.isAlly(player, (Player)e.getEntity())) e.setCancelled(true);
			else e.setDamage(shootdamage);
		}else if(e.getDamager().equals(player) && e.getEntity() instanceof Player){
			if(arena.isAlly(player, (Player)e.getEntity())) e.setCancelled(true);
			else e.setDamage(shootdamage);
		}
		if(e.getEntity().equals(player) && reflect){
			if(e.getDamager() instanceof Projectile){
				if(e.getDamager() instanceof Snowball){
					Snowball snow = player.getWorld().spawn(player.getEyeLocation().add(0,0.5,0), Snowball.class);
					snow.setVelocity(player.getLocation().getDirection().multiply(3));
					snow.setShooter(player);
					snow.setMetadata(player.getName(), new FixedMetadataValue(Core.plugin, "Genji"));
				}else if(e.getDamager() instanceof Egg){
					Egg snow = player.getWorld().spawn(player.getEyeLocation().add(0,0.5,0), Egg.class);
					snow.setVelocity(player.getLocation().getDirection().multiply(3));
					snow.setShooter(player);
					snow.setMetadata(player.getName(), new FixedMetadataValue(Core.plugin, "Genji"));
				}else if(e.getDamager() instanceof Arrow){
					Arrow snow = player.getWorld().spawn(player.getEyeLocation().add(0,0.5,0), Arrow.class);
					snow.setVelocity(player.getLocation().getDirection().multiply(3));
					snow.setShooter(player);
					snow.setMetadata(player.getName(), new FixedMetadataValue(Core.plugin, "Genji"));
				}
				e.getDamager().remove();
				e.setCancelled(true);
				Core.playParticle(EnumParticle.EXPLOSION_NORMAL, player.getEyeLocation(), 1);
			}
		}
	}
	
	@EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
		if(!event.getPlayer().equals(player)) return;
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.ADVENTURE) {
            player.setFlying(false);
            player.setVelocity(player.getVelocity().add(new Vector(0,0.7,0)));
            player.setAllowFlight(false);
            event.setCancelled(true);
        }
    }
	
	public void stop(){
		start = false;
		HandlerList.unregisterAll(this);
		player.setLevel(0); player.setExp(0);
		player.setLevel((int)arena.expStore.get(player)[0]);
		player.setExp(arena.expStore.get(player)[1]);
		player.setAllowFlight(false);
		for(Entity en : player.getWorld().getEntities()) if(en.hasMetadata(player.getName())) en.remove();
	}
}
