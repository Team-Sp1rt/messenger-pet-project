package messenger.backend.repositories;

import messenger.backend.dtos.Message;
import messenger.backend.dtos.NewMessage;
import messenger.backend.exceptions.repostitories.messages.NoSuchMessageException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

@Repository
public class MessagesRepository {
    private final DataSource dataSource;

    public MessagesRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Message insertNewMessageReturnsMessage(NewMessage newMessage) throws SQLException {
        String sql = """
            INSERT INTO messages(chat_id, user_id, content)
            VALUES (?, ?, ?)
            RETURNING *;
            """;

        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, newMessage.chatID());
            preparedStatement.setLong(2, newMessage.userID());
            preparedStatement.setString(3, newMessage.content());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException("Couldn't add message due to unknown reason");
                }

                return mapRowToMessage(resultSet);
            }
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    public Message editMessageReturnsMessage(Long id, String newContent) throws SQLException {
        String sql = """
            UPDATE messages SET
            content = ?
            WHERE id = ?
            RETURNING *;
            """;

        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, newContent);
            preparedStatement.setLong(2, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException("Couldn't edit message content due to unknown reason");
                }

                return mapRowToMessage(resultSet);
            }
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    public void deleteMessage(Long id) throws SQLException {
        String sql = """
            DELETE FROM messages
            WHERE id = ?;
            """;

        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);

            preparedStatement.execute();
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    public List<Message> getLastNMessagesInTheChat(Integer n, Long chatID) throws SQLException {
        String sql = """
            SELECT * FROM messages
            WHERE chat_id = ?
            ORDER BY created_at DESC, id DESC
            LIMIT ?;
            """;

        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, chatID);
            preparedStatement.setInt(2, n);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return mapResultSetToMessagesList(resultSet);
            }
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    public List<Message> getNMessagesInTheChatBeforeMessage(Integer n, Long chatID, Message message) throws SQLException {
        String sql = """
            SELECT * FROM messages
            WHERE chat_id = ? AND created_at < ?
            ORDER BY created_at 
            DESC, id DESC
            LIMIT ?;
            """;

        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, chatID);
            preparedStatement.setObject(2, message.createdAt());
            preparedStatement.setInt(3, n);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return mapResultSetToMessagesList(resultSet);
            }
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    public Long getUserIDByMessageID(Long messageID) throws SQLException{
        String sql = """
            SELECT user_id FROM messages
            WHERE id = ?;
            """;

        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, messageID);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if(!resultSet.next()) {
                    throw new NoSuchMessageException("Couldn't find message with specified id");
                }

                return resultSet.getLong("user_id");
            }
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }


    private Message mapRowToMessage(ResultSet resultSet) throws SQLException {
        return new Message(
                resultSet.getLong("id"),
                resultSet.getLong("chat_id"),
                resultSet.getLong("user_id"),
                resultSet.getString("content"),
                resultSet.getTimestamp("created_at")
        );
    }

    private List<Message> mapResultSetToMessagesList(ResultSet resultSet) throws SQLException {
        List<Message> messagesList = new LinkedList<>();

        while (resultSet.next()) {
            messagesList.add(mapRowToMessage(resultSet));
        }

        return messagesList;
    }
}
