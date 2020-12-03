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
                         + "AND firstName = '" + firstName + "' "
                         + "AND encryptedpassword = '" + password + "'";
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
            Double CurrentBill;
            Double LateFees;
            while(rs.next()) {
                CurrentBill = Double.parseDouble(rs.getString("CurrentBill"));
                LateFees = Double.parseDouble(rs.getString("LateFees"));
                CurrentBill += calculateRentDue();
                LateFees += calculateLateFees();
                System.out.printf("%15.2f %15.2f\n", CurrentBill, LateFees);
            }
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void payBalance() {
        try {
            Statement s = c.createStatement();
            String query = "SELECT * "
                         + "FROM USER "
                         + "WHERE UserID = " + UserID;
            ResultSet rs = s.executeQuery(query);
            Double CurrentBill;
            Double LateFees;
            while(rs.next()) {
                CurrentBill = Double.parseDouble(rs.getString("CurrentBill"));
                LateFees = Double.parseDouble(rs.getString("LateFees"));
                CurrentBill += calculateRentDue();
                LateFees += calculateLateFees();
                
                s = c.createStatement();
                query = "UPDATE USER "
                      + "SET CurrentBill = " + CurrentBill + ", LateFees = " + LateFees + " "
                      + "WHERE UserID = " + UserID;
                s.executeUpdate(query);
            }
            //Here we could add something like a function that uses credit card information to bill the customer's checking account.
            //I actually set the bill and the late fees here. If I did that before, I could add late fees on to those late fees. Those values before now only existed in the calculation.
                
            //Now, to show all rentals as returned, set rental return dates, and reset customer balance.
            s = c.createStatement();
            query = "UPDATE USER "  
                  + "SET CurrentBill = 0.00, LateFees = 0.00 "
                  + "WHERE UserID = " + UserID;
            s.executeUpdate(query);
            
            SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");
            s = c.createStatement();
            query = "SELECT * "
                  + "FROM RENTAL AS R JOIN TRANSACTION AS T "
                    + "ON R.TransactionID = T.TransactionID JOIN MOVIE_TRANSACTION "
                        + "ON MOVIE_TRANSACTION.TransactionID = T.TransactionID JOIN MOVIE AS M "
                            + "ON MOVIE_TRANSACTION.MovieID = M.MovieID "
                  + "WHERE UserID = " + UserID + " "
                  + "AND R.IsPaid = 0";
            rs = s.executeQuery(query);
            
            while(rs.next()) {
                //If this works properly, we are calculating the rent due and the late fees due for specific rental transactions and setting
                //the total payment to the sum of all those fees. If we don't, it won't show in reports.
                s = c.createStatement();
                query = "UPDATE TRANSACTION "
                      + "SET TotalPayment = " + (calculateRentDue(rs.getInt("R.TransactionID"), rs.getInt("R.ItemNumber")) + calculateLateFees(rs.getInt("R.TransactionID"), rs.getInt("R.ItemNumber"))) + " "
                      + "WHERE TransactionID = " + rs.getInt("R.TransactionID") + " "
                      + "AND ItemNumber = " + rs.getInt("R.ItemNumber");
                s.executeUpdate(query);
                
                s = c.createStatement();
                if(((int) Math.ceil(daysBetween(rs.getString("T.TransactionDate")))) < 15) { 
                    query = "UPDATE RENTAL "
                          + "SET IsPaid = 1, DateReturned = '" + myFormat.format(new java.util.Date(System.currentTimeMillis())) + "' "
                          + "WHERE TransactionID = " + rs.getInt("R.TransactionID");
                }
                else {
                    query = "UPDATE RENTAL "
                          + "SET IsPaid = 1 "
                          + "WHERE TransactionID = " + rs.getInt("R.TransactionID");
                }
                if(!isDigital(rs.getInt("MovieID")) && ((int) Math.ceil(daysBetween(rs.getString("T.TransactionDate")))) < 15)
                    increaseQuantity(rs.getInt("MovieID"));
                s.executeUpdate(query);
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
                         + "FROM MOVIE JOIN GENRE "
                            + "ON MOVIE.GenreID = GENRE.GenreID "
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
        
        if(checkAvailability(MovieID) && !checkDuplicateMovie(MovieID) && checkMoviesRented() <= 2 && !(checkMoviesRented() == 2 && transactionType == 1) && checkBalanceAndLateFees()) {
            if(cart == null)
                cart = new ArrayList();
            ArrayList<Integer> movie = new ArrayList<>();
            movie.add(MovieID);
            movie.add(transactionType);
            
            cart.add(movie);
            System.out.println("Your movie was successfully added to your cart.");
        }
        else
            System.out.println("Your movie could not be added to your cart.");
    }
    
    public void checkout() {
        if(!(cart != null && !cart.isEmpty()))
            System.out.println("Your cart is currently empty.");
        else {
            try {
                Boolean first = true;
                int ItemNumber = 1;
                int TransactionID = 0;
                
                //Obtain the current date information and create a format that will be accepted by MySQL.
                SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");
                java.util.Date currentDate = new java.util.Date(System.currentTimeMillis());
                
                Statement s;
                String query;
                
                //Determine the transaction type.
                String transactionType;
                
                String price;
                Double totalPrice = 0.00;
                String dueDate = null;
                
                for(ArrayList a: cart) {    
                    int MovieID = (int)(a.toArray()[0]);

                    //The price will either by the full price of the movie or 0.00 if the movie is a rental because the price is calculated when the bill is paid (I may update this when the bill is paid).
                    if((int)(a.toArray()[1]) == 0) {
                        transactionType = "Purchase";
                        s = c.createStatement();
                        query = "SELECT * "
                              + "FROM MOVIE "
                              + "WHERE MovieID = " + MovieID;
                        ResultSet rs = s.executeQuery(query);
                        rs.next();
                        price = rs.getString("PurchasePrice");
                    }
                    else {
                        transactionType = "Rental";
                        s = c.createStatement();
                        query = "SELECT * "
                              + "FROM MOVIE "
                              + "WHERE MovieID = " + MovieID;
                        ResultSet rs = s.executeQuery(query);
                        rs.next();
                        price = "0.00";
                        
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        Calendar c = Calendar.getInstance();
                        c.setTime(new java.util.Date(System.currentTimeMillis()));
                        c.add(Calendar.DATE, rs.getInt("MaximumRentalPeriodDays"));
                        dueDate = sdf.format(c.getTime());  
                    }
                    
                    //Insert into the transaction table.
                    if(first) {
                        s = c.createStatement();
                        query = "INSERT INTO TRANSACTION VALUES(NULL," + ItemNumber + ",'" + myFormat.format(currentDate) + "','" + transactionType + "'," + price + ",NULL," + UserID + ")";
                        s.executeUpdate(query);
                        first = false;
                        
                        s = c.createStatement();
                        query = "SELECT * "
                              + "FROM TRANSACTION";
                        ResultSet rs = s.executeQuery(query);
                        while(rs.next()) {
                            TransactionID = rs.getInt("TransactionID");
                        }
                    }
                    else {
                        s = c.createStatement();
                        query = "INSERT INTO TRANSACTION VALUES(" + TransactionID + "," + ItemNumber + ",'" + myFormat.format(currentDate) + "','" + transactionType + "'," + price + ",NULL," + UserID + ")";
                        s.executeUpdate(query);
                    }
                    
                    //Oh boy, do it for the subtypes too. Screw comments. Kill me. This method is already over 50 lines long. If I see a tunnel with a light at the end of it, I'm walking towards it.
                    if(transactionType.equals("Purchase")) {
                        s = c.createStatement();
                        query = "INSERT INTO PURCHASE VALUES(" + TransactionID + "," + ItemNumber + ",'" + myFormat.format(currentDate) + "',0,0)";
                        s.executeUpdate(query);
                    }
                    else {
                        s = c.createStatement();
                        query = "INSERT INTO RENTAL VALUES(" + TransactionID + "," + ItemNumber + ",'" + dueDate + "',NULL,0)";
                        s.executeUpdate(query);
                    }
                    
                    s = c.createStatement();
                    query = "INSERT INTO MOVIE_TRANSACTION VALUES(" + (int)(a.toArray()[0]) + "," + TransactionID + ")";
                    s.executeUpdate(query);
                    
                    if(!isDigital(MovieID))
                        reduceQuantity(MovieID);
                    
                    ItemNumber++;
                    totalPrice += Double.parseDouble(price);
                }
                //This could overwrite a non-zero balance that a customer has with a new value, but we don't let customers make purchases without a balance of 0.00, so that can't happen.
                s = c.createStatement();
                query = "UPDATE USER "
                      + "SET CurrentBill = " + totalPrice + " "
                      + "WHERE UserID = " + UserID;
                s.executeUpdate(query);
            }
            catch(Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            System.out.println("Your transaction was successful.");
        }
    }
    
    public void makeReturn(int MovieID) {
        try {
            Statement s = c.createStatement();
            String query = "SELECT * "
                         + "FROM TRANSACTION AS T JOIN PURCHASE AS P "
                            + "ON T.TransactionID = P.TransactionID JOIN MOVIE_TRANSACTION "
                                + "ON MOVIE_TRANSACTION.TransactionID = T.TransactionID "
                         + "WHERE UserID = " + UserID + " "
                         + "AND MovieID = " + MovieID;
            ResultSet rs = s.executeQuery(query);
            if(rs.next()) {
                if(checkBalanceAndLateFees() && ((int) Math.ceil(daysBetween(rs.getString("T.TransactionDate")))) <= 30 && !rs.getBoolean("HasWatched") && !rs.getBoolean("HasDownloaded")) {
                    int TransactionID = rs.getInt("T.TransactionID");
                    s = c.createStatement();
                    query = "DELETE " + 
                            "FROM PURCHASE "
                          + "WHERE TransactionID = " + TransactionID;
                    s.executeUpdate(query);
                    
                    query = "DELETE " + 
                            "FROM MOVIE_TRANSACTION "
                          + "WHERE TransactionID = " + TransactionID;
                    s.executeUpdate(query);

                    s = c.createStatement();
                    query = "DELETE " + 
                            "FROM TRANSACTION "
                          + "WHERE TransactionID = " + TransactionID;
                    s.executeUpdate(query);
                    if(!isDigital(MovieID))
                        increaseQuantity(MovieID);

                    System.out.println("Movie successfully returned.");
                }
                else
                    System.out.println("The movie cannot be returned.");
            }
            else
                System.out.println("We're sorry, but we have no record of you purchasing this movie.");
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    
    public int getMovieIDFromTitle(String title) {
        int MovieID = 0;
        try {
            Statement s = c.createStatement();
            String query = "SELECT * "
                         + "FROM MOVIE "
                         + "WHERE Title = '" + title + "'";
            ResultSet rs = s.executeQuery(query);
            if(rs.next())
               MovieID = rs.getInt("MovieID");
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return MovieID;
    }
    
    //Movies can only be added to cart one time. 
    private Boolean checkDuplicateMovie(int MovieID) {
        if(cart != null) {
            for(ArrayList a: cart) {
                if((int)(a.toArray()[0]) == MovieID) {
                    System.out.println("This movie is already in your cart.");
                    return true;
                }
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
    
    private Boolean checkAvailability(int MovieID) {
        Boolean available = false;
        try {
            Statement s = c.createStatement();
            String query = "SELECT * "
                         + "FROM MOVIE "
                         + "WHERE MovieID = " + MovieID;
            ResultSet rs = s.executeQuery(query);
            rs.next();
            if(rs.getInt("NumberOfCopies") > 0 || isDigital(MovieID))
                available = true;
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return available;
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
    
    private Boolean isDigital(int MovieID) {
        Boolean isDigital = false;
        try {
            Statement s = c.createStatement();
            String query = "SELECT * "
                         + "FROM MOVIE "
                         + "WHERE MovieID = " + MovieID;
            ResultSet rs = s.executeQuery(query);
            rs.next();
            if(rs.getBoolean("isDigital"))
                isDigital = true;
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return isDigital;
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
    
    private void increaseQuantity(int MovieID) {
        try {
            Statement s = c.createStatement();
            String query = "SELECT * "
                         + "FROM MOVIE "
                         + "WHERE MovieID = " + MovieID;
            ResultSet rs = s.executeQuery(query);
            rs.next();
            int NumberOfCopies = rs.getInt("NumberOfCopies");
            
            s = c.createStatement();
            query = "UPDATE MOVIE "
                  + "SET NumberOfCopies = " + ++NumberOfCopies + " "
                  + "WHERE MovieID = " + MovieID + " ";
            s.executeUpdate(query);
            
            s = c.createStatement();
            query = "INSERT INTO SKU_NUMBER VALUES(NULL," + MovieID + ")";
            s.executeUpdate(query);
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void reduceQuantity(int MovieID) {
        try {
            Statement s = c.createStatement();
            String query = "SELECT * "
                         + "FROM MOVIE "
                         + "WHERE MovieID = " + MovieID;
            ResultSet rs = s.executeQuery(query);
            rs.next();
            int NumberOfCopies = rs.getInt("NumberOfCopies");
            
            s = c.createStatement();
            query = "UPDATE MOVIE "
                  + "SET NumberOfCopies = " + --NumberOfCopies + " "
                  + "WHERE MovieID = " + MovieID + " ";
            s.executeUpdate(query);
            
            s = c.createStatement();
            query = "DELETE "
                  + "FROM SKU_NUMBER "
                  + "WHERE MovieID = " + MovieID + " "
                  + "LIMIT 1";
            s.executeUpdate(query);
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    
    private Double calculateRentDue() {
        Double RentDue = 0.00;
        try {
            Statement s = c.createStatement();
            String query = "SELECT * "
                         + "FROM TRANSACTION AS T JOIN RENTAL AS R "
                            + "ON T.TransactionID = R.TransactionID JOIN MOVIE_TRANSACTION "
                                + "ON T.TransactionID = MOVIE_TRANSACTION.TransactionID JOIN MOVIE AS M "
                                    + "ON MOVIE_TRANSACTION.MovieID = M.MovieID "
                         + "WHERE T.UserID = " + UserID + " "
                         + "AND IsPaid = 0";
            ResultSet rs = s.executeQuery(query);
            while(rs.next()) {
                int iterations = Math.min((int) Math.ceil(daysBetween(rs.getString("T.TransactionDate"))),rs.getInt("M.MaximumRentalPeriodDays") - 1) + 1;
                
                for(int i = 0; i < iterations; i++) {
                    RentDue += rs.getDouble("M.RentalPrice");
                }
            }
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return RentDue;
    }
    
    private Double calculateLateFees() {
        Double LateFees = 0.00;
        try {
            Statement s = c.createStatement();
            String query = "SELECT * "
                         + "FROM TRANSACTION AS T JOIN RENTAL AS R "
                            + "ON T.TransactionID = R.TransactionID JOIN MOVIE_TRANSACTION "
                                + "ON T.TransactionID = MOVIE_TRANSACTION.TransactionID JOIN MOVIE AS M "
                                    + "ON MOVIE_TRANSACTION.MovieID = M.MovieID "
                         + "WHERE T.UserID = " + UserID + " "
                         + "AND R.IsPaid = 0 ";
            ResultSet rs = s.executeQuery(query);
            while(rs.next()) {
                int iterations = Math.min((int) Math.ceil(daysBetween(rs.getString("T.TransactionDate"))),15) + 1;
                if(iterations < 15) {
                    for(int i = rs.getInt("M.MaximumRentalPeriodDays"); i < iterations; i++) {
                        LateFees += rs.getDouble("M.LateFeeRate");
                    }
                }
                else
                    LateFees = rs.getDouble("M.PurchasePrice");
            }
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return LateFees;
    }
    
    private Double calculateRentDue(int TransactionID, int ItemNumber) {
        Double RentDue = 0.00;
        try {
            Statement s = c.createStatement();
            String query = "SELECT * "
                         + "FROM TRANSACTION AS T JOIN RENTAL AS R "
                            + "ON T.TransactionID = R.TransactionID JOIN MOVIE_TRANSACTION "
                                + "ON T.TransactionID = MOVIE_TRANSACTION.TransactionID JOIN MOVIE AS M "
                                    + "ON MOVIE_TRANSACTION.MovieID = M.MovieID "
                         + "WHERE T.UserID = " + UserID + " "
                         + "AND IsPaid = 0 "
                         + "AND R.TransactionID = " + TransactionID + " "
                         + "AND R.ItemNumber = " + ItemNumber;
            ResultSet rs = s.executeQuery(query);
            while(rs.next()) {
                int iterations = Math.min((int) Math.ceil(daysBetween(rs.getString("T.TransactionDate"))),rs.getInt("M.MaximumRentalPeriodDays") - 1) + 1;
                
                for(int i = 0; i < iterations; i++) {
                    RentDue += rs.getDouble("M.RentalPrice");
                }
            }
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return RentDue;
    }
    
    private Double calculateLateFees(int TransactionID, int ItemNumber) {
        Double LateFees = 0.00;
        try {
            Statement s = c.createStatement();
            String query = "SELECT * "
                         + "FROM TRANSACTION AS T JOIN RENTAL AS R "
                            + "ON T.TransactionID = R.TransactionID JOIN MOVIE_TRANSACTION "
                                + "ON T.TransactionID = MOVIE_TRANSACTION.TransactionID JOIN MOVIE AS M "
                                    + "ON MOVIE_TRANSACTION.MovieID = M.MovieID "
                         + "WHERE T.UserID = " + UserID + " "
                         + "AND R.IsPaid = 0 "
                         + "AND R.TransactionID = " + TransactionID + " "
                         + "AND R.ItemNumber = " + ItemNumber;
            ResultSet rs = s.executeQuery(query);
            while(rs.next()) {
                int iterations = Math.min((int) Math.ceil(daysBetween(rs.getString("T.TransactionDate"))),15) + 1;
                if(iterations < 15) {
                    for(int i = rs.getInt("M.MaximumRentalPeriodDays"); i < iterations; i++) {
                        LateFees += rs.getDouble("M.LateFeeRate");
                    }
                }
                else
                    LateFees = rs.getDouble("M.PurchasePrice");
            }
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return LateFees;
    }
    
    private float daysBetween(String transactionDate) {
        float daysBetween = 0;
        try {
            SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date initialDate = myFormat.parse(transactionDate);
            java.util.Date currentDate = new java.util.Date(System.currentTimeMillis());
            long difference = currentDate.getTime() - initialDate.getTime();
            daysBetween = (difference / (1000*60*60*24));
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
        }
        return daysBetween;
    }
}
