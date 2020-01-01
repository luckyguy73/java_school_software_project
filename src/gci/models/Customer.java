package gci.models;

import javafx.beans.property.*;

public class Customer {

    private final StringProperty name;
    private final StringProperty phone;
    private final StringProperty address;
    private final StringProperty city;
    private final StringProperty postalCode;
    private final StringProperty country;
    private final IntegerProperty addressId;
    private final IntegerProperty cityId;
    private final IntegerProperty countryId;
    private final IntegerProperty customerId;

    public Customer() {
        this(null, null, null, null, null, null, 0, 0, 0, 0);
    }

    public Customer(String name, String phone, String address, String city, String postalCode,
        String country, int addressId, int cityId, int countryId, int customerId) {
        this.name = new SimpleStringProperty(name);
        this.phone = new SimpleStringProperty(phone);
        this.address = new SimpleStringProperty(address);
        this.city = new SimpleStringProperty(city);
        this.postalCode = new SimpleStringProperty(postalCode);
        this.country = new SimpleStringProperty(country);
        this.addressId = new SimpleIntegerProperty(addressId);
        this.cityId = new SimpleIntegerProperty(cityId);
        this.countryId = new SimpleIntegerProperty(countryId);
        this.customerId = new SimpleIntegerProperty(customerId);
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

    public String getPhone() {
        return phone.get();
    }

    public void setPhone(String phone) {
        this.phone.set(phone);
    }

    public StringProperty phoneProperty() {
        return phone;
    }

    public String getAddress() {
        return address.get();
    }

    public void setAddress(String address) {
        this.address.set(address);
    }

    public StringProperty addressProperty() {
        return address;
    }

    public String getCity() {
        return city.get();
    }

    public void setCity(String city) {
        this.city.set(city);
    }

    public StringProperty cityProperty() {
        return city;
    }

    public String getPostalCode() {
        return postalCode.get();
    }

    public void setPostalCode(String postalCode) {
        this.postalCode.set(postalCode);

    }

    public StringProperty postalCodeProperty() {
        return postalCode;
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

    public int getAddressId() {
        return addressId.get();
    }

    public void setAddressId(int addressId) {
        this.addressId.set(addressId);
    }

    public IntegerProperty addressIdProperty() {
        return addressId;
    }

    public int getCityId() {
        return cityId.get();
    }

    public void setCityId(int cityId) {
        this.cityId.set(cityId);
    }

    public IntegerProperty cityIdProperty() {
        return cityId;
    }

    public int getCountryId() {
        return countryId.get();
    }

    public void setCountryId(int countryId) {
        this.countryId.set(countryId);
    }

    public IntegerProperty countryIdProperty() {
        return countryId;
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

    @Override
    public String toString() {
        return this.getName();
    }
    
}
