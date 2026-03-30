package moda.passwordmanager.backend;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class Cryptography {

    // Argon2id Settings
    private final static int ARGON_MEMORY = 64;  // The memory is converted to KB in the hash function (Assuming is always in MB)
    private final static int ARGON_PARALLELISM = 2;
    private final static int ARGON_ITERATIONS = 3;
    private final static int ARGON_SALT_SIZE = 16;

    // AES-GCM Settings
    private final static int AES_KEY_SIZE = 32;  // The length is set to bytes for better compatibility
    private final static int AES_IV_SIZE = 12;
    private final static int AES_GMC_TAG_SIZE = 128;  // The length is set to bytes

    // Class attributes
    private byte[] masterPassword;  // The master password that allows encryption/decryption
    private SecureRandom secureRandom;
    private Argon2Parameters.Builder hashBuilder;  // The builder for hash generation

    public Cryptography(){
        this.masterPassword = null;  // The master password is set only with the set method
        this.secureRandom = new SecureRandom();

        initCryptography();
    }

    private void initCryptography(){
        // Set the Argon2 builder with the settings specified in the attributes of the class
        this.hashBuilder = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withIterations(Cryptography.ARGON_ITERATIONS)  // Set the number of iterations
                .withMemoryAsKB(Cryptography.ARGON_MEMORY*1000)  // Set the RAM usage of Argon in KiloBytes
                .withParallelism(Cryptography.ARGON_PARALLELISM);  // Set the number of threads
    }

    /**
     * Set the master password for encryption and decryption
     * @param masterPassword The master password the user has provided
     */
    public void setMasterPassword(byte[] masterPassword) {
        this.masterPassword = masterPassword.clone();
    }

    /**
    * Generate and return a String, which contains in the following order:
    * <ul>
    *     <li>Ciphertext of the plaintext data</li>
    *     <li>GMC Tag, used to check that the ciphertext hasn't been changed by an error or an attacker</li>
    *     <li>IV, used to generate the ciphertext</li>
    *     <li>Salt, used to compute the Encryption Key for the decryption (16 bytes)</li>
    * </ul>
    */
    public byte[] encrypt(String plaintext){
        reseed();  // Reseed the Random Generator before any operations

        byte[] plaintextBytes = plaintext.getBytes();  // Convert the plaintext string to bytes

        // Generate a random salt for the hash and an IV for AES
        byte[] salt = new byte[ARGON_SALT_SIZE];
        byte[] iv = new byte[AES_IV_SIZE];
        generateBytes(salt);
        generateBytes(iv);

        byte[] hash = hash(salt);  // Generate a hash, which will be the Encryption Key for AES, using Argon2id

        byte[] ciphertext = computeAES(plaintextBytes, hash, iv, Cipher.ENCRYPT_MODE);

        // Add the Initialization Vector and the Salt to the ciphertext byte array
        ciphertext = addData(ciphertext, iv);
        ciphertext = addData(ciphertext, salt);

        return ciphertext;  // Return the encrypted data
    }

    // Removes all the headers from the ciphertext and decrypts it
    public byte[] decrypt(byte[] encryptedData){
        byte[] salt = readHeader(encryptedData, ARGON_SALT_SIZE);
        encryptedData = removeHeader(encryptedData, ARGON_SALT_SIZE);

        byte[] iv = readHeader(encryptedData, AES_IV_SIZE);
        encryptedData = removeHeader(encryptedData, AES_IV_SIZE);

        byte[] hash = hash(salt); // Regenerate the hash, which will be the Decryption Key for AES, using Argon2id

        byte[] plaintextData = computeAES(encryptedData, hash, iv, Cipher.DECRYPT_MODE);

        return plaintextData;
    }

    /**
     * Generate random bytes for a variable
     * @param array The byte array which bytes are generated
     */
    private void generateBytes(byte[] array){
         this.secureRandom.nextBytes(array);
    }

    /**
     * Reseed the secure random generator
     */
    private void reseed(){
        this.secureRandom.reseed();
    }

    // Hash the master password with a salt using Argon2id with the above settings

    /**
     * Generate an Hash using the Argon2id algorithm
     * @param salt A randomically generated salt
     * @return Random hash generated with Argon2id
     */
    private byte[] hash(byte[] salt){
        this.hashBuilder.withSalt(salt);  // Set the salt of the hash generator

        // The hashGenerator has to be created locally, otherwise a race condition can happen
        Argon2BytesGenerator hashGenerator = new Argon2BytesGenerator();
        hashGenerator.init(this.hashBuilder.build());

        byte[] hash = new byte[Cryptography.AES_KEY_SIZE];  // Create the array of bytes where the hash will be stored
        hashGenerator.generateBytes(this.masterPassword, hash);  // Compute the hashes using the master password

        return hash;
    }

    /**
     * Encrypt or decrypt a byte array using AES-GCM
     * @param data The data to be encrypted/decrypted
     * @param hash The hash used to create the encryption key
     * @param iv The Initialization Vector used to compute the AES
     * @param cipherMode Specifies whether to encrypt or decrypt
     * @return The encrypted/decrypted data encoded in a byte array
     */
    private byte[] computeAES(byte[] data, byte[] hash, byte[] iv, int cipherMode){
        // The aes object has to be created locally, otherwise a race condition can happen
        Cipher aes;
        try {
            aes = Cipher.getInstance("AES/GCM/NoPadding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
        SecretKeySpec encryptionKey = new SecretKeySpec(hash, "AES");  // Create a Secret Key from the hash

        // Define the parameters of the AES GCM cipher
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(AES_GMC_TAG_SIZE, iv);
        try {
            // Initialize the AES in encryption/decryption mode with the encryption key (the hash) generated from
            // Argon2id and the parameters specified in the GCMParamterSpec object
            aes.init(cipherMode, encryptionKey, gcmParameterSpec);

            return aes.doFinal(data);  // Encrypt/decrypt the data
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException |
                 InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Add data in byte array to another one
     * @param array The array which data will be copied to
     * @param data The data to copy
     * @return A new byte array with both the arrays
     */
    private byte[] addData(byte[] array, byte[] data){
        // The ByteArrayOutputStream allows to write each array into its stream before getting back a byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(array.length + data.length);  // Set the len
        try {
            // Write the arrays into the stream
            outputStream.write(array);
            outputStream.write(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return outputStream.toByteArray();
    }

    /**
     * Read a header from the encrypted data
     * @param data The encrypted data
     * @param headerLength The length of the header
     * @return The header read from the last byte to the (last byte - length)
     */
    private byte[] readHeader(byte[] data, int headerLength){
        byte[] header = new byte[headerLength];

        for (int i = 0; i < headerLength; i++){
            header[i] = data[data.length-headerLength+i];
        }

        return header;
    }

    /**
     * Remove a header from the data
     * @param data The encrypted data
     * @param headerLength The length of the header to be removed
     * @return The data without the header which is removed from the last byte to the (last byte - length)
     */
    private byte[] removeHeader(byte[] data, int headerLength){
        byte[] newData = new byte[data.length - headerLength];

        for (int i = 0; i < newData.length; i++){
            newData[i] = data[i];
        }

        return newData;
    }

}
