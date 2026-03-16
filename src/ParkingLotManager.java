import java.util.*;

/**
 * Problem 8: Parking Lot Management with Open Addressing
 * Array-based hash table using linear probing for collision resolution.
 * Tracks entry/exit times for billing and generates parking statistics.
 */
public class ParkingLotManager {

    private static final int EMPTY = 0;
    private static final int OCCUPIED = 1;
    private static final int DELETED = 2;

    static class ParkingRecord {
        String licensePlate;
        int spot;
        long entryTime;

        ParkingRecord(String licensePlate, int spot) {
            this.licensePlate = licensePlate;
            this.spot = spot;
            this.entryTime = System.currentTimeMillis();
        }

        double hoursParked() {
            return (System.currentTimeMillis() - entryTime) / 3_600_000.0;
        }

        double fee(double ratePerHour) {
            return Math.max(1.0, hoursParked()) * ratePerHour;
        }
    }

    private final int capacity;
    private final int[] status;
    private final ParkingRecord[] spots;
    private int occupiedCount = 0;
    private long totalProbes = 0;
    private long totalParkings = 0;

    private static final double HOURLY_RATE = 5.50;
    private final HashMap<String, ParkingRecord> vehicleMap = new HashMap<>();
    private final HashMap<Integer, Integer> hourlyTraffic = new HashMap<>();

    public ParkingLotManager(int capacity) {
        this.capacity = capacity;
        this.status = new int[capacity];
        this.spots = new ParkingRecord[capacity];
        Arrays.fill(status, EMPTY);
    }

    private int hash(String licensePlate) {
        int hash = 0;
        for (char c : licensePlate.toCharArray()) {
            hash = (hash * 31 + c) % capacity;
        }
        return Math.abs(hash);
    }

    public int parkVehicle(String licensePlate) {
        if (occupiedCount >= capacity) {
            System.out.println("parkVehicle(\"" + licensePlate + "\") → Lot is FULL.");
            return -1;
        }

        if (vehicleMap.containsKey(licensePlate)) {
            System.out.println("parkVehicle(\"" + licensePlate + "\") → Already parked at spot #"
                    + vehicleMap.get(licensePlate).spot);
            return vehicleMap.get(licensePlate).spot;
        }

        int preferred = hash(licensePlate);
        int probes = 0;
        int idx = preferred;

        while (status[idx] == OCCUPIED) {
            probes++;
            idx = (idx + 1) % capacity;
        }

        ParkingRecord record = new ParkingRecord(licensePlate, idx);
        spots[idx] = record;
        status[idx] = OCCUPIED;
        vehicleMap.put(licensePlate, record);
        occupiedCount++;
        totalProbes += probes;
        totalParkings++;

        int hour = java.time.LocalTime.now().getHour();
        hourlyTraffic.merge(hour, 1, Integer::sum);

        System.out.printf("parkVehicle(\"%s\") → Assigned spot #%d (%d probe%s)%n",
                licensePlate, idx, probes, probes == 1 ? "" : "s");
        return idx;
    }

    public void exitVehicle(String licensePlate) {
        ParkingRecord record = vehicleMap.get(licensePlate);
        if (record == null) {
            System.out.println("exitVehicle(\"" + licensePlate + "\") → Vehicle not found.");
            return;
        }

        double hours = record.hoursParked();
        double fee = record.fee(HOURLY_RATE);

        spots[record.spot] = null;
        status[record.spot] = DELETED;
        vehicleMap.remove(licensePlate);
        occupiedCount--;

        System.out.printf("exitVehicle(\"%s\") → Spot #%d freed, Duration: %.0fh %.0fm, Fee: $%.2f%n",
                licensePlate, record.spot,
                Math.floor(hours), (hours % 1) * 60, fee);
    }

    public int findNearestAvailable() {
        for (int i = 0; i < capacity; i++) {
            if (status[i] != OCCUPIED) return i;
        }
        return -1;
    }

    public void getStatistics() {
        double occupancy = (occupiedCount * 100.0) / capacity;
        double avgProbes = totalParkings > 0 ? (totalProbes * 1.0 / totalParkings) : 0;

        String peakHour = hourlyTraffic.isEmpty() ? "N/A" :
                hourlyTraffic.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(e -> e.getKey() + ":00-" + (e.getKey() + 1) + ":00")
                        .orElse("N/A");

        System.out.println("\n--- Parking Lot Statistics ---");
        System.out.printf("  Capacity    : %d spots%n", capacity);
        System.out.printf("  Occupied    : %d spots (%.1f%%)%n", occupiedCount, occupancy);
        System.out.printf("  Available   : %d spots%n", capacity - occupiedCount);
        System.out.printf("  Avg Probes  : %.2f%n", avgProbes);
        System.out.printf("  Total Parks : %d%n", totalParkings);
        System.out.printf("  Peak Hour   : %s%n", peakHour);
        System.out.printf("  Nearest Spot: #%d%n", findNearestAvailable());
    }

    public static void main(String[] args) throws InterruptedException {
        ParkingLotManager lot = new ParkingLotManager(500);
        System.out.println("=== Problem 8: Parking Lot Management with Open Addressing ===\n");

        lot.parkVehicle("ABC-1234");
        lot.parkVehicle("ABC-1235");
        lot.parkVehicle("XYZ-9999");
        lot.parkVehicle("DEF-4567");
        lot.parkVehicle("GHI-8901");
        System.out.println();

        lot.parkVehicle("ABC-1234");
        System.out.println();

        Thread.sleep(100);
        lot.exitVehicle("ABC-1234");
        lot.exitVehicle("GHI-0000");
        System.out.println();

        lot.parkVehicle("NEW-0001");
        lot.parkVehicle("NEW-0002");

        lot.getStatistics();
    }
}