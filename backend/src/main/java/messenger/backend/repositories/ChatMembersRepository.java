package messenger.backend.repositories;

import org.springframework.jdbc.datasource.DataSourceUtils;
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

    public void insertNewChatMember(long chatID, long userID) throws SQLException {
        String sql = """
            INSERT INTO chat_members(chat_id, user_id)
            VALUES (?, ?)
            """;

        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, chatID);
            preparedStatement.setLong(2, userID);

            preparedStatement.execute();
        }
        finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    public Set<Long> getAllMembersOfTheChat(long chatID) throws SQLException {
        String sql = """
            SELECT user_id FROM chat_members
            WHERE chat_id = ?;
            """;

        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, chatID);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                Set<Long> userIDsSet = new HashSet<>();

                while (resultSet.next()) {
                    userIDsSet.add(resultSet.getLong("user_id"));
                }

                if (userIDsSet.isEmpty()) {
                    throw new SQLException("Specified chat doesn't have any users");
                }

                return userIDsSet;
            } finally {
                DataSourceUtils.releaseConnection(connection, dataSource);
            }
        }
    }
}
