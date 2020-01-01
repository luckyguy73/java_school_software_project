
package gci.controllers;

import gci.controllers.dialogs.LoginController;
import gci.models.Appointment;
import gci.utilities.*;
import java.io.*;
import java.net.URL;
import java.sql.SQLException;
import java.time.*;
import java.time.format.*;
import java.time.temporal.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;
import javafx.collections.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;

public class AppController implements Initializable {

    @FXML private BorderPane border_pane;
    @FXML private Label copyrightLabel;

    private static final AppointmentDAO ADAO = new AppointmentDAO();
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId ZID = ZoneId.systemDefault();
    private static ObservableList<Appointment> appointments = FXCollections.observableArrayList();
    private static ObservableList<LocalDateTime> datesOfAppointmentsWithinMonth = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setCopyright();
        logTimeStamp();
    }

    @FXML
    private void showCustomers(MouseEvent event) throws IOException {
        ViewHelpers.loadBorderCenter(border_pane, "customer");
    }

    @FXML
    private void showCalendar(MouseEvent event) throws IOException {
        ViewHelpers.loadBorderCenter(border_pane, "calendar");
    }

    @FXML
    private void showAppointments(MouseEvent event) throws IOException {
        ViewHelpers.loadBorderCenter(border_pane, "appointment");
    }

    @FXML
    private void showReports(MouseEvent event) throws IOException {
        ViewHelpers.loadBorderCenter(border_pane, "report");
    }

    private void setCopyright() {
        int year = LocalDate.now().getYear();
        copyrightLabel.setText("Copyright © " + year + " Global Consulting Institution");
    }

    public void checkForAppointmentWithinFifteenMinutes() {
        try {
            appointments = ADAO.retrieveAll()
                .filtered(f -> f.getUserId() == LoginController.loggedInUserId);
        } catch (SQLException ex) {
            Logger.getLogger(AppController.class.getName()).log(Level.SEVERE, null, ex);
        }

        datesOfAppointmentsWithinMonth = appointments.stream()
            .map(m -> LocalDateTime.parse(m.getStart(), TIMESTAMP_FORMAT))
            .filter(f -> f.query(new CalendarMonthQuery()))
            .collect(Collectors.toCollection(FXCollections::observableArrayList));

        if (datesOfAppointmentsWithinMonth.size() > 0) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Appointments");
            alert.setHeaderText("Appointment Reminder");
            alert.setContentText("There is an appointment scheduled within the next 15 minutes.");
            alert.showAndWait();
        }

    }

    private void logTimeStamp() {
        File file = new File("user_login_timestamps.txt");
        String timestamp = String.valueOf(ZonedDateTime.now());
        int userid = LoginController.loggedInUserId;
        try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file, true)))) {
            pw.write("UserId " + userid + " logged in at timestamp: " + timestamp + "\n");
            System.out.println("UserId " + userid + " logged in at timestamp: " + timestamp);
        } catch (IOException ex) {
            Logger.getLogger(AppController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    class CalendarMonthQuery implements TemporalQuery<Boolean> {

        @Override
        public Boolean queryFrom(TemporalAccessor temporal) {
            LocalDateTime date = LocalDateTime.from(temporal);
            
            ZoneOffset offset = OffsetDateTime.now(ZID).getOffset();
            LocalTime time = OffsetTime.of(LocalDateTime.now().toLocalTime(), offset)
                .withOffsetSameInstant(ZoneOffset.UTC).toLocalTime();
            LocalDate ld = LocalDateTime.now().toLocalDate();
            LocalDateTime now = LocalDateTime.of(ld, time);
            
            return date.isBefore(now.plusMinutes(15)) && now.isBefore(date);
        }
    }

}
