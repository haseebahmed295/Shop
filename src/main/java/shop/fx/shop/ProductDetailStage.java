package shop.fx.shop;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.SQLException;
import javax.imageio.ImageIO;
import javafx.scene.control.TextArea;
// Product detail page
public class ProductDetailStage extends Stage {
    private static final double WINDOW_WIDTH = 600;
    private static final double WINDOW_HEIGHT = 700;

    public ProductDetailStage(Product product, Stage parent, User loggedInUser, CartManager cartManager, DatabaseManager db , MarketplaceApp marketplaceApp) {
        setTitle(product.getName());
        setMinWidth(400);
        setMinHeight(500);
        this.getIcons().add(new Image(getClass().getResourceAsStream("store.png")));
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(0)); // Removed padding
        root.setStyle("-fx-background-color: #F5F5F5;");

        // Image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(WINDOW_WIDTH);
        imageView.setFitHeight(300);
        imageView.setPreserveRatio(false); // Set to false to fit the image to the specified height
        if (product.getImage() != null) {
            try {
                Image image = new Image(new ByteArrayInputStream(product.getImage()));
                imageView.setImage(image);
            } catch (Exception e) {
                imageView.setImage(createPlaceholderImage(WINDOW_WIDTH, 300));
            }
        } else {
            imageView.setImage(createPlaceholderImage(WINDOW_WIDTH, 300));
        }

        // Details
        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #333333;");
        nameLabel.setAlignment(Pos.CENTER);

        Label sellerLabel = new Label("Seller: " + product.getSellerName());
        sellerLabel.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 14; -fx-text-fill: #666666;");

        TextArea descArea = new TextArea(product.getDescription() != null ? product.getDescription() : "");
        descArea.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 14; -fx-text-fill: #666666; -fx-background-color: transparent;");
        descArea.setWrapText(true);
        descArea.setEditable(false);
        descArea.setPrefWidth(WINDOW_WIDTH - 40);

        VBox detailsPane = new VBox(10, nameLabel,sellerLabel, descArea);
        detailsPane.setAlignment(Pos.TOP_CENTER);
        detailsPane.setPadding(new Insets(10));
        ScrollPane detailsScroll = new ScrollPane(detailsPane);
        detailsScroll.setFitToWidth(true);
        detailsScroll.setStyle("-fx-background: #FFFFFF; -fx-border-color: transparent;");

        // Buttons
        Button addToCartButton = new Button("Add to Cart");
        addToCartButton.setStyle("-fx-background-color: #0078D4; -fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 14; -fx-background-radius: 8;");
        addToCartButton.setOnAction(e -> cartManager.addToCart(product));

        Button removeProductButton = null;
        try {
            if (db.isAdmin(loggedInUser.getId())) {
                removeProductButton = new Button("Remove Product");
                removeProductButton.setStyle("-fx-background-color: #DC3545; -fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 14; -fx-background-radius: 8;");
                removeProductButton.setOnAction(e -> {
                    // Remove product logic
                    try {
                        db.removeProduct(product.getId());
                        marketplaceApp.loadProducts();
                        close();
                    } catch (SQLException ex) {
                        // Handle exception
                    }
                });
            }
        } catch (SQLException e) {
            // Handle exception
        }

        HBox buttonsPane = new HBox(10);
        buttonsPane.setAlignment(Pos.CENTER_RIGHT);
        buttonsPane.setPadding(new Insets(10));
        buttonsPane.getChildren().add(addToCartButton);
        if (removeProductButton != null) {
            buttonsPane.getChildren().add(removeProductButton);
        }

        // Layout
        root.setTop(imageView);
        root.setCenter(detailsScroll);
        root.setBottom(buttonsPane);

        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        setScene(scene);
        initOwner(parent);
        setX(parent.getX() + (parent.getWidth() - WINDOW_WIDTH) / 2);
        setY(parent.getY() + (parent.getHeight() - WINDOW_HEIGHT) / 2);
    }

    private Image createPlaceholderImage(double width, double height) {
        BufferedImage img = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(0xE0E0E0));
        g.fillRect(0, 0, (int) width, (int) height);
        g.setColor(new Color(0x999999));
        g.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        g.drawString("No Image", (int) width / 2 - 40, (int) height / 2);
        g.dispose();
        try {
            java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();
            ImageIO.write(img, "png", os);
            return new Image(new ByteArrayInputStream(os.toByteArray()));
        } catch (IOException e) {
            return null;
        }
    }
}