package crawler;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProcessUrl implements Callable<Map<String, Set<String>>> {

    private final String url;

    public ProcessUrl(String url) {
        this.url = url;
    }

    @Override
    public Map<String, Set<String>> call() throws Exception {
        Map<String, Set<String>> titleAndUrls = new HashMap<>();
        String htmlText = getHtmlText();
        if (htmlText != null) {
            String title = getTitleFrom(htmlText);
            Set<String> urls = collectURLsFrom(htmlText);
            titleAndUrls.put(title, urls);
        }
        return titleAndUrls;
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

    private String getTitleFrom(String htmlText) {
        Pattern pattern = Pattern.compile("<title>(.+)</title>");
        Matcher matcher = pattern.matcher(htmlText);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

    public Set<String> collectURLsFrom(String htmlText) {
        Set<String> urls = new HashSet<>();
        Pattern pattern = Pattern.compile("(?i)<a\\s+(?:[^>].*)?href=(['\"])(.*\\..*?)\\1");
        Matcher matcher = pattern.matcher(htmlText);
        while (matcher.find()) {
            String url = matcher.group(2);
            if (!url.startsWith("http")) {
                url = "https:" + url;
            }
            urls.add(url);
        }
        return urls;
    }

}
