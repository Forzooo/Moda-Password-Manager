package moda.passwordmanager.frontend;

import moda.passwordmanager.Application;
import moda.passwordmanager.frontend.dialogs.Startup;
import moda.passwordmanager.frontend.properties.Languages;
import moda.passwordmanager.frontend.properties.Themes;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;
import moda.passwordmanager.interthreadcommunication.Event;
import moda.passwordmanager.frontend.panels.*;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.LinkedBlockingQueue;

public class Frontend extends JPanel {

    // The InterThreadCommunication objects used to communicate with the Backend thread
    private final InterThreadCommunication ITC;

    // The Frontend Event Listener used to receive and handle requests from the backend
    private FrontendEventListener eventListener;

    // The panel that contains the one being shown in the frontend and is used to retrieve its CardLayout to switch
    // between panels, which have to implement the PanelTitle interface
    private JPanel frontendPanel;

    private ShowData showDataPanel;

    public Frontend(LinkedBlockingQueue<Event> backendQueue, LinkedBlockingQueue<Event> frontendQueue,
                    String databasePath){

        // Start the communication between the backend and the frontend
        this.ITC = new InterThreadCommunication(backendQueue, frontendQueue);

        setInitialDatabase(databasePath);  // Set, if not empty, the database to use

        // Set the default exception handler for the Frontend threads
        Thread.setDefaultUncaughtExceptionHandler(this::exceptionHandler);

        Application.setApplicationLanguage(getApplicationLanguage());
        Application.applyTheme(getApplicationTheme());
        initStartup();

        initPanel();  // Set the properties of the panel
        initComponents();  // Initialize all the JPanels
        initEventListener();  // Initialize the Event Listener only after all the frontend components have been init

    }

    /**
     * Handle the unhandled exception in the frontend by showing a messagebox about it
     * @param e The exception that has occurred
     */
    private void exceptionHandler(Thread t, Throwable e){
        String stackTrace = Utilities.getStackTrace(e);  // Get the full stack trace of the throwable
        // Show the exception as a Message Dialog with the type of error message
        JOptionPane.showMessageDialog(this, Utilities.getStackTraceRows(stackTrace, 5),
                Utilities.getLocaleString("Moda.Frontend.exceptionHandlerTitle") + " " + t.getName(), JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Set the initial database to use
     * @param databasePath The path of the database to use
     */
    private void setInitialDatabase(String databasePath){
        // If the path is empty, the user has not chosen a database to open at the execution of the application
        if (databasePath.isEmpty()){
            return;
        }
        Event event = new Event("set-database", databasePath);
        this.ITC.request(event);
    }

    /**
     * Get the theme of the application from the settings
     */
    private Themes getApplicationTheme(){
        Event request = new Event("get-application-theme");
        Event response = this.ITC.request(request);

        return (Themes) response.getData().getFirst();
    }

    /**
     * Get the language of the application from the settings
     */
    private Languages getApplicationLanguage(){
        Event request = new Event("get-application-language");
        Event response = this.ITC.request(request);

        return (Languages) response.getData().getFirst();
    }

    /**
     * Ask the user for the master password before starting to use the password manager
     */
    private void initStartup(){
        Startup startup = new Startup(this.ITC);
        startup.setVisible(true);
    }

    /**
     * Initialize the panel
     */
    private void initPanel(){
        setFocusable(true);  // Set the focus on the frame to get the keyboard inputs
        setLayout(new MigLayout("insets 0 0 0 0, fill", "[]0[]"));  // The gap between cols must be 0
    }

    /**
     * Initialize the Frontend event listener
     */
    private void initEventListener(){
        this.eventListener = new FrontendEventListener(this.ITC, this.showDataPanel);
        this.eventListener.start();
    }

    /**
     * Initialize the components
     */
    private void initComponents(){
        Sidebar sidebar = new Sidebar(this.ITC);

        this.frontendPanel = new JPanel();
        this.frontendPanel.setLayout(new CardLayout());

        this.showDataPanel = new ShowData(this.ITC);
        this.frontendPanel.add(this.showDataPanel, ShowData.getPanelTitle());
        this.frontendPanel.add(new AddData(this.ITC), AddData.getPanelTitle());

        add(sidebar, "grow");
        add(this.frontendPanel, "span, grow, push");
    }

    /**
     * Switch to another of the panels of the frontend
     * @param panelTitle The title of the panel, which is provided by the PanelTitle interface
     */
    public void switchPanel(String panelTitle){
        CardLayout cardLayout = (CardLayout) this.frontendPanel.getLayout();
        cardLayout.show(this.frontendPanel, panelTitle);
    }

}
