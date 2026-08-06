package messenger.backend.repositories;

import messenger.backend.dtos.User;
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

    public void insertNewUser(long id, String username, LocalDate birthday) throws SQLException {
        String sql = """
            INSERT INTO users(id, username, birthday)
            VALUES(?, ?, ?);
            """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setLong(1, id);
            preparedStatement.setString(2, username);
            preparedStatement.setObject(3, birthday);

            preparedStatement.execute();
        }
    }

    public User getUserByID(long id) throws SQLException {
        String sql = """
            SELECT username, bio, birthday FROM users
            WHERE id = ?;
            """;

        String username = "";
        String bio = "";
        LocalDate birthday = LocalDate.of(1, 1, 1);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setLong(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while(resultSet.next()) {
                    username = resultSet.getString("username");
                    bio = resultSet.getString("bio");
                    birthday = resultSet.getObject("birthday", LocalDate.class);
                }
            }
        }

        if (username.isEmpty() || birthday.isEqual(LocalDate.of(1, 1 ,1))) {
            throw new SQLException("Couldn't get user due to unknown reason");
        }

        return new User(id, username, bio, birthday);
    }

    public void changeUserBioByID(long id, String newBio) throws SQLException {
        String sql = """
            UPDATE users SET
            bio = ?
            WHERE id = ?
            """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, newBio);
            preparedStatement.setLong(2, id);

            preparedStatement.execute();
        }
    }

    @Deprecated
    public void showEverything() throws SQLException{
        String sql = "SELECT * FROM users";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {


            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                long id;
                String username;
                String bio;
                LocalDate birthday;

                while (resultSet.next()) {
                    id = resultSet.getLong("id");
                    username = resultSet.getString("username");
                    bio = resultSet.getString("bio");
                    birthday = resultSet.getObject("birthday", LocalDate.class);
                    System.out.println(id + " " + username + " " + bio + " " + birthday);
                }
            }
        }
    }
}
