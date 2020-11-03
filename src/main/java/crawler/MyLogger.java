package crawler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyLogger {
    private static final Logger logger = LoggerFactory.getLogger(MyLogger.class);

    private MyLogger() {}

    public static Logger getLogger() {
        return logger;
    }
}
