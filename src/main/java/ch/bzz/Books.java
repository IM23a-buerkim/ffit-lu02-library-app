package ch.bzz;

public class Books {
    private int id;
    private String isbn;
    private String title;
    private String author;
    private int year;

    public Books(int id, String isbn, String title, String author, int year) {
        this.id = id;
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.year = year;
    }

    @Override
    public String toString() {
        return "Books{" +
                "id: " + id +
                ", isbn: " + isbn + '\'' +
                ", title: " + title + '\'' +
                ", author: " + author + '\'' +
                 ", year: " + year +
                '}';
    }
}
