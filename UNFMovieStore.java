
package unfmoviestore;
import java.util.Scanner;

public class UNFMovieStore {


    public static void main(String[] args) {
        // TODO code application logic here
        Scanner input = new Scanner (System.in);
        System.out.print("Please enter your role, 'employee' or 'customer': ");
        switch (input.next()) {
            case "employee":
                {
                    System.out.print("Enter your last name: ");
                    String lname = input.next();
                    System.out.print("Enter your first name: ");
                    String fname = input.next();
                    System.out.print("Enter your password: ");
                    String password = input.next();
                    administrator a = new administrator(lname, fname, password);
                    
                    int i = 0;
                    while(i < 1){
                    System.out.print(fname+", please select an action. Enter 'locate title', 'update inventory', 'manage customer balance', or 'generate reports': ");
                    input = new Scanner (System.in);
                    switch (input.nextLine()){
                        case "locate title":{
                        //call locateTitle prints SKU
                            System.out.print("Enter the title you want to locate: ");
                            String title = input.nextLine();
                            a.locateTitle(title);
                            i = 0;
                            break;
                        }
                        case "update inventory":{
                        //call deleteMovieFromDatabase then call decreaseInventory?
                        //call addMovieToDatabase then call increaseInventory?
                            System.out.print("would you like to 'add' or 'delete' a movie or 'manage inventory'? ");
                            input = new Scanner (System.in);
                            switch (input.nextLine()){
                                case "add":{
                                    System.out.println("Provide the following details for the movie you would like to add.");
                                    System.out.print("Title: ");
                                    String title = input.nextLine();
                                    System.out.print("Genre: ");
                                    String genre = input.nextLine();
                                    System.out.print("Description: ");
                                    String descr = input.nextLine();
                                    System.out.print("Release Date: ");
                                    String release = input.nextLine();
                                    System.out.print("Is it Digital?(enter 'yes' or 'no') ");
                                    int digital = -1;
                                    input = new Scanner (System.in);
                                    switch (input.next()){
                                        case "yes":{
                                            digital = 1;
                                            break;
                                        }
                                        case "no":{
                                            digital = 0;
                                            break;
                                        }
                                    }
                                    System.out.print("Is the movie a new or old release?(enter 'new' or 'old') ");
                                    double oldRate = 0.00;
                                    double newRate = 0.00;
                                    int oldPeriod = 0;
                                    int newPeriod = 0;
                                    input = new Scanner (System.in);
                                    switch (input.next()){
                                        case "new":{
                                            System.out.print("Enter the rental rate for the movie: ");
                                            newRate = input.nextDouble();
                                            System.out.print("Enter the rental period for the movie in days: ");
                                            newPeriod = input.nextInt();
                                            break;
                                        }
                                        case "old":{
                                            System.out.print("Enter the rental rate for the movie: ");
                                            oldRate = input.nextDouble();
                                            System.out.print("Enter the rental period for the movie in days: ");
                                            oldPeriod = input.nextInt();
                                            break;
                                        }
                                    }
                                    System.out.print("What would you like the late fee rate to be? ");
                                    double lateFeeRate = input.nextDouble();
                                    System.out.print("Purchase Price: ");
                                    double price = input.nextDouble();
                                    System.out.print("Age Rating: ");
                                    String ageRate = input.next();
                                    System.out.print("Last Name of the Director: ");
                                    String directorLast = input.next();
                                    System.out.print("First Name of the Director: ");
                                    String directorFirst = input.next();
                                    System.out.print("Is this movie is a sequel?(enter 'yes' or 'no') ");
                                    String sequel = null;
                                    Integer sequelTo = null;
                                    input = new Scanner (System.in);
                                    switch (input.next()){
                                        case "yes":{
                                            System.out.print("Enter the ID of the prequel to this movie: ");
                                            sequel = input.next();
                                            sequelTo = Integer.valueOf(sequel);
                                            break;
                                        }
                                        case "no":{
                                            sequelTo = null;
                                            break;
                                       }  
                                    }
                                    //int sequelTo = Integer.valueOf(sequel);
                                    System.out.print("Production Company: ");
                                    String prodCo = input.nextLine();
                                    a.addMovieToDatabase(title, genre, descr, release, digital, oldRate, newRate, oldPeriod, newPeriod, lateFeeRate, price, ageRate, directorLast, directorFirst, sequelTo, prodCo);
                                    i = 0;
                                    break;
                                }
                                case "delete":{
                                    System.out.print("What is the title of the movie you want to delete? ");
                                    String title = input.nextLine();
                                    a.deleteMovieFromDatabase(title);
                                    i = 0;
                                    break;
                                }
                                case "manage inventory":{
                                    System.out.print("Would you like to increase or decrease the inventory for a movie?(enter 'increase' or 'decrease') ");
                                    input = new Scanner (System.in);
                                    switch (input.next()){
                                        case "increase":{
                                            System.out.print("What is the title of the movie you want to increase the inventory for? ");
                                            input = new Scanner (System.in);
                                            String title = input.nextLine();
                                            a.increaseInventory(title);
                                            i = 0;
                                            break;
                                        }
                                        case "decrease":{
                                            System.out.print("What is the title of the movie you want to decrease the inventory for? ");
                                            input = new Scanner (System.in);
                                            String title = input.nextLine();
                                            System.out.print("What is the SKU of the movie you want to decrease the inventory for? ");
                                            input = new Scanner (System.in);
                                            int sku = input.nextInt();
                                            a.decreaseInventory(title, sku);
                                            i = 0;
                                            break;
                                        }
                                    }
                                    i = 0;
                                    break;
                                }
                            }
                            i = 0;
                            break;
                        }
                        case "manage customer balance":{
                        //call checkUserBalance
                        //call updateRentalRate
                        //call updateLateFee
                        System.out.print("Would you like to check user balance, update a the rental rate or update a late fee?(enter 'user balance', 'rental rate' or 'late fee') ");
                            input = new Scanner (System.in);
                            switch (input.nextLine()){
                                case "user balance":{
                                    a.checkUserBalance();
                                    i = 0;
                                    break;
                                }
                                case "rental rate":{
                                    System.out.print("What is old rate? ");
                                        double oldRate = input.nextDouble();
                                    System.out.print("What would you like to be new rate? ");
                                        double newRate = input.nextDouble();
                                    a.updateRentalRate(oldRate, newRate);
                                    i = 0;
                                    break;
                                }
                                case "late fee":{
                                    System.out.print("What would you like the new fee amount to be? ");
                                        double fee = input.nextDouble();
                                    System.out.print("What would you like the minimum rental period to be?(in days) ");
                                        int min = input.nextInt();
                                    System.out.print("What would you like the maximum rental period to be?(in days) ");
                                        int max = input.nextInt();
                                    a.updateLateFee(fee, max, max);
                                    i = 0;
                                    break;
                                }
                            }
                            i = 0;
                            break;
                        }
                        case "generate reports":{
                        //call RevenueByTitleAndGenre and use getGenreIDFromGenre??
                        //call RevenuePeriodic
                        System.out.print("How do you want to view reports?(enter 'by title' or 'by period') ");
                            input = new Scanner (System.in);
                            switch (input.nextLine()){
                                case "by title":{
                                    a.revenueByTitleAndGenre();
                                    i = 0;
                                    break;
                                }
                                case "by period":{
                                    a.revenuePeriodic();
                                    i = 0;
                                    break;
                                }
                            }
                            i = 0;
                            break;
                        }
                    
                    }
                }
                break;
                }
            case "customer":
                {
                    System.out.print("Enter your last name: ");
                    String lname = input.next();
                    System.out.print("Enter your first name: ");
                    String fname = input.next();
                    System.out.print("Enter your password: ");
                    String password = input.next();
                    customer c;
                    c = new customer(lname, fname, password);
                    
                    int i = 0;
                    while(i < 1){
                    System.out.print(fname+", please select an action. Enter enter 'search movies', 'checkout', 'return', 'manage balance',or 'write review': ");
                    input = new Scanner (System.in);
                    switch (input.nextLine()){
                        case "search movies":{
                        //call searchMovieDatabase
                            System.out.print("Enter the title you would like to search for: ");
                            String search = input.nextLine();
                            c.searchMovieDatabase(search);
                            i = 0;
                            break;
                        }
                        case "checkout":{
                        //call addMovieToCart
                        //call checkout
                            int x = 0;
                            while(x < 1){
                                System.out.print("Would you like to 'rent' or 'buy': ");
                                int type = -1;
                                input = new Scanner (System.in);
                                switch (input.next()){
                                    case "rent":{
                                        type = 1;
                                        break;
                                    }
                                    case "buy":{
                                       type = 0;
                                       break;
                                    }
                                }   
                            
                                System.out.print("Enter the title you would like to add to your cart: ");
                                String title = input.nextLine();
                                int id = c.getMovieIDFromTitle(title);
                                c.addMovieToCart(id, type);
                            
                                System.out.print("Are you ready to check out? (enter 'yes' or 'no') ");
                                input = new Scanner (System.in);
                                switch (input.next()){
                                    case "yes":{
                                        x = 1;
                                        c.checkout();
                                        break;
                                    }
                                    case "no":{
                                        x = 0;
                                        break;
                                    }
                                }
                            }
                            i = 0;
                            break;
                        }
                        case "return":{
                        //call makeReturn
                        System.out.print("Enter the title you would like to return: ");
                            String title = input.nextLine();
                            int id = c.getMovieIDFromTitle(title);
                            c.makeReturn(id);
                            i = 0;
                            break;
                        }
                        case "manage balance":{
                        //call checkBalance
                        //call payBalance
                        System.out.print("Would you like to check or pay your balance? (enter 'check' or 'pay') ");
                        input = new Scanner (System.in);
                        switch (input.next()){
                                case "check":{
                                    c.checkBalance();
                                    break;
                                }
                                case "pay":{
                                    c.payBalance();
                                    break;
                                }
                            }
                            i = 0;
                            break;
                        }
                        case "write review":{
                        //call writeReview
                        System.out.print("Enter the title of the movie you would like to review: ");
                        String title = input.nextLine();
                        System.out.print("Enter the content of the review: ");
                        String text = input.nextLine();
                        System.out.print("Enter the score of the movie: ");
                        int score = input.nextInt();
                        c.writeReview(text, score, title);
                        i = 0;
                        break;
                        }
                    }
                }   
                break;
                }
            default:
                System.out.println("Please try again and enter either 'employee' or 'customer'");
                break;
        }
    }
    
}

