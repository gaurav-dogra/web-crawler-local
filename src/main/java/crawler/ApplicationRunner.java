package crawler;

import org.slf4j.Logger;

import javax.swing.*;

public class ApplicationRunner {

    public static void main(String[] args) {
        Logger logger = MyLogger.getLogger();
        logger.info("Starting application.....");
        SwingUtilities.invokeLater(() -> {
            WebCrawlerFrame window = new WebCrawlerFrame();
            Listener controller = new Listener(window);
            controller.start();
        });

    }
}