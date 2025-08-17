package moda.passwordManager.backend;

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
    private static final String APPLICATION_NAME = "Moda Password Manager Drive API";

    // Path of the directory where the authentication token is stored
    // Authentication tokens are used to not authenticate each time the software is opened
    private static final String TOEKENS_PATH = "tokens";

    // Path of the JSON file where there is the API key (inside "/resources")
    private static final String API_CREDENTIALS_PATH = "/credentials.json";

    // Define the scopes of the API
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);

    // Define the name of the directory used to store the database inside Drive
    private static final String DIRECTORY_DRIVE_NAME = ".moda";

    private Drive drive;  // The Google Drive service
    private JsonFactory jsonFactory;  // Used to handle all the data of the JSON

    public GoogleDrive(){
        this.jsonFactory = GsonFactory.getDefaultInstance();

        // TODO: Temporary disabled until proper backend event are defined
//        initDriveService();
//
//        createDirectory();
    }

    // Initialize the Drive service
    private void initDriveService(){
        try {
            NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();  // Create an HTTP transport object to handle all the HTTP operations
            Credential credentials = authentication(httpTransport);  // Retrieve the credentials from the authentication

            // Create the Drive object based on the HTTP transport and the credentials retrieved
            this.drive = new Drive.Builder(httpTransport, this.jsonFactory, credentials).setApplicationName(GoogleDrive.APPLICATION_NAME).build();

        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Authorize the Password Manager using OAuth 2.0 and return the credentials
    private Credential authentication(NetHttpTransport httpTransport){

        Credential credential; // Define the credential to be returned before the try-catch block

        // Read the API key from the file
        InputStream inputStream = getClass().getResourceAsStream(GoogleDrive.API_CREDENTIALS_PATH);

        try {
            // Create the client secrets from the API key
            GoogleClientSecrets googleClientSecrets = GoogleClientSecrets.load(this.jsonFactory, new InputStreamReader(inputStream));

            // Create the Authorization Flow used to exchange the authorization code for a token
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport, this.jsonFactory, googleClientSecrets, SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(GoogleDrive.TOEKENS_PATH)))  // Set the directory where the token will be stored
                    .setAccessType("offline")  // Set the Access Type to offline to have a token that lasts longer
                    .build();
            LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();  // Create a local server used for the authentication
            credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return credential;  // Return the credential given by the OAuth

    }

    // Create the Drive directory where the database will be stored
    private void createDirectory(){
        // If the directory already exist we can skip the creation of it
        if (getDirectoryID() != null){
            return;
        }

        // Define the new directory
        File directoryMetadata = new File();
        directoryMetadata.setName(GoogleDrive.DIRECTORY_DRIVE_NAME);
        directoryMetadata.setMimeType("application/vnd.google-apps.folder");

        // Create the directory
        try {
            this.drive.files().create(directoryMetadata).setFields("id").execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    // Retrieve the ID of the folder of the password manager
    // Returns null only if the directory does not exist
    private String getDirectoryID(){

        List<File> folders;  // Define the list of the folders before the try-catch block

        try {
            // Look only for folders and with the same name of the directory we are searching the ID, in the root directory
            FileList result = this.drive.files().list()
                    .setQ("mimeType='application/vnd.google-apps.folder' and name='" + GoogleDrive.DIRECTORY_DRIVE_NAME + "'")
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

    // Retrieve the ID of the database of the password manager
    // Returns null only if the database does not exist
    private String getDatabaseID(){

         List<File> files;

        try {
            FileList result = this.drive.files().list()
                    .setQ("name='"+Database.getDbName()+"' and '"+getDirectoryID()+"' in parents")
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

    // Upload the database to Drive
    private void uploadDatabase(){
        // Create a Java File object with the path of the database
        java.io.File database = new java.io.File(Database.getDbPath());

        // Create the metadata of the database for the Drive upload
        File databaseMetadata = new File();
        databaseMetadata.setName(Database.getDbName());

        // Specify that the database has to be uploaded inside the database directory
        databaseMetadata.setParents(Collections.singletonList(getDirectoryID()));

        // Specify how the file should be sent
        FileContent fileContent = new FileContent("application/octet-stream", database);

        try {
            // Delete the previous database file inside the directory only if the database exists
            String databaseID = getDatabaseID();
            if (databaseID != null){
                this.drive.files().delete(getDatabaseID()).execute();
            }


            // Upload the file to Drive and set its id and its parents
            this.drive.files().create(databaseMetadata, fileContent).setFields("id, parents").execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Download the database from Drive
    private void downloadDatabase(){
        OutputStream outputStream;
        try {
            outputStream = new FileOutputStream(Database.getDbPath());  // Define the path where the DB will be saved
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            // Retrieve the database based on its ID and download it to the local path
            this.drive.files().get(getDatabaseID()).executeMediaAndDownloadTo(outputStream);

            outputStream.flush();  // Clean the buffer of the download
            outputStream.close();  // Close the file to save the changes
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Return the last change made to the database inside the drive
    public long getLastChangeDrive(){
        File database;

        String databaseID = getDatabaseID();  // Get the ID of the database

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

    // Return the last change made to the local database file
    public long getLastChangeLocal(){
        java.io.File database = new java.io.File(Database.getDbPath());

        long lastChange = database.lastModified();

        return lastChange;  // Return the last change as a long value
    }

    // Check whether the local database is newer than the drive version and synchronize it based on the result obtained
    public void sync(){

        // Check if the local database is newer than the drive version
        if (getLastChangeLocal() > getLastChangeDrive()){
            uploadDatabase();
        }else{
            downloadDatabase();  // Download the database because the drive version is newer
        }
    }
}
