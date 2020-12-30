package crawler;

import org.slf4j.Logger;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.*;

// Controller
public class Listener {

    private final Set<UrlAndData> urlsDataCollected = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Logger logger = MyLogger.getLogger();
    private static final Long DEFAULT_TIME_LIMIT_SECONDS = 150L;
    private static final int DEFAULT_DEPTH = 2;
    private static final int DEFAULT_NO_OF_WORKERS = 10;
    private int noOfWorkers;
    private int prescribedDepth;
    private boolean isTimeLimited = false;
    private boolean isDepthLimited = false;
    private long timeLimit;
    private final WebCrawlerFrame window;
    private long startTime;

    public Listener(WebCrawlerFrame window) {
        this.window = window;
    }

    public void start() {

        isTimeLimited = window.getTimeLimitCheckBox().isSelected();
        setupActionListeners();


//        if (window.getDepthCheckBox().isSelected()) {
//            isDepthLimited = true;
//            prescribedDepth = getDepth(window.getDepthField().getText());
//        }
//
//        noOfWorkers = getNumberOfWorkers(window.getWorkersField().getText());
//
//        String inputUrl = window.getUrlField().getText().toLowerCase();
//        urlsDataCollected.clear();
//        System.out.println("inputUrl = " + inputUrl);
//        System.out.println("noOfWorkers = " + noOfWorkers);
//        System.out.println("prescribedDepth = " + prescribedDepth);
//        System.out.println("isTimeLimited = " + isTimeLimited);
//        System.out.println("isDepthLimited = " + isDepthLimited);
//        crawlFirstUrl(inputUrl);
//        System.out.println("And " + urlsDataCollected.size() + " url's Data collected");
//        printResultsToFile();
    }

    private void setupActionListeners() {
        window.getRunButton().addActionListener(e -> {
            startTime = System.currentTimeMillis();
            timeLimit = getTimeLimit();
            final int oneSecondDelay = 1000;
            Timer timer = new Timer(oneSecondDelay, this::timerThreadExecute);
            timer.setInitialDelay(100);
            timer.start();
        });
    }

    private long getTimeLimit() {
        String timeLimitInput = window.getTimeLimitField().getText();
        Long timeLimit = null;
        if (!timeLimitInput.equals("")) {
            timeLimit = parseInputToLong(timeLimitInput);
        }
        if (timeLimit == null || !window.getTimeLimitCheckBox().isSelected()) {
            timeLimit = DEFAULT_TIME_LIMIT_SECONDS;
        }
        return timeLimit;
    }

    private Long parseInputToLong(String timeLimitInput) {
        Long timeLimitSeconds = null;
        try {
            timeLimitSeconds = Long.parseLong(timeLimitInput);
        } catch (NumberFormatException ignored) {
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
        } catch (Exception e) {
            System.out.println("The inputted Url is not correct");
            return;
        }
        urlsDataCollected.add(data);
        crawlOnwards(data.getUrls(), 1);
    }

    private UrlAndData process(String url) throws WebCrawlerException {
        UrlProcessingService service = new UrlProcessingService();
        String htmlText = service.getHtml(url);
        String title = service.getTitle(htmlText);
        Set<String> urls = service.collectUrls(htmlText);
        System.out.println("url: " + url + " title: " + title);
//        System.out.println("urls.size() = " + urls.size());
        return new UrlAndData(url, title, urls);
    }

    private void crawlOnwards(Set<String> urlCollection, int currentDepth) {
        if (elapsedTimeExceedLimit()) {
            return;
        }
        if (isDepthLimited && prescribedDepth < currentDepth) {
            return;
        }
        ExecutorService executor = Executors.newFixedThreadPool(noOfWorkers);
        final Set<String> currentLevelUrlCollection = Collections.newSetFromMap(new ConcurrentHashMap<>());
        for (String url : urlCollection) {
            executor.execute(() -> {
                try {
                    UrlAndData data = process(url);
                    urlsDataCollected.add(data);
                    currentLevelUrlCollection.addAll(data.getUrls());
                } catch (WebCrawlerException e) {
                    System.out.println("Not able to process " + url);
                }
            });
        }
        executor.shutdown();
        try {
            boolean isFinished = executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException ignored) {
            return;
        }
        System.out.println(currentLevelUrlCollection.size() + " urls collected at level " + currentDepth);
        crawlOnwards(currentLevelUrlCollection, ++currentDepth);
    }

    private boolean elapsedTimeExceedLimit() {
        return false;
    }

    public void timerThreadExecute(ActionEvent ae) {
        SwingWorker<String, Void> sWorker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                long millisecondsElapsed = System.currentTimeMillis() - startTime;
                long elapsedSeconds = millisecondsElapsed / 1000;
                System.out.println("elapsedSeconds = " + elapsedSeconds);
                System.out.println("timeLimit = " + timeLimit);
                if (elapsedSeconds >= timeLimit) {
                    System.out.println("Listener.doInBackground Inside If");
                    Timer t = (Timer) ae.getSource();
                    t.stop();
                }
                long second = elapsedSeconds % 60;
                long minute = (elapsedSeconds / 60) % 60;
                return String.format("%02d:%02d", minute, second);
            }

            @Override
            protected void done() {
                try {
                    window.getElapsedTimeDisplay().setText(get());
                } catch (InterruptedException | ExecutionException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }
        };
        sWorker.execute();
    }
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

