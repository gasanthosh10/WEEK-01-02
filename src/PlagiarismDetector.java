import java.util.*;

/**
 * Problem 4: Plagiarism Detection System
 * Uses n-gram hashing to detect document similarity in O(n) time.
 * Breaks documents into 5-grams stored in a HashMap for fast lookups.
 */
public class PlagiarismDetector {

    private final HashMap<String, Set<String>> ngramIndex = new HashMap<>();
    private final HashMap<String, List<String>> documentNgrams = new HashMap<>();
    private final HashMap<String, String> documentStore = new HashMap<>();

    private static final int N = 5;

    public void addDocument(String docId, String text) {
        List<String> ngrams = extractNgrams(text, N);
        documentStore.put(docId, text);
        documentNgrams.put(docId, ngrams);
        for (String ngram : ngrams) {
            ngramIndex.computeIfAbsent(ngram, k -> new HashSet<>()).add(docId);
        }
        System.out.println("Indexed \"" + docId + "\" → " + ngrams.size() + " n-grams extracted.");
    }

    private List<String> extractNgrams(String text, int n) {
        String[] words = text.toLowerCase().replaceAll("[^a-z0-9 ]", " ").split("\\s+");
        List<String> ngrams = new ArrayList<>();
        for (int i = 0; i <= words.length - n; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = i; j < i + n; j++) {
                if (j > i) sb.append(" ");
                sb.append(words[j]);
            }
            ngrams.add(sb.toString());
        }
        return ngrams;
    }

    public void analyzeDocument(String docId, String text) {
        System.out.println("\nanalyzeDocument(\"" + docId + "\")");
        List<String> ngrams = extractNgrams(text, N);
        System.out.println("  → Extracted " + ngrams.size() + " n-grams");

        HashMap<String, Integer> matchCounts = new HashMap<>();
        for (String ngram : ngrams) {
            Set<String> matchingDocs = ngramIndex.get(ngram);
            if (matchingDocs != null) {
                for (String matchDoc : matchingDocs) {
                    if (!matchDoc.equals(docId)) {
                        matchCounts.merge(matchDoc, 1, Integer::sum);
                    }
                }
            }
        }

        if (matchCounts.isEmpty()) {
            System.out.println("  → No matching documents found. Original content.");
            return;
        }

        matchCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> {
                    String matchDoc = entry.getKey();
                    int matchCount = entry.getValue();
                    double similarity = (matchCount * 100.0) / ngrams.size();
                    String verdict = similarity >= 50 ? "PLAGIARISM DETECTED"
                            : similarity >= 15 ? "suspicious"
                            : "low similarity";
                    System.out.printf("  → Found %d matching n-grams with \"%s\"%n", matchCount, matchDoc);
                    System.out.printf("     Similarity: %.1f%% (%s)%n", similarity, verdict);
                });
    }

    public double getSimilarity(String docA, String docB) {
        List<String> ngramsA = documentNgrams.get(docA);
        List<String> ngramsB = documentNgrams.get(docB);
        if (ngramsA == null || ngramsB == null) return 0.0;
        Set<String> setA = new HashSet<>(ngramsA);
        Set<String> setB = new HashSet<>(ngramsB);
        long common = setA.stream().filter(setB::contains).count();
        double similarity = (common * 100.0) / Math.max(setA.size(), setB.size());
        System.out.printf("getSimilarity(\"%s\", \"%s\") → %.1f%% (%d common n-grams)%n",
                docA, docB, similarity, common);
        return similarity;
    }

    public void printStats() {
        System.out.println("\n--- Plagiarism Detector Stats ---");
        System.out.println("  Documents indexed : " + documentStore.size());
        System.out.println("  Unique n-grams    : " + ngramIndex.size());
    }

    public static void main(String[] args) {
        PlagiarismDetector detector = new PlagiarismDetector();
        System.out.println("=== Problem 4: Plagiarism Detection System ===\n");

        String essay089 = "The impact of climate change on global ecosystems has been profound and far-reaching. "
                + "Scientists have observed significant shifts in temperature patterns across all continents. "
                + "These changes have led to widespread disruption of natural habitats and biodiversity. "
                + "Polar ice caps are melting at unprecedented rates causing sea levels to rise. "
                + "Many coastal communities face the threat of permanent flooding in coming decades.";

        String essay092 = "Climate change impact on global ecosystems has been profound and far-reaching. "
                + "Scientists observed significant shifts in temperature patterns across all continents. "
                + "These changes have led to disruption of natural habitats and biodiversity loss. "
                + "Polar ice caps are melting at unprecedented rates and sea levels continue to rise. "
                + "Coastal communities around the world face threats of flooding in coming decades. "
                + "Governments must act swiftly to reduce carbon emissions worldwide.";

        String essay123 = "Renewable energy solutions offer a promising path toward a sustainable future. "
                + "Solar and wind power technologies have advanced dramatically over the past decade. "
                + "The cost of clean energy production has fallen sharply making it competitive. "
                + "Many countries are now investing heavily in green energy infrastructure. "
                + "Transitioning away from fossil fuels is both economically and environmentally necessary.";

        detector.addDocument("essay_089.txt", essay089);
        detector.addDocument("essay_092.txt", essay092);
        detector.addDocument("essay_123.txt", essay123);
        System.out.println();

        String newSubmission = "The impact of climate change on global ecosystems has been profound and far-reaching. "
                + "Scientists have observed significant shifts in temperature patterns across all continents. "
                + "These changes have led to widespread disruption of natural habitats and biodiversity. "
                + "Polar ice caps are melting at unprecedented rates causing sea levels to rise. "
                + "Many coastal communities face the threat of permanent flooding in coming decades. "
                + "It is essential that world leaders take immediate action.";

        detector.analyzeDocument("essay_new.txt", newSubmission);
        System.out.println();
        detector.getSimilarity("essay_089.txt", "essay_092.txt");
        detector.getSimilarity("essay_089.txt", "essay_123.txt");
        detector.printStats();
    }
}