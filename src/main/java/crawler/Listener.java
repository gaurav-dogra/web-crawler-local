package crawler;

import org.slf4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Listener {

    private final WebCrawlerFrame crawler;
//    private final List<UrlData> results = Collections.synchronizedList(new ArrayList<>());
    private final List<UrlData> results = new ArrayList<>();
    private MyTimer timer;
    private final Logger logger = MyLogger.getLogger();
    private static final int DEFAULT_TIME_LIMIT_SECONDS = 90_000;
    private static final int DEFAULT_DEPTH = 2;
    private static final int DEFAULT_NO_OF_WORKERS = 2;
    private final int noOfWorkers;
    private int prescribedDepth;
    private boolean isTimeLimited = false;
    private boolean isDepthLimited = false;

    public Listener(WebCrawlerFrame crawler) {
        this.crawler = crawler;
        if (crawler.getTimeLimitCheckBox().isEnabled()) {
            isTimeLimited = true;
            startTimer(getTimeLimit());
        }

        if (crawler.getDepthCheckBox().isEnabled()) {
            isDepthLimited = true;
            prescribedDepth = getDepth();
        }

        String workerValue = crawler.getWorkersTextField().getText();
        noOfWorkers = workerValue.equals("") ? parseToInt(workerValue) : DEFAULT_NO_OF_WORKERS;
    }

    public void startCrawling() {
        crawler.getRunButton().setEnabled(false);
        String firstUrl = crawler.getUrlTextField().getText().toLowerCase();


        results.clear();
        crawlFirstUrl(firstUrl);
        System.out.println("And " + results.size() + " url's Data collected");
//        printResultsToFile();
    }

    private void startTimer(Long timeLimitSeconds) {
        timer = new MyTimer(crawler.getElapsedTimeLabel(), timeLimitSeconds);
        timer.execute();
    }

    private Long getTimeLimit() {
        long timeLimitSeconds;
        try {
            timeLimitSeconds = Long.parseLong(crawler.getTimeLimitTextField().getText());
        } catch (NumberFormatException e) {
            timeLimitSeconds = DEFAULT_TIME_LIMIT_SECONDS;
        }
        return timeLimitSeconds;
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

    private int parseToInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.info("Setting number of workers to default of 2");
        }
        return DEFAULT_NO_OF_WORKERS;
    }

    private void crawlFirstUrl(String inputUrl) {

        UrlData data;
        try {
            data = UrlProcessingService.getData(inputUrl);
        } catch (IOException e) {
            logger.info("Unable to process url: {}", inputUrl);
            return;
        }
        results.add(data);

        int currentDepth = 0;
        crawlOnwards(data.getUrls(), currentDepth);
    }

    private void crawlOnwards(Set<String> urlCollection, int currentDepth) {
        if (!depthAndTimeCheck(++currentDepth)) {
            return;
        }

        ExecutorService executor = Executors.newFixedThreadPool(noOfWorkers);
//        Set<String> currentLevelUrls = Collections.synchronizedSet(new HashSet<>());
        Set<String> currentLevelUrls = new HashSet<>();

        List<Future<UrlData>> futures = new ArrayList<>();
        for (String url : urlCollection) {
            futures.add(executor.submit(new UrlProcessingService(url)));
        }
        for (Future<UrlData> future : futures) {
            UrlData data;
            try {
                data = future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                continue;
            }
            currentLevelUrls.addAll(data.getUrls());
            results.add(data);
        }
        executor.shutdown();
        crawlOnwards(currentLevelUrls, currentDepth);
    }

    private boolean depthAndTimeCheck(int currentDepth) {
        if (isTimeLimited && timer.isDone()) {
            return false;
        }

        if (isDepthLimited && currentDepth > prescribedDepth) {
            return false;
        }
        return true;
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
