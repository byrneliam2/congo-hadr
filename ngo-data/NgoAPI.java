import javax.swing.*;
import java.sql.*;
import java.time.LocalDate;

import org.json.simple.JSONObject;

@SuppressWarnings("SqlNoDataSourceInspection")
public class NgoAPI {

    private Connection connection;

    public NgoAPI(String userid, String password) {
        getConnection(userid, password);
    }

    private void getConnection(String userid, String password) {
        try {
            Class.forName("org.postgresql.Driver");

            String url = "jdbc:postgresql://db.ecs.vuw.ac.nz/" + userid + "_jdbc";
            this.connection = DriverManager.getConnection(url, userid, password);
        } catch (ClassNotFoundException e) {
            System.err.println("Cannot find the PostgreSQL driver. Try checking your CLASSPATH.");
        } catch (SQLException e) {
            System.err.println("Cannot get connection to database: " + e.getMessage());
        }
    }

/* ======================================================================== */

    public JSONObject getAllOrganisations() {
        JSONObject json = new JSONObject();
        ArrayList<String> list = new ArrayList<>();

        try (
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(
                "SELECT UNIQUE Organisation FROM RESOURCES ASC")) {
            while (results.next()) {
                list.add(results.getString("Organisation"));
            }
            json.put("orgs", list);
        } catch (SQLException e) {
            System.err.println("SQL Exception occurred");
            e.printStackTrace();
        }
        return json;
    }

    public JSONObject getTotalResourceCount(String org) {
        JSONObject json = new JSONObject();

        try (
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(
                "<query goes here>")) {
            while (results.next()) {
                // use results.XXX() to do stuff
            }
            json.put("<name>", null); // use this to put values into JSON object
        } catch (SQLException e) {
            System.err.println("SQL Exception occurred");
            e.printStackTrace();
        }
        return json;
    }

    public void updateResourceCount(String org, String resource, String quantity) {
        try {
            connection.setAutoCommit(false);

            Statement statement = connection.createStatement();
            statement.executeUpdate(
                "UPDATE RESOURCES SET Quantity = " + quantity +
                "WHERE Organisation = " + org + " AND Resource = " + resource + ";");
            
            connection.commit();
            statement.close();
        } catch (SQLException e) {
            System.err.println("SQL Exception occurred");
            e.printStackTrace();
            try {
                connection.rollback();
            } catch (SQLException e1) {
                System.err.println("SQL Exception occurred during rollback");
                e1.printStackTrace();
            }
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("SQL Exception occurred during setting of auto commit");
                e.printStackTrace();
            }
        }
    }

/* ======================================================================== */

    public void closeDBConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            System.err.println("SQL Exception occurred while closing.");
            e.printStackTrace();
        }
    }

    public String deleteCus(int customerID) {
        StringBuilder output = new StringBuilder();
        output.append("Delete Customer:\n\t");

        int loansDeleted = 0;

        try {
            connection.setAutoCommit(false);

            Statement stmt1 = connection.createStatement();
            ResultSet results1 = stmt1.executeQuery("SELECT FROM CUSTOMER WHERE " +
                    "CustomerId = " + customerID + " FOR UPDATE;");

            if (!results1.next()) {
                throw new LibraryException
                        ("Customer with ID " + customerID + " does not exist in database");
            }

            Statement stmt2 = connection.createStatement();
            ResultSet results2 = stmt2.executeQuery("SELECT * FROM CUST_BOOK " +
                    "WHERE CustomerId = " + customerID + " FOR UPDATE;");

            if (results2.next()) {
                do {
                    Statement stmt2a = connection.createStatement();
                    stmt2a.executeUpdate("UPDATE BOOK SET NumLeft = (NumLeft + 1) " +
                            "WHERE ISBN = " + results2.getInt("ISBN") + ";");
                    stmt2a.close();
                } while (results2.next());
                loansDeleted = stmt2.executeUpdate("DELETE FROM CUST_BOOK " +
                        "WHERE CustomerId = " + customerID + ";");
            }

            int pane = JOptionPane.showConfirmDialog(dialogParent, "Confirm customer delete?",
                    "Are You Sure?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (pane == JOptionPane.NO_OPTION || pane == JOptionPane.CANCEL_OPTION) {
                throw new LibraryException("Operation cancelled by user.");
            }

            Statement stmt3 = connection.createStatement();
            stmt3.executeUpdate("DELETE FROM CUSTOMER " +
                    "WHERE CustomerId = " + customerID + ";");
            output.append("Customer with ID ")
                    .append(customerID)
                    .append(" has been deleted.")
                    .append("\n\t")
                    .append(loansDeleted)
                    .append(" loans were also recorded as returned.")
                    .append("\n\t");

            connection.commit();

            stmt3.close();
            results2.close();
            stmt2.close();
            results1.close();
            stmt1.close();
        } catch (LibraryException e) {
            output.append("Error: ").append(e.getMessage());
            try {
                connection.rollback();
            } catch (SQLException e1) {
                System.err.println("SQL Exception occurred during rollback");
                e1.printStackTrace();
            }
            return output.toString();
        } catch (SQLException e) {
            System.err.println("SQL Exception occurred during Delete Customer");
            e.printStackTrace();
            try {
                connection.rollback();
            } catch (SQLException e1) {
                System.err.println("SQL Exception occurred during rollback");
                e1.printStackTrace();
            }
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("SQL Exception occurred during setting of auto commit");
                e.printStackTrace();
            }
        }
        return output.toString();
    }

    public String deleteAuthor(int authorID) {
        StringBuilder output = new StringBuilder();
        output.append("Delete Author:\n\t");

        int authDeleted = 0;

        try {
            connection.setAutoCommit(false);

            Statement stmt1 = connection.createStatement();
            ResultSet results1 = stmt1.executeQuery("SELECT FROM AUTHOR WHERE " +
                    "AuthorId = " + authorID + " FOR UPDATE;");

            if (!results1.next()) {
                throw new LibraryException
                        ("Author with ID " + authorID + " does not exist in database");
            }

            Statement stmt2 = connection.createStatement();
            ResultSet results2 = stmt2.executeQuery("SELECT * FROM BOOK_AUTHOR " +
                    "WHERE AuthorId = " + authorID + " FOR UPDATE;");

            if (results2.next()) {
                authDeleted = stmt2.executeUpdate("DELETE FROM BOOK_AUTHOR " +
                        "WHERE AuthorId = " + authorID + ";");
            }

            int pane = JOptionPane.showConfirmDialog(dialogParent, "Confirm author delete?",
                    "Are You Sure?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (pane == JOptionPane.NO_OPTION || pane == JOptionPane.CANCEL_OPTION) {
                throw new LibraryException("Operation cancelled by user.");
            }

            Statement stmt3 = connection.createStatement();
            stmt3.executeUpdate("DELETE FROM AUTHOR " +
                    "WHERE AuthorId = " + authorID + ";");
            output.append("Author with ID ")
                    .append(authorID)
                    .append(" has been deleted.")
                    .append("\n\t")
                    .append(authDeleted)
                    .append(" author records were also deleted.")
                    .append("\n\t");

            connection.commit();

            stmt3.close();
            results2.close();
            stmt2.close();
            results1.close();
            stmt1.close();
        } catch (LibraryException e) {
            output.append("Error: ").append(e.getMessage());
            try {
                connection.rollback();
            } catch (SQLException e1) {
                System.err.println("SQL Exception occurred during rollback");
                e1.printStackTrace();
            }
            return output.toString();
        } catch (SQLException e) {
            System.err.println("SQL Exception occurred during Delete Author");
            e.printStackTrace();
            try {
                connection.rollback();
            } catch (SQLException e1) {
                System.err.println("SQL Exception occurred during rollback");
                e1.printStackTrace();
            }
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("SQL Exception occurred during setting of auto commit");
                e.printStackTrace();
            }
        }
        return output.toString();
    }

    public String deleteBook(int isbn) {
        StringBuilder output = new StringBuilder();
        output.append("Delete Book:\n\t");

        int authDeleted = 0;
        int loansDeleted = 0;

        try {
            connection.setAutoCommit(false);

            Statement stmt1 = connection.createStatement();
            ResultSet results1 = stmt1.executeQuery("SELECT FROM BOOK WHERE " +
                    "ISBN = " + isbn + " FOR UPDATE;");

            if (!results1.next()) {
                throw new LibraryException
                        ("Book with ISBN " + isbn + " does not exist in database");
            }

            Statement stmt2 = connection.createStatement();
            ResultSet results2 = stmt2.executeQuery("SELECT * FROM BOOK_AUTHOR " +
                    "WHERE ISBN = " + isbn + " FOR UPDATE;");

            if (results2.next()) {
                authDeleted = stmt2.executeUpdate("DELETE FROM BOOK_AUTHOR " +
                        "WHERE ISBN = " + isbn + ";");
            }

            Statement stmt3 = connection.createStatement();
            ResultSet results3 = stmt3.executeQuery("SELECT FROM CUST_BOOK WHERE " +
                    "ISBN = " + isbn + " FOR UPDATE;");

            if (results3.next()) {
                do {
                    Statement stmt3a = connection.createStatement();
                    stmt3a.executeUpdate("UPDATE BOOK SET NumLeft = (NumLeft + 1) " +
                            "WHERE ISBN = " + isbn + ";");
                    stmt3a.close();
                } while (results3.next());
                loansDeleted = stmt3.executeUpdate("DELETE FROM CUST_BOOK " +
                        "WHERE ISBN = " + isbn + ";");
            }

            int pane = JOptionPane.showConfirmDialog(dialogParent, "Confirm book delete?",
                    "Are You Sure?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (pane == JOptionPane.NO_OPTION || pane == JOptionPane.CANCEL_OPTION) {
                throw new LibraryException("Operation cancelled by user.");
            }

            Statement stmt4 = connection.createStatement();
            stmt4.executeUpdate("DELETE FROM BOOK " +
                    "WHERE ISBN = " + isbn + ";");
            output.append("Book with ISBN ")
                    .append(isbn)
                    .append(" has been deleted.")
                    .append("\n\t")
                    .append(authDeleted)
                    .append(" author records were also deleted.")
                    .append("\n\t")
                    .append(loansDeleted)
                    .append(" loans were also recorded as returned.")
                    .append("\n\t");

            connection.commit();

            stmt4.close();
            stmt3.close();
            results2.close();
            stmt2.close();
            results1.close();
            stmt1.close();
        } catch (LibraryException e) {
            output.append("Error: ").append(e.getMessage());
            try {
                connection.rollback();
            } catch (SQLException e1) {
                System.err.println("SQL Exception occurred during rollback");
                e1.printStackTrace();
            }
            return output.toString();
        } catch (SQLException e) {
            System.err.println("SQL Exception occurred during Delete Book");
            e.printStackTrace();
            try {
                connection.rollback();
            } catch (SQLException e1) {
                System.err.println("SQL Exception occurred during rollback");
                e1.printStackTrace();
            }
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("SQL Exception occurred during setting of auto commit");
                e.printStackTrace();
            }
        }
        return output.toString();
    }

}