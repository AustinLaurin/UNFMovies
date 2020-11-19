package unfmovies;

import java.sql.Connection;
import java.sql.DriverManager;

public class databaseConnection {
    public static Connection getConnection() {
        try{
        //Make sure you login using the appropriate database url, account name, and account password for the corresponding data. 
        //The connection is supplied by this method to create ease of changing databases. Without, you would have to change it in every place a connection is made.
         return DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/unfmovies", "root", "password");
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
