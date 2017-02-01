package com.Tuong.Database;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
	
	public Object[] getInfo(String UUID, String table){
		String sql = "SELECT * FROM "+table+" WHERE UUID='"+UUID+"'";
		try {
		PreparedStatement myPreparedStatement = connection.prepareStatement(sql);
		ResultSet results;
			results = myPreparedStatement.executeQuery();
			if (!results.next()) {
				
			} else {
			    Object[] obj = new Object[6];
			    
			    return obj;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void close(){
		 try {
			if(connection!=null && !connection.isClosed())connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
