package application;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;

public class BorrowerManagementPane {
    private TableView<Borrower> table;
    private ObservableList<Borrower> borrowerData;

    public VBox getView() {
        VBox vbox = new VBox();
        table = new TableView<>();
        borrowerData = FXCollections.observableArrayList();

        // Table columns for Borrower
        TableColumn<Borrower, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getName()));

        TableColumn<Borrower, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getEmail()));

        TableColumn<Borrower, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getPhone()));

        table.getColumns().addAll(nameCol, emailCol, phoneCol);
        table.setItems(borrowerData);

        // Buttons for CRUD operations
        Button addButton = new Button("Add Borrower");
        Button deleteButton = new Button("Delete Borrower");

        addButton.setOnAction(event -> openAddBorrowerDialog());
        deleteButton.setOnAction(event -> deleteSelectedBorrower());

        vbox.getChildren().addAll(table, addButton, deleteButton);
        loadBorrowers();

        return vbox;
    }

    private void loadBorrowers() {
        try (Connection connection = Database.getConnection()) {
            String query = "SELECT * FROM borrowers";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                Borrower borrower = new Borrower(rs.getInt("id"), rs.getString("name"), rs.getString("email"), rs.getString("phone"));
                borrowerData.add(borrower);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

private void openAddBorrowerDialog() {
    // Create a new stage for the form
    Stage addBorrowerStage = new Stage();
    addBorrowerStage.setTitle("Add New Borrower");

    // Create the form (VBox) for entering borrower details
    VBox formVBox = new VBox(10);

    // Create labels and text fields for the borrower's name, email, and phone
    Label nameLabel = new Label("Name:");
    TextField nameField = new TextField();
    nameField.setPromptText("Enter name");

    Label emailLabel = new Label("Email:");
    TextField emailField = new TextField();
    emailField.setPromptText("Enter email");

    Label phoneLabel = new Label("Phone:");
    TextField phoneField = new TextField();
    phoneField.setPromptText("Enter phone");

    // Create a button to submit the form
    Button submitButton = new Button("Add Borrower");

    submitButton.setOnAction(event -> {
        String name = nameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();

        // Validate inputs
        if (!name.isEmpty() && !email.isEmpty() && !phone.isEmpty()) {
            addBorrowerToDatabase(name, email, phone);
            showAlert("Success", "Borrower added successfully.");
            addBorrowerStage.close(); // Close the form after adding the borrower
        } else {
            // Show an error message if fields are empty
            showAlert("Input Error", "Please fill in all fields.");
        }
    });

    // Add the labels and text fields to the form VBox
    formVBox.getChildren().addAll(nameLabel, nameField, emailLabel, emailField, phoneLabel, phoneField, submitButton);

    // Set the scene and show the form stage
    Scene formScene = new Scene(formVBox, 300, 250);
    addBorrowerStage.setScene(formScene);
    addBorrowerStage.show();
}

   private void addBorrowerToDatabase(String name, String email, String phone) {
    try (Connection connection = Database.getConnection()) {
        String query = "CALL AddBorrower(?, ?, ?)";
        CallableStatement stmt = connection.prepareCall(query);
        stmt.setString(1, name);
        stmt.setString(2, email);
        stmt.setString(3, phone);
        stmt.execute();
        borrowerData.add(new Borrower(0, name, email, phone)); // assuming auto-incremented id
    } catch (SQLException e) {
        showAlert("Database Error", "An error occurred while adding the borrower.");
        e.printStackTrace();
    }
}
   private void deleteSelectedBorrower() {
    Borrower selectedBorrower = table.getSelectionModel().getSelectedItem();
    if (selectedBorrower != null) {
        try (Connection connection = Database.getConnection()) {
            String query = "CALL DeleteBorrower(?)";
            CallableStatement stmt = connection.prepareCall(query);
            stmt.setInt(1, selectedBorrower.getId());
            stmt.execute();
            borrowerData.remove(selectedBorrower);

            // Show success alert
            showAlert("Success", "Borrower deleted successfully.");
        } catch (SQLException e) {
            showAlert("Database Error", "An error occurred while deleting the borrower.");
            e.printStackTrace();
        }
    } else {
        // Show alert if no borrower is selected
        showAlert("No Borrower Selected", "Please select a borrower to delete.");
    }
}
   
// Helper function to show an alert dialog
private void showAlert(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
}

}
