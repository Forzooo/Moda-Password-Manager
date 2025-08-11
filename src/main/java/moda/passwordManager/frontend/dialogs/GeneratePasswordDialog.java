package moda.passwordManager.frontend.dialogs;

import moda.passwordManager.frontend.components.Placeholder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A JDialog used to retrieve the parameters of the generation of the password
 */
public class GeneratePasswordDialog extends JDialog {

    private JTextField passwordLengthTextField;
    private Placeholder passwordLengthPlaceholder;

    private JCheckBox lettersCheckBox;
    private JCheckBox numbersCheckBox;
    private JCheckBox specialCharactersCheckBox;

    private JButton saveConfigurationButton;

    public GeneratePasswordDialog(){
        super();
        initDialog();
        initComponents();
        initActionListener();
    }

    /**
     * Get the layout used for the Dialog
     *
     * @return BoxLayout
     */
    private FlowLayout getDialogLayout() {
        return new FlowLayout();  // TODO: Set a proper layout
    }

    /**
     * Set the configuration of the Dialog
     */
    private void initDialog(){
        setLayout(getDialogLayout());  // Set its layout

        setTitle("Configuration of the password");

        // Set the preferred size
        setSize(new Dimension(700, 700));  // TODO: Set a proper dimension

        setBackground(Color.WHITE);
    }

    /**
     * Initialize and add all the Swing components of the Dialog
     */
    private void initComponents(){
        this.passwordLengthTextField = new JTextField();
        this.passwordLengthPlaceholder = new Placeholder(this.passwordLengthTextField, "Enter the password length");
        this.passwordLengthPlaceholder.show();

        this.lettersCheckBox = new JCheckBox();
        this.lettersCheckBox.setText("Include letters (a-z, A-Z)");

        this.numbersCheckBox = new JCheckBox();
        this.numbersCheckBox.setText("Include numbers (0-9)");

        this.specialCharactersCheckBox = new JCheckBox();
        this.specialCharactersCheckBox.setText("Include special characters (!?.,)");

        this.saveConfigurationButton = new JButton();
        this.saveConfigurationButton.setText("Save configuration");

        add(this.passwordLengthTextField);
        add(this.lettersCheckBox);
        add(this.numbersCheckBox);
        add(this.specialCharactersCheckBox);
        add(this.saveConfigurationButton);
    }

    /**
     * Initialize all the Action Listeners of the components
     */
    private void initActionListener(){
        // TODO: Add check on the user input to allow only numbers
        this.passwordLengthTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });

        this.saveConfigurationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO: Add Event to send to the Backend to save the configuration

            }
        });
    }
}
