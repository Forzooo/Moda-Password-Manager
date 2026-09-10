package moda.passwordmanager.backend;

import moda.passwordmanager.backend.settings.Settings;
import moda.passwordmanager.frontend.properties.Languages;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The Helper class is used to provide methods that can be used by all the APIs of the Backend class.
 */
public class Helper {

    private static final String DEFAULT_DATABASE = "moda-password-manager"+Database.getFileExtension();
    private static final String SETTINGS_FILE = "settings.json";

    private final Cryptography cryptography;
    private final Settings settings;

    // Execute operations in the background
    private final ScheduledExecutorService backgroundExecutor;

    public Helper(Cryptography cryptography, Settings settings){
        this.cryptography = cryptography;
        this.settings = settings;

        this.backgroundExecutor = Executors.newScheduledThreadPool(2);  // Initialize the Background Executor
    }

    /**
     * Return the path of "%appdata%/Moda/Password-Manager/" directory for the current user
     */
    public static String getPasswordManagerAppDataPath() {
        return System.getenv("APPDATA")+"\\Moda\\Password-Manager\\";
    }

    /**
     * Return the name of the default database used by the password manager
     */
    public static String getDefaultDatabase() {
        return DEFAULT_DATABASE;
    }

    /**
     * Return the name of the settings file used by the password manager
     */
    public static String getSettingsFile() {
        return SETTINGS_FILE;
    }

    /**
     * Schedule a method to be executed in the background
     * @param method The method to be executed
     */
    public void executeInBackground(Runnable method){
        this.backgroundExecutor.schedule(method, 0, TimeUnit.SECONDS);
    }

    /** Schedule a method to be executed in the background
     * @param method The method to be executed
     * @param period The period that has to pass before executing again the method
     */
    public void executePeriodicallyInBackground(Runnable method, long period){
        this.backgroundExecutor.scheduleAtFixedRate(method, 0, period, TimeUnit.SECONDS);
    }

    /**
     * Decrypts any string that was encrypted and decodes it
     * @param ciphertext The string to decrypt
     * @return Decrypted string
     */
    public String decrypt(String ciphertext){
        return new String(this.cryptography.decrypt(decodeBase64(ciphertext)));
    }

    /**
     * Encrypts any string and encodes it to base64
     * @param plaintext The string to encrypt
     * @return Encrypted and encoded string
     */
    public String encrypt(String plaintext){
        return encodeBase64(this.cryptography.encrypt(plaintext));
    }

    /**
     * Encrypts all the plaintext strings of a Data object
     * @param data The Data object to be encrypted
     * @return The encrypted data
     */
    public Data encryptData(Data data){
        String username = encrypt(data.getUsername());
        String emailAddress = encrypt(data.getEmailAddress());
        String password = encrypt(data.getPassword());
        String service = encrypt(data.getService());
        String additionalData = encrypt(data.getAdditionalData());

        return new Data(data.getId(), username, emailAddress, password, service, additionalData);
    }

    /**
     * Decrypts all the ciphertext strings of a Data object
     * @param data The Data object to be decrypted
     * @return The decrypted data
     */
    public Data decryptData(Data data){
        LinkedHashMap<String, String> fields = data.asLinkedHashMap();

        for (Map.Entry<String, String> entry : fields.entrySet()){
            if (entry.getValue() != null && !entry.getKey().equals("id")){
                fields.put(entry.getKey(), decrypt(entry.getValue()));
            }
        }

        return new Data(data.getId(), fields.get("username"), fields.get("email_address"), fields.get("password"),
                fields.get("service"), fields.get("additional_data"));
    }

    /**
     * Encode any given data, in byte array format, to a Base64 format string
     * @param data
     * @return A Base64 encoded string
     */
    public static String encodeBase64(byte[] data){
        return Base64.getEncoder().encodeToString(data);
    }

    /**
     * Decode any given data, in Base64 byte array format
     * @param data
     * @return A byte array
     */
    public static byte[] decodeBase64(String data){
        return Base64.getDecoder().decode(data.getBytes());
    }

    /**
     * Read the string generation configuration from the settings file
     * @return An ArrayList containing the properties of the generation in the following order: 0 - String Length,
     * 1 - Boolean Letters, 2 - Boolean Numbers, 3 - Boolean Special
     */
    public List<Object> getStringGenerationConfiguration(){
        ArrayList<Object> stringGeneration = new ArrayList<>();

        // Read all the properties from the settings file
        int stringLength = this.settings.readIntProperty("string_generation/length");
        boolean letters = this.settings.readBooleanProperty("string_generation/letters");
        boolean numbers = this.settings.readBooleanProperty("string_generation/numbers");
        boolean special = this.settings.readBooleanProperty("string_generation/special");

        // Add the properties to the ArrayList
        stringGeneration.add(stringLength);
        stringGeneration.add(letters);
        stringGeneration.add(numbers);
        stringGeneration.add(special);

        return stringGeneration;
    }

    /**
     * Retrieve from the settings file the path of the database
     * @return String that indicates the path of the database
     */
    public String getDatabasePath(){
        return this.settings.readStringProperty("database/selected");  // Read the path from settings
    }

    /**
     * Generate a random string of a certain length
     * @param charNum The length of the string
     * @param charSet The set of the characters to use
     */
    public static StringBuilder generateRandomString(int charNum, char[] charSet){
        SecureRandom secureRandom = new SecureRandom();  // Create a secure random object to generate the string
        StringBuilder stringBuilder = new StringBuilder();  // Create a StringBuilder object to append characters better

        for (int i = 0; i < charNum; i++){
            int index = secureRandom.nextInt(charSet.length);  // Generate an index between 0, and the length of the set
            stringBuilder.append(charSet[index]);  // Append the character at the random index to the string builder
        }

        return stringBuilder;
    }

    /**
     * Returns the language used by the OS, but only if supported by the Password Manager, otherwise it returns English.
     */
    public static String getSystemLanguage(){
        Locale systemLanguage = Locale.getDefault();

        String passwordManagerLanguage;

        switch (systemLanguage.getLanguage()){
            case "en" -> passwordManagerLanguage = Languages.ENGLISH.toString();
            case "it" -> passwordManagerLanguage = Languages.ITALIAN.toString();
            default -> passwordManagerLanguage = Languages.ENGLISH.toString();
        }

        return passwordManagerLanguage;
    }

    /**
     * Calculates the MD5 Hash of the content of a file
     */
    public static String calculateFileMD5(String filepath){
        try {
            // Read the file content and calculate the MD5 using Apache Commons library
            return DigestUtils.md5Hex(Files.readAllBytes(Path.of(filepath)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
