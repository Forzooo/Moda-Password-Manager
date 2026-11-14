package moda.passwordmanager.backend.settings;

import java.util.List;

/**
 * The Abstract Settings class defines the methods that each Setting class must have
 */
public abstract class AbstractSettings {
    public abstract String readStringProperty(String propertyPath);
    public abstract int readIntProperty(String propertyPath);
    public abstract boolean readBooleanProperty(String propertyPath);
    public abstract List<Object> readListProperty(String propertyPath);
    public abstract void writeProperty(String property, Object value);
    public abstract void writeSubproperty(String propertyPath, Object value);
    public abstract void writeListProperty(String property, List<Object> values);
    public abstract void writeListSubproperty(String propertyPath, List<Object> values);
}