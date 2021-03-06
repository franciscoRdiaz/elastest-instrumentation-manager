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
    	String sqlSearchIagent = "SELECT iagent FROM agent WHERE codigo=?";
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
    		
    		if (!existsAgent(conn, host.getAddress())) {
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
	
	//TODO
	//setMonitored
	//deleteAgent
	//agentConfigurationRepo
	
    
}
