package gci.utilities;

import static gci.database.ConnectionManager.getConnection;
import gci.models.*;
import java.sql.*;
import javafx.collections.*;

public class AppointmentDAO implements DAO<Appointment> {

    private static final ObservableList<Appointment> appointments = FXCollections.observableArrayList();
    private Connection con;
    private PreparedStatement stmt;
    private ResultSet rs;

    @Override
    public Appointment retrieve(int index) {
        return appointments.get(index);
    }

    @Override
    public ObservableList<Appointment> retrieveAll() throws SQLException {
        appointments.clear();
        if (con == null || con.isClosed()) {
            con = getConnection();
        }
        String query = "select * from appointment_view";
        stmt = con.prepareStatement(query);
        rs = stmt.executeQuery();
        while (rs.next()) {
            appointments.add(new Appointment(rs.getString(1), rs.getString(2), rs.getString(3),
                rs.getString(4), rs.getString(5), rs.getInt(6), rs.getInt(7),
                rs.getInt(8)));
        }
        sortByStart();
        return appointments;

    }

    private void sortByStart() {
        appointments.sort((c1, c2) -> c1.getStart().compareTo(c2.getStart()));
    }

    @Override
    public void create(Appointment t) {
        appointments.add(t);
        sortByStart();
    }

    @Override
    public void update(Appointment t) {
        Appointment foundAppointment = appointments.stream().filter(f -> f.getAppointmentId()
            == t.getAppointmentId()).findFirst().get();
        int index = appointments.indexOf(foundAppointment);
        appointments.set(index, t);
        sortByStart();
    }

    @Override
    public void delete(Appointment t) throws SQLException {
        if (con == null || con.isClosed()) {
            con = getConnection();
        }
        String query = "delete from appointment where appointmentId = ?";
        stmt = con.prepareStatement(query);
        stmt.setInt(1, t.getAppointmentId());
        stmt.executeUpdate();
    }

}
