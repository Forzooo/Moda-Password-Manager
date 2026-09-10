package moda.passwordmanager.backend;

import java.util.LinkedHashMap;
import java.util.Objects;

public class Data {

    /**
     * The value of the ID if it has not been set by the constructor
     */
    private static final int ID_NOT_SET = -1;

    private final int id;
    private final String username;
    private final String emailAddress;
    private final String password;
    private final String service;
    private final String additionalData;

    public Data(int id, String username, String emailAddress, String password, String service, String additionalData){
        this.id = id;
        this.username = username;
        this.emailAddress = emailAddress;
        this.password = password;
        this.service = service;
        this.additionalData = additionalData;
    }

    /**
     * Constructor used for the JList of "Show Data" of Frontend where only ID and service are used
     */
    public Data(int id, String service){
        this.id = id;
        this.username = null;
        this.emailAddress = null;
        this.password = null;
        this.service = service;
        this.additionalData = null;

    }

    public Data(String username, String emailAddress, String password, String service, String additionalData){
        this.id = ID_NOT_SET;  // Set the ID as -1 as it won't be used when this constructor is called
        this.username = username;
        this.emailAddress = emailAddress;
        this.password = password;
        this.service = service;
        this.additionalData = additionalData;
    }

    public int getId() {
        return this.id;
    }

    public String getUsername() {
        return this.username;
    }

    public String getEmailAddress() {
        return this.emailAddress;
    }

    public String getPassword() {
        return this.password;
    }

    public String getService() {
        return this.service;
    }

    public String getAdditionalData() {
        return this.additionalData;
    }

    /**
     * Get all the attributes as a linked hash map object to perform operations easily over them, where also the ID
     * is saved as a String object
     */
    public LinkedHashMap<String, String> asLinkedHashMap(){
        LinkedHashMap<String, String> data = new LinkedHashMap<>();
        data.put("id", String.valueOf(this.id));
        data.put("username", this.username);
        data.put("email_address", this.emailAddress);
        data.put("password", this.password);
        data.put("service", this.service);
        data.put("additional_data", this.additionalData);
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Data data = (Data) o;
        return this.id == data.id && Objects.equals(this.username, data.getUsername()) &&
                Objects.equals(this.emailAddress, data.getEmailAddress()) &&
                Objects.equals(this.password, data.getPassword()) && Objects.equals(this.service, data.getService()) &&
                Objects.equals(this.additionalData, data.getAdditionalData());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.username, this.emailAddress, this.password, this.service,
                this.additionalData);
    }
}
