package crawler;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UrlProcessingServiceTest {
    private static String expectedHtml;
    private final UrlProcessingService service = new UrlProcessingService();
    private static Set<String> expectedUrls;

    @BeforeAll
    static void populate() throws IOException {
        //read from local file
        File expectedHtmlFile = new File("wikipedia.html");
        expectedHtml = new Scanner(expectedHtmlFile).useDelimiter("\\Z").next();
        expectedUrls = readFile("output.txt");

    }

    private static Set<String> readFile(String filename) throws IOException {
        Set<String> expectedUrls = new HashSet<>();

        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
        while ((line = br.readLine()) != null) {
            if (line.startsWith("http")) {
                expectedUrls.add(line);
            }
        }
        return expectedUrls;
    }

    @Test
    void getHtml() throws WebCrawlerException {
        String actualHtml = service.getHtml("https://www.wikipedia.org/");
        assertEquals(expectedHtml, actualHtml);
    }

    @Test
    void getTitle() {
        assertEquals("Wikipedia", service.getTitle(expectedHtml));
    }

    @Test
    void testUrlsCollected() throws WebCrawlerException {
        Set<String> actualUrls = service.collectUrls(service.getHtml("https://www.wikipedia.org"));
        actualUrls.add("https://www.wikipedia.org/"); // adding input url
        assertEquals(expectedUrls, actualUrls);
    }
}