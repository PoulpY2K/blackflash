package fr.fumbus.blackflash.discord.slash.handlers;

import fr.fumbus.blackflash.music.manager.GuildMusicManager;
import fr.fumbus.blackflash.music.manager.GuildMusicManagerRegistry;
import fr.fumbus.blackflash.music.player.LoopMode;
import fr.fumbus.blackflash.music.player.TrackScheduler;
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

import static fr.fumbus.blackflash.discord.slash.utils.SlashCommandConstants.COMMAND_LOOP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlashLoopCommandHandlerTests {

    @Mock
    GuildMusicManagerRegistry registry;

    @InjectMocks
    SlashLoopCommandHandler handler;

    @Test
    void commandData_isGuildScopedWithCorrectName() {
        SlashCommandData data = (SlashCommandData) handler.commandData();
        assertThat(data.getName()).isEqualTo(COMMAND_LOOP);
        assertThat(data.getContexts()).contains(InteractionContextType.GUILD);
        assertThat(data.getOptions()).isEmpty();
    }

    @Test
    void handle_cyclesLoopModeFromDisabledToTrackAndReplies() {
        assertLoopCycle(LoopMode.DISABLED, LoopMode.TRACK, "🔂 Loop track enabled!");
    }

    @Test
    void handle_cyclesLoopModeFromTrackToQueueAndReplies() {
        assertLoopCycle(LoopMode.TRACK, LoopMode.QUEUE, "🔁 Loop queue enabled!");
    }

    @Test
    void handle_cyclesLoopModeFromQueueToDisabledAndReplies() {
        assertLoopCycle(LoopMode.QUEUE, LoopMode.DISABLED, "Loop disabled!");
    }

    private void assertLoopCycle(LoopMode currentMode, LoopMode expectedMode, String expectedReply) {
        long guildId = 42L;
        TrackScheduler scheduler = mock(TrackScheduler.class);
        when(scheduler.getLoopMode()).thenReturn(currentMode);
        GuildMusicManager manager = mock(GuildMusicManager.class);
        when(manager.getTrackScheduler()).thenReturn(scheduler);
        when(registry.getOrCreate(guildId)).thenReturn(manager);

        SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class, Answers.RETURNS_DEEP_STUBS);
        Guild guild = mock(Guild.class, Answers.RETURNS_DEEP_STUBS);
        when(guild.getIdLong()).thenReturn(guildId);

        handler.handle(event, guild);

        verify(scheduler).setLoopMode(expectedMode);
        verify(event).reply(expectedReply);
    }
}

