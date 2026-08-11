package messenger.backend.repositories;

import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import javax.xml.crypto.Data;
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

    public long insertNewChatReturnsChatID() throws SQLException {
        String sql = """
            INSERT INTO chats
            DEFAULT VALUES
            RETURNING id;
            """;

        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            long id = 0;

            if(resultSet.next()) {
                id = resultSet.getLong("id");
            }

            if (id == 0) {
                throw new SQLException("Couldn't insert new chat due to unknown reason");
            }

            return id;
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    @Deprecated
    public void showEverything() throws SQLException {
        Connection connection = dataSource.getConnection();

        String sql = "SELECT * FROM chats";

        PreparedStatement preparedStatement = connection.prepareStatement(sql);

        ResultSet resultSet = preparedStatement.executeQuery();

        long id;

        while(resultSet.next()) {
            id = resultSet.getLong("id");
            System.out.println(id);
        }

        resultSet.close();
        preparedStatement.close();
        connection.close();
    }
}
