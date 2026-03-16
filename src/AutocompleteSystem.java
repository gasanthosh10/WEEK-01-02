import java.util.*;

/**
 * Problem 7: Autocomplete System for Search Engine
 * Hybrid Trie + HashMap approach for prefix search in <50ms.
 * Supports frequency updates, top-K suggestions, and basic typo tolerance.
 */
public class AutocompleteSystem {

    static class TrieNode {
        HashMap<Character, TrieNode> children = new HashMap<>();
        int frequency = 0;
        String query = null;
    }

    private final TrieNode root = new TrieNode();
    private final HashMap<String, Integer> queryFrequency = new HashMap<>();

    public void insert(String query, int frequency) {
        query = query.toLowerCase().trim();
        queryFrequency.merge(query, frequency, Integer::sum);

        TrieNode current = root;
        for (char c : query.toCharArray()) {
            current.children.putIfAbsent(c, new TrieNode());
            current = current.children.get(c);
        }
        current.frequency = queryFrequency.get(query);
        current.query = query;
    }

    public void updateFrequency(String query) {
        query = query.toLowerCase().trim();
        int newFreq = queryFrequency.merge(query, 1, Integer::sum);

        TrieNode current = root;
        for (char c : query.toCharArray()) {
            if (!current.children.containsKey(c)) {
                insert(query, 1);
                System.out.println("updateFrequency(\"" + query + "\") → New query, frequency: 1");
                return;
            }
            current = current.children.get(c);
        }
        current.frequency = newFreq;
        System.out.println("updateFrequency(\"" + query + "\") → Frequency: "
                + (newFreq - 1) + " → " + newFreq);
    }

    public List<String[]> search(String prefix, int topK) {
        prefix = prefix.toLowerCase().trim();

        TrieNode current = root;
        for (char c : prefix.toCharArray()) {
            if (!current.children.containsKey(c)) {
                System.out.println("search(\"" + prefix + "\") → No suggestions found.");
                return Collections.emptyList();
            }
            current = current.children.get(c);
        }

        PriorityQueue<TrieNode> minHeap = new PriorityQueue<>(
                Comparator.comparingInt(n -> n.frequency));

        collectQueries(current, minHeap, topK);

        List<String[]> results = new ArrayList<>(minHeap.size());
        for (TrieNode node : minHeap) {
            results.add(new String[]{node.query, String.valueOf(node.frequency)});
        }
        results.sort((a, b) -> Integer.compare(
                Integer.parseInt(b[1]), Integer.parseInt(a[1])));

        System.out.println("search(\"" + prefix + "\") →");
        int rank = 1;
        for (String[] r : results) {
            System.out.printf("  %d. \"%s\" (%,d searches)%n",
                    rank++, r[0], Integer.parseInt(r[1]));
        }
        return results;
    }

    private void collectQueries(TrieNode node, PriorityQueue<TrieNode> minHeap, int k) {
        if (node.query != null && node.frequency > 0) {
            minHeap.offer(node);
            if (minHeap.size() > k) minHeap.poll();
        }
        for (TrieNode child : node.children.values()) {
            collectQueries(child, minHeap, k);
        }
    }

    public List<String> suggestCorrections(String query) {
        query = query.toLowerCase().trim();
        List<String> suggestions = new ArrayList<>();
        char[] chars = query.toCharArray();

        for (int i = 0; i < chars.length - 1; i++) {
            char[] swapped = chars.clone();
            char tmp = swapped[i];
            swapped[i] = swapped[i + 1];
            swapped[i + 1] = tmp;
            String candidate = new String(swapped);
            if (queryFrequency.containsKey(candidate) && !candidate.equals(query)) {
                suggestions.add(candidate + " (" + queryFrequency.get(candidate) + " searches)");
            }
        }

        if (suggestions.isEmpty()) {
            System.out.println("suggestCorrections(\"" + query + "\") → No corrections found.");
        } else {
            System.out.println("suggestCorrections(\"" + query + "\") → " + suggestions);
        }
        return suggestions;
    }

    public static void main(String[] args) {
        AutocompleteSystem autocomplete = new AutocompleteSystem();
        System.out.println("=== Problem 7: Autocomplete System ===\n");

        autocomplete.insert("java tutorial", 1_234_567);
        autocomplete.insert("javascript", 987_654);
        autocomplete.insert("java download", 456_789);
        autocomplete.insert("java 21 features", 1);
        autocomplete.insert("java interview questions", 345_000);
        autocomplete.insert("javascript frameworks", 230_000);
        autocomplete.insert("javascript vs python", 150_000);
        autocomplete.insert("python tutorial", 1_100_000);
        autocomplete.insert("python download", 400_000);
        autocomplete.insert("python for beginners", 620_000);

        System.out.println("Trie initialized with sample queries.\n");

        autocomplete.search("jav", 5);
        System.out.println();

        autocomplete.search("python", 3);
        System.out.println();

        autocomplete.updateFrequency("java 21 features");
        autocomplete.updateFrequency("java 21 features");
        System.out.println();

        autocomplete.suggestCorrections("avascript");
        System.out.println();

        autocomplete.search("xyz", 5);
    }
}