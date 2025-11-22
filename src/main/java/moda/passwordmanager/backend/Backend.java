package moda.passwordmanager.backend;

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

public class Backend extends Thread {

    private BackendHelper helper;

    // Main components of the backend classes
    private Cryptography cryptography;
    private Database database;
    private GoogleDrive googleDrive;
    private Settings settings;

    /**
     * The servicesMap is used to map IDs with the hashCode of the service field
     */
    private TreeMap<Integer, Integer> servicesMap;

    // The InterThreadCommunication object used to communicate with the Frontend thread
    private InterThreadCommunication itc;

    /**
     * The event received from the ITC. It's used only when an exception is raised, otherwise the local value
     * is preferred and this one is ignored.
     */
    private Event eventReceived;
    private Event eventToSend;  // The event that is sent as a reply to the frontend
    private boolean runFlag;  // Let the backend run until the connection is closed by the frontend

    public Backend(LinkedBlockingQueue<Event> backendQueue, LinkedBlockingQueue<Event> frontendQueue){
        super("Backend");  // Set the name of the thread for debug purposes

        // Create the communication handler with the two queues
        this.itc = new InterThreadCommunication(frontendQueue, backendQueue);

        // Initialize all the backend components
        this.settings = new Settings();
        this.cryptography = new Cryptography();
        this.googleDrive = new GoogleDrive(this.settings.getAPPDATA_DIRECTORY_PATH());

        this.helper = new BackendHelper(this.cryptography, this.settings, this.googleDrive);

        // The path of the database is retrieved from the helper
        this.database = new Database(this.helper.getDatabasePath());

        startGoogleDrive();  // Initialize the connection with Google Drive only if enabled by the user

        this.eventReceived = null;
        this.eventToSend = null;
        this.runFlag = true;
    }

    /**
     * Provide the method used for uncaught exceptions. It must be public otherwise the thread object
     * that is inside the main method, cannot access it
     */
    public void handleException(Thread t, Throwable e) {
        // Create the traceback file that contains the full stack trace of the exception before anything else
        createTracebackFile(t,e);

        // Before sending the exception we need to send back the event because if the request one is an high-priority
        // one, then EDT is waiting for the response before handling the exception
        this.eventToSend.addData(null);  // Add null as the only element of the event
        this.itc.makeResponse(this.eventReceived, this.eventToSend);

        // Send the event to the frontend with the exception communication
        Event event = new Event("exception-raised");
        event.addData(t.getName());
        event.addData(e);

        // Receive the response from the frontend
        Event frontendResponse = this.itc.request(event);

        // Check whether the event response is close-connection to stop the execution
        if (frontendResponse.getNAME().equals("close-connection")){
            event = new Event("close-connection");  // Create the event to confirm the stop
            this.itc.send(event);  // Send the event
            this.runFlag = false;  // Set the run flag to false to stop the thread
        }
    }

    @Override
    public void run() {
        while (this.runFlag){
            Event event = this.itc.receive();  // Wait for an event from the Frontend
            this.eventReceived = event;  // Set the eventReceived for the exceptions handler

            createEvent(event);  // Create an event to send to the frontend
            handleEvent(event);  // Handle the operation requested from the frontend

            // Check whether there is an Event to send to the Frontend
            if (this.eventToSend != null){
                this.itc.makeResponse(event, this.eventToSend);
                resetSendData();  // Reset the data to send to the frontend
            }
        }
    }

    /**
     * Reset the data to be sent after it has been sent to the frontend.
     */
    private void resetSendData(){
        this.eventToSend = null;
    }

    private void handleEvent(Event event){
        ArrayList<Object> eventData = event.getData();  // Get the data associated with the event
        switch (event.getNAME()){
            case "set-master-password":
                setMasterPassword((char[]) eventData.getFirst());

                // We need to store the result of testMasterPassword to initialize the ServiceMapping
                boolean test = testMasterPassword();
                if (test){
                    // Initialize the Service Mapping after the Master Password has been set
                    this.helper.executeInBackground(this::initServiceMapping);
                }
                break;

            case "close-connection":
                closeConnection();
                break;

            case "save-data":
                saveData((Data) eventData.getFirst());
                this.helper.executeInBackground(this::updateServiceFields);
                break;

            case "get-data":
                getData((int) eventData.getFirst());
                break;

            case "delete-data":
                deleteSingleData((int) eventData.getFirst());
                this.helper.executeInBackground(this::updateServiceFields);
                break;

            case "change-data":
                changeData((Data) eventData.getFirst());
                this.helper.executeInBackground(this::updateServiceFields);
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
                // As a new database is set, we need to reset the service fields to update the Frontend with the new
                // data, but updating with initServiceFields happens after the master password has been set,
                // otherwise the services would be shown as encrypted
                this.helper.executeInBackground(this::resetServiceFields);
                break;

            case "get-database":
                getDatabasePath();
                break;

            case "get-string-generation-configuration":
                getStringGenerationConfiguration();
                break;

            case "change-master-password":
                changeMasterPassword((char[]) eventData.getFirst());
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
                this.helper.executeInBackground(this::updateServiceFields);
                break;

            case "enable-google-drive-synchronization":
                enableGoogleDriveSynchronization();
                break;

            case "disable-google-drive-synchronization":
                disableGoogleDriveSynchronization();
                break;

            default:  // If the event is not handled by one of the cases above, then discard the event
                resetSendData();
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
            File traceback = new File(this.settings.getAPPDATA_DIRECTORY_PATH()+"traceback-"+
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
     * Create the event that will be sent to the Frontend
     * @param event
     */
    private void createEvent(Event event){
        this.eventToSend = new Event(event.getNAME());
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
            this.eventToSend.addData(true);
            return true;
        }

        byte[] service = Data.decode(testData.getSERVICE());  // Decode from base64

        // Try to decrypt it and add the data to the event based on whether an exception has been thrown
        try{
            this.cryptography.decrypt(service);
            this.eventToSend.addData(true);
            return true;
        } catch (RuntimeException e){
            this.eventToSend.addData(false);
            return false;
        }
    }

    /**
     * Close the connection with the Frontend and stop the execution of the thread
     */
    private void closeConnection(){
        this.itc = null;
        this.runFlag = false;
    }

    /**
     * Initialize the mapping of the service fields and send the service fields in chunks
     */
    private void initServiceMapping(){
        this.servicesMap = new TreeMap<>();  // Initialize the TreeMap to associate IDs with their hash

        // Initialize an ArrayList that stores the data objects that are sent to the Frontend
        ArrayList<Data> dataToSend = new ArrayList<>();

        // Iterate over the service fields
        for (Data data : this.database.getServiceFields()){
            // Add the ID and the hash of the service to the map
            this.servicesMap.put(data.getID(), data.getSERVICE().hashCode());

            // Create a Data object with the ID and the decrypted service string
            dataToSend.add(new Data(data.getID(), this.helper.decryptString(data.getSERVICE())));
        }

        // Create the Event with the data and send it only if the data is not empty
        if (!dataToSend.isEmpty()){
            Event updateService = new Event("update-service-fields", dataToSend);
            this.itc.send(updateService);
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
        for (Data data : serviceFields){
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
            updatedData.add(new Data(id, ""));
            this.servicesMap.remove(id);  // Remove the ID from the service map as it has been deleted
        }

        // Create the Event with the data and send it only if the data is not empty
        if (!updatedData.isEmpty()){
            Event updateService = new Event("update-service-fields", updatedData);
            this.itc.send(updateService);
        }
    }

    /**
     * Reset the service fields data shown in the Frontend
     */
    private void resetServiceFields(){
        // Ensure that the services map is not empty, otherwise resetting the service fields is useless
        if (!this.servicesMap.isEmpty()){
            Event reset = new Event("reset-service-fields");
            this.itc.send(reset);
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
    private void changeMasterPassword(char[] masterPassword){
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

    /**
     * Synchronization performed automatically every 60 seconds
     */
    private void automaticSynchronizeGoogleDrive(){
        // Ensure that Google Drive is enabled, and the Synchronization is enabled before synchronizing
        if (this.helper.isGoogleDriveEnabled() && this.settings.readBooleanSetting("google_drive/automatic_synchronization")){
            this.googleDrive.sync(this.helper.getDatabasePath(), this.database.getDatabaseName());
            this.helper.executeInBackground(this::updateServiceFields);  // Update the service fields in the Frontend
        }
    }

    /**
     * Enable the Google Drive automatic synchronization
     */
    private void enableGoogleDriveSynchronization(){
        this.settings.writeSetting("google_drive/automatic_synchronization", true);
    }

    /**
     * Disable the Google Drive automatic synchronization
     */
    private void disableGoogleDriveSynchronization(){
        this.settings.writeSetting("google_drive/automatic_synchronization", false);
    }
}
