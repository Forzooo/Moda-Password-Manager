package moda.passwordManager.backend;

import java.sql.*;

public class Database {

    private static final String DB_NAME = "moda-password-manager.db";  // Name of the database file
    private static final String DB_PATH = "./"+DB_NAME;  // Path of the local database
    private static final String TABLE_NAME = "moda";  // Name of the table of the database

    private Connection connection;  // Attribute used to handle all the database queries

    public Database(){
        try {
            // Try to connect to the database if it exists, otherwise create it and then connect to it
            this.connection = DriverManager.getConnection("jdbc:sqlite:"+DB_PATH);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        createTable();  // Create the table of the database if it does not already exist
    }

    public static String getDbName() {
        return Database.DB_NAME;
    }

    public static String getDbPath() {
        return Database.DB_PATH;
    }

    // Create a table, if it does not exist already, used to store all the data
    private void createTable(){
        try {
            Statement query = this.connection.createStatement();  // Define a new query

            query.execute(
                "CREATE TABLE IF NOT EXISTS "+Database.TABLE_NAME+" (" +
                        "     id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "     username_data TEXT," +
                        "     email_address_data TEXT," +
                        "     password_data TEXT," +
                        "     service_data TEXT," +
                        "     additional_data TEXT" +
                        ");"
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Given a string array of 5 elements in the following indexes, add it to the table:
     * <ul>
     *     <li>0 - username_data</li>
     *     <li>1 - email_address_data</li>
     *     <li>2 - password_data</li>
     *     <li>3 - service_data</li>
     *     <li>4 - additional_data</li>
     * </ul>
     * @param data
     */
    public void addData(Data data){

        try {
            // Create an INSERT INTO query
            PreparedStatement query = this.connection.prepareStatement(
                    "INSERT INTO " + Database.TABLE_NAME + " " +
                            "(username_data, email_address_data, password_data," +
                            "service_data, additional_data)" +
                            " VALUES (?, ?, ?, ?, ?)"
            );

            // Add all the data to the query
            query.setString(1, data.getUSERNAME());
            query.setString(2, data.getEMAIL_ADDRESS());
            query.setString(3, data.getPASSWORD());
            query.setString(4, data.getSERVICE());
            query.setString(5, data.getADDITIONAL_DATA());

            // Execute the query
            query.executeUpdate();

            // Close the query
            query.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Delete an entire row based on the id provided
    public void deleteData(int id){
        try {
            PreparedStatement query = this.connection.prepareStatement("DELETE FROM "+Database.TABLE_NAME+" WHERE id=?");
            query.setInt(1, id);  // Set the ID of the row

            query.executeUpdate();  // Execute the query

            // Close the query
            query.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Based on the ID given retrieve all the data of that row
     * @param id
     * @return Data object
     */
    public Data getData(int id){
        String[] data = new String[5];

        try {
            // Read all the data from a row based on its ID
            PreparedStatement query = this.connection.prepareStatement("SELECT * FROM "+Database.TABLE_NAME+" WHERE id=?");
            query.setInt(1, id);

            // Execute the query and retrive the data from the row
            ResultSet queryResult = query.executeQuery();
            data[0] = queryResult.getString("username_data");
            data[1] = queryResult.getString("email_address_data");
            data[2] = queryResult.getString("password_data");
            data[3] = queryResult.getString("service_data");
            data[4] = queryResult.getString("additional_data");

            // Close the query
            query.close();
            queryResult.close();


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Return a Data object containing all the data retrieved
        return new Data(id, data[0], data[1], data[2], data[3], data[4]);
    }

    /**
     * Get each ID and service_data from the Database
     * @return ResultSet
     */
    public ResultSet getFullServiceData(){

        ResultSet queryResult; // The set where are stored the records found in the database

        try {
            PreparedStatement query = this.connection.prepareStatement(
                    "SELECT id, service_data FROM "+Database.TABLE_NAME
            );
            queryResult = query.executeQuery();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return queryResult;
    }

    // Modify the data of a column of a row
    public void changeData(Data data){

        try {
            // Create the UPDATE query and set its parameters
            PreparedStatement query = this.connection.prepareStatement(
                    "UPDATE "+Database.TABLE_NAME+" SET username_data=?, email_address_data=?, password_data=?, service_data=?, additional_data=? WHERE id=?"
            );
            query.setString(1, data.getUSERNAME());
            query.setString(2, data.getEMAIL_ADDRESS());
            query.setString(3, data.getPASSWORD());
            query.setString(4, data.getSERVICE());
            query.setString(5, data.getADDITIONAL_DATA());
            query.setInt(6, data.getID());

            query.executeUpdate();  // Execute the query

            // Close the query
            query.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Return the number of rows inside the database
    public int getRowsNumber(){
        int rowsNumber = 0;  // Initialize the number of rows to 0

        try {
            // Create the SELECT query to get the number of rows
            PreparedStatement query = this.connection.prepareStatement(
                    "SELECT COUNT(id) FROM " + Database.TABLE_NAME
            );
            ResultSet queryResult = query.executeQuery();  // Execute the query

            // If the number of rows exist then move to the next row and read it from the first column
            if (queryResult.next()){
                rowsNumber = queryResult.getInt(1);
            }

            // Close the query
            query.close();
            queryResult.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return rowsNumber;
    }
}
