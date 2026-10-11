package moda.passwordmanager.backend;

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

    private final Cryptography cryptography;

    // Execute operations in the background
    private final ScheduledExecutorService backgroundExecutor;

    public Helper(Cryptography cryptography){
        this.cryptography = cryptography;

        this.backgroundExecutor = Executors.newScheduledThreadPool(2);  // Initialize the Background Executor
    }

    /**
     * Return the path of "%appdata%/Moda/Password-Manager/" directory for the current user
     */
    public static String getPasswordManagerAppDataPath() {
        return System.getenv("APPDATA")+"\\Moda\\Password-Manager\\";
    }

    /**
     * Schedule a method to be executed in the background
     * @param method The method to be executed
     */
    public void executeInBackground(Runnable method){
        this.backgroundExecutor.schedule(method, 0, TimeUnit.SECONDS);
    }

    /**
     * Schedule a method to be executed in the background
     * @param delay The time to wait before executing the method (in seconds)
     * @param method The method to be executed
     */
    public void executeInBackground(int delay, Runnable method){
        this.backgroundExecutor.schedule(method, delay, TimeUnit.SECONDS);
    }

    /** Schedule a method to be executed in the background
     * @param method The method to be executed
     * @param period The period that has to pass before executing again the method (in seconds)
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
        LinkedHashMap<Data.Fields, String> fields = data.asLinkedHashMap();

        for (Map.Entry<Data.Fields, String> entry : fields.entrySet()){
            if (entry.getValue() != null && !entry.getKey().equals(Data.Fields.ID)){
                fields.put(entry.getKey(), decrypt(entry.getValue()));  // Update each field with its decrypted value
            }
        }

        // We put the ID inside the fields to use a cleaner Data constructor
        fields.put(Data.Fields.ID, String.valueOf(data.getId()));
        return new Data(fields);
    }

    /**
     * Encode any given data, in byte array format, to a Base64 format string
     * @return A Base64 encoded string
     */
    public static String encodeBase64(byte[] data){
        return Base64.getEncoder().encodeToString(data);
    }

    /**
     * Decode any given data, in Base64 byte array format
     * @return A byte array
     */
    public static byte[] decodeBase64(String data){
        return Base64.getDecoder().decode(data.getBytes());
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
