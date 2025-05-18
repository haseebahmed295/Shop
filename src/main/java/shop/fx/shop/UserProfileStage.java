package shop.fx.shop;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserProfileStage extends Stage {
    private final DatabaseManager db;
    private final User user;

    public UserProfileStage(DatabaseManager db, User user) {
        this.db = db;
        this.user = user;
        setTitle("User Profile");
        setResizable(false);
        this.getIcons().add(new Image(getClass().getResourceAsStream("store.png")));

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("root-pane");

        Label titleLabel = new Label("User Profile");
        titleLabel.getStyleClass().add("login-title");

        // GridPane for side-by-side labels
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER_LEFT);

        Label usernameLabel = new Label("Username:");
        usernameLabel.getStyleClass().add("field-label");
        Label usernameValue = new Label(user.getUsername());
        usernameValue.getStyleClass().add("value-label");

        Label emailLabel = new Label("Email:");
        emailLabel.getStyleClass().add("field-label");
        Label emailValue = new Label(user.getEmail());
        emailValue.getStyleClass().add("value-label");
        Label passwordLabel = new Label("New Password:");
        passwordLabel.getStyleClass().add("field-label");

        grid.add(usernameLabel, 0, 0);
        grid.add(usernameValue, 1, 0);
        grid.add(emailLabel, 0, 1);
        grid.add(emailValue, 1, 1);
        grid.add(passwordLabel, 0, 2);

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.getStyleClass().add("text-field");

        // Check if user has default password
        try {
            if (hasDefaultPassword()) {
                showAlert(Alert.AlertType.WARNING, "Security Warning",
                        "Your account has a temporary password. Please set a new password.");
                newPasswordField.setPromptText("Set a new password (required)");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        }

        Button saveButton = new Button("Save Changes");
        saveButton.getStyleClass().add("action-button");
        saveButton.setOnAction(e -> handleSave(newPasswordField.getText()));

        Button deleteButton = new Button("Delete Profile");
        deleteButton.getStyleClass().add("delete-button");
        deleteButton.setOnAction(e -> handleDelete());

        root.getChildren().addAll(titleLabel, grid, newPasswordField, saveButton, deleteButton);

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

    private void handleSave(String newPassword) {
        try {
            // If user has default password, require a new one
            if (hasDefaultPassword() && newPassword.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "You must set a new password.");
                return;
            }

            // Only update password if provided
            if (!newPassword.isEmpty()) {
                db.updateUser(user.getId(), user.getUsername(), user.getEmail(), "User", newPassword);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Password updated successfully!");
                close();
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Info", "No changes to save.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        }
    }

    private void handleDelete() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Profile");
        confirmAlert.setHeaderText("Are you sure you want to delete your profile?");
        confirmAlert.setContentText("This action cannot be undone.");
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    db.removeUser(user.getId());
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Profile deleted successfully.");
                    close();

                    // Reset loggedInUser and show login stage
                    Stage primaryStage = (Stage) getScene().getWindow();
                    MarketplaceApp app = (MarketplaceApp) primaryStage.getUserData();
                    if (app != null) {
                        app.setLoggedInUser(null);
                    }

                    new LoginStage(db, newUser -> {
                        if (app != null) {
                            app.setLoggedInUser(newUser);
                            primaryStage.setScene(app.createScene(app.createRootPane()));
                        }
                    }).show();
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete profile: " + e.getMessage());
                }
            }
        });
    }

    private boolean hasDefaultPassword() throws SQLException {
        String sql = "SELECT password FROM users WHERE id = ?";
        try (PreparedStatement pstmt = db.conn.prepareStatement(sql)) {
            pstmt.setInt(1, user.getId());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    String defaultPassword = hashPassword("temp");
                    return storedPassword.equals(defaultPassword);
                }
            }
        }
        throw new SQLException("User not found");
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Password hashing failed", e);
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