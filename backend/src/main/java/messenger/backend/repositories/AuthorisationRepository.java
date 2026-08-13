package messenger.backend.repositories;

import org.springframework.jdbc.datasource.DataSourceUtils;
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

    public Long insertNewAuthorisationReturnsUserID(String login, String passwordHash) throws SQLException {
        String sql = """
            INSERT INTO authorisation(login, password_hash)
            VALUES(?, ?)
            RETURNING id;
            """;

        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, login);
            preparedStatement.setString(2, passwordHash);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                long id;

                if(!resultSet.next()) {
                    throw new SQLException("Couldn't add new authorisation data due to unknown reason");
                }

                id = resultSet.getLong("id");

                return id;
            }
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    public String getPasswordHashByLogin(String login) throws SQLException {
        String sql = """
            SELECT password_hash FROM authorisation
            WHERE login = ?
            """;

        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, login);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                String passwordHash;

                if(!resultSet.next()) {
                    throw new SQLException("Couldn't get password hash due to unknown reason");
                }

                passwordHash = resultSet.getString("password_hash");

                return passwordHash;
            }
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    public Long getUserIDByLogin(String login) throws SQLException {
        String sql = """
            SELECT id FROM authorisation
            WHERE login = ?
            """;

        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, login);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                long id;

                if(!resultSet.next()) {
                    throw new SQLException("Couldn't get user id due to unknown reason");
                }

                id = resultSet.getLong("id");

                return id;
            }
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }
}
