package moda.passwordManager.frontend.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

// TODO: Change from JTextField only to any Swing Component
/**
 * The Placeholder class is used to show a placeholder in Swing components.
 */
public class Placeholder implements FocusListener {

    private JTextField textField;  // The JTextField needs to be an attribute otherwise changes cannot be applied to it
    private final String TEXT;  // The text which is shown as a placeholder
    private boolean showPlaceholderFlag;  // A flag that indicates whether it's needed to show the placeholder or not

    public Placeholder(JTextField textField, String text){
        this.textField = textField;
        this.TEXT = text;
        this.showPlaceholderFlag = true;

        this.textField.setText(text);
        this.textField.setForeground(Color.GRAY);
        this.textField.addFocusListener(this);
    }

    // If the user clicks on the TextField then remove the placeholder
    @Override
    public void focusGained(FocusEvent e) {
        if (this.showPlaceholderFlag){
            hidePlaceholder();
        }
    }

    // If the user clicks outside the TextField and the text entered is empty then show again the placeholder
    @Override
    public void focusLost(FocusEvent e) {
        // Show again the TextField only if the text inside it is empty
        if (this.textField.getText().isEmpty()){
            showPlaceholder();
        }
    }

    // Show the placeholder
    public void showPlaceholder(){
        this.textField.setText(this.TEXT);
        this.textField.setForeground(Color.GRAY);

        this.showPlaceholderFlag = true;
    }

    // Hide the placeholder
    public void hidePlaceholder(){
        this.textField.setText("");
        this.textField.setForeground(Color.BLACK);

        this.showPlaceholderFlag = false;
    }

    public boolean isShowPlaceholderFlag() {
        return showPlaceholderFlag;
    }
}
