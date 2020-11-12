package crawler;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProcessUrl {

    public static String getHtmlTextFrom(String url) throws Exception {
        InputStream inputStream = new BufferedInputStream(new URL(url).openStream());
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }

    public static Set<String> collectAllUrlsFrom(String htmlText) {
        Set<String> urlsOnPage = new HashSet<>();
        Pattern pattern = Pattern.compile("(?i)<a\\s+(?:[^>].*)?href=(['\"])(.*\\..*?)\\1");
        Matcher matcher = pattern.matcher(htmlText);
        while (matcher.find()) {
            String url = matcher.group(2);
            if (!url.startsWith("http")) {
                url = "https:" + url;
            }
            urlsOnPage.add(url);
        }
        return urlsOnPage;
    }
}
