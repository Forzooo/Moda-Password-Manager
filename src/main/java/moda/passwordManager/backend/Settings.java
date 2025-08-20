package moda.passwordManager.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;

/**
 * The settings class manages all the I/O operations made to the user settings of the Password Manager
 */
public class Settings {

    private final String APPDATA_DIRECTORY_PATH;
    private File settingsFile;
    private ObjectMapper objectMapper;

    public Settings(){
        this.APPDATA_DIRECTORY_PATH = System.getenv("APPDATA")+"\\Moda\\PasswordManager\\";  // Windows only
        this.objectMapper = new ObjectMapper();  // Create the object mapper used to write/read from the settings file

        initSettings();
    }

    public String getAPPDATA_DIRECTORY_PATH() {
        return APPDATA_DIRECTORY_PATH;
    }


    private void initSettings(){
        createAppdataDirectory();
        createSettingsFile();
    }

    /**
     * Create the Appdata folder for the software to store inside it files
     */
    private void createAppdataDirectory(){
        File appdataDirectory = new File(this.APPDATA_DIRECTORY_PATH);

        // Check whether the directory already exists to avoid recreating it
        if (appdataDirectory.exists()){
            return;
        }

        appdataDirectory.mkdirs();  // Create the directories
    }

    /**
     * Create the settings.json file inside the appdata directory
     */
    private void createSettingsFile(){
        try {
            this.settingsFile = new File(this.APPDATA_DIRECTORY_PATH+"settings.json");

            // If the file exists already the creation is skipped
            if (!this.settingsFile.createNewFile()){
                return;
            }

            ObjectNode rootNode = this.objectMapper.createObjectNode();  // The root of all the JSON nodes

            // Set all the database values
            ObjectNode databaseNode = this.objectMapper.createObjectNode();  // Contains all the database values
            databaseNode.put("path", this.APPDATA_DIRECTORY_PATH+"moda-password-manager.db");

            // Set the string generation configuration
            ObjectNode stringGeneration = this.objectMapper.createObjectNode();
            stringGeneration.put("length", 32);
            stringGeneration.put("letters", true);
            stringGeneration.put("numbers", true);
            stringGeneration.put("special", true);

            // Define the hierarchy of the JSON
            rootNode.put("database", databaseNode);
            rootNode.put("string_generation", stringGeneration);

            // Write the default data inside the settings file
            this.objectMapper.writeValue(this.settingsFile, rootNode);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieve a node from the path
     * @param nodePath The path to get to the node (ex. database/path)
     * @return The node requested
     */
    private JsonNode retrieveNode(String nodePath){
        JsonNode currentNode;

        try {
            String[] nodes = nodePath.split("/");  // Split each node
            JsonNode rootNode = this.objectMapper.readTree(this.settingsFile);  // Read the settings

            // To get to the desired node the current node is updated with each iteration to get to the final one
            currentNode = rootNode.deepCopy();
            for (String node : nodes){
                currentNode = currentNode.get(node);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return currentNode;
    }

    /**
     * Retrieve a node from the path
     * @param rootNode The root node can be provided if it's required to keep the same root variable for changed to properties
     * @param nodePath The path to get to the node (ex. database/path)
     * @return The node requested
     */
    private JsonNode retrieveNode(JsonNode rootNode, String nodePath){

        String[] nodes = nodePath.split("/");  // Split each node

        // To get to the desired node the current node is updated with each iteration to get to the final one
        JsonNode currentNode = rootNode;  // We want to use the same addresses so we cannot deepCopy the node tree
        for (String node : nodes){
            currentNode = currentNode.get(node);
        }

        return currentNode;
    }

    /**
     * Write the settings.json file with the updated tree
     * @param rootNode The entire settings tree
     */
    private void updateSettings(JsonNode rootNode){
        try {
            this.objectMapper.writeValue(this.settingsFile, rootNode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Read a property from the settings file
     * @param nodePath A string where contains the path to the property: each node is divided by a '/' (database/path)
     * @return Value of the property
     */
    public String readStringSetting(String nodePath){
        JsonNode property = retrieveNode(nodePath);  // Retrieve the property

        return property.asText();
    }

    /**
     * Read a property from the settings file
     * @param nodePath A string where contains the path to the property: each node is divided by a '/' (database/path)
     * @return Value of the property
     */
    public int readIntSetting(String nodePath){
        JsonNode property = retrieveNode(nodePath);  // Retrieve the property
        return property.asInt();
    }

    /**
     * Read a property from the settings file
     * @param nodePath A string where contains the path to the property: each node is divided by a '/' (database/path)
     * @return Value of the property
     */
    public boolean readBooleanSetting(String nodePath){
        JsonNode property = retrieveNode(nodePath);  // Retrieve the property
        return property.asBoolean();
    }

    /**
     * Read a property from the settings file
     * @param nodePath A string where contains the path to the property: each node is divided by a '/' (database/path)
     * @param value The new value of the property
     */
    public void writeSetting(String nodePath, Object value){
        try {
            // As we need to change a property we need to keep the same root node, otherwise the changes would not be saved
            JsonNode rootNode = this.objectMapper.readTree(this.settingsFile);

            // To set the new value we first need to get to the node previous to the one we want to change
            // so we have to split the nodePath based on the last '/' provided
            String nodeName = nodePath.substring(nodePath.lastIndexOf("/")+1);
            nodePath = nodePath.substring(0, nodePath.lastIndexOf("/"));
            ObjectNode node = (ObjectNode) retrieveNode(rootNode, nodePath);  // Cast to ObjectNode otherwise it cannot be modified

            // Convert the value to a JsonNode because there is no method to handle Object in ObjectNode
            node.put(nodeName, this.objectMapper.valueToTree(value));

            updateSettings(rootNode);  // Update the file

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
