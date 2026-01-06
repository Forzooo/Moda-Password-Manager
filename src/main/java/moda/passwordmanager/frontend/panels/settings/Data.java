package moda.passwordmanager.frontend.panels.settings;

import moda.passwordmanager.frontend.dialogs.ConfigurePasswordGeneration;
import moda.passwordmanager.frontend.dialogs.Startup;
import moda.passwordmanager.interthreadcommunication.Event;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Data extends Section {

    private JButton changeMasterPasswordButton;  // Change the master password of the current database
    private JButton configureStringGenerationButton;

    public Data(InterThreadCommunication itc){
        super("Data", itc);

        initComponents();
        initListeners();
    }

    @Override
    public JPanel getPanel() {
        return this;
    }

    private void initComponents(){
        Dimension buttonDimension = new Dimension(250, 20);  // Define the dimension of any JButton

        this.changeMasterPasswordButton = new JButton();
        this.changeMasterPasswordButton.setText("Change the master password");
        this.changeMasterPasswordButton.setMaximumSize(buttonDimension);

        this.configureStringGenerationButton = new JButton();
        this.configureStringGenerationButton.setText("Configure the string generation");
        this.configureStringGenerationButton.setMaximumSize(buttonDimension);

        addOption(this.changeMasterPasswordButton);

        addSection("String generation");
        addOption(this.configureStringGenerationButton);
    }

    private void initListeners(){
        this.changeMasterPasswordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Set the master password of the database before using it
                String masterPassword = JOptionPane.showInputDialog(getRootPane(), "Enter the new master password",
                        "");

                // Initial checks on the master password entered to ensure that it is a valid string, otherwise abort the
                // operation
                if (masterPassword == null || masterPassword.isBlank()){
                    return;
                }

                changeMasterPassword(masterPassword);
            }
        });

        this.configureStringGenerationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ConfigurePasswordGeneration configurePasswordGeneration = new ConfigurePasswordGeneration(getITC());
                configurePasswordGeneration.setVisible(true);
            }
        });
    }

    /**
     * Change the current master password of the database to a new one
     * @param masterPassword The new master password
     */
    private void changeMasterPassword(String masterPassword){
        // Perform some initial conditions check on the master password
        if (!Startup.checkMasterPassword(masterPassword.toCharArray())){
            return;
        }

        Event event = new Event("change-master-password", masterPassword.toCharArray());
        getITC().request(event);  // Wait for the end of the operations in the backend
    }

}
