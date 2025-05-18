package shop.fx.shop;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.util.function.Consumer;

public class LoginStage extends Stage {
    private final DatabaseManager db;
    private final Consumer<User> onLoginSuccess;
    private boolean isSignUpMode = false;

    public LoginStage(DatabaseManager db, Consumer<User> onLoginSuccess) {
        this.db = db;
        this.onLoginSuccess = onLoginSuccess;
        setTitle("Login");
        setResizable(false);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("root-pane");

        Label titleLabel = new Label("Login");
        titleLabel.getStyleClass().add("login-title");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.getStyleClass().add("text-field");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.getStyleClass().add("text-field");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.getStyleClass().add("text-field");
        emailField.setVisible(false);

        Button actionButton = new Button("Login");
        actionButton.getStyleClass().add("action-button");
        actionButton.setOnAction(e -> handleAction(usernameField.getText(), passwordField.getText(),
                emailField.getText()));

        Button toggleButton = new Button("Sign Up Instead");
        toggleButton.getStyleClass().add("toggle-button");
        toggleButton.setOnAction(e -> toggleMode(root, titleLabel, actionButton, toggleButton,
                emailField));

        root.getChildren().addAll(titleLabel, usernameField, passwordField, emailField,
                actionButton, toggleButton);

        Scene scene = new Scene(root, 350, 350);
        String cssPath = getClass().getResource("login-styles.css") != null ?
                getClass().getResource("login-styles.css").toExternalForm() : null;
        if (cssPath != null) {
            scene.getStylesheets().add(cssPath);
        } else {
            System.err.println("Warning: login-styles.css not found");
        }
        setScene(scene);
    }

    private void toggleMode(VBox root, Label titleLabel, Button actionButton, Button toggleButton,
                            TextField emailField) {
        isSignUpMode = !isSignUpMode;
        titleLabel.setText(isSignUpMode ? "Sign Up" : "Login");
        actionButton.setText(isSignUpMode ? "Sign Up" : "Login");
        toggleButton.setText(isSignUpMode ? "Login Instead" : "Sign Up Instead");
        emailField.setVisible(isSignUpMode);
        root.setPrefHeight(isSignUpMode ? 400 : 350);
    }

    private void handleAction(String username, String password, String email) {
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Username and password are required.");
            return;
        }

        try {
            if (isSignUpMode) {
                if (email.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Email is required for sign-up.");
                    return;
                }
                User user = new User(username, email, "Admin");
                db.addUser(user, password);
                showAlert(Alert.AlertType.INFORMATION, "Success", "User created successfully!");
                VBox root = (VBox) getScene().getRoot();
                toggleMode(root, (Label) root.getChildren().get(0),
                        (Button) root.getChildren().get(4),
                        (Button) root.getChildren().get(5),
                        (TextField) root.getChildren().get(3));
            } else {
                System.out.println("Attempting login for username: " + username);
                User user = db.authenticate(username, password);
                if (user != null) {
                    System.out.println("Login successful for user: " + user.getUsername());
                    onLoginSuccess.accept(user);
                    close();
                } else {
                    System.err.println("Authentication failed for username: " + username);
                    showAlert(Alert.AlertType.ERROR, "Error", "Invalid username or password.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error during login: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}