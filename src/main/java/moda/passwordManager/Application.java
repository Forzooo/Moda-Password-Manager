package moda.passwordManager;

import moda.passwordManager.backend.Backend;
import moda.passwordManager.communicationHandler.Event;
import moda.passwordManager.frontend.Frontend;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.LinkedBlockingQueue;


public class Application extends JFrame {

    public Application(LinkedBlockingQueue<Event> backendQueue, LinkedBlockingQueue<Event> frontendQueue){
        initUI(backendQueue, frontendQueue);
    }

    private void initUI(LinkedBlockingQueue<Event> backendQueue, LinkedBlockingQueue<Event> frontendQueue){
        Dimension screen = getToolkit().getScreenSize();

        int width = (int) (screen.getWidth() * 3/4);
        int height = (int) (screen.getHeight() * 3/4);

        add(new Frontend(backendQueue, frontendQueue, width, height));
        pack();

        setTitle("MODA - Password Manager");
        setSize(width, height);
        setVisible(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        // Create the two LinkedBlockingQueue objects here to pass them to the Backend and the Frontend
        LinkedBlockingQueue<Event> backendQueue = new LinkedBlockingQueue<>();
        LinkedBlockingQueue<Event> frontendQueue = new LinkedBlockingQueue<>();

        EventQueue.invokeLater(() -> {
            Application ex = new Application(backendQueue, frontendQueue);
            ex.setVisible(true);
        });

        Backend backend = new Backend(backendQueue, frontendQueue);
        backend.start();
    }
}