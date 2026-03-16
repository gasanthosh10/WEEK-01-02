import java.util.*;
import java.util.concurrent.*;

/**
 * Problem 3: DNS Cache with TTL (Time To Live)
 * Stores domain → IP mappings, supports TTL expiry, LRU eviction,
 * cache hit/miss stats, and simulates upstream DNS resolution.
 */
public class DNSCache {

    static class DNSEntry {
        String domain;
        String ipAddress;
        long createdAt;
        long ttlMillis;

        DNSEntry(String domain, String ipAddress, long ttlSeconds) {
            this.domain = domain;
            this.ipAddress = ipAddress;
            this.createdAt = System.currentTimeMillis();
            this.ttlMillis = ttlSeconds * 1000;
        }

        boolean isExpired() {
            return System.currentTimeMillis() - createdAt > ttlMillis;
        }

        long remainingTTL() {
            long remaining = ttlMillis - (System.currentTimeMillis() - createdAt);
            return Math.max(remaining / 1000, 0);
        }
    }

    private static final int MAX_CACHE_SIZE = 5;

    private final LinkedHashMap<String, DNSEntry> cache;
    private int hits = 0;
    private int misses = 0;
    private long totalLookupTime = 0;
    private int totalLookups = 0;

    private final HashMap<String, String> upstreamDNS = new HashMap<>();

    public DNSCache() {
        this.cache = new LinkedHashMap<>(MAX_CACHE_SIZE, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, DNSEntry> eldest) {
                return size() > MAX_CACHE_SIZE;
            }
        };
        upstreamDNS.put("google.com", "172.217.14.206");
        upstreamDNS.put("facebook.com", "157.240.1.35");
        upstreamDNS.put("github.com", "140.82.121.4");
        upstreamDNS.put("openai.com", "104.18.7.23");
        upstreamDNS.put("amazon.com", "52.94.236.248");
        upstreamDNS.put("netflix.com", "54.74.3.42");

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::cleanExpiredEntries, 5, 5, TimeUnit.SECONDS);
    }

    public String resolve(String domain) {
        long startTime = System.nanoTime();
        totalLookups++;
        DNSEntry entry = cache.get(domain.toLowerCase());

        if (entry != null && !entry.isExpired()) {
            hits++;
            long elapsed = (System.nanoTime() - startTime) / 1_000_000;
            totalLookupTime += elapsed;
            System.out.println("resolve(\"" + domain + "\") → Cache HIT → "
                    + entry.ipAddress + " (TTL remaining: " + entry.remainingTTL() + "s, " + elapsed + "ms)");
            return entry.ipAddress;
        } else if (entry != null) {
            cache.remove(domain.toLowerCase());
            misses++;
            System.out.print("resolve(\"" + domain + "\") → Cache EXPIRED → ");
        } else {
            misses++;
            System.out.print("resolve(\"" + domain + "\") → Cache MISS → ");
        }

        String ip = queryUpstream(domain);
        long elapsed = (System.nanoTime() - startTime) / 1_000_000;
        totalLookupTime += elapsed;

        if (ip != null) {
            long ttl = 300;
            cache.put(domain.toLowerCase(), new DNSEntry(domain, ip, ttl));
            System.out.println("Queried upstream → " + ip + " (TTL: " + ttl + "s, " + elapsed + "ms)");
        } else {
            System.out.println("NXDOMAIN (domain not found)");
        }
        return ip;
    }

    private String queryUpstream(String domain) {
        try { Thread.sleep(2); } catch (InterruptedException ignored) {}
        return upstreamDNS.get(domain.toLowerCase());
    }

    private void cleanExpiredEntries() {
        cache.entrySet().removeIf(e -> e.getValue().isExpired());
    }

    public void addRecord(String domain, String ip, long ttlSeconds) {
        cache.put(domain.toLowerCase(), new DNSEntry(domain, ip, ttlSeconds));
        System.out.println("Manual record added: " + domain + " → " + ip + " (TTL: " + ttlSeconds + "s)");
    }

    public void getCacheStats() {
        double hitRate = totalLookups > 0 ? (hits * 100.0 / totalLookups) : 0;
        double avgTime = totalLookups > 0 ? (totalLookupTime * 1.0 / totalLookups) : 0;
        System.out.println("\n--- Cache Statistics ---");
        System.out.printf("  Total Lookups : %d%n", totalLookups);
        System.out.printf("  Cache Hits    : %d%n", hits);
        System.out.printf("  Cache Misses  : %d%n", misses);
        System.out.printf("  Hit Rate      : %.1f%%%n", hitRate);
        System.out.printf("  Avg Lookup    : %.1fms%n", avgTime);
        System.out.printf("  Cache Size    : %d / %d%n", cache.size(), MAX_CACHE_SIZE);
    }

    public void printCache() {
        System.out.println("\n--- Current Cache ---");
        cache.forEach((k, v) -> System.out.printf("  %-20s → %-20s TTL: %ds%n",
                k, v.ipAddress, v.remainingTTL()));
    }

    public static void main(String[] args) throws InterruptedException {
        DNSCache dns = new DNSCache();
        System.out.println("=== Problem 3: DNS Cache with TTL ===\n");
        dns.resolve("google.com");
        dns.resolve("facebook.com");
        dns.resolve("github.com");
        System.out.println();
        dns.resolve("google.com");
        dns.resolve("facebook.com");
        System.out.println();
        dns.resolve("unknown-site.xyz");
        System.out.println();
        dns.resolve("openai.com");
        dns.resolve("amazon.com");
        dns.resolve("netflix.com");
        dns.printCache();
        dns.getCacheStats();
    }
}