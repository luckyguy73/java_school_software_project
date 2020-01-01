
package gci;

import gci.controllers.AppController;
import gci.database.ConnectionManager;
import static gci.utilities.ViewHelpers.loadFromViews;
import java.io.IOException;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.stage.Stage;

public class App extends Application {

    private static final String LOGIN_VIEW = "views/dialogs/login.fxml";
    private static final String RESOURCE_PATH = "gci.assets.languages/rb";
    private static Stage mainStage;
    private ResourceBundle rb;
    private Parent root;
    private Scene login;


    public static void main(String[] args) throws IOException {

        //uncomment line directly below and import java.util.Locale; to view Login form in Spanish
        //Locale.setDefault(new Locale("es", "MX"));
        
        ConnectionManager.setProperties();
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        this.mainStage = stage;
        getResourceBundle();
        initializeLoginScene();
        mainStage.show();
    }

    private void getResourceBundle() {
        this.rb = ResourceBundle.getBundle(RESOURCE_PATH);
    }

    private void initializeLoginScene() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource(LOGIN_VIEW));
        loader.setResources(rb);
        root = loader.load();
        login = new Scene(root);
        mainStage.setScene(login);
    }

    public static void loadApp() throws IOException {
        mainStage.setScene(new Scene(loadFromViews("app")));
        mainStage.show();
        mainStage.centerOnScreen();
        AppController controller = new AppController();
        controller.checkForAppointmentWithinFifteenMinutes();
    }
    
    public static Stage getStage() {
        return mainStage;
    }
}
