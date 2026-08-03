package messenger.backend.repositories;

import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;

@Repository
public class UserRepository {
    private final DataSource dataSource;

    public UserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void createNewUser(String login, String password, String username, LocalDate birthday) throws SQLException {
        long id = insertNewAuthorisationReturnsUserID(login, password);
        insertNewUser(id, username, birthday);
    }

    private long insertNewAuthorisationReturnsUserID(String login, String password) throws SQLException {
        Connection connection = dataSource.getConnection();

        String sql = """
            INSERT INTO authorisation(login, password)
            VALUES(?, ?)
            RETURNING id;
            """;

        PreparedStatement preparedStatement = connection.prepareStatement(sql);

        preparedStatement.setString(1, login);
        preparedStatement.setString(2, password);

        ResultSet resultSet = preparedStatement.executeQuery();

        long id = 0;

        while(resultSet.next()) {
            id = resultSet.getLong("id");
        }

        resultSet.close();
        preparedStatement.close();
        connection.close();
        return id;
    }

    private void insertNewUser(long id, String username, LocalDate birthday) throws SQLException {
        Connection connection = dataSource.getConnection();

        String sql = """
            INSERT INTO users(id, username, birthday)
            VALUES(?, ?, ?);
            """;

        PreparedStatement preparedStatement = connection.prepareStatement(sql);

        preparedStatement.setLong(1, id);
        preparedStatement.setString(2, username);
        preparedStatement.setObject(3, birthday);

        preparedStatement.execute();

        preparedStatement.close();
        connection.close();
    }

    public void showEverything() throws Exception{
        Connection connection = dataSource.getConnection();

        String sql = "SELECT * FROM users";

        PreparedStatement preparedStatement = connection.prepareStatement(sql);

        ResultSet resultSet = preparedStatement.executeQuery();

        long id;
        String username;
        String bio;
        LocalDate birthday;

        while(resultSet.next()) {
            id = resultSet.getLong("id");
            username = resultSet.getString("username");
            bio = resultSet.getString("bio");
            birthday = resultSet.getObject("birthday", LocalDate.class);
            System.out.println(id + " " + username + " " + bio + " " + birthday);
        }

        resultSet.close();
        preparedStatement.close();
        connection.close();
    }
}
