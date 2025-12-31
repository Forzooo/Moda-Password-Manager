package moda.passwordmanager.frontend.panels;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UserDataField extends JPanel {

    private JTextField dataField;
    private JButton copyButton;

    public UserDataField(String dataField){
        initPanel();
        initComponents(dataField);
        initListeners();
    }

    /**
     * Set the configuration of the Panel
     */
    private void initPanel(){
        setLayout(new FlowLayout());
    }

    /**
     * Initialize the components of the panel
     */
    private void initComponents(String dataField){
        this.dataField = new JTextField();
        this.dataField.setText(dataField);
        this.dataField.setEditable(false);  // The user cannot modify the data unless "Modify" is clicked
        this.dataField.setPreferredSize(new Dimension(600, 70));
        this.dataField.setMaximumSize(new Dimension(600, 70));

        // Create the JButton to copy the data field
        this.copyButton = new JButton();
        this.copyButton.setText("❏");
        this.copyButton.setToolTipText("Copy");
        this.copyButton.setPreferredSize(new Dimension(50, 70));
        this.copyButton.setMaximumSize(new Dimension(50, 70));

        add(this.dataField);
        add(this.copyButton);
    }

    /**
     * Initialize all the listeners on the components
     */
    private void initListeners(){
        this.copyButton.addActionListener(getCopyDataActionListener());
    }

    /**
     * Create an Action Listener to copy the data used by the buttons
     * @return ActionListener with actionPerformed method
     */
    private ActionListener getCopyDataActionListener(){
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Get the clipboard from the system
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection dataToCopy = new StringSelection(dataField.getText());  // Create a Transferable
                clipboard.setContents(dataToCopy, dataToCopy);  // Copy the transferable
                JOptionPane.showMessageDialog(getRootPane(), "Copied the data to the clipboard.");
            }
        };
    }

    /**
     * Update the clipboard action with the modified data
     */
    private void updateClipboardCopy(){
        // Remove the old action listener, which is always at index 0, and create a new one with the updated data
        this.copyButton.removeActionListener(this.copyButton.getActionListeners()[0]);
        this.copyButton.addActionListener(getCopyDataActionListener());
    }

    /**
     * Set the data to be editable
     */
    public void enableEditing(){
        this.dataField.setEditable(true);
        this.copyButton.setEnabled(false);  // Copying is not allowed in editing mode
        this.copyButton.setVisible(false);
    }

    /**
     * Set the data to be read-only
     */
    public void disableEditing(){
        this.dataField.setEditable(false);
        updateClipboardCopy();  // We assume that when the editing is finished the data has been changed
        this.copyButton.setEnabled(true);  // Enable again the copy button
        this.copyButton.setVisible(true);
    }

    /**
     * Get the data inside the TextField
     */
    public String getData(){
        return this.dataField.getText();
    }

    /**
     * Set the text inside the TextField
     */
    protected void setText(String text){
        this.dataField.setText(text);
    }

}
