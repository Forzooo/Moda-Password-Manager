package moda.passwordManager.backend;

import moda.passwordManager.communicationHandler.CommunicationHandler;
import moda.passwordManager.communicationHandler.Event;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class Backend extends Thread {

    // Objects of the backend classes
    private Cryptography cryptography;
    private Database database;
//    private GoogleDrive googleDrive;  // Disabled until it's fully developed
    private Settings settings;

    // The CommunicationHandler object used to communicate with the Frontend thread
    private CommunicationHandler communicationHandler;

    private Event eventToSend;  // The event that is sent to the frontend. Must be set using its setter
    private ArrayList dataToSend;  // The data that is added to the Event to send to the frontend

    private boolean runFlag;  // Let the thread run until the connection is closed

    public Backend(LinkedBlockingQueue<Event> backendQueue, LinkedBlockingQueue<Event> frontendQueue){
        // Create the communication handler with the two queues
        this.communicationHandler = new CommunicationHandler(backendQueue, frontendQueue);

        // Initialize all the backend components
        this.settings = new Settings();

        this.cryptography = new Cryptography();

        // The path of the database is retrieved from the settings
        this.database = new Database(this.settings.readStringSetting("database/path"));

//        this.googleDrive = new GoogleDrive();  // Disabled until fully developed

        this.eventToSend = null;
        this.dataToSend = new ArrayList();

        this.runFlag = true;
    }

    @Override
    public void run() {
        while (this.runFlag){
            /**
             * If there's an even to send, send it
             * It does not make the thread to stop forever because if an event is read from the Frontend, it's
             * processed and the data will be sent in another event before checking for new events.
             * Lastly, Backend does not send Event on its own so waiting for events it's not a problem.
             */
            if (this.eventToSend != null){
                this.communicationHandler.send(this.eventToSend);
                resetSendData();  // Reset the data to send to the frontend
            }
            Event event = this.communicationHandler.receive();  // Wait for an event from the Frontend
            processEvent(event);  // Process the operation requested from the frontend
            createEvent(event);  // Create an event to send to the frontend
        }
    }

    /**
     * Reset the data to be sent after it has been sent to the frontend.
     */
    public void resetSendData(){
        this.eventToSend = null;
        this.dataToSend.clear();  // Clear the data
    }

    public void processEvent(Event event){
        ArrayList eventData = event.getData();
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
        }
    }

    /**
     * Create the event that will be sent to the Frontend
     * @param event
     */
    public void createEvent(Event event){
        this.eventToSend = new Event(event.getNAME()+"-completed", this.dataToSend);
    }

    /**
     * Decrypts any string that was encrypted and decodes it
     * @param encryptedString
     * @return Decrypted string
     */
    private String decryptData(String encryptedString){
        return new String(this.cryptography.decrypt(Data.decode(encryptedString)));
    }

    /**
     * Encrypts any plaintext string and encodes it to base64
     * @param plaintextData
     * @return
     */
    private String encryptData(String plaintextData){
        return Data.encodeToBase64(this.cryptography.encrypt(plaintextData));
    }

    /**
     * Internally set the master password. <br/>
     * Can be called only from event: "set-master-password"
     * @param masterPassword
     */
    private void setMasterPassword(String masterPassword){
        this.cryptography.setMasterPassword(masterPassword.getBytes());
    }

    /**
     * Close the connection with the Frontend and stop the execution of the thread
     */
    private void closeConnection(){
        this.communicationHandler = null;
        this.runFlag = false;
    }

    /**
     * Retrieve all the service fields with their IDs from the database
     */
    private void getServiceFields(){
        ResultSet resultSet = this.database.getServiceFields();

        ArrayList<Data> data = new ArrayList<>();  // The IDs and service are stored inside a Data object

        while (true){
            try {
                if (!resultSet.next()){
                    resultSet.close();  // Close the ResultSet, and implicitly the query, as it has completed its purpose
                    break;
                }
                int id = resultSet.getInt("id");
                String serviceData = decryptData(resultSet.getString("service"));
                data.add(new Data(id, serviceData));

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        this.dataToSend.add(data);  // Add the data to the data to send
    }

    /**
     * Save the user data, after encrypting it, inside the database
     * @param data
     */
    private void saveData(Data data){
        String username = encryptData(data.getUSERNAME());
        String emailAddress = encryptData(data.getEMAIL_ADDRESS());
        String password = encryptData(data.getPASSWORD());
        String service = encryptData(data.getSERVICE());
        String additionalData = encryptData(data.getADDITIONAL_DATA());

        Data dataEncrypted = new Data(data.getID(), username, emailAddress, password, service, additionalData);
        this.database.addRecord(dataEncrypted);
    }

    /**
     * Retrieve the data with the associated id from the database <br/>
     * Moreover decrypt it
     * @param id
     */
    private void getData(int id){
        Data singleData = this.database.getRecord(id);

        String username = decryptData(singleData.getUSERNAME());
        String emailAddress = decryptData(singleData.getEMAIL_ADDRESS());
        String password = decryptData(singleData.getPASSWORD());
        String service = decryptData(singleData.getSERVICE());
        String additionalData = decryptData(singleData.getADDITIONAL_DATA());

        Data decryptedData = new Data(id, username, emailAddress, password, service, additionalData);
        this.dataToSend.add(decryptedData);
    }

    /**
     * Delete the record of the database with a specific ID
     * @param id
     */
    private void deleteSingleData(int id){
        this.database.deleteRecord(id);
    }

    /**
     * Change the data of a record inside the database
     * @param data
     */
    private void changeData(Data data){
        // Encrypt the data before saving it into the database
        Data encryptedData = new Data(
                data.getID(),
                encryptData(data.getUSERNAME()),
                encryptData(data.getEMAIL_ADDRESS()),
                encryptData(data.getPASSWORD()),
                encryptData(data.getSERVICE()),
                encryptData(data.getADDITIONAL_DATA())
        );
        this.database.changeRecord(encryptedData);
    }

    /**
     * Randomically generate a string of a certain length
     */
    private void generateString(){
        // Read all the properties from the settings file
        int stringLength = this.settings.readIntSetting("string_generation/length");
        boolean letters = this.settings.readBooleanSetting("string_generation/letters");
        boolean numbers = this.settings.readBooleanSetting("string_generation/numbers");
        boolean special = this.settings.readBooleanSetting("string_generation/special");

        char[] stringCharacters = generateStringCharacters(letters, numbers, special);  // Generate the characters

        this.dataToSend.add(this.cryptography.generateString(stringLength, stringCharacters).toString());
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

}
