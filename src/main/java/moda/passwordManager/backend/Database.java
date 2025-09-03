package moda.passwordManager.backend;

import java.sql.*;
import java.util.ArrayList;

public class Database {

    private String databasePath;  // Path of the database current in use
    private static final String TABLE_NAME = "moda";  // Name of the table of the database

    private Connection connection;  // Attribute used to handle all the database queries

    public Database(String databasePath){
        this.databasePath = databasePath;  // Set the path of the database

        initConnection();  // Connect to the database
        createTable();  // Create the table of the database if it does not already exist
    }

    /**
     * Try to connect to the database if it exists, otherwise create it and then connect to it
     */
    private void initConnection(){
        try {
            this.connection = DriverManager.getConnection("jdbc:sqlite:"+this.databasePath);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Create a table, if it does not exist already, used to store all the data
    private void createTable(){
        try {
            Statement query = this.connection.createStatement();  // Define a new query

            query.execute(
                "CREATE TABLE IF NOT EXISTS "+Database.TABLE_NAME+" (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "username TEXT," +
                        "email_address TEXT," +
                        "password TEXT," +
                        "service TEXT," +
                        "additional_data TEXT" +
                        ");"
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void changeDatabase(String databasePath){
        this.databasePath = databasePath; // Set the new path of the database
        initConnection();  // Reinitialize the connection
        createTable();  // Create the table inside the database
    }

    /**
     * Add a record to the table
     * @param data Data object to add
     */
    public void addRecord(Data data){

        try {
            // Create an INSERT INTO query
            PreparedStatement query = this.connection.prepareStatement(
                    "INSERT INTO " + Database.TABLE_NAME + " " + "(username, email_address, password, service," +
                            " additional_data) VALUES (?, ?, ?, ?, ?)"
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

    /**
     * Delete a record from the table
     * @param id The record ID
     */
    public void deleteRecord(int id){
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
     * Retrieve all the Data from a record
     * @param id The record ID
     * @return Data object
     */
    public Data getRecord(int id){
        String[] data = new String[5];

        try {
            // Read all the data from a row based on its ID
            PreparedStatement query = this.connection.prepareStatement("SELECT * FROM "+Database.TABLE_NAME+
                    " WHERE id=?");
            query.setInt(1, id);

            // Execute the query and retrive the data from the row
            ResultSet queryResult = query.executeQuery();
            data[0] = queryResult.getString("username");
            data[1] = queryResult.getString("email_address");
            data[2] = queryResult.getString("password");
            data[3] = queryResult.getString("service");
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
     * Change the data fields inside a record
     * @param data The data to replace the previous one
     */
    public void changeRecord(Data data){
        try {
            // Create the UPDATE query and set its parameters
            PreparedStatement query = this.connection.prepareStatement(
                    "UPDATE "+Database.TABLE_NAME+" SET username=?, email_address=?, password=?, service=?," +
                            " additional_data=? WHERE id=?"
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

    /**
     * Retrieve each service field with its ID from the table
     * @return An ArrayList of Data object
     */
    public ArrayList<Data> getServiceFields(){
        ArrayList<Data> serviceFields = new ArrayList<>();  // The service fields are stored here

        try {
            PreparedStatement query = this.connection.prepareStatement(
                    "SELECT id, service FROM "+Database.TABLE_NAME
            );
            ResultSet queryResult = query.executeQuery();  // The set where are stored the records found in the database

            while (queryResult.next()){
                // Retrieve the data for each record and save it inside the ArrayList
                int id = queryResult.getInt("id");
                String service = queryResult.getString("service");

                serviceFields.add(new Data(id, service));
            }

            query.close();
            queryResult.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return serviceFields;
    }
}
