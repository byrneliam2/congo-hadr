import javax.swing.*;
import java.sql.*;
import java.time.LocalDate;

import org.json.simple.JSONObject;

/**
 * Java API for CoNGO database service. Allows operations on data, including
 * generic queries, updating data and deleting records.
 */
public class CongoAPI {

    private Connection connection;

    /**
     * Constructor takes a user ID and password. In practice, this would be
     * far more secure! This may not even be needed since a private system
     * would be used as opposed to an existing enterprise system.
     */
    public CongoAPI(String userid, String password) {
        getConnection(userid, password);
    }

    /**
     * Make the connection to the database. Once again, this would be
     * far different in practice.
     */
    private void getConnection(String userid, String password) {
        try {
            Class.forName("org.postgresql.Driver");

            String url = "jdbc:postgresql://db.ecs.vuw.ac.nz/congo-hadr";
            this.connection = DriverManager.getConnection(url, userid, password);
        } catch (ClassNotFoundException e) {
            System.err.println("Cannot find the PostgreSQL driver. Try checking your CLASSPATH.");
        } catch (SQLException e) {
            System.err.println("Cannot get connection to database: " + e.getMessage());
        }
    }

    /* ======================= SIMPLE QUERIES: RESOURCES ====================== */

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
     * Returns a JSON object that in this instance holds mappings of
     * resource to quantity.
     */
    public JSONObject getResourceCount(String org) {
        JSONObject json = new JSONObject();
        try (
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(
                "SELECT Resource, SUM(Quantity) As SumQ FROM RESOURCES " +
                "WHERE Organisation = \'" + org + "\' GROUP BY " +
                "Resource ORDER BY Resource ASC;")) {
            while (results.next()) {
                json.put(results.getString("Resource"), results.getInt("SumQ"));
            }
        } catch (SQLException e) {
            System.err.println("SQL Exception occurred");
            e.printStackTrace();
        }
        return json;
    }

    /**
     * Retrieve resources and their counts for all organisations.
     * Returns a JSON object that in this instance holds mappings of
     * resource to quantity within organisations. Note that because
     * organisations and resources are keys, this essentially returns
     * the entire dataset.
     */
    public JSONObject getResourceCounts() {
        JSONObject json = new JSONObject();
        try (
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(
                "SELECT Organisation, Resource, SUM(Quantity) As SumQ FROM RESOURCES " +
                "GROUP BY Organisation, Resource ORDER BY Organisation ASC;")) {
            while (results.next()) {
                // json.put(results.getString("Resource"), results.getInt("SumQ"));
                JSONObject j = new JSONObject();
                j.put(results.getString("Resource"), results.getInt("SumQ"));
                json.put(results.getString("Organisation"), j);
            }
        } catch (SQLException e) {
            System.err.println("SQL Exception occurred");
            e.printStackTrace();
        }
        return json;
    }

    /**
     * Retrieve total resource count for a given organisation.
     * Returns a JSON object that in this instance holds a singleton
     * mapping with the total resource count for that organisation.
     */
    public JSONObject getTotalResourceCount(String org) {
        JSONObject json = new JSONObject();
        try (
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(
                "SELECT SUM(Quantity) AS SumQ FROM RESOURCES " +
                "WHERE Organisation = \'" + org + "\' GROUP BY Organisation;")) {
            while (results.next()) {
                json.put("total", results.getInt("SumQ"));
            }
        } catch (SQLException e) {
            System.err.println("SQL Exception occurred");
            e.printStackTrace();
        }
        return json;
    }

    /**
     * Retrieve total resource counts for all organisations.
     * Returns a JSON object that in this instance holds a singleton
     * mapping with the total resource count for that organisation.
     */
    public JSONObject getTotalResourceCounts(String org) {
        JSONObject json = new JSONObject();
        try (
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(
                "SELECT Organisation, SUM(Quantity) AS SumQ FROM RESOURCES " +
                "GROUP BY Organisation;")) {
            while (results.next()) {
                // json.put("total", results.getInt("SumQ"));
                JSONObject j = new JSONObject();
                j.put("total", results.getInt("SumQ"));
                json.put(results.getString("Organisation"), j);
            }
        } catch (SQLException e) {
            System.err.println("SQL Exception occurred");
            e.printStackTrace();
        }
        return json;
    }

    /* ======================= STATE CHANGING: RESOURCES ====================== */

    /**
     * Insert a resource record for a given organisation and resource. 
     */
    public void insertResourceRecord(String org, String resource, int quantity, String description) {
        String vals = "\'" + org + "\',\'" + resource + "\'," + quantity + ",\'" + description "\'";
        
        try {
            connection.setAutoCommit(false);

            Statement statement1 = connection.createStatement();
            ResultSet results1 = statement.executeQuery(
                "SELECT * FROM RESOURCES " + "WHERE Organisation = " + org + 
                " AND Resource = " + resource + " FOR UPDATE;");

            if (!results1.next()) {
                throw new CongoAPIException
                        ("Resource record already exists with values [org = " + org +
                        ", resource = " + resource + "]. Try updating the record instead.");
            }

            Statement statement2 = connection.createStatement();
            statement2.executeUpdate("INSERT INTO RESOURCES " +
                    "VALUES (" + vals + ");");

            connection.commit();

            statement2.close();
            results1.close();
            statement1.close();
        } catch (CongoAPIException e) {
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
                throw new CongoAPIException
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
        } catch (CongoAPIException e) {
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
    public void deleteResourceRecord(String org, String resource) {
        try {
            connection.setAutoCommit(false);

            Statement statement1 = connection.createStatement();
            ResultSet results1 = statement.executeQuery(
                "SELECT * FROM RESOURCES " + "WHERE Organisation = " + org + 
                " AND Resource = " + resource + " FOR UPDATE;");

            if (!results1.next()) {
                throw new CongoAPIException
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
        } catch (CongoAPIException e) {
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
    }

    /* ======================= SIMPLE QUERIES: LOCATIONS ====================== */

    public JSONObject getOrganisationsAndLocations() {
        return null;
    }

    public JSONObject getResourcesPerLocation() {
        return null;
    }

    /* ======================= STATE CHANGING: LOCATIONS ====================== */

    public void insertLocationRecord(String org, String resource, int quantity, String location) {
        //
    }

    public void updateRecordLocation(String org, String resource, String location) {
        //
    }

    public void deleteLocationRecord(String org, String resource, String location) {
        //
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
    class CongoAPIException extends Exception {

        CongoAPIException(String message) {
            super(message);
        }
    }

}
