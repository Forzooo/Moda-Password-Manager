package moda.passwordmanager.backend.settings;

import moda.passwordmanager.backend.Database;
import moda.passwordmanager.backend.Helper;

import java.util.List;

/**
 * The sensitive settings are stored inside the database file instead of the settings.json file, and are always encrypted
 */
public class SensitiveSettings extends AbstractSettings {

    private final Database database;
    private final Helper helper;

    public SensitiveSettings(Database database, Helper helper){
        this.database = database;
        this.helper = helper;

    }

    /**
     * The Sensitive Settings must be initialized separately as they require the master password to be used
     */
    public void init(){
        initRecords();
    }

    /**
     * Add all the record inside the table with their default values
     */
    private void initRecords(){
        this.database.addSensitiveSettingsRecord("google_drive/enabled", this.helper.encrypt("false"));
        this.database.addSensitiveSettingsRecord("google_drive/credentials", this.helper.encrypt(""));
        this.database.addSensitiveSettingsRecord("google_drive/stored_credentials", this.helper.encrypt(""));
        this.database.addSensitiveSettingsRecord("google_drive/automatic_synchronization", this.helper.encrypt("false"));
    }

    /**
     * Read a property from the sensitive settings inside the database
     * @param propertyPath The path of the property (ex. google_drive/enabled)
     */
    @Override
    public String readStringProperty(String propertyPath){
        String encryptedValue = this.database.getSensitiveSettingsValue(propertyPath);

        return this.helper.decrypt(encryptedValue);
    }

    /**
     * Read a property from the sensitive settings inside the database
     * @param propertyPath The path of the property (ex. google_drive/enabled)
     */
    @Override
    public int readIntProperty(String propertyPath){
        String encryptedValue = this.database.getSensitiveSettingsValue(propertyPath);
        String decryptedValue = this.helper.decrypt(encryptedValue);

        return Integer.parseInt(decryptedValue);  // All the values inside the database are treated as TEXT, so it must
                                                  // be parsed to Integer
    }

    /**
     * Read a property from the sensitive settings inside the database
     * @param propertyPath The path of the property (ex. google_drive/enabled)
     */
    @Override
    public boolean readBooleanProperty(String propertyPath){
        String encryptedValue = this.database.getSensitiveSettingsValue(propertyPath);
        String decryptedValue = this.helper.decrypt(encryptedValue);

        return Boolean.parseBoolean(decryptedValue);  // All the values inside the database are treated as TEXT, so it must
                                                      // be parsed to Boolean
    }

    @Override
    public List<Object> readListProperty(String propertyPath){
        throw new UnsupportedOperationException();
    }

    /**
     * Update a property value of the Sensitive Settings
     * @param propertyPath The path of the property (ex. google_drive/enabled)
     * @param value The new value of the property (not encrypted)
     */
    @Override
    public void writeProperty(String propertyPath, Object value){
        String encryptedValue = this.helper.encrypt(value.toString());
        this.database.updateSensitiveSettingsValue(propertyPath, encryptedValue);
    }

    @Override
    public void writeSubproperty(String propertyPath, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeListProperty(String propertyPath, List<Object> values){
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeListSubproperty(String propertyPath, List<Object> values) {
        throw new UnsupportedOperationException();
    }
}
