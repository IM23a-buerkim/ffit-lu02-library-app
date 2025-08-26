package ch.bzz;

import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

public class LibraryAppMain {
    private static final List<Books> booksList = List.of(
            new Books(1, "978-3-8362-9544-4", "Java ist auch eine Insel", "Christian Ullenboom", 2023),
            new Books(2, "978-3-658-43573-8", "Grundkurs Java", "Dietmar Abts", 2024)
    );

    public static void main(String[] args) {

        System.out.println("HelloWorld");
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
            } else if (command.equals("listBooks")) {
                System.out.println("Bücherliste: ");
                booksList.forEach(System.out::println);booksList.forEach(System.out::println);
            } else {
                System.out.println("Unbekannter Befehl: " + command);
            }
        }
        scanner.close();


    }
}


