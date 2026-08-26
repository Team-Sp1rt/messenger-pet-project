package messenger.backend.messaging;


import messenger.backend.services.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
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
import messenger.backend.dtos.User;
import messenger.backend.repositories.AuthorisationRepository;
import messenger.backend.repositories.ChatMembersRepository;
import messenger.backend.repositories.ChatsRepository;
import messenger.backend.repositories.UserRepository;

import java.time.LocalDate;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    private ChatMembersRepository chatMembersRepository;

    private WebSocketStompClient stompClient;

    @BeforeEach
    void setUp() {
        stompClient = new WebSocketStompClient(
                new StandardWebSocketClient()
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

        CompletableFuture<StompSession> connection = stompClient.connectAsync(
                url,
                new WebSocketHttpHeaders(),
                connectHeaders,
                new StompSessionHandlerAdapter() {});


        assertThrows(ExecutionException.class, () -> connection.get(5, TimeUnit.SECONDS));

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

        CompletableFuture<StompSession> connection = stompClient.connectAsync(
                url,
                new WebSocketHttpHeaders(),
                connectHeaders,
                new StompSessionHandlerAdapter() {});


        assertThrows(ExecutionException.class, () -> connection.get(5, TimeUnit.SECONDS));

    }

    @Test
    void subscribeToChatAsMember_isAccepted() throws Exception {
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


    }
}