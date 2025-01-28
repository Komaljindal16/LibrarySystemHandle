package application;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.sql.*;

public class BookManagementPane {
    private TableView<Book> table;
    private ObservableList<Book> bookData;

    public VBox getView() {
        VBox vbox = new VBox();
        table = new TableView<>();
        bookData = FXCollections.observableArrayList();

        // Table columns for Book
        TableColumn<Book, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getTitle()));

        TableColumn<Book, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getAuthor()));

        TableColumn<Book, String> genreCol = new TableColumn<>("Genre");
        genreCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getGenre()));

        TableColumn<Book, Boolean> availabilityCol = new TableColumn<>("Available");
        availabilityCol.setCellValueFactory(param -> new SimpleBooleanProperty(param.getValue().isAvailable()).asObject());

        table.getColumns().addAll(titleCol, authorCol, genreCol, availabilityCol);
        table.setItems(bookData);

        // Buttons for CRUD operations
        Button addButton = new Button("Add Book");
        Button deleteButton = new Button("Delete Book");

        addButton.setOnAction(event -> openAddBookDialog());
        deleteButton.setOnAction(event -> deleteSelectedBook());

        vbox.getChildren().addAll(table, addButton, deleteButton);
        loadBooks();

        return vbox;
    }

    private void loadBooks() {
        try (Connection connection = Database.getConnection()) {
            String query = "SELECT * FROM available_books";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                Book book = new Book(rs.getInt("id"), rs.getString("title"), rs.getString("author"), rs.getString("genre"), rs.getBoolean("isAvailable"));
                bookData.add(book);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Open dialog to add a new book
   private void openAddBookDialog() {
    Stage dialog = new Stage();
    dialog.setTitle("Add Book");

    // Create input fields with labels
    Label titleLabel = new Label("Book Title:");
    TextField titleField = new TextField();
    titleField.setPromptText("Enter Book Title");

    Label authorLabel = new Label("Author:");
    TextField authorField = new TextField();
    authorField.setPromptText("Enter Author");

    Label genreLabel = new Label("Genre:");
    TextField genreField = new TextField();
    genreField.setPromptText("Enter Genre");

    Label availabilityLabel = new Label("Availability:");
    CheckBox availabilityCheckBox = new CheckBox("Available");

    // Save button
    Button saveButton = new Button("Save");
    saveButton.setOnAction(event -> {
        String title = titleField.getText();
        String author = authorField.getText();
        String genre = genreField.getText();
        boolean isAvailable = availabilityCheckBox.isSelected();

        if (!title.isEmpty() && !author.isEmpty() && !genre.isEmpty()) {
            try (Connection connection = Database.getConnection()) {
                String query = "INSERT INTO available_books (title, author, genre, isAvailable) VALUES (?, ?, ?, ?)";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setString(1, title);
                stmt.setString(2, author);
                stmt.setString(3, genre);
                stmt.setBoolean(4, isAvailable);
                stmt.executeUpdate();

                // Add new book to the table view
                Book newBook = new Book(-1, title, author, genre, isAvailable);  // Temporarily set ID to -1, as it is auto-generated
                bookData.add(newBook);
                
                // Show success alert
                showAlert("Book Added", "The book has been successfully added.");
                dialog.close();
            } catch (SQLException e) {
                showAlert("Error", "Failed to add the book.");
                e.printStackTrace();
            }
        } else {
            showAlert("Input Error", "Please fill in all fields.");
        }
    });

    // Arrange labels, text fields, and button in a VBox
    VBox dialogVBox = new VBox(10, titleLabel, titleField, authorLabel, authorField, genreLabel, genreField, availabilityLabel, availabilityCheckBox, saveButton);
    Scene dialogScene = new Scene(dialogVBox, 300, 250);
    dialog.setScene(dialogScene);
    dialog.show();
}

    // Delete the selected book
   // Delete the selected book
private void deleteSelectedBook() {
    Book selectedBook = table.getSelectionModel().getSelectedItem();
    if (selectedBook != null) {
        try (Connection connection = Database.getConnection()) {
            String query = "DELETE FROM available_books WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, selectedBook.getId());
            stmt.executeUpdate();

            // Remove the selected book from the table view
            bookData.remove(selectedBook);
            
            // Show success alert
            showAlert("Book Deleted", "The book has been successfully deleted.");
        } catch (SQLException e) {
            showAlert("Error", "Failed to delete the book.");
            e.printStackTrace();
        }
    } else {
        // Show alert if no book is selected
        showAlert("No Book Selected", "Please select a book to delete.");
    }
}

//Helper function to show an alert dialog
private void showAlert(String title, String message) {
 Alert alert = new Alert(Alert.AlertType.INFORMATION);
 alert.setTitle(title);
 alert.setHeaderText(null);
 alert.setContentText(message);
 alert.showAndWait();
}

}
