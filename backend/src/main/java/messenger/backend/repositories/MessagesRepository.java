package messenger.backend.repositories;

import messenger.backend.dtos.MessageDto;
import messenger.backend.dtos.NewMessage;
import messenger.backend.exceptions.repostitories.NoSuchMessageException;
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

    public MessageDto insertNewMessageReturnsMessage(NewMessage newMessage) throws SQLException {
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
                resultSet.next();
                return mapRowToMessage(resultSet);
            }
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    public MessageDto editMessageReturnsMessage(Long id, String newContent) throws SQLException, NoSuchMessageException {
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
                    throw new NoSuchMessageException("There is no message with specified id");
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

    public List<MessageDto> getLastNMessagesInTheChat(Integer n, Long chatID) throws SQLException {
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

    public List<MessageDto> getNMessagesInTheChatBeforeMessageWithID(Integer n, Long chatID, Long messageID) throws SQLException, NoSuchMessageException {
        MessageDto message = getMessageById(messageID);
        return getNMessagesInTheChatBeforeMessage(n, chatID, message);
    }

    public List<MessageDto> getNMessagesInTheChatBeforeMessage(Integer n, Long chatID, MessageDto message) throws SQLException {
        String sql = """
            SELECT * FROM messages
            WHERE chat_id = ? AND created_at < ?
            ORDER BY created_at DESC, id DESC
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

    public Long getUserIDByMessageID(Long messageID) throws SQLException, NoSuchMessageException {
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

    public MessageDto getMessageById(Long messageId) throws SQLException, NoSuchMessageException {
        String sql = """
        SELECT *
        FROM messages
        WHERE id = ?;
        """;

        Connection connection = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setLong(1, messageId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new NoSuchMessageException("Message with id " + messageId + " was not found");
                }

                return mapRowToMessage(resultSet);
            }
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private MessageDto mapRowToMessage(ResultSet resultSet) throws SQLException {
        return new MessageDto(
                resultSet.getLong("id"),
                resultSet.getLong("chat_id"),
                resultSet.getLong("user_id"),
                resultSet.getString("content"),
                resultSet.getTimestamp("created_at")
        );
    }

    private List<MessageDto> mapResultSetToMessagesList(ResultSet resultSet) throws SQLException {
        List<MessageDto> messagesList = new LinkedList<>();

        while (resultSet.next()) {
            messagesList.add(mapRowToMessage(resultSet));
        }

        return messagesList;
    }
}
