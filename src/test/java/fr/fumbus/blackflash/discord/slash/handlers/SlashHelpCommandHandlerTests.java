package fr.fumbus.blackflash.discord.slash.handlers;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static fr.fumbus.blackflash.discord.slash.utils.SlashCommandConstants.COMMAND_HELP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlashHelpCommandHandlerTests {

    @InjectMocks
    SlashHelpCommandHandler handler;

    @Test
    void commandData_isGuildScopedWithCorrectName() {
        SlashCommandData data = (SlashCommandData) handler.commandData();
        assertThat(data.getName()).isEqualTo(COMMAND_HELP);
        assertThat(data.getContexts()).contains(InteractionContextType.GUILD);
        assertThat(data.getOptions()).isEmpty();
    }

    @Test
    void requiresBotInVoiceChannel_returnsFalse() {
        assertThat(handler.requiresBotInVoiceChannel()).isFalse();
    }

    @Test
    void handle_repliesWithEmbedEphemeral() {
        SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class, Answers.RETURNS_DEEP_STUBS);
        Guild guild = mock(Guild.class);

        handler.handle(event, guild);

        ArgumentCaptor<MessageEmbed> embedCaptor = ArgumentCaptor.forClass(MessageEmbed.class);
        verify(event).replyEmbeds(embedCaptor.capture());
        assertThat(embedCaptor.getValue().getTitle()).isEqualTo("🎵 Blackflash");
        assertThat(embedCaptor.getValue().getUrl()).isEqualTo("https://github.com/poulpy2k/blackflash");
        verify(event.replyEmbeds(embedCaptor.getValue())).setEphemeral(true);
    }
}



