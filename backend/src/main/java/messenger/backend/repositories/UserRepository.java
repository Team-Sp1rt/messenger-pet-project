package messenger.backend.repositories;

import messenger.backend.DTOs.User;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.Objects;

@Repository
public class UserRepository {
    private final DataSource dataSource;

    public UserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void createNewUser(String login, String passwordHash, String username, LocalDate birthday) throws SQLException {
        long id = insertNewAuthorisationReturnsUserID(login, passwordHash);
        insertNewUser(id, username, birthday);
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

    private void insertNewUser(long id, String username, LocalDate birthday) throws SQLException {
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

    public User getUserByLogin(String login) throws SQLException {
        long userID = getUserIDByLogin(login);
        return getUserByID(userID);
    }

    private long getUserIDByLogin(String login) throws SQLException {
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

    private User getUserByID(long id) throws SQLException {
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
