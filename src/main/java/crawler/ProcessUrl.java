package crawler;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProcessUrl implements Runnable {

    private final String url;

    private Map<String, String> urlsAndTitles;
    private Set<String> currentLevelUrls = new HashSet<>();

    public ProcessUrl(String url, Set<String> currentLevelUrls) {
        this.url = url;
        this.currentLevelUrls = currentLevelUrls;
    }

    @Override
    public void run() {
        processUrl();
    }

    private void processUrl() {
        String htmlText = getHtmlText();
        if (htmlText != null) {
            String title = getTitle(htmlText);
            if (title != null) {
                collectAllUrls(htmlText);
                synchronized (this) {
                    urlsAndTitles.put(url, title);
                }
            }
        }

    }

    private String getHtmlText() {
        InputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new URL(url).openStream());
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Could not reach " + url);
            return null;
        }
    }

    private String getTitle(String htmlText) {
        Pattern pattern = Pattern.compile("<title>(.+)</title>");
        Matcher matcher = pattern.matcher(htmlText);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

    public void collectAllUrls(String htmlText) {
        Pattern pattern = Pattern.compile("(?i)<a\\s+(?:[^>].*)?href=(['\"])(.*\\..*?)\\1");
        Matcher matcher = pattern.matcher(htmlText);
        while (matcher.find()) {
            String url = matcher.group(2);
            if (!url.startsWith("http")) {
                url = "https:" + url;
            }
            currentLevelUrls.add(url);
        }
    }
}
