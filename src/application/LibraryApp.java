package application;
	
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class LibraryApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        TabPane tabPane = new TabPane();

        Tab bookTab = new Tab("Book Management", new BookManagementPane().getView());
        Tab borrowerTab = new Tab("Borrower Management", new BorrowerManagementPane().getView());
        Tab transactionTab = new Tab("Transaction Management", new TransactionManagementPane().getView());

        tabPane.getTabs().addAll(bookTab, borrowerTab, transactionTab);

        root.setCenter(tabPane);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Library System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
