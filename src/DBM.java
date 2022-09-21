import java.sql.*;
import java.util.ArrayList;

public class DBM {
	//client connection to database as a class attribute
	Connection clientConnection = null;
	int fileID = 0;

	//constructor
	//assigns a client connection to existing database
	DBM(){
		try {
			this.clientConnection = DriverManager.getConnection("jdbc:sqlite:fileDB.sqlite");
		} catch (SQLException e) {
			System.out.println(e);
		}
	}
	
	public static void createNewDatabase() {
		Connection serverConnection = null;   
		String url = "jdbc:sqlite:fileDB.sqlite";
       	   try {  
       		   	//create database
       		   	serverConnection = DriverManager.getConnection(url);  // connect
				if (serverConnection != null) { 
					System.out.println("The database has been created.");  
            	}
			
				//create file table
			   String sql = "CREATE TABLE IF NOT EXISTS fileTable (\n"  
				        + " fileID integer PRIMARY KEY,\n"  
				        + " fileName text NOT NULL,\n"  
				        + " fileContent text "
				        + ");";            
				 
				   		Statement stmt = serverConnection.createStatement();  
				   		stmt.execute(sql);
				   		
				serverConnection.close();
				
       	   	} catch (SQLException e) {
       	   		System.out.println(e);
            		System.out.println("ERROR: could not create database.");  
        	}  
    }
	    
	
	public ArrayList<String> getFileNames() throws SQLException{  
		  	
		  	ArrayList<String> File_names = new ArrayList<String>();
		    
	    	String sql = "SELECT * FROM fileTable";            
	      	Statement stmt  = clientConnection.createStatement();  
	        ResultSet rs = stmt.executeQuery(sql);  
	        
	         //loop through the result set  
	        while (rs.next()) {  
	           
	        	File_names.add(rs.getString("fileName"));
	        	
	        }  
	    	return File_names;
	 }
	  
	
	public ArrayList<Integer> getFileIds() throws SQLException{  
		    
		  	ArrayList<Integer> File_Ids= new ArrayList<Integer>();
		  
	    	String sql = "SELECT * FROM fileTable";            
	      	Statement stmt  = clientConnection.createStatement();  
	        ResultSet rs = stmt.executeQuery(sql);  
	        
	         //loop through the result set  
	        while (rs.next()) {  
	           
	        	File_Ids.add(rs.getInt("fileID"));
	        	
	        }  
	    	return File_Ids;
	 }
	  
	
    public void newFile(int fileID1, String fileName) throws SQLException { 
    		
        	String sql = "INSERT INTO fileTable VALUES("
        			+ fileID1 +  ", '" + fileName + "', '" + "" + "')";  
            
        	 
        	Statement stmt = clientConnection.createStatement();
        	stmt.executeUpdate(sql);

      } 
    
    
    public void editFile(int fileID1, String newFileContent) {
        String sql = "UPDATE fileTable \n"
        		+ "SET fileContent = '" + newFileContent + "'\n"
                + "WHERE fileID = "  + fileID1 + ";";

        try {
        	PreparedStatement pstmt = clientConnection.prepareStatement(sql);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Could not edit file.");
        }
    }
    
    
    public void deleteFile(int fileID1) throws SQLException {
    	
    	String sql = "DELETE FROM fileTable \n " 
		        +  "WHERE fileID = " + fileID1   ;         
      	Statement stmt  = clientConnection.createStatement();  
        stmt.executeUpdate(sql);
    }
    
        
    public String getContent(int fileID1) throws SQLException{
    	
    	String content = "";
    	
    	String sql = "SELECT fileContent FROM fileTable \n"
    				+ "WHERE fileID = " + fileID1;            
      	Statement stmt  = clientConnection.createStatement();  
        ResultSet rs = stmt.executeQuery(sql);
        
        while (rs.next()) {
        	content = rs.getString("fileContent");
        }
        
    	return content;
    	
    }
    
    
    public void closeClientConnection() {
    	try {
			this.clientConnection.close();
		} catch (SQLException e) {
			System.out.println("The client connection could not be closed.");
		}
    }
}
