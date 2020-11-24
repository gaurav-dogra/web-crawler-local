package crawler;

import org.slf4j.Logger;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

public class Listener {

    private final WebCrawlerFrame crawler;
    private final List<UrlData> results = new ArrayList<>();

    private final Logger logger = MyLogger.getLogger();
    private static final int DEFAULT_TIME_LIMIT_SECONDS = 100;
    private static final int DEFAULT_DEPTH = 2;

    public Listener(WebCrawlerFrame crawler) {
        this.crawler = crawler;
    }

    public void startCrawling() {
        long startTime = System.currentTimeMillis();
        crawler.getRunButton().setEnabled(false);
        String firstUrl = crawler.getUrlTextField().getText().toLowerCase();
        results.clear();
        crawl(firstUrl);
        long end = System.currentTimeMillis();
        System.out.println("Time taken: " + (end - startTime));
        System.out.println("And " + results.size() + " url's Data collected");
//        printResultsToFile();
    }

    private int getDepth() {
        if (crawler.getDepthCheckBox().isSelected()) {
            try {
                return Integer.parseInt(crawler.getDepthTextField().getText());
            } catch (NumberFormatException e) {
                logger.info("prescribed depth is not given, so setting it to {}", DEFAULT_DEPTH);
            }
        }
        return DEFAULT_DEPTH;
    }

    private int getMaxTime() {
        if (crawler.getTimeLimitCheckBox().isSelected()) {
            try {
                return Integer.parseInt(crawler.getTimeLimitTextField().getText());
            } catch (NumberFormatException e) {
                logger.info("Maximum time is not given, so setting it to {}", DEFAULT_TIME_LIMIT_SECONDS);
            }
        }
        return DEFAULT_TIME_LIMIT_SECONDS;
    }

    private int getNoOfThreads() {
        try {
            return Integer.parseInt(crawler.getWorkersTextField().getText());
        } catch (NumberFormatException e) {
            logger.info("Setting number of workers to default of 2");
        }
        return 2;
    }

    private void crawl(String inputUrl) {

        UrlData data = null;
        try {
            data = UrlProcessingService.getData(inputUrl);
        } catch (IOException e) {
            logger.info("Unable to process url: {}", inputUrl);
            return;
        }
        results.add(data);
        crawl(data.getUrls());
    }

    private void crawl(Set<String> passedUrls) {
        int workers = getNoOfThreads();
        int depth = getDepth();
        int maxTime = getMaxTime();

        ExecutorService executor = Executors.newFixedThreadPool(workers);
        Set<String> nextLevelUrls = new HashSet<>();

        logger.info("current depth: {}", currentDepth);
        System.out.println("currentDepth = " + currentDepth);

        if (exceedingPrescribedDepth()) {
            return;
        }

        List<Future<UrlData>> allFutures = new ArrayList<>();

        for (String url : passedUrls) {
            logger.info("Collecting urls from url {} at level {}", url, currentDepth);
            System.out.printf("Collecting urls from url %s at level %s\n", url, currentDepth);
            Future<UrlData> future = executor.submit(new UrlProcessingService(url));
            allFutures.add(future);
        }
        System.out.println("All tasks submitted");

        for (Future<UrlData> future : allFutures) {
            UrlData oneUrlData;
            try {
                oneUrlData = future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                continue;
            }
            if (oneUrlData != null) {
                System.out.println("from the future " + oneUrlData);
                results.add(oneUrlData);
                nextLevelUrls.addAll(oneUrlData.getUrls());
            }
        }
        currentDepth++;
        System.out.println(nextLevelUrls.size() + " urls at level " + currentDepth);
        crawl(nextLevelUrls);
    }

    private void printResultsToFile() {
        String fileName = exportUrlTextField.getText();
        if (fileName.equals("")) {
            System.out.println("No file name given, so saving to output.txt");
            fileName = "output.txt";
        }

        System.out.println("data size = " + results.size());
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(fileName), StandardCharsets.UTF_8))) {
            for (UrlData oneUrlData : results) {
                writer.write(oneUrlData.getUrl());
                writer.newLine();
                writer.write(oneUrlData.getTitle());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private boolean exceedingPrescribedDepth() {
        return currentDepth > depth;
    }

}
