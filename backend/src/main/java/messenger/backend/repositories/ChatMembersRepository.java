package messenger.backend.repositories;

import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

@Repository
public class ChatMembersRepository {
    private final DataSource dataSource;

    public ChatMembersRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private void insertNewChatMember(long chatID, long userID, Connection connection) throws SQLException {
        String sql = """
            INSERT INTO chat_members(chat_id, user_id)
            VALUES (?, ?)
            """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setLong(1, chatID);
            preparedStatement.setLong(2, userID);

            preparedStatement.execute();
        }
    }

    public void insertNewChatMember(long chatID, long userID) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            insertNewChatMember(chatID, userID, connection);
        }
    }

    public void insertNewChatMembers(long chatID, Set<Long> userIDs) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                for (long userID : userIDs) {
                    insertNewChatMember(chatID, userID, connection);
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public Set<Long> getAllMembersOfSpecifiedChat(long chatID) throws SQLException {
        String sql = """
            SELECT user_id FROM chat_members
            WHERE chat_id = ?;
            """;

        Set<Long> userIDsSet = new HashSet<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setLong(1, chatID);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    userIDsSet.add(resultSet.getLong("user_id"));
                }
            }
        }

        if (userIDsSet.isEmpty()) {
            throw new SQLException("Specified chat doesn't have any users");
        }

        return userIDsSet;
    }
}
