package messenger.backend.messaging;

import messenger.backend.dtos.ChatCreatedEvent;
import messenger.backend.dtos.ChatEventType;
import messenger.backend.generated.model.Chat;
import messenger.backend.generated.model.CreateChatRequest;
import messenger.backend.services.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@SpringBootTest
@Testcontainers
@SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED)
@Sql(
        scripts = {
                "/fixtures/cleanup.sql",
                "/fixtures/chat-created/setup.sql"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@Sql(
        scripts = "/fixtures/cleanup.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
class ChatCreatedTransactionIntegrationTest {

    @Container
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:16")
                    .withDatabaseName("messenger")
                    .withUsername("messenger")
                    .withPassword("messenger");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private ChatService chatService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @MockitoBean
    private SimpMessagingTemplate messagingTemplate;

    @Test
    void createChat_sendsNotificationsOnlyAfterCommit() {
        TransactionTemplate transactionTemplate =
                new TransactionTemplate(transactionManager);

        Chat chat = transactionTemplate.execute(status -> {
            Chat created = chatService.createChat(
                    new CreateChatRequest(2L),
                    1L
            );

            verifyNoInteractions(messagingTemplate);

            return created;
        });

        assertNotNull(chat);

        ChatCreatedEvent notification =
                new ChatCreatedEvent(ChatEventType.CHAT_CREATED, chat);

        verify(messagingTemplate).convertAndSendToUser(
                "1",
                "/queue/chats",
                notification
        );
        verify(messagingTemplate).convertAndSendToUser(
                "2",
                "/queue/chats",
                notification
        );
        verifyNoMoreInteractions(messagingTemplate);
    }
}