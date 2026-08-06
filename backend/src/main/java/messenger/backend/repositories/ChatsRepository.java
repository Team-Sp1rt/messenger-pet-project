package messenger.backend.repositories;

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
