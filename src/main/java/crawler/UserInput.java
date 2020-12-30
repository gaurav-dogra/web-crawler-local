package crawler;

import lombok.Getter;
import java.io.File;

@Getter
public class UserInput {
    private final String startUrl;
    private final int workers;
    private final int maxDepth;
    private final boolean isDepthCheckBoxSelected;
    private final int timeLimit;
    private final boolean iTimeLimitCheckBoxSelected;
    private final String exportFile;
    private static final int DEFAULT_NO_OF_WORKERS = 10;
    private static final int DEFAULT_MAX_DEPTH = 1;
    private static final int DEFAULT_MAX_TIME = 150;
    private static final String DEFAULT_FILE_URL = "resultFile";

    public UserInput(WebCrawlerFrame window) {
        startUrl = window.getUrlField().getText();
        workers = getWorkers(window);
        maxDepth = getMaxDepth(window);
        isDepthCheckBoxSelected = window.getDepthCheckBox().isSelected();
        timeLimit = getTimeLimit(window);
        iTimeLimitCheckBoxSelected = window.getTimeLimitCheckBox().isSelected();
        exportFile = getFile(window);
    }

    private int getWorkers(WebCrawlerFrame window) {
        String input = window.getWorkersField().getText();
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException nfe) {
            return DEFAULT_NO_OF_WORKERS;
        }
    }

    private int getMaxDepth(WebCrawlerFrame window) {
        String input = window.getDepthField().getText();
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException nfe) {
            return DEFAULT_MAX_DEPTH;
        }
    }

    private int getTimeLimit(WebCrawlerFrame window) {
        String input = window.getTimeLimitField().getText();
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException nfe) {
            return DEFAULT_MAX_TIME;
        }
    }

    private String getFile(WebCrawlerFrame window) {
        String input = window.getExportUrlField().getText();
        try {
            new File(input);
        } catch (NullPointerException e) {
            return DEFAULT_FILE_URL;
        }
        return input;
    }
}
