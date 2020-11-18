package printTable;

import java.util.*;

import java.sql.*;

public class databaseConnectionReference {
    public static void main(String[] args) {
        
        try {
            //You do not need to include the line below.
            //Class.forName("com.mysql.jdbc.Driver");
            
            //This file is for reference. We will likely make a class that manages connections. The database url and login information will need to be updated per machine.
            Connection c = DriverManager.getConnection("jdbc:mysql://cisvm-winsrv-mysql1.unfcsd.unf.edu:3308/group3", "n01040501", "Fall20200501");
        	
        	  Statement s1 = c.createStatement();

            ResultSet rs = s1.executeQuery("SELECT * FROM groupmembers");

            while (rs.next()) {
                String LastName = rs.getString("LastName");
                String FirstName = rs.getString("FirstName");
                String Email = rs.getString("Email");
                System.out.printf("%10s %10s %10s\n", LastName, FirstName, Email);
            }
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
