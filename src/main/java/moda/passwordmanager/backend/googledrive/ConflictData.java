package moda.passwordmanager.backend.googledrive;

import moda.passwordmanager.backend.Data;

public class ConflictData extends Data {

    private final ConflictType type;

    public ConflictData(Data data, ConflictType type){
        super(data.getId(), data.getUsername(), data.getEmailAddress(), data.getPassword(), data.getService(), data.getAdditionalData());
        this.type = type;
    }

    public ConflictType getType() {
        return type;
    }

    public enum ConflictType {
        NEW_REMOTE,
        MODIFIED,
        DELETED_LOCAL,
        DELETED_REMOTE
    }
}
