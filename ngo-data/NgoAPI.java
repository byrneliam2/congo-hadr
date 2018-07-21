import javax.swing.*;
import java.sql.*;
import java.time.LocalDate;

import org.json.simple.JSONObject;

/**
 * Java API for NGO database service. Allows operations on data, including
 * generic queries, updating data and deleting records.
 */
public class NgoAPI {

    private Connection connection;

    /**
     * Constructor takes a user ID and password. In practice, this would be
     * far more secure! This may not even be needed since a private system
     * would be used as opposed to an existing enterprise system.
     */
    public NgoAPI(String userid, String password) {
        getConnection(userid, password);
    }

    /**
     * Make the connection to the database. Once again, this would be
     * far different in practice.
     */
    private void getConnection(String userid, String password) {
        try {
            Class.forName("org.postgresql.Driver");

            String url = "jdbc:postgresql://db.ecs.vuw.ac.nz/startup-hadr";
            this.connection = DriverManager.getConnection(url, userid, password);
        } catch (ClassNotFoundException e) {
            System.err.println("Cannot find the PostgreSQL driver. Try checking your CLASSPATH.");
        } catch (SQLException e) {
            System.err.println("Cannot get connection to database: " + e.getMessage());
        }
    }

/* ======================================================================== */

    /**
     * Simple query to retrieve all organisations listed in the database.
     * Returns a JSON object that in this instance holds the list of organisations
     * in an array.
     */
    public JSONObject getAllOrganisations() {
        JSONObject json = new JSONObject();
        ArrayList<String> list = new ArrayList<>();

        try (
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(
                "SELECT DISTINCT Organisation FROM RESOURCES ORDER BY Organisation ASC;")) {
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

    /**
     * Retrieve resources and their counts for a given organisation.
     * Returns a JSON object that in this instance holds //.
     */
    public JSONObject getResourceCount(String org) {
        JSONObject json = new JSONObject();
        String[][] TotalResources;
        int i = 0;
        try (
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(
                "<query goes here>")) {
            while (results.next()) {
                // use results.XXX() to do stuff
                TotalResources[i]=results.getString("Resource");
                TotalResources[i][]=Integer.toString(results.getInt("Quantity"));
                i ++;
            }
            json.put(org, TotalResources); // use this to put values into JSON object
        } catch (SQLException e) {
            System.err.println("SQL Exception occurred");
            e.printStackTrace();
        }
        return json;
    }

    /**
     * Retrieve total resource count for a given organisation.
     * Returns a JSON object that in this instance holds //.
     */
    public JSONObject getTotalResourceCount(String org) {
        JSONObject json = new JSONObject();
        int rescount = 0;
        try (
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(
                "<query goes here>")) {
            while (results.next()) {
                // use results.XXX() to do stuff
                int Trescount = results.getInt("Quantity");
                int rescount;
                rescount += Trescount;
            }
            json.put(org, rescount); // use this to put values into JSON object
        } catch (SQLException e) {
            System.err.println("SQL Exception occurred");
            e.printStackTrace();
        }
        return json;
    }

    /**
     * Update the resource count for a given organisation and resource to
     * the quantity supplied. This method will throw an exception if the
     * organisation/resource combination does not exist.
     */
    public void updateResourceCount(String org, String resource, int quantity) {
        try {
            connection.setAutoCommit(false);

            Statement statement1 = connection.createStatement();
            ResultSet results1 = statement.executeQuery(
                "SELECT * FROM RESOURCES " + "WHERE Organisation = " + org + 
                " AND Resource = " + resource + " FOR UPDATE;");

            if (!results1.next()) {
                throw new NgoAPIException
                        ("Resource record does not exist with values [org = " + org +
                        ", resource = " + resource + "]");
            }

            Statement statement2 = connection.createStatement();
            statement2.executeUpdate(
                "UPDATE RESOURCES SET Quantity = " + quantity +
                "WHERE Organisation = " + org + " AND Resource = " + resource + ";");
            
            connection.commit();

            statement2.close();
            results1.close();
            statement1.close();
        } catch (NgoAPIException e) {
            output.append("Error: ").append(e.getMessage());
            try {
                connection.rollback();
            } catch (SQLException e1) {
                System.err.println("SQL Exception occurred during rollback");
                e1.printStackTrace();
            }
            return output.toString();
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

    /**
     * Delete the resource record for a given organisation and resource. 
     * This method will throw an exception if the organisation/resource 
     * combination does not exist.
     */
    public String deleteResourceRecord(String org, String resource) {
        try {
            connection.setAutoCommit(false);

            Statement statement1 = connection.createStatement();
            ResultSet results1 = statement.executeQuery(
                "SELECT * FROM RESOURCES " + "WHERE Organisation = " + org + 
                " AND Resource = " + resource + " FOR UPDATE;");

            if (!results1.next()) {
                throw new NgoAPIException
                        ("Resource record does not exist with values [org = " + org +
                        ", resource = " + resource + "]");
            }

            Statement statement2 = connection.createStatement();
            statement2.executeUpdate("DELETE FROM RESOURCES " +
                    "WHERE Organisation = " + org + 
                    " AND Resource = " + resource + ";");

            connection.commit();

            statement2.close();
            results1.close();
            statement1.close();
        } catch (NgoAPIException e) {
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

/* ======================================================================== */

    public void closeDBConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            System.err.println("SQL Exception occurred while closing.");
            e.printStackTrace();
        }
    }

    /**
     * Custom exception to handle missing value cases.
     */
    class NgoAPIException extends Exception {

        NgoAPIException(String message) {
            super(message);
        }
    }

}
