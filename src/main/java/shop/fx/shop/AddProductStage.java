package shop.fx.shop;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.sql.SQLException;

public class AddProductStage extends Stage {
    private final DatabaseManager db;
    private final User user;
    private final MarketplaceApp marketplaceApp;

    public AddProductStage(DatabaseManager db, User user, Stage parent , MarketplaceApp marketplaceApp) {
        this.db = db;
        this.user = user;
        this.marketplaceApp = marketplaceApp;
        setTitle("Add New Product");
        setResizable(false);
        this.getIcons().add(new Image(getClass().getResourceAsStream("store.png")));
        // Check if user is admin
        try {
            if (!db.isAdmin(user.getId())) {
                showAlert(Alert.AlertType.ERROR, "Access Denied", "Only administrators can add products.");
                close();
                return;
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
            close();
            return;
        }

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("root-pane");

        Label titleLabel = new Label("Add New Product");
        titleLabel.getStyleClass().add("add-product-title");

        TextField nameField = new TextField();
        nameField.setPromptText("Product Name");
        nameField.getStyleClass().add("text-field");

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Product Description");
        descriptionArea.setWrapText(true);
        descriptionArea.getStyleClass().add("text-area");
        descriptionArea.setPrefRowCount(4);

        TextField sellerField = new TextField();
        sellerField.setPromptText("Seller Name");
        sellerField.setEditable(true);
        sellerField.getStyleClass().add("text-field");

        TextField priceField = new TextField();
        priceField.setPromptText("Price");
        priceField.getStyleClass().add("text-field");

        Button imageButton = new Button("Choose Image");
        imageButton.getStyleClass().add("action-button");
        Label imageLabel = new Label("No image selected");
        imageLabel.getStyleClass().add("image-label");
        File[] selectedFile = new File[1]; // Store selected file

        imageButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
            );
            File file = fileChooser.showOpenDialog(this);
            if (file != null) {
                selectedFile[0] = file;
                imageLabel.setText(file.getName());
            }
        });

        Button addButton = new Button("Add Product");
        addButton.getStyleClass().add("action-button");
        addButton.setOnAction(e -> handleAddProduct(
                nameField.getText(),
                descriptionArea.getText(),
                priceField.getText(),
                sellerField.getText(),
                selectedFile[0]
        ));

        root.getChildren().addAll(
                titleLabel,
                nameField,
                descriptionArea,
                sellerField,
                priceField,
                imageButton,
                imageLabel,
                addButton
        );

        Scene scene = new Scene(root, 400, 500);
        String cssPath = getClass().getResource("add-product-styles.css") != null ?
                getClass().getResource("add-product-styles.css").toExternalForm() : null;
        if (cssPath != null) {
            scene.getStylesheets().add(cssPath);
        } else {
            System.err.println("Warning: add-product-styles.css not found");
        }

        setScene(scene);
        initOwner(parent);
        setX(parent.getX() + (parent.getWidth() - 400) / 2);
        setY(parent.getY() + (parent.getHeight() - 500) / 2);
    }

    private void handleAddProduct(String name, String description, String price,String seller, File imageFile) {
        // Validate inputs
        if (name.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Product name is required.");
            return;
        }

        double priceValue;
        try {
            priceValue = Double.parseDouble(price);
            if (priceValue <= 0) {
                showAlert(Alert.AlertType.ERROR, "Error", "Price must be a positive number.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid price format.");
            return;
        }


        // Create product
        try {

            Product product = new Product(
                    name,
                    description.isEmpty() ? null : description,
                    priceValue,
                    seller,
                    imageFile.getPath()
            );

            db.addProduct(product);
            marketplaceApp.loadProducts();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Product added successfully!");
            close();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to read image file: " + e.getMessage());
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