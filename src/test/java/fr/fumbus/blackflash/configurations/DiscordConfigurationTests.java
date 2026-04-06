package fr.fumbus.blackflash.configurations;

import dev.arbjerg.lavalink.client.LavalinkClient;
import fr.fumbus.blackflash.discord.jda.slash.SlashCommandListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscordConfigurationTests {

    @Mock
    LavalinkClient lavalink;

    @Mock(answer = Answers.RETURNS_SELF)
    JDABuilder jdaBuilder;

    @Mock
    JDA jda;

    @Mock
    SlashCommandListener listener;

    @Spy
    @InjectMocks
    private DiscordConfiguration discordConfiguration;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(discordConfiguration, "token", "test-token");
        ReflectionTestUtils.setField(discordConfiguration, "activity", "test activity");
    }

    @Test
    void initializeJDA_doesNotThrowWhenTokenIsInvalid() {
        try (MockedStatic<JDABuilder> mockedJDABuilder = mockStatic(JDABuilder.class)) {
            mockedJDABuilder.when(() -> JDABuilder.createDefault(anyString())).thenReturn(jdaBuilder);
            when(jdaBuilder.build()).thenThrow(new InvalidTokenException());

            assertDoesNotThrow(() -> discordConfiguration.initializeJDA());
        }
    }

    @Test
    void initializeJDA_rethrowsUnexpectedExceptions() {
        try (MockedStatic<JDABuilder> mockedJDABuilder = mockStatic(JDABuilder.class)) {
            mockedJDABuilder.when(() -> JDABuilder.createDefault(anyString())).thenReturn(jdaBuilder);
            when(jdaBuilder.build()).thenThrow(new RuntimeException("unexpected error"));

            assertThrows(RuntimeException.class, () -> discordConfiguration.initializeJDA());
        }
    }

    @Test
    void initializeJDA_buildsJdaAndAwaitsReadiness() throws Exception {
        try (MockedStatic<JDABuilder> mockedJDABuilder = mockStatic(JDABuilder.class)) {
            mockedJDABuilder.when(() -> JDABuilder.createDefault(anyString())).thenReturn(jdaBuilder);
            when(jdaBuilder.build()).thenReturn(jda);
            when(jda.awaitReady()).thenReturn(jda);

            assertDoesNotThrow(() -> discordConfiguration.initializeJDA());

            verify(jda).awaitReady();
        }
    }
}
