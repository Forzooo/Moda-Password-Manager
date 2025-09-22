package moda.passwordmanager.backend;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class GoogleDrive {

    // The name of the Drive service
    private static final String APPLICATION_NAME = "Moda Password Manager - Google Drive API";

    // The absolute path of the directory where all the files related to drive are stored
    private final String API_DIRECTORY;

    // Path of the directory where the authentication token is stored
    // Authentication tokens are used to not authenticate each time the software is opened
    private static final String TOKENS_DIRECTORY_NAME = "tokens";

    // Path of the JSON file where there is the API key (inside "/resources")
    private static final String API_FILE_NAME = "credentials.json";

    // Define the scopes of the API
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);

    // Define the name of the directory used to store the database inside Drive
    private static final String ROOT_DIRECTORY = ".moda";
    private static final String PASSWORD_MANAGER_DIRECTORY = "password-manager";

    private final String API_FILE_PATH;  // The absolute path of the credentials.json file
    private final String TOKENS_DIRECTORY_PATH;  // The absolute path of the tokes directory

    private Drive drive;  // The Google Drive service
    private JsonFactory jsonFactory;  // Used to handle all the data of the JSON

    // The IDs are set as attributes to reduce the number of requests made to the API
    private String rootDirectoryID;
    private String passwordManagerDirectoryID;

    public GoogleDrive(String appDataDirectory){
        this.jsonFactory = GsonFactory.getDefaultInstance();

        // The ID of the directories are set in the init method
        this.rootDirectoryID = null;
        this.passwordManagerDirectoryID = null;

        // Set the absolutes paths based on the path of the AppData directory retrieved from the Settings class
        this.API_DIRECTORY = appDataDirectory+"google-drive\\";
        this.API_FILE_PATH = API_DIRECTORY+API_FILE_NAME;
        this.TOKENS_DIRECTORY_PATH = API_DIRECTORY+TOKENS_DIRECTORY_NAME;
    }

    public String getAPI_DIRECTORY() {
        return API_DIRECTORY;
    }

    public String getTOKENS_DIRECTORY_PATH() {
        return TOKENS_DIRECTORY_PATH;
    }

    public String getAPI_FILE_PATH() {
        return this.API_FILE_PATH;
    }

    /**
     * Initialize the Google Drive modules
     */
    public void init(){
        initDriveService();
        this.rootDirectoryID = retrieveRootDirectoryID();
        this.passwordManagerDirectoryID = retrievePasswordManagerDirectoryID();
        createRootDirectory();
        createPasswordManagerDirectory();
    }

    /**
     * Initialize the connection with the Drive API
     */
    private void initDriveService(){
        try {
            // Create an HTTP transport object to handle all the HTTP operations
            NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            Credential credentials = authentication(httpTransport);  // Retrieve the credentials from the authentication

            // Create the Drive object based on the HTTP transport and the credentials retrieved
            this.drive = new Drive.Builder(httpTransport, this.jsonFactory, credentials)
                    .setApplicationName(GoogleDrive.APPLICATION_NAME).build();

        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Authorize the Password Manager using OAuth and return the credentials
     * @return User credentials
     */
    private Credential authentication(NetHttpTransport httpTransport){

        Credential credential; // Define the credential to be returned before the try-catch block

        try {
            // Read the API key from the file
            InputStream inputStream = new FileInputStream(this.API_FILE_PATH);

            // Create the client secrets from the API key
            GoogleClientSecrets googleClientSecrets = GoogleClientSecrets.load(this.jsonFactory, new InputStreamReader(inputStream));

            // Create the Authorization Flow used to exchange the authorization code for a token
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport, this.jsonFactory, googleClientSecrets, SCOPES)
                    // Set the directory where the token will be stored
                    .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(this.TOKENS_DIRECTORY_PATH)))
                    .setAccessType("offline")  // Set the Access Type to offline to have a token that lasts longer
                    .build();

            // Create a local server used for the authentication
            LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
            credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return credential;  // Return the credentials given by the OAuth

    }

    /**
     * Create the ".moda" directory inside the root of Drive
     */
    private void createRootDirectory(){
        // If the directory already exist we can skip the creation of it
        if (this.rootDirectoryID != null){
            return;
        }

        // Define the new directory
        File directoryMetadata = new File();
        directoryMetadata.setName(ROOT_DIRECTORY);
        directoryMetadata.setMimeType("application/vnd.google-apps.folder");

        // Create the directory
        try {
            this.drive.files().create(directoryMetadata).setFields("id").execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.rootDirectoryID = retrieveRootDirectoryID();  // Retrieve the ID of the root directory
    }

    /**
     * Create the "Password-Manager" directory inside the ".moda" directory
     */
    private void createPasswordManagerDirectory(){
        // If the directory already exist we can skip the creation of it
        if (this.passwordManagerDirectoryID != null){
            return;
        }

        // Define the new directory
        File directoryMetadata = new File();
        directoryMetadata.setName(PASSWORD_MANAGER_DIRECTORY);
        directoryMetadata.setMimeType("application/vnd.google-apps.folder");
        directoryMetadata.setParents(Collections.singletonList(this.rootDirectoryID));

        // Create the directory
        try {
            this.drive.files().create(directoryMetadata).setFields("id").execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.passwordManagerDirectoryID = this.retrievePasswordManagerDirectoryID();  // Retrieve the ID of the dir

    }

    /**
     * Retrieve the ID of the root directory (".moda") from Google Drive
     * @return String containing the ID if exists, null otherwise
     */
    private String retrieveRootDirectoryID(){
        List<File> folders;  // Define the list of the folders before the try-catch block
        try {
            // Look only for folders and with the same name of the directory we are searching the ID, in the root directory
            FileList result = this.drive.files().list()
                    .setQ("mimeType='application/vnd.google-apps.folder' and name='" + ROOT_DIRECTORY + "'")
                    .setSpaces("drive")
                    .setFields("files(id, name)")
                    .execute();

            folders = result.getFiles();  // Get the folders from the result

            // If the folder does not exist return null
            if (folders.isEmpty()){
                return null;
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Return the ID of the directory
        return folders.getFirst().getId();
    }

    /**
     * Retrieve the ID of the "Password-Manager" from Google Drive
     * @return String containing the ID if exists, null otherwise
     */
    private String retrievePasswordManagerDirectoryID(){
        List<File> folders;  // Define the list of the folders before the try-catch block
        try {
            // Look only for folders and with the same name of the directory we are searching the ID, in the .moda
            FileList result = this.drive.files().list()
                    .setQ("mimeType='application/vnd.google-apps.folder' and name='" + PASSWORD_MANAGER_DIRECTORY+ "'")
                    .setSpaces("drive")
                    .setFields("files(id, name)")
                    .execute();

            folders = result.getFiles();  // Get the folders from the result

            // If the folder does not exist return null
            if (folders.isEmpty()){
                return null;
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Return the ID of the directory
        return folders.getFirst().getId();
    }

    /**
     * Retrieve the ID of the current database in use
     * @param databaseName The name of the database in use
     * @return ID of the database if it exists, null otherwise
     */
    private String getDatabaseID(String databaseName){
         List<File> files;
        try {
            FileList result = this.drive.files().list()
                    .setQ("name='"+databaseName+"' and '"+this.passwordManagerDirectoryID+"' in parents")
                    .setSpaces("drive")
                    .setFields("files(id)")
                    .execute();

            files = result.getFiles();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // If the file does not exist return null
        if (files.isEmpty()){
            return null;
        }

        return files.getFirst().getId();  // Return the ID of the file

    }

    /**
     * Upload the current database in use to the Drive folder
     * @param databasePath The path of the database in use
     * @param databaseName The name of the database in use
     */
    private void uploadDatabase(String databasePath, String databaseName){
        // Create a Java File object with the path of the database
        java.io.File database = new java.io.File(databasePath);

        // Create the metadata of the database for the Drive upload
        File databaseMetadata = new File();
        databaseMetadata.setName(databaseName);

        // Specify that the database has to be uploaded inside the database directory
        databaseMetadata.setParents(Collections.singletonList(this.passwordManagerDirectoryID));

        // Specify how the file should be sent
        FileContent fileContent = new FileContent("application/octet-stream", database);

        try {
            // Delete the previous database file inside the directory only if the database exists
            String databaseID = getDatabaseID(databaseName);
            if (databaseID != null){
                this.drive.files().delete(getDatabaseID(databaseName)).execute();
            }


            // Upload the file to Drive and set its id and its parents
            this.drive.files().create(databaseMetadata, fileContent).setFields("id, parents").execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Download the database from Drive by its name
     * @param databasePath The path where the database will be saved
     * @param databaseName The name of the database that will be downloaded
     */
    private void downloadDatabase(String databasePath, String databaseName){
        OutputStream outputStream;
        try {
            outputStream = new FileOutputStream(databasePath);  // Define the path where the DB will be saved
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            // Retrieve the database based on its ID and download it to the local path
            this.drive.files().get(getDatabaseID(databaseName)).executeMediaAndDownloadTo(outputStream);

            outputStream.flush();  // Clean the buffer of the download
            outputStream.close();  // Close the file to save the changes
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Return the last change made to the database inside the drive

    /**
     * Get the last change of the database in Google Drive
     * @param databaseName The name of the database
     * @return The last change to the database encoded in long unit
     */
    private long getLastChangeDrive(String databaseName){
        File database;

        String databaseID = getDatabaseID(databaseName);  // Get the ID of the database

        // If the database does not exist then return 0
        if (databaseID == null){
            return 0L;
        }

        try {
            // Retrieve from the drive file the last change made to it
            database = this.drive.files().get(databaseID)
                    .setFields("id, name, modifiedTime")
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return database.getModifiedTime().getValue();  // Return the last change as a long value
    }

    /**
     * Get the latest change made to the local database
     * @param databasePath The path of the database
     * @return The last change to the database encoded in long unit
     */
    private long getLastChangeLocal(String databasePath){
        java.io.File database = new java.io.File(databasePath);
        return database.lastModified();  // Return the last change as a long value
    }

    // Check whether the local database is newer than the drive version and synchronize it based on the result obtained

    /**
     * Synchronize the current database and download/upload it based on the last change made
     * @param databasePath The path of the database in use
     * @param databaseName The name of the database in use
     */
    public void sync(String databasePath, String databaseName){
        // Check if the local database is newer than the drive version
        if (getLastChangeLocal(databasePath) > getLastChangeDrive(databaseName)){
            uploadDatabase(databasePath, databaseName);
        }else{
            downloadDatabase(databasePath, databaseName);  // Download the database because the drive version is newer
        }
    }
}
