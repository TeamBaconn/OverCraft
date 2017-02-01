package com.Tuong.Arena;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ArenaManager {
	public ArrayList<Arena> listArena;
	public ArenaManager(){
		listArena = new ArrayList<Arena>();
	}
	public boolean createArena(String arenaName, Location[] locationInfo, int[] numbericInfo){
		if(getArenaByName(arenaName) != null) return false;
		listArena.add(new Arena(arenaName, locationInfo, numbericInfo));
		return true;
	}
	public void removeArena(Arena arena){
		if(listArena.contains(arena)) listArena.remove(arena);
	}
	public Arena inArena(Player player){
		for(Arena arena : listArena) if(arena.inArena(player)) return arena;
		return null;
	}
	public ArrayList<Arena> getArrayList(){
		return this.listArena;
	}
	public Arena getArenaByName(String name){
		for(Arena arena : listArena) if(arena.getArenaName().equals(name)) return arena;
		return null;
	}
}
