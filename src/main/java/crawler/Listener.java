package crawler;

import org.slf4j.Logger;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Listener {

    private final WebCrawlerFrame crawler;
    private final Set<UrlAndData> urlsDataCollected = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Set<UrlAndData> previousData = new HashSet<>();
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

        noOfWorkers = getNumberOfWorkers();
    }

    public void startCrawling() {
        crawler.getRunButton().setEnabled(false);
        String firstUrl = crawler.getUrlTextField().getText().toLowerCase();
        urlsDataCollected.clear();
        crawlFirstUrl(firstUrl);
        System.out.println("And " + urlsDataCollected.size() + " url's Data collected");
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

    private int getNumberOfWorkers() {
        String workerValue = crawler.getWorkersTextField().getText();

        try {
            return Integer.parseInt(workerValue);
        } catch (NumberFormatException e) {
            logger.info("Setting number of workers to default of 2");
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
        crawlOnwards(data.getUrls(), currentDepth);
    }

    private UrlAndData process(String url) throws IOException {
        System.out.println("Processing url = " + url);
        String htmlText = getHtml(url);
        String title = getTitle(htmlText);
        Set<String> urls = collectUrls(htmlText);
        return new UrlAndData(url, title, urls);
    }

    private String getHtml(String url) throws IOException {
            InputStream inputStream = new BufferedInputStream(new URL(url).openStream());
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }

    private String getTitle(String html) {
        Pattern pattern = Pattern.compile("<title>(.+)</title>");
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return "NO TITLE FOUND";
        }
    }
    private Set<String> collectUrls(String html) {
        Set<String> urls = new HashSet<>();
        Pattern pattern = Pattern.compile("(?i)<a\\s+(?:[^>].*)?href=(['\"])(.*\\..*?)\\1");
        Matcher matcher = pattern.matcher(html);
        while (matcher.find()) {
            String url = matcher.group(2);
            if (!url.startsWith("http")) {
                url = "https:" + url;
            }
            urls.add(url);
        }
        return urls;
    }

    private void crawlOnwards(Set<String> urlCollection, int currentDepth) {
        if (depthExceedLimit(++currentDepth) || elapsedTimeExceedLimit()) {
            return;
        }

        ExecutorService executor = Executors.newFixedThreadPool(noOfWorkers);

        for (String url : urlCollection) {
            executor.execute(() -> {
                try {
                    urlsDataCollected.add(process(url));
                } catch (IOException e) {
                    System.out.println("Not able to process " + url);
                }
            });
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(getTimeLimit(), TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }

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
