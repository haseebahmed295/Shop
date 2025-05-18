package shop.fx.shop;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.feather.Feather;
import java.util.ArrayList;
import java.util.List;

// Manages the shopping cart functionality
public class CartManager {
    private List<Product> cart = new ArrayList<>();

    // Add product to cart
    public void addToCart(Product product) {
        cart.add(product);
        showInfoAlert("Added to Cart", product.getName() + " has been added to your cart.");
    }

    // Remove product from cart
    public void removeFromCart(Product product) {
        cart.remove(product);
    }

    // Get the current cart
    public List<Product> getCart() {
        return new ArrayList<>(cart);
    }

    // Show cart stage
    public void showCartStage() {
        Stage cartStage = new Stage();
        cartStage.getIcons().add(new Image(getClass().getResourceAsStream("store.png")));
        cartStage.setTitle("Shopping Cart");
        cartStage.setMinWidth(600);

        BorderPane cartRoot = new BorderPane();
        cartRoot.getStyleClass().add("root-pane");
        cartRoot.setPadding(new Insets(10));

        VBox cartItemsPane = new VBox(10);
        cartItemsPane.setPadding(new Insets(10));
        cartItemsPane.getStyleClass().add("cart-items-pane");

        for (Product product : cart) {
            cartItemsPane.getChildren().add(createCartItemPane(product));
        }

        ScrollPane cartScrollPane = new ScrollPane(cartItemsPane);
        cartScrollPane.setFitToWidth(true);
        cartRoot.setCenter(cartScrollPane);

        // Bottom HBox for Buy button and Total price
        HBox bottomBox = new HBox(10);
        bottomBox.setPadding(new Insets(10));
        bottomBox.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button buyButton = new Button();
        FontIcon buyIcon = new FontIcon(Feather.CHECK);
        buyIcon.getStyleClass().add("button-icon");
        buyButton.setGraphic(buyIcon);
        buyButton.getStyleClass().addAll("rounded-button", "buy-button");
        buyButton.setOnAction(e -> showInfoAlert("Purchase", "Thank you for your purchase!"));

        Label totalLabel = new Label(String.format("Total: $%.2f", cart.stream().mapToDouble(Product::getPrice).sum()));
        totalLabel.getStyleClass().add("cart-total");

        bottomBox.getChildren().addAll(spacer, totalLabel, buyButton);
        cartRoot.setBottom(bottomBox);

        Scene cartScene = new Scene(cartRoot, 600, 400);
        String cssPath = CartManager.class.getResource("cart-styles.css") != null ?
                CartManager.class.getResource("cart-styles.css").toExternalForm() : null;
        if (cssPath != null) {
            cartScene.getStylesheets().add(cssPath);
        } else {
            System.err.println("Warning: cart-styles.css not found");
        }
        cartStage.setScene(cartScene);
        cartStage.show();
    }

    // Create a pane for a cart item
    private HBox createCartItemPane(Product product) {
        HBox itemPane = new HBox(10);
        itemPane.getStyleClass().add("cart-item");
        itemPane.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(product.getName());
        nameLabel.getStyleClass().add("cart-item-name");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label priceLabel = new Label(String.format("$%.2f", product.getPrice()));
        priceLabel.getStyleClass().add("cart-item-price");

        Button removeButton = new Button();
        FontIcon removeIcon = new FontIcon(Feather.TRASH_2);
        removeIcon.getStyleClass().add("button-icon");
        removeButton.setGraphic(removeIcon);
        removeButton.getStyleClass().addAll("rounded-button", "remove-button");
        removeButton.setOnAction(e -> {
            removeFromCart(product);
            showInfoAlert("Removed from Cart", product.getName() + " has been removed from your cart.");
            showCartStage();
        });

        itemPane.getChildren().addAll(nameLabel, spacer, priceLabel, removeButton);
        return itemPane;
    }

    // Show info alert
    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}