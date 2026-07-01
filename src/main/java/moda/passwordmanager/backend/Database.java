package moda.passwordmanager.backend;

import java.sql.*;
import java.util.ArrayList;

public class Database {

    private String path;  // Path of the database current in use
    private static final String DATA_TABLE = "data";  // The table that contains the Data objects
    private static final String GROUP_TABLE = "groups";  // The table that contains the Groups

    /**
    The table that contains the sensitive settings of the application, which cannot be stored on the settings.json file.
    Lastly, the records are handled by the SensitiveSettings class
     */
    private static final String SENSITIVE_SETTINGS_TABLE = "sensitive_settings";

    private Connection connection;  // Attribute that handles all the database queries

    public Database(String path){
        this.path = path;  // Set the path of the database

        initConnection();  // Connect to the database
        createTables();  // Create the tables of the Vault
    }

    /**
     * Try to connect to the database if it exists, otherwise create it and then connect to it
     */
    private void initConnection(){
        try {
            this.connection = DriverManager.getConnection("jdbc:sqlite:"+this.path);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Terminate the connection with the database
     */
    public void closeConnection(){
        try {
            this.connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create the tables of the database, if they not exist already
     */
    private void createTables(){
        try {
            Statement query = this.connection.createStatement();  // Define a new query

            query.execute(
                    "CREATE TABLE IF NOT EXISTS "+Database.GROUP_TABLE+" (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, " +
                            "name TEXT NOT NULL" +
                        ");"
            );

            query.execute(
                    "CREATE TABLE IF NOT EXISTS "+Database.DATA_TABLE+" (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "username TEXT, " +
                            "email_address TEXT, " +
                            "password TEXT, " +
                            "service TEXT NOT NULL, " +
                            "additional_data TEXT, " +
                            "group_id INTEGER, " +
                            "FOREIGN KEY(group_id) REFERENCES "+Database.GROUP_TABLE+"(id)" +
                        ");"
            );

            query.execute(
                    "CREATE TABLE IF NOT EXISTS "+Database.SENSITIVE_SETTINGS_TABLE +" (" +
                            "propertyPath TEXT PRIMARY KEY NOT NULL UNIQUE," +
                            "value TEXT NOT NULL" +  // All the values are treated as text, and will be cast by Sensitive
                                                     // Settings class
                        ");"
            );

            query.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieve the path of the database in use
     */
    public String getPath(){
        return this.path;
    }

    /**
     * Retrieve the name of the database, including the file extension, from the database path
     * @return String containing the name of the current database
     */
    public String getName(){
        // The name of the database is gotten from the last slash of the path, and the +1 is required to
        // remove the slash from the name
        return this.path.substring(this.path.lastIndexOf("\\")+1);
    }

    /**
     * Change the database in use
     * @param databasePath The path of the new database to use
     */
    public void change(String databasePath){
        closeConnection();  // Close the previous connection
        this.path = databasePath; // Set the new path of the database
        initConnection();  // Reinitialize the connection
        createTables();  // Create the table inside the database
    }

    /**
     * Add a record to the Data table
     * @param data Data object to add
     */
    public void addDataRecord(Data data){

        try {
            // Create an INSERT INTO query
            PreparedStatement query = this.connection.prepareStatement(
                    "INSERT INTO " + Database.DATA_TABLE + " (username, email_address, password, service," +
                            " additional_data) VALUES (?, ?, ?, ?, ?)"
            );

            // Add all the data to the query
            query.setString(1, data.getUSERNAME());
            query.setString(2, data.getEMAIL_ADDRESS());
            query.setString(3, data.getPASSWORD());
            query.setString(4, data.getSERVICE());
            query.setString(5, data.getADDITIONAL_DATA());

            // Execute the query
            query.execute();

            // Close the query
            query.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Add a sensitive setting to the Sensitive Settings table
     * @param propertyPath The path of the setting (ex. google_drive/enabled)
     * @param value The value of the setting
     */
    public void addSensitiveSettingsRecord(String propertyPath, String value){
        try{
            // As the property field of the Sensitive Settings table is unique, if the SensitiveSettings class tries
            // to readd the same property, it is automatically skipped
            PreparedStatement query = this.connection.prepareStatement(
                    "INSERT OR IGNORE INTO "+Database.SENSITIVE_SETTINGS_TABLE+" (propertyPath, value) VALUES (?, ?) ;"
            );

            query.setString(1, propertyPath);
            query.setString(2, value);

            query.execute();
            query.close();
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    /**
     * Delete a record from the Data table
     * @param id The record ID
     */
    public void deleteDataRecord(int id){
        try {
            PreparedStatement query = this.connection.prepareStatement("DELETE FROM "+Database.DATA_TABLE +" WHERE id=?");
            query.setInt(1, id);  // Set the ID of the row

            query.executeUpdate();  // Execute the query

            // Close the query
            query.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Retrieve all the data from a record of the Data table
     * @param id The record ID
     * @return Data object
     */
    public Data getDataRecord(int id){
        String[] data = new String[5];

        try {
            // Read all the data from a row based on its ID
            PreparedStatement query = this.connection.prepareStatement("SELECT * FROM "+Database.DATA_TABLE +
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
     * Get the value of a Sensitive Setting
     * @param propertyPath The path of the property
     * @return The encrypted value
     */
    public String getSensitiveSettingsValue(String propertyPath){
        try {
            PreparedStatement query = this.connection.prepareStatement(
                    "SELECT value FROM " + Database.SENSITIVE_SETTINGS_TABLE + " WHERE propertyPath=?;"
            );

            query.setString(1, propertyPath);
            ResultSet queryResult = query.executeQuery();

            String value = queryResult.getString("value");
            query.close();
            queryResult.close();

            return value;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Change the data fields of a record of the Data table
     * @param data The data to replace the previous one
     */
    public void updateDataRecord(Data data){
        try {
            // Create the UPDATE query and set its parameters
            PreparedStatement query = this.connection.prepareStatement(
                    "UPDATE "+Database.DATA_TABLE +" SET username=?, email_address=?, password=?, service=?," +
                            " additional_data=? WHERE id=?"
            );
            query.setString(1, data.getUSERNAME());
            query.setString(2, data.getEMAIL_ADDRESS());
            query.setString(3, data.getPASSWORD());
            query.setString(4, data.getSERVICE());
            query.setString(5, data.getADDITIONAL_DATA());
            query.setInt(6, data.getID());

            query.execute();
            query.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Change the value field of a record of the Sensitive Settings table
     * @param propertyPath The path of the property
     * @param value The new encrypted value of the property
     * */
    public void updateSensitiveSettingsValue(String propertyPath, String value){
        try {
            PreparedStatement query = this.connection.prepareStatement(
                    "UPDATE "+Database.SENSITIVE_SETTINGS_TABLE+" SET value=? WHERE propertyPath=?;"
            );
            query.setString(1, value);
            query.setString(2, propertyPath);

            query.execute();
            query.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieve the first service field from the Data table
     * @return Data containing the encrypted service field
     */
    public Data getDataFirstServiceField(){
        try{
            // Create the query to retrieve the service
            PreparedStatement query = this.connection.prepareStatement(
                    "SELECT id, service FROM "+ DATA_TABLE + " LIMIT 1"
            );
            ResultSet result = query.executeQuery();  // Execute the query and retrieve the result

            result.next();  // Set the cursor to the row
            int serviceID = result.getInt("id");  // Get the ID
            String service = result.getString("service");  // Get the service

            // Close the query after the end of the operations
            query.close();
            result.close();

            return new Data(serviceID, service);
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
    }


    /**
     * Retrieve each service field with its ID from the Data table
     * @return An ArrayList of Data object
     */
    public ArrayList<Data> getDataServiceFields(){
        ArrayList<Data> serviceFields = new ArrayList<>();  // The service fields are stored here

        try {
            PreparedStatement query = this.connection.prepareStatement(
                    "SELECT id, service FROM "+ DATA_TABLE
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

    /**
     * Retrieve all the records inside the Data table
     * @return ArrayList containing all the Data objects
     */
    public ArrayList<Data> getDataRecords(){
        ArrayList<Data> records = new ArrayList<>();

        try {
            // Read all the records
            PreparedStatement query = this.connection.prepareStatement("SELECT * FROM "+ Database.DATA_TABLE);

            ResultSet queryResult = query.executeQuery();  // Execute the query and retrive all the data

            // Iterate over all the records
            while (queryResult.next()){
                int id = queryResult.getInt("id");
                String username = queryResult.getString("username");
                String emailAddress = queryResult.getString("email_address");
                String password = queryResult.getString("password");
                String service = queryResult.getString("service");
                String additionalData = queryResult.getString("additional_data");

                // Create a data per record and save it inside the ArrayList
                Data data = new Data(id, username, emailAddress, password, service, additionalData);
                records.add(data);
            }

            // Close the queries
            query.close();
            queryResult.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return records;
    }

    /**
     * Change all the records inside the database
     * @param records The new records
     */
    public void changeDataRecords(ArrayList<Data> records){
        try {
            PreparedStatement query = this.connection.prepareStatement(
                    "UPDATE "+Database.DATA_TABLE +" SET username=?, email_address=?, password=?, service=?," +
                            " additional_data=? WHERE id=?"
            );

            // Temporarily set the auto commit to false as we want to execute in a batch group
            this.connection.setAutoCommit(false);

            // Iterate over all the data to commit them together
            for (Data data : records){
                query.setString(1, data.getUSERNAME());
                query.setString(2, data.getEMAIL_ADDRESS());
                query.setString(3, data.getPASSWORD());
                query.setString(4, data.getSERVICE());
                query.setString(5, data.getADDITIONAL_DATA());
                query.setInt(6, data.getID());
                query.addBatch();  // Add the data to the set of the query
            }

            query.executeBatch();  // Execute the query as a batch to group all the updates
            query.close();  // Close the query after the execution

            this.connection.setAutoCommit(true);  // Set again the auto commit to true

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
