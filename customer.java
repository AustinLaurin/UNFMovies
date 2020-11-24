package unfmovies;

import java.util.*;
import java.sql.*;
import java.text.*;

public class customer {
    private int UserID;
    private Connection c;
    private ArrayList<ArrayList<Integer>> cart;
    
    customer(String lastName, String firstName, String password) {
        //You will need to put the details of the MySQL database that you are using.
        try {
            //First argument is the database url, second is the account, third is the password.
            c = databaseConnection.getConnection();
            Statement s = c.createStatement();
            String query = "SELECT UserID "
                         + "FROM User "
                         + "WHERE lastName = '" + lastName + "' "
                         + "AND firstName='" + firstName 
                         + "' AND encryptedpassword='" + password + "'";
            ResultSet rs = s.executeQuery(query);
            if(rs.next()) 
                UserID = rs.getInt("UserID");
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
    
    public void checkBalance() {
        try {
            Statement s = c.createStatement();
            String query = "SELECT * "
                         + "FROM USER "
                         + "WHERE UserID = " + UserID;
            ResultSet rs = s.executeQuery(query);
            System.out.printf("%15s %15s\n", "Current Bill", "Late Fees");
            while(rs.next()) {
                System.out.printf("%15s %15s\n", rs.getString("CurrentBill"), rs.getString("Late Fees"));
            }
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void searchMovieDatabase(String title) {
        try {
            updateMovieScores();
            Statement s = c.createStatement();
            String query = "SELECT * "
                         + "FROM MOVIE "
                         + "WHERE title LIKE '%" + title + "%'";
            ResultSet rs = s.executeQuery(query);
            
            System.out.printf("%15s %15s %15s %15s %15s %15s %15s %15s %15s %15s\n", "Title", "Genre", "Movie Score", "Date Released", "Format", "Available", "Rental Price", "Purchase Price", "Rating", "Director");
            while(rs.next()) {
                String DirectorName = getDirectorName(rs.getInt("DirectorID"));
                if(rs.getString("isDigital").equals("0"))
                    System.out.printf("%15s %15s %15s %15s %15s %15s %15s %15s %15s %15s\n", rs.getString("Title"), rs.getString("Genre"), rs.getString("MovieScore"), rs.getString("ReleaseDate"), "Physical", rs.getString("NumberOfCopies"), rs.getString("RentalPrice"), rs.getString("PurchasePrice"), rs.getString("AgeRating"), DirectorName);
                else
                    System.out.printf("%15s %15s %15s %15s %15s %15s %15s %15s %15s %15s\n", rs.getString("Title"), rs.getString("Genre"), rs.getString("MovieScore"), rs.getString("ReleaseDate"), "Digital", "INF", rs.getString("RentalPrice"), rs.getString("PurchasePrice"), rs.getString("AgeRating"), DirectorName);
            }
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void writeReview(String reviewText, int reviewScore, String movieTitle) {
        try {
            Statement s = c.createStatement(); 
            String query = "SELECT MovieID "
                         + "FROM MOVIE "
                         + "WHERE Title = '" + movieTitle + "'";
            ResultSet rs = s.executeQuery(query);
            rs.next();
            int MovieID = rs.getInt("MovieID");
            query = "INSERT INTO REVIEW VALUES(NULL," + reviewScore + ",'" + reviewText + "'," + MovieID + "," + UserID + ")";
            s.executeUpdate(query);
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    
    //Transaction type is 0 for purchase and 1 for rental.
    public void addMovieToCart(int MovieID, int transactionType) {
        if(!checkDuplicateMovie(MovieID) && checkMoviesRented() <= 2 && !(checkMoviesRented() == 2 && transactionType == 1) && checkBalanceAndLateFees()) {
            if(cart == null)
                cart = new ArrayList();
            ArrayList<Integer> movie = new ArrayList<>();
            movie.add(MovieID);
            movie.add(transactionType);

            cart.add(movie);
            System.out.println("Your move was successfully added to your cart.");
        }
        else
            System.out.println("Your movie could not be added to your cart.");
    }
    
    //Movies can only be added to cart one time. 
    private Boolean checkDuplicateMovie(int MovieID) {
        for(ArrayList a: cart) {
            if((int)(a.toArray()[0]) == MovieID) {
                System.out.println("This movie is already in your cart.");
                return true;
            }
        }
        return false;
    }
    
    private int checkMoviesRented() {
        int moviesRented = 0;
        try {
            Statement s = c.createStatement();
            String query = "SELECT * "
                         + "FROM USER AS U JOIN TRANSACTION AS T "
                            + "ON U.UserID = T.UserID JOIN RENTAL AS R "
                                + "ON T.TransactionID = R.TransactionID "
                         + "WHERE U.UserID = " + UserID + " "
                         + "AND IsPaid = 0";
            ResultSet rs = s.executeQuery(query);
            while(rs.next()) {
                moviesRented++;
            }
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        
        return moviesRented;
    }
    
    private Boolean checkBalanceAndLateFees() {
        Boolean clear = true;
        try {
            Statement s = c.createStatement();
            String query = "SELECT CurrentBill, LateFees "
                         + "FROM USER "
                         + "WHERE UserID = " + UserID;
            ResultSet rs = s.executeQuery(query);
            while(rs.next()) {
                if(rs.getFloat("CurrentBill") > 0.00 || rs.getFloat("LateFees") > 0.00)
                    clear = false;
            }
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return clear;
    }
    
    private void updateMovieScores() {
        try {
            Statement s = c.createStatement();
            String query = "SELECT MovieID, ROUND(AVG(ReviewScore),2) AS 'MovieScore' "
                         + "FROM REVIEW "
                         + "GROUP BY MovieID";
            ResultSet rs = s.executeQuery(query);
            
            while(rs.next()) {
                s = c.createStatement();
                query = "UPDATE MOVIE "
                      + "SET MovieScore = " + rs.getString("MovieScore") + " " 
                      + "WHERE MovieID = " + rs.getString("MovieID") + " ";
                s.executeUpdate(query);
            }
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    
    private String getDirectorName(int DirectorID) {
        String DirectorName = null;
        try {
            Statement s = c.createStatement();
            String query = "SELECT FirstName, LastName "
                         + "FROM DIRECTOR "
                         + "WHERE DirectorID = " + DirectorID;
            ResultSet rs = s.executeQuery(query);
            rs.next();
            DirectorName = rs.getString("FirstName") + " " + rs.getString("LastName");
            
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return DirectorName;
    }
}
