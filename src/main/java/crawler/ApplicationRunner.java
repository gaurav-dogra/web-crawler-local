package crawler;

import org.slf4j.Logger;
import javax.swing.*;

public class ApplicationRunner {

    public static void main(String[] args) {
        Logger logger = MyLogger.getLogger();
        logger.info("Starting application.....");
        SwingUtilities.invokeLater(WebCrawler::new);
    }
}