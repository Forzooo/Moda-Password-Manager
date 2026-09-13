package moda.passwordmanager.frontend.panels;

import moda.passwordmanager.backend.Data;
import moda.passwordmanager.frontend.Utilities;
import moda.passwordmanager.frontend.components.*;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.function.IntConsumer;

public class ShowData extends JPanel {

    // Attribute to communicate with the backend
    private final InterThreadCommunication itc;

    private JTabbedPane dataTabbedPane;  // The tabbed pane shows the dataList and the data the user has selected

    private ModaTextField searchBar;

    /**
     * The data shown in the JList, which are updated by the FrontendEventListener
     */
    private final DefaultListModel<Data> userDataModel;

    private final ArrayList<Data> userFilteredData;  // The data that is hidden from the JList when the search bar is used

    // Swing components
    private JList<Data> dataList;

    public ShowData(InterThreadCommunication itc){
        super();  // Initialize the Panel

        // Set the attributes given by the JFrame
        this.itc = itc;

        this.userDataModel = new DefaultListModel<>();
        this.userFilteredData = new ArrayList<>();

        initPanel();
        initComponents();
        initListeners();
    }

    /**
     * Set the configuration of the JPanel
     */
    private void initPanel(){
        // A generic boxlayout can be used as it's the only component of the panel
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    /**
     * Initialize the components of the panel
     */
    private void initComponents(){
        JPanel userDataPanel = new JPanel();  // In the userDataPanel the scrollPane and the search bar are added
        userDataPanel.setLayout(new MigLayout("insets 10 10 10 10, fill"));

        this.dataTabbedPane = new JTabbedPane();

        // Create the JList used to show all the data saved inside the database
        this.dataList = new JList<>();
        this.dataList.setModel(this.userDataModel);
        this.dataList.setFont(new Font(UIManager.getString("Moda.GeneralUseFontFamily"), Font.PLAIN, 20));
        this.dataList.setFixedCellHeight(30);
        this.dataList.setBackground(null);

        ModaScrollPane scrollPane = new ModaScrollPane(this.dataList);
        scrollPane.setBorder(new EmptyBorder(10,10,10,10));
        scrollPane.setBackground(Color.WHITE);

        int searchBarSize = 50;  // The size of the search bar
        this.searchBar = new ModaTextField(searchBarSize);

        JLabel searchIconLabel = new JLabel();
        ImageIcon searchIcon = Utilities.getIcon("search_icon.png");
        searchIcon.setImage(searchIcon.getImage().getScaledInstance(searchBarSize, searchBarSize, Image.SCALE_SMOOTH));
        searchIconLabel.setIcon(searchIcon);

        this.searchBar.putClientProperty("JTextField.trailingComponent", searchIconLabel);
        this.searchBar.putClientProperty("JTextField.placeholderText", Utilities.getLocaleString("Moda.ShowData.searchBarPlaceholder"));

        // Add the scroll pane and the search bar into the userDataPanel to make them both in the same tab of the dataTabbedPane
        userDataPanel.add(scrollPane, "grow, push, wrap");
        userDataPanel.add(this.searchBar, "span, growx, pushx");

        this.dataTabbedPane.addTab(getPanelTitle(), userDataPanel);
        add(this.dataTabbedPane);
    }

    /**
     * Initialize all the listeners on the components
     */
    private void initListeners(){
        this.dataList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                // Only allow double clicks or more
                if (e.getClickCount() >= 2) {
                    // We can retrieve the ID by getting it from the selected data
                    Data data = dataList.getSelectedValue();
                    int id = data.getId();

                    // If the tab exists, then set it to be the selected one instead of creating a new tab for it
                    if (checkDataTabExist(id)){
                        dataTabbedPane.setSelectedIndex(indexOfDataTab(id));
                    }else{  // Otherwise add the tab
                        addDataTab(id);
                    }
                }
            }
        });

        this.dataList.setCellRenderer(new DefaultListCellRenderer(){
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                // As we want to show only the service field we have to set the text of each cell to be it
                Data data = (Data) value;
                setText(data.getService());

                if (isSelected){  // The selected row has black background
                    setBackground(UIManager.getColor("Moda.ShowData.DataList.selectionBackground"));
                } else if (index % 2 == 0) {  // The other rows have two different colors based on the index
                    setBackground(UIManager.getColor("Moda.ShowData.DataList.evenBackground"));
                } else {
                    setBackground(UIManager.getColor("Moda.ShowData.DataList.oddBackground"));
                }

                return this;
            }
        });

        // Handles the search bar
        this.searchBar.getDocument().addDocumentListener(new DocumentListener(){
            @Override
            public void insertUpdate(DocumentEvent e){
                String searchText = searchBar.getText().toLowerCase().trim();  // Get the input of the user

                // Iterates over the data shown in the JList and filters, by moving the data into the userFilteredData
                // ArrayList, all the ones that don't match the search bar text
                for (int i = userDataModel.size()-1; i >= 0; i--){
                    Data data = userDataModel.get(i);
                    if (!data.getService().toLowerCase().trim().startsWith(searchText)){
                        userFilteredData.add(data);
                        userDataModel.removeElement(data);
                    }
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e){
                String searchText = searchBar.getText().toLowerCase().trim();  // Get the input of the user

                // Iterates over the data filtered and readds it if it now matches the search bar text
                for (int i = userFilteredData.size()-1; i >= 0; i--){
                    Data data = userFilteredData.get(i);
                    if (data.getService().toLowerCase().trim().startsWith(searchText)){
                        userDataModel.addElement(data);
                        userFilteredData.remove(data);
                    }
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e){
                // Not relevant to the search
            }
        });
    }

    /**
     * Returns the title of the panel
     */
    public static String getPanelTitle() {
        return Utilities.getLocaleString("Moda.ShowData.panelTitle");
    }

    /**
     * Check whether the service selected, thus its ID, has already a tab
     */
    private boolean checkDataTabExist(int id){
        // Iterate over all the UserData tabs and check if their ID is the same as the one given
        // (We can skip the first one as it is this class)
        for (int i = 1; i < this.dataTabbedPane.getTabCount(); i++){
            Component tab = this.dataTabbedPane.getComponentAt(i);
            if (tab.getClass() == UserData.class && ((UserData) tab).getId() == id){  // Ensure that the tab is a UserData one
                return true;
            }
        }
        return false;
    }

    /**
     * Add a tab to the TabbedPane with the service selected by the user
     */
    private void addDataTab(int id){
        UserData userDataTab = new UserData(this.itc, id);
        userDataTab.putClientProperty("JTabbedPane.tabClosable", true);  // Set only the tab to be closeable, not the
                                                                         // entire JTabbedPane
        userDataTab.putClientProperty("JTabbedPane.tabCloseCallback",
                (IntConsumer) tabIndex -> this.dataTabbedPane.remove(tabIndex));

        String tabName = userDataTab.getTitle();  // Get the tab name from the object itself
        this.dataTabbedPane.add(tabName, userDataTab);
        this.dataTabbedPane.setSelectedComponent(userDataTab);  // Set the tab to be shown to be the one created
    }

    public DefaultListModel<Data> getUserDataModel() {
        return this.userDataModel;
    }

    /**
     * Return the index of the UserData tab that has the same ID. -1 is returned if it does not exist
     */
    private int indexOfDataTab(int id){
        // Iterate over all the UserData tabs and check if their ID is the same as the one given
        for (int i = 0; i < this.dataTabbedPane.getTabCount(); i++){
            Component tab = this.dataTabbedPane.getComponentAt(i);
            if (tab.getClass() == UserData.class && ((UserData) tab).getId() == id){  // Ensure that the tab is a UserData one
                return i;
            }
        }
        return -1;
    }
}