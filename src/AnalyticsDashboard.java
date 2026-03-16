import java.util.*;
import java.util.stream.*;

/**
 * Problem 5: Real-Time Analytics Dashboard for Website Traffic
 * Processes page view events and maintains top pages, unique visitors,
 * and traffic source breakdowns using multiple HashMaps.
 */
public class AnalyticsDashboard {

    private final HashMap<String, Long> pageViews = new HashMap<>();
    private final HashMap<String, Set<String>> uniqueVisitors = new HashMap<>();
    private final HashMap<String, Long> trafficSources = new HashMap<>();
    private long totalEvents = 0;

    static class PageViewEvent {
        String url;
        String userId;
        String source;
        long timestamp;

        PageViewEvent(String url, String userId, String source) {
            this.url = url;
            this.userId = userId;
            this.source = source;
            this.timestamp = System.currentTimeMillis();
        }
    }

    public void processEvent(PageViewEvent event) {
        pageViews.merge(event.url, 1L, Long::sum);
        uniqueVisitors.computeIfAbsent(event.url, k -> new HashSet<>()).add(event.userId);
        trafficSources.merge(event.source.toLowerCase(), 1L, Long::sum);
        totalEvents++;
    }

    public void processBatch(List<PageViewEvent> events) {
        events.forEach(this::processEvent);
        System.out.println("Processed batch of " + events.size() + " events.");
    }

    public List<Map.Entry<String, Long>> getTopPages(int n) {
        PriorityQueue<Map.Entry<String, Long>> heap =
                new PriorityQueue<>(Map.Entry.comparingByValue());
        for (Map.Entry<String, Long> entry : pageViews.entrySet()) {
            heap.offer(entry);
            if (heap.size() > n) heap.poll();
        }
        List<Map.Entry<String, Long>> top = new ArrayList<>(heap);
        top.sort(Map.Entry.<String, Long>comparingByValue().reversed());
        return top;
    }

    public void getDashboard() {
        System.out.println("\n======= Real-Time Analytics Dashboard =======");
        System.out.println("Total Events Processed: " + totalEvents);
        System.out.println();

        System.out.println("--- Top Pages ---");
        List<Map.Entry<String, Long>> topPages = getTopPages(5);
        int rank = 1;
        for (Map.Entry<String, Long> entry : topPages) {
            String url = entry.getKey();
            long views = entry.getValue();
            int unique = uniqueVisitors.getOrDefault(url, Collections.emptySet()).size();
            System.out.printf("  %d. %-40s %,6d views  (%,d unique)%n",
                    rank++, url, views, unique);
        }

        System.out.println();
        System.out.println("--- Traffic Sources ---");
        long total = trafficSources.values().stream().mapToLong(Long::longValue).sum();
        trafficSources.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(entry -> {
                    double pct = total > 0 ? entry.getValue() * 100.0 / total : 0;
                    System.out.printf("  %-12s: %5.1f%%  (%,d visits)%n",
                            capitalize(entry.getKey()), pct, entry.getValue());
                });
        System.out.println("=============================================");
    }

    private String capitalize(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public static void main(String[] args) throws InterruptedException {
        AnalyticsDashboard dashboard = new AnalyticsDashboard();
        System.out.println("=== Problem 5: Real-Time Analytics Dashboard ===\n");

        String[] urls = {
                "/article/breaking-news",
                "/sports/championship",
                "/tech/ai-update",
                "/world/economy",
                "/entertainment/movies"
        };
        String[] sources = {"google", "facebook", "direct", "twitter", "other"};

        Random rand = new Random(42);
        List<PageViewEvent> events = new ArrayList<>();

        for (int i = 0; i < 500; i++) {
            String url = urls[rand.nextInt(urls.length)];
            if (rand.nextInt(3) == 0) url = "/article/breaking-news";
            String userId = "user_" + rand.nextInt(300);
            String source = sources[rand.nextInt(sources.length)];
            if (rand.nextInt(2) == 0) source = "google";
            events.add(new PageViewEvent(url, userId, source));
        }

        int batchSize = 100;
        for (int i = 0; i < events.size(); i += batchSize) {
            dashboard.processBatch(events.subList(i, Math.min(i + batchSize, events.size())));
            Thread.sleep(100);
        }

        dashboard.getDashboard();
    }
}