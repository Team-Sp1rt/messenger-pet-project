package messenger.backend.repositories;

import messenger.backend.dtos.User;
import org.springframework.jdbc.datasource.DataSourceUtils;
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

    public void insertNewUser(User user) throws SQLException {
        String sql = """
            INSERT INTO users(id, username, bio, birthday)
            VALUES(?, ?, ?, ?);
            """;

        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, user.id());
            preparedStatement.setString(2, user.username());
            preparedStatement.setString(3, user.bio());
            preparedStatement.setObject(4, user.birthday());

            preparedStatement.execute();
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    public User getUserByID(Long id) throws SQLException {
        String sql = """
            SELECT username, bio, birthday FROM users
            WHERE id = ?;
            """;

        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);

            String username;
            String bio;
            LocalDate birthday;

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException("Couldn't get user due to unknown reason");
                }

                username = resultSet.getString("username");
                bio = resultSet.getString("bio");
                birthday = resultSet.getObject("birthday", LocalDate.class);
            }

            return new User(id, username, bio, birthday);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    public void changeUserBioByID(Long id, String newBio) throws SQLException {
        String sql = """
            UPDATE users SET
            bio = ?
            WHERE id = ?
            """;

        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, newBio);
            preparedStatement.setLong(2, id);

            preparedStatement.execute();
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }
}
