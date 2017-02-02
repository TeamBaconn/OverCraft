package com.Tuong.Database;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.entity.Player;

import com.Tuong.OverCraftCore.Core;
import com.mysql.jdbc.Connection;

public class Database {
	private Connection connection;
	public boolean connected;
	public Database(String user, String pass, String name){
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			Core.plugin.getLogger().info("Database driver not available");
			this.connected = false;
		} 
		try {
			this.connection = (Connection) DriverManager.getConnection(name,user,pass);
			this.connected = true;
		} catch (SQLException e) {
			Core.plugin.getLogger().info("Connect to database fail");
			this.connected = false;
		}
		
	}
	
	public void createTable(String name, String elements){
		String createTable = "CREATE TABLE IF NOT EXISTS "+name+"("+elements+");";
		try {
		         PreparedStatement table = connection.prepareStatement(createTable);
		            table.executeUpdate();
		        } catch (SQLException e) {
		            e.printStackTrace();
		        }
	}
	public void add(Player p, int win, int rank_win, int kill, int killstreak, int rank, int coin){
		String sql = "SELECT * FROM overcraft WHERE UUID='"+p.getUniqueId()+"'";
		try {
			PreparedStatement myPreparedStatement = connection.prepareStatement(sql);
			ResultSet results = myPreparedStatement.executeQuery();
			if (!results.next()) {
			     PreparedStatement myPreparedStatement1 = connection.prepareStatement("INSERT INTO overcraft(UUID,RANK,WIN,RANK_WIN,KILLS,KILL_STREAK,COIN) VALUES ('"+p.getUniqueId()+"','"+rank+"','"+win+"','"+rank_win+"','"+kill+"','"+killstreak+"','"+coin+"')");
			     myPreparedStatement1.executeUpdate();
			} else {
				int kill_st = results.getInt("KILL_STREAK");
				if(killstreak > kill_st) kill_st = killstreak;
				 PreparedStatement myPreparedStatement2 = connection.prepareStatement("UPDATE overcraft SET RANK = '"+(results.getInt("RANK")+rank)+"', WIN = '"+(results.getInt("WIN")+win)+"', RANK_WIN = '"+(results.getInt("RANK_WIN")+rank_win)+"', KILLS = '"+(results.getInt("KILLS")+kill)+"', KILL_STREAK='"+kill_st+"', COIN ='"+(coin+results.getInt("COIN"))+"' WHERE UUID = '"+p.getUniqueId()+"'");
				 myPreparedStatement2.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public void close(){
		 try {
			if(connection!=null && !connection.isClosed())connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
