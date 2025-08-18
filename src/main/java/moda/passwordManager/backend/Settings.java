package moda.passwordManager.backend;

import java.io.File;

/**
 * The settings class manages all the I/O operations made to the user settings of the Password Manager
 */
public class Settings {

    private final String APPDATA_DIRECTORY_PATH;

    public Settings(){
        this.APPDATA_DIRECTORY_PATH = System.getenv("APPDATA")+"\\Moda\\PasswordManager\\";  // Windows only

        initSettings();
    }

    public String getAPPDATA_DIRECTORY_PATH() {
        return APPDATA_DIRECTORY_PATH;
    }


    private void initSettings(){
        createAppdataDirectory();
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
}
