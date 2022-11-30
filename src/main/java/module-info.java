module com.example.paint {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;

    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires java.desktop;

    opens by.Iren137.paint to javafx.fxml;
    exports by.Iren137.paint;
}