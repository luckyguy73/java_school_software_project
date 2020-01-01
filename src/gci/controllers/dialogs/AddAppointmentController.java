package gci.controllers.dialogs;

import gci.App;
import gci.database.ConnectionManager;
import static gci.database.ConnectionManager.getConnection;
import gci.models.*;
import gci.utilities.*;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;
import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.util.*;

public class AddAppointmentController implements Initializable {

    @FXML private ChoiceBox<Customer> nameChoiceBox;
    @FXML private ChoiceBox<String> typeChoiceBox;
    @FXML private ChoiceBox<String> timeChoiceBox;
    @FXML private TextArea locationTextArea;
    @FXML private Label titleLabel;
    @FXML private Label copyrightLabel;
    @FXML private Label timeLabel;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private DatePicker datePicker;

    private static final DateTimeFormatter MILITARY_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter AMPM_FORMAT = DateTimeFormatter.ofPattern("h:mm a");
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId ZID = ZoneId.systemDefault();
    private static final Region modal = new Region();
    private final CustomerDAO cDao = new CustomerDAO();
    private final AppointmentDAO aDao = new AppointmentDAO();
    private Appointment appt;
    private Stage stage;
    private String type;
    private String location;
    private LocalDate date;
    private LocalTime time;
    private String start;
    private String end;
    private Connection con;
    private PreparedStatement ps;
    private String query;
    private ResultSet rs;
    private int customerId;
    private int userId;
    private int appointmentId;
    private String modifyStart = "";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initApptChoiceBoxes();
        initTimeLabel();
        setDatePicker();
        setCopyright();
        checkConnection();
        modal.setStyle("-fx-background-color: #00000099;");
    }

    private void initApptChoiceBoxes() {
        try {
            nameChoiceBox.setItems(cDao.retrieveAll());
        } catch (SQLException ex) {
            Logger.getLogger(AddAppointmentController.class.getName()).log(Level.SEVERE, null, ex);
        }

        typeChoiceBox.setItems(FXCollections.observableArrayList("In-Person", "Phone", "WebMeeting"));
    }

    private void initTimeLabel() {
        timeLabel.setText("Available Times ("
            + ZoneId.systemDefault().getDisplayName(TextStyle.SHORT, Locale.getDefault()) + ")");
    }

    private void setDatePicker() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        StringConverter<LocalDate> converter = datePicker.getConverter();
        datePicker.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                LocalDate date = converter.fromString(string);
                if (date.getDayOfWeek() == DayOfWeek.SATURDAY
                    || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                    return datePicker.getValue();
                } else {
                    return date;
                }
            }
        });

        datePicker.setDayCellFactory(d -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now())
                    || date.getDayOfWeek() == DayOfWeek.SATURDAY
                    || date.getDayOfWeek() == DayOfWeek.SUNDAY);
            }
        });

        datePicker.valueProperty().addListener((obs, ov, nv) -> {
            if (nv.getDayOfWeek() == DayOfWeek.SATURDAY || nv.getDayOfWeek() == DayOfWeek.SUNDAY) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Select Appointment");
                alert.setHeaderText(null);
                alert.setContentText("Business Hours are Mon - Fri, 8am - 5pm PST");
                alert.showAndWait();
            }
        });
    }

    private void setCopyright() {
        int year = LocalDate.now().getYear();
        copyrightLabel.setText("Copyright © " + year + " Global Consulting Institution");
    }

    private void checkConnection() {

        try {
            if (con == null || con.isClosed()) {
                con = getConnection();
            }
        } catch (SQLException e) {
            ConnectionManager.showErrorMessage(e);
        }
    }

    public void loadAddModifyAppointmentScene(Appointment appt) throws IOException {
        Stage addAppointmentForm = new Stage();
        Stage mainStage = App.getStage();

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(App.class.getResource("views/dialogs/add_appointment.fxml"));

        Parent root = loader.load();
        Parent parent = mainStage.getScene().getRoot();

        StackPane stack = new StackPane(parent, modal);
        modal.setVisible(true);
        mainStage.setScene(new Scene(stack));

        AddAppointmentController controller = loader.getController();
        controller.setStage(addAppointmentForm);
        controller.setAppointment(appt);

        addAppointmentForm.setScene(new Scene(root));
        addAppointmentForm.initModality(Modality.WINDOW_MODAL);
        addAppointmentForm.initOwner(mainStage);
        addAppointmentForm.initStyle(StageStyle.UNDECORATED);
        addAppointmentForm.show();
        addAppointmentForm.centerOnScreen();
    }

    private void setStage(Stage stage) {
        this.stage = stage;
    }

    private void setAppointment(Appointment appt) {
        if (appt.getAppointmentId() > 0) {
            this.appt = appt;
            if (appt.getAppointmentId() != 0) {
                titleLabel.setText("Modify Appointment");
            }
            nameChoiceBox.setValue(cDao.retrieve(appt.getCustomerId()));
            typeChoiceBox.setValue(appt.getType());
            locationTextArea.setText(appt.getLocation());
            datePicker.setValue(LocalDate.parse(appt.getStart(),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            modifyStart = appt.getStart().substring(11);

            try {
                findAvailableTimes();
            } catch (SQLException ex) {
                Logger.getLogger(AddAppointmentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    @FXML
    private void handleCancelButton(ActionEvent event) {
        this.stage.close();
        modalOff();
    }

    @FXML
    private void handleSaveButton(ActionEvent event) {
        boolean valid = validateForm();
        if (valid) {
            saveAppointment();
        }
    }

    private boolean validateForm() {
        String errors = "";
        if (nameChoiceBox.getSelectionModel().getSelectedItem() == null) {
            errors += "Customer Name\n";
        }
        if (typeChoiceBox.getSelectionModel().getSelectedItem() == null) {
            errors += "Appointment Type\n";
        }
        if (locationTextArea.getLength() < 1) {
            errors += "Location Details\n";
        }
        if (datePicker.getValue() == null) {
            errors += "Date\n";
        }
        if (timeChoiceBox.getSelectionModel().getSelectedItem() == null) {
            errors += "Time\n";
        }
        if (errors.length() < 1) {
            return true;
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Incomplete Form");
        alert.setHeaderText("The following fields still need to be populated");
        alert.setContentText(errors);
        alert.showAndWait();

        return false;
    }

    public void modalOff() {
        modal.setVisible(false);
    }

    @FXML
    private void findAvailableTimes() throws SQLException {
        String[] available = {
            "15:00:00",
            "16:00:00",
            "17:00:00",
            "18:00:00",
            "20:00:00",
            "21:00:00",
            "22:00:00",
            "23:00:00"
        }; //appointment times as UTC based on beginning appt of 8am PST

        Set<String> appts = aDao.retrieveAll().stream()
            .filter(f -> f.getUserId() == LoginController.loggedInUserId)
            .map(m -> m.getStart())
            .filter(f -> f.substring(0, 10).equals(datePicker.getValue().toString()))
            .map(m -> m.substring(11))
            .collect(Collectors.toSet());

        Set<String> times = Arrays.asList(available).stream()
            .filter(t -> !appts.contains(t)).collect(Collectors.toSet());

        if (modifyStart.length() > 0) {
            times.add(modifyStart);
        }

        updateTimeChoiceBox(new TreeSet<>(times));
    }

    private void updateTimeChoiceBox(Set<String> times) {
        List<String> available = new ArrayList<>();

        ZoneOffset offset = OffsetDateTime.now(ZID).getOffset();

        times.stream()
            .map(m -> LocalTime.parse(m, MILITARY_FORMAT))
            .map(m -> OffsetTime.of(m, ZoneOffset.UTC).withOffsetSameInstant(offset).toLocalTime())
            .map(m -> m.format(AMPM_FORMAT))
            .forEach(available::add);

        timeChoiceBox.setItems(FXCollections.observableList(available));
        modifyStart = "";
    }

    private void saveAppointment() {
        getAppointmentValues();

        if (appt != null) {
            query = "update appointment set customerId = ?, userId = ?, location = ?, type =?, "
                + "start = ?, end = ? where appointmentId = ?";

            try {
                ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, customerId);
                ps.setInt(2, userId);
                ps.setString(3, location);
                ps.setString(4, type);
                ps.setString(5, start);
                ps.setString(6, end);
                ps.setInt(7, appointmentId = appt.getAppointmentId());
                ps.executeUpdate();
                ps.close();
                aDao.update(appt);
                aDao.retrieveAll();
            } catch (SQLException e) {
                Logger.getLogger(AddAppointmentController.class.getName()).log(Level.SEVERE, null, e);
            }
        } else {
            query = "insert into appointment (customerId, userId, location, type, start, end) values "
                + "(?, ?, ?, ?, ?, ?)";

            try {
                ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, customerId);
                ps.setInt(2, userId);
                ps.setString(3, location);
                ps.setString(4, type);
                ps.setString(5, start);
                ps.setString(6, end);
                ps.executeUpdate();
                rs = ps.getGeneratedKeys();
                rs.next();
                appointmentId = rs.getInt(1);
                ps.close();
                aDao.create(new Appointment(
                    cDao.retrieve(customerId).getName(),
                    type,
                    location,
                    start,
                    end,
                    appointmentId,
                    customerId,
                    userId
                ));
            } catch (SQLException e) {
                Logger.getLogger(AddAppointmentController.class.getName()).log(Level.SEVERE, null, e);
            }

        }

        this.stage.close();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titleLabel.getText());
        alert.setHeaderText(null);
        alert.setContentText("The Appointment was saved successfully.");
        alert.showAndWait();
        modalOff();

    }

    private void getAppointmentValues() {
        customerId = nameChoiceBox.getSelectionModel().getSelectedItem().getCustomerId();
        userId = LoginController.loggedInUserId;
        location = locationTextArea.getText();
        type = typeChoiceBox.getSelectionModel().getSelectedItem();
        date = datePicker.getValue();
        time = LocalTime.parse(timeChoiceBox.getValue(), AMPM_FORMAT);
        start = LocalDateTime.of(date, time).atZone(ZID).withZoneSameInstant(ZoneId.of("UTC"))
            .format(TIMESTAMP_FORMAT);
        end = LocalDateTime.parse(start, TIMESTAMP_FORMAT).plusHours(1).format(TIMESTAMP_FORMAT);
    }

}
