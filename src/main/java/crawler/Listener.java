package crawler;

import org.slf4j.Logger;

import javax.swing.*;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Listener {

    private final Set<UrlAndData> urlsDataCollected = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Set<UrlAndData> previousData = new HashSet<>();
    private MyTimer timer;
    private final Logger logger = MyLogger.getLogger();
    private static final int DEFAULT_TIME_LIMIT_SECONDS = 90_000;
    private static final int DEFAULT_DEPTH = 2;
    private static final int DEFAULT_NO_OF_WORKERS = 10;
    private int noOfWorkers;
    private int prescribedDepth;
    private boolean isTimeLimited = false;
    private boolean isDepthLimited = false;

    public void startCrawling(WebCrawlerFrame crawler) {

        long startingTime = System.currentTimeMillis();
        crawler.getRunButton().setEnabled(false);

        if (crawler.getTimeLimitCheckBox().isEnabled()) {
            isTimeLimited = true;
            startTimer(crawler.getElapsedTimeLabel(), getTimeLimit(crawler.getTimeLimitTextField().getText()));
        }

        if (crawler.getDepthCheckBox().isEnabled()) {
            isDepthLimited = true;
            prescribedDepth = getDepth(crawler.getDepthTextField().getText());
        }

        noOfWorkers = getNumberOfWorkers(crawler.getWorkersTextField().getText());

        String inputUrl = crawler.getUrlTextField().getText().toLowerCase();
        urlsDataCollected.clear();
        crawlFirstUrl(inputUrl);
        System.out.println("And " + urlsDataCollected.size() + " url's Data collected");
        System.out.println("Time taken = " + (System.currentTimeMillis() - startingTime));
//        printResultsToFile();
    }

    private void startTimer(JLabel elapsedTimeLabel, long timeLimitSeconds) {
        timer = new MyTimer(elapsedTimeLabel, timeLimitSeconds);
        timer.execute();
    }

    private Long getTimeLimit(String userInputtedTimeLimit) {
        long timeLimitSeconds;
        try {
            timeLimitSeconds = Long.parseLong(userInputtedTimeLimit);
        } catch (NumberFormatException e) {
            timeLimitSeconds = DEFAULT_TIME_LIMIT_SECONDS;
        }
        return timeLimitSeconds;
    }

    private int getDepth(String depth) {
        try {
            return Integer.parseInt(depth);
        } catch (NumberFormatException e) {
            logger.info("prescribed depth is not given, so setting it to {}", DEFAULT_DEPTH);
        }
        return DEFAULT_DEPTH;
    }

    private int getNumberOfWorkers(String numberOfWorkers) {

        try {
            return Integer.parseInt(numberOfWorkers);
        } catch (NumberFormatException e) {
            logger.info("Setting number of workers to default of 10");
        }
        return DEFAULT_NO_OF_WORKERS;
    }

    private void crawlFirstUrl(String inputUrl) {
        UrlAndData data;
        try {
            data = process(inputUrl);
        } catch (IOException e) {
            System.out.println("The inputted Url is not correct");
            return;
        }
        urlsDataCollected.add(data);
        int currentDepth = 0;
        System.out.println(data.getUrls());
        System.out.println("Size() " + data.getUrls().size());
//        crawlOnwards(data.getUrls(), currentDepth);
    }

    private UrlAndData process(String url) throws IOException {
        UrlProcessingService service = new UrlProcessingService();
        String htmlText = service.getHtml(url);
        String title = service.getTitle(htmlText);
        Set<String> urls = service.collectUrls(htmlText);
        return new UrlAndData(url, title, urls);
    }

    private void crawlOnwards(Set<String> urlCollection, int currentDepth) {
        if (depthExceedLimit(++currentDepth) || elapsedTimeExceedLimit()) {
            return;
        }

        ExecutorService executor = Executors.newFixedThreadPool(noOfWorkers);
//        System.out.println("urlCollection = " + urlCollection);
        for (String url : urlCollection) {

            executor.execute(() -> {
                try {
                    urlsDataCollected.add(process(url));
                } catch (IOException | IllegalArgumentException e) {
                    System.out.println("Not able to process " + url);
                }
            });
        }
        executor.shutdown();

        Set<String> currentLevelUrls = getCurrentLevelUrls();
        previousData.addAll(urlsDataCollected);
        crawlOnwards(currentLevelUrls, currentDepth);
    }

    private Set<String> getCurrentLevelUrls() {
        Set<UrlAndData> currentLevelUrlsAndData = new HashSet<>(urlsDataCollected);
        currentLevelUrlsAndData.removeAll(previousData);
        Set<String> currentLevelUrls = new HashSet<>();
        for (UrlAndData data : currentLevelUrlsAndData) {
            currentLevelUrls.addAll(data.getUrls());
        }
        return currentLevelUrls;
    }

    private boolean elapsedTimeExceedLimit() {
        return isTimeLimited && timer.isDone();
    }

    private boolean depthExceedLimit(int currentDepth) {
        return isDepthLimited && currentDepth > prescribedDepth;
    }


//    private void printResultsToFile() {
//        String fileName = exportUrlTextField.getText();
//        if (fileName.equals("")) {
//            System.out.println("No file name given, so saving to output.txt");
//            fileName = "output.txt";
//        }
//
//        System.out.println("data size = " + results.size());
//        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
//                new FileOutputStream(fileName), StandardCharsets.UTF_8))) {
//            for (UrlData oneUrlData : results) {
//                writer.write(oneUrlData.getUrl());
//                writer.newLine();
//                writer.write(oneUrlData.getTitle());
//                writer.newLine();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }

}
