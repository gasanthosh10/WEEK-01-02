import java.util.*;

/**
 * Problem 9: Two-Sum Problem Variants for Financial Transactions
 * Detects fraudulent pairs/groups using HashMap complement lookup in O(n).
 * Supports two-sum, time-window two-sum, k-sum, and duplicate detection.
 */
public class FinancialTransactionAnalyzer {

    static class Transaction {
        int id;
        double amount;
        String merchant;
        String account;
        long timestamp;

        Transaction(int id, double amount, String merchant, String account, long timestamp) {
            this.id = id;
            this.amount = amount;
            this.merchant = merchant;
            this.account = account;
            this.timestamp = timestamp;
        }

        @Override
        public String toString() {
            return String.format("{id:%d, $%.0f, %s, %s}", id, amount, merchant, account);
        }
    }

    private final List<Transaction> transactions = new ArrayList<>();

    public void addTransaction(int id, double amount, String merchant,
                               String account, long timestamp) {
        transactions.add(new Transaction(id, amount, merchant, account, timestamp));
    }

    public List<int[]> findTwoSum(double target) {
        List<int[]> results = new ArrayList<>();
        HashMap<Double, Integer> seen = new HashMap<>();

        for (int i = 0; i < transactions.size(); i++) {
            double current = transactions.get(i).amount;
            double complement = target - current;
            if (seen.containsKey(complement)) {
                results.add(new int[]{seen.get(complement), i});
            }
            seen.put(current, i);
        }

        System.out.println("findTwoSum(target=" + (int) target + ") →");
        if (results.isEmpty()) {
            System.out.println("  No pairs found.");
        } else {
            for (int[] pair : results) {
                Transaction a = transactions.get(pair[0]);
                Transaction b = transactions.get(pair[1]);
                System.out.printf("  (%s + %s) = %.0f%n", a, b, a.amount + b.amount);
            }
        }
        return results;
    }

    public List<int[]> findTwoSumWithWindow(double target, long windowMinutes) {
        List<int[]> results = new ArrayList<>();

        for (int i = 0; i < transactions.size(); i++) {
            for (int j = i + 1; j < transactions.size(); j++) {
                Transaction a = transactions.get(i);
                Transaction b = transactions.get(j);
                if (Math.abs(a.timestamp - b.timestamp) <= windowMinutes
                        && a.amount + b.amount == target) {
                    results.add(new int[]{i, j});
                }
            }
        }

        System.out.println("findTwoSumWithWindow(target=" + (int) target
                + ", window=" + windowMinutes + "min) →");
        if (results.isEmpty()) {
            System.out.println("  No pairs found within time window.");
        } else {
            for (int[] pair : results) {
                Transaction a = transactions.get(pair[0]);
                Transaction b = transactions.get(pair[1]);
                System.out.printf("  (%s + %s) within %d minutes%n", a, b, windowMinutes);
            }
        }
        return results;
    }

    public List<List<Transaction>> findKSum(int k, double target) {
        List<List<Transaction>> results = new ArrayList<>();
        List<Double> amounts = new ArrayList<>();
        for (Transaction t : transactions) amounts.add(t.amount);
        kSumHelper(amounts, target, k, 0, new ArrayList<>(), results);

        System.out.println("findKSum(k=" + k + ", target=" + (int) target + ") →");
        if (results.isEmpty()) {
            System.out.println("  No combinations found.");
        } else {
            for (List<Transaction> combo : results) {
                double sum = combo.stream().mapToDouble(t -> t.amount).sum();
                System.out.println("  " + combo + " = " + (int) sum);
            }
        }
        return results;
    }

    private void kSumHelper(List<Double> amounts, double target, int k,
                            int start, List<Transaction> current,
                            List<List<Transaction>> results) {
        if (k == 0 && Math.abs(target) < 0.001) {
            results.add(new ArrayList<>(current));
            return;
        }
        if (k == 0 || start >= amounts.size()) return;

        for (int i = start; i < amounts.size(); i++) {
            current.add(transactions.get(i));
            kSumHelper(amounts, target - amounts.get(i), k - 1, i + 1, current, results);
            current.remove(current.size() - 1);
        }
    }

    public void detectDuplicates() {
        HashMap<String, List<Transaction>> grouping = new HashMap<>();
        for (Transaction t : transactions) {
            String key = (int) t.amount + "_" + t.merchant;
            grouping.computeIfAbsent(key, k -> new ArrayList<>()).add(t);
        }

        System.out.println("detectDuplicates() →");
        boolean found = false;
        for (Map.Entry<String, List<Transaction>> entry : grouping.entrySet()) {
            List<Transaction> group = entry.getValue();
            Set<String> accounts = new HashSet<>();
            for (Transaction t : group) accounts.add(t.account);
            if (group.size() > 1 && accounts.size() > 1) {
                found = true;
                String[] parts = entry.getKey().split("_");
                System.out.printf("  Suspicious: $%s at %s — accounts: %s%n",
                        parts[0], parts[1], accounts);
            }
        }
        if (!found) System.out.println("  No duplicates detected.");
    }

    public static void main(String[] args) {
        FinancialTransactionAnalyzer analyzer = new FinancialTransactionAnalyzer();
        System.out.println("=== Problem 9: Two-Sum for Financial Transactions ===\n");

        analyzer.addTransaction(1, 500, "Store A", "acc_001", 600);
        analyzer.addTransaction(2, 300, "Store B", "acc_002", 615);
        analyzer.addTransaction(3, 200, "Store C", "acc_003", 630);
        analyzer.addTransaction(4, 700, "Store D", "acc_004", 700);
        analyzer.addTransaction(5, 500, "Store A", "acc_005", 620);
        analyzer.addTransaction(6, 150, "Store E", "acc_006", 635);
        analyzer.addTransaction(7, 350, "Store F", "acc_007", 640);

        System.out.println("Transactions loaded: 7\n");

        analyzer.findTwoSum(500);
        System.out.println();

        analyzer.findTwoSumWithWindow(500, 60);
        System.out.println();

        analyzer.findKSum(3, 1000);
        System.out.println();

        analyzer.detectDuplicates();
    }
}