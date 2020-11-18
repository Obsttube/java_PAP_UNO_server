import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class Database {
 
    private static final String filename = "database.db";
    private static final String url = "jdbc:sqlite:" + filename;
    private static Connection connection;

    public static Connection getConnection() {
        if(connection == null){
            try {
                connection = DriverManager.getConnection(url);
            } catch (SQLException e) {
                System.out.println("getConnection: " + e.getMessage());
            }
        }
        return connection;
    }

    public static void closeConnection(){
        try {
            if (connection != null){
                connection.close();
                connection = null;
            }
        } catch (SQLException e) {
            System.out.println("closeConnection: " + e.getMessage());
        }
    }
    
    private static void initializeDatabase(){
        String sql = "CREATE TABLE IF NOT EXISTS test ("
                + "	id integer PRIMARY KEY,"
                + "	text text NOT NULL);";
        try {
            Statement statement = connection.createStatement();
            statement.execute(sql);
        } catch (SQLException e) {
            System.out.println("initializeDatabase: " + e.getMessage());
        }
    }

    private static void insertTestData(String text){
        String sql = "INSERT INTO test(text) VALUES(?);";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, text);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("insertTestData: " + e.getMessage());
        }
    }

    private static String getTestData(){
        String sql = "SELECT id, text FROM test;";
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                System.out.println(resultSet.getInt("id") + " '" + resultSet.getString("text") + "'");
            }
        } catch (SQLException e) {
            System.out.println("getTestData: " + e.getMessage());
        }
        return null;
    }

    public static void main(String[] args) { // only to test the database
        File file = new File(filename);
        boolean databaseExists = file.exists();
        getConnection();
        if(!databaseExists) {
            initializeDatabase();
            insertTestData("test text 1");
            insertTestData("second test text");
        }
        getTestData();
        closeConnection();
    }
}