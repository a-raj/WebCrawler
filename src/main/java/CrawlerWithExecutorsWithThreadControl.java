import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CrawlerWithExecutorsWithThreadControl {

    private final ExecutorCompletionService<String> executor;
    private final AtomicInteger MAX_PAGE_VISITS;

    // To control number on thread creation
    private final BlockingQueue<Object> threadLocker;

    private final Set<String> visitedPages      = new CopyOnWriteArraySet<>();
    private final BlockingQueue<String> tasks   = new LinkedBlockingQueue<>();




    public CrawlerWithExecutorsWithThreadControl(String baseURL, int executorCount, int maxPages) throws InterruptedException {

        ExecutorService executorService = Executors.newFixedThreadPool(executorCount);
        this.executor                   = new ExecutorCompletionService<>(executorService);
        this.MAX_PAGE_VISITS            = new AtomicInteger(maxPages);
        threadLocker                    = new ArrayBlockingQueue<>(executorCount);
        tasks.put(baseURL);
    }

    private void crawl(String url) {
        try {
            threadLocker.put(new Object()); // blocking method
            Thread thread = new Thread(() -> {
                try {
                    Document document       = Jsoup.connect(url).get();
                    Elements linksOnPage    = document.select("a[href]");

                    visitedPages.add(url);

                    System.out.println("Visited : " + url);

                    for (Element link : linksOnPage) {
                        String url1 = link.absUrl("href");
                        if (visitedPages.add(url1)) {
                            tasks.put(url1);
                        }
                    }
                    threadLocker.take();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            thread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startCrawling() {
        Thread crawler = new Thread(() -> {
            int count = 0; // Can remove this logic as executorCompletionService Shutdowns automatically after all application threads complete execution, but the program may execute for a very long period
            while (MAX_PAGE_VISITS.get() >= count) {
                try {
                    String url = tasks.take();
                    crawl(url);
                    count++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        crawler.start();
    }


    public static void main(String[] args) throws InterruptedException {
        new CrawlerWithExecutorsWithThreadControl("https://www.example.com", 10, 100).startCrawling();
    }
}