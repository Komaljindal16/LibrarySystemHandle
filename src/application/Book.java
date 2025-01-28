package application;

public class Book {
    private int id;
    private String title;
    private String author;
    private String genre;
    private boolean isAvailable;

    public Book(int id, String title, String author, String genre, boolean isAvailable) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.isAvailable = isAvailable;
    }

    // Getters and setters for all fields
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean isAvailable) { this.isAvailable = isAvailable; }
    
    @Override
    public String toString() {
        return title;  // Display the title of the book in the ComboBox
    }
}
