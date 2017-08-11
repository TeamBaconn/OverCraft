package com.Tuong.Arena;

public class ArenaPlayerScore {
	public int kill_streak,kill,best_kill_streak;
	public ArenaPlayerScore(){
		this.kill_streak = 0;
		this.kill = 0;
		this.best_kill_streak = 0;
	}
	public void resetKillStreak(){
		if(kill_streak > best_kill_streak) best_kill_streak = kill_streak;
		kill_streak = 0;
	}
	public void addKill(){
		this.kill++;
		this.kill_streak++;
	}
	public int getBestKillStreak(){
		if(kill_streak > best_kill_streak) best_kill_streak = kill_streak;
		return best_kill_streak;
	}
}
