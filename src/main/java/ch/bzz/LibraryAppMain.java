package ch.bzz;

import java.sql.*;
import java.util.Scanner;
import java.util.List;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LibraryAppMain {
    private static final Logger log = LoggerFactory.getLogger(LibraryAppMain.class);

    public static void main(String[] args) {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            properties.load(fis);
            log.info("Configuration file loaded successfully.");
        } catch (IOException e) {
            log.error("Error loading configuration file: {}", e.getMessage(), e);
            System.out.println("Error loading configuration file: " + e.getMessage());
            return;
        }

        String dbUrl = properties.getProperty("DB_URL");
        String dbUser = properties.getProperty("DB_USER");
        String dbPassword = properties.getProperty("DB_PASSWORD");

        if (dbUrl == null || dbUser == null || dbPassword == null) {
            log.error("Missing required database configuration properties.");
            System.out.println("Configuration file is missing required properties. Please check the config.properties file.");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("Geben Sie einen Befehl ein: ");
            String command = scanner.nextLine();
            log.info("User entered command: {}", command);

            if (command.equals("quit")) {
                running = false;
                log.info("Application is shutting down.");
                System.out.println("Programm wird beendet.");

            } else if (command.equals("help")) {
                System.out.println("Befehle: ");
                System.out.println("quit - Programm beenden");
                System.out.println("help - Hilfe anzeigen");
                System.out.println("listBooks [limit] - Bücherliste anzeigen");
                System.out.println("importBooks <FILE_PATH> - Bücher aus einer Datei importieren");

            } else if (command.startsWith("listBooks")) {
                String[] parts = command.split(" ");
                if (parts.length == 2) {
                    try {
                        int limit = Integer.parseInt(parts[1]);
                        printDBbooks(dbUrl, dbUser, dbPassword, limit);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid limit provided for listBooks: {}", parts[1]);
                        System.out.println("Invalid limit. Please provide a valid number.");
                    }
                } else {
                    printDBbooks(dbUrl, dbUser, dbPassword, Integer.MAX_VALUE);
                }

            } else if (command.startsWith("importBooks")) {
                String[] parts = command.split(" ");
                if (parts.length == 2) {
                    importBooks(parts[1], dbUrl, dbUser, dbPassword);
                } else {
                    System.out.println("Usage: importBooks <FILE_PATH>");
                }

            } else {
                log.warn("Unknown command entered: {}", command);
                System.out.println("Unbekannter Befehl: " + command);
            }
        }
        scanner.close();
    }

    private static void printDBbooks(String dbUrl, String dbUser, String dbPassword, int limit) {
        String query = "SELECT * FROM books LIMIT ?";
        try (Connection con = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement pstmt = con.prepareStatement(query)) {

            pstmt.setInt(1, limit);
            log.info("Executing query to fetch books with limit: {}", limit);

            try (ResultSet resultSet = pstmt.executeQuery()) {
                ResultSetMetaData metadata = resultSet.getMetaData();
                int columnCount = metadata.getColumnCount();

                System.out.println("Bücher in Datenbank: ");
                for (int i = 1; i <= columnCount; i++) {
                    System.out.print(metadata.getColumnName(i) + "\t");
                }
                System.out.println();

                while (resultSet.next()) {
                    for (int i = 1; i <= columnCount; i++) {
                        System.out.print(resultSet.getString(i) + "\t");
                    }
                    System.out.println();
                }
            }
        } catch (SQLException e) {
            log.error("Error fetching books from the database: {}", e.getMessage(), e);
            System.out.println("Error fetching books: " + e.getMessage());
        }
    }

    private static void importBooks(String filePath, String dbUrl, String dbUser, String dbPassword) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath));
             Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {

            log.info("Database connection established for importing books.");
            String line;
            List<Book> books = new ArrayList<>();
            if ((line = reader.readLine()) != null) {
                log.info("Skipping header line in the file: {}", filePath);
            }
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split("\t");
                if (fields.length != 5) {
                    log.warn("Invalid line format: {}", line);
                    continue;
                }

                int id = Integer.parseInt(fields[0]);
                String isbn = fields[1];
                String title = fields[2];
                String author = fields[3];
                int year = Integer.parseInt(fields[4]);

                books.add(new Book(id, isbn, title, author, year));
            }

            for (Book book : books) {
                String sql = "INSERT INTO books (id, isbn, title, author, publication_year) VALUES (?, ?, ?, ?, ?) " +
                        "ON CONFLICT (id) DO UPDATE SET isbn = EXCLUDED.isbn, title = EXCLUDED.title, " +
                        "author = EXCLUDED.author, publication_year = EXCLUDED.publication_year";
                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    pstmt.setInt(1, book.getId());
                    pstmt.setString(2, book.getIsbn());
                    pstmt.setString(3, book.getTitle());
                    pstmt.setString(4, book.getAuthor());
                    pstmt.setInt(5, book.getYear());
                    pstmt.executeUpdate();
                }
            }

            log.info("Books imported successfully from file: {}", filePath);
        } catch (Exception e) {
            log.error("Error importing books from file {}: {}", filePath, e.getMessage(), e);
            System.out.println("Error importing books: " + e.getMessage());
        }
    }
}