package shop.fx.shop;
import javafx.scene.control.*;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;
import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

// Main JavaFX application for the marketplace
public class MarketplaceApp extends Application {
    private DatabaseManager db;
    private GridPane productsGrid;
    private TextField searchField;
    private CartManager cartManager;

    private static final double PRODUCT_WIDTH = 250;
    private static final double PRODUCT_HEIGHT = 300;
    private static final double HORIZONTAL_GAP = 10;
    private static final double VERTICAL_GAP = 10;
    private static final double LEFT_PADDING = 20;

    @Override
    public void start(Stage primaryStage) {
        initializeDatabase();
        if (db == null) {
            showErrorAlert("Failed to initialize database");
            return;
        }

        cartManager = new CartManager();

        primaryStage.setTitle("Marketplace");
        primaryStage.setMinWidth(680);
        primaryStage.setMinHeight(420);

        BorderPane root = createRootPane();
        Scene scene = createScene(root);
        setupResizeListener(primaryStage);
        primaryStage.setScene(scene);
        primaryStage.show();

        loadProducts();
    }

    // Initialize database connection
    private void initializeDatabase() {
        try {
            db = new DatabaseManager();
        } catch (RuntimeException e) {
            db = null;
        }
    }

    // Create the root BorderPane
    private BorderPane createRootPane() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.getStyleClass().add("root-pane");
        root.setTop(createHeaderPane());
        root.setCenter(createProductsScrollPane());
        return root;
    }

    // Create the header with search and cart button
    private HBox createHeaderPane() {
        HBox headerPane = new HBox(10);
        headerPane.getStyleClass().add("header-pane");
        headerPane.setPadding(new Insets(15));
        headerPane.setAlignment(Pos.CENTER_LEFT);

        HBox searchFrame = new HBox(5); // Reduced spacing for tighter layout
        searchFrame.getStyleClass().add("search-frame");
        searchFrame.setAlignment(Pos.CENTER_LEFT);

        searchField = new TextField();
        searchField.setPromptText("Search products...");
        searchField.getStyleClass().add("search-field");
        searchField.setOnAction(e -> searchProducts());
        HBox.setHgrow(searchField, Priority.ALWAYS); // Search field expands

        Button refreshButton = createStyledButton("", FontAwesome.SEARCH, "refresh-button");
        refreshButton.setOnAction(e -> searchProducts());
        HBox.setMargin(refreshButton, new Insets(0, 0, 0, 5)); // Small margin for spacing

        searchFrame.getChildren().addAll(searchField, refreshButton);

        Button cartButton = createStyledButton("", FontAwesome.SHOPPING_CART, "cart-button");
        cartButton.setOnAction(e -> cartManager.showCartStage());

        headerPane.getChildren().addAll(searchFrame, cartButton);
        HBox.setHgrow(searchFrame, Priority.ALWAYS);
        return headerPane;
    }

    // Create a styled button with text, optional icon, and style class
    private Button createStyledButton(String text, FontAwesome icon, String styleClass) {
        Button button = new Button(text);
        if (icon != null) {
            FontIcon fontIcon = new FontIcon(icon);
            fontIcon.getStyleClass().add("button-icon");
            button.setGraphic(fontIcon);
        }
        button.getStyleClass().addAll("rounded-button", styleClass);
        return button;
    }

    // Create the ScrollPane containing the products grid
    private ScrollPane createProductsScrollPane() {
        productsGrid = new GridPane();
        productsGrid.setHgap(HORIZONTAL_GAP);
        productsGrid.setVgap(VERTICAL_GAP);
        productsGrid.setPadding(new Insets(10, 10, 10, 25));
        productsGrid.getStyleClass().add("products-grid");

        ScrollPane scrollPane = new ScrollPane(productsGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("products-scroll-pane");
        return scrollPane;
    }

    // Create the main scene with CSS
    private Scene createScene(BorderPane root) {
        Scene scene = new Scene(root, 1000, 600);
        String cssPath = getClass().getResource("styles.css") != null ?
                getClass().getResource("styles.css").toExternalForm() : null;
        if (cssPath != null) {
            scene.getStylesheets().add(cssPath);
        } else {
            System.err.println("Warning: styles.css not found");
        }
        return scene;
    }

    // Setup window resize listener
    private void setupResizeListener(Stage stage) {
        ChangeListener<Number> resizeListener = (obs, oldVal, newVal) -> loadProducts();
        stage.widthProperty().addListener(resizeListener);
    }

    // Create a product pane for display
    private VBox createProductPane(Product product) {
        VBox pane = new VBox(10);
        pane.setPrefSize(PRODUCT_WIDTH, PRODUCT_HEIGHT);
        pane.setMaxSize(PRODUCT_WIDTH, PRODUCT_HEIGHT);
        pane.getStyleClass().add("product-frame");
        pane.setAlignment(Pos.TOP_CENTER);

        ImageView imageView = createProductImage(product);
        Label nameLabel = createProductLabel(product.getName(), "product-name");
        Label descLabel = createProductDescriptionLabel(product.getDescription());
        Label priceLabel = createProductLabel(String.format("$%.2f", product.getPrice()), "product-price");
        Button addToCartButton = createStyledButton("", FontAwesome.PLUS, "add-to-cart-button");
        addToCartButton.setOnAction(e -> cartManager.addToCart(product));

        HBox priceAndButtonBox = new HBox(10);
        priceAndButtonBox.setAlignment(Pos.CENTER);
        priceAndButtonBox.getChildren().addAll(addToCartButton,priceLabel);

        pane.getChildren().addAll(imageView, nameLabel, descLabel, priceAndButtonBox);
        setupProductPaneInteractions(pane, product);
        return pane;
    }

    // Create product image
    private ImageView createProductImage(Product product) {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(280);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(true);
        if (product.getImage() != null) {
            try {
                Image image = new Image(new ByteArrayInputStream(product.getImage()));
                imageView.setImage(image);
            } catch (Exception e) {
                imageView.setImage(createPlaceholderImage(280, 180));
            }
        } else {
            imageView.setImage(createPlaceholderImage(280, 180));
        }
        return imageView;
    }

    // Create a styled label
    private Label createProductLabel(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        return label;
    }

    // Create product description label with truncation
    private Label createProductDescriptionLabel(String description) {
        String desc = description != null ? description : "";
        if (desc.length() > 50) desc = desc.substring(0, 47) + "...";
        return createProductLabel(desc, "product-description");
    }

    // Setup product pane hover and click interactions
    private void setupProductPaneInteractions(VBox pane, Product product) {
        pane.setOnMouseEntered(e -> pane.getStyleClass().add("product-frame-hover"));
        pane.setOnMouseExited(e -> pane.getStyleClass().remove("product-frame-hover"));
        pane.setOnMouseClicked(e -> new ProductDetailStage(product, (Stage) pane.getScene().getWindow()).show());
    }

    // Load products from database
    private void loadProducts() {
        Task<List<Product>> task = new Task<>() {
            @Override
            protected List<Product> call() throws SQLException {
                return db.getAllProducts();
            }
        };
        task.setOnSucceeded(e -> layoutProducts(task.getValue()));
        task.setOnFailed(e -> showErrorAlert("Error loading products: " + task.getException().getMessage()));
        new Thread(task).start();
    }

    // Search products based on query
    private void searchProducts() {
        System.out.println("Searching for: " + searchField.getText());
        try {
            String query = searchField.getText().trim();
            if (query.isEmpty()) {
                loadProducts();
            } else {
                List<Product> products = db.searchProducts(query);
                layoutProducts(products);
            }
        } catch (SQLException e) {
            showErrorAlert("Error searching products: " + e.getMessage());
        }
    }

    // Layout products in the grid
    private void layoutProducts(List<Product> products) {
        productsGrid.getChildren().clear();
        double paneWidth = productsGrid.getScene() != null ? productsGrid.getScene().getWidth() - 40 : 900;
        int columns = Math.max(1, (int) ((paneWidth - LEFT_PADDING) / (PRODUCT_WIDTH + HORIZONTAL_GAP)));
        int col = 0;
        int row = 0;

        for (Product product : products) {
            productsGrid.add(createProductPane(product), col, row);
            col++;
            if (col >= columns) {
                col = 0;
                row++;
            }
        }
    }

    // Create placeholder image
    private Image createPlaceholderImage(double width, double height) {
        BufferedImage img = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(0xE0E0E0));
        g.fillRect(0, 0, (int) width, (int) height);
        g.setColor(new Color(0x999999));
        g.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        g.drawString("No Image", (int) width / 2 - 40, (int) height / 2);
        g.dispose();
        return new Image(imgToInputStream(img));
    }

    // Convert BufferedImage to InputStream
    private ByteArrayInputStream imgToInputStream(BufferedImage img) {
        try {
            java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();
            ImageIO.write(img, "png", os);
            return new ByteArrayInputStream(os.toByteArray());
        } catch (IOException e) {
            return null;
        }
    }

    // Show error alert
    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}