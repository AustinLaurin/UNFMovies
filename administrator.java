package unfmovies;

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
            else {
                System.out.println("Your account could not be authenticated.");
                c = null;
            }
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        
    }
    
    public void addMovieToDatabase(String title, String genre, String description, String releaseDate, int isDigital, double purchasePrice, String ageRating, String directorLastName, String directorFirstName, Integer sequelTo, String productionCompany) 
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
                if(daysBetween > 60)
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
                    query = "INSERT INTO MOVIE VALUES(NULL,'" + title + "','" + getGenreIDFromGenre(genre) + "','" + description + "',NULL,'" + releaseDate + "'," + isDigital + ",0," + rentalPrice + "," + purchasePrice + ",'" + ageRating + "'," + directorID + "," + sequelTo + "," + productionCompany + ")";
                else
                    query = "INSERT INTO MOVIE VALUES(NULL,'" + title + "','" + getGenreIDFromGenre(genre) + "','" + description + "',NULL,'" + releaseDate + "'," + isDigital + ",0," + rentalPrice + "," + purchasePrice + ",'" + ageRating + "'," + directorID + "," + sequelTo + ",'" + productionCompany + "')";
                
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
    
    public void increaseInventory(String title) {
        try {
            Statement s = c.createStatement();
            
            //First, let's check if the movie is available in digital format. If so, we do not have physical copies of it.
            //We will also be obtaining the MovieID of the movie and the number of copies.
            //We are going to assume that administrators will only increase and decrease inventories of movies known to be in the database.
            Boolean isDigital = true;
            int MovieID = 0;
            int numberOfCopies = 0;
            String query = "SELECT MovieID, isDigital, numberOfCopies "
                         + "FROM MOVIE "
                         + "WHERE Title = '" + title + "'";
            ResultSet rs = s.executeQuery(query);
            if(rs.next()) {
                isDigital = rs.getBoolean("isDigital");
                MovieID = rs.getInt("MovieID");
                numberOfCopies = rs.getInt("NumberOfCopies");
            }
            
            //Now we will increase the number of movies by one and create a new entry in the SKU table as long as the format isn't digital.
            if(!isDigital) {
                query = "UPDATE MOVIE "
                      + "SET NumberOfCopies = " + ++numberOfCopies + " "
                      + "WHERE Title = '" + title + "'";
                s.executeUpdate(query);
                query = "INSERT INTO SKU_NUMBER VALUES(NULL," + MovieID + ")";
                s.executeUpdate(query);
            }
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            e.getStackTrace();
        }
    }
    
    public void decreaseInventory(String title) {
        try {
            Statement s = c.createStatement();
            
            //First, let's check if the movie is available in digital format. If so, we do not have physical copies of it.
            //We will also be obtaining the MovieID of the movie and the number of copies.
            //We are going to assume that administrators will only increase and decrease inventories of movies known to be in the database.
            Boolean isDigital = true;
            int MovieID = 0;
            int numberOfCopies = 0;
            String query = "SELECT MovieID, isDigital, numberOfCopies "
                         + "FROM MOVIE "
                         + "WHERE Title = '" + title + "'";
            ResultSet rs = s.executeQuery(query);
            if(rs.next()) {
                isDigital = rs.getBoolean("isDigital");
                MovieID = rs.getInt("MovieID");
                numberOfCopies = rs.getInt("NumberOfCopies");
            }
            
            //Now we will increase the number of movies by one and create a new entry in the SKU table as long as the format isn't digital.
            if(!isDigital && numberOfCopies != 0) {
                query = "DELETE "
                      + "FROM SKU_NUMBER "
                      + "WHERE MovieID = " + MovieID + " "
                      + "LIMIT 1";
                s.executeUpdate(query);
                query = "UPDATE MOVIE "
                      + "SET NumberOfCopies = " + --numberOfCopies + " "
                      + "WHERE Title = '" + title + "'";
                s.executeUpdate(query);
            }
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            e.getStackTrace();
        }
    }
    
    public void locateTitle(String title) {
        try {
            Statement s = c.createStatement();
            String query = "SELECT Title, SKU "
                         + "FROM MOVIE AS M JOIN SKU_NUMBER AS SN "
                            + "ON M.MovieID = SN.MovieID";
            ResultSet rs = s.executeQuery(query); 
            System.out.printf("%10s %10s\n", "Title", "SKU");
            while(rs.next()) {
                System.out.printf("%10s %10s\n", rs.getString("Title"), rs.getInt("SKU"));
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void revenueByTitleAndGenre() {
        try {
            Statement s = c.createStatement();
            String query = "SELECT * FROM REVENUE_REPORT_TITLE_AND_GENRE";
            ResultSet rs = s.executeQuery(query);
            System.out.printf("%10s %10s %10s %10s\n", "Title", "Genre", "Total Payment", "Transaction Type");
            while(rs.next()) {
                System.out.printf("%10s %10s %10s %10s\n", rs.getString("Title"), rs.getString("Genre"), rs.getString("TotalPayment"), rs.getString("TransactionType"));
            }
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void revenuePeriodic() {
        try {
            Statement s = c.createStatement();
            String query = "SELECT * FROM REVENUE_REPORT_PERIODIC";
            ResultSet rs = s.executeQuery(query);
            System.out.printf("%10s %10s %10s %10s\n", "Week", "Month", "Year", "Total Payment", "Transaction Type");
            while(rs.next()) {
                System.out.printf("%10s %10s %10s %10s\n", rs.getInt("Week"), rs.getInt("Month"), rs.getInt("Year"), rs.getString("TotalPayment"), rs.getString("TransactionType"));
            }
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void checkUserBalance() {
        try {
            Statement s = c.createStatement();
            String query = "SELECT * FROM USER_BALANCE_VIEW";
            ResultSet rs = s.executeQuery(query);
            System.out.printf("%10s %10s %10s %10s\n", "UserID", "Last Name", "First Name", "Current Bill");
            while(rs.next()) {
                System.out.printf("%10s %10s %10s %10s\n", rs.getString("UserID"), rs.getString("LastName"), rs.getString("FirstName"), rs.getString("CurrentBill"));
            }
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    
    private int getGenreIDFromGenre(String Genre) {
        int GenreID = 0;
        try{
            Statement s = c.createStatement();
            String query = "SELECT * "
                         + "FROM Genre"
                         + "WHERE Genre = '" + Genre + "'";
            ResultSet rs = s.executeQuery(query);
            //There should only be one result. I'm just adding this to keep to form, but rs.next() should really only need to be called once.
            while(rs.next()) {
                GenreID = rs.getInt("GenreID");
            }
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return GenreID;
    }
}
