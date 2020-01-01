
package gci.controllers;

import static gci.database.ConnectionManager.getConnection;
import gci.models.Report;
import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.logging.*;
import javafx.collections.*;
import javafx.fxml.*;
import javafx.scene.control.*;

public class ReportController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private ChoiceBox<String> reportChoiceBox;
    @FXML private TableView<Report> apptTypesTV;
    @FXML private TableColumn<Report, String> apptTypeColumn;
    @FXML private TableColumn<Report, String> consultantColumn;
    @FXML private TableColumn<Report, String> scheduleColumn;
    @FXML private TableColumn<Report, String> monthColumn;
    @FXML private TableColumn<Report, String> countryColumn;
    @FXML private TableColumn<Report, String> totalColumn;

    private static final ObservableList<Report> reports = FXCollections.observableArrayList();
    private Connection con;
    private PreparedStatement stmt;
    private ResultSet rs;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initReportChoiceBox();
        queryApptTypeReport();
        initTableColumns();
        popTableView();
    }

    private void initReportChoiceBox() {
        reportChoiceBox.setItems(FXCollections.observableArrayList(
            "Number of Appointment Types by Month",
            "Consultant Appointment Schedule",
            "Number of Customers by Country",
            "Number of Appointments by Consultant"));

        reportChoiceBox.getSelectionModel().selectedIndexProperty().addListener((ob, ov, nv) -> {
            setColumnVisibility((int) nv);
            popTableView();
        });

        reportChoiceBox.getSelectionModel().selectFirst();
    }

    private void queryApptTypeReport() {

        String query = "SELECT type, CONCAT(MONTHNAME(START), \" \", YEAR(START)) AS MONTH, "
            + "Count(type) AS total FROM appointment GROUP BY MONTH, "
            + "TYPE ORDER BY YEAR(START), MONTH(START)";

        reports.clear();
        try {
            if (con == null || con.isClosed()) {
                con = getConnection();
            }
            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();
            while (rs.next()) {
                reports.add(new Report(rs.getString(1), rs.getString(2), rs.getString(3)));
            }
        } catch (SQLException ex) {
            Logger.getLogger(ReportController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void queryConsultantReport() {
        String offset = ZoneOffset.systemDefault().getRules().getOffset(LocalDateTime.now()).toString();

        String query = "SELECT TYPE, country, username, "
            + "DATE_FORMAT(CONVERT_TZ(start,'+00:00','" + offset + "'), '%M %D %Y %r') AS START "
            + "FROM appointment_view AS a INNER JOIN customer_view AS c ON c.customerid = a.customerid "
            + "ORDER BY MONTH(START), DAY(START), TIME(START)";

        reports.clear();
        try {
            if (con == null || con.isClosed()) {
                con = getConnection();
            }
            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();
            while (rs.next()) {
                reports.add(new Report(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4)));
            }
        } catch (SQLException ex) {
            Logger.getLogger(ReportController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void queryCustByCountryReport() {

        String query = "SELECT country.country, COUNT(customer_view.countryId) AS total "
            + "FROM customer_view INNER JOIN country ON customer_view.countryId = country.countryId "
            + "GROUP BY 1 ORDER BY 2 desc";

        reports.clear();
        try {
            if (con == null || con.isClosed()) {
                con = getConnection();
            }
            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();
            while (rs.next()) {
                reports.add(new Report(rs.getString(1), rs.getString(2)));
            }
        } catch (SQLException ex) {
            Logger.getLogger(ReportController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void queryApptByConsultantReport() {

        String query = "SELECT username, count(appointmentId) AS total FROM appointment_view "
            + "GROUP BY 1 ORDER BY 2 DESC";
        
        reports.clear();
        try {
            if (con == null || con.isClosed()) {
                con = getConnection();
            }
            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();
            while (rs.next()) {
                reports.add(new Report(null, null, null, rs.getString(2), rs.getString(1), null));
            }
        } catch (SQLException ex) {
            Logger.getLogger(ReportController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void initTableColumns() {
        apptTypeColumn.setCellValueFactory(cellData -> cellData.getValue().apptTypeProperty());
        monthColumn.setCellValueFactory(cellData -> cellData.getValue().monthProperty());
        totalColumn.setCellValueFactory(cellData -> cellData.getValue().totalProperty());
        countryColumn.setCellValueFactory(cellData -> cellData.getValue().countryProperty());
        consultantColumn.setCellValueFactory(cellData -> cellData.getValue().consultantProperty());
        scheduleColumn.setCellValueFactory(cellData -> cellData.getValue().scheduleProperty());

        countryColumn.setVisible(false);
        consultantColumn.setVisible(false);
        scheduleColumn.setVisible(false);
    }

    private void popTableView() {
        apptTypesTV.setItems(reports);
    }

    private void setColumnVisibility(int i) {
        switch (i) {
            case 0:
                apptTypeColumn.setVisible(true);
                monthColumn.setVisible(true);
                countryColumn.setVisible(false);
                totalColumn.setVisible(true);
                consultantColumn.setVisible(false);
                scheduleColumn.setVisible(false);
                queryApptTypeReport();
                break;
            case 1:
                apptTypeColumn.setVisible(true);
                monthColumn.setVisible(false);
                countryColumn.setVisible(true);
                totalColumn.setVisible(false);
                consultantColumn.setVisible(true);
                scheduleColumn.setVisible(true);
                queryConsultantReport();
                break;
            case 2:
                apptTypeColumn.setVisible(false);
                monthColumn.setVisible(false);
                countryColumn.setVisible(true);
                totalColumn.setVisible(true);
                consultantColumn.setVisible(false);
                scheduleColumn.setVisible(false);
                queryCustByCountryReport();
                break;
            case 3:
                apptTypeColumn.setVisible(false);
                monthColumn.setVisible(false);
                countryColumn.setVisible(false);
                totalColumn.setVisible(true);
                consultantColumn.setVisible(true);
                scheduleColumn.setVisible(false);
                queryApptByConsultantReport();
                break;
            default:
                break;
        }
    }

}
