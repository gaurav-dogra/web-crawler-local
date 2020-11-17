package crawler;

import org.slf4j.Logger;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

public class Listener {

    private final WebCrawler crawler;
    private final Map<String, String> urlAndTitleCollection = new HashMap<>();
    private JTextField urlTextField;
    private JToggleButton runButton;
    private JTextField workersTextField;
    private JTextField depthTextField;
    private JCheckBox depthCheckBox;
    private JTextField timeLimitTextField;
    private JCheckBox timeLimitEnabledCB;
    private JLabel elapsedTimeActual;
    private JLabel parsedPagesActual;
    private JTextField exportUrlTextField;
    private JButton exportButton;

    private final Logger logger = MyLogger.getLogger();
    private int currentDepth;
    private int prescribedDepth;

    public Listener(WebCrawler crawler) {
        this.crawler = crawler;
        getFieldsFromUI();
    }

    private void getFieldsFromUI() {
        this.urlTextField = crawler.getUrlTextField();
        this.runButton = crawler.getRunButton();
        this.workersTextField = crawler.getWorkersTextField();
        this.depthTextField = crawler.getDepthTextField();
        this.depthCheckBox = crawler.getDepthCheckBox();
        this.timeLimitTextField = crawler.getTimeLimitTextField();
        this.timeLimitEnabledCB = crawler.getTimeLimitEnabledCB();
        this.elapsedTimeActual = crawler.getElapsedTimeActual();
        this.parsedPagesActual = crawler.getParsedPagesActual();
        this.exportUrlTextField = crawler.getExportUrlTextField();
        this.exportButton = crawler.getExportButton();
    }

    public void start() {
        long start = System.currentTimeMillis();
        String inputtedUrl = urlTextField.getText().toLowerCase();
        prescribedDepth = getPrescribedDepth();
        logger.info("Input url is " + inputtedUrl);
        System.out.println("input url is " + inputtedUrl);
        logger.info("prescribed depth is " + prescribedDepth);
        System.out.println("prescribed depth is " + prescribedDepth);
        System.out.println("max time is " + timeLimitTextField.getText());
        urlAndTitleCollection.clear();
        collectUrlsRecursivelyFrom(inputtedUrl);
        long end = System.currentTimeMillis();
        System.out.println("Time taken: " + (end - start));
        printResultsToFile();
    }

    private int getPrescribedDepth() {
        if (depthCheckBox.isSelected()) {
            try {
                return Integer.parseInt(depthTextField.getText());
            } catch (NumberFormatException e) {
                logger.info("prescribed depth is not given, so it is set to Max Integer Value");
            }
        }
        return Integer.MAX_VALUE;
    }

    private void collectUrlsRecursivelyFrom(String inputtedUrl) {
        ConcurrentLinkedQueue<String> initialUrl = new ConcurrentLinkedQueue<>();
        initialUrl.add(inputtedUrl);
        collectUrlsRecursivelyFrom(initialUrl);
    }

    private void collectUrlsRecursivelyFrom(ConcurrentLinkedQueue<String> passedUrls) {
        int workers = Integer.parseInt(workersTextField.getText());
        ExecutorService executor = Executors.newFixedThreadPool(workers);

        logger.info("current depth: {}", currentDepth);
        System.out.println("currentDepth = " + currentDepth);

        if (exceedingPrescribedDepth()) {
            return;
        }

        Set<String> currentLevelUrls = new HashSet<>();
        List<Future> allFutures = new ArrayList<>();

        for (String url : passedUrls) {
            logger.info("Collecting urls from url {} at level {}", url, currentDepth);
            System.out.printf("Collecting urls from url %s at level %s\n", url, currentDepth);
            ProcessUrl processor = new ProcessUrl(url);
            allFutures.add(executor.submit(processor));
        }

        executor.shutdown();
        System.out.println("All tasks submitted");

        try {
            String timeOutLimitInSeconds = timeLimitTextField.getText();
            if (!timeOutLimitInSeconds.equals("") && timeLimitEnabledCB.isEnabled()) {
                int timeOutLimit = Integer.parseInt(timeOutLimitInSeconds);
                executor.awaitTermination(timeOutLimit, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (currentLevelUrls.size() > 0) {
            collectUrlsRecursivelyFrom(currentLevelUrls);
        }
    }

    private void printResultsToFile() {
        String fileName = exportUrlTextField.getText();
        if (fileName.equals("")) {
            System.out.println("No file name given, so saving to output.txt");
            fileName = "output.txt";
        }
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(fileName), StandardCharsets.UTF_8))) {
            for (String url : urlAndTitleCollection.keySet()) {
                writer.write(url);
                writer.newLine();
                writer.write(urlAndTitleCollection.get(url));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private boolean exceedingPrescribedDepth() {
        currentDepth++;
        return currentDepth > prescribedDepth;
    }

}
