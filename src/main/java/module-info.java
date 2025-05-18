module shop.fx.shop {
    requires javafx.fxml;
    requires org.kordamp.ikonli.javafx;
    requires java.desktop;
    requires org.kordamp.ikonli.fontawesome;
    requires org.kordamp.ikonli.feather;
    requires atlantafx.base;
    requires org.xerial.sqlitejdbc;

    opens shop.fx.shop to javafx.fxml;
    exports shop.fx.shop;
}