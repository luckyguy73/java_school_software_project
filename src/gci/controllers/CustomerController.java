
package gci.controllers;

import gci.controllers.dialogs.AddCustomerController;
import gci.database.ConnectionManager;
import gci.models.Customer;
import gci.utilities.CustomerDAO;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.*;
import java.util.stream.Collectors;
import javafx.collections.*;
import javafx.fxml.*;
import javafx.scene.control.*;


public class CustomerController implements Initializable {

    @FXML private TableView<Customer> mainTableView;
    @FXML private TableColumn<Customer, String> nameColumn;
    @FXML private TableColumn<Customer, String> phoneColumn;
    @FXML private TableColumn<Customer, String> addressColumn;
    @FXML private TableColumn<Customer, String> cityColumn;
    @FXML private TableColumn<Customer, String> postalColumn;
    @FXML private TableColumn<Customer, String> countryColumn;
    @FXML private Button searchButton;
    @FXML private TextField searchTextField;
    @FXML private Label titleLabel;
    @FXML private Button addButton;
    @FXML private Button modifyButton;
    @FXML private Button deleteButton;

    private final CustomerDAO dao = new CustomerDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        //lambdas here are more efficient than writing out the full anonymouse inner class
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        phoneColumn.setCellValueFactory(cellData -> cellData.getValue().phoneProperty());
        addressColumn.setCellValueFactory(cellData -> cellData.getValue().addressProperty());
        cityColumn.setCellValueFactory(cellData -> cellData.getValue().cityProperty());
        postalColumn.setCellValueFactory(cellData -> cellData.getValue().postalCodeProperty());
        countryColumn.setCellValueFactory(cellData -> cellData.getValue().countryProperty());

        try {
            popTableView(dao.retrieveAll());
        } catch (SQLException ex) {
            ConnectionManager.showErrorMessage(ex);
            Logger.getLogger(CustomerController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void handleAddButton() throws IOException {
        Customer temp = new Customer();
        AddCustomerController controller = new AddCustomerController();
        controller.loadAddModifyCustomerScene(temp);
    }

    @FXML
    private void handleModifyButton() {
        try {
            Customer cust = mainTableView.getSelectionModel().getSelectedItem();
            AddCustomerController controller = new AddCustomerController();
            controller.loadAddModifyCustomerScene(cust);
        } catch (NullPointerException | IOException e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Selection Warning");
            alert.setHeaderText("Unable to Modify");
            alert.setContentText("Please choose a customer from the table to modify.");
            alert.showAndWait();
            AddCustomerController controller = new AddCustomerController();
            controller.modalOff();
        }
    }

    @FXML
    private void handleDeleteButton() {
        try {
            if (mainTableView.getSelectionModel().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("No Selection Warning");
                alert.setHeaderText("Unable to Delete");
                alert.setContentText("Please choose a customer from the table.");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Delete Customer");
                alert.setHeaderText("Confirm Delete");
                alert.setContentText("Click OK to delete the customer.");
                Optional<ButtonType> option = alert.showAndWait();
                if (option.get() == ButtonType.OK) {
                    dao.delete(mainTableView.getSelectionModel().getSelectedItem());
                    popTableView(dao.retrieveAll());
                }

            }
        } catch (NullPointerException | SQLException e) {
            e.printStackTrace(System.err);
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Delete Customer Error");
            alert.setHeaderText("Unable to Delete");
            alert.setContentText("Please choose a customer from the table.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleSearchButton() throws SQLException {
        String text = searchTextField.getText();
        ObservableList<Customer> ob = dao.retrieveAll();
        //like to use lambdas for functional programming to stream collections more robust
        ObservableList<Customer> obFiltered = ob.stream().filter(c -> c.getName()
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

    public void popTableView(ObservableList<Customer> ob) throws SQLException {
        mainTableView.setItems(ob);
    }

}
