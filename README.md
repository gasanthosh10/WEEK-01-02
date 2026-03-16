# Hash Table Problems — Java Implementations

> **Student:** Santhosh | **Reg. No.:** RA2411056010166

A collection of 10 real-world problems solved using Hash Table data structures in Java.
Each problem is implemented as a standalone class with a `main()` method you can run directly.

---

## Problems Overview

| # | Class File | Problem | Key Concept |
|---|-----------|---------|-------------|
| 1 | `UsernameAvailabilityChecker.java` | Social Media Username Checker | HashMap O(1) lookup, frequency counting |
| 2 | `FlashSaleInventoryManager.java` | Flash Sale Inventory Manager | ConcurrentHashMap, atomic ops, FIFO waiting list |
| 3 | `DNSCache.java` | DNS Cache with TTL | LinkedHashMap LRU, TTL expiry, hit/miss stats |
| 4 | `PlagiarismDetector.java` | Plagiarism Detection System | N-gram hashing, similarity scoring |
| 5 | `AnalyticsDashboard.java` | Real-Time Analytics Dashboard | Multi-HashMap, PriorityQueue top-K |
| 6 | `RateLimiter.java` | Distributed Rate Limiter | Token bucket, ConcurrentHashMap, thread safety |
| 7 | `AutocompleteSystem.java` | Autocomplete Search Engine | Trie + HashMap hybrid, min-heap top-K |
| 8 | `ParkingLotManager.java` | Parking Lot (Open Addressing) | Linear probing, tombstone deletion, custom hash |
| 9 | `FinancialTransactionAnalyzer.java` | Two-Sum Financial Fraud Detection | Complement HashMap, K-Sum recursion, duplicate detection |
| 10 | `MultiLevelCache.java` | Multi-Level Cache (L1/L2/L3) | LinkedHashMap LRU, cache promotion, eviction |

---

## How to Run

Each file is a standalone Java class. Compile and run individually:
```bash
# Compile a single file
javac UsernameAvailabilityChecker.java

# Run it
java UsernameAvailabilityChecker
```

Or compile all at once:
```bash
javac src/*.java
java -cp src UsernameAvailabilityChecker
```

---

## Concepts Covered

- **Hash Table Basics** — key-value mapping, O(1) lookup and insert
- **Collision Resolution** — chaining (HashMap) and open addressing (linear probing)
- **LRU Eviction** — using `LinkedHashMap` in access-order mode
- **Thread Safety** — `ConcurrentHashMap`, `AtomicInteger`, `synchronized` blocks
- **N-gram Hashing** — string fingerprinting for similarity detection
- **Token Bucket Algorithm** — rate limiting with time-based refill
- **Trie + HashMap Hybrid** — prefix search for autocomplete
- **Frequency Counting** — `merge()` for efficient counting
- **Load Factor Management** — understanding resize triggers
- **Performance Benchmarking** — hit rates, latency tracking, probe counts

---

## Sample Output (Problem 1)
```
checkAvailability("john_doe") → false (already taken)
checkAvailability("jane_smith") → true (available)
suggestAlternatives("john_doe") → [john_doe1, john_doe2, john_doe3, john_doe4, john_doe5, john.doe, john_doe_99]
getMostAttempted() → "admin" (12 attempts)
```