package moda.passwordmanager.backend;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class Data {

    /**
     * The Fields enum provides an accessible way to interact with the methods that return an instance of the class as a
     * map
     */
    public enum Fields {
        ID,
        USERNAME,
        EMAIL_ADDRESS,
        PASSWORD,
        SERVICE,
        ADDITIONAL_DATA,
        /**
         * The "Added on remote" field is used only by the Google Drive class to indicate a Data record that has been
         * added on the remote version of the database. Thus, it must be used only in that context. Lastly, it does not
         * cause any issue with the asLinkedHashMap method because it does not appear in it.
         */
        ADDED_ON_REMOTE
    }

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

    public Data(Map<Fields, String> dataMap){
        this.id = Integer.parseInt(dataMap.get(Fields.ID));
        this.username = dataMap.get(Fields.USERNAME);
        this.emailAddress = dataMap.get(Fields.EMAIL_ADDRESS);
        this.password = dataMap.get(Fields.PASSWORD);
        this.service = dataMap.get(Fields.SERVICE);
        this.additionalData = dataMap.get(Fields.ADDITIONAL_DATA);
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
    public LinkedHashMap<Fields, String> asLinkedHashMap(){
        LinkedHashMap<Fields, String> data = new LinkedHashMap<>();
        data.put(Fields.ID, String.valueOf(this.id));
        data.put(Fields.USERNAME, this.username);
        data.put(Fields.EMAIL_ADDRESS, this.emailAddress);
        data.put(Fields.PASSWORD, this.password);
        data.put(Fields.SERVICE, this.service);
        data.put(Fields.ADDITIONAL_DATA, this.additionalData);
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
