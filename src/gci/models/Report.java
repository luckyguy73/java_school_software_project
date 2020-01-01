
package gci.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


public class Report {
    
    private final StringProperty apptType;
    private final StringProperty month;
    private final StringProperty total;
    private final StringProperty country;
    private final StringProperty consultant;
    private final StringProperty schedule;
    
    public Report(String country, String total) {
        this(null, null, country, total, null, null);
    }
    
    public Report(String apptType, String month, String total) {
        this(apptType, month, null, total, null, null);
    }
    
    public Report(String apptType, String country, String consultant, String schedule) {
        this(apptType, null, country, null, consultant, schedule);
    }
    
    public Report(String apptType, String month, String country, String total, String consultant, String schedule) {
        this.apptType = new SimpleStringProperty(apptType);
        this.month = new SimpleStringProperty(month);
        this.country = new SimpleStringProperty(country);
        this.total = new SimpleStringProperty(total);
        this.consultant = new SimpleStringProperty(consultant);
        this.schedule = new SimpleStringProperty(schedule);
    }
    
    public String getApptType() {
        return apptType.get();
    }
    
    public void setApptType(String apptType) {
        this.apptType.set(apptType);
    }
        
    public StringProperty apptTypeProperty() {
        return apptType;
    }
    
    public String getMonth() {
        return month.get();
    }
    
    public void setMonth(String month) {
        this.month.set(month);
    }
        
    public StringProperty monthProperty() {
        return month;
    }
    
    public String getCountry() {
        return country.get();
    }
    
    public void setCountry(String country) {
        this.country.set(country);
    }
        
    public StringProperty countryProperty() {
        return country;
    }
    
    public String getTotal() {
        return total.get();
    }
    
    public void setYotal(String total) {
        this.total.set(total);
    }
        
    public StringProperty totalProperty() {
        return total;
    }
    
    public String getConsultant() {
        return consultant.get();
    }
    
    public void setConsultant(String consultant) {
        this.consultant.set(consultant);
    }
        
    public StringProperty consultantProperty() {
        return consultant;
    }
    
    public String getSchedule() {
        return schedule.get();
    }
    
    public void setSchedule(String schedule) {
        this.schedule.set(schedule);
    }
        
    public StringProperty scheduleProperty() {
        return schedule;
    }

}
