package moda.passwordmanager.backend;

import moda.passwordmanager.interthreadcommunication.EventListener;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;
import moda.passwordmanager.interthreadcommunication.Event;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;
import java.util.concurrent.LinkedBlockingQueue;

public class Backend extends EventListener {

    private Helper helper;

    // Main components of the backend classes
    private Cryptography cryptography;
    private Database database;
    private GoogleDrive googleDrive;
    private Settings settings;

    /**
     * The servicesMap is used to map IDs with the hashCode of the service field
     */
    private TreeMap<Integer, Integer> servicesMap;
    private final static int SERVICES_CHUNK = 4;  // The number of services sent per chunk

    // The InterThreadCommunication object used to communicate with the Frontend thread
    private final InterThreadCommunication ITC;

    public Backend(LinkedBlockingQueue<Event> backendQueue, LinkedBlockingQueue<Event> frontendQueue){
        // Set the name of the thread for debug purposes
        super(new InterThreadCommunication(frontendQueue, backendQueue), "Backend");

        // Get the ITC from the EventListener, otherwise we would have to create two separate ITC object
        this.ITC = getITC();

        createAppdataDirectory();  // Create the folder to store the configuration files inside it

        // Initialize all the backend components
        this.settings = new Settings(Helper.getAppDataDirectory()+Helper.getSettingsFile());
        this.cryptography = new Cryptography();
        this.googleDrive = new GoogleDrive(Helper.getAppDataDirectory());

        this.helper = new Helper(this.cryptography, this.settings);

        // The path of the database is retrieved from the helper
        this.database = new Database(this.helper.getDatabasePath());

        startGoogleDrive();  // Initialize the connection with Google Drive only if enabled by the user
        initHandler();  // Initialize all the operations to handle
    }

    /**
     * Create the Appdata folder for the software to store inside it files
     */
    private void createAppdataDirectory(){
        File appdataDirectory = new File(Helper.getAppDataDirectory());

        // Check whether the directory already exists to avoid recreating it
        if (appdataDirectory.exists()){
            return;
        }

        appdataDirectory.mkdirs();  // Create the directories
    }

    /**
     * Add all the operations to handle
     */
    private void initHandler(){
        // Set the master password and initialize the service mapping
        addOperation("set-master-password", () -> {
            setMasterPassword((char[]) getRequestData().getFirst());

            // We need to store the result of testMasterPassword to initialize the ServiceMapping
            boolean test = testMasterPassword();
            if (test){
                // Initialize the Service Mapping after the Master Password has been set
                this.helper.executeInBackground(this::initServiceMapping);
            }
        });

        addOperation("close-connection", this::closeConnection);

        // Save the data and update the service fields
        addOperation("save-data", () -> {
            saveData((Data) getRequestData().getFirst());
            this.helper.executeInBackground(this::updateServiceFields);
        });
        addOperation("get-data", this::getData);

        // Delete a record from the database and update the service fields
        addOperation("delete-data", () -> {
            deleteSingleData((int) getRequestData().getFirst());
            this.helper.executeInBackground(this::updateServiceFields);
        });

        // Change a data and update the service fields
        addOperation("update-data", () -> {
            updateData((Data) getRequestData().getFirst());
            this.helper.executeInBackground(this::updateServiceFields);
        });

        addOperation("generate-string", this::generateString);
        addOperation("configure-string-generation", this::configureStringGeneration);

        // Set a database path and reset the service fields
        addOperation("set-database", () -> {
            String path = (String) getRequestData().getFirst();
            setDatabasePath(path);
            updateRecentDatabases(path);  // Update the recent databases list with this path

            // As a new database is set, we need to reset the service fields to update the Frontend with the new
            // data, but updating with initServiceFields happens after the master password has been set,
            // otherwise the services would be shown as encrypted
            this.helper.executeInBackground(this::resetServiceFields);
        });

        addOperation("get-database", this::getDatabasePath);
        addOperation("get-string-generation-configuration", this::getStringGenerationConfiguration);
        addOperation("update-master-password", this::updateMasterPassword);
        addOperation("get-google-drive", this::getGoogleDrive);
        addOperation("get-google-drive-synchronization", this::getGoogleDriveSynchronization);
        addOperation("google-drive-authenticate", this::authenticateGoogleDrive);
        addOperation("google-drive-deauthenticate", this::deauthenticateGoogleDrive);

        // Synchronize with Google Drive and update the service fields
        addOperation("google-drive-synchronize", () -> {
            synchronizeGoogleDrive();
            this.helper.executeInBackground(this::updateServiceFields);
        });
        addOperation("enable-google-drive-synchronization", this::enableGoogleDriveSynchronization);
        addOperation("disable-google-drive-synchronization", this::disableGoogleDriveSynchronization);
        addOperation("get-recent-databases", this::getRecentDatabases);
    }

    /**
     * Provide the method used for uncaught exceptions. It must be public otherwise the thread object
     * that is inside the main method, cannot access it
     */
    public void handleException(Thread t, Throwable e) {
        // Create the traceback file that contains the full stack trace of the exception before anything else
        createTracebackFile(t,e);
        this.database.closeConnection();  // Close the connection with the database

        // Before sending the exception we need to send back the event because if the request one is a synchronous
        // one, then EDT is waiting for the response before handling the exception
        handleExceptionRaised();

        // Send the event to the frontend with the exception communication
        Event event = new Event("exception-raised");
        event.addData(t.getName());
        event.addData(e);

        // Receive the response from the frontend
        Event frontendResponse = this.ITC.request(event);

        // Check whether the event response is close-connection to stop the execution
        if (frontendResponse.getOperation().equals("close-connection")){
            event = new Event("close-connection");  // Create the event to confirm the stop
            this.ITC.send(event);  // Send the event
        }
    }

    /**
     * Create a traceback file containing the full stack trace exception
     */
    private void createTracebackFile(Thread thread, Throwable throwable){
        // StringWriter and PrintWriter are used to get the stack trace of the exception into the string format
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);

        // The timestamp is used for the filename, and needs a proper formatter as otherwise would use ":" which
        // cannot be used in filenames
        String timestamp = new SimpleDateFormat("yyyy-M-dd-HH-mm-ss").format(new Date());

        try {
            File traceback = new File(Helper.getAppDataDirectory()+"traceback-"+
                    timestamp+".txt");
            traceback.createNewFile();  // Create the traceback file

            // Write the stack inside the traceback file
            FileWriter fileWriter = new FileWriter(traceback);
            fileWriter.write("The following exception occurred in the " + thread.getName() + " thread\r\n"+
                    stringWriter);
            fileWriter.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Set the master password of the cryptography object
     * @param masterPassword The master password provided by the user
     */
    private void setMasterPassword(char[] masterPassword){
        this.cryptography.setMasterPassword(new String(masterPassword).getBytes());
    }

    /**
     * Test the master password the user has entered at the login to know whether is wrong
     * @return Returns a boolean value to locally indicate whether the master password is right
     */
    private boolean testMasterPassword(){
        Data testData = this.database.getFirstServiceField();  // Get the first service to try to decrypt it

        // If the service is null, it means there isn't data in it yet, thus the master password is always correct
        if (testData.getSERVICE() == null){
            addResponseData(true);
            return true;
        }

        byte[] service = Helper.decodeBase64(testData.getSERVICE());  // Decode from base64

        // Try to decrypt it and add the data to the event based on whether an exception has been thrown
        try{
            this.cryptography.decrypt(service);
            addResponseData(true);
            return true;
        } catch (RuntimeException e){
            addResponseData(false);
            return false;
        }
    }

    /**
     * Initialize the mapping of the service fields and send them in chunks
     */
    private void initServiceMapping(){
        this.servicesMap = new TreeMap<>();  // Initialize the TreeMap to associate IDs with their hashcode

        // Initialize an ArrayList that stores the data objects that are sent to the Frontend
        ArrayList<Data> dataToSend = new ArrayList<>();
        ArrayList<Data> serviceFields = this.database.getServiceFields();

        for (int i = 0; i < serviceFields.size(); i++){
            // Check the current size of the data to send to know if a chunk size is reached to send it
            if (dataToSend.size() >= SERVICES_CHUNK){
                Event updateService = new Event("update-service-fields", dataToSend);
                this.ITC.send(updateService);

                // We need to recreate the dataToSend object as otherwise it would use the same address as the one sent
                // to the FrontendEventListener which would raise a concurrent exception
                dataToSend = new ArrayList<>();
            }

            Data data = serviceFields.get(i);

            // Add the ID and the hash of the service to the map
            this.servicesMap.put(data.getID(), data.getSERVICE().hashCode());

            // Create a Data object with the ID and the decrypted service string
            dataToSend.add(new Data(data.getID(), this.helper.decryptString(data.getSERVICE())));
        }

        // Create the Event with the data and send it only if the data is not empty
        if (!dataToSend.isEmpty()){
            Event updateService = new Event("update-service-fields", dataToSend);
            this.ITC.send(updateService);
        }
    }

    /**
     * Retrieve all the service fields with their IDs from the database, compare their hashes and decrypt the updated
     * ones to send them to the frontend
     */
    private void updateServiceFields(){
        ArrayList<Data> serviceFields = this.database.getServiceFields();  // Read the service fields from the DB
        ArrayList<Data> updatedData = new ArrayList<>();  // The data to send to the frontend is stored here

        // It's required to initialize an ArrayList over the KeySet because otherwise we would have that updating
        // the keyset, with the removal of IDs, would also update the servicesMap
        // The ID are stored to remove all the ones that are inside the database, thus if the ArrayList is not empty,
        // that means at least one ID has been deleted from the DB
        ArrayList<Integer> servicesMapID = new ArrayList<>(this.servicesMap.keySet());

        // Iterate over the Data of the Database
        for (int i = 0; i < serviceFields.size(); i++){
            // Check the current size of the data to send to know if a chunk size is reached to send it
            if (updatedData.size() >= SERVICES_CHUNK){
                Event updateService = new Event("update-service-fields", updatedData);
                this.ITC.send(updateService);

                // We need to recreate the dataToSend object as otherwise it would use the same address as the one sent
                // to the FrontendEventListener which would raise a concurrent exception
                updatedData = new ArrayList<>();
            }

            Data data = serviceFields.get(i);

            int id = data.getID();
            int hash = data.getSERVICE().hashCode();

            // Check if the ID already exists or is a new one
            if (this.servicesMap.containsKey(id)){
                // If the hashes of the service are different, then it means the service field has been updated
                if (this.servicesMap.get(id) != hash){
                    updatedData.add(new Data(id, this.helper.decryptString(data.getSERVICE())));
                    this.servicesMap.replace(id, hash);
                }
                servicesMapID.remove((Integer) id);  // Remove the ID from the list because it has been found
            }else{  // Because the ID is new we can add it to the servicesMap and add it to the data to send
                this.servicesMap.put(id, hash);
                updatedData.add(new Data(id, this.helper.decryptString(data.getSERVICE())));
            }
        }

        // Iterate over the IDs that haven't been deleted, and set their service field to be empty to remove them
        // from the frontend
        for (Integer id : servicesMapID){
            // Check the current size of the data to send to know if a chunk size is reached to send it
            if (updatedData.size() >= SERVICES_CHUNK){
                Event updateService = new Event("update-service-fields", updatedData);
                this.ITC.send(updateService);

                // We need to recreate the dataToSend object as otherwise it would use the same address as the one sent
                // to the FrontendEventListener which would raise a concurrent exception
                updatedData = new ArrayList<>();
            }

            updatedData.add(new Data(id, ""));
            this.servicesMap.remove(id);  // Remove the ID from the service map as it has been deleted
        }

        // Create the Event with the data and send it only if the data is not empty
        if (!updatedData.isEmpty()){
            Event updateService = new Event("update-service-fields", updatedData);
            this.ITC.send(updateService);
        }
    }

    /**
     * Reset the service fields data shown in the Frontend
     */
    private void resetServiceFields(){
        // Ensure that the services map is not empty, otherwise resetting the service fields is useless
        if (!this.servicesMap.isEmpty()){
            Event reset = new Event("reset-service-fields");
            this.ITC.send(reset);
        }
    }

    /**
     * Save the user data, after encrypting it, inside the database
     * @param data The data to save inside the database
     */
    private void saveData(Data data){
        this.database.addRecord(this.helper.encryptData(data));  // Encrypt the data with the helper, then add it
    }

    /**
     * Retrieve the data with the associated id from the database and decrypt it
     */
    private void getData() {
        int id = (int) getRequestData().getFirst();  // The ID of the record to read
        Data singleData = this.database.getRecord(id);  // Retrive the data associated with the ID

        addResponseData(this.helper.decryptData(singleData));  // Decrypt the data with the helper
    }

    /**
     * Delete the record of the database with a specific ID
     * @param id The ID of the record to delete
     */
    private void deleteSingleData(int id){
        this.database.deleteRecord(id);
    }

    /**
     * Change the data of a record inside the database
     * @param data The updated data to save
     */
    private void updateData(Data data){
        this.database.updateRecord(this.helper.encryptData(data));  // Encrypt the data with the helper before saving it
    }

    /**
     * Randomically generate a string of a certain length
     */
    private void generateString(){
        // Retrieve the properties from the helper
        ArrayList<Object> configuration = this.helper.getStringGenerationConfiguration();

        char[] stringCharacters = generateStringCharacters((Boolean) configuration.get(1), (Boolean) configuration.get(2),
                (Boolean) configuration.get(3));  // Generate the characters

        addResponseData(Helper.generateRandomString((int) configuration.getFirst(), stringCharacters).toString());
    }

    /**
     * Generate the string characters used for the string generation
     * @param letters Flag to indicate whether letters are generated
     * @param numbers Flag to indicate whether numbers are generated
     * @param special Flag to indicate whether special characters are generated
     * @return A char array that contains all the characters chosen for the string generation
     */
    private char[] generateStringCharacters(boolean letters, boolean numbers, boolean special){
        // Initialize the arrays with the different options of the characters
        char[] lettersArray = {
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
                'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
        };

        char[] numbersArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

        char[] specialArray = {
                '!', '?', '.', ',', '#', '$', '%', '&', '\'', '"', '(', ')', '+',
                '-', '*', ':', ';', '@', '^', '_', '[', ']', '{', '}', '<', '>'
        };

        // Define the set as an ArrayList as it's easier to handle
        ArrayList<Character> stringCharactersArrayList = new ArrayList<>();

        if (letters){
            for (char letter : lettersArray){
                stringCharactersArrayList.add(letter);
            }
        }

        if (numbers){
            for (char number : numbersArray){
                stringCharactersArrayList.add(number);
            }
        }

        if (special){
            for (char specialCharacter : specialArray){
                stringCharactersArrayList.add(specialCharacter);
            }
        }

        // Convert the ArrayList to a char array for compatibility with string generation
        char[] stringCharacters = new char[stringCharactersArrayList.size()];

        for (int i = 0; i < stringCharacters.length; i++){
            stringCharacters[i] = stringCharactersArrayList.get(i);
        }

        return stringCharacters;
    }

    /**
     * Set in the settings file the user preferences for the generation of strings
     */
    private void configureStringGeneration(){
        ArrayList<Object> requestData = getRequestData();

        int length = (int) requestData.getFirst();  // The length of the string
        boolean letters = (boolean) requestData.get(1);  // Flag to indicate whether letters are generated
        boolean numbers = (boolean) requestData.get(2);  // Flag to indicate whether numbers are generated
        boolean special = (boolean) requestData.get(3);  // Flag to indicate whether special characters are generated

        this.settings.writeProperty("string_generation/length", length);
        this.settings.writeProperty("string_generation/letters", letters);
        this.settings.writeProperty("string_generation/numbers", numbers);
        this.settings.writeProperty("string_generation/special", special);
    }

    /**
     * Set the database to use and save the path into the settings
     * @param databasePath The path of the database chosen
     */
    private void setDatabasePath(String databasePath){
        this.settings.writeProperty("database/path", databasePath);  // Set the path of the database
        this.database.changeDatabase(databasePath);  // Set the new database to be the one used
    }

    /**
     * Retrieve the path of the database current in use
     */
    private void getDatabasePath(){
        addResponseData(this.helper.getDatabasePath());  // Add the path to the data to send
    }

    /**
     * Retrieve from the settings file all the parameters of the string generation
     */
    private void getStringGenerationConfiguration(){
        // Retrieve the properties from the helper
        ArrayList<Object> configuration = this.helper.getStringGenerationConfiguration();

        addResponseData(configuration.getFirst());
        addResponseData(configuration.get(1));
        addResponseData(configuration.get(2));
        addResponseData(configuration.get(3));
    }

    /**
     * Change the current master password in use for the database, and encrypt the data with the new password
     */
    private void updateMasterPassword(){
        char[] masterPassword = (char[]) getRequestData().getFirst();  // The new master password

        ArrayList<Data> oldData = this.database.getRecords();  // Get all the data from the database
        ArrayList<Data> newData = new ArrayList<>();  // The data re-encrypted with the new master password

        // Decrypt all the data and add it to newData
        for (Data data : oldData){
            newData.add(this.helper.decryptData(data));
        }

        setMasterPassword(masterPassword);  // Set the new master password before re-encrypting the data

        // Iterate over the decrypted data while removing it, and re-adding them as the last element per cycle
        for (int i = 0; i < newData.size(); i++){
            Data data = newData.getFirst();  // Always get the first element
            newData.removeFirst();  // Remove it from the ArrayList
            newData.addLast(this.helper.encryptData(data));  // Re-encrypt the data and add it as the last element
        }

        this.database.changeRecords(newData);  // Change all the records of the database with the new ones
    }

    /**
     * Retrieve from the settings file whether Google Drive is enabled
     */
    private void getGoogleDrive(){
        // Retrieve from the helper whether Google Drive is enabled
       addResponseData(this.helper.isGoogleDriveEnabled());
    }

    /**
     * Retrieve from the settings file whether the automatic synchronization is enabled
     */
    private void getGoogleDriveSynchronization(){
        boolean synchronizationEnabled = this.settings.readBooleanProperty("google_drive/automatic_synchronization");
        addResponseData(synchronizationEnabled);
    }

    /**
     * Start the Google Drive communication and the automatic synchronization only if they're enabled in the settings
     * file
     */
    private void startGoogleDrive(){
        if (this.helper.isGoogleDriveEnabled()){
            this.googleDrive.init();
        }

        // We need to schedule the synchronization even if the Google Drive module is not enabled because otherwise
        // it can happen that the synchronization is scheduled more than one time
        this.helper.executePeriodicallyInBackground(this::automaticSynchronizeGoogleDrive, 60);
    }

    /**
     * Enable in the settings file the Google Drive synchronization and move the user credentials.json into the local
     * appdata folder, then authenticate the user
     */
    private void authenticateGoogleDrive(){
        String credentialsPath = (String) getRequestData().getFirst();  // The path of the credentials.json file
        try {
            new File(this.googleDrive.getAPI_DIRECTORY()).mkdirs();  // Create the Google Drive dir (skipped if it already exists)

            // Move the file to the directory
            Files.move(Path.of(credentialsPath), Path.of(this.googleDrive.getAPI_FILE_PATH()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.googleDrive.init();  // Start the Google Drive communication
        this.settings.writeProperty("google_drive/enabled", true);  // Set Google Drive to enabled
    }

    /**
     * Disable in the settings file the Google Drive synchronization and delete the stored credentials, if there's any
     */
    private void deauthenticateGoogleDrive(){
        try {
            FileUtils.deleteDirectory(new File(this.googleDrive.getTOKENS_DIRECTORY_PATH()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.settings.writeProperty("google_drive/enabled", false);
    }

    /**
     * Synchronize the database with Google Drive
     */
    private void synchronizeGoogleDrive(){
        this.googleDrive.sync(this.helper.getDatabasePath(), this.database.getDatabaseName());
    }

    /**
     * Synchronization performed automatically every 60 seconds
     */
    private void automaticSynchronizeGoogleDrive(){
        // Ensure that Google Drive is enabled, and the Synchronization is enabled before synchronizing
        if (this.helper.isGoogleDriveEnabled() && this.settings.readBooleanProperty("google_drive/automatic_synchronization")){
            this.googleDrive.sync(this.helper.getDatabasePath(), this.database.getDatabaseName());
            this.helper.executeInBackground(this::updateServiceFields);  // Update the service fields in the Frontend
        }
    }

    /**
     * Enable the Google Drive automatic synchronization
     */
    private void enableGoogleDriveSynchronization(){
        this.settings.writeProperty("google_drive/automatic_synchronization", true);
    }

    /**
     * Disable the Google Drive automatic synchronization
     */
    private void disableGoogleDriveSynchronization(){
        this.settings.writeProperty("google_drive/automatic_synchronization", false);
    }

    /**
     * Get the last databases used from the settings file
     */
    private void getRecentDatabases(){
        ArrayList<String> recentDatabases = this.settings.readListProperty("database/recent");
        addResponseData(recentDatabases);
    }

    /**
     * Add to the recent databases the one with the path provided, otherwise if it already exists set it to be first
     */
    private void updateRecentDatabases(String path){
        ArrayList<String> recentDatabases = this.settings.readListProperty("database/recent");
        int pathIndex = recentDatabases.indexOf(path);  // Get the index of the path from the list

        // If the index of the path is not -1, it means that the path has already been added to the list
        if (pathIndex != -1){
            recentDatabases.remove(pathIndex);
        }
        recentDatabases.addFirst(path);  // Add the path as the first element of the list
        this.settings.writeListProperty("database/recent", recentDatabases);  // Write the updated list in the settings
    }

}
