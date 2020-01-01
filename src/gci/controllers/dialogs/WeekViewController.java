
package gci.controllers.dialogs;

import gci.*;
import gci.controllers.*;
import gci.database.ConnectionManager;
import gci.models.Appointment;
import gci.utilities.AppointmentDAO;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.*;
import static java.time.DayOfWeek.MONDAY;
import java.time.format.*;
import java.time.temporal.*;
import static java.time.temporal.TemporalAdjusters.previousOrSame;
import java.util.*;
import java.util.logging.*;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

public class WeekViewController implements Initializable {

    @FXML private Label copyrightLabel;
    @FXML private TableView<Appointment> mainTableView;
    @FXML private TableColumn<Appointment, String> nameColumn;
    @FXML private TableColumn<Appointment, String> typeColumn;
    @FXML private TableColumn<Appointment, String> locationColumn;
    @FXML private TableColumn<Appointment, String> startColumn;
    @FXML private TableColumn<Appointment, String> endColumn;

    private static final DateTimeFormatter format = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a");
    private static final DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final AppointmentDAO dao = new AppointmentDAO();
    private static final Region modal = new Region();
    private Stage stage;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        modal.setStyle("-fx-background-color: #00000099;");
        setCopyright();
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        typeColumn.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        locationColumn.setCellValueFactory(cellData -> cellData.getValue().locationProperty());
        startColumn.setCellValueFactory(cellData -> formatTableColumn(cellData, "start"));
        endColumn.setCellValueFactory(cellData -> formatTableColumn(cellData, "end"));

        try {
            popTableView(dao.retrieveAll());
        } catch (SQLException ex) {
            ConnectionManager.showErrorMessage(ex);
            Logger.getLogger(AppointmentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static StringProperty formatTableColumn(
        TableColumn.CellDataFeatures<Appointment, String> cellData, String prop) {
        StringProperty stringprop;
        if (prop.equals("start")) {
            stringprop = cellData.getValue().startProperty();
        } else {
            stringprop = cellData.getValue().endProperty();
        }

        String str = LocalDateTime.parse(stringprop.get(), pattern).atZone(ZoneId.of("UTC"))
            .withZoneSameInstant(ZoneId.systemDefault()).format(format);

        return new SimpleStringProperty(str);
    }

    public void popTableView(ObservableList<Appointment> ob) throws SQLException {
        ObservableList<Appointment> filteredByUser = ob
            .filtered(f -> f.getUserId() == LoginController.loggedInUserId)
            .filtered(f -> LocalDate.parse(f.getStart(), pattern).query(new CalendarMonthQuery()));

        mainTableView.setItems(filteredByUser);
    }

    public void loadWeekViewScene() throws IOException {
        Stage weekViewStage = new Stage();
        Stage mainStage = App.getStage();

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(App.class.getResource("views/dialogs/week_view.fxml"));

        Parent root = loader.load();
        Parent parent = mainStage.getScene().getRoot();

        StackPane stack = new StackPane(parent, modal);
        modal.setVisible(true);
        mainStage.setScene(new Scene(stack));

        WeekViewController controller = loader.getController();
        controller.setStage(weekViewStage);
        
        weekViewStage.setScene(new Scene(root));
        weekViewStage.initModality(Modality.WINDOW_MODAL);
        weekViewStage.initOwner(mainStage);
        weekViewStage.setOnCloseRequest(e -> {
            weekViewStage.close();
            modalOff();
        });
        weekViewStage.show();
        weekViewStage.centerOnScreen();
    }
    
    private void setStage(Stage stage) {
        this.stage = stage;
    }
    
    public void modalOff() {
        modal.setVisible(false);
    }

    private void setCopyright() {
        int year = LocalDate.now().getYear();
        copyrightLabel.setText("Copyright © " + year + " Global Consulting Institution");
    }

    class CalendarMonthQuery implements TemporalQuery<Boolean> {

        @Override
        public Boolean queryFrom(TemporalAccessor temporal) {
            LocalDate date = LocalDate.from(temporal);
            LocalDate today = LocalDate.now();

            LocalDate monday = today.with(previousOrSame(MONDAY));
            LocalDate friday = monday.plusDays(5);

            return date.isAfter(monday.minusDays(1))
                && date.isBefore(friday);
        }
    }

}
