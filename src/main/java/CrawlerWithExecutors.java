import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CrawlerWithExecutors implements Runnable{

    private final int MAX_PAGES = 100;
    private String newUrl;
    private final ExecutorService executorService;
    private final ConcurrentHashMap<String, Object> visitedPages;

    private final Object OBJECT = new Object();

    public CrawlerWithExecutors(String newUrl, ExecutorService executorService, ConcurrentHashMap<String, Object> visitedPages) {
        this.newUrl = newUrl;
        this.executorService = executorService;
        this.visitedPages = visitedPages;
    }

    @Override
    public void run() {

        if ( !visitedPages.containsKey(newUrl) && visitedPages.size() < MAX_PAGES) {

            try {
                Document document = Jsoup.connect(newUrl).get();
                Elements linksOnPage = document.select("a[href]");

                visitedPages.put(newUrl, OBJECT);

                System.out.println("Visited : " + newUrl);

                for (Element link : linksOnPage) {
                    link.absUrl("href");
                    executorService.submit(new CrawlerWithExecutors(link.absUrl("href"), executorService, visitedPages));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    public static void main(String[] args) throws InterruptedException{

        String url = "https://en.wikipedia.org/wiki/Main_Page";
        ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        executorService.submit(new CrawlerWithExecutors(url, executorService, map));
    }


}
