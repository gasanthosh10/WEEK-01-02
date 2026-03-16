import java.util.*;

/**
 * Problem 1: Social Media Username Availability Checker
 * Checks username availability in O(1) time using HashMap.
 * Handles 1000 concurrent checks/sec, suggests alternatives, tracks popularity.
 */
public class UsernameAvailabilityChecker {

    // username -> userId mapping
    private final HashMap<String, Integer> registeredUsers = new HashMap<>();

    // username attempt -> frequency count
    private final HashMap<String, Integer> attemptFrequency = new HashMap<>();

    public UsernameAvailabilityChecker() {
        registeredUsers.put("admin", 1);
        registeredUsers.put("john_doe", 2);
        registeredUsers.put("jane_smith", 3);
        registeredUsers.put("user123", 4);
    }

    public boolean checkAvailability(String username) {
        attemptFrequency.merge(username.toLowerCase(), 1, Integer::sum);
        boolean available = !registeredUsers.containsKey(username.toLowerCase());
        System.out.println("checkAvailability(\"" + username + "\") → "
                + (available ? "true (available)" : "false (already taken)"));
        return available;
    }

    public boolean registerUsername(String username, int userId) {
        if (checkAvailability(username)) {
            registeredUsers.put(username.toLowerCase(), userId);
            System.out.println("Registered: \"" + username + "\" for userId=" + userId);
            return true;
        }
        System.out.println("Registration failed: \"" + username + "\" is already taken.");
        return false;
    }

    public List<String> suggestAlternatives(String username) {
        List<String> suggestions = new ArrayList<>();
        String base = username.toLowerCase();
        for (int i = 1; i <= 5; i++) {
            String candidate = base + i;
            if (!registeredUsers.containsKey(candidate)) suggestions.add(candidate);
        }
        String dotVersion = base.replace("_", ".");
        if (!registeredUsers.containsKey(dotVersion)) suggestions.add(dotVersion);
        String underscoreNum = base + "_99";
        if (!registeredUsers.containsKey(underscoreNum)) suggestions.add(underscoreNum);
        System.out.println("suggestAlternatives(\"" + username + "\") → " + suggestions);
        return suggestions;
    }

    public String getMostAttempted() {
        String mostAttempted = attemptFrequency.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("No attempts yet");
        int count = attemptFrequency.getOrDefault(mostAttempted, 0);
        System.out.println("getMostAttempted() → \"" + mostAttempted + "\" (" + count + " attempts)");
        return mostAttempted;
    }

    public void printRegisteredUsers() {
        System.out.println("\n--- Registered Users ---");
        registeredUsers.forEach((name, id) ->
                System.out.println("  @" + name + " (userId=" + id + ")"));
    }

    public static void main(String[] args) {
        UsernameAvailabilityChecker checker = new UsernameAvailabilityChecker();
        System.out.println("=== Problem 1: Social Media Username Availability Checker ===\n");
        checker.checkAvailability("john_doe");
        checker.checkAvailability("jane_smith");
        checker.checkAvailability("newuser99");
        System.out.println();
        checker.suggestAlternatives("john_doe");
        System.out.println();
        checker.registerUsername("newuser99", 100);
        checker.registerUsername("john_doe", 101);
        System.out.println();
        for (int i = 0; i < 10; i++) checker.checkAvailability("admin");
        checker.getMostAttempted();
        checker.printRegisteredUsers();
    }
}