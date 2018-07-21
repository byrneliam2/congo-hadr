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

    public void updateResourceCount(String org, String resource, int quantity) {
        try {
            connection.setAutoCommit(false);

<<<<<<< HEAD
            Statement statement = connection.createStatement();
            statement.executeUpdate(
                "UPDATE RESOURCES SET Quantity = " + quantity +
                "WHERE Organisation = " + org + " AND Resource = " + resource + ";");
<<<<<<< HEAD

=======
            
>>>>>>> 449c70c93f362b4833303a3fa476ba9a02e93887
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
=======
            Statement statement1 = connection.createStatement();
            ResultSet results1 = statement.executeQuery(
                "SELECT * FROM RESOURCES " + "WHERE Organisation = " + org + 
                " AND Resource = " + resource + " FOR UPDATE;");
>>>>>>> 3d16f692c49ddc089f1e2f97f326dbabc9c910fe

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

            statementt2.close();
            results1.close();
            statementt1.close();
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

    class NgoAPIException extends Exception {

        NgoAPIException(String message) {
            super(message);
        }
    }

}
