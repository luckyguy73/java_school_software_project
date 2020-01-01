package gci.utilities;

import static gci.database.ConnectionManager.getConnection;
import gci.models.Customer;
import java.sql.*;
import javafx.collections.*;

public class CustomerDAO implements DAO<Customer> {

    public static final ObservableList<Customer> customers = FXCollections.observableArrayList();
    private Connection con;
    private PreparedStatement stmt;
    private ResultSet rs;

    @Override
    public Customer retrieve(int index) {
        Customer foundCustomer = customers.stream().filter(f -> f.getCustomerId() == index)
            .findFirst().get();
        return foundCustomer;
    }

    @Override
    public ObservableList<Customer> retrieveAll() throws SQLException {
        customers.clear();
        if (con == null || con.isClosed()) {
            con = getConnection();
        }
        String query = "select * from customer_view";
        stmt = con.prepareStatement(query);
        rs = stmt.executeQuery();
        while (rs.next()) {
            customers.add(new Customer(rs.getString(1), rs.getString(2), rs.getString(3),
                rs.getString(4), rs.getString(5), rs.getString(6), rs.getInt(7), rs.getInt(8),
                rs.getInt(9), rs.getInt(10)));
        }
        sortByName();
        return customers;
    }

    private void sortByName() {
        customers.sort((c1, c2) -> c1.getName().compareTo(c2.getName()));
    }

    @Override
    public void create(Customer t) {
        customers.add(t);
        sortByName();
    }

    @Override
    public void update(Customer t) {
        Customer foundCustomer = customers.stream().filter(f -> f.getCustomerId()
            == t.getCustomerId()).findFirst().get();
        int index = customers.indexOf(foundCustomer);
        customers.set(index, t);
        sortByName();
    }

    @Override
    public void delete(Customer t) throws SQLException {
        if (con == null || con.isClosed()) {
            con = getConnection();
        }
        String query = "delete from customer where customerId = ?";
        stmt = con.prepareStatement(query);
        stmt.setInt(1, t.getCustomerId());
        stmt.executeUpdate();
    }

}
