package moda.passwordManager.frontend.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * The Placeholder class is used to show a placeholder in JTextField components.
 */
public class Placeholder implements FocusListener {

    private JTextField textField;  // The JTextField needs to be an attribute otherwise changes cannot be applied to it
    private final String TEXT;  // The text which is shown as a placeholder
    private boolean shown;  // A flag that indicates whether it's needed to show the placeholder or not

    public Placeholder(JTextField textField, String text){
        this.textField = textField;
        this.TEXT = text;
        this.shown = true;

        this.textField.setText(text);
        this.textField.setForeground(Color.GRAY);
        this.textField.addFocusListener(this);
    }

    // If the user clicks on the TextField then remove the placeholder
    @Override
    public void focusGained(FocusEvent e) {
        if (this.shown){
            hide();
        }
    }

    // If the user clicks outside the TextField and the text entered is empty then show again the placeholder
    @Override
    public void focusLost(FocusEvent e) {
        // Show again the TextField only if the text inside it is empty
        if (this.textField.getText().isEmpty()){
            show();
        }
    }

    // Show the placeholder
    public void show(){
        this.textField.setText(this.TEXT);
        this.textField.setForeground(Color.GRAY);

        this.shown = true;
    }

    // Hide the placeholder
    public void hide(){
        this.textField.setText("");
        this.textField.setForeground(Color.BLACK);

        this.shown = false;
    }

    public boolean isShown() {
        return shown;
    }
}
