package moda.passwordmanager.backend;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class Data {

    /**
     * The value of the ID if it has not been set by the constructor
     */
    private final static int ID_NOT_SET = -1;

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
        this.ID = ID_NOT_SET;  // Set the ID as -1 as it won't be used when this constructor is called
        this.USERNAME = new String(username, StandardCharsets.UTF_8);
        this.EMAIL_ADDRESS = new String(emailAddress, StandardCharsets.UTF_8);
        this.PASSWORD = new String(password, StandardCharsets.UTF_8);
        this.SERVICE = new String(service, StandardCharsets.UTF_8);
        this.ADDITIONAL_DATA = new String(additional, StandardCharsets.UTF_8);
    }

    public Data(String username, String emailAddress, String password, String service, String additional){
        this.ID = ID_NOT_SET;  // Set the ID as -1 as it won't be used when this constructor is called
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
        return this.ID;
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
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Data data = (Data) o;
        return this.ID == data.ID && Objects.equals(this.USERNAME, data.getUSERNAME()) &&
                Objects.equals(this.EMAIL_ADDRESS, data.getEMAIL_ADDRESS()) &&
                Objects.equals(this.PASSWORD, data.getPASSWORD()) && Objects.equals(this.SERVICE, data.getSERVICE()) &&
                Objects.equals(this.ADDITIONAL_DATA, data.getADDITIONAL_DATA());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.ID, this.USERNAME, this.EMAIL_ADDRESS, this.PASSWORD, this.SERVICE,
                this.ADDITIONAL_DATA);
    }
}
