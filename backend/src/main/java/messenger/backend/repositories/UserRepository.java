package messenger.backend.repositories;

import messenger.backend.dtos.User;
import messenger.backend.exceptions.repostitories.users.NoSuchUserException;
import messenger.backend.generated.model.UserSummary;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

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
            SELECT * FROM users
            WHERE id = ?;
            """;

        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new NoSuchUserException("Couldn't get user due to unknown reason");
                }

                return mapRowToUser(resultSet);
            }
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    public List<UserSummary> getNUserSummariesStartingWithSubstring(String substring, Integer n) throws SQLException {
        String sql = """
            SELECT * FROM users
            WHERE username ILIKE ?
            LIMIT ?;
            """;

        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, substring + '%');
            preparedStatement.setInt(2, n);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<UserSummary> foundUsers = mapResultSetToUserSummariesList(resultSet);

                if (foundUsers.isEmpty()) {
                    throw new NoSuchUserException("Couldn't find any user with username like provided");
                }

                return foundUsers;
            }
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

    private User mapRowToUser(ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getLong("id"),
                resultSet.getString("username"),
                resultSet.getString("bio"),
                resultSet.getObject("birthday", LocalDate.class)
        );
    }

    private List<User> mapResultSetToUsersList(ResultSet resultSet) throws SQLException {
        List<User> usersList = new LinkedList<>();

        while (resultSet.next()) {
            usersList.add(mapRowToUser(resultSet));
        }

        return usersList;
    }

    private UserSummary mapRowToUserSummary(ResultSet resultSet) throws SQLException {
        return new UserSummary(
                Long.toString(resultSet.getLong("id")),
                resultSet.getString("username")
        );
    }

    private List<UserSummary> mapResultSetToUserSummariesList(ResultSet resultSet) throws SQLException {
        List<UserSummary> userSummariesList = new LinkedList<>();

        while (resultSet.next()) {
            userSummariesList.add(mapRowToUserSummary(resultSet));
        }

        return userSummariesList;
    }
}
