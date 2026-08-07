package messenger.backend.repositories;

import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class AuthorisationRepository {
    private final DataSource dataSource;

    public AuthorisationRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public long insertNewAuthorisationReturnsUserID(String login, String passwordHash) throws SQLException {
        String sql = """
            INSERT INTO authorisation(login, password_hash)
            VALUES(?, ?)
            RETURNING id;
            """;

        long id = 0;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, login);
            preparedStatement.setString(2, passwordHash);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if(resultSet.next()) {
                    id = resultSet.getLong("id");
                }
            }
        }

        if (id == 0) {
            throw new SQLException("Couldn't add new authorisation data due to unknown reason");
        }

        return id;
    }

    public String getPasswordHashByLogin(String login) throws SQLException {
        String sql = """
            SELECT password_hash FROM authorisation
            WHERE login = ?
            """;

        String passwordHash = "";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, login);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if(resultSet.next()) {
                    passwordHash = resultSet.getString("password_hash");
                }
            }
        }

        if (passwordHash.isEmpty()) {
            throw new SQLException("Couldn't get password hash due to unknown reason");
        }

        return passwordHash;
    }

    public long getUserIDByLogin(String login) throws SQLException {
        String sql = """
            SELECT id FROM authorisation
            WHERE login = ?
            """;

        long id = 0;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, login);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if(resultSet.next()) {
                    id = resultSet.getLong("id");
                }
            }
        }

        if (id == 0) {
            throw new SQLException("Couldn't get user id due to unknown reason");
        }

        return id;
    }

    @Deprecated
    public void showEverything() throws Exception{
        Connection connection = dataSource.getConnection();

        String sql = "SELECT * FROM authorisation";

        PreparedStatement preparedStatement = connection.prepareStatement(sql);

        ResultSet resultSet = preparedStatement.executeQuery();

        long id;
        String login;
        String passwordHash;

        while(resultSet.next()) {
            id = resultSet.getLong("id");
            login = resultSet.getString("login");
            passwordHash = resultSet.getString("password_hash");
            System.out.println(id + " " + login + " " + passwordHash);
        }

        resultSet.close();
        preparedStatement.close();
        connection.close();
    }
}
