package moda.passwordManager.backend;

import java.util.ArrayList;

/**
 * The BackendHelper class is used to provide methods that can be used by all the APIs of the Backend class.
 */
public class BackendHelper {

    private Cryptography cryptography;
    private Settings settings;

    public BackendHelper(Cryptography cryptography, Settings settings){
        this.cryptography = cryptography;
        this.settings = settings;
    }

    /**
     * Decrypts any string that was encrypted and decodes it
     * @param ciphertext The string to decrypt
     * @return Decrypted string
     */
    private String decrypt(String ciphertext){
        return new String(this.cryptography.decrypt(Data.decode(ciphertext)));
    }

    /**
     * Encrypts any string and encodes it to base64
     * @param plaintext The string to encrypt
     * @return Encrypted and encoded string
     */
    private String encrypt(String plaintext){
        return Data.encodeToBase64(this.cryptography.encrypt(plaintext));
    }

    /**
     * Encrypts all the plaintext strings of a Data object
     * @param data The Data object to be encrypted
     * @return The encrypted data
     */
    public Data encryptData(Data data){
        String username = encrypt(data.getUSERNAME());
        String emailAddress = encrypt(data.getEMAIL_ADDRESS());
        String password = encrypt(data.getPASSWORD());
        String service = encrypt(data.getSERVICE());
        String additionalData = encrypt(data.getADDITIONAL_DATA());

        return new Data(data.getID(), username, emailAddress, password, service, additionalData);
    }

    /**
     * Decrypts all the ciphertext strings of a Data object
     * @param data The Data object to be decrypted
     * @return The decrypted data
     */
    public Data decryptData(Data data){
        ArrayList<String> fields = new ArrayList<>();  // Store the encrypted fields here before setting them in Data

        // Iterate over all the fields and decrypt them if they are strings, otherwise add them as null
        for (String field : data.getFullUserData()){
            if (field == null){
                fields.add(null);
            }else{
                fields.add(decrypt(field));
            }
        }

        return new Data(data.getID(), fields.getFirst(), fields.get(1), fields.get(2), fields.get(3), fields.get(4));
    }

    /**
     * Read the string generation configuration from the settings file
     * @return An ArrayList containing the properties of the generation in the following order: 0 - String Length,
     * 1 - Boolean Letters, 2 - Boolean Numbers, 3 - Boolean Special
     */
    public ArrayList<Object> getStringGenerationConfiguration(){
        ArrayList<Object> stringGeneration = new ArrayList<>();

        // Read all the properties from the settings file
        int stringLength = this.settings.readIntSetting("string_generation/length");
        boolean letters = this.settings.readBooleanSetting("string_generation/letters");
        boolean numbers = this.settings.readBooleanSetting("string_generation/numbers");
        boolean special = this.settings.readBooleanSetting("string_generation/special");

        // Add the properties to the ArrayList
        stringGeneration.add(stringLength);
        stringGeneration.add(letters);
        stringGeneration.add(numbers);
        stringGeneration.add(special);

        return stringGeneration;
    }

}
