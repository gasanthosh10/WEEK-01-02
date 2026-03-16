import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/**
 * Problem 2: E-Commerce Flash Sale Inventory Manager
 * Tracks real-time stock, processes purchases in O(1), handles concurrency,
 * and maintains a FIFO waiting list using LinkedHashMap.
 */
public class FlashSaleInventoryManager {

    private final ConcurrentHashMap<String, AtomicInteger> inventory = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Queue<Integer>> waitingList = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<String>> purchaseLogs = new ConcurrentHashMap<>();

    public FlashSaleInventoryManager() {
        addProduct("IPHONE15_256GB", 100);
        addProduct("SAMSUNG_S24", 50);
        addProduct("MACBOOK_PRO", 20);
    }

    public void addProduct(String productId, int stock) {
        inventory.put(productId, new AtomicInteger(stock));
        waitingList.put(productId, new LinkedList<>());
        purchaseLogs.put(productId, new ArrayList<>());
        System.out.println("Product added: " + productId + " with " + stock + " units.");
    }

    public int checkStock(String productId) {
        AtomicInteger stock = inventory.get(productId);
        int count = (stock != null) ? stock.get() : 0;
        System.out.println("checkStock(\"" + productId + "\") → " + count + " units available");
        return count;
    }

    public String purchaseItem(String productId, int userId) {
        AtomicInteger stock = inventory.get(productId);
        if (stock == null) return "Product not found: " + productId;

        while (true) {
            int current = stock.get();
            if (current <= 0) {
                Queue<Integer> waitList = waitingList.get(productId);
                waitList.offer(userId);
                int position = ((LinkedList<Integer>) waitList).indexOf(userId) + 1;
                String msg = "purchaseItem(\"" + productId + "\", userId=" + userId
                        + ") → Added to waiting list, position #" + position;
                System.out.println(msg);
                return msg;
            }
            if (stock.compareAndSet(current, current - 1)) {
                int remaining = current - 1;
                purchaseLogs.get(productId).add("userId=" + userId);
                String msg = "purchaseItem(\"" + productId + "\", userId=" + userId
                        + ") → Success, " + remaining + " units remaining";
                System.out.println(msg);
                return msg;
            }
        }
    }

    public void restockProduct(String productId, int units) {
        AtomicInteger stock = inventory.get(productId);
        if (stock == null) { System.out.println("Product not found."); return; }
        stock.addAndGet(units);
        System.out.println("\nRestocked \"" + productId + "\" with " + units + " units.");
        Queue<Integer> waitList = waitingList.get(productId);
        int u = units;
        while (u-- > 0 && !waitList.isEmpty()) {
            purchaseItem(productId, waitList.poll());
        }
    }

    public void printSummary(String productId) {
        System.out.println("\n--- Summary for " + productId + " ---");
        System.out.println("Stock remaining : " + inventory.get(productId).get());
        System.out.println("Waiting list    : " + waitingList.get(productId).size() + " users");
        System.out.println("Total purchases : " + purchaseLogs.get(productId).size());
    }

    public static void main(String[] args) {
        FlashSaleInventoryManager manager = new FlashSaleInventoryManager();
        System.out.println("\n=== Problem 2: Flash Sale Inventory Manager ===\n");
        manager.checkStock("IPHONE15_256GB");
        System.out.println();
        for (int i = 1; i <= 5; i++) manager.purchaseItem("IPHONE15_256GB", 10000 + i);
        System.out.println();
        for (int i = 100; i <= 194; i++) manager.purchaseItem("IPHONE15_256GB", i);
        System.out.println();
        manager.purchaseItem("IPHONE15_256GB", 99999);
        manager.purchaseItem("IPHONE15_256GB", 88888);
        manager.printSummary("IPHONE15_256GB");
        manager.restockProduct("IPHONE15_256GB", 2);
    }
}