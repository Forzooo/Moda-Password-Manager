package moda.passwordmanager.backend;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The settings class manages all the I/O operations made to the user settings of the Password Manager
 */
public class Settings {

    private final File SETTINGS_FILE;
    private ObjectMapper objectMapper;

    public Settings(String settingsPath){
        this.objectMapper = new ObjectMapper();  // Create the object mapper used to write/read from the settings file

        this.SETTINGS_FILE = new File(settingsPath);

        // If the file does not exist yet, create it and add the default settings
        try {
            if (this.SETTINGS_FILE.createNewFile()){
                addDefaultSettings();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create the settings.json file inside the appdata directory
     */
    private void addDefaultSettings(){
        try {
            ObjectNode rootNode = this.objectMapper.createObjectNode();  // The root of all the JSON nodes

            // Set all the database values
            ArrayList<String> recentDatabases = new ArrayList<>();
            recentDatabases.add(Helper.getAppDataDirectory()+Helper.getDefaultDatabase());  // The default database used

            ObjectNode database = this.objectMapper.createObjectNode();  // Contains all the database values
            database.put("path", recentDatabases.getFirst());
            database.putPOJO("recent", recentDatabases);  // The last 5 database used

            // Set the string generation configuration
            ObjectNode stringGeneration = this.objectMapper.createObjectNode();
            stringGeneration.put("length", 32);
            stringGeneration.put("letters", true);
            stringGeneration.put("numbers", true);
            stringGeneration.put("special", true);

            // Set the Google Drive properties
            ObjectNode googleDrive = this.objectMapper.createObjectNode();
            googleDrive.put("enabled", false);
            googleDrive.put("automatic_synchronization", false);

            // Define the hierarchy of the JSON
            rootNode.put("database", database);
            rootNode.put("string_generation", stringGeneration);
            rootNode.put("google_drive", googleDrive);

            // Write the default data inside the settings file
            this.objectMapper.writeValue(this.SETTINGS_FILE, rootNode);

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
            JsonNode rootNode = this.objectMapper.readTree(this.SETTINGS_FILE);  // Read the settings

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
     * @param rootNode The root node can be provided if it's required to keep the same root variable for changed to
     *                 properties
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
    private void updateSettingsFile(JsonNode rootNode){
        try {
            this.objectMapper.writeValue(this.SETTINGS_FILE, rootNode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Read a property from the settings file
     * @param nodePath A string where contains the path to the property: each node is divided by a '/' (database/path)
     * @return Value of the property
     */
    public String readStringProperty(String nodePath){
        JsonNode property = retrieveNode(nodePath);  // Retrieve the property

        return property.asText();
    }

    /**
     * Read a property from the settings file
     * @param nodePath A string where contains the path to the property: each node is divided by a '/' (database/path)
     * @return Value of the property
     */
    public int readIntProperty(String nodePath){
        JsonNode property = retrieveNode(nodePath);  // Retrieve the property
        return property.asInt();
    }

    /**
     * Read a property from the settings file
     * @param nodePath A string where contains the path to the property: each node is divided by a '/' (database/path)
     * @return Value of the property
     */
    public boolean readBooleanProperty(String nodePath){
        JsonNode property = retrieveNode(nodePath);  // Retrieve the property
        return property.asBoolean();
    }

    /**
     * Read a list property from the settings file
     * @param nodePath A string where contains the path to the property: each node is divided by a '/' (database/path)
     * @return ArrayList of the property
     */
    public ArrayList readListProperty(String nodePath){
        JsonNode property = retrieveNode(nodePath);
        return this.objectMapper.convertValue(property, new TypeReference<>(){});
    }

    /**
     * Write a property from the settings file
     * @param nodePath A string where contains the path to the property: each node is divided by a '/' (database/path)
     * @param value The new value of the property
     */
    public void writeProperty(String nodePath, Object value){
        try {
            // As we need to change a property we need to keep the same root node, otherwise the changes would not be saved
            JsonNode rootNode = this.objectMapper.readTree(this.SETTINGS_FILE);

            // To set the new value we first need to get to the node previous to the one we want to change
            // so we have to split the nodePath based on the last '/' provided
            String nodeName = nodePath.substring(nodePath.lastIndexOf("/")+1);
            nodePath = nodePath.substring(0, nodePath.lastIndexOf("/"));
            ObjectNode node = (ObjectNode) retrieveNode(rootNode, nodePath);  // Cast to ObjectNode otherwise it cannot be modified

            // Convert the value to a JsonNode because there is no method to handle Object in ObjectNode
            node.put(nodeName, this.objectMapper.valueToTree(value));

            updateSettingsFile(rootNode);  // Update the file

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeListProperty(String nodePath, List values){
        try {
            // As we need to change a property we need to keep the same root node, otherwise the changes would not be saved
            JsonNode rootNode = this.objectMapper.readTree(this.SETTINGS_FILE);

            // To set the new value we first need to get to the node previous to the one we want to change
            // so we have to split the nodePath based on the last '/' provided
            String nodeName = nodePath.substring(nodePath.lastIndexOf("/")+1);
            nodePath = nodePath.substring(0, nodePath.lastIndexOf("/"));
            ObjectNode node = (ObjectNode) retrieveNode(rootNode, nodePath);  // Cast to ObjectNode otherwise it cannot be modified

            node.putPOJO(nodeName, values);  // Set the list to the node name

            updateSettingsFile(rootNode);  // Update the file
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
