import java.util.*;
import java.util.concurrent.*;

/**
 * Problem 6: Distributed Rate Limiter for API Gateway
 * Implements a token bucket algorithm per client using HashMap.
 * Supports burst traffic, automatic refill, and 1ms response time.
 */
public class RateLimiter {

    static class TokenBucket {
        String clientId;
        double tokens;
        double maxTokens;
        double refillRate;
        long lastRefillTime;
        long windowResetTime;

        TokenBucket(String clientId, double maxTokens, double refillRatePerHour) {
            this.clientId = clientId;
            this.maxTokens = maxTokens;
            this.tokens = maxTokens;
            this.refillRate = refillRatePerHour / (3600.0 * 1000);
            this.lastRefillTime = System.currentTimeMillis();
            this.windowResetTime = System.currentTimeMillis() + 3_600_000;
        }

        void refill() {
            long now = System.currentTimeMillis();
            double elapsed = now - lastRefillTime;
            double newTokens = elapsed * refillRate;
            tokens = Math.min(maxTokens, tokens + newTokens);
            lastRefillTime = now;
        }

        boolean consume() {
            refill();
            if (tokens >= 1.0) {
                tokens -= 1.0;
                return true;
            }
            return false;
        }

        long secondsUntilReset() {
            long now = System.currentTimeMillis();
            return Math.max(0, (windowResetTime - now) / 1000);
        }

        int remainingRequests() {
            refill();
            return (int) Math.floor(tokens);
        }
    }

    private final ConcurrentHashMap<String, TokenBucket> clients = new ConcurrentHashMap<>();
    private static final double MAX_TOKENS = 1000;
    private static final double REFILL_RATE_PER_HOUR = 1000;

    private TokenBucket getBucket(String clientId) {
        return clients.computeIfAbsent(clientId,
                id -> new TokenBucket(id, MAX_TOKENS, REFILL_RATE_PER_HOUR));
    }

    public RateLimitResult checkRateLimit(String clientId) {
        long start = System.nanoTime();
        TokenBucket bucket = getBucket(clientId);

        boolean allowed;
        synchronized (bucket) {
            allowed = bucket.consume();
        }

        long elapsedUs = (System.nanoTime() - start) / 1000;
        int remaining = bucket.remainingRequests();
        long retryAfter = bucket.secondsUntilReset();

        RateLimitResult result = new RateLimitResult(clientId, allowed, remaining, retryAfter, elapsedUs);
        System.out.println(result);
        return result;
    }

    public void getRateLimitStatus(String clientId) {
        TokenBucket bucket = getBucket(clientId);
        long used = (long)(MAX_TOKENS - bucket.remainingRequests());
        System.out.printf("getRateLimitStatus(\"%s\") → {used: %d, limit: %d, reset: %ds}%n",
                clientId, used, (long) MAX_TOKENS, bucket.secondsUntilReset());
    }

    static class RateLimitResult {
        String clientId;
        boolean allowed;
        int remaining;
        long retryAfterSeconds;
        long latencyMicros;

        RateLimitResult(String clientId, boolean allowed, int remaining,
                        long retryAfterSeconds, long latencyMicros) {
            this.clientId = clientId;
            this.allowed = allowed;
            this.remaining = remaining;
            this.retryAfterSeconds = retryAfterSeconds;
            this.latencyMicros = latencyMicros;
        }

        @Override
        public String toString() {
            if (allowed) {
                return String.format("checkRateLimit(\"%s\") → Allowed (%d requests remaining) [%dµs]",
                        clientId, remaining, latencyMicros);
            } else {
                return String.format("checkRateLimit(\"%s\") → DENIED (0 remaining, retry after %ds) [%dµs]",
                        clientId, retryAfterSeconds, latencyMicros);
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        RateLimiter limiter = new RateLimiter();
        System.out.println("=== Problem 6: Distributed Rate Limiter ===\n");

        limiter.checkRateLimit("abc123");
        limiter.checkRateLimit("abc123");
        limiter.checkRateLimit("abc123");
        System.out.println();

        TokenBucket demoBucket = new TokenBucket("xyz789", 5, 5);
        limiter.clients.put("xyz789", demoBucket);

        System.out.println("-- Draining client xyz789 (5 token demo bucket) --");
        for (int i = 0; i < 7; i++) {
            limiter.checkRateLimit("xyz789");
        }
        System.out.println();

        System.out.println("-- Multi-client scenario --");
        String[] clients = {"client_A", "client_B", "client_C"};
        for (String client : clients) {
            limiter.checkRateLimit(client);
        }
        System.out.println();

        limiter.getRateLimitStatus("abc123");
        limiter.getRateLimitStatus("xyz789");
    }
}