package moda.passwordmanager.backend;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.*;

public class GoogleDrive {

    // The name of the Drive service
    private static final String APPLICATION_NAME = "Moda Password Manager";

    // Define the scopes of the API
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);

    // Define the name of the directory used to store the database inside Drive
    private static final String ROOT_DIRECTORY = ".moda";
    private static final String PASSWORD_MANAGER_DIRECTORY = "password-manager";

    private Drive drive;  // The Google Drive service
    private final JsonFactory JSON_FACTORY;  // Used to handle all the data of the JSON
    private final Helper HELPER;  // The helper is used for the cryptographic operations

    // The IDs of the directories used by the password manager to perform operations on the databases
    private String rootDirectoryID;
    private String passwordManagerDirectoryID;

    public GoogleDrive(Helper helper){
        this.HELPER = helper;
        this.JSON_FACTORY = GsonFactory.getDefaultInstance();

        // The ID of the directories are retrieved when the init method is called
        this.rootDirectoryID = "";
        this.passwordManagerDirectoryID = "";
    }

    /**
     * Initialize the Google Drive modules
     */
    public StoredCredential init(String apiData, String storedCredentials){
        StoredCredential credential = initDriveService(apiData, storedCredentials);

        // Create both directories only if they don't exist
        this.rootDirectoryID = retrieveDirectoryID(ROOT_DIRECTORY);
        if (this.rootDirectoryID.isBlank()){
            createDirectory(ROOT_DIRECTORY, "");
            this.rootDirectoryID = retrieveDirectoryID(ROOT_DIRECTORY);
        }

        this.passwordManagerDirectoryID = retrieveDirectoryID(PASSWORD_MANAGER_DIRECTORY);
        if (this.passwordManagerDirectoryID.isBlank()){
            createDirectory(PASSWORD_MANAGER_DIRECTORY, this.rootDirectoryID);
            this.passwordManagerDirectoryID = retrieveDirectoryID(PASSWORD_MANAGER_DIRECTORY);
        }

        return credential;
    }

    /**
     * Initialize the connection with the Drive API
     */
    private StoredCredential initDriveService(String apiData, String storedCredentials){
        try {
            // Create an HTTP transport object to handle all the HTTP operations
            NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

            // Retrieve the credentials from the authentication
            Object[] authenticationResults = authentication(httpTransport, apiData, storedCredentials);

            // Create the Drive object based on the HTTP transport and the credentials retrieved
            this.drive = new Drive.Builder(httpTransport, this.JSON_FACTORY, (Credential) authenticationResults[0])
                    .setApplicationName(GoogleDrive.APPLICATION_NAME).build();

            return ((DataStore<StoredCredential>) authenticationResults[1]).get("user");  // The update stored credentials are returned
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Authorize the Password Manager using OAuth and return the credentials
     * @return Index 0: Credential object, Index 1: User credentials
     */
    private Object[] authentication(NetHttpTransport httpTransport, String apiData, String storedCredentials){
        try {
            // Create the client secrets from the API key
            GoogleClientSecrets googleClientSecrets = GoogleClientSecrets.load(this.JSON_FACTORY, new StringReader(apiData));

            MemoryDataStoreFactory memoryDataStoreFactory = MemoryDataStoreFactory.getDefaultInstance();

            // If a stored credential has already been created, then we can use it instead of generating another one
            if (storedCredentials != null){
                // When the stored credential has been saved inside the database, it had to be converted to a Java Map
                // to be parsed to JSON. Now we have to do the inverse process to allow the memory data store factory
                // to use the stored credential as there is not right now a simpler way to do it
                Map<String, Object> storedCredentialsMap = this.JSON_FACTORY.fromString(storedCredentials, Map.class);
                StoredCredential credential = new StoredCredential()
                        .setAccessToken((String) storedCredentialsMap.get("accessToken"))
                        .setExpirationTimeMilliseconds(((Number) storedCredentialsMap.get("expirationTimeMilliseconds")).longValue())
                        .setRefreshToken((String) storedCredentialsMap.get("refreshToken"));

                memoryDataStoreFactory.getDataStore("StoredCredential").set("user", credential);
            }

            // Create the Authorization Flow used to exchange the authorization code for a token
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport, this.JSON_FACTORY, googleClientSecrets, SCOPES)
                    // Set the directory where the token will be stored
                    .setDataStoreFactory(memoryDataStoreFactory)
                    .setAccessType("offline")  // Set the Access Type to offline to have a token that lasts longer
                    .build();

            // Create a local server used for the authentication
            LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();

            // As both are required by other methods, the return must be a Object array
            Object[] returnObjects = new Object[2];
            returnObjects[0] = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
            returnObjects[1] = memoryDataStoreFactory.getDataStore("StoredCredential");

            // Return the credentials given by the OAuth
            return returnObjects;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a directory inside Google Drive
     * @param directoryName The name of the directory
     * @param parentDirectoryID Specifies the parent directory of the one that will be created, if it's blank then it
     *                          will be created in the root directory
     */
    private void createDirectory(String directoryName, String parentDirectoryID){
        // The directory is first created as a Google File object to set its metadata
        File directoryMetadata = new File();
        directoryMetadata.setName(directoryName);
        directoryMetadata.setMimeType("application/vnd.google-apps.folder");

        // If the ID is not blank, then set the directory as a subdirectory of another one
        if (!parentDirectoryID.isBlank()){
            directoryMetadata.setParents(Collections.singletonList(parentDirectoryID));
        }

        try {
            this.drive.files().create(directoryMetadata).setFields("id").execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieve the ID of a directory from Google Drive
     * @return The ID if exists, an empty string otherwise
     */
    private String retrieveDirectoryID(String directoryName){
        List<File> directories;

        try {
            // Look only for folders with the same name of the directory we are searching the ID, in the root directory
            FileList result = this.drive.files().list()
                    .setQ("mimeType='application/vnd.google-apps.folder' and name='" + directoryName + "'")
                    .setSpaces("drive")
                    .setFields("files(id)")
                    .execute();

            directories = result.getFiles();  // Get the folders from the result

            // If the folder does not exist return an empty string
            if (directories.isEmpty()){
                return "";
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return directories.getFirst().getId();
    }

    /**
     * Retrieve the ID of a database
     * @param databaseName The name of the database
     * @return ID of the database if it exists, an empty string otherwise
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

        // If the file does not exist return an empty string
        if (files.isEmpty()){
            return "";
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
            String databaseID = getDatabaseID(databaseName);
            if (!databaseID.isBlank()){  // If the database ID is not blank, then a database must exist
                this.drive.files().delete(databaseID).execute();  // TODO: Update the file instead of recreating it
            }

            // Upload the file to Drive and set its id and its parents
            this.drive.files().create(databaseMetadata, fileContent).setFields("id, parents").execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Download the database from Drive and saves it locally
     * @param databaseName The name of the database that will be downloaded
     * @param remoteDatabasePath The path where the remote database will be stored
     */
    private Database downloadDatabase(String databaseName, String remoteDatabasePath){
        OutputStream outputStream;
        try {
            // The database will temporally be stored inside the password manager AppdData directory to be
            // opened by the Database object
            outputStream = new FileOutputStream(remoteDatabasePath);
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

        return new Database(remoteDatabasePath);
    }

    /**
     * Get the MD5 checksum of a database
     * @param databaseName The name of the database
     * @return The checksum as a string, an empty string if the file does not exists
     */
    private String getDatabaseChecksum(String databaseName){
        String databaseID = getDatabaseID(databaseName);  // Get the ID of the database

        // If the database does not exist then return an empty string
        if (databaseID.isBlank()){
            return "";
        }

        try {
            File database = this.drive.files().get(databaseID)
                    .setFields("md5Checksum")
                    .execute();
            return database.getMd5Checksum();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Synchronize the current database and download/upload it based on the last change made
     * @param databasePath The path of the database in use
     * @param databaseName The name of the database in use
     */
    public void sync(String databasePath, String databaseName){
        // If the remote database and the local one have the same checksum it means that no changes have been made,
        // thus we can avoid further operations
        String remoteDatabaseChecksum = getDatabaseChecksum(databaseName);

        if (!remoteDatabaseChecksum.equals(Helper.calculateFileMD5(databasePath)) && !remoteDatabaseChecksum.isBlank()){
            // The name of the remote database is the checksum of the remote database to ensure that there
            // are no duplicate files (hash collision are rare)
            String downloadedDatabasePath = Helper.getPasswordManagerAppDataPath()+remoteDatabaseChecksum+".modb";

            Database remoteDatabase = downloadDatabase(databaseName, downloadedDatabasePath);

            // After all the operations on the remote database are done, we can delete it from the file system
//            java.io.File remoteDatabaseFile = new java.io.File(downloadedDatabasePath);
//            remoteDatabaseFile.delete();

        }
    }
}