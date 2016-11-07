package com.Tuong.Heros;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryClickEvent;
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
import net.minecraft.server.v1_9_R2.EnumParticle;

public class Tracer implements Listener{
	private Player player;
	private Arena arena;
	private int charge,save,ammo,maxhealth;
	private float ultimate_charge;
	private final double damageshoot = 6;
	private Location loc;
	private boolean start,shift,msg,reloading,first;
	public Tracer(Player player, Arena arena){
		player.getInventory().clear();
		player.getInventory().setHeldItemSlot(8);
		player.getInventory().setItemInOffHand(getItemName());
		player.getInventory().setItemInMainHand(getItemName());
		if(arena.team.get(player).equals("BLUE")) player.getInventory().setChestplate(arena.getBlueChestplate());
		else player.getInventory().setChestplate(arena.getRedChestplate());
		if(Core.t) player.playSound(player.getLocation(), Sound.ENTITY_BAT_HURT, 1, 1);
		this.player = player;
		this.arena = arena;
		this.charge = 3;
		this.first = true;
		this.start = true;
		this.shift = false;
		this.msg = false;
		this.loc = player.getLocation();
		this.save = 4;
		this.ammo = 40;
		this.maxhealth = 15;
		this.reloading = false;
		this.ultimate_charge = 0;
		player.setMaxHealth(maxhealth);
		player.setHealth(player.getMaxHealth());
		Bukkit.getPluginManager().registerEvents(this, Core.plugin);
		recharge();
	}
	
	private ItemStack getItemName(){
        ItemStack pistol = new ItemStack(Material.DIAMOND_SPADE, 1);
        ItemMeta meta = pistol.getItemMeta();
        pistol.setItemMeta(meta);
        meta.spigot().setUnbreakable(true);
        meta.setDisplayName(ChatColor.BOLD + "" + ChatColor.GRAY + "Pulse Pistols");
        meta.setLore(Arrays.asList(ChatColor.GOLD + "Type: " + ChatColor.GRAY + "" + ChatColor.ITALIC + "Rapid Fire Hitscan",
                ChatColor.GOLD + "Damage: " + ChatColor.GRAY + "" + ChatColor.ITALIC + "1.5 - 6/Round",
                ChatColor.GOLD + "Falloff range: " + ChatColor.GRAY + "" + ChatColor.ITALIC + "11 - 30 Meters",
                ChatColor.GOLD + "Rate of fire: " + ChatColor.GRAY + "" + ChatColor.ITALIC + "40 Rounds/Second",
                ChatColor.GOLD + "Ammo: " + ChatColor.GRAY + "" + ChatColor.ITALIC + "40",
                ChatColor.GOLD + "Reload Time: " + ChatColor.GRAY + "" + ChatColor.ITALIC + "1 Second",
                ChatColor.LIGHT_PURPLE + "Description: " + ChatColor.RED + "" + ChatColor.ITALIC + "Tracer rapid-fires both of her pistols!"));
        pistol.setItemMeta(meta);
        return pistol;
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
				if(charge < 3) charge++;
				if(ultimate_charge + 0.01 >= 1){
					ultimate_charge = 1;
					if(msg == false){
						if(Core.t) player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BAT_LOOP, 1, 1);
						Core.sendTitle(player, 5, 51, 1, ChatColor.GOLD+"ULTIMATE", ChatColor.GREEN+"Ready to launch");
						msg = true;
					}else updateCharge();
				}else{
					ultimate_charge += 0.01;
					updateCharge();
				}
				if(start == false) this.cancel();
				if(start)player.setLevel(ammo);
				if(start)player.setExp(ultimate_charge);
				save--;
				if(save == 0){
					save = 4;
					loc = player.getLocation();
				}
			}
		}.runTaskTimer(Core.plugin, 0, 52);
	}
	
	public Player getPlayer(){
		return this.player;
	}
	
	public void updateCharge(){
		String s = "";
		switch (charge) {
		case 3:
			s = "░";
			break;
		case 2:
			s = "▒";
			break;
		case 1:
			s = "▓";
			break;
		}
		Core.sendTitle(player, 0, 53, 0, "", ChatColor.AQUA+""+ChatColor.BOLD+s);
	}
	
	@EventHandler
	public void fall(EntityDamageEvent e){
		if(e.getEntity().equals(player) && e.getCause() == DamageCause.FALL) e.setCancelled(true);
	}
	
	@EventHandler
	public void click(InventoryClickEvent e){
		if(e.getWhoClicked().equals(player)) e.setCancelled(true);
	}
	
	@EventHandler
	public void kill(EntityDeathEvent e){
		if(e.getEntity().getKiller() != null && e.getEntity().getKiller().equals(player) && !e.getEntity().equals(player)) {
			if(ultimate_charge + 0.4 >= 1){
				if(start)player.setLevel(ammo);
				ultimate_charge = 1;
				if(msg == false){
					player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BAT_LOOP, 1, 1);
					Core.sendTitle(player, 5, 51, 1, ChatColor.GOLD+"ULTIMATE", ChatColor.GREEN+"Ready to launch");
					msg = true;
				}
			}else ultimate_charge += 0.4;
			if(start)player.setExp(ultimate_charge);
		}
	}
	
	@EventHandler
	public void skillSneak(PlayerToggleSneakEvent e){
		if(e.getPlayer().equals(player) && arena.death.contains(player)) return;
		if(e.getPlayer().equals(player) && shift == false){
			if(charge > 0){
				charge--;
				updateCharge();
				ArmorStand am = player.getWorld().spawn(player.getLocation(), ArmorStand.class);
				am.setSmall(true);
				am.setVisible(false);
				am.setVelocity(player.getLocation().getDirection().multiply(3));
				shift = true;
				new BukkitRunnable() {
					@Override
					public void run() {
						if((arena.spawnRedRegion.contains(am.getLocation()) && arena.team.get(e.getPlayer()).equals("BLUE")) || (arena.spawnBlueRegion.contains(am.getLocation())&& arena.team.get(e.getPlayer()).equals("RED"))){
							am.remove();
							shift = false;
							return;
						}
						player.teleport(am.getLocation());
						player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_ENDERMEN_TELEPORT, 1, 1);
						am.remove();
						shift = false;
					}
				}.runTaskLater(Core.plugin, 5);
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
	public void rekall(PlayerItemHeldEvent e){
		if(e.getPlayer().equals(player) && arena.death.contains(player)){
			e.setCancelled(true);
			return;
		}
		if(e.getPlayer().equals(player)){
			e.setCancelled(true);
			if(first){
				first = false;
				this.loc = e.getPlayer().getLocation();
				return;
			}
			if(!player.hasPotionEffect(PotionEffectType.LUCK)){
				player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 240, 1));
				player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 18, 20));
				player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 10));
				player.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 20, 2));
				player.teleport(loc);
			}
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
							Core.playParticle(EnumParticle.WATER_DROP, loc, 1);
							loc.subtract(x,y,z);
							if (t > 30)break;
						}
					}
				ammo-=2;
				player.playSound(player.getLocation(), Sound.ENTITY_BAT_DEATH, 1, 1);
				if(start)player.setLevel(ammo);
			}else{
				e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_OFF, 1, 1);
				if(reloading) return;
				reloading = true;
				new BukkitRunnable() {
					@Override
					public void run() {
						e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
						ammo = 40;
						if(start)player.setLevel(ammo);
						reloading = false;
					}
				}.runTaskLater(Core.plugin, 20);
			}
		}
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
				ammo = 40;
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
			if(ultimate_charge == 1){
				ultimate_charge = 0;
				TNTPrimed tnt = player.getWorld().spawn(player.getEyeLocation(), TNTPrimed.class);
				tnt.setVelocity(player.getVelocity().multiply(0.5));
				tnt.setFuseTicks(40);
				tnt.setMetadata(player.getName(), new FixedMetadataValue(Core.plugin, "TRACER"));
				msg = false;
				if(Core.t)player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_BAT_AMBIENT, 1, 1);
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
		start = false;
		HandlerList.unregisterAll(this);
		player.setLevel(0); player.setExp(0);
		player.setLevel((int)arena.expStore.get(player)[0]);
		player.setExp(arena.expStore.get(player)[1]);
		for(Entity en : player.getWorld().getEntities()) if(en.hasMetadata(player.getName())) en.remove();
	}
}
