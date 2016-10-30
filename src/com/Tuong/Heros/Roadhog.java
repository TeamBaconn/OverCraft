package com.Tuong.Heros;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
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

public class Roadhog implements Listener{
	private Player player;
	private Arena arena;
	private int ammo,maxhealth;
	private float ultimate_charge;
	private double shootdamage;
	private boolean start,msg,reloading;
	private Bat pat;
	public Roadhog(Player player, Arena arena){
		player.getInventory().clear();
		player.getInventory().setHeldItemSlot(8);
		player.getInventory().setItemInMainHand(getItemName());
		if(arena.team.get(player).equals("BLUE")) player.getInventory().setChestplate(arena.getBlueChestplate());
		else player.getInventory().setChestplate(arena.getRedChestplate());
		if(Core.t) player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_PIG_AMBIENT, 1, 1);
		this.player = player;
		this.arena = arena;
		this.start = true;
		this.reloading= false;
		this.ammo = 4;
		this.shootdamage = 8;
		this.maxhealth = 60;
		this.pat = null;
		player.setMaxHealth(maxhealth);
		player.setHealth(player.getMaxHealth());
		Bukkit.getPluginManager().registerEvents(this, Core.plugin);
		recharge();
	}
	@EventHandler
	public void bomb(PlayerDropItemEvent e){
		if(e.getPlayer().equals(player)){
			e.setCancelled(true);
			if(e.getPlayer().equals(player) && arena.death.contains(player)) return;
			if(ultimate_charge == 1){
				if(Core.t) player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_PIG_DEATH, 1, 1);
				ultimate_charge = 0;
				new BukkitRunnable() {
					int t = 0;
					float accuracy = 0.1F;
					@Override
					public void run() {
						if(t == 20) this.cancel();
						for(int i = 0; i < 12; i++){
							Egg egg = player.getWorld().spawn(player.getEyeLocation(), Egg.class);
							Vector d = player.getLocation().getDirection().multiply(2);
							d.add(new Vector(Math.random() * accuracy - accuracy,Math.random() * accuracy - accuracy,Math.random() * accuracy - accuracy));
							egg.setVelocity(d);
							egg.setGlowing(true);
							egg.setShooter(player);
							egg.setMetadata(player.getName(), new FixedMetadataValue(Core.plugin, "Roadhog"));
						}
						t++;
					}
				}.runTaskTimer(Core.plugin, 0, 2);
			}
		}
	}
	
	@EventHandler
	public void respawn(PlayerRespawnEvent e){
		if(!e.getPlayer().equals(player)) return;
		player.setMaxHealth(maxhealth);
		player.setHealth(player.getMaxHealth());
	}
	
	public void recharge(){
		new BukkitRunnable() {
			@Override
			public void run() {
				if(start == false) this.cancel();
				if(ultimate_charge + 0.01 >= 1){
					ultimate_charge = 1;
					if(msg == false){
						if(Core.t) player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_ENDERMEN_DEATH, 1, 1);
						Core.sendTitle(player, 5, 51, 1, ChatColor.GOLD+"ULTIMATE", ChatColor.GREEN+"Ready to launch");
						msg = true;
					}
				}else ultimate_charge += 0.01;
				if(start) player.setLevel(ammo);
				if(start) player.setExp(ultimate_charge);
			}
		}.runTaskTimer(Core.plugin, 0, 52);
	}
	private ItemStack getItemName(){
        ItemStack scrapgun = new ItemStack(Material.CARROT_STICK, 1);
        ItemMeta meta = scrapgun.getItemMeta();
        scrapgun.setItemMeta(meta);
        meta.spigot().setUnbreakable(true);
        meta.setDisplayName(ChatColor.BOLD + "" + ChatColor.GRAY + "Scrap Gun");
        meta.setLore(Arrays.asList(ChatColor.GOLD + "Type: " + ChatColor.GRAY + "" + ChatColor.ITALIC + "Shotgun projectile",
                ChatColor.GOLD + "Damage: " + ChatColor.GRAY + "" + ChatColor.ITALIC + "2 - 9 per pellet",
                ChatColor.GOLD + "Projectile speed: " + ChatColor.GRAY + "" + ChatColor.ITALIC + "  57 meters per second",
                ChatColor.GOLD + "Number of pellets: " + ChatColor.GRAY + "" + ChatColor.ITALIC + " 25 pellets per shot",
                ChatColor.GOLD + "Rate of fire: " + ChatColor.GRAY + "" + ChatColor.ITALIC + "1 shot per second",
                ChatColor.GOLD + "Ammo: " + ChatColor.GRAY + "" + ChatColor.ITALIC + "4",
                ChatColor.LIGHT_PURPLE + "Description: " + ChatColor.RED + "" + ChatColor.ITALIC + "Roadhog's Scrap Gun fires short-range blasts of shrapnel with a wide spread. Alternatively, it can launch a shrapnel ball that detonates farther away, scattering metal fragments from the point of impact!"));
        scrapgun.setItemMeta(meta);
        return scrapgun;
    }
	@EventHandler
	public void explode(EntityExplodeEvent e){
		if(e.getEntity().hasMetadata(player.getName())){
			e.blockList().clear();
		}
	}
	
	@EventHandler
	public void kill(EntityDeathEvent e){
		if(e.getEntity().getKiller() != null && e.getEntity().getKiller().equals(player) && !e.getEntity().equals(player)) {
			if(ultimate_charge + 0.4 >= 1){
				if(start) player.setLevel(ammo);
				ultimate_charge = 1;
				if(msg == false){
					if(Core.t) player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_ENDERMEN_DEATH, 1, 1);
					Core.sendTitle(player, 5, 51, 1, ChatColor.GOLD+"ULTIMATE", ChatColor.GREEN+"Ready to launch");
					msg = true;
				}
			}else ultimate_charge += 0.4;
			if(start) player.setExp(ultimate_charge);
		}
	}
	@EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event)
    {
        if (event.getSpawnReason() == SpawnReason.EGG && event.getEntity().getWorld().equals(arena.getLocationInfo()[0].getWorld())){
            event.setCancelled(true);
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
	public void shoot(PlayerInteractEvent e){
		if(e.getPlayer().equals(player) && (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR)){
			e.setCancelled(true);
			if(e.getPlayer().equals(player) && (arena.death.contains(player) || reloading)) return;
			if(ammo > 0){
				float accuracy = 0.2F;
				for(int i = 0; i < 10; i++){
					Egg egg = player.getWorld().spawn(player.getEyeLocation(), Egg.class);
					Vector d = player.getLocation().getDirection().multiply(2);
					d.add(new Vector(Math.random() * accuracy - accuracy,Math.random() * accuracy - accuracy,Math.random() * accuracy - accuracy));
					egg.setVelocity(d);
					egg.setShooter(player);
					egg.setMetadata(player.getName(), new FixedMetadataValue(Core.plugin, "Roadhog"));
				}
				ammo--;
				player.playSound(player.getLocation(), Sound.BLOCK_STONE_STEP, 1, 1);
				if(start) player.setLevel(ammo);
			}else{
				e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_OFF, 1, 1);
				if(reloading) return;
				reloading = true;
				new BukkitRunnable() {
					@Override
					public void run() {
						e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
						ammo = 4;
						if(start) player.setLevel(ammo);
						reloading = false;
					}
				}.runTaskLater(Core.plugin, 20);
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
				ammo = 4;
				if(start) player.setLevel(ammo);
				reloading = false;
			}
		}.runTaskLater(Core.plugin, 20);
	}
	@EventHandler
	public void rekall(PlayerItemHeldEvent e){
		if(e.getPlayer().equals(player) && arena.death.contains(player)) {
			e.setCancelled(true);
			return;
		}
		if(e.getPlayer().equals(player)){
			e.setCancelled(true);
			if(!player.hasPotionEffect(PotionEffectType.LUCK) && e.getPlayer().getHealth() != maxhealth){
				player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 160, 1));
				player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 50, 20));
				player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 50, 10));
				player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 4));
			}
		}
	}
	@EventHandler
	public void catchbokemon(PlayerToggleSneakEvent e){
		if(e.getPlayer().equals(player) && arena.death.contains(player)) return;
		if(e.getPlayer().equals(player)){
			if(!player.hasPotionEffect(PotionEffectType.UNLUCK)){
				player.addPotionEffect(new PotionEffect(PotionEffectType.UNLUCK, 120, 1));
				Bat bat = player.getWorld().spawn(player.getEyeLocation().add(0,-1,0), Bat.class);
				Arrow arrow = player.getWorld().spawn(player.getEyeLocation().add(0,1.2,0), Arrow.class);
				bat.setMetadata(player.getName(), new FixedMetadataValue(Core.plugin, "Roadhog"));
				bat.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 5));
				bat.setLeashHolder(arrow);
				bat.setAI(false);
				arrow.setVelocity(player.getLocation().getDirection().multiply(4));
				arrow.setMetadata(player.getName(), new FixedMetadataValue(Core.plugin, "Roadhog"));
				if(pat != null) {
					if(pat.getLeashHolder() != null) pat.getLeashHolder().remove();
					pat.remove();
					pat = null;
				}
				pat = bat;
			}
		}
	}
	@EventHandler
	public void fall(EntityDamageEvent e){
		if(e.getEntity().equals(player) && e.getCause() == DamageCause.VOID) ((Player)e.getEntity()).damage(100);
		if(e.getEntity().equals(player) && e.getCause() == DamageCause.FALL) e.setCancelled(true);
	}
	
	@EventHandler
	public void hit(ProjectileHitEvent e){
		if(e.getEntity().hasMetadata(player.getName()) && e.getEntity() instanceof Arrow){
			e.getEntity().remove();	
			if(pat != null) pat.remove();
			pat = null;
		}
	}
	@EventHandler
	public void nodamage(EntityDamageByEntityEvent e){
		if(e.getDamager().equals(player) && e.getEntity() instanceof Player) {
			if(arena.isAlly(player, (Player)e.getEntity())) e.setCancelled(true);
			else e.setDamage(shootdamage); 
		}
		if(e.getDamager().hasMetadata(player.getName()) && e.getDamager() instanceof Arrow && e.getEntity() instanceof Player && !e.getEntity().equals(player)){
			Core.pullEntityToLocation(e.getEntity(), player.getEyeLocation());
			e.getDamager().remove();	
			if(pat != null) pat.remove();
			e.setCancelled(true);
			return;
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
