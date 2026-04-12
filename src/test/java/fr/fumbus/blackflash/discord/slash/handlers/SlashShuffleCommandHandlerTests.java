package fr.fumbus.blackflash.discord.slash.handlers;

import dev.arbjerg.lavalink.client.player.Track;
import fr.fumbus.blackflash.music.manager.GuildMusicManager;
import fr.fumbus.blackflash.music.manager.GuildMusicManagerRegistry;
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

import java.util.List;
import java.util.Optional;

import static fr.fumbus.blackflash.discord.slash.utils.SlashCommandConstants.COMMAND_SHUFFLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlashShuffleCommandHandlerTests {

    @Mock
    GuildMusicManagerRegistry registry;

    @InjectMocks
    SlashShuffleCommandHandler handler;

    @Test
    void commandData_isGuildScopedWithCorrectName() {
        SlashCommandData data = (SlashCommandData) handler.commandData();
        assertThat(data.getName()).isEqualTo(COMMAND_SHUFFLE);
        assertThat(data.getContexts()).contains(InteractionContextType.GUILD);
        assertThat(data.getOptions()).isEmpty();
    }

    @Test
    void handle_repliesEphemeralWhenQueueHasOneOrZeroTracks() {
        long guildId = 42L;
        GuildMusicManager manager = mock(GuildMusicManager.class, Answers.RETURNS_DEEP_STUBS);
        TrackScheduler scheduler = new TrackScheduler(manager);
        when(manager.getTrackScheduler()).thenReturn(scheduler);
        when(registry.getOrCreate(guildId)).thenReturn(manager);

        SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class, Answers.RETURNS_DEEP_STUBS);
        Guild guild = mock(Guild.class, Answers.RETURNS_DEEP_STUBS);
        when(guild.getIdLong()).thenReturn(guildId);

        handler.handle(event, guild);

        verify(event).reply("There needs to be at least 2 tracks in the queue to shuffle!");
        verify(event.reply("There needs to be at least 2 tracks in the queue to shuffle!")).setEphemeral(true);
    }

    @Test
    void handle_shufflesQueueAndReplies() {
        long guildId = 42L;
        GuildMusicManager manager = mock(GuildMusicManager.class, Answers.RETURNS_DEEP_STUBS);
        TrackScheduler scheduler = new TrackScheduler(manager);
        scheduler.queue.addAll(List.of(mock(Track.class), mock(Track.class)));
        when(manager.getTrackScheduler()).thenReturn(scheduler);
        when(manager.getPlayer()).thenReturn(Optional.empty());
        when(registry.getOrCreate(guildId)).thenReturn(manager);

        SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class, Answers.RETURNS_DEEP_STUBS);
        Guild guild = mock(Guild.class, Answers.RETURNS_DEEP_STUBS);
        when(guild.getIdLong()).thenReturn(guildId);

        handler.handle(event, guild);

        assertThat(scheduler.queue).hasSize(2);
        verify(event).reply("🔀 Queue shuffled!");
    }
}
