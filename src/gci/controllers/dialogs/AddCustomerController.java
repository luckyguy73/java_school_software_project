package gci.controllers.dialogs;

import gci.App;
import gci.database.ConnectionManager;
import static gci.database.ConnectionManager.getConnection;
import gci.models.Customer;
import gci.utilities.CustomerDAO;
import static gci.utilities.DatabaseQueryHelper.toTitleCase;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

public class AddCustomerController implements Initializable {

    @FXML private TextField nameTextField;
    @FXML private TextField phoneTextField;
    @FXML private TextField addressTextField;
    @FXML private TextField cityTextField;
    @FXML private TextField postalTextField;
    @FXML private TextField countryTextField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Label titleLabel;
    @FXML private Label copyrightLabel;

    private Customer customer;
    private Dialog dialog;
    private final CustomerDAO dao = new CustomerDAO();
    private Connection con;
    private PreparedStatement ps;
    private ResultSet rs;
    private Stage stage;
    private String param;
    private String query;
    private int countryId;
    private int customerId;
    private int cityId;
    private int addressId;
    private int primaryKey;
    private static final Region modal = new Region();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setCopyright();

        try {
            if (con == null || con.isClosed()) {
                con = getConnection();
            }
        } catch (SQLException e) {
            ConnectionManager.showErrorMessage(e);
        }

        modal.setStyle("-fx-background-color: #00000099;");
    }

    @FXML
    private void handleCancelButton(ActionEvent event) {
        this.stage.close();
        modalOff();
    }

    public void setCustomer(Customer cust) {
        this.customer = cust;
        if (customer.getCustomerId() != 0) {
            titleLabel.setText("Modify Customer");
        }
        nameTextField.setText(customer.getName());
        phoneTextField.setText(customer.getPhone());
        addressTextField.setText(customer.getAddress());
        cityTextField.setText(customer.getCity());
        postalTextField.setText(customer.getPostalCode());
        countryTextField.setText(customer.getCountry());
        countryId = customer.getCountryId();
        customerId = customer.getCustomerId();
        cityId = customer.getCityId();
        addressId = customer.getAddressId();
    }

    public void setDialog(Dialog dialog) {
        this.dialog = dialog;
    }

    @FXML
    private void handleSaveButton() throws SQLException, IOException {
        boolean valid = validateForm();

        if (valid) {
            if (!titleLabel.getText()
                .contains("Modify")) {
                countryId = addCountryId();
                cityId = addCityId();
                addressId = addAddressId();
            } else {
                if (!countryTextField.getText().toLowerCase().equals(customer.getCountry().toLowerCase())) {
                    countryId = addCountryId();
                    cityId = addCityId();
                    addressId = addAddressId();
                }
                if (!cityTextField.getText().toLowerCase().equals(customer.getCity().toLowerCase())) {
                    cityId = addCityId();
                    addressId = addAddressId();
                }
                if (!addressTextField.getText().toLowerCase().equals(customer.getAddress().toLowerCase())
                    || !phoneTextField.getText().equals(customer.getPhone())
                    || !postalTextField.getText().equals(customer.getPostalCode())) {
                    addressId = addAddressId();
                }
            }

            saveCustomer();
        }
    }

    private boolean validateForm() {
        String errors = "";
        if (nameTextField.getText() == null) {
            errors += "Customer Name\n";
        }
        if (phoneTextField.getText() == null) {
            errors += "Phone Number\n";
        }
        if (addressTextField.getText() == null) {
            errors += "Address\n";
        }
        if (cityTextField.getText() == null) {
            errors += "City\n";
        }
        if (postalTextField.getText() == null) {
            errors += "Postal Code\n";
        }
        if (countryTextField.getText() == null) {
            errors += "Country\n";
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

    private Integer addCountryId() throws SQLException {
        param = countryTextField.getText().toUpperCase();
        query = "insert into country (country) values (?)";
        ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, param);
        try {
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            rs.next();
            primaryKey = rs.getInt(1);
        } catch (SQLIntegrityConstraintViolationException e) {
            ps = con.prepareStatement("select countryId from country where country = ?");
            ps.setString(1, param);
            rs = ps.executeQuery();
            rs.next();
            primaryKey = rs.getInt(1);
        } finally {
            ps.close();
        }
        return primaryKey;
    }

    private Integer addCityId() throws SQLException {
        param = toTitleCase(cityTextField.getText());
        query = "insert into city (city, countryId) values (?, ?)";
        ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, param);
        ps.setInt(2, countryId);
        try {
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            rs.next();
            primaryKey = rs.getInt(1);
        } catch (SQLIntegrityConstraintViolationException e) {
            ps = con.prepareStatement("select cityId from city where city = ? and countryId = ?");
            ps.setString(1, param);
            ps.setInt(2, countryId);
            rs = ps.executeQuery();
            rs.next();
            primaryKey = rs.getInt(1);
        } finally {
            ps.close();
        }
        return primaryKey;
    }

    private Integer addAddressId() throws SQLException {
        param = toTitleCase(addressTextField.getText());
        query = "insert into address (address, cityId, postalCode, phone) values (?,?,?,?)";
        ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, param);
        ps.setInt(2, cityId);
        ps.setString(3, postalTextField.getText());
        ps.setString(4, phoneTextField.getText());
        try {
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            rs.next();
            primaryKey = rs.getInt(1);
        } catch (SQLIntegrityConstraintViolationException e) {
            ps = con.prepareStatement("select addressId from address where address = ? and cityId = ?");
            ps.setString(1, param);
            ps.setInt(2, cityId);
            rs = ps.executeQuery();
            rs.next();
            primaryKey = rs.getInt(1);
            ps = con.prepareStatement("update address set postalCode = ?, phone = ? where addressId = ?");
            ps.setString(1, postalTextField.getText());
            ps.setString(2, phoneTextField.getText());
            ps.setInt(3, primaryKey);
            ps.executeUpdate();
        } finally {
            ps.close();
        }
        return primaryKey;
    }

    private void saveCustomer() throws SQLException, IOException {
        param = toTitleCase(nameTextField.getText());
        if (customerId != 0) {
            query = "update customer set customerName = ?, addressId = ? where customerId = ?";
            ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, param);
            ps.setInt(2, addressId);
            ps.setInt(3, customerId);
            ps.executeUpdate();
            dao.update(new Customer(
                param,
                toTitleCase(phoneTextField.getText()),
                toTitleCase(addressTextField.getText()),
                toTitleCase(cityTextField.getText()),
                toTitleCase(postalTextField.getText()),
                countryTextField.getText().toUpperCase(),
                addressId, cityId, countryId, customerId));
            ps.close();
            this.stage.close();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(titleLabel.getText());
            alert.setHeaderText(null);
            alert.setContentText("The Customer was saved successfully.");
            alert.showAndWait();
            modalOff();
        } else {
            query = "insert into customer (customerName, addressId) values (?, ?)";
            ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, param);
            ps.setInt(2, addressId);
            try {
                ps.executeUpdate();
                dao.create(new Customer(
                    param,
                    toTitleCase(phoneTextField.getText()),
                    toTitleCase(addressTextField.getText()),
                    toTitleCase(cityTextField.getText()),
                    toTitleCase(postalTextField.getText()),
                    countryTextField.getText().toUpperCase(),
                    addressId, cityId, countryId, customerId));
                ps.close();
                this.stage.close();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle(titleLabel.getText());
                alert.setHeaderText(null);
                alert.setContentText("The Customer was saved successfully.");
                alert.showAndWait();
                modalOff();
            } catch (SQLIntegrityConstraintViolationException e) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle(titleLabel.getText());
                alert.setHeaderText(null);
                alert.setContentText("Customer with same name and address already exists");
                alert.showAndWait();
            }
        }
    }

    public void loadAddModifyCustomerScene(Customer cust) throws IOException {
        Stage addCustomerForm = new Stage();
        Stage mainStage = App.getStage();
        FXMLLoader loader = new FXMLLoader();
        loader
            .setLocation(App.class
                .getResource("views/dialogs/add_customer.fxml"));
        Parent root = loader.load();
        Parent parent = mainStage.getScene().getRoot();
        StackPane stack = new StackPane(parent, modal);
        modal.setVisible(true);
        mainStage.setScene(new Scene(stack));
        addCustomerForm.setScene(new Scene(root));
        addCustomerForm.initModality(Modality.WINDOW_MODAL);
        addCustomerForm.initOwner(mainStage);
        addCustomerForm.initStyle(StageStyle.UNDECORATED);
        AddCustomerController controller = loader.getController();
        controller.setStage(addCustomerForm);
        controller.setCustomer(cust);
        addCustomerForm.show();
        addCustomerForm.centerOnScreen();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void modalOff() {
        modal.setVisible(false);
    }

    private void setCopyright() {
        int year = LocalDate.now().getYear();
        copyrightLabel.setText("Copyright © " + year + " Global Consulting Institution");
    }
}
