package moda.passwordManager.backend;

import moda.passwordManager.communicationHandler.CommunicationHandler;
import moda.passwordManager.communicationHandler.Event;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class Backend extends Thread {

    private BackendHelper helper;

    // Objects of the backend classes
    private Cryptography cryptography;
    private Database database;
    private GoogleDrive googleDrive;
    private Settings settings;

    // The CommunicationHandler object used to communicate with the Frontend thread
    private CommunicationHandler communicationHandler;
    private CommunicationHandler exceptionsCommunicationHandler;

    private Event eventToSend;  // The event that is sent to the frontend. Must be set using its setter

    private boolean runFlag;  // Let the thread run until the connection is closed

    public Backend(LinkedBlockingQueue<Event> backendQueue, LinkedBlockingQueue<Event> frontendQueue,
                   LinkedBlockingQueue<Event> backendExceptionQueue, LinkedBlockingQueue<Event> frontendExceptionQueue){

        // Create the communication handler with the two queues
        this.communicationHandler = new CommunicationHandler(backendQueue, frontendQueue);
        this.exceptionsCommunicationHandler = new CommunicationHandler(backendExceptionQueue, frontendExceptionQueue);

        // Initialize all the backend components
        this.settings = new Settings();
        this.cryptography = new Cryptography();
        this.googleDrive = new GoogleDrive(this.settings.getAPPDATA_DIRECTORY_PATH());

        // The path of the database is retrieved from the settings
        this.database = new Database(this.settings.readStringSetting("database/path"));

        this.helper = new BackendHelper(this.cryptography, this.settings, this.googleDrive);

        startGoogleDrive();  // Initialize the connection with Google Drive only if enabled by the user

        this.eventToSend = null;
        this.runFlag = true;
    }

    /**
     * Provide the method used for uncaught exceptions. It must be public otherwise the thread object
     * that is inside the main method, cannot access it
     */
    public void uncaughtException(Thread t, Throwable e) {
        // Send the event to the frontend with the exception communication
        Event event = new Event("exception-raised", e.toString());
        exceptionsCommunicationHandler.send(event);

        // Receive the response from the frontend
        Event frontendResponse = exceptionsCommunicationHandler.receive();

        // Check whether the event response is close-connection to stop the execution
        if (frontendResponse.getNAME().equals("close-connection")){
            event = new Event("close-connection-confirm");  // Create the event to confirm the stop
            exceptionsCommunicationHandler.send(event);  // Send the event
            runFlag = false;  // Set the run flag to false to stop the thread
        }
    }

    @Override
    public void run() {
        while (this.runFlag){
            // Check whether there is an Event to send to the Frontend
            if (this.eventToSend != null){
                this.communicationHandler.send(this.eventToSend);
                resetSendData();  // Reset the data to send to the frontend
            }
            Event event = this.communicationHandler.receive();  // Wait for an event from the Frontend
            createEvent(event);  // Create an event to send to the frontend
            processEvent(event);  // Process the operation requested from the frontend
        }
    }

    /**
     * Reset the data to be sent after it has been sent to the frontend.
     */
    private void resetSendData(){
        this.eventToSend = null;
    }

    private void processEvent(Event event){
        ArrayList<Object> eventData = event.getData();  // Get the data associated with the event
        switch (event.getNAME()){
            case "set-master-password":
                setMasterPassword((String) eventData.getFirst());
                break;

            case "close-connection":
                closeConnection();
                break;

            case "get-service-fields":
                getServiceFields();
                break;

            case "save-data":
                saveData((Data) eventData.getFirst());
                break;

            case "get-data":
                getData((int) eventData.getFirst());
                break;

            case "delete-data":
                deleteSingleData((int) eventData.getFirst());
                break;

            case "change-data":
                changeData((Data) eventData.getFirst());
                break;

            case "generate-string":
                generateString();
                break;

            case "configure-string-generation":
                configureStringGeneration((int) eventData.getFirst(), (boolean) eventData.get(1),
                        (boolean) eventData.get(2), (boolean) eventData.get(3));
                break;

            case "set-database":
                setDatabasePath((String) eventData.getFirst());
                break;

            case "get-database-path":
                getDatabasePath();
                break;

            case "get-string-generation-configuration":
                getStringGenerationConfiguration();
                break;

            case "change-master-password":
                changeMasterPassword((String) eventData.getFirst());
                break;

            case "get-google-drive":
                getGoogleDrive();
                break;

            case "get-google-drive-synchronization":
                getGoogleDriveSynchronization();
                break;

            case "google-drive-authenticate":
                authenticateGoogleDrive((String) eventData.getFirst());
                break;

            case "google-drive-unauthenticate":
                unauthenticateGoogleDrive();
                break;

            case "google-drive-synchronize":
                synchronizeGoogleDrive();
                break;
        }
    }

    /**
     * Create the event that will be sent to the Frontend
     * @param event
     */
    private void createEvent(Event event){
        this.eventToSend = new Event(event.getNAME()+"-completed");
    }

    /**
     * Set the master password of the cryptography object
     * @param masterPassword The master password provided by the user
     */
    private void setMasterPassword(String masterPassword){
        this.cryptography.setMasterPassword(masterPassword.getBytes());
    }

    /**
     * Close the connection with the Frontend and stop the execution of the thread
     */
    private void closeConnection(){
        this.communicationHandler = null;
        this.exceptionsCommunicationHandler = null;
        this.runFlag = false;
    }

    /**
     * Retrieve all the service fields with their IDs from the database, and decrypt them
     */
    private void getServiceFields(){
        ArrayList<Data> serviceFields = this.database.getServiceFields();
        ArrayList<Data> decryptedFields = new ArrayList<>();  // The service fields are decrypted and stored here

        for (Data field : serviceFields){
            decryptedFields.add(this.helper.decryptData(field));  // Decrypt the data in the helper and add it
        }

        this.eventToSend.addData(decryptedFields);
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
     * @param id The ID of the record to read
     */
    private void getData(int id) {
        Data singleData = this.database.getRecord(id);  // Retrive the data associated with the ID

        this.eventToSend.addData(this.helper.decryptData(singleData));  // Decrypt the data with the helper
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
    private void changeData(Data data){
        this.database.changeRecord(this.helper.encryptData(data));  // Encrypt the data with the helper before saving it
    }

    /**
     * Randomically generate a string of a certain length
     */
    private void generateString(){
        // Retrieve the properties from the helper
        ArrayList<Object> configuration = this.helper.getStringGenerationConfiguration();

        char[] stringCharacters = generateStringCharacters((Boolean) configuration.get(1), (Boolean) configuration.get(2),
                (Boolean) configuration.get(3));  // Generate the characters

        this.eventToSend.addData(this.cryptography.generateString((int) configuration.getFirst(), stringCharacters).toString());
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
     * @param length The length of the string
     * @param letters Flag to indicate whether letters are generated
     * @param numbers Flag to indicate whether numbers are generated
     * @param special Flag to indicate whether special characters are generated
     */
    private void configureStringGeneration(int length, boolean letters, boolean numbers, boolean special){
        this.settings.writeSetting("string_generation/length", length);
        this.settings.writeSetting("string_generation/letters", letters);
        this.settings.writeSetting("string_generation/numbers", numbers);
        this.settings.writeSetting("string_generation/special", special);
    }

    /**
     * Set the database to use and save the path into the settings
     * @param databasePath The path of the database chosen
     */
    private void setDatabasePath(String databasePath){
        this.settings.writeSetting("database/path", databasePath);  // Set the path of the database
        this.database.changeDatabase(databasePath);  // Set the new database to be the one used
    }

    /**
     * Retrieve the path of the database current in use
     */
    private void getDatabasePath(){
        this.eventToSend.addData(this.helper.getDatabasePath());  // Add the path to the data to send
    }

    /**
     * Retrieve from the settings file all the parameters of the string generation
     */
    private void getStringGenerationConfiguration(){
        // Retrieve the properties from the helper
        ArrayList<Object> configuration = this.helper.getStringGenerationConfiguration();

        this.eventToSend.addData(configuration.getFirst());
        this.eventToSend.addData(configuration.get(1));
        this.eventToSend.addData(configuration.get(2));
        this.eventToSend.addData(configuration.get(3));
    }

    /**
     * Change the current master password by generating again the encrypted data with the new password
     * @param masterPassword The new master password
     */
    private void changeMasterPassword(String masterPassword){
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
       this.eventToSend.addData(this.helper.isGoogleDriveEnabled());
    }

    /**
     * Retrieve from the settings file whether the automatic synchronization is enabled
     */
    private void getGoogleDriveSynchronization(){
        boolean synchronizationEnabled = this.settings.readBooleanSetting("google_drive/automatic_synchronization");
        this.eventToSend.addData(synchronizationEnabled);
    }

    /**
     * Start the Google Drive communication only if it's enabled in the settings file
     */
    private void startGoogleDrive(){
        if (this.helper.isGoogleDriveEnabled()){
            this.googleDrive.init();
        }
    }

    /**
     * Enable in the settings file the Google Drive synchronization and move the user credentials.json into the local
     * appdata folder, then authenticate the user
     * @param credentialsPath The path of the credentials.json file
     */
    private void authenticateGoogleDrive(String credentialsPath){
        try {
            new File(this.googleDrive.getAPI_DIRECTORY()).mkdirs();  // Create the Google Drive dir (skipped if it already exists)

            // Move the file to the directory
            Files.move(Path.of(credentialsPath), Path.of(this.googleDrive.getAPI_FILE_PATH()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.googleDrive.init();  // Start the Google Drive communication
        this.settings.writeSetting("google_drive/enabled", true);  // Set Google Drive to enabled
    }

    /**
     * Disable in the settings file the Google Drive synchronization and delete the stored credentials, if there's any
     */
    private void unauthenticateGoogleDrive(){
        try {
            FileUtils.deleteDirectory(new File(this.googleDrive.getTOKENS_DIRECTORY_PATH()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.settings.writeSetting("google_drive/enabled", false);
    }

    /**
     * Synchronize the database with Google Drive
     */
    private void synchronizeGoogleDrive(){
        this.googleDrive.sync(this.helper.getDatabasePath(), this.database.getDatabaseName());
    }

}
