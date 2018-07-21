import javax.swing.*;
import java.sql.*;
import java.time.LocalDate;

@SuppressWarnings("SqlNoDataSourceInspection")
public class LibraryModel {

    private Connection connection;

    public LibraryModel(String userid, String password) {
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

    public String bookLookup(int isbn) {
        StringBuilder output = new StringBuilder();
        output.append("Book Lookup:\n\t");
        try ( // using try-with-resources to ensure JDBC objects closed when done
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(
                    "SELECT Title, Surname, Edition_No, NumOfCop, NumLeft" +
                    " FROM (BOOK_AUTHOR NATURAL JOIN AUTHOR) NATURAL JOIN BOOK " +
                    "WHERE ISBN = " + isbn + " ORDER BY AuthorSeqNo;")) {

            if (results.next()) {
                output.append(isbn).append(": ")
                        .append(results.getString("Title"))
                        .append("\n\t")
                        .append("Edition: ")
                        .append(results.getInt("Edition_No"))
                        .append("\n\t")
                        .append("Number of copies: ")
                        .append(results.getInt("NumOfCop"))
                        .append("\n\t")
                        .append("Copies remaining: ")
                        .append(results.getInt("NumLeft"))
                        .append("\n\t")
                        .append("Authors: ")
                        .append(results.getString("Surname").trim());
                while (results.next()) {
                    output.append(", ");
                    output.append(results.getString("Surname").trim());
                }
            } else {
                output.append("No book found with ISBN ").append(isbn);
            }
        } catch (SQLException e) {
            System.err.println("SQL Exception occurred during Book Lookup.");
            e.printStackTrace();
        }
        return output.toString();
    }

    public String showCatalogue() {
        StringBuilder output = new StringBuilder();
        output.append("Show Catalogue:\n\t");
        try (
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(
                    "SELECT ISBN, Title, Surname, Edition_No, NumOfCop, NumLeft" +
                            " FROM (BOOK_AUTHOR NATURAL JOIN AUTHOR) NATURAL JOIN BOOK " +
                            "ORDER BY ISBN;")) {

            int lastISBN = -1;
            while (results.next()) {
                int isbn = results.getInt("ISBN");
                if (lastISBN == isbn) {
                    output.append(", ");
                    output.append(results.getString("Surname").trim());
                } else {
                    if (lastISBN != -1) output.append("\n\n\t");
                    output.append(isbn)
                            .append(": ")
                            .append(results.getString("Title"))
                            .append("\n\t")
                            .append("Edition: ")
                            .append(results.getInt("Edition_No"))
                            .append("\n\t")
                            .append("Number of copies: ")
                            .append(results.getInt("NumOfCop"))
                            .append("\n\t")
                            .append("Copies remaining: ")
                            .append(results.getInt("NumLeft"))
                            .append("\n\t")
                            .append("Authors: ")
                            .append(results.getString("Surname").trim());
                }
                lastISBN = isbn;
            }
        } catch (SQLException e) {
            System.err.println("SQL Exception occurred during Show Catalogue.");
            e.printStackTrace();
        }
        return output.toString();
    }

    public String showLoanedBooks() {
        StringBuilder output = new StringBuilder();
        output.append("Show Loaned Books:\n\t");
        try (
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(
                    "SELECT ISBN, Title, CustomerId, F_Name, L_Name, DueDate" +
                            " FROM CUSTOMER " +
                            "NATURAL JOIN (CUST_BOOK NATURAL JOIN BOOK) " +
                            "ORDER BY ISBN;")) {

            int lastISBN = -1;
            if (results.next()) {
                do {
                    int isbn = results.getInt("ISBN");
                    if (lastISBN == isbn) {
                        output.append("\n\t\t")
                                .append(results.getString("F_Name").trim())
                                .append(" ")
                                .append(results.getString("L_Name").trim())
                                .append(" (ID " + results.getInt("CustomerId") + ") ")
                                .append("until " + results.getString("DueDate"));
                    } else {
                        if (lastISBN != -1) output.append("\n\n\t");
                        output.append(isbn)
                                .append(": ")
                                .append(results.getString("Title"))
                                .append("\n\t")
                                .append("On loan to: ")
                                .append("\n\t\t")
                                .append(results.getString("F_Name").trim())
                                .append(" ")
                                .append(results.getString("L_Name").trim())
                                .append(" (ID " + results.getInt("CustomerId") + ") ")
                                .append("until " + results.getString("DueDate"));
                    }
                    lastISBN = isbn;
                } while (results.next());
            } else output.append("No books currently on loan.");
        } catch (SQLException e) {
            System.err.println("SQL Exception occurred during Show Loaned Books.");
            e.printStackTrace();
        }
        return output.toString();
    }

    public String showAuthor(int authorID) {
        StringBuilder output = new StringBuilder();
        output.append("Show Author:\n\t");
        try (
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(
                    "SELECT AUTHOR.AuthorId, Name, Surname, ISBN, Title" +
                            " FROM AUTHOR LEFT JOIN (BOOK_AUTHOR NATURAL JOIN BOOK) " +
                            "ON AUTHOR.AuthorId = BOOK_AUTHOR.AuthorId" +
                            " WHERE AUTHOR.AuthorId = " + authorID +
                            " ORDER BY ISBN;")) {

            // We use a left join here so that authors with no books published are not omitted
            // from the final result.

            boolean printed = false;
            if (results.next()) {
                do {
                    if (!printed) {
                        output.append(results.getInt("AuthorID"))
                                .append(": ")
                                .append(results.getString("Name").trim())
                                .append(" ")
                                .append(results.getString("Surname").trim())
                                .append("\n\n\t");
                        output.append("Books: ")
                                .append("\n\t");
                        printed = true;
                    }
                    if (results.getString("Title") == null) {
                        output.append("No books published.");
                        continue;
                    }
                    output.append(results.getInt("ISBN"))
                            .append(": ")
                            .append(results.getString("Title"));
                    output.append("\n\t");
                } while (results.next());
            } else output.append("No author found with ID " + authorID);
        } catch (SQLException e) {
            System.err.println("SQL Exception occurred during Show Author.");
            e.printStackTrace();
        }
        return output.toString();
    }

    public String showAllAuthors() {
        StringBuilder output = new StringBuilder();
        output.append("Show All Authors:\n\t");
        try (
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(
                    "SELECT * FROM AUTHOR ORDER BY AuthorId;")) {

            while (results.next()) {
                int id = results.getInt("AuthorId");
                if (id <= 0) continue;
                    output.append(id)
                            .append(": ")
                            .append(results.getString("Name").trim())
                            .append(" ")
                            .append(results.getString("Surname").trim())
                            .append("\n\t");
            }
        } catch (SQLException e) {
            System.err.println("SQL Exception occurred during Show All Authors.");
            e.printStackTrace();
        }
        return output.toString();
    }

    public String showCustomer(int customerID) {
        StringBuilder output = new StringBuilder();
        output.append("Show Customer:\n\t");
        try (
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(
                    "SELECT CUSTOMER.CustomerId, F_Name, L_Name, ISBN, Title" +
                            " FROM CUSTOMER LEFT JOIN (CUST_BOOK NATURAL JOIN BOOK) " +
                            "ON CUSTOMER.CustomerId = CUST_BOOK.CustomerId" +
                            " WHERE CUSTOMER.CustomerId = " + customerID +
                            " ORDER BY ISBN;")) {

            boolean printed = false;
            if (results.next()) {
                do {
                    if (!printed) {
                        output.append(results.getInt("CustomerID"))
                                .append(": ")
                                .append(results.getString("F_Name").trim())
                                .append(" ")
                                .append(results.getString("L_Name").trim())
                                .append("\n\n\t");
                        output.append("Books loaned: ")
                                .append("\n\t");
                        printed = true;
                    }
                    if (results.getString("Title") == null) {
                        output.append("No books loaned.");
                        continue;
                    }
                    output.append(results.getInt("ISBN"))
                            .append(": ")
                            .append(results.getString("Title"));
                    output.append("\n\t");
                } while (results.next());
            } else output.append("No customer found with ID " + customerID);
        } catch (SQLException e) {
            System.err.println("SQL Exception occurred during Show Customer");
            e.printStackTrace();
        }
        return output.toString();
    }

    public String showAllCustomers() {
        StringBuilder output = new StringBuilder();
        output.append("Show All Customers:\n\t");
        try (
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(
                    "SELECT * FROM CUSTOMER ORDER BY CustomerId;")) {

            while (results.next()) {
                int id = results.getInt("CustomerId");
                if (id <= 0) continue;
                output.append(id)
                        .append(": ")
                        .append(results.getString("L_Name").trim())
                        .append(", ")
                        .append(results.getString("F_Name").trim())
                        .append("\n\tCity: ")
                        .append(results.getString("City").trim())
                        .append("\n\n\t");
            }
        } catch (SQLException e) {
            System.err.println("SQL Exception occurred during Show All Customers.");
            e.printStackTrace();
        }
        return output.toString();
    }

    /* ========================================= STATE CHANGING ============================================ */

    public String borrowBook(int isbn, int customerID,
                             int day, int month, int year) {
        StringBuilder output = new StringBuilder();
        output.append("Borrow Book:\n\t");

        String valueToInsert = "(" + isbn + ", '" + year + "-" + month + "-" + day
                + "', " + customerID + ")";

        try {
            connection.setAutoCommit(false);

            Statement stmt1 = connection.createStatement();
            ResultSet results1 = stmt1.executeQuery("SELECT * FROM CUSTOMER " +
                    "WHERE CustomerId = " + customerID + " FOR UPDATE;");

            if (!results1.next()) {
                throw new LibraryException
                        ("Customer with ID " + customerID + " does not exist in database");
            }

            Statement stmt2 = connection.createStatement();
            ResultSet results2 = stmt2.executeQuery("SELECT * FROM BOOK " +
                    "WHERE ISBN = " + isbn + " FOR UPDATE;");

            if (!results2.next()) {
                throw new LibraryException
                        ("Book with ISBN " + isbn + " does not exist in database");
            } else if (results2.getInt("NumLeft") == 0) {
                throw new LibraryException
                        ("No copies of this book are available to borrow.");
            } else if (LocalDate.of(year, month, day).isBefore(LocalDate.now())) {
                throw new LibraryException
                        ("Invalid date entered: " + LocalDate.of(year, month, day).toString() +
                        " is before today's date (" + LocalDate.now().toString() + ").");
            }

            if (connection.createStatement().executeQuery("SELECT FROM CUST_BOOK WHERE ISBN = "
                    + isbn + " AND CustomerId = " + customerID + ";").next()) {
                throw new LibraryException
                        ("Customer already has a copy of this book on loan.");
            }

            Statement stmt3 = connection.createStatement();
            stmt3.executeUpdate("INSERT INTO CUST_BOOK VALUES " +
                    valueToInsert + ";");

            int pane = JOptionPane.showConfirmDialog(dialogParent, "Confirm book borrow?",
                    "Are You Sure?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (pane == JOptionPane.NO_OPTION || pane == JOptionPane.CANCEL_OPTION) {
                throw new LibraryException("Operation cancelled by user.");
            }

            Statement stmt4 = connection.createStatement();
            stmt4.executeUpdate("UPDATE BOOK SET NumLeft = (NumLeft - 1) " +
                    "WHERE ISBN = " + isbn + ";");

            // form output for user
            output.append(isbn)
                    .append(": ")
                    .append(results2.getString("Title").trim())
                    .append(" - ")
                    .append(results2.getInt("NumLeft") - 1)
                    .append(" copies remaining.")
                    .append("\n\t")
                    .append("Loaned to: ")
                    .append(results1.getString("F_Name").trim())
                    .append(" ")
                    .append(results1.getString("L_Name").trim())
                    .append(" (ID ")
                    .append(customerID)
                    .append(")\n\t")
                    .append("Loaned until: ")
                    .append(year + "-" + month + "-" + day)
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
            System.err.println("SQL Exception occurred during Borrow Book");
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

    public String returnBook(int isbn, int customerID) {
        StringBuilder output = new StringBuilder();
        output.append("Return Book:\n\t");

        try {
            connection.setAutoCommit(false);

            Statement stmt1 = connection.createStatement();
            ResultSet results1 = stmt1.executeQuery("SELECT * FROM CUSTOMER " +
                    "WHERE CustomerId = " + customerID + " FOR UPDATE;");

            if (!results1.next()) {
                throw new LibraryException
                        ("Customer with ID " + customerID + " does not exist in database");
            }

            Statement stmt2 = connection.createStatement();
            ResultSet results2 = stmt2.executeQuery("SELECT * FROM BOOK " +
                    "WHERE ISBN = " + isbn + " FOR UPDATE;");

            if (!results2.next()) {
                throw new LibraryException
                        ("Book with ISBN " + isbn + " does not exist in database");
            }

            if (!connection.createStatement().executeQuery("SELECT FROM CUST_BOOK WHERE ISBN = "
                    + isbn + " AND CustomerId = " + customerID + ";").next()) {
                throw new LibraryException
                        ("Customer does not have a copy of this book on loan.");
            }

            Statement stmt3 = connection.createStatement();
            stmt3.executeUpdate("DELETE FROM CUST_BOOK WHERE ISBN = "
                    + isbn + " AND CustomerId = " + customerID + ";");

            int pane = JOptionPane.showConfirmDialog(dialogParent, "Confirm book return?",
                    "Are You Sure?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (pane == JOptionPane.NO_OPTION || pane == JOptionPane.CANCEL_OPTION) {
                throw new LibraryException("Operation cancelled by user.");
            }

            Statement stmt4 = connection.createStatement();
            stmt4.executeUpdate("UPDATE BOOK SET NumLeft = (NumLeft + 1) " +
                    "WHERE ISBN = " + isbn + ";");
            output.append(isbn)
                    .append(": ")
                    .append(results2.getString("Title").trim())
                    .append(" - ")
                    .append(results2.getInt("NumLeft") + 1)
                    .append(" copies remaining.")
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
            System.err.println("SQL Exception occurred during Borrow Book");
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

    /* ===================================================================================================== */

    /* For neat handling of problems in state-changing methods. */
    class LibraryException extends Exception {

        LibraryException(String message) {
            super(message);
        }

    }

}