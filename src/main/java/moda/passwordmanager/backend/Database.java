package moda.passwordmanager.backend;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {

    private String path;  // Path of the database current in use
    private static final String FILE_EXTENSION = ".modb";  // The extension of any password manager database
    private static final int PRAGMA_USER_VERSION = 1;  // The current version of the schema of the database
                                                       // To know the history of the versions check the docs

    private static final String DATA_TABLE = "data";  // The table that contains the Data objects

    /**
    The table that contains the sensitive settings of the application, which cannot be stored on the settings.json file.
    Lastly, the records are handled by the SensitiveSettings class
     */
    private static final String SENSITIVE_SETTINGS_TABLE = "sensitive_settings";

    private Connection connection;  // Attribute that handles all the database queries

    public Database(String path){
        this.path = path;  // Set the path of the database
        init();  // Initializes the database
    }

    /**
     * Initialize the database
     */
    private void init(){
        initConnection();  // Start the connection

        // We have to check whether the database has just been created
        if (isNew()){
            createTables();  // Add all the tables to the database
            updateToLatestVersion();  // Update the version of the database to the latest one
        }

        // We check the version of the database to upgrade it if it's in an older version
        if (!isLatestVersion()){
            createTables();  // Create the new tables
            updateFromOldVersion();  // Update the old tables to the latest schema
            updateToLatestVersion();  // Update the version of the database to the latest one
        }
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
     * Change the database in use
     * @param databasePath The path of the new database to use
     */
    public void change(String databasePath){
        closeConnection();  // Close the previous connection
        this.path = databasePath; // Set the new path of the database
        init();  // Reinitialize the connection with the database
    }

    /**
     * Closes the connection with the database and deletes it from the file system
     */
    public void delete(){
        closeConnection();  // First we close the connection to avoid further errors
        try {
            Files.delete(Path.of(this.path));
        } catch (IOException e) {
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
     * Get the file extension of any database file of the password manager
     */
    public static String getFileExtension(){
        return FILE_EXTENSION;
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
     * Checks whether the database has just been created
     */
    private boolean isNew(){
        try {
            Statement query = this.connection.createStatement();

            // To check if the database is new we can count the number of tables that are inside the database
            // and are not sqlite ones
            ResultSet resultSet = query.executeQuery(
                    "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite%';"
            );

            int numberOfTables = resultSet.getInt(1);

            query.close();

            return numberOfTables == 0;  // If it's equal to 0 then it's new
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks whether the database is updated to the latest version
     */
    private boolean isLatestVersion(){
        try {
            Statement query = this.connection.createStatement();

            // To know the version we check it from the PRAGMA user_version contained in the database
            ResultSet resultSet = query.executeQuery("PRAGMA user_version");

            int userVersion = resultSet.getInt(1);
            query.close();

            return userVersion == PRAGMA_USER_VERSION;
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
                    "CREATE TABLE IF NOT EXISTS "+Database.DATA_TABLE+" (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "username TEXT, " +
                            "email_address TEXT, " +
                            "password TEXT, " +
                            "service TEXT NOT NULL, " +
                            "additional_data TEXT" +
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

    private void updateFromOldVersion(){
        try {
            Statement query = this.connection.createStatement();  // Define a new query

            query.execute("INSERT INTO " + Database.DATA_TABLE + " SELECT * FROM moda");

            query.execute("DROP TABLE moda");

            query.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Update the database version to the latest one
     */
    private void updateToLatestVersion(){
        try {
            Statement query = this.connection.createStatement();
            query.execute("PRAGMA user_version="+PRAGMA_USER_VERSION);  // We set the value of the PRAGMA user_version
                                                                            // to the latest one
            query.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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
            query.setString(1, data.getUsername());
            query.setString(2, data.getEmailAddress());
            query.setString(3, data.getPassword());
            query.setString(4, data.getService());
            query.setString(5, data.getAdditionalData());

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
     * Get the autoincrement integer value of the Data table
     * @return
     */
    public int getAutoincrementDataTable(){
        try {
            // To retrieve the value we have to use the columns name and seq of the table sqlite_sequence
            PreparedStatement query = this.connection.prepareStatement("SELECT seq FROM sqlite_sequence WHERE" +
                    " name=?;");
            query.setString(1, DATA_TABLE);

            ResultSet resultSet = query.executeQuery();
            query.close();

            return resultSet.getInt("seq");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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
            query.setString(1, data.getUsername());
            query.setString(2, data.getEmailAddress());
            query.setString(3, data.getPassword());
            query.setString(4, data.getService());
            query.setString(5, data.getAdditionalData());
            query.setInt(6, data.getId());

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
     */
    public List<Data> getDataServiceFields(){
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
    public List<Data> getDataRecords(){
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
    public void changeDataRecords(List<Data> records){
        try {
            PreparedStatement query = this.connection.prepareStatement(
                    "UPDATE "+Database.DATA_TABLE +" SET username=?, email_address=?, password=?, service=?," +
                            " additional_data=? WHERE id=?"
            );

            // Temporarily set the auto commit to false as we want to execute in a batch group
            this.connection.setAutoCommit(false);

            // Iterate over all the data to commit them together
            for (Data data : records){
                query.setString(1, data.getUsername());
                query.setString(2, data.getEmailAddress());
                query.setString(3, data.getPassword());
                query.setString(4, data.getService());
                query.setString(5, data.getAdditionalData());
                query.setInt(6, data.getId());
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
