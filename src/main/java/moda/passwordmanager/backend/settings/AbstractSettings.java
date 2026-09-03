package moda.passwordmanager.backend.settings;

import java.util.ArrayList;

/**
 * The Abstract Settings class defines the methods that each Setting class must have
 */
public abstract class AbstractSettings {
    public abstract String readStringProperty(String propertyPath);
    public abstract int readIntProperty(String propertyPath);
    public abstract boolean readBooleanProperty(String propertyPath);
    public abstract ArrayList readListProperty(String propertyPath);
    public abstract void writeProperty(String propertyPath, Object value);
    public abstract void writeListProperty(String propertyPath, ArrayList values);
}