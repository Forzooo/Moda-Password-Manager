package moda.passwordManager.backend;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

public class Cryptography {

    // Argon2id Settings
    private final static int ARGON_MEMORY = 64;  // The memory is converted to KB in the hash function (Assuming is always in MB)
    private final static int ARGON_PARALLELISM = 2;
    private final static int ARGON_ITERATIONS = 3;
    private final static int ARGON_SALT_SIZE = 16;

    // AES-GCM Settings
    private final static int AES_KEY_SIZE = 32;  // The length is set to bytes for better compatibility
    private final static int AES_IV_SIZE = 12;
    private final static int AES_GMC_TAG_SIZE = 16;

    // Attributes
    private byte[] salt;
    private byte[] iv;
    private byte[] gmcTag;
    private byte[] masterPassword;  // The master password that allows for encryption/decryption

    public Cryptography(){
        this.salt = new byte[Cryptography.ARGON_SALT_SIZE];
        this.iv = new byte[Cryptography.AES_IV_SIZE];
        this.gmcTag = new byte[Cryptography.AES_GMC_TAG_SIZE];

        // The master password is set only with the set method
        this.masterPassword = null;
    }

    // Set the master password as it's deleted every time the hash method is called
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
     public byte[] encrypt(String plaintextData){
          // Generate a new hash and IV for each encryption
          byte[] plaintextBytes = plaintextData.getBytes();
          generateHash();
          generateIV();

          byte[] hash = hash();  // Generate a hash, which will be the Encryption Key for AES, using Argon2id
          SecretKeySpec encryptionKey = new SecretKeySpec(hash, "AES");  // Create a Secret Key from the hash

          byte[] ciphertext = computeAES(plaintextBytes, encryptionKey, Cipher.ENCRYPT_MODE);

          byte[] encryptedData = addIV(ciphertext);  // Create a byte array and add the IV as an header

          encryptedData = addSalt(encryptedData);  // Add the salt to the encrypted data

          return encryptedData;  // Return the encrypted data
    }

    // Removes all the headers from the ciphertext and decrypts it
    public byte[] decrypt(byte[] encryptedData){

        encryptedData = extractSalt(encryptedData);  // Remove the Salt from the encrypted data
        encryptedData = extractIV(encryptedData);  // Remove the IV from the encrypted data

        byte[] hash = hash(); // Regenerate the hash, which will be the Decryption Key for AES, using Argon2id
        SecretKeySpec encryptionKey = new SecretKeySpec(hash, "AES");  // Create a Secret Key from the hash

        byte[] plaintextData = computeAES(encryptedData, encryptionKey, Cipher.DECRYPT_MODE);

        return plaintextData;
    }

    // Generate a secure hash for the hash function
    private void generateHash(){
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(this.salt);
    }

    // Generate a secure Initialization Vector for the AES function
    private void generateIV(){
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(this.iv);
    }

    // Hash the master password with a salt using Argon2id with the above settings
    private byte[] hash(){

        // Check whether the master password hasn't been set before calling an encryption/decryption
        if (Arrays.equals(this.masterPassword, new byte[this.masterPassword.length])){
            try {
                throw new Exception("The Master Password needs to be set before requesting an encryption/decryption.");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // Set the Argon2 builder with the settings specified in the attributes of the class
        Argon2Parameters.Builder builder = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withSalt(this.salt)  // Set the salt to the hash function
                .withIterations(Cryptography.ARGON_ITERATIONS)  // Set the number of iterations
                .withMemoryAsKB(Cryptography.ARGON_MEMORY*1000)  // Set the RAM usage of Argon in KiloBytes
                .withParallelism(Cryptography.ARGON_PARALLELISM);  // Set the number of threads

        // Create an Argon2 generator and set it with the parameters specified in the other Argon 2 object
        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(builder.build());

        byte[] hash = new byte[Cryptography.AES_KEY_SIZE];  // Create the array of bytes where the hash will be stored
        generator.generateBytes(this.masterPassword, hash);  // Compute the hashes using the master password

        // To prevent memory dump attacks the master password is deleted every time the hash method is called
//        Arrays.fill(this.masterPassword, (byte) 0);

        return hash;
    }

    // Compute the ciphertext/plaintext data using AES
    private byte[] computeAES(byte[] password, SecretKey encryptionKey, int cipherMode){
        Cipher aes;
        try {
            aes = Cipher.getInstance("AES/GCM/NoPadding");  // Define that the cipher used is AES-GCM
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }

        // Define the parameters of the AES GCM cipher
        // The TAG needs to be converted to bit from bytes
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(Cryptography.AES_GMC_TAG_SIZE*8, this.iv);
        try {
            // Init the AES in encryption/decryption mode with the encryption key generated
            // and the settings specified in the GCMParamterSpec object
            aes.init(cipherMode, encryptionKey, gcmParameterSpec);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }

        try {
            return aes.doFinal(password);  // Finally encrypt/decrypt the data
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    // Add the Salt to the ciphertext data
    private byte[] addSalt(byte[] ciphertext){

        byte[] data = new byte[ciphertext.length + Cryptography.ARGON_SALT_SIZE];

        for (int i = 0; i < ciphertext.length; i++){
            data[i] = ciphertext[i];
        }

        for (int i = 0; i < Cryptography.ARGON_SALT_SIZE; i++){
            data[ciphertext.length+i] = this.salt[i];
        }

        return data;
    }

    // Add the IV to the ciphertext data
    private byte[] addIV(byte[] ciphertext){

        byte[] data = new byte[ciphertext.length + Cryptography.AES_IV_SIZE];

        for (int i = 0; i < ciphertext.length; i++){
            data[i] = ciphertext[i];
        }

        for (int i = 0; i < Cryptography.AES_IV_SIZE; i++){
            data[ciphertext.length+i] = this.iv[i];
        }

        return data;
    }

    // Extract the salt from the encrypted data
    private byte[] extractSalt(byte[] encryptedData){
        for (int i = 0; i < Cryptography.ARGON_SALT_SIZE; i++){
            this.salt[i] = encryptedData[encryptedData.length - Cryptography.ARGON_SALT_SIZE+i];
        }

        encryptedData = updateEncryptedData(encryptedData, Cryptography.ARGON_SALT_SIZE);
        return encryptedData;
    }

    // Extract the IV from the encrypted data
    private byte[] extractIV(byte[] encryptedData){
        for (int i = 0; i < Cryptography.AES_IV_SIZE; i++){
            this.iv[i] = encryptedData[encryptedData.length-Cryptography.AES_IV_SIZE+i];
        }

        encryptedData = updateEncryptedData(encryptedData, Cryptography.AES_IV_SIZE);
        return encryptedData;
    }

    // Update the encrypted data removing an header: hash or IV
    private byte[] updateEncryptedData(byte[] encryptedData, int lenght){
        byte[] temporaryData = new byte[encryptedData.length - lenght];  // Recreate the encrypted data array without an header

        for (int i = 0; i < temporaryData.length; i++){
            temporaryData[i] = encryptedData[i];
        }

        return temporaryData;
    }

    /**
     * Randomically generate a string of a certain length
     * @param charNum The length of the string
     * @param charSet The set of the characters to use
     */
    public StringBuilder generateString(int charNum, char[] charSet){
        StringBuilder stringBuilder = new StringBuilder();  // Create a StringBuilder object to append characters better
        SecureRandom secureRandom = new SecureRandom();  // Used for the random character to pick

        for (int i = 0; i < charNum; i++){
            int index = secureRandom.nextInt(charSet.length);  // Generate an index between 0, and the length of the set
            stringBuilder.append(charSet[index]);  // Append the character at the random index to the string builder
        }

        return stringBuilder;
    }

}
