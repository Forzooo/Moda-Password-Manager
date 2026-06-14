package moda.passwordmanager.backend;

/**
 * The sensitive settings are stored inside the database file instead of the settings.json file, and are always encrypted
 */
public class SensitiveSettings {

    private final Database DATABASE;
    private final Helper HELPER;

    public SensitiveSettings(Database database, Helper helper){
        this.DATABASE = database;
        this.HELPER = helper;

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

}
