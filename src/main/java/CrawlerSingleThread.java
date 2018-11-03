import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class CrawlerSingleThread {

    private Set<String> visitedPages = new HashSet<>();
    private Queue<String> pagesToVisit = new LinkedList<>();

    public void crawl(String url) {

        pagesToVisit.add(url);
        long startTime = System.currentTimeMillis();
        while (!pagesToVisit.isEmpty() && visitedPages.size() < 100) {

            try {
                String visit = pagesToVisit.poll();

                if (visitedPages.contains(visit)) continue;

                Document document = Jsoup.connect(visit).get();
                Elements linksOnPage = document.select("a[href]");

                visitedPages.add(visit);
                System.out.println("Visited : " + visit);

                for (Element link : linksOnPage) {
                    pagesToVisit.add(link.absUrl("href"));
                }

            } catch (IOException e) {
                System.out.println(" Error while fetching " + url);
                e.printStackTrace();
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Time taken : " + (endTime - startTime));
    }

    public static void main(String[] args) {
        new CrawlerSingleThread().crawl("https://www.example.com");
    }

}
