import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;

import java.sql.*;

/**
 * Created by Vi on 19.10.2016.
 */
public class MyConnection {
    public static PreparedStatement ps;
    public static Connection connection;

    //устанавливаем соединение с бд
    MyConnection(String url, String db_name, String name, String pass) {
        try {
            connection = DriverManager.getConnection(url + db_name, name, pass);
        } catch (Exception ex) {
            getAlert(ex.getMessage());
        }
    }

    public static void getAlert(String str) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error Dialog");
        alert.setHeaderText("Look, an Error Dialog");
        alert.setContentText(str);

        alert.showAndWait();
    }

    public static void outputResultSet(ResultSet rs, TextArea resultText) throws Exception {
        ResultSetMetaData rsMetaData = rs.getMetaData();
        int numberOfColumns = rsMetaData.getColumnCount();
        for (int i = 1; i < numberOfColumns + 1; i++) {
            String columnName = rsMetaData.getColumnName(i);
            resultText.setText(resultText.getText() + columnName + "   ");

        }
        resultText.setText(resultText.getText() + "\n----------------------\n");

        while (rs.next()) {
            for (int i = 1; i < numberOfColumns + 1; i++) {
                resultText.setText(resultText.getText() + rs.getString(i) + "   ");
            }
            resultText.setText(resultText.getText() + "\n");
        }
    }
}
