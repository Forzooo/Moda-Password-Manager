package moda.passwordmanager.backend;

import java.util.ArrayList;

/**
 * The sensitive settings are stored inside the database file instead of the settings.json file, and are always encrypted
 */
public class SensitiveSettings extends AbstractSettings {

    private final Database DATABASE;
    private final Helper HELPER;

    public SensitiveSettings(Database database, Helper helper){
        this.DATABASE = database;
        this.HELPER = helper;

    }

    /**
     * The Sensitive Settings must be initialized separately as they require the
     */
    public void init(){
        initRecords();
    }

    /**
     * Add all the record inside the table with their default values
     */
    private void initRecords(){
        this.DATABASE.addSensitiveSettingsRecord("google_drive/enabled", this.HELPER.encrypt("false"));
        this.DATABASE.addSensitiveSettingsRecord("google_drive/credentials", this.HELPER.encrypt(""));
        this.DATABASE.addSensitiveSettingsRecord("google_drive/storedCredential", this.HELPER.encrypt(""));
        this.DATABASE.addSensitiveSettingsRecord("google_drive/automatic_synchronization", this.HELPER.encrypt("false"));
    }

    /**
     * Read a property from the sensitive settings inside the database
     * @param propertyPath The path of the property (ex. google_drive/enabled)
     */
    @Override
    public String readStringProperty(String propertyPath){
        String encryptedValue = this.DATABASE.getSensitiveSettingsValue(propertyPath);

        return this.HELPER.decrypt(encryptedValue);
    }

    /**
     * Read a property from the sensitive settings inside the database
     * @param propertyPath The path of the property (ex. google_drive/enabled)
     */
    @Override
    public int readIntProperty(String propertyPath){
        String encryptedValue = this.DATABASE.getSensitiveSettingsValue(propertyPath);
        String decryptedValue = this.HELPER.decrypt(encryptedValue);

        return Integer.parseInt(decryptedValue);  // All the values inside the database are treated as TEXT, so it must
                                                  // be parsed to Integer
    }

    /**
     * Read a property from the sensitive settings inside the database
     * @param propertyPath The path of the property (ex. google_drive/enabled)
     */
    @Override
    public boolean readBooleanProperty(String propertyPath){
        String encryptedValue = this.DATABASE.getSensitiveSettingsValue(propertyPath);
        String decryptedValue = this.HELPER.decrypt(encryptedValue);

        return Boolean.parseBoolean(decryptedValue);  // All the values inside the database are treated as TEXT, so it must
                                                      // be parsed to Boolean
    }

    @Override
    public ArrayList readListProperty(String propertyPath){
        return null;
    }

    /**
     * Update a property value of the Sensitive Settings
     * @param propertyPath The path of the property (ex. google_drive/enabled)
     * @param value The new value of the property (not encrypted)
     */
    @Override
    public void writeProperty(String propertyPath, Object value){
        String encryptedValue = this.HELPER.encrypt(value.toString());
        this.DATABASE.updateSensitiveSettingsValue(propertyPath, encryptedValue);
    }

    @Override
    public void writeListProperty(String propertyPath, ArrayList values){

    }
}
