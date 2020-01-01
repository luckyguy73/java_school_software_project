package gci.models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Appointment {

    private final StringProperty name;
    private final StringProperty type;
    private final StringProperty location;
    private final StringProperty start;
    private final StringProperty end;
    private final IntegerProperty appointmentId;
    private final IntegerProperty customerId;
    private final IntegerProperty userId;

    public Appointment() {
        this(null, null, null, null, null, 0, 0, 0);
    }

    public Appointment(String name, String type, String location, String start, String end,
        int appointmentId, int customerId, int userId) {
        this.name = new SimpleStringProperty(name);
        this.type = new SimpleStringProperty(type);
        this.location = new SimpleStringProperty(location);
        this.start = new SimpleStringProperty(start);
        this.end = new SimpleStringProperty(end);
        this.appointmentId = new SimpleIntegerProperty(appointmentId);
        this.customerId = new SimpleIntegerProperty(customerId);
        this.userId = new SimpleIntegerProperty(userId);
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getType() {
        return type.get();
    }

    public void setType(String type) {
        this.type.set(type);
    }

    public StringProperty typeProperty() {
        return type;
    }

    public String getLocation() {
        return location.get();
    }

    public void setLocation(String location) {
        this.location.set(location);
    }

    public StringProperty locationProperty() {
        return location;
    }

    public String getStart() {
        return start.get();
    }

    public void setStart(String start) {
        this.start.set(start);
    }

    public StringProperty startProperty() {
        return start;
    }

    public String getEnd() {
        return end.get();
    }

    public void setEnd(String end) {
        this.end.set(end);
    }

    public StringProperty endProperty() {
        return end;
    }

    public int getAppointmentId() {
        return appointmentId.get();
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId.set(appointmentId);
    }

    public IntegerProperty appointmentIdProperty() {
        return appointmentId;
    }

    public int getCustomerId() {
        return customerId.get();
    }

    public void setCustomerId(int customerId) {
        this.customerId.set(customerId);
    }

    public IntegerProperty customerIdProperty() {
        return customerId;
    }

    public int getUserId() {
        return userId.get();
    }

    public void setUserId(int userId) {
        this.userId.set(userId);
    }

    public IntegerProperty userIdProperty() {
        return userId;
    }

}
