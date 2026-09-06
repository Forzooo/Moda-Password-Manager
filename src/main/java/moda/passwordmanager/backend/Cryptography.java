package moda.passwordmanager.backend;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class Cryptography {

    // Argon2id Settings
    private static final int ARGON_MEMORY = 64000;  // The memory usage is in KB
    private static final int ARGON_PARALLELISM = 2;
    private static final int ARGON_ITERATIONS = 3;
    private static final int ARGON_SALT_SIZE = 16;

    // AES-GCM Settings
    private static final int AES_KEY_SIZE = 32;  // The length is set to bytes for better compatibility
    private static final int AES_IV_SIZE = 12;
    private static final int AES_GMC_TAG_SIZE = 128;  // The length is set to bits

    // Class attributes
    private byte[] masterPassword;  // The master password that allows encryption/decryption
    private final SecureRandom secureRandom;

    public Cryptography(){
        this.masterPassword = null;  // The master password is set only with the set method
        this.secureRandom = new SecureRandom();
    }

    /**
     * Set the master password for encryption and decryption
     * @param masterPassword The master password the user has provided
     */
    public void setMasterPassword(byte[] masterPassword) {
        this.masterPassword = masterPassword.clone();
    }

    /**
     * Encrypts the plaintext using AES with the given Master Password
     * @return A bytes array containing in the following order the ciphertext, the GCM Tag, the IV and the salt
     */
    public byte[] encrypt(String plaintext){
        // If by mistake a cryptography dependant operation is executed before setting the master password, then an
        // exception is arisen to let the developer know the issue
        if (this.masterPassword == null){
            throw new MasterPasswordNotSet("The master password must be set before executing any cryptographical operation.");
        }

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
        ciphertext = addHeader(ciphertext, iv);
        ciphertext = addHeader(ciphertext, salt);

        return ciphertext;  // Return the encrypted data
    }

    /**
     * Decrypts the ciphertext using AES with the given Master Password
     * @param encryptedData The encrypted data must contain in the following order the ciphertext, the GCM Tag, the IV
     *                      and the salt to allow the decryption
     * @return The plaintext
     */
    public byte[] decrypt(byte[] encryptedData){
        // If by mistake a cryptography dependant operation is executed before setting the master password, then an
        // exception is arisen to let the developer know the issue
        if (this.masterPassword == null){
            throw new MasterPasswordNotSet("The master password must be set before executing any cryptographical operation.");
        }

        byte[] salt = readHeader(encryptedData, ARGON_SALT_SIZE);
        encryptedData = removeHeader(encryptedData, ARGON_SALT_SIZE);

        byte[] iv = readHeader(encryptedData, AES_IV_SIZE);
        encryptedData = removeHeader(encryptedData, AES_IV_SIZE);

        byte[] hash = hash(salt); // Regenerate the hash, which will be the Decryption Key for AES, using Argon2id

        return computeAES(encryptedData, hash, iv, Cipher.DECRYPT_MODE);  // Return the plaintext data
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

    /**
     * Generate a random hash using the Argon2id algorithm
     * @param salt A randomly generated salt
     */
    private byte[] hash(byte[] salt){
        // The parameters and the hashGenerator have to be created locally, otherwise a race condition can happen
        Argon2Parameters parameters = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withIterations(Cryptography.ARGON_ITERATIONS)  // Set the number of iterations
                .withMemoryAsKB(Cryptography.ARGON_MEMORY)  // Set the RAM usage of Argon
                .withParallelism(Cryptography.ARGON_PARALLELISM)  // Set the number of threads
                .withSalt(salt)  // Set the salt of the hash generator
                .build();

        Argon2BytesGenerator hashGenerator = new Argon2BytesGenerator();
        hashGenerator.init(parameters);

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
     * Add a header to the end of a byte array
     * @param data The array which data will be copied to
     * @param header The data to copy
     * @return A new byte array with both the arrays
     */
    private byte[] addHeader(byte[] data, byte[] header){
        byte[] newData = new byte[data.length+header.length];

        System.arraycopy(data, 0, newData, 0, data.length);
        System.arraycopy(header, 0, newData, data.length, header.length);

        return newData;
    }

    /**
     * Read a header from the end of the bytes array
     * @param headerLength The length of the header to be read
     */
    private byte[] readHeader(byte[] data, int headerLength){
        byte[] header = new byte[headerLength];
        System.arraycopy(data, data.length-headerLength, header, 0, headerLength);
        return header;
    }

    /**
     * Remove a header from the end of the bytes array
     * @param headerLength The length of the header to be removed
     */
    private byte[] removeHeader(byte[] data, int headerLength){
        byte[] newData = new byte[data.length-headerLength];
        System.arraycopy(data, 0, newData, 0, data.length-headerLength);
        return newData;
    }

}
