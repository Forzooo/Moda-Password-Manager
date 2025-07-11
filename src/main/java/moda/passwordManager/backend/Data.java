package moda.passwordManager.backend;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Data {

    private final String USERNAME;
    private final String EMAIL_ADDRESS;
    private final String PASSWORD;
    private final String SERVICE;
    private final String ADDITIONAL_DATA;

    public Data(String username, String emailAddress, String password, String service, String additional){
        this.USERNAME = username;
        this.EMAIL_ADDRESS = emailAddress;
        this.PASSWORD = password;
        this.SERVICE = service;
        this.ADDITIONAL_DATA = additional;
    }

    public Data(byte[] username, byte[] emailAddress, byte[] password, byte[] service, byte[] additional){
        this.USERNAME = new String(username, StandardCharsets.UTF_8);
        this.EMAIL_ADDRESS = new String(emailAddress, StandardCharsets.UTF_8);
        this.PASSWORD = new String(password, StandardCharsets.UTF_8);
        this.SERVICE = new String(service, StandardCharsets.UTF_8);
        this.ADDITIONAL_DATA = new String(additional, StandardCharsets.UTF_8);
    }

    /*
    * Get section for all the data to be able to be read from other classes.
    */
    public String getUSERNAME() {
        return this.USERNAME;
    }

    public String getEMAIL_ADDRESS() {
        return this.EMAIL_ADDRESS;
    }

    public String getPASSWORD() {
        return this.PASSWORD;
    }

    public String getSERVICE() {
        return this.SERVICE;
    }

    public String getADDITIONAL_DATA() {
        return this.ADDITIONAL_DATA;
    }

    /**
     * Encode any given data, in byte array format, to the Base64 format
     * @param data
     * @return A Base64 byte array
     */
    public static byte[] encode(byte[] data){
        return Base64.getEncoder().encode(data);
    }

    /**
     * Encode any given data, in byte array format, to the Base64 format
     * @param data
     * @return A Base64 byte array
     */
    public static byte[] encode(String data){
        return Base64.getEncoder().encode(data.getBytes());
    }

    public static String encodeToString(byte[] data){
        return new String(data, StandardCharsets.UTF_8);
    }


    /**
     * Encode any given data, in byte array format, to a Base64 format string
     * @param data
     * @return A Base64 encoded string
     */
    public static String encodeToBase64(byte[] data){
        return Base64.getEncoder().encodeToString(data);
    }

    /**
     * Decode any given data, in Base64 byte array format
     * @param data
     * @return A byte array
     */
    public static byte[] decode(byte[] data){
        return Base64.getDecoder().decode(data);
    }

    /**
     * Decode any given data, in Base64 byte array format
     * @param data
     * @return A byte array
     */
    public static byte[] decode(String data){
        return Base64.getDecoder().decode(data.getBytes());
    }
}
