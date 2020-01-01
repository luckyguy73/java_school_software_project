
package gci.controllers.dialogs;

import gci.App;
import static gci.database.ConnectionManager.getConnection;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import javafx.fxml.*;
import javafx.scene.control.*;

public class LoginController implements Initializable {

    @FXML private TextField usernameTextField;
    @FXML private TextField visiblePasswordTextField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox showPasswordCheckBox;
    @FXML private Label copyrightLabel;
    @FXML private Button login;

    public static int loggedInUserId;
    private ResourceBundle rb;
    private Connection con;
    private PreparedStatement stmt;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setResourceBundle(rb);
        bindShowPasswordFields();
        setCopyright();
    }

    private void setResourceBundle(ResourceBundle rb) {
        this.rb = rb;
        if (Locale.getDefault().toString().contains("es")) {
            setLoginLocale();
        }
    }

    private void setLoginLocale() {
        usernameTextField.setPromptText(rb.getString("username"));
        passwordField.setPromptText(rb.getString("password"));
        visiblePasswordTextField.setPromptText(rb.getString("password"));
        showPasswordCheckBox.setText(rb.getString("show"));
        login.setText(rb.getString("login"));
    }

    private void bindShowPasswordFields() {
        visiblePasswordTextField.textProperty().bindBidirectional(passwordField.textProperty());
//        visiblePasswordTextField.layoutXProperty().bindBidirectional(passwordField.layoutXProperty());
        visiblePasswordTextField.visibleProperty().bind(showPasswordCheckBox.selectedProperty());
        passwordField.visibleProperty().bind(showPasswordCheckBox.selectedProperty().not());
    }

    private void setCopyright() {
        int year = LocalDate.now().getYear();
        copyrightLabel.setText("Copyright © " + year + " Global Consulting Institution");
    }

    @FXML
    public void handleLoginButton() throws IOException, SQLException {
        stmt = getStatement();
        try {
            ResultSet rs = stmt.executeQuery();
            if (isLoginValid(rs)) {
                loggedInUserId = rs.getInt("userId");
                App.loadApp();
            } else {
                getAlert();
            }
        } catch (IOException | SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    private PreparedStatement getStatement() throws SQLException {
        if (con == null || con.isClosed()) {
            con = getConnection();
        }
        String loginString = "select username, password, userId from user where username = ?";
        stmt = con.prepareStatement(loginString);
        stmt.setString(1, usernameTextField.getText());
        return stmt;
    }

    private Boolean isLoginValid(ResultSet rs) throws SQLException {
        return rs.next() && usernameTextField.getText().equals(rs.getString("username"))
            && passwordField.getText().equals(rs.getString("password"));
    }

    private void getAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(rb.getString("title"));
        alert.setHeaderText(null);
        alert.setContentText("\n\t" + rb.getString("message"));
        alert.showAndWait();
    }

}
