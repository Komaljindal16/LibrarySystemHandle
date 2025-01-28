package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TransactionManagementPane {
    private ComboBox<Book> bookComboBox;  // For borrowing
    private ComboBox<Book> returnBookComboBox;  // For returning
    private ComboBox<Borrower> borrowerComboBox;
    private Button borrowButton;
    private Button returnButton;

    public VBox getView() {
        VBox vbox = new VBox();

        // Add prompt for borrowing section
        Label borrowPrompt = new Label("Select a book to borrow and a borrower:");
        borrowPrompt.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // ComboBoxes for Borrowing
        bookComboBox = new ComboBox<>();
        borrowerComboBox = new ComboBox<>();
        loadBooksAndBorrowers();

        // Add prompt for returning section
        Label returnPrompt = new Label("Select a book to return:");
        returnPrompt.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // ComboBox for Returning
        returnBookComboBox = new ComboBox<>();
        loadBooksToReturn();

        // Buttons for Borrow/Return
        borrowButton = new Button("Borrow Book");
        returnButton = new Button("Return Book");

        borrowButton.setOnAction(event -> borrowBook());
        returnButton.setOnAction(event -> returnBook());

        // Adding gap between sections
        Region gap = new Region();
        gap.setMinHeight(20);  // Adjust the height of the gap as needed

        // Add the elements to the layout
        vbox.getChildren().addAll(borrowPrompt, bookComboBox, borrowerComboBox, borrowButton, gap, returnPrompt, returnBookComboBox, returnButton);
        return vbox;
    }

    private void loadBooksAndBorrowers() {
        try (Connection connection = Database.getConnection()) {
            // Load books (available for borrowing)
            String bookQuery = "SELECT * FROM available_books WHERE isAvailable = 1";
            Statement stmt = connection.createStatement();
            ResultSet rsBooks = stmt.executeQuery(bookQuery);
            while (rsBooks.next()) {
                Book book = new Book(
                    rsBooks.getInt("id"),
                    rsBooks.getString("title"),
                    rsBooks.getString("author"),
                    rsBooks.getString("genre"),
                    rsBooks.getBoolean("isAvailable")
                );
                bookComboBox.getItems().add(book);  // Adding Book objects to ComboBox for borrowing
            }

            // Load borrowers
            String borrowerQuery = "SELECT * FROM borrowers";
            ResultSet rsBorrowers = stmt.executeQuery(borrowerQuery);
            while (rsBorrowers.next()) {
                Borrower borrower = new Borrower(
                    rsBorrowers.getInt("id"),
                    rsBorrowers.getString("name"),
                    rsBorrowers.getString("email"),
                    rsBorrowers.getString("phone")
                );
                borrowerComboBox.getItems().add(borrower);  // Adding Borrower objects to ComboBox
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadBooksToReturn() {
        try (Connection connection = Database.getConnection()) {
            // Load books that are currently borrowed
            String returnQuery = "SELECT available_books.* FROM available_books " +
                                 "JOIN transactions ON available_books.id = transactions.book_id " +
                                 "WHERE transactions.return_date IS NULL";
            Statement stmt = connection.createStatement();
            ResultSet rsBooks = stmt.executeQuery(returnQuery);
            while (rsBooks.next()) {
                Book book = new Book(
                    rsBooks.getInt("id"),
                    rsBooks.getString("title"),
                    rsBooks.getString("author"),
                    rsBooks.getString("genre"),
                    rsBooks.getBoolean("isAvailable")
                );
                returnBookComboBox.getItems().add(book);  // Adding Book objects to ComboBox for return
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void borrowBook() {
        Book selectedBook = bookComboBox.getValue();
        Borrower selectedBorrower = borrowerComboBox.getValue();
        if (selectedBook != null && selectedBorrower != null) {
            try (Connection connection = Database.getConnection()) {
                String borrowQuery = "INSERT INTO transactions (book_id, borrower_id, borrow_date) VALUES (?, ?, ?)";
                PreparedStatement stmt = connection.prepareStatement(borrowQuery);

                String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                stmt.setInt(1, selectedBook.getId());
                stmt.setInt(2, selectedBorrower.getId());
                stmt.setString(3, date);
                stmt.executeUpdate();

                String updateBookQuery = "UPDATE available_books SET isAvailable = 0 WHERE id = ?";
                PreparedStatement updateStmt = connection.prepareStatement(updateBookQuery);
                updateStmt.setInt(1, selectedBook.getId());
                updateStmt.executeUpdate();

                // Show success alert
                showAlert("Success", "Book borrowed successfully!", Alert.AlertType.INFORMATION);
                refreshBookList();  // Refresh the book list after borrowing
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to borrow the book.", Alert.AlertType.ERROR);
            }
        } else {
            showAlert("Error", "Please select both a book and a borrower.", Alert.AlertType.ERROR);
        }
    }

    private void refreshBookList() {
        bookComboBox.getItems().clear();  // Clear the current list
        returnBookComboBox.getItems().clear();  // Clear the return list
        borrowerComboBox.getItems().clear();
        loadBooksAndBorrowers();  // Reload the books and borrowers
        loadBooksToReturn();  // Reload the books to return
    }

private void returnBook() {
    Book selectedBook = returnBookComboBox.getValue();
    if (selectedBook != null) {
        try (Connection connection = Database.getConnection()) {
            // Get the borrower associated with the book
            String borrowerQuery = "SELECT borrowers.id, borrowers.name FROM borrowers " +
                                    "JOIN transactions ON borrowers.id = transactions.borrower_id " +
                                    "WHERE transactions.book_id = ? AND transactions.return_date IS NULL";
            PreparedStatement stmt = connection.prepareStatement(borrowerQuery);
            stmt.setInt(1, selectedBook.getId());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int borrowerId = rs.getInt("id");
                String borrowerName = rs.getString("name");

                // Open return book screen with borrower details
                openReturnBookScreen(selectedBook, borrowerId, borrowerName);
            } else {
                showAlert("Error", "This book is not currently borrowed.", Alert.AlertType.ERROR);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to retrieve borrower details.", Alert.AlertType.ERROR);
        }
    } else {
        showAlert("Error", "Please select a book to return.", Alert.AlertType.ERROR);
    }
}

private void openReturnBookScreen(Book book, int borrowerId, String borrowerName) {
    // Create a new scene for returning the book
    Stage returnStage = new Stage();
    VBox returnLayout = new VBox();
    Label label = new Label("Return Book: " + book.getTitle() + " by " + borrowerName);
    Button confirmReturnButton = new Button("Confirm Return");

    confirmReturnButton.setOnAction(event -> {
        try (Connection connection = Database.getConnection()) {
            String returnQuery = "UPDATE transactions SET return_date = ? WHERE book_id = ? AND borrower_id = ? AND return_date IS NULL";
            PreparedStatement stmt = connection.prepareStatement(returnQuery);
            String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            stmt.setString(1, date);
            stmt.setInt(2, book.getId());
            stmt.setInt(3, borrowerId);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Successfully updated the return date
                String updateBookQuery = "UPDATE available_books SET isAvailable = 1 WHERE id = ?";
                PreparedStatement updateStmt = connection.prepareStatement(updateBookQuery);
                updateStmt.setInt(1, book.getId());
                updateStmt.executeUpdate();

                showAlert("Success", "Book returned successfully!", Alert.AlertType.INFORMATION);
                returnStage.close();
                refreshBookList();  // Refresh the book list after returning
            } else {
                showAlert("Error", "This book may have already been returned.", Alert.AlertType.ERROR);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to return the book.", Alert.AlertType.ERROR);
        }
    });

    returnLayout.getChildren().addAll(label, confirmReturnButton);
    Scene returnScene = new Scene(returnLayout, 300, 200);
    returnStage.setScene(returnScene);
    returnStage.setTitle("Return Book");
    returnStage.show();
}

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
