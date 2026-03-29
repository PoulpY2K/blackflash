package fr.fumbus.blackflash.configurations;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscordConfigurationTests {

    @Mock(answer = Answers.RETURNS_SELF)
    JDABuilder jdaBuilder;

    @Mock
    JDA jda;

    @Mock(answer = Answers.RETURNS_SELF)
    CommandListUpdateAction updateAction;

    @Spy
    @InjectMocks
    private DiscordConfiguration discordConfiguration;

    @Test
    void initializeJDA_doesNotThrowWhenTokenIsInvalid() {
        ReflectionTestUtils.setField(discordConfiguration, "token", "test-token");
        ReflectionTestUtils.setField(discordConfiguration, "activity", "test activity");

        try (MockedStatic<JDABuilder> mockedJDABuilder = mockStatic(JDABuilder.class)) {
            mockedJDABuilder.when(() -> JDABuilder.createDefault(anyString())).thenReturn(jdaBuilder);
            when(jdaBuilder.build()).thenThrow(mock(InvalidTokenException.class));

            assertDoesNotThrow(() -> discordConfiguration.initializeJDA());
        }
    }

    @Test
    void initializeJDA_rethrowsUnexpectedExceptions() {
        ReflectionTestUtils.setField(discordConfiguration, "token", "test-token");
        ReflectionTestUtils.setField(discordConfiguration, "activity", "test activity");

        try (MockedStatic<JDABuilder> mockedJDABuilder = mockStatic(JDABuilder.class)) {
            mockedJDABuilder.when(() -> JDABuilder.createDefault(anyString())).thenReturn(jdaBuilder);
            when(jdaBuilder.build()).thenThrow(new RuntimeException("unexpected error"));

            assertThrows(RuntimeException.class, () -> discordConfiguration.initializeJDA());
        }
    }

    @Test
    void initializeJDA_buildsJdaAndRegistersCommandsOnSuccess() {
        ReflectionTestUtils.setField(discordConfiguration, "token", "test-token");
        ReflectionTestUtils.setField(discordConfiguration, "activity", "test activity");

        try (MockedStatic<JDABuilder> mockedJDABuilder = mockStatic(JDABuilder.class)) {
            mockedJDABuilder.when(() -> JDABuilder.createDefault(anyString())).thenReturn(jdaBuilder);
            when(jdaBuilder.build()).thenReturn(jda);
            when(jda.updateCommands()).thenReturn(updateAction);

            assertDoesNotThrow(() -> discordConfiguration.initializeJDA());

            verify(jda).updateCommands();
            verify(updateAction).queue();
        }
    }
}
