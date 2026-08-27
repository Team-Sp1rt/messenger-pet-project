package messenger.backend.repositories;

import messenger.backend.exceptions.repostitories.NoSuchChatException;
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

    public void insertNewChatMember(Long chatID, Long userID) throws SQLException {
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

    public Set<Long> getAllMembersOfTheChat(Long chatID) throws SQLException, NoSuchChatException {
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

                //как будто мы должны гарантировать недостижимость этой ветки и нахуй убрать эту проверку и ошибку
                if (userIDsSet.isEmpty()) {
                    throw new NoSuchChatException("Specified chat doesn't have any users");
                }

                return userIDsSet;
            }
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    public Set<Long> getAllChatsOfTheMember(Long memberID) throws SQLException {
        String sql = """
            SELECT chat_id FROM chat_members
            WHERE user_id = ?;
            """;

        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, memberID);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                Set<Long> chatIDsSet = new HashSet<>();

                while (resultSet.next()) {
                    chatIDsSet.add(resultSet.getLong("chat_id"));
                }

                return chatIDsSet;
            }
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }
}
