package messenger.backend.messaging;


import messenger.backend.dtos.*;
import messenger.backend.dtos.requests.EditMessageRequest;
import messenger.backend.dtos.requests.SendMessageRequest;
import messenger.backend.exceptions.repostitories.NoSuchMessageException;
import messenger.backend.generated.model.Chat;
import messenger.backend.generated.model.Message;
import messenger.backend.repositories.*;
import messenger.backend.services.JwtService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.lang.reflect.Type;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class WebSocketIntegrationTest {

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

    @LocalServerPort
    private int port;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private AuthorisationRepository authorisationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatsRepository chatsRepository;

    @Autowired
    private MessagesRepository messagesRepository;

    @Autowired
    private ChatMembersRepository chatMembersRepository;

    private WebSocketStompClient stompClient;

    @BeforeEach
    void setUp() {
        stompClient = new WebSocketStompClient(
                new StandardWebSocketClient()
        );

        stompClient.setMessageConverter(
                new JacksonJsonMessageConverter()
        );
    }

    @AfterEach
    void tearDown() {
        stompClient.stop();
    }

    @Test
    void connectWithValidJwt_establishesStompSession() throws Exception {
        String token = jwtService.generateAccessToken(42L, "alex");

        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        String url = "ws://localhost:" + port + "/ws";

        StompSession session = stompClient.connectAsync(
                url,
                new WebSocketHttpHeaders(),
                connectHeaders,
                new StompSessionHandlerAdapter() {},
                new Object[0]
        ).get(5, TimeUnit.SECONDS);

        try {
            assertTrue(session.isConnected());
        } finally {
            session.disconnect();
        }
    }

    @Test
    void connectWithoutJwt_doesNotEstablishStompSession() throws Exception {
        StompHeaders connectHeaders = new StompHeaders();

        String url = "ws://localhost:" + port + "/ws";

        CompletableFuture<String> stompError = new CompletableFuture<>();

        StompSessionHandlerAdapter sessionHandler =
                new StompSessionHandlerAdapter() {
                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        stompError.complete(headers.getFirst("message"));
                    }
                };

        CompletableFuture<StompSession> connection = stompClient.connectAsync(
                url,
                new WebSocketHttpHeaders(),
                connectHeaders,
                sessionHandler);

        ExecutionException exception = assertThrows(ExecutionException.class, () -> connection.get(5, TimeUnit.SECONDS));
        String errorMessage = stompError.get(5, TimeUnit.SECONDS);
        assertTrue(errorMessage.contains("UNAUTHORIZED: JWT is missing"));

    }

    @Test
    void connectWithExpiredJwt_doesNotEstablishStompSession() throws Exception {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject("42")
                .claim("username", "alex")
                .issuedAt(now.minusSeconds(300))
                .expiresAt(now.minusSeconds(120))
                .build();

        String expiredToken = jwtEncoder
                .encode(JwtEncoderParameters.from(claims))
                .getTokenValue();

        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken);

        String url = "ws://localhost:" + port + "/ws";

        CompletableFuture<String> stompError = new CompletableFuture<>();

        StompSessionHandlerAdapter sessionHandler =
                new StompSessionHandlerAdapter() {
                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        stompError.complete(headers.getFirst("message"));
                    }
                };

        CompletableFuture<StompSession> connection = stompClient.connectAsync(
                url,
                new WebSocketHttpHeaders(),
                connectHeaders,
                sessionHandler);


        ExecutionException exception = assertThrows(ExecutionException.class, () -> connection.get(5, TimeUnit.SECONDS));
        String errorMessage = stompError.get(5, TimeUnit.SECONDS);
        assertTrue(errorMessage.contains("UNAUTHORIZED: JWT is invalid or expired"));
    }


    @Test
    public void sendMessage_validRequest_savesMessageAndPublishesEvent() throws ExecutionException, InterruptedException, TimeoutException, SQLException, NoSuchMessageException {
        Long userId = authorisationRepository.insertNewAuthorisationReturnsUserID("User3", "333");

        User user = new User(userId, "User3", "woman", LocalDate.of(2003, 1, 25));

        userRepository.insertNewUser(user);

        Long chatId = chatsRepository.insertNewChatReturnsChatID();

        chatMembersRepository.insertNewChatMember(chatId, user.id());

        String token = jwtService.generateAccessToken(
                user.id(),
                user.username()
        );

        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        String url = "ws://localhost:" + port + "/ws";

        StompSession session = stompClient.connectAsync(
                url,
                new WebSocketHttpHeaders(),
                connectHeaders,
                new StompSessionHandlerAdapter() {},
                new Object[0]
        ).get(5, TimeUnit.SECONDS);

        CompletableFuture<MessageChangedEvent> received =
                new CompletableFuture<>();

        StompHeaders subscribeHeaders = new StompHeaders();

        subscribeHeaders.setDestination(
                "/topic/chats/" + chatId + "/events"
        );

        subscribeHeaders.add(
                HttpHeaders.AUTHORIZATION,
                "Bearer " + token
        );

        session.subscribe(
                subscribeHeaders,
                new StompFrameHandler() {

                    @Override
                    public @NotNull Type getPayloadType(@NotNull StompHeaders headers) {
                        return MessageChangedEvent.class;
                    }

                    @Override
                    public void handleFrame(
                            @NotNull StompHeaders headers,
                            Object payload) {

                        received.complete(
                                (MessageChangedEvent) payload
                        );
                    }
                }
        );

        StompHeaders sendHeaders = new StompHeaders();

        sendHeaders.setDestination(
                "/app/chats/" + chatId + "/messages"
        );

        sendHeaders.add(
                HttpHeaders.AUTHORIZATION,
                "Bearer " + token
        );

        session.send(
                sendHeaders,
                new SendMessageRequest("Successful test")
        );

        MessageChangedEvent event =
                received.get(5, TimeUnit.SECONDS);

        assertEquals(
                ChatEventType.MESSAGE_CREATED,
                event.type()
        );

        Message message = event.message();

        assertEquals("Successful test", message.getContent());
        assertEquals(chatId, message.getChatId());
        assertEquals(user.id(), message.getUserId());

        Long savedUserId = messagesRepository.getUserIDByMessageID(message.getId());

        assertEquals(user.id(), savedUserId);

    }

    @Test
    public void editMessage_validRequest_savesMessageAndPublishesEvent() throws ExecutionException, InterruptedException, TimeoutException, SQLException, NoSuchMessageException {
        Long userId = authorisationRepository.insertNewAuthorisationReturnsUserID("User33", "333");

        User user = new User(userId, "User33", "woman", LocalDate.of(2003, 1, 25));

        userRepository.insertNewUser(user);

        Long chatId = chatsRepository.insertNewChatReturnsChatID();

        chatMembersRepository.insertNewChatMember(chatId, user.id());

        NewMessage newMessage = new NewMessage(chatId, user.id(), "Message");

        messenger.backend.dtos.Message oldMessage = messagesRepository.insertNewMessageReturnsMessage(newMessage);

        String token = jwtService.generateAccessToken(
                user.id(),
                user.username()
        );

        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        String url = "ws://localhost:" + port + "/ws";

        StompSession session = stompClient.connectAsync(
                url,
                new WebSocketHttpHeaders(),
                connectHeaders,
                new StompSessionHandlerAdapter() {},
                new Object[0]
        ).get(5, TimeUnit.SECONDS);

        CompletableFuture<MessageChangedEvent> received =
                new CompletableFuture<>();

        StompHeaders subscribeHeaders = new StompHeaders();

        subscribeHeaders.setDestination(
                "/topic/chats/" + chatId + "/events"
        );

        subscribeHeaders.add(
                HttpHeaders.AUTHORIZATION,
                "Bearer " + token
        );

        session.subscribe(
                subscribeHeaders,
                new StompFrameHandler() {

                    @Override
                    public @NotNull Type getPayloadType(@NotNull StompHeaders headers) {
                        return MessageChangedEvent.class;
                    }

                    @Override
                    public void handleFrame(
                            @NotNull StompHeaders headers,
                            Object payload) {

                        received.complete(
                                (MessageChangedEvent) payload
                        );
                    }
                }
        );

        StompHeaders sendHeaders = new StompHeaders();

        sendHeaders.setDestination(
                "/app/chats/" + chatId + "/messages/" + oldMessage.id() + "/edit"
        );

        sendHeaders.add(
                HttpHeaders.AUTHORIZATION,
                "Bearer " + token
        );

        session.send(
                sendHeaders,
                new EditMessageRequest("Successful test")
        );

        MessageChangedEvent event =
                received.get(5, TimeUnit.SECONDS);

        assertEquals(
                ChatEventType.MESSAGE_UPDATED,
                event.type()
        );

        Message message = event.message();

        assertEquals("Successful test", message.getContent());
        assertEquals(chatId, message.getChatId());
        assertEquals(user.id(), message.getUserId());
        assertEquals(oldMessage.id(), message.getId());
        assertNotEquals(oldMessage.content(), message.getContent());

    }

    @Test
    public void deleteMessage_validRequest_PublishesEvent() throws ExecutionException, InterruptedException, TimeoutException, SQLException, NoSuchMessageException {
        Long userId = authorisationRepository.insertNewAuthorisationReturnsUserID("User333", "333");

        User user = new User(userId, "User333", "woman", LocalDate.of(2003, 1, 25));

        userRepository.insertNewUser(user);

        Long chatId = chatsRepository.insertNewChatReturnsChatID();

        chatMembersRepository.insertNewChatMember(chatId, user.id());

        NewMessage newMessage = new NewMessage(chatId, user.id(), "Message");

        messenger.backend.dtos.Message oldMessage = messagesRepository.insertNewMessageReturnsMessage(newMessage);

        String token = jwtService.generateAccessToken(
                user.id(),
                user.username()
        );

        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        String url = "ws://localhost:" + port + "/ws";

        StompSession session = stompClient.connectAsync(
                url,
                new WebSocketHttpHeaders(),
                connectHeaders,
                new StompSessionHandlerAdapter() {},
                new Object[0]
        ).get(5, TimeUnit.SECONDS);

        CompletableFuture<MessageDeletedEvent> received =
                new CompletableFuture<>();

        StompHeaders subscribeHeaders = new StompHeaders();

        subscribeHeaders.setDestination(
                "/topic/chats/" + chatId + "/events"
        );

        subscribeHeaders.add(
                HttpHeaders.AUTHORIZATION,
                "Bearer " + token
        );

        session.subscribe(
                subscribeHeaders,
                new StompFrameHandler() {

                    @Override
                    public @NotNull Type getPayloadType(@NotNull StompHeaders headers) {
                        return MessageDeletedEvent.class;
                    }

                    @Override
                    public void handleFrame(
                            @NotNull StompHeaders headers,
                            Object payload) {

                        received.complete(
                                (MessageDeletedEvent) payload
                        );
                    }
                }
        );

        StompHeaders sendHeaders = new StompHeaders();

        sendHeaders.setDestination(
                "/app/chats/" + chatId + "/messages/" + oldMessage.id() + "/delete"
        );

        sendHeaders.add(
                HttpHeaders.AUTHORIZATION,
                "Bearer " + token
        );

        session.send(
                sendHeaders,
                new byte[0]
        );

        MessageDeletedEvent event =
                received.get(5, TimeUnit.SECONDS);

        assertEquals(
                ChatEventType.MESSAGE_DELETED,
                event.type()
        );

        assertEquals(chatId, event.chatId());
        assertEquals(oldMessage.id(), event.messageId());

        assertThrows(
                NoSuchMessageException.class,
                () -> messagesRepository.getUserIDByMessageID(oldMessage.id())
        );

    }

}