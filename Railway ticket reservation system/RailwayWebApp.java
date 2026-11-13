import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

// UNIT-II: Custom Exception
class SeatNotAvailableException extends Exception {
    public SeatNotAvailableException(String msg) { super(msg); }
}

// UNIT-I & II: Abstract Class, Inheritance & Method Overriding
abstract class Coach {
    protected double ratePerKm;
    public abstract double calculateFare(int distance, int seats);
}

class SleeperCoach extends Coach {
    public SleeperCoach() { this.ratePerKm = 0.8; }
    @Override
    public double calculateFare(int distance, int seats) { return distance * ratePerKm * seats; }
}

class ACCoach extends Coach {
    public ACCoach() { this.ratePerKm = 2.0; }
    @Override
    public double calculateFare(int distance, int seats) { return (distance * ratePerKm * seats) + 150; }
}

// UNIT-I: Encapsulation, Constructors, 'this', 'static'
class Train {
    private int trainNo;
    private String name, source, destination;
    private int availableSeats, distance;

    public Train(int trainNo, String name, String source, String destination, int availableSeats, int distance) {
        this.trainNo = trainNo;
        this.name = name;
        this.source = source;
        this.destination = destination;
        this.availableSeats = availableSeats;
        this.distance = distance;
    }

    public int getTrainNo() { return trainNo; }
    public String getName() { return name; }
    public String getSource() { return source; }
    public String getDestination() { return destination; }
    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int seats) { this.availableSeats = seats; }
    public int getDistance() { return distance; }
}

public class RailwayWebApp {
    // UNIT-I: static & final variables
    private static long pnrCounter = 100001L;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/railway_db";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root"; // Change to your MySQL password

    // UNIT-III: Collections & Generics (HashMap & ArrayList)
    private static Map<Integer, Train> trainMap = new HashMap<>();
    private static List<String> bookingHistory = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        // Load sample data into HashMap
        trainMap.put(12723, new Train(12723, "Telangana Express", "Hyderabad", "New Delhi", 50, 1670));
        trainMap.put(12759, new Train(12759, "Charminar Express", "Chennai", "Hyderabad", 40, 790));
        trainMap.put(12704, new Train(12704, "Falaknuma Express", "Secunderabad", "Howrah", 30, 1545));

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Serve Frontend Website (index.html)
        server.createContext("/", exchange -> {
            byte[] response = Files.readAllBytes(new File("index.html").toPath());
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });

        // Search Trains API (Uses UNIT-III Stream API)
        server.createContext("/trains", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            String src = getParam(query, "source");
            String dest = getParam(query, "dest");

            List<Train> list = trainMap.values().stream()
                .filter(t -> (src.isEmpty() || t.getSource().equalsIgnoreCase(src)) &&
                             (dest.isEmpty() || t.getDestination().equalsIgnoreCase(dest)))
                .collect(Collectors.toList());

            // UNIT-I: StringBuilder
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                Train t = list.get(i);
                json.append(String.format("{\"trainNo\":%d,\"name\":\"%s\",\"source\":\"%s\",\"dest\":\"%s\",\"seats\":%d,\"distance\":%d}",
                        t.getTrainNo(), t.getName(), t.getSource(), t.getDestination(), t.getAvailableSeats(), t.getDistance()));
                if (i < list.size() - 1) json.append(",");
            }
            json.append("]");
            sendResponse(exchange, json.toString());
        });

        // Book Ticket API
        server.createContext("/book", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            int trainNo = Integer.parseInt(getParam(query, "trainNo"));
            String passenger = getParam(query, "passenger");
            String coachType = getParam(query, "coach");
            int seats = Integer.parseInt(getParam(query, "seats"));

            try {
                String receipt = bookTicket(trainNo, passenger, coachType, seats);
                sendResponse(exchange, "{\"status\":\"success\",\"message\":\"" + receipt + "\"}");
            } catch (SeatNotAvailableException e) {
                sendResponse(exchange, "{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
            }
        });

        // View Bookings API
        server.createContext("/bookings", exchange -> {
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < bookingHistory.size(); i++) {
                json.append("\"").append(bookingHistory.get(i)).append("\"");
                if (i < bookingHistory.size() - 1) json.append(",");
            }
            json.append("]");
            sendResponse(exchange, json.toString());
        });

        server.start();
        System.out.println("Server running! Open http://localhost:8080 in your browser.");
    }

    // UNIT-IV: 'synchronized' keyword for Thread Safety
    private static synchronized String bookTicket(int trainNo, String passenger, String coachType, int seats) throws SeatNotAvailableException {
        Train train = trainMap.get(trainNo);
        if (train == null || train.getAvailableSeats() < seats) {
            throw new SeatNotAvailableException("Not enough seats available on Train " + trainNo);
        }

        // UNIT-II: Polymorphism
        Coach coach = coachType.equalsIgnoreCase("AC") ? new ACCoach() : new SleeperCoach();
        double fare = coach.calculateFare(train.getDistance(), seats);
        long pnr = pnrCounter++;

        // Deduct seats in memory
        train.setAvailableSeats(train.getAvailableSeats() - seats);

        // UNIT-V: JDBC Transaction Handling (Auto-skips if MySQL isn't running)
        saveToDatabase(pnr, trainNo, passenger, coachType, seats, fare);

        String record = "PNR: " + pnr + " | " + passenger + " | Train: " + train.getName() + " (" + coachType + ") | Seats: " + seats + " | Fare: Rs." + fare;
        bookingHistory.add(record);

        // UNIT-IV: File Handling (Write receipt to text file)
        try (FileWriter fw = new FileWriter("Ticket_" + pnr + ".txt")) {
            fw.write("=== RAILWAY E-TICKET ===\n" + record);
        } catch (IOException ignored) {}

        return record;
    }

    // UNIT-V: JDBC Connection, PreparedStatement, Commit & Rollback
    private static void saveToDatabase(long pnr, int trainNo, String passenger, String coach, int seats, double fare) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            conn.setAutoCommit(false); // Begin Transaction
            try (PreparedStatement ps1 = conn.prepareStatement("INSERT INTO bookings VALUES (?, ?, ?, ?, ?, ?)");
                 PreparedStatement ps2 = conn.prepareStatement("UPDATE trains SET seats = seats - ? WHERE train_no = ?")) {

                ps1.setLong(1, pnr);
                ps1.setInt(2, trainNo);
                ps1.setString(3, passenger);
                ps1.setString(4, coach);
                ps1.setInt(5, seats);
                ps1.setDouble(6, fare);
                ps1.executeUpdate();

                ps2.setInt(1, seats);
                ps2.setInt(2, trainNo);
                ps2.executeUpdate();

                conn.commit(); // Commit Transaction
            } catch (SQLException e) {
                conn.rollback(); // Rollback on failure
            }
        } catch (SQLException ignored) {
            // Runs in-memory if MySQL is not connected
        }
    }

    private static String getParam(String query, String key) {
        if (query == null) return "";
        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length == 2 && pair[0].equals(key)) {
                return URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
            }
        }
        return "";
    }

    private static void sendResponse(HttpExchange exchange, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}
