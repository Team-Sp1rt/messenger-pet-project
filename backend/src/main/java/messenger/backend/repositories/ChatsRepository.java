package messenger.backend.repositories;

import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class ChatsRepository {
    private final DataSource dataSource;

    public ChatsRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Long insertNewChatReturnsChatID() throws SQLException {
        String sql = """
            INSERT INTO chats
            DEFAULT VALUES
            RETURNING id;
            """;

        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            resultSet.next();
            return resultSet.getLong("id");
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }
}
