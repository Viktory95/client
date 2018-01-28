import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

import java.io.*;
import java.sql.*;
import java.util.prefs.Preferences;

public class Controller {

    @FXML
    TextField login;

    @FXML
    TextField password;

    @FXML
    TextField db_name;

    @FXML
    TextField url;

    @FXML
    TextArea logsArea;

    @FXML
    TextArea inputArea;

    Preferences prefs = Preferences.userNodeForPackage(Controller.class);

    public void setPref() {
//получение логина пароля и названия бд
        prefs.put("url", PropertyLoader.getInstance().getProperty("url"));
        prefs.put("login", PropertyLoader.getInstance().getProperty("db_name"));
        prefs.put("password", PropertyLoader.getInstance().getProperty("login"));
        prefs.put("db_name", PropertyLoader.getInstance().getProperty("password"));

    }

    //кнопка коннект
    public void onConnect() {
        MyConnection myConnection = new MyConnection(url.getText(), db_name.getText(), login.getText(), password.getText());
        PropertyLoader.getInstance().setProperty("url", url.getText());
        PropertyLoader.getInstance().setProperty("db_name", db_name.getText());
        PropertyLoader.getInstance().setProperty("login", login.getText());
        PropertyLoader.getInstance().setProperty("password", password.getText());
    }

    public void onKeyType(KeyEvent event) {

    }

    //кнопка квери
    //принимает все запросы кроме селекта и хранит в PreparedStatement
    public void onQuery() {
        //очистка окна вывода
        logsArea.clear();
        try {
            PreparedStatement ps = MyConnection.connection.prepareStatement(inputArea.getText());
            //записываем набор запросов, отделенных знаком ;
            String[] sqls = inputArea.getText().split(";");
            //проходим по запросам и применяем их
            for (int i = 0; i < sqls.length; i++) {
                ps = MyConnection.connection.prepareStatement(sqls[i]);
                ps.addBatch();
                ResultSet resultSet = ps.executeQuery();
                //вывод результата запросов
                MyConnection.outputResultSet(resultSet, logsArea);

                logsArea.setText(logsArea.getText() + "\n");
            }
        } catch (SQLException e) {
            logsArea.setText(logsArea.getText() + "\n" + e.getMessage());
            MyConnection.getAlert(e.getMessage());
        } catch (Exception e) {
            logsArea.setText(logsArea.getText() + "\n" + e.getMessage());
            MyConnection.getAlert(e.getMessage());
        }
    }


    public void onCommit() {
        logsArea.clear();
        try {
            MyConnection.connection.setAutoCommit(false);
            MyConnection.connection.commit();
            logsArea.setText("DONE");
        } catch (SQLException e) {
            logsArea.setText(e.getMessage());
            MyConnection.getAlert(logsArea.getText() + "\n" + e.getMessage());
        }
    }

    public void onRollback() {
        logsArea.clear();
        try {
            MyConnection.connection.rollback();
            logsArea.setText("DONE");
        } catch (SQLException e) {
            logsArea.setText(e.getMessage());
            MyConnection.getAlert(logsArea.getText() + "\n" + e.getMessage());
        }
    }

    public void onDBCopy() {
        try {
            //открываем потов на запись файлв
            OutputStreamWriter outWriter = new OutputStreamWriter(new FileOutputStream("db_copy.txt"), "utf-8");
            logsArea.clear();
            //получаем данные о базе
            DatabaseMetaData dbmd = MyConnection.connection.getMetaData();
            //получаем данные о таблицах
            ResultSet rs = dbmd.getTables(null, null, null, null);
            String dbCreate = "";//запрос для создания базы
            String tbsCreate = "";//запрос на создание таблицы
            String data = "";//запрос на добавление данных
            //проходим по полученным данным пока они не закончатся (по таблицам)
            while (rs.next()) {
                //берем из ResultSet rs название бд
                dbCreate = rs.getString(1);
                //берем из ResultSet rs название таблицы и формируем запрос на создание таблицы
                tbsCreate = tbsCreate + "\nCREATE TABLE " + dbCreate + "." + rs.getString(3) + "(";
                //выполняем запрос в бд на данные, содержащиеся в текущей таблице
                PreparedStatement ps = MyConnection.connection.prepareStatement("select * from " + rs.getString(3));
                ResultSet resultSet = ps.executeQuery();
                //продолжение формирования запроса по созданию таблицы (вносим все колонки и их типы)
                ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                int columnCount = resultSetMetaData.getColumnCount();
                for (int i = 0; i < columnCount; i++) {
                    tbsCreate += "\n" + resultSetMetaData.getColumnName(i + 1) + " " + resultSetMetaData.getColumnTypeName(i + 1) + " NOT NULL,";
                }
                tbsCreate += "\nPRIMARY KEY (" + resultSetMetaData.getColumnName(1) + "));";

                //формирование запросов на добавление данных
                while (resultSet.next()) {
                    String columns = "";
                    String datas = "";
                    for (int i = 0; i < columnCount; i++) {
                        columns += (i != columnCount - 1 ? resultSetMetaData.getColumnName(i + 1) + "," : resultSetMetaData.getColumnName(i + 1));
                        datas += (i != columnCount - 1 ? "'" + resultSet.getObject(i + 1) + "'," : "'" + resultSet.getObject(i + 1) + "'");
                    }
                    data += "\nINSERT INTO " + rs.getString(3) + "(" + columns + ") VALUES (" + datas + ");";
                }
            }

            //формирование запроса на создание бд
            dbCreate = "CREATE SCHEMA " + dbCreate + ";";

            //записываем все в файл
            outWriter.write(dbCreate + "\n" + "\n" + tbsCreate + data);
            //закрываем файл
            outWriter.close();
            logsArea.setText("DONE");
        } catch (FileNotFoundException e) {
            logsArea.setText(logsArea.getText() + "\n" + e.getMessage());
        } catch (SQLException e) {
            logsArea.setText(logsArea.getText() + "\n" + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            logsArea.setText(logsArea.getText() + "\n" + e.getMessage());
        } catch (IOException e) {
            logsArea.setText(logsArea.getText() + "\n" + e.getMessage());
        }
    }

    //выполнение запросов типа insert, delete, update
    public void onSql() {
        logsArea.clear();
        try {
            //берем все запросы и разделяем по символу ";"
            String[] sqls = inputArea.getText().split(";");
            //запрещаем автоматическое внесение изменений в бд
            MyConnection.connection.setAutoCommit(false);
            //вывполняем все запросы
            for (int i = 0; i < sqls.length; i++) {
                MyConnection.ps = MyConnection.connection.prepareStatement(sqls[i]);
                MyConnection.ps.addBatch();
                MyConnection.ps.execute();
            }
            logsArea.setText("DONE");
        } catch (SQLException e) {
            logsArea.setText(logsArea.getText() + "\n" + e.getMessage());
            MyConnection.getAlert(e.getMessage());
        } catch (Exception e) {
            logsArea.setText(logsArea.getText() + "\n" + e.getMessage());
            MyConnection.getAlert(e.getMessage());
        }
    }


}