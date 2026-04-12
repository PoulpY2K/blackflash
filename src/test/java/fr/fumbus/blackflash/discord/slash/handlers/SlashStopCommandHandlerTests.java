package fr.fumbus.blackflash.discord.slash.handlers;

import fr.fumbus.blackflash.music.manager.GuildMusicManager;
import fr.fumbus.blackflash.music.manager.GuildMusicManagerRegistry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static fr.fumbus.blackflash.discord.slash.utils.SlashCommandConstants.COMMAND_STOP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlashStopCommandHandlerTests {

    @Mock
    GuildMusicManagerRegistry registry;

    @InjectMocks
    SlashStopCommandHandler handler;

    @Test
    void commandData_isGuildScopedWithCorrectName() {
        SlashCommandData data = (SlashCommandData) handler.commandData();
        assertThat(data.getName()).isEqualTo(COMMAND_STOP);
        assertThat(data.getContexts()).contains(InteractionContextType.GUILD);
        assertThat(data.getOptions()).isEmpty();
    }

    @Test
    void handle_stopsManagerAndReplies() {
        long guildId = 42L;
        GuildMusicManager manager = mock(GuildMusicManager.class);
        when(registry.getOrCreate(guildId)).thenReturn(manager);

        SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class, Answers.RETURNS_DEEP_STUBS);
        Guild guild = mock(Guild.class, Answers.RETURNS_DEEP_STUBS);
        when(guild.getIdLong()).thenReturn(guildId);

        handler.handle(event, guild);

        verify(manager).stop();
        verify(event).reply("Stopped the current track!");
    }
}

