package messenger.backend.repositories;

import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

@Repository
public class MessagesRepository {
    private final DataSource dataSource;

    public MessagesRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
