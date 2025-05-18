module shop.fx.shop {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.kordamp.ikonli.javafx;
    requires java.desktop;
    requires java.sql;
    requires org.kordamp.ikonli.fontawesome;
    requires org.kordamp.ikonli.feather;

    opens shop.fx.shop to javafx.fxml;
    exports shop.fx.shop;
}