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

public class LibraryAppMain {

    public static void main(String[] args) throws SQLException {

        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            properties.load(fis);
        } catch (IOException e) {
            System.out.println("Error loading configuration file: " + e.getMessage());
            return;
        }

        String dbUrl = properties.getProperty("DB_URL");
        String dbUser = properties.getProperty("DB_USER");
        String dbPassword = properties.getProperty("DB_PASSWORD");

        System.out.println("Configuration loaded successfully.");
        System.out.println("DB_URL: " + dbUrl);
        System.out.println("DB_USER: " + dbUser);

        Scanner scanner = new Scanner(System.in);
        String command = "";
        boolean running = true;

        while (running) {
            System.out.println("Geben Sie einen Befehl ein: ");
            command = scanner.nextLine();

            if (command.equals("quit")) {
                running = false;
                System.out.println("Programm wird beendet.");

            } else if (command.equals("help")) {
                System.out.println("Befehle: ");
                System.out.println("quit - Programm beenden");
                System.out.println("help - Hilfe anzeigen");
                System.out.println("listBooks - Bücherliste anzeigen");
                System.out.println("importBooks <FILE_PATH> - Bücher aus einer Datei importieren");

            } else if (command.equals("listBooks")) {
                printDBbooks(dbUrl, dbUser, dbPassword);

            } else if (command.startsWith("importBooks")) {
                String[] parts = command.split(" ");
                if (parts.length == 2) {
                    importBooks(parts[1], dbUrl, dbUser, dbPassword);
                } else {
                    System.out.println("Usage: importBooks <FILE_PATH>");
                }

            } else {
                System.out.println("Unbekannter Befehl: " + command);
            }
        }
        scanner.close();
    }

    private static void printDBbooks(String dbUrl, String dbUser, String dbPassword) {
        try (Connection con = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            try (Statement stmt = con.createStatement()) {
                try (ResultSet resultSet = stmt.executeQuery("SELECT * FROM books")) {
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
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void importBooks(String filePath, String dbUrl, String dbUser, String dbPassword) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath));
             Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {

            String line;
            List<Book> books = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                String[] fields = line.split("\t");
                if (fields.length != 5) {
                    System.out.println("Invalid line format: " + line);
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
                String sql = "INSERT INTO books (id, isbn, title, author, year) VALUES (?, ?, ?, ?, ?) " +
                        "ON CONFLICT (id) DO UPDATE SET isbn = EXCLUDED.isbn, title = EXCLUDED.title, " +
                        "author = EXCLUDED.author, year = EXCLUDED.year";
                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    pstmt.setInt(1, book.getId());
                    pstmt.setString(2, book.getIsbn());
                    pstmt.setString(3, book.getTitle());
                    pstmt.setString(4, book.getAuthor());
                    pstmt.setInt(5, book.getYear());
                    pstmt.executeUpdate();
                }
            }

            System.out.println("Books imported successfully.");
        } catch (Exception e) {
            System.out.println("Error importing books: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

class Book {
    private int id;
    private String isbn;
    private String title;
    private String author;
    private int year;

    public Book(int id, String isbn, String title, String author, int year) {
        this.id = id;
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.year = year;
    }

    public int getId() {
        return id;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getYear() {
        return year;
    }
}