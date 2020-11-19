import java.util.*;
import java.sql.*;
import java.text.*;

public class administrator {
    private int EmployeeID;
    private Connection c;
    
    administrator(String lastName, String firstName, String password) {
        //You will need to put the details of the MySQL database that you are using.
        try {
            //First argument is the database url, second is the account, third is the password.
            c = databaseConnection.getConnection();
            Statement s = c.createStatement();
            String query = "SELECT EmployeeID "
                         + "FROM Employee "
                         + "WHERE lastName = '" + lastName + "' "
                         + "AND firstName='" + firstName 
                         + "' AND encryptedpassword='" + password + "'";
            ResultSet rs = s.executeQuery(query);
            if(rs.next()) 
                EmployeeID = rs.getInt("EmployeeID");
            else
                System.out.println("Your account could not be authenticated.");
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        
    }
    
    public void addMovieToDatabase(String title, String genre, String description, String releaseDate, int isDigital, int numberOfCopies, double purchasePrice, String ageRating, String directorLastName, String directorFirstName, Integer sequelTo, String productionCompany) 
    {
        try {
            Statement s = c.createStatement();
            String query = "SELECT DirectorID "
                         + "FROM DIRECTOR "
                         + "WHERE LastName = '" + directorLastName + "'"
                         + "AND FirstName = '" + directorFirstName + "'";
            ResultSet rs = s.executeQuery(query);
            if(rs.next()) {
                int directorID = rs.getInt("DirectorID");
                SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-dd-MM");
                java.util.Date initialDate = myFormat.parse(releaseDate);
                java.util.Date currentDate = new java.util.Date(System.currentTimeMillis());
                long difference = currentDate.getTime() - initialDate.getTime();
                float daysBetween = (difference / (1000*60*60*24));
                
                double rentalPrice;               
                if(daysBetween >= 60)
                    rentalPrice = 3.00;
                else
                    rentalPrice = 4.50;
                
                if(productionCompany != null) {
                    Integer ProductionID = null;
                    query = "SELECT ProductionID"
                          + "FROM PRODUCTION_COMPANY"
                          + "WHERE Headquarters = '" + productionCompany + "'";
                    s.executeQuery(query);
                    if(rs.next())
                        ProductionID = rs.getInt("ProductionID");
                }
                
                if(productionCompany == null)
                    query = "INSERT INTO MOVIE VALUES(NULL,'" + title + "','" + genre + "','" + description + "',NULL,'" + releaseDate + "'," + isDigital + "," + numberOfCopies + "," + rentalPrice + "," + purchasePrice + ",'" + ageRating + "'," + directorID + "," + sequelTo + "," + productionCompany + ")";
                else
                    query = "INSERT INTO MOVIE VALUES(NULL,'" + title + "','" + genre + "','" + description + "',NULL,'" + releaseDate + "'," + isDigital + "," + numberOfCopies + "," + rentalPrice + "," + purchasePrice + ",'" + ageRating + "'," + directorID + "," + sequelTo + ",'" + productionCompany + "')";
                
                s.executeUpdate(query);
            }
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void deleteMovieFromDatabase(String title) {
        try {
            Statement s = c.createStatement();
            String query = "DELETE "
                         + "FROM MOVIE "
                         + "WHERE Title = '" + title + "'";
            s.executeUpdate(query);
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            e.getStackTrace();
        }
    }
}
