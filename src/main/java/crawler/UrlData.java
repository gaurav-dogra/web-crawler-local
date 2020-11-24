package crawler;

import java.util.Set;

public class UrlData {
    private final String url;
    private final String title;
    private final Set<String> allUrls;

    public UrlData(String url, String title, Set<String> allUrls) {
        this.url = url;
        this.title = title;
        this.allUrls = allUrls;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public Set<String> getUrls() {
        return allUrls;
    }

    @Override
    public boolean equals(Object obj) {
        String newUrl = ((UrlData) obj).getUrl();
        return url.equals(newUrl);
    }

    @Override
    public String toString() {
        return "Url: " + url + ", Title: " + title + ", Urls on page: " + allUrls.size();
    }
}
