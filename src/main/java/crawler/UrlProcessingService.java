package crawler;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlProcessingService {

    public String getHtml(String url) throws IOException, IllegalArgumentException {
        InputStream inputStream;
        try {
            inputStream = new BufferedInputStream(new URL(url).openStream());
        } catch (IOException e) {
            throw new IOException();
        }
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }

    public String getTitle(String html) {
        Pattern pattern = Pattern.compile("<title>(.+)</title>");
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return "NO TITLE FOUND";
        }
    }

    public Set<String> collectUrls(String html) {
        Set<String> urls = new HashSet<>();
        Pattern pattern = Pattern.compile("(?<=href=[\"']).*?(?=[\"'])");
        Matcher matcher = pattern.matcher(html);
        while (matcher.find()) {
            String url = matcher.group();
            if (!url.startsWith("http")) {
                url = "https:" + url;
            }
            urls.add(url);
        }
        return urls;
    }
}
