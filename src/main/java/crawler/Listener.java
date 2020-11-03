package crawler;

import org.slf4j.Logger;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Listener {

    private final WebCrawler crawler;
    private final Map<String, String> results = new HashMap<>();
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

    private Logger logger = MyLogger.getLogger();
    private int currentDepth;
    private final List<String> masterUrlsList = new ArrayList<>();


    public Listener(WebCrawler crawler) {
        this.crawler = crawler;
        getFields();
    }

    private void getFields() {
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
        String firstUrl = urlTextField.getText().toLowerCase();
        logger.debug("original url is " + firstUrl);
        results.clear();
        List<String> firstLevelUrl = new ArrayList<>();
        firstLevelUrl.add(firstUrl);
        collectUrlsRecursively(firstLevelUrl);
        getTitlesFromUrls(masterUrlsList);
        printResultsToFile(results);
    }

    private void printResultsToFile(Map<String, String> results) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(exportUrlTextField.getText()), StandardCharsets.UTF_8))) {
            for (String url : results.keySet()) {
                writer.write(url);
                writer.newLine();
                writer.write(results.get(url));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void getTitlesFromUrls(List<String> urls) {
        for (String url : urls) {
            String title = getTitle(url);
            if (!title.equals("Not Found")) {
                results.put(url, title);
            }
        }
    }

    private void collectUrlsRecursively(List<String> passedUrls) {
        if (depthCheckBox.isSelected()) {
            currentDepth++;
            int maximumDepthAllowed = Integer.parseInt(depthTextField.getText());
            logger.debug("Maximum depth requested: {} AND current depth: {}", maximumDepthAllowed, currentDepth);
            if (currentDepth > maximumDepthAllowed) {
                return;
            }
        }

        List<String> currentLevelUrls = new ArrayList<>();
        for (String url : passedUrls) {
            logger.debug("Collecting urls from url {} at level {}", url, currentDepth);
            String htmlText = null;
            try (InputStream inputStream = new BufferedInputStream(new URL(url).openStream())) {
                htmlText = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                System.out.println("Listener.collectUrlsRecursively Exception");
                logger.debug(url + ": please check your input");
                System.out.println(e.getMessage());
            }
            if (htmlText != null) {
                List<String> urlsFromCurrentUrl = collectUrlsFromHtmlText(htmlText);
                logger.debug("Collected {} urls from url {}", urlsFromCurrentUrl.size(), url);
                currentLevelUrls.addAll(urlsFromCurrentUrl);
            }
        }
        masterUrlsList.addAll(currentLevelUrls);
        logger.debug("{} urls added to masterUrlList", currentLevelUrls.size());
        collectUrlsRecursively(currentLevelUrls);
    }

        private List<String> collectUrlsFromHtmlText (String htmlText){
            List<String> urlsOnPage = new ArrayList<>();
            Pattern pattern = Pattern.compile("(?i)<a\\s+(?:[^>].*)?href=(['\"])(.*\\..*?)\\1");
            Matcher matcher = pattern.matcher(htmlText);
            while (matcher.find()) {
                String url = matcher.group(2);
                if (!url.startsWith("http://") || url.startsWith("https://")) {
                    url = "https:" + url;
                }
                urlsOnPage.add(url);
            }
            return urlsOnPage;
        }

        private String getTitle (String htmlText){
            Pattern pattern = Pattern.compile("<title>(.+)</title>");
            Matcher matcher = pattern.matcher(htmlText);
            if (matcher.find()) {
                return matcher.group(1);
            } else {
                return "Not Found";
            }
        }

    }
