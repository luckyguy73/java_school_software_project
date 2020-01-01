package gci.utilities;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Dialog;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.Window;

public class ViewHelpers {

    public static void closeDialogWindow(Node node) {
        Stage window = (Stage) node.getScene().getWindow();
        window.close();
    }

    public static Dialog getDialog(String resource) throws IOException {

        Parent root = loadFromViews(resource);
        Dialog dialog = new Dialog();
        Window window = dialog.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event -> window.hide());
        dialog.getDialogPane().setContent(root);
        dialog.initOwner(window);
        return dialog;
    }

    public static void loadBorderCenter(BorderPane borderPane, String resource) throws IOException {
        Parent dashboard = loadFromViews(resource);
        borderPane.setCenter(dashboard);
    }

    public static Parent loadFromViews(String resource) throws IOException {
        Parent root = FXMLLoader.load(new ViewHelpers().getClass()
            .getResource("/gci/views/" + resource + ".fxml"));
        return root;
    }

}
