/**
 * Copyright (c) 2017 Atos
 * This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0
 * which accompanies this distribution, and is available at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Contributors:
 *    @author David Rojo Antona (Atos)
 *    
 * Developed in the context of ElasTest EU project http://elastest.io 
 */

package io.elastest.eim.database.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import io.elastest.eim.database.AgentRepository;
import io.swagger.model.AgentConfiguration;
import io.swagger.model.AgentConfigurationDatabase;
import io.swagger.model.AgentConfigurationFilebeat;
import io.swagger.model.AgentConfigurationPacketbeat;
import io.swagger.model.AgentConfigurationTopbeat;
import io.swagger.model.AgentFull;
import io.swagger.model.Host;

public class EimDbManager {

	// JDBC driver name and database URL
    static final String JDBC_DRIVER = "org.mariadb.jdbc.Driver";
    static final String DB_URL = "jdbc:mariadb://localhost/eim";

    //  Database credentials
    static final String USER = "elastest";
    static final String PASS = "elastest";

    private static Logger logger = Logger.getLogger(AgentRepository.class);
    
    public EimDbManager() {
    	
    }
    
    public Connection getConnection() {
    	Connection conn = null;
        try {
            //Register JDBC driver
            Class.forName(JDBC_DRIVER);

            //Open a connection
            logger.info("Connecting to EIM database...");
            conn = DriverManager.getConnection(
            		DB_URL, USER, PASS);
            logger.info("Connected to EIM database successfully...");
            

            return conn;
        } catch (SQLException se) {
            //Handle errors for JDBC
        	se.printStackTrace();
            logger.error(se.getMessage());
            return null;
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
            logger.error(e.getMessage());
            return null;
        } 
    }
    
    
    private String getNewAgentId(Connection conn) throws SQLException{
		String newId = "iagent";
		List<AgentFull> agents = getAgents(conn);
		int elements;
		if (agents != null) {
			elements = agents.size();
			newId += elements;
		}		
		return newId;
	}
    
    private boolean existsAgent(Connection conn, String agentId) throws SQLException {
    	String sqlSearchIagent = "SELECT AGENT_ID FROM AGENT WHERE AGENT_ID=?";
		PreparedStatement pstSearchIagent = conn.prepareStatement(sqlSearchIagent);
		pstSearchIagent.setString(1, agentId);
		ResultSet rs = pstSearchIagent.executeQuery();
		if (!rs.next()) {
			return false;
		}
		else {
			return true;
		}
    }
    
    public AgentFull addHost(Host host) {
    	AgentFull agent = null;
    	Connection conn = null;
    	try {
    		
    		conn = getConnection();
    		
    		if (getAgentByIpAddress(conn, host.getAddress()) != null) {
    			agent = addHost(conn, host);
    			return agent;
    		}
    		else { 
    			logger.info("Agent with agentId = " + host.getAddress() + " exists in database");
    			return null;
    		}
    		
    	} catch (SQLException ex) {
            logger.error("Error " + ex.getErrorCode() + ": " + ex.getMessage());
        }
    	finally {
    		try{
    			if(conn!=null)
    				conn.close();
    		} catch(SQLException se){
    			logger.error(se.getMessage());
    			se.printStackTrace();
    		}
    	}
		return agent;
    }
    
    
    private AgentFull addHost( Connection conn, Host host) throws SQLException{
    	AgentFull inserted = null;
    	PreparedStatement pstInsertHost = null;
    	try {
    		logger.info("Adding new host to DB, host with ipAddress = " + host.getAddress());
    		System.out.println("Adding new host to DB, host with ipAddress = " + host.getAddress());
    		
    		String sqlInsertHost = "INSERT INTO agent VALUES (?,?,?,?,?)";
    		pstInsertHost = conn.prepareStatement(sqlInsertHost);
    	
    		pstInsertHost.setString(1, getNewAgentId(conn));
    		pstInsertHost.setString(2, host.getAddress());
    		pstInsertHost.setBoolean(3, false);
    		pstInsertHost.setString(4, host.getLogstashIp());
    		pstInsertHost.setString(5, host.getLogstashPort());
    		pstInsertHost.executeUpdate();
    		logger.info("Agent inserted in database wiht agentId = " + host.getAddress());
    		
    		inserted = getAgentByIpAddress(conn, host.getAddress());
    		
    	}
		finally {
    		try{
    			if(pstInsertHost!=null)
    				pstInsertHost.close();
    		} catch(SQLException se){
    			logger.error(se.getMessage());
    			se.printStackTrace();
    		}
    	}
    	return inserted;
    }
   
    
	private AgentFull getAgentByIpAddress(Connection conn, String ipAddress) throws SQLException {
		logger.info("Searching host in DB with ipAddress = " + ipAddress);
		System.out.println("Searching host in DB with ipAddress = " + ipAddress);
		AgentFull agent = null;
		PreparedStatement pstSelectAgent = null;
		
		try {
			String selectSQL = "SELECT AGENT_ID, HOST, LOGSTASH_IP, LOGSTASH_PORT FROM AGENT WHERE HOST = ?";
			pstSelectAgent = conn.prepareStatement(selectSQL);
			pstSelectAgent.setString(1, ipAddress);
			ResultSet rs = pstSelectAgent.executeQuery(selectSQL );
			while (rs.next()) {
				agent = new AgentFull();
				agent.setAgentId(rs.getString("AGENT_ID"));
				agent.setHost(rs.getString("HOST"));
				agent.setLogstashIp(rs.getString("LOGSTASH_IP"));
				agent.setLogstashPort(rs.getString("LOGSTASH_PORT"));
	        	logger.info("Host finded in DB with ipAddress = " + ipAddress + " with ID " + agent.getAgentId());
	        	System.out.println("Host finded in DB with ipAddress = " + ipAddress + " with ID " + agent.getAgentId());
				return agent;
			}
		}
		finally {
    		try{
    			if(pstSelectAgent!=null)
    				pstSelectAgent.close();
    		} catch(SQLException se){
    			logger.error(se.getMessage());
    			se.printStackTrace();
    		}
    	}
		return agent;

	}
	
	private AgentFull getAgentByAgentId(Connection conn, String agentId) throws SQLException {
		logger.info("Searching host in DB with agentId = " + agentId);
		System.out.println("Searching host in DB with agentId = " + agentId);
		AgentFull agent = null;
		PreparedStatement pstSelectAgent = null;
		
		try {
			String selectSQL = "SELECT AGENT_ID, HOST, LOGSTASH_IP, LOGSTASH_PORT FROM AGENT WHERE AGENT_ID = ?";
			pstSelectAgent = conn.prepareStatement(selectSQL);
			pstSelectAgent.setString(1, agentId);
			ResultSet rs = pstSelectAgent.executeQuery(selectSQL );
			while (rs.next()) {
				agent = new AgentFull();
				agent.setAgentId(rs.getString("AGENT_ID"));
				agent.setHost(rs.getString("HOST"));
				agent.setLogstashIp(rs.getString("LOGSTASH_IP"));
				agent.setLogstashPort(rs.getString("LOGSTASH_PORT"));
	        	logger.info("Host finded in DB with agentId = " + agentId + " with ID " + agent.getAgentId());
	        	System.out.println("Host finded in DB with agentId = " + agentId + " with ID " + agent.getAgentId());
				return agent;
			}
		}
		finally {
    		try{
    			if(pstSelectAgent!=null)
    				pstSelectAgent.close();
    		} catch(SQLException se){
    			logger.error(se.getMessage());
    			se.printStackTrace();
    		}
    	}
		return agent;

	}
	
	
	public AgentFull getAgentByAgentId(String agentId) {
    	AgentFull agent = null;
    	Connection conn = null;
    	try {
    		
    		conn = getConnection();
    		return getAgentByAgentId(conn, agentId);
    		
    	} catch (SQLException ex) {
            logger.error("Error " + ex.getErrorCode() + ": " + ex.getMessage());
        }
    	finally {
    		try{
    			if(conn!=null)
    				conn.close();
    		} catch(SQLException se){
    			logger.error(se.getMessage());
    			se.printStackTrace();
    		}
    	}
		return agent;
    }
	
	private List<AgentFull> getAgents(Connection conn) throws SQLException{
		logger.info("Searching agents in DB");
		System.out.println("Searching agents in DB");
		List<AgentFull> agents = null;
		PreparedStatement pstSelectAgents = null;
		
		try {
			String selectSQL = "SELECT AGENT_ID, HOST, LOGSTASH_IP, LOGSTASH_PORT FROM AGENT";
			pstSelectAgents = conn.prepareStatement(selectSQL);
			ResultSet rs = pstSelectAgents.executeQuery(selectSQL);
			agents = new ArrayList<AgentFull>();
			while (rs.next()) {
				AgentFull agent = new AgentFull();
				agent.setAgentId(rs.getString("AGENT_ID"));
				agent.setHost(rs.getString("HOST"));
				agent.setLogstashIp(rs.getString("LOGSTASH_IP"));
				agent.setLogstashPort(rs.getString("LOGSTASH_PORT"));
	        	agents.add(agent);
			}
		}
		finally {
    		try{
    			if(pstSelectAgents!=null)
    				pstSelectAgents.close();
    		} catch(SQLException se){
    			logger.error(se.getMessage());
    			se.printStackTrace();
    		}
    	}
		return agents;
	}
	
	public AgentFull setMonitored(String agentId, boolean monitored) {
		AgentFull agent = null;
    	Connection conn = null;
    	try {
    		
    		conn = getConnection();
    		
    		if (existsAgent(conn,agentId)) {
    			agent = setMonitored(conn, agentId, monitored);
    			return agent;
    		}
    		else { 
    			logger.info("Agent with agentId = " + agentId + " does not exists in database");
    			return null;
    		}
    		
    	} catch (SQLException ex) {
            logger.error("Error " + ex.getErrorCode() + ": " + ex.getMessage());
        }
    	finally {
    		try{
    			if(conn!=null)
    				conn.close();
    		} catch(SQLException se){
    			logger.error(se.getMessage());
    			se.printStackTrace();
    		}
    	}
		return agent;
	}

	private AgentFull setMonitored(Connection conn, String agentId, boolean monitored) throws SQLException {
		logger.info("Setting monitored = " + monitored + "the agent with agentId = " + agentId + " in DB");
		System.out.println("Setting monitored = " + monitored + "the agent with agentId = " + agentId + " in DB");
		PreparedStatement pstUpdatedMonitored = null;
		
		try {
 			String updateSQL = "UPDATE AGENT SET MONITORED = ? WHERE AGENT_ID = ?";
			pstUpdatedMonitored = conn.prepareStatement(updateSQL);
			String strMonitored = "false";
			if (monitored) 
				strMonitored = "true";
			pstUpdatedMonitored.setString(1, strMonitored);
			pstUpdatedMonitored.setString(2, agentId);
			int updatedRows = pstUpdatedMonitored.executeUpdate(updateSQL);
			if (updatedRows == 1) {
				return getAgentByAgentId(conn, agentId);
			}
			else {
				logger.error(updatedRows + " rows has been updated when trying to set monitored = " + strMonitored + " for agent " + agentId);
				return null;
			}
		}
		finally {
    		try{
    			if(pstUpdatedMonitored!=null)
    				pstUpdatedMonitored.close();
    		} catch(SQLException se){
    			logger.error(se.getMessage());
    			se.printStackTrace();
    		}
    	}
	}
	
	
	public boolean deleteAgent(String agentId) {

		Connection conn = null;
    	try {
    		
    		conn = getConnection();
    		return deleteAgent(conn, agentId);
    		
    	} catch (SQLException ex) {
            logger.error("Error " + ex.getErrorCode() + ": " + ex.getMessage());
        }
    	finally {
    		try{
    			if(conn!=null)
    				conn.close();
    		} catch(SQLException se){
    			logger.error(se.getMessage());
    			se.printStackTrace();
    		}
    	}
		return false;
	}
	
	private boolean deleteAgent(Connection conn, String agentId) throws SQLException {
		logger.info("Deleting agent agentId = " + agentId + "...");
		System.out.println("Deleting agent agentId = " + agentId + "...");
		PreparedStatement pstDeleteAgent = null;
		
		try {
 			String deleteSQL = "DELETE FROM AGENT WHERE AGENT_ID = ?";
			pstDeleteAgent = conn.prepareStatement(deleteSQL);
			pstDeleteAgent.setString(1, agentId);

			int deletedRows = pstDeleteAgent.executeUpdate();
			if (deletedRows == 1) {
				return true;
			}
			else {
				logger.error(deletedRows + " rows has been removed when trying to delete agent with agentId = " + agentId);
				return false;
			}
		}
		finally {
    		try{
    			if(pstDeleteAgent!=null)
    				pstDeleteAgent.close();
    		} catch(SQLException se){
    			logger.error(se.getMessage());
    			se.printStackTrace();
    		}
    	}
	}
	
	public List<AgentFull> getAgents() {
    	
    	Connection conn = null;
    	try {
    		
    		conn = getConnection();
    		return getAgents(conn);
    		
    	} catch (SQLException ex) {
            logger.error("Error " + ex.getErrorCode() + ": " + ex.getMessage());
        }
    	finally {
    		try{
    			if(conn!=null)
    				conn.close();
    		} catch(SQLException se){
    			logger.error(se.getMessage());
    			se.printStackTrace();
    		}
    	}
		return null;
    }
	
	
	//TODO
	//agentConfigurationRepo
	
	public AgentConfigurationDatabase getAgentConfigurationByAgentId(String agentId){
		
		AgentConfigurationDatabase agentCfgDb = null;
    	Connection conn = null;
    	try {
    		
    		conn = getConnection();
    		return getAgentConfigurationByAgentId(conn, agentId);
    		
    	} catch (SQLException ex) {
            logger.error("Error " + ex.getErrorCode() + ": " + ex.getMessage());
        }
    	finally {
    		try{
    			if(conn!=null)
    				conn.close();
    		} catch(SQLException se){
    			logger.error(se.getMessage());
    			se.printStackTrace();
    		}
    	}
		return agentCfgDb;
			
	}

	private AgentConfigurationDatabase getAgentConfigurationByAgentId(Connection conn, String agentId) throws SQLException {
		logger.info("Searching host in DB with agentId = " + agentId);
		System.out.println("Searching host in DB with agentId = " + agentId);
		AgentConfigurationDatabase agentCfg = null;
		PreparedStatement pstSelectAgentCfg = null;
		
		try {
			String selectSQL = "SELECT AGENT_ID, EXEC, COMPONENT, "
					+ "PACKETBEAT_STREAM, TOPBEAT_STREAM, FILEBEAT_STREAM, "
					+ "FILEBEAT_PATHS "
					+ "FROM AGENT_CONFIGURATION "
					+ "WHERE AGENT_ID = ?";
			pstSelectAgentCfg = conn.prepareStatement(selectSQL);
			pstSelectAgentCfg.setString(1, agentId);
			ResultSet rs = pstSelectAgentCfg.executeQuery(selectSQL );
			while (rs.next()) {
				
				logger.info("Agent cfg finded in DB with agentId = " + agentId + " with ID " + agentId);
	        	System.out.println("Agent cfg finded in DB with agentId = " + agentId + " with ID " + agentId);
				return this.toAgentCfgDbObject(rs);
			}
		}
		finally {
    		try{
    			if(pstSelectAgentCfg!=null)
    				pstSelectAgentCfg.close();
    		} catch(SQLException se){
    			logger.error(se.getMessage());
    			se.printStackTrace();
    		}
    	}
		return agentCfg;

	}
	
	private AgentConfigurationDatabase toAgentCfgDbObject(ResultSet rs) throws SQLException {
		AgentConfigurationDatabase agentCfgDb = new AgentConfigurationDatabase();
		agentCfgDb.setAgentId(rs.getString("AGENT_ID"));

		AgentConfiguration ac = new AgentConfiguration();
		
		ac.setExec(rs.getString("EXEC"));
    	ac.setComponent(rs.getString("COMPONENT"));
    	
    	AgentConfigurationPacketbeat packetbeat = new AgentConfigurationPacketbeat();
    	packetbeat.setStream(rs.getString("PACKETBEAT_STREAM"));
    	ac.setPacketbeat(packetbeat);
    	
    	AgentConfigurationTopbeat topbeat = new AgentConfigurationTopbeat();
    	packetbeat.setStream(rs.getString("TOPBEAT_STREAM"));
    	ac.setTopbeat(topbeat);
    	
    	AgentConfigurationFilebeat filebeat = new AgentConfigurationFilebeat();
    	filebeat.setStream(rs.getString("FILEBEAT_STREAM"));
    	List<String> pathsList = new ArrayList<String>();
    	String strPaths = rs.getString("FILEBEAT_PATHS");
    	String[] paths = strPaths.split(",");
    	for (String path : paths) {
    		pathsList.add(path);
    	}
    	filebeat.setPaths(pathsList);
    	ac.setFilebeat(filebeat);

    	agentCfgDb.setAgentConfiguration(ac);		
		return agentCfgDb;		
	}
	
	
	private List<AgentConfigurationDatabase> getAgentConfigurations(Connection conn) throws SQLException{
		logger.info("Searching agent configurations in DB");
		System.out.println("Searching agent configurations in DB");
		List<AgentConfigurationDatabase> agents = null;
		PreparedStatement pstSelectAgentCfgs = null;
		
		try {
			String selectSQL = "SELECT AGENT_ID, EXEC, COMPONENT, "
					+ "PACKETBEAT_STREAM, TOPBEAT_STREAM, FILEBEAT_STREAM, "
					+ "FILEBEAT_PATHS "
					+ "FROM AGENT_CONFIGURATION";
			pstSelectAgentCfgs = conn.prepareStatement(selectSQL);
			ResultSet rs = pstSelectAgentCfgs.executeQuery(selectSQL);
			agents = new ArrayList<AgentConfigurationDatabase>();
			while (rs.next()) {
	        	agents.add(this.toAgentCfgDbObject(rs));
			}
		}
		finally {
    		try{
    			if(pstSelectAgentCfgs!=null)
    				pstSelectAgentCfgs.close();
    		} catch(SQLException se){
    			logger.error(se.getMessage());
    			se.printStackTrace();
    		}
    	}
		return agents;
	}
	
	public List<AgentConfigurationDatabase> getAgentConfigurations(){
		
		List<AgentConfigurationDatabase> agentCfgDb = null;
    	Connection conn = null;
    	try {
    		
    		conn = getConnection();
    		return getAgentConfigurations(conn);
    		
    	} catch (SQLException ex) {
            logger.error("Error " + ex.getErrorCode() + ": " + ex.getMessage());
        }
    	finally {
    		try{
    			if(conn!=null)
    				conn.close();
    		} catch(SQLException se){
    			logger.error(se.getMessage());
    			se.printStackTrace();
    		}
    	}
		return agentCfgDb;
			
	}
	
	
	public boolean deleteAgentConfiguration(String agentId) {

		Connection conn = null;
    	try {
    		
    		conn = getConnection();
    		return deleteAgentConfiguration(conn, agentId);
    		
    	} catch (SQLException ex) {
            logger.error("Error " + ex.getErrorCode() + ": " + ex.getMessage());
        }
    	finally {
    		try{
    			if(conn!=null)
    				conn.close();
    		} catch(SQLException se){
    			logger.error(se.getMessage());
    			se.printStackTrace();
    		}
    	}
		return false;
	}
	
	
	private boolean deleteAgentConfiguration(Connection conn, String agentId) throws SQLException {
		logger.info("Deleting agent cfg for agentId = " + agentId + "...");
		System.out.println("Deleting agent cfg for agentId = " + agentId + "...");
		PreparedStatement pstDeleteAgentCfg = null;
		
		try {
 			String deleteSQL = "DELETE FROM AGENT_CONFIGURATION WHERE AGENT_ID = ?";
 			pstDeleteAgentCfg = conn.prepareStatement(deleteSQL);
 			pstDeleteAgentCfg.setString(1, agentId);

			int deletedRows = pstDeleteAgentCfg.executeUpdate();
			if (deletedRows == 1) {
				return true;
			}
			else {
				logger.error(deletedRows + " rows has been removed when trying to delete agent cfg with agentId = " + agentId);
				return false;
			}
		}
		finally {
    		try{
    			if(pstDeleteAgentCfg!=null)
    				pstDeleteAgentCfg.close();
    		} catch(SQLException se){
    			logger.error(se.getMessage());
    			se.printStackTrace();
    		}
    	}
	}
	
	public AgentConfigurationDatabase addAgentCfg(String agentId, AgentConfiguration agentCfgObj){
		AgentConfigurationDatabase agentCfgDb = null;
    	Connection conn = null;
    	try {
    		
    		conn = getConnection();
    		
    		if (getAgentConfigurationByAgentId(conn, agentId) != null) {
    			agentCfgDb = addAgentCfg(conn, agentId, agentCfgObj);
    			return agentCfgDb;
    		}
    		else { 
    			logger.info("Agent with agentId = " + agentId + " exists in database");
    			return null;
    		}
    		
    	} catch (SQLException ex) {
            logger.error("Error " + ex.getErrorCode() + ": " + ex.getMessage());
        }
    	finally {
    		try{
    			if(conn!=null)
    				conn.close();
    		} catch(SQLException se){
    			logger.error(se.getMessage());
    			se.printStackTrace();
    		}
    	}
		return agentCfgDb;
	}

	private AgentConfigurationDatabase addAgentCfg(Connection conn, String agentId, AgentConfiguration agentCfgObj) throws SQLException {
		AgentConfigurationDatabase inserted = null;
    	PreparedStatement pstInsertHost = null;
    	try {
    		logger.info("Adding new agent cfg to DB, agent with agentId = " + agentId);
    		System.out.println("Adding new agent cfg to DB, agent with agentId = " + agentId);
    		
    		String sqlInsertAgentCfg = "INSERT INTO AGENT_CONFIGURATION VALUES (?,?,?,?,?,?,?)";
    		pstInsertHost = conn.prepareStatement(sqlInsertAgentCfg);
    	
    		pstInsertHost.setString(1, agentId);
    		pstInsertHost.setString(2, agentCfgObj.getExec());
    		pstInsertHost.setString(3, agentCfgObj.getComponent());
    		pstInsertHost.setString(4, agentCfgObj.getPacketbeat().getStream());
    		pstInsertHost.setString(5, agentCfgObj.getTopbeat().getStream());
    		pstInsertHost.setString(6, agentCfgObj.getFilebeat().getStream());
    		List<String> pathsList = agentCfgObj.getFilebeat().getPaths();
    		String strPaths = "";
    		for (String path : pathsList) {
    			strPaths += path + ",";
    		}
    		strPaths = strPaths.substring(0, strPaths.length()-1);
    		pstInsertHost.setString(7, strPaths);
    		pstInsertHost.executeUpdate();
    		logger.info("Agent cfg inserted in database for agentId = " + agentId);
    		
    		inserted = getAgentConfigurationByAgentId(conn, agentId);
    		
    	}
		finally {
    		try{
    			if(pstInsertHost!=null)
    				pstInsertHost.close();
    		} catch(SQLException se){
    			logger.error(se.getMessage());
    			se.printStackTrace();
    		}
    	}
    	return inserted;
	}
	
	
	public AgentFull getAgentByIpAddress(String ipAddress) {
    	AgentFull agent = null;
    	Connection conn = null;
    	try {
    		
    		conn = getConnection();
    		agent = getAgentByIpAddress(conn, ipAddress);
    		if (agent != null) {
    			return agent;
    		}
    		else {
				logger.info("Agent with agentId = " + ipAddress + " exists in database");
    			return null;
    		}
    		
    	} catch (SQLException ex) {
            logger.error("Error " + ex.getErrorCode() + ": " + ex.getMessage());
        }
    	finally {
    		try{
    			if(conn!=null)
    				conn.close();
    		} catch(SQLException se){
    			logger.error(se.getMessage());
    			se.printStackTrace();
    		}
    	}
		return agent;
    }
    
}
