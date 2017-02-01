package com.Tuong.Heros;

import java.util.Arrays;
import java.util.Set;

import org.bukkit.Achievement;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftEnderDragon;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftLivingEntity;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TippedArrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerAchievementAwardedEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.Tuong.Arena.Arena;
import com.Tuong.OverCraftCore.Core;

import net.md_5.bungee.api.ChatColor;

public class Hanzo implements Listener{
	private Player player;
	private Arena arena;
	private float ultimate_charge;
	private double shootdamage;
	private int maxhealth;
	private boolean start,msg,swap;
	public Hanzo(Player player, Arena arena){
		player.removeAchievement(Achievement.OPEN_INVENTORY);
		player.getInventory().clear();
		player.getInventory().setHeldItemSlot(8);
		player.getInventory().setItem(0, new ItemStack(Material.ARROW));
		player.getInventory().setItemInMainHand(getItemName());
		if(arena.team.get(player).equals("BLUE")) player.getInventory().setChestplate(arena.getBlueChestplate());
		else player.getInventory().setChestplate(arena.getRedChestplate());
		if(Core.t) player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_CAT_AMBIENT, 1, 1);
		this.player = player;
		this.arena = arena;
		this.start = true; this.swap = false;
		this.ultimate_charge = 0;
		this.shootdamage = 8;
		this.maxhealth = 20;
		player.setMaxHealth(maxhealth);
		player.setHealth(player.getMaxHealth());
		Bukkit.getPluginManager().registerEvents(this, Core.plugin);
		recharge();
	}
	
	@EventHandler
	public void respawn(PlayerRespawnEvent e){
		if(!e.getPlayer().equals(player)) return;
		player.setMaxHealth(maxhealth);
		player.setHealth(player.getMaxHealth());
	}
	
	@EventHandler
	public void bomb(PlayerDropItemEvent e){
		if(e.getPlayer().equals(player)){
			e.setCancelled(true);
			if(e.getPlayer().equals(player) && arena.death.contains(player)) return;
			if(ultimate_charge == 1){
				if(player.getInventory().getItem(0).equals(getUtiArrow())) player.getInventory().setItem(0, new ItemStack(Material.ARROW));
				else player.getInventory().setItem(0, getUtiArrow());
			}
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
	@EventHandler
	public void player(PlayerMoveEvent e){
		if(!e.getPlayer().equals(player)) return;
		if(e.getPlayer().equals(player) && player.getLocation().getPitch() >= -60 && player.getLocation().getPitch() <= -40){
			Block targetblock = player.getTargetBlock((Set<Material>)null, 1);
			Block targetblockunder = targetblock.getWorld().getBlockAt(targetblock.getLocation().add(0,-1,0));
			if(!targetblock.isLiquid() && targetblock.getType() != Material.AIR && !targetblockunder.isLiquid() && targetblockunder.getType() != Material.AIR){
				player.setVelocity(new Vector(0,0.5,0));
			}
		}
	}
	
	public void recharge(){
		new BukkitRunnable() {
			@Override
			public void run() {
				if(start == false) this.cancel();
				if(ultimate_charge + 0.01 >= 1){
					ultimate_charge = 1;
					if(msg == false){
						if(Core.t) player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_CAT_HISS, 1, 1);
						Core.sendTitle(player, 5, 51, 1, ChatColor.GOLD+"ULTIMATE", ChatColor.GREEN+"Ready to launch");
						msg = true;
					}
				}else ultimate_charge += 0.01;
				if(start) player.setLevel(1);
				if(start) player.setExp(ultimate_charge);
			}
		}.runTaskTimer(Core.plugin, 0, 52);
	}
	private ItemStack getItemName(){
		ItemStack bow = new ItemStack(Material.BOW, 1);
		ItemMeta meta = bow.getItemMeta();
		meta.spigot().setUnbreakable(true);
		bow.setItemMeta(meta);
		meta.setDisplayName(ChatColor.BOLD + "" + ChatColor.GRAY + "Storm Bow");
		meta.setLore(Arrays.asList(ChatColor.GOLD + "Type: " + ChatColor.GRAY + "" + ChatColor.ITALIC + "Arcing Projectile",
				ChatColor.GOLD + "Damage: " + ChatColor.GRAY + "" + ChatColor.ITALIC +"Fully charged: 125 | Uncharged: 29",
				ChatColor.GOLD + "Projectile speed: " + ChatColor.GRAY + "" + ChatColor.ITALIC + "Fully charged: 66.66 m/s | Uncharged: 26 m/s",
				ChatColor.GOLD + "Rate of fire: " + ChatColor.GRAY + "" + ChatColor.ITALIC + "0.5 Second Charging | 0.5 Second Delay",
				ChatColor.GOLD + "Ammo: " + ChatColor.GRAY + "" + ChatColor.ITALIC + "∞",
				ChatColor.LIGHT_PURPLE + "Description: " + ChatColor.RED + "" + ChatColor.ITALIC + "Hanzo nocks and fires an arrow at his target!"));
		bow.setItemMeta(meta);
		return bow;
	}
	
	private ItemStack getUtiArrow(){
		ItemStack arrow = new ItemStack(Material.TIPPED_ARROW);
		PotionMeta meta = (PotionMeta) arrow.getItemMeta();
		meta.addCustomEffect(new PotionEffect(PotionEffectType.HARM, 0, 1), true);
		arrow.setItemMeta(meta);
		return arrow;
	}
	
	@EventHandler
	public void kill(EntityDeathEvent e){
		if(e.getEntity().getKiller() != null && e.getEntity().getKiller().equals(player) && !e.getEntity().equals(player)) {
			if(ultimate_charge + 0.4 >= 1){
				ultimate_charge = 1;
				if(msg == false){
					if(Core.t) player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_CAT_HISS, 1, 1);
					Core.sendTitle(player, 5, 51, 1, ChatColor.GOLD+"ULTIMATE", ChatColor.GREEN+"Ready to launch");
					msg = true;
				}
			}else ultimate_charge += 0.4;
			if(start)player.setExp(ultimate_charge);
		}
	}
	
	private ItemStack getMultiArrow(){
		ItemStack arrow = new ItemStack(Material.TIPPED_ARROW);
		PotionMeta meta = (PotionMeta) arrow.getItemMeta();
		meta.addCustomEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 0, 1), true);
		arrow.setItemMeta(meta);
		return arrow;
	}
	
	private ItemStack getSneakArrow(){
		ItemStack arrow = new ItemStack(Material.TIPPED_ARROW);
		PotionMeta meta = (PotionMeta) arrow.getItemMeta();
		meta.addCustomEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 0, 1), true);
		arrow.setItemMeta(meta);
		return arrow;
	}
	@EventHandler
	public void explode(EntityExplodeEvent e){
		if(e.getEntity().hasMetadata(player.getName())){
			e.blockList().clear();
		}
	}
	
	@EventHandler
	public void sneak(PlayerToggleSneakEvent e){
		if(e.getPlayer().equals(player) && arena.death.contains(player)) return;
		if(e.getPlayer().equals(player)){
			if(e.getPlayer().hasPotionEffect(PotionEffectType.UNLUCK)) return;
			if(player.getInventory().getItem(0).equals(getSneakArrow())) player.getInventory().setItem(0, new ItemStack(Material.ARROW));
			else player.getInventory().setItem(0, getSneakArrow());
		}
	}
	
	@EventHandler
	public void shoot(ProjectileLaunchEvent e){
		if(e.getEntity().getShooter() != null && e.getEntity().getShooter().equals(player)){
			if(arena.death.contains(player)){
				e.setCancelled(true);
				return;
			}
			if(player.getInventory().getItem(0).equals(getUtiArrow())){
				e.setCancelled(true);
				if(Core.t) player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_CAT_PURREOW, 1, 1);
				player.getInventory().setItem(0, new ItemStack(Material.ARROW));
				ultimate_charge = 0;
				Vector dir = player.getLocation().getDirection();
				EnderDragon ender = e.getEntity().getWorld().spawn(e.getEntity().getLocation(), EnderDragon.class);
				ender.setHealth(100);
				ender.setMetadata(player.getName(), new FixedMetadataValue(Core.plugin, "Hanzo"));
				ender.setAI(true);
				float yaw = player.getLocation().getYaw();
				float pitch = player.getLocation().getPitch();
				e.setCancelled(true);
				new BukkitRunnable() {
					int t = 0;
					@Override
					public void run() {
						if(t == 100) {
							ender.remove();
							this.cancel();
						}
						for(Entity en : ender.getNearbyEntities(5, 3, 5)) if(en instanceof Player && !arena.isAlly((Player)en, player)){
							((CraftLivingEntity) en).damage(0.2,player);
						}
						t++;
						ender.teleport(ender.getLocation().add(dir));
						((CraftEnderDragon)ender).getHandle().setPositionRotation(ender.getLocation().getX(), ender.getLocation().getY(), ender.getLocation().getZ(), yaw-180, pitch);;
					}
				}.runTaskTimer(Core.plugin, 5, 2);
				return;
			}
			player.getInventory().setItem(0, new ItemStack(Material.ARROW));
			e.getEntity().setMetadata(player.getName(), new FixedMetadataValue(Core.plugin, "Hanzo"));
			if(e.getEntity() instanceof TippedArrow && ((TippedArrow)e.getEntity()).hasCustomEffect(PotionEffectType.NIGHT_VISION))player.addPotionEffect(new PotionEffect(PotionEffectType.UNLUCK, 400, 1));
			if(e.getEntity() instanceof TippedArrow && ((TippedArrow)e.getEntity()).hasCustomEffect(PotionEffectType.INCREASE_DAMAGE))player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 200, 1));
		}
	}
	@EventHandler
	public void fall(EntityDamageEvent e){
		if(e.getEntity().equals(player) && e.getCause() == DamageCause.FALL) e.setCancelled(true);
	}
	
	@EventHandler
	public void hit(ProjectileHitEvent e){
		if(e.getEntity().hasMetadata(player.getName()) && e.getEntity() instanceof TippedArrow){
			if(((TippedArrow)e.getEntity()).hasCustomEffect(PotionEffectType.NIGHT_VISION)){
				//if(e.getEntity() instanceof TippedArrow) ((TippedArrow)e.getEntity()).clearCustomEffects();
				for(Entity en : e.getEntity().getNearbyEntities(10, 10, 10)){
	    			if(en instanceof LivingEntity && !en.equals(player) && !(en instanceof ArmorStand)){
	    				if(en instanceof Player && arena.isAlly((Player)en, player)) return;
	    				((LivingEntity) en).addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 300, 2));
	    			}
	    		}
			}else if(((TippedArrow)e.getEntity()).hasCustomEffect(PotionEffectType.INCREASE_DAMAGE)){
				//if(e.getEntity() instanceof TippedArrow) ((TippedArrow)e.getEntity()).clearCustomEffects();
				for(int i = 0; i < 8; i++){
	    			Arrow arrow = e.getEntity().getWorld().spawn(e.getEntity().getLocation(), Arrow.class);
	    			arrow.setVelocity(Core.getRandomVelocity(false));
	    			arrow.setShooter(player);
	    			arrow.setCritical(true);
	    			arrow.setMetadata(player.getName(), new FixedMetadataValue(Core.plugin, "Hanzo"));
	    		}
			}
		}
	}
	
	@EventHandler
	public void rekall(PlayerAchievementAwardedEvent e){
		if(e.getPlayer().equals(player) && e.getAchievement().equals(Achievement.OPEN_INVENTORY)){
			e.setCancelled(true);
			e.getPlayer().closeInventory();
			if(e.getPlayer().equals(player) && arena.death.contains(player)) return;
			if(swap) {
				swap = false;
				return;
			}
			swap = true;
			if(e.getPlayer().hasPotionEffect(PotionEffectType.LUCK)) return;
			if(player.getInventory().getItem(0).equals(getMultiArrow())) player.getInventory().setItem(0, new ItemStack(Material.ARROW));
			else player.getInventory().setItem(0, getMultiArrow());
		}
	}
	@EventHandler
	public void nodamage(EntityDamageByEntityEvent e){
		if(e.getDamager().equals(player) && e.getEntity() instanceof Player) {
			if(arena.isAlly(player, (Player)e.getEntity())) e.setCancelled(true);
			else {
				int i = 0;
				if(e.getDamager() instanceof Arrow && ((Arrow)e.getDamager()).isCritical()) i+=2;
				if(e.getDamager() instanceof TippedArrow && ((TippedArrow)e.getDamager()).isCritical()) i+=3;
				e.setDamage(shootdamage+i);
			}
		}
		if(e.getDamager().hasMetadata(player.getName()) && e.getEntity() instanceof Player) {
			if(arena.isAlly(player, (Player)e.getEntity())) e.setCancelled(true);
			else {
				int i = 0;
				if(e.getDamager() instanceof Arrow && ((Arrow)e.getDamager()).isCritical()) i+=2;
				if(e.getDamager() instanceof TippedArrow && ((TippedArrow)e.getDamager()).isCritical()) i+=3;
				e.setDamage(shootdamage+i);
			}
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
