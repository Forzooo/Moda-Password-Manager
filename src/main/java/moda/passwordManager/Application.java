package moda.passwordManager;

import moda.passwordManager.backend.Backend;
import moda.passwordManager.communicationHandler.Event;
import moda.passwordManager.frontend.Frontend;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.LinkedBlockingQueue;


public class Application extends JFrame {

    public Application(LinkedBlockingQueue<Event> backendQueue, LinkedBlockingQueue<Event> frontendQueue,
                       LinkedBlockingQueue<Event> backendExceptionQueue, LinkedBlockingQueue<Event> frontendExceptionQueue){
        initUI(backendQueue, frontendQueue, backendExceptionQueue, frontendExceptionQueue);
    }

    private void initUI(LinkedBlockingQueue<Event> backendQueue, LinkedBlockingQueue<Event> frontendQueue,
                        LinkedBlockingQueue<Event> backendExceptionQueue, LinkedBlockingQueue<Event> frontendExceptionQueue){
        Dimension screen = getToolkit().getScreenSize();

        int width = (int) (screen.getWidth() * 4/5);
        int height = (int) (screen.getHeight() * 4/5);

        add(new Frontend(backendQueue, frontendQueue, backendExceptionQueue, frontendExceptionQueue, width, height));
        pack();

        setTitle("MODA - Password Manager");
        setSize(width, height);

        setIconImage(Frontend.getIcon());  // Get the icon and set it

        setVisible(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        // Create the two LinkedBlockingQueue objects here to pass them to the Backend and the Frontend
        LinkedBlockingQueue<Event> backendQueue = new LinkedBlockingQueue<>();
        LinkedBlockingQueue<Event> frontendQueue = new LinkedBlockingQueue<>();

        // The queues that communicate exceptions
        LinkedBlockingQueue<Event> backendExceptionQueue = new LinkedBlockingQueue<>();
        LinkedBlockingQueue<Event> frontendExceptionQueue = new LinkedBlockingQueue<>();

        EventQueue.invokeLater(() -> {
            Application ex = new Application(backendQueue, frontendQueue, backendExceptionQueue, frontendExceptionQueue);
            ex.setVisible(true);
        });

        Backend backend = new Backend(backendQueue, frontendQueue, backendExceptionQueue, frontendExceptionQueue);
        // Set the handler from the object itself otherwise it would use the one from the Frontend
        backend.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                backend.uncaughtException(t, e);
            }
        });
        backend.start();
    }
}