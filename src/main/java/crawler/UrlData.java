package crawler;

import java.util.Set;

public class PageInfo {
    private final String url;
    private final String title;
    private final Set<String> allUrls;

    public PageInfo(String url, String title, Set<String> allUrls) {
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

    public Set<String> getAllUrls() {
        return allUrls;
    }

    @Override
    public boolean equals(Object obj) {
        String newUrl = ((PageInfo) obj).getUrl();
        return url.equals(newUrl);
    }
}
