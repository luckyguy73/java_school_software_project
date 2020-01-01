package gci.controllers;

import gci.controllers.dialogs.*;
import gci.database.ConnectionManager;
import gci.models.Appointment;
import gci.utilities.AppointmentDAO;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.Collectors;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.scene.control.*;

public class AppointmentController implements Initializable {

    @FXML private Button addButton;
    @FXML private Button modifyButton;
    @FXML private Button deleteButton;
    @FXML private Button searchButton;
    @FXML private Label titleLabel;
    @FXML private TextField searchTextField;
    @FXML private TableView<Appointment> mainTableView;
    @FXML private TableColumn<Appointment, String> nameColumn;
    @FXML private TableColumn<Appointment, String> typeColumn;
    @FXML private TableColumn<Appointment, String> locationColumn;
    @FXML private TableColumn<Appointment, String> startColumn;
    @FXML private TableColumn<Appointment, String> endColumn;

    private static final DateTimeFormatter format = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a");
    private static final DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final AppointmentDAO dao = new AppointmentDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
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
            .filtered(f -> f.getUserId() == LoginController.loggedInUserId);
        
        mainTableView.setItems(filteredByUser);
    }

    @FXML
    private void handleAddButton(ActionEvent event) throws IOException {
        Appointment temp = new Appointment();
        AddAppointmentController controller = new AddAppointmentController();
        controller.loadAddModifyAppointmentScene(temp);
    }

    @FXML
    private void handleModifyButton(ActionEvent event) {
        try {
            Appointment appt = mainTableView.getSelectionModel().getSelectedItem();
            AddAppointmentController controller = new AddAppointmentController();
            controller.loadAddModifyAppointmentScene(appt);
        } catch (NullPointerException | IOException e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Selection Warning");
            alert.setHeaderText("Unable to Modify");
            alert.setContentText("Please choose a customer from the table to modify.");
            alert.showAndWait();
            AddAppointmentController controller = new AddAppointmentController();
            controller.modalOff();
        }

    }

    @FXML
    private void handleDeleteButton(ActionEvent event) {
        try {
            if (mainTableView.getSelectionModel().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("No Selection Warning");
                alert.setHeaderText("Unable to Delete");
                alert.setContentText("Please choose an appointment from the table.");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Delete Appointment");
                alert.setHeaderText("Confirm Delete");
                alert.setContentText("Click OK to delete the appointment.");
                Optional<ButtonType> option = alert.showAndWait();
                if (option.get() == ButtonType.OK) {
                    dao.delete(mainTableView.getSelectionModel().getSelectedItem());
                    popTableView(dao.retrieveAll());
                }

            }
        } catch (SQLException | NullPointerException e) {
            e.printStackTrace(System.err);
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Delete Customer Error");
            alert.setHeaderText("Unable to Delete");
            alert.setContentText("Please choose an appointment from the table.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleSearchButton(ActionEvent event) throws SQLException {
        String text = searchTextField.getText();
        ObservableList<Appointment> ob = dao.retrieveAll();
        ObservableList<Appointment> obFiltered = ob.stream().filter(a -> a.getName()
            .toLowerCase().contains(text.toLowerCase()))
            .collect(Collectors.toCollection(FXCollections::observableArrayList));
        if (obFiltered.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Matches Found");
            alert.setHeaderText(null);
            alert.setContentText("Unable to Find any matches for " + text);
            alert.showAndWait();
        } else {
            popTableView(obFiltered);
        }

        searchTextField.setText("");

    }

}
