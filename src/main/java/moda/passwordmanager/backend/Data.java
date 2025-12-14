package moda.passwordmanager.backend;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

public class Data {

    private final int ID;
    private final String USERNAME;
    private final String EMAIL_ADDRESS;
    private final String PASSWORD;
    private final String SERVICE;
    private final String ADDITIONAL_DATA;

    public Data(int id, String username, String emailAddress, String password, String service, String additional){
        this.ID = id;
        this.USERNAME = username;
        this.EMAIL_ADDRESS = emailAddress;
        this.PASSWORD = password;
        this.SERVICE = service;
        this.ADDITIONAL_DATA = additional;
    }

    /**
     * Constructor used for the JList of "Show Data" of Frontend where only ID and service are used
     * @param id
     * @param service
     */
    public Data(int id, String service){
        this.ID = id;
        this.USERNAME = null;
        this.EMAIL_ADDRESS = null;
        this.PASSWORD = null;
        this.SERVICE = service;
        this.ADDITIONAL_DATA = null;

    }

    public Data(byte[] username, byte[] emailAddress, byte[] password, byte[] service, byte[] additional){
        this.ID = -1;  // Set the ID as -1 as it won't be used when this constructor is called
        this.USERNAME = new String(username, StandardCharsets.UTF_8);
        this.EMAIL_ADDRESS = new String(emailAddress, StandardCharsets.UTF_8);
        this.PASSWORD = new String(password, StandardCharsets.UTF_8);
        this.SERVICE = new String(service, StandardCharsets.UTF_8);
        this.ADDITIONAL_DATA = new String(additional, StandardCharsets.UTF_8);
    }

    public Data(String username, String emailAddress, String password, String service, String additional){
        this.ID = -1;  // Set the ID as -1 as it won't be used when this constructor is called
        this.USERNAME = username;
        this.EMAIL_ADDRESS = emailAddress;
        this.PASSWORD = password;
        this.SERVICE = service;
        this.ADDITIONAL_DATA = additional;
    }

    /*
     * Get section for all the data to be able to be read from other classes.
     */

    public int getID() {
        return ID;
    }

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
     * Return a String array which includes all the data of a record, not including the ID, to iterate more
     * easily over them.
     * @return String array of 5 elements
     */
    public String[] getFullUserData(){
        return new String[]{this.USERNAME, this.EMAIL_ADDRESS, this.PASSWORD, this.SERVICE, this.ADDITIONAL_DATA};
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.ID, this.USERNAME, this.EMAIL_ADDRESS, this.PASSWORD, this.SERVICE,
                this.ADDITIONAL_DATA);
    }
}
