import java.util.*;

/**
 * Problem 10: Multi-Level Cache System with Hash Tables
 * Simulates L1 (in-memory), L2 (SSD-backed), and L3 (database) caching
 * for a video streaming service with LRU eviction and access-based promotion.
 */
public class MultiLevelCache {

    static class VideoData {
        String videoId;
        String title;
        long sizeKB;
        String filePath;

        VideoData(String videoId, String title, long sizeKB) {
            this.videoId = videoId;
            this.title = title;
            this.sizeKB = sizeKB;
        }

        VideoData(String videoId, String filePath) {
            this.videoId = videoId;
            this.filePath = filePath;
            this.title = "SSD:" + videoId;
            this.sizeKB = 0;
        }

        @Override
        public String toString() {
            return "[" + videoId + ": " + title + "]";
        }
    }

    private static final int L1_CAPACITY = 5;
    private static final int L2_CAPACITY = 10;
    private static final int PROMOTION_THRESHOLD = 2;

    private final LinkedHashMap<String, VideoData> l1Cache;
    private final LinkedHashMap<String, VideoData> l2Cache;
    private final HashMap<String, VideoData> l3Database = new HashMap<>();
    private final HashMap<String, Integer> accessCount = new HashMap<>();

    private int l1Hits, l2Hits, l3Hits, misses;
    private long totalLatencyMs;
    private int totalRequests;

    public MultiLevelCache() {
        l1Cache = new LinkedHashMap<>(L1_CAPACITY, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, VideoData> eldest) {
                if (size() > L1_CAPACITY) {
                    System.out.println("  [L1 Eviction] Evicting "
                            + eldest.getKey() + " → demoted to L2");
                    l2Cache.put(eldest.getKey(),
                            new VideoData(eldest.getKey(),
                                    "/ssd/" + eldest.getKey() + ".mp4"));
                    return true;
                }
                return false;
            }
        };

        l2Cache = new LinkedHashMap<>(L2_CAPACITY, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, VideoData> eldest) {
                if (size() > L2_CAPACITY) {
                    System.out.println("  [L2 Eviction] Evicting "
                            + eldest.getKey() + " from SSD");
                    return true;
                }
                return false;
            }
        };

        String[] ids = {"video_100", "video_123", "video_200", "video_300",
                "video_400", "video_500", "video_999"};
        String[] titles = {"Avengers", "Inception", "Interstellar", "Joker",
                "Parasite", "Dune", "Oppenheimer"};
        for (int i = 0; i < ids.length; i++) {
            l3Database.put(ids[i], new VideoData(ids[i], titles[i], 4_000_000));
        }
    }

    public VideoData getVideo(String videoId) {
        totalRequests++;
        accessCount.merge(videoId, 1, Integer::sum);
        int count = accessCount.get(videoId);

        System.out.println("getVideo(\"" + videoId + "\")");

        // L1 Check
        VideoData data = l1Cache.get(videoId);
        if (data != null) {
            l1Hits++;
            totalLatencyMs += 1;
            System.out.println("  → L1 Cache HIT (1ms)");
            return data;
        }
        System.out.println("  → L1 Cache MISS");

        // L2 Check
        data = l2Cache.get(videoId);
        if (data != null) {
            l2Hits++;
            totalLatencyMs += 5;
            System.out.println("  → L2 Cache HIT (5ms)");
            if (count >= PROMOTION_THRESHOLD) {
                VideoData fullData = l3Database.getOrDefault(videoId,
                        new VideoData(videoId, "Cached Video", 1000));
                l1Cache.put(videoId, fullData);
                System.out.println("  → Promoted to L1 (access count: " + count + ")");
            }
            return data;
        }
        System.out.println("  → L2 Cache MISS");

        // L3 Database
        data = l3Database.get(videoId);
        if (data != null) {
            l3Hits++;
            totalLatencyMs += 150;
            System.out.println("  → L3 Database HIT (150ms)");
            l2Cache.put(videoId, new VideoData(videoId, "/ssd/" + videoId + ".mp4"));
            System.out.println("  → Added to L2 (access count: " + count + ")");
            return data;
        }

        misses++;
        totalLatencyMs += 150;
        System.out.println("  → NOT FOUND in any cache layer (150ms)");
        return null;
    }

    public void invalidate(String videoId) {
        l1Cache.remove(videoId);
        l2Cache.remove(videoId);
        System.out.println("Invalidated \"" + videoId + "\" from L1 and L2.");
    }

    public void getStatistics() {
        double l1Rate = totalRequests > 0 ? l1Hits * 100.0 / totalRequests : 0;
        double l2Rate = totalRequests > 0 ? l2Hits * 100.0 / totalRequests : 0;
        double l3Rate = totalRequests > 0 ? l3Hits * 100.0 / totalRequests : 0;
        double overallRate = totalRequests > 0
                ? (l1Hits + l2Hits + l3Hits) * 100.0 / totalRequests : 0;
        double avgLatency = totalRequests > 0
                ? totalLatencyMs * 1.0 / totalRequests : 0;

        System.out.println("\n========= Cache Statistics =========");
        System.out.printf("  L1 (Memory) : Hit Rate %5.1f%%  Avg Time: ~1ms   | Size: %d/%d%n",
                l1Rate, l1Cache.size(), L1_CAPACITY);
        System.out.printf("  L2 (SSD)    : Hit Rate %5.1f%%  Avg Time: ~5ms   | Size: %d/%d%n",
                l2Rate, l2Cache.size(), L2_CAPACITY);
        System.out.printf("  L3 (DB)     : Hit Rate %5.1f%%  Avg Time: ~150ms | (all videos)%n",
                l3Rate);
        System.out.printf("  Overall     : Hit Rate %5.1f%%  Avg Latency: %.1fms%n",
                overallRate, avgLatency);
        System.out.println("====================================");
    }

    public static void main(String[] args) throws InterruptedException {
        MultiLevelCache cache = new MultiLevelCache();
        System.out.println("=== Problem 10: Multi-Level Cache System ===\n");

        cache.getVideo("video_123");
        System.out.println();

        cache.getVideo("video_123");
        System.out.println();

        cache.getVideo("video_123");
        System.out.println();

        cache.getVideo("video_999");
        System.out.println();

        cache.getVideo("video_FAKE");
        System.out.println();

        System.out.println("--- Filling L1 cache to trigger eviction ---");
        cache.getVideo("video_100");
        cache.getVideo("video_100");
        cache.getVideo("video_200");
        cache.getVideo("video_200");
        cache.getVideo("video_300");
        cache.getVideo("video_300");
        cache.getVideo("video_400");
        cache.getVideo("video_400");
        System.out.println();

        cache.invalidate("video_123");
        System.out.println();

        cache.getStatistics();
    }
}