package moda.passwordmanager.backend.settings;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import moda.passwordmanager.backend.Helper;
import moda.passwordmanager.frontend.properties.Themes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The settings class manages all the I/O operations made to the user settings of the Password Manager
 */
public class Settings extends AbstractSettings {

    /**
     * The settings version is used to know whether the file needs to be updated with the new data. Also, it can occur
     * that the version does not match the application's one as the setting's version updates only when they are
     * modified.
     */
    private final static String SETTINGS_VERSION = "0.6.0";
    private final ObjectMapper OBJECT_MAPPER;
    private final File SETTINGS_FILE;

    public Settings(String settingsPath){
        this.OBJECT_MAPPER = new ObjectMapper();  // Create the object mapper used to write/read from the settings file
        this.SETTINGS_FILE = new File(settingsPath);

        try {
            // If the file does not exist yet, create it
            if (this.SETTINGS_FILE.createNewFile()) {
                updateSettingsFile(getDefaultSettings());  // Update the file with the default settings
            }
            // TODO. The version check has to be used after 0.6.0 because every user needs to already have the
            // TODO. version in the settings
            // }else if (!readStringProperty("version").equals(SETTINGS_VERSION)){  // The settings must be updated to
                                                                                 // follow the latest changes made
            updateSettingsFile(updateFromOldVersion());
            // }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieve the current default settings for the application
     */
    private ObjectNode getDefaultSettings(){
        ObjectNode rootNode = this.OBJECT_MAPPER.createObjectNode();  // The root of all the JSON nodes

        // Set all the database values
        ArrayList<String> recentDatabases = new ArrayList<>();
        recentDatabases.add(Helper.getPasswordManagerAppDataPath()+Helper.getDefaultDatabase());  // The default database used

        ObjectNode database = this.OBJECT_MAPPER.createObjectNode();  // Contains all the database values
        database.put("selected", recentDatabases.getFirst());
        database.putPOJO("recent", recentDatabases);  // The last 5 database used

        // Set the string generation configuration
        ObjectNode stringGeneration = this.OBJECT_MAPPER.createObjectNode();
        stringGeneration.put("length", 32);
        stringGeneration.put("letters", true);
        stringGeneration.put("numbers", true);
        stringGeneration.put("special", true);

        // Set the appearance properties
        ObjectNode appearance = this.OBJECT_MAPPER.createObjectNode();
        appearance.put("language", Helper.getSystemLanguage());
        appearance.put("theme", Themes.Light.toString());

        // Define the hierarchy of the JSON
        rootNode.put("version", SETTINGS_VERSION);
        rootNode.put("database", database);
        rootNode.put("string_generation", stringGeneration);
        rootNode.put("appearance", appearance);

        return rootNode;
    }

    /**
     * Reads the settings file and returns it with the changes made to match the latest version
     */
    private JsonNode updateFromOldVersion(){
        // We convert both the JSON to Map object to handle the data operations easily
        Map<String, Object> defaultSettings = this.OBJECT_MAPPER.convertValue(getDefaultSettings(), new TypeReference<>(){});
        try {
            Map<String, Object> oldSettings = this.OBJECT_MAPPER.readValue(this.SETTINGS_FILE, new TypeReference<>(){});

            // We iterate over the old settings file and we remove all the old properties that have been removed in
            // newer version
            for (String property : oldSettings.keySet()){
                // If the newer settings file does not contain the property we remove them
                if (!defaultSettings.containsKey(property)){
                    oldSettings.remove(property);
                }else{
                    // Check whether the properties contain a subproperty to check if they have to be removed
                    if (oldSettings.get(property) instanceof LinkedHashMap<?,?>){
                        LinkedHashMap<String, Object> defaultSetting = (LinkedHashMap<String, Object>) defaultSettings.get(property);
                        LinkedHashMap<String, Object> oldSetting = (LinkedHashMap<String, Object>) oldSettings.get(property);

                        // The subproperties that have to be removed they first need to be saved in an array to avoid
                        // a Concurrent Modification Exception
                        ArrayList<String> oldSubproperties = new ArrayList<>();

                        // Iterate over the subproperties to remove the ones that are not inside the newer one
                        for (String subproperty : oldSetting.keySet()){
                            if (!defaultSetting.containsKey(subproperty)){
                                oldSubproperties.add(subproperty);
                            }
                        }

                        for (String subproperty : oldSubproperties){
                            oldSetting.remove(subproperty);
                        }
                    }
                }
            }

            // We iterate over the new settings and we add the to the old one all the properties that aren't in it yet
            for (String property : defaultSettings.keySet()){
                // If the old file does not contain a property we add it
                if (!oldSettings.containsKey(property)){
                    oldSettings.put(property, defaultSettings.get(property));
                }else{
                    // Check whether the properties contain a subproperty to check if they have to be added
                    if (defaultSettings.get(property) instanceof LinkedHashMap<?,?>){
                        LinkedHashMap<String, Object> defaultSetting = (LinkedHashMap<String, Object>) defaultSettings.get(property);
                        LinkedHashMap<String, Object> oldSetting = (LinkedHashMap<String, Object>) oldSettings.get(property);

                        // Iterate over the subproperties to add the ones that are not inside the old settings yet
                        for (String subproperty : defaultSetting.keySet()){
                            if (!oldSetting.containsKey(subproperty)){
                                oldSetting.put(subproperty, defaultSetting.get(subproperty));
                            }
                        }
                    }
                }
            }

            return this.OBJECT_MAPPER.valueToTree(oldSettings);  // Convert the map back to a JsonNode
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieve a node from the path
     * @param nodePath The path to get to the node (ex. database/selected)
     * @return The node requested
     */
    private JsonNode retrieveNode(String nodePath){
        JsonNode currentNode;

        try {
            String[] nodes = nodePath.split("/");  // Split each node
            JsonNode rootNode = this.OBJECT_MAPPER.readTree(this.SETTINGS_FILE);  // Read the settings

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
     * @param nodePath The path to get to the node (ex. database/selected)
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
            this.OBJECT_MAPPER.writeValue(this.SETTINGS_FILE, rootNode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Read a property from the settings file
     * @param nodePath A string where contains the path to the property: each node is divided by a '/' (database/selected)
     * @return Value of the property
     */
    @Override
    public String readStringProperty(String nodePath){
        JsonNode property = retrieveNode(nodePath);  // Retrieve the property

        return property.asText();
    }

    /**
     * Read a property from the settings file
     * @param nodePath A string where contains the path to the property: each node is divided by a '/' (database/selected)
     * @return Value of the property
     */
    @Override
    public int readIntProperty(String nodePath){
        JsonNode property = retrieveNode(nodePath);  // Retrieve the property
        return property.asInt();
    }

    /**
     * Read a property from the settings file
     * @param nodePath A string where contains the path to the property: each node is divided by a '/' (database/selected)
     * @return Value of the property
     */
    @Override
    public boolean readBooleanProperty(String nodePath){
        JsonNode property = retrieveNode(nodePath);  // Retrieve the property
        return property.asBoolean();
    }

    /**
     * Read a list property from the settings file
     * @param nodePath A string where contains the path to the property: each node is divided by a '/' (database/selected)
     * @return ArrayList of the property
     */
    @Override
    public ArrayList readListProperty(String nodePath){
        JsonNode property = retrieveNode(nodePath);
        return this.OBJECT_MAPPER.convertValue(property, new TypeReference<>(){});
    }

    /**
     * Write a property to the settings file
     * @param nodePath A string where contains the path to the property: each node is divided by a '/' (database/selected)
     * @param value The new value of the property
     */
    @Override
    public void writeProperty(String nodePath, Object value){
        try {
            // As we need to change a property we need to keep the same root node, otherwise the changes would not be saved
            JsonNode rootNode = this.OBJECT_MAPPER.readTree(this.SETTINGS_FILE);

            // To set the new value we first need to get to the node previous to the one we want to change
            // so we have to split the nodePath based on the last '/' provided
            String nodeName = nodePath.substring(nodePath.lastIndexOf("/")+1);
            nodePath = nodePath.substring(0, nodePath.lastIndexOf("/"));
            ObjectNode node = (ObjectNode) retrieveNode(rootNode, nodePath);  // Cast to ObjectNode otherwise it cannot be modified

            // Convert the value to a JsonNode because there is no method to handle Object in ObjectNode
            node.put(nodeName, this.OBJECT_MAPPER.valueToTree(value));

            updateSettingsFile(rootNode);  // Update the file

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeListProperty(String nodePath, ArrayList values){
        try {
            // As we need to change a property we need to keep the same root node, otherwise the changes would not be saved
            JsonNode rootNode = this.OBJECT_MAPPER.readTree(this.SETTINGS_FILE);

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
