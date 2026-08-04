package messenger.backend.repositories;

import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Repository
public class AuthorisationRepository {
    private final DataSource dataSource;

    public AuthorisationRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void showEverything() throws Exception{
        Connection connection = dataSource.getConnection();

        String sql = "SELECT * FROM authorisation";

        PreparedStatement preparedStatement = connection.prepareStatement(sql);

        ResultSet resultSet = preparedStatement.executeQuery();

        long id;
        String login;
        String passwordHash;

        while(resultSet.next()) {
            id = resultSet.getLong("id");
            login = resultSet.getString("login");
            passwordHash = resultSet.getString("password_hash");
            System.out.println(id + " " + login + " " + passwordHash);
        }

        resultSet.close();
        preparedStatement.close();
        connection.close();
    }
}
