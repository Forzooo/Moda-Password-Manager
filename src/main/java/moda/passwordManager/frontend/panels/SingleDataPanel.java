package moda.passwordManager.frontend.panels;

import moda.passwordManager.backend.Data;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class SingleDataPanel extends JPanel {

    private Data data;
    private List<JTextField> dataFields;

    // Swing Components
    private JButton closeButton;
    private JButton modifyButton;
    private JButton deleteButton;

    public SingleDataPanel(Data data) {
        super();  // Initialize the Panel

        this.data = data;

        initPanel();
        initComponents();
    }

    /**
     * Set the configuration of the JPanel
     */
    private void initPanel(){
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));  // Set its layout
        setBorder(new EmptyBorder(20,20,20,20));
    }

    /**
     * Initialize the components of the panel
     */
    private void initComponents(){
        // Create all the JButton
        this.modifyButton = new JButton("Modify");
        this.deleteButton = new JButton("Delete");

        // Add dynamically all the data fields
        this.dataFields = new ArrayList<>();  // All the data fields are stored here

        for (String dataField : this.data.getFullUserData()){
            // Create the JTextField for each field
            JTextField dataTextField = new JTextField();
            dataTextField.setText(dataField);
            dataTextField.setEditable(false);  // The user cannot modify the data unless "Modify" is clicked
            dataFields.add(dataTextField);

            // Create the JButton to copy the data field
            JButton copyDataButton = new JButton();
            copyDataButton.setText("❏");
            copyDataButton.setPreferredSize(new Dimension(50, 50));
            copyDataButton.setMaximumSize(new Dimension(50, 50));
            copyDataButton.setMinimumSize(new Dimension(50, 50));
            copyDataButton.addActionListener(getCopyDataActionListener(this, dataField));

            // Add the JTextField and the JButton to the panel
            JPanel rowPanel = new JPanel(new BorderLayout(5, 0));
            rowPanel.setBorder(new EmptyBorder(5, 0, 5, 0));  // Padding between rows
            rowPanel.add(dataTextField, BorderLayout.CENTER);
            rowPanel.add(copyDataButton, BorderLayout.EAST);
            add(rowPanel);
        }

        JPanel buttons = new JPanel();
        buttons.add(this.modifyButton);
        buttons.add(this.deleteButton);

        add(buttons);
    }

    /**
     * Create an Action Listener to copy a dataField
     * @param dataField
     * @return ActionListener with actionPerformed method
     */
    private ActionListener getCopyDataActionListener(JPanel panel, String dataField){
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Get the clipboard from the system
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection dataToCopy = new StringSelection(dataField);  // Create a Transferable
                clipboard.setContents(dataToCopy, dataToCopy);  // Copy the transferable
                JOptionPane.showMessageDialog(panel, "Copied the data to the clipboard.");
            }
        };
    }
}
