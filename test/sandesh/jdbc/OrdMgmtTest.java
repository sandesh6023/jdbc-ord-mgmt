package sandesh.jdbc;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static junit.framework.Assert.assertEquals;

public class OrdMgmtTest {
    static java.sql.Connection conn = null;
    static Statement stmt = null;

    @BeforeClass
    public static void createConnection() throws Exception {
        final String DatatbaseUrl = "jdbc:mysql://localhost";

        final String userName = "root";
        final String password = "Sandy6023!";
        Class.forName("org.mariadb.jdbc.Driver");
        conn = DriverManager.getConnection(DatatbaseUrl, userName, password);
        stmt = conn.createStatement();

        String sql = "CREATE SCHEMA ordMgmt";
        assertEquals(1, stmt.executeUpdate(sql));

        try {

            String createProductTable = "CREATE TABLE ordMgmt.product (\n" +
                    "\tprod_id INT PRIMARY KEY AUTO_INCREMENT,\n" +
                    "\tprod_name VARCHAR(30),\n" +
                    "\tunit_price FLOAT\n" +
                    ");";
            assertEquals(0, stmt.executeUpdate(createProductTable));

            String createCustomerTable = "CREATE TABLE ordMgmt.customer (\n" +
                    "\tcust_id INT PRIMARY KEY AUTO_INCREMENT,\n" +
                    "\tcust_name VARCHAR(30),\n" +
                    "\taddress VARCHAR(30),\n" +
                    "\tcity VARCHAR(30),\n" +
                    "\tstate VARCHAR(30),\n" +
                    "\tcontact BIGINT\n" +
                    ")";
            assertEquals(0, stmt.executeUpdate(createCustomerTable));

            String createOrderInfoTable = "CREATE TABLE ordMgmt.orderInfo (\n" +
                    "\torder_id INT PRIMARY KEY AUTO_INCREMENT,\n" +
                    "\tcust_id INT,\n" +
                    "\tdate_of_order DATETIME,\n" +
                    "\tdelivery_date DATETIME \n" +
                    ")";
            assertEquals(0, stmt.executeUpdate(createOrderInfoTable));


            String createOrderItemTable = "CREATE TABLE ordMgmt.orderItems (\n" +
                    "\torder_item_id INT PRIMARY KEY AUTO_INCREMENT,\n" +
                    "\torder_id INT,\n" +
                    "\tprod_id INT,\n" +
                    "\tquantity INT,\n" +
                    "\titem_price FLOAT\n" +
                    ")";
            assertEquals(0, stmt.executeUpdate(createOrderItemTable));


            String addForeignKeyToOrderInfo = "ALTER TABLE ordMgmt.orderInfo \n" +
                    "\tADD CONSTRAINT orderInfo_custID_fk FOREIGN KEY(cust_id)\n" +
                    "\tREFERENCES customer(cust_id);\n";
            assertEquals(0, stmt.executeUpdate(addForeignKeyToOrderInfo));

            String addForeignKeyToOrderItems = "ALTER TABLE ordMgmt.orderItems \n" +
                    "\tADD CONSTRAINT orderIems_orderId_fk FOREIGN KEY(order_id)\n" +
                    "\tREFERENCES orderInfo(order_id)\n";
            assertEquals(0, stmt.executeUpdate(addForeignKeyToOrderItems));

            String addForeignKeyToOrderItemsForProduct = "ALTER TABLE ordMgmt.orderItems \n" +
                    "\tADD CONSTRAINT orderIems_prodId_fk FOREIGN KEY(prod_id)\n" +
                    "\tREFERENCES product(prod_id);\n";
            assertEquals(0, stmt.executeUpdate(addForeignKeyToOrderItemsForProduct));

        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testInsertSingleRecordIntoProductTable() throws Exception {
        String firstRecord = "INSERT INTO ordMgmt.product(prod_name,unit_price) VALUES('Pencil',10)";
        assertEquals(1, stmt.executeUpdate(firstRecord));

    }

    @Test
    public void testInsertMultipleRecordsIntoProductTable() throws Exception {
        String secondRecord = "INSERT INTO ordMgmt.product(prod_name,unit_price) VALUES('Notebook',30)";
        assertEquals(1, stmt.executeUpdate(secondRecord));

        String thirdRecord = "INSERT INTO ordMgmt.product(prod_name,unit_price) VALUES('sharpner',3)";
        assertEquals(1, stmt.executeUpdate(thirdRecord));

        String forthRecord = "INSERT INTO ordMgmt.product(prod_name,unit_price) VALUES('geometry-box',75)";
        assertEquals(1, stmt.executeUpdate(forthRecord));
    }

    @Test
    public void testForSelectingRequiredRecordsFromProductTable() throws Exception {
        String sql = "SELECT * from ordMgmt.product;";
        ResultSet res = stmt.executeQuery(sql);

        String[] productName = {"Pencil", "Notebook", "sharpner", "geometry-box"};
        int[] unitPrice = {10, 30, 3, 75};

        while (res.next()) {
            assertEquals(productName[res.getRow() - 1], res.getString("prod_name"));
            assertEquals(unitPrice[res.getRow() - 1], res.getInt("unit_price"));
        }
    }

    @Test
    public void testForInsertRecordIntoCustomerTable() throws Exception {
        String sql1 = "INSERT INTO ordMgmt.customer(cust_name ,address ,city ,state ,contact ) VALUES('Sandesh','3rd block','Kormangala','Karnataka',8123852388);";
        assertEquals(1, stmt.executeUpdate(sql1));
    }

    @Test
    public void testForInsertingRecordIntoOrderInfoTable() throws Exception {

        String inOrderInfo = "INSERT INTO ordMgmt.orderInfo(cust_id,date_of_order,delivery_date) VALUES(1,now(),now())";
        assertEquals(1, stmt.executeUpdate(inOrderInfo));

    }

    @Test
    public void testForInsertingRecordIntoOrderItemsUsingSelectSubquery() throws Exception {
        String inOrderItem = "INSERT INTO ordMgmt.orderItems(order_id,prod_id,quantity,item_price)VALUES((SELECT MAX(order_id) from ordMgmt.orderInfo where cust_id =1),(SELECT prod_id from ordMgmt.product where prod_name='Pen'),10,(SELECT 10*unit_price from ordMgmt.product where prod_name='Pen'))";
        assertEquals(1, stmt.executeUpdate(inOrderItem));
    }

    @Test
    public void testForGeneratingPaymentUsingSelectQuery() throws Exception {
        ResultSet res;
        String gettingPayment = "SELECT ordMgmt.orderItems.order_id,ordMgmt.customer.cust_name,ordMgmt.orderItems.item_price " +
                "FROM ordMgmt.orderItems INNER JOIN ordMgmt.orderInfo INNER JOIN ordMgmt.customer " +
                "ON ordMgmt.orderItems.order_id = ordMgmt.orderInfo.order_id AND ordMgmt.orderInfo.cust_id = ordMgmt.customer.cust_id WHERE ordMgmt.orderItems.order_id IN (SELECT MAX(order_id) from ordMgmt.orderItems)";
        res = stmt.executeQuery(gettingPayment);

        while (res.next()) {
            assertEquals(1, res.getInt(1));
            assertEquals("Sandesh", (res.getString(2)));
            assertEquals(100, res.getInt(3));
        }
    }

    @Test
    public void testForDeleteRecordFromProductTable() throws Exception {
        String firstRecord = "DELETE FROM ordMgmt.product WHERE prod_name ='Pen'";
        ResultSet res = stmt.executeQuery(firstRecord);
        assertEquals(0, res.getRow());
    }

    @AfterClass
    public static void tearDown() throws Exception {
        try {
            String dropQuery = "DROP SCHEMA ordMgmt";
            stmt.executeUpdate(dropQuery);
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
