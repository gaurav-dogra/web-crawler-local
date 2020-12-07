package crawler;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlProcessingService implements Callable<UrlData> {

    private final String url;

    public UrlProcessingService(String url) {
        this.url = url;
    }

    public static UrlData getData(String url) throws IOException {
        String htmlText = getHtmlText(url);
        String title = getTitle(htmlText);
        Set<String> urls = collectUrls(htmlText);
        UrlData data = new UrlData(url, title, urls);
        System.out.println(data);
        return data;
    }

    private static String getHtmlText(String url) throws IOException {
        try {
            InputStream inputStream = new BufferedInputStream(new URL(url).openStream());
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IOException();
        }
    }

    private static String getTitle(String htmlText) {
        Pattern pattern = Pattern.compile("<title>(.+)</title>");
        Matcher matcher = pattern.matcher(htmlText);
        if (matcher.find()) {
//            System.out.println("matcher.group(1) = " + matcher.group(1));
            return matcher.group(1);
        } else {
            return "NO TITLE FOUND";
        }
    }

    public static Set<String> collectUrls(String htmlText) {
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

    @Override
    public UrlData call() throws Exception {
        return UrlProcessingService.getData(url);
    }
}
