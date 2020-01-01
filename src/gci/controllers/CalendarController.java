
package gci.controllers;

import gci.controllers.dialogs.*;
import gci.models.Appointment;
import gci.utilities.AppointmentDAO;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.*;
import java.time.format.*;
import java.time.temporal.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.Collectors;
import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

public class CalendarController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private ToggleGroup view;
    @FXML private DatePicker datePicker;
    @FXML private GridPane grid;

    private static final AppointmentDAO ADAO = new AppointmentDAO();
    private static final DateTimeFormatter MILITARY_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter AMPM_FORMAT = DateTimeFormatter.ofPattern("h:mm a");
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId ZID = ZoneId.systemDefault();
    private static ObservableList<Appointment> appointments = FXCollections.observableArrayList();
    private static ObservableList<LocalDateTime> datesOfAppointmentsWithinMonth = FXCollections.observableArrayList();
    private final List<String> times = new ArrayList<>();
    private LocalDate date;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initAppointmentsList();
        setDatePicker();
        createCalendar();
    }

    private void initAppointmentsList() {
        try {
            appointments = ADAO.retrieveAll().filtered(f -> f.getUserId() == LoginController.loggedInUserId);
        } catch (SQLException ex) {
            Logger.getLogger(CalendarController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void setDatePicker() {
        datePicker.setValue(LocalDate.now());
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM yyyy");
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
                return date;
            }
        });
    }

    @FXML
    private void createCalendar() {
        ObservableList<Node> nodes = grid.getChildren();
        for (Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
            Node value = iterator.next();
            if (value instanceof VBox) {
                iterator.remove();
            }
        }

        date = datePicker.getValue();
        int value = date.with(TemporalAdjusters.firstDayOfMonth()).getDayOfWeek().getValue();
        int startingColumn = 0;
        int daysInMonth = date.with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth();
        //lambdas are useful for converting collections on the fly
        datesOfAppointmentsWithinMonth = appointments.stream()
            .map(m -> LocalDateTime.parse(m.getStart(), TIMESTAMP_FORMAT))
            .filter(f -> f.query(new CalendarMonthQuery()))
            .collect(Collectors.toCollection(FXCollections::observableArrayList));

        switch (value) {
            case 1:
                startingColumn = 1;
                break;
            case 2:
                startingColumn = 2;
                break;
            case 3:
                startingColumn = 3;
                break;
            case 4:
                startingColumn = 4;
                break;
            case 5:
                startingColumn = 5;
                break;
            case 6:
                startingColumn = 6;
                break;
            case 7:
                startingColumn = 0;
                break;
        }

        int counter = 0;
        int clone = startingColumn;
        for (int row = 0; row < 7; row++) {
            for (int col = 0; col < 7; col++) {
                if (row == 0 && counter == (7 - startingColumn)) {
                    break;
                }
                VBox vbox = createVBox(counter);
                if (row == 0) {
                    grid.add(vbox, clone, row + 1);
                } else {
                    grid.add(vbox, col, row + 1);
                }
                counter++;
                clone++;
                if (counter == daysInMonth) {
                    break;
                }
            }
            if (counter == daysInMonth) {
                break;
            }
        }
    }

    private VBox createVBox(int counter) {
        Button button = new Button();
        int appCount = appointmentToday(counter + 1);
        List<String> newTimes = new ArrayList<>(times);
        String str = appCount == 1 ? " Appointment" : " Appointments";
        VBox.setVgrow(button, Priority.ALWAYS);
        VBox vbox = new VBox(new Label(counter + 1 + ""));
        if (appCount > 0) {
            button.setText(appCount + str);
            vbox.getChildren().add(button);
        }
        button.setOnMouseClicked(e -> this.handleButtonAction(appCount, newTimes));
        vbox.setOnMouseClicked(e -> this.handleButtonAction(appCount, newTimes));
        vbox.setAlignment(Pos.BASELINE_RIGHT);
        vbox.setPadding(new Insets(5, 5, 5, 5));
        return vbox;
    }

    private int appointmentToday(int i) {
        List<Integer> list = datesOfAppointmentsWithinMonth.stream()
            .map(m -> m.getDayOfMonth())
            .filter(f -> f == i)
            .collect(Collectors.toList());

        times.clear();
        ZoneOffset offset = OffsetDateTime.now(ZID).getOffset();
        
        datesOfAppointmentsWithinMonth.stream()
            .filter(f -> f.getDayOfMonth() == i)
            .map(m -> LocalTime.parse(m.toString().substring(11), MILITARY_FORMAT))
            .sorted()
            .map(m -> OffsetTime.of(m, ZoneOffset.UTC).withOffsetSameInstant(offset).toLocalTime())
            .map(m -> m.format(AMPM_FORMAT))
            .forEach(times::add);

        return list.size();
    }

    private void handleButtonAction(int count, List<String> times) {
        String str = count == 1 ? " Appointment" : " Appointments";
        String tz = ZoneId.systemDefault().getDisplayName(TextStyle.SHORT, Locale.getDefault());
        StringBuilder sb = new StringBuilder();
        times.forEach(f -> sb.append(f).append("\n"));
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Appointments");
        alert.setHeaderText(count + str + " Scheduled (" + tz + ")");
        alert.setContentText(sb.toString());
        alert.showAndWait();
    }
    
    @FXML
    private void handleWeekViewButton(ActionEvent event) throws IOException {
        WeekViewController controller = new WeekViewController();
        controller.loadWeekViewScene();
    }

    class CalendarMonthQuery implements TemporalQuery<Boolean> {

        @Override
        public Boolean queryFrom(TemporalAccessor temporal) {
            LocalDate date = LocalDate.from(temporal);
            LocalDate datepicker = datePicker.getValue();

            MonthDay first = MonthDay.of(datepicker.getMonth(), 1);
            MonthDay last = MonthDay.of(datepicker.getMonth(),
                datepicker.with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth());

            return date.isAfter(first.atYear(date.getYear()).minusDays(1))
                && date.isBefore(last.atYear(date.getYear()).plusDays(1));
        }
    }

}
