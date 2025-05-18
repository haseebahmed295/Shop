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
import javax.imageio.ImageIO;
import javafx.scene.control.TextArea;
// Product detail page
public class ProductDetailStage extends Stage {
    private static final double WINDOW_WIDTH = 600;
    private static final double WINDOW_HEIGHT = 700;

    public ProductDetailStage(Product product, Stage parent) {
        setTitle(product.getName());
        setMinWidth(400);
        setMinHeight(500);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #F5F5F5;");

        // Back button
        Button backButton = new Button("Back");
        backButton.setStyle("-fx-background-color: #0078D4; -fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 14; -fx-background-radius: 8;");
        backButton.getStyleClass().add("rounded-button");
        backButton.setOnMouseEntered(e -> backButton.setStyle("-fx-background-color: #005BA1; -fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 14; -fx-background-radius: 8;"));
        backButton.setOnMouseExited(e -> backButton.setStyle("-fx-background-color: #0078D4; -fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 14; -fx-background-radius: 8;"));
        backButton.setOnAction(e -> close());

        HBox backPane = new HBox(backButton);
        backPane.setPadding(new Insets(5));
        backPane.setStyle("-fx-background-color: #F5F5F5;");
        root.setTop(backPane);

        // Content
        VBox contentPane = new VBox(10);
        contentPane.getStyleClass().add("product-frame");
        contentPane.setAlignment(Pos.TOP_CENTER);
        contentPane.setPadding(new Insets(10));

        // Image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(WINDOW_WIDTH - 40);
        imageView.setFitHeight(300);
        imageView.setPreserveRatio(true);
        if (product.getImage() != null) {
            try {
                Image image = new Image(new ByteArrayInputStream(product.getImage()));
                imageView.setImage(image);
            } catch (Exception e) {
                imageView.setImage(createPlaceholderImage(WINDOW_WIDTH - 40, 300));
            }
        } else {
            imageView.setImage(createPlaceholderImage(WINDOW_WIDTH - 40, 300));
        }

        // Details
        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #333333;");
        nameLabel.setAlignment(Pos.CENTER);

        TextArea descArea = new TextArea(product.getDescription() != null ? product.getDescription() : "");
        descArea.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 14; -fx-text-fill: #666666; -fx-background-color: transparent;");
        descArea.setWrapText(true);
        descArea.setEditable(false);
        descArea.setPrefWidth(WINDOW_WIDTH - 40);

        VBox detailsPane = new VBox(10, nameLabel, descArea);
        detailsPane.setAlignment(Pos.TOP_CENTER);
        ScrollPane detailsScroll = new ScrollPane(detailsPane);
        detailsScroll.setFitToWidth(true);
        detailsScroll.setStyle("-fx-background: #FFFFFF; -fx-border-color: transparent;");

        contentPane.getChildren().addAll(imageView, detailsScroll);
        root.setCenter(contentPane);

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