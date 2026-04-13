package fr.fumbus.blackflash.discord.slash.handlers;

import fr.fumbus.blackflash.music.manager.GuildMusicManager;
import fr.fumbus.blackflash.music.manager.GuildMusicManagerRegistry;
import fr.fumbus.blackflash.music.player.TrackScheduler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static fr.fumbus.blackflash.discord.BotEmbeds.COLOR_WARNING;
import static fr.fumbus.blackflash.discord.slash.utils.SlashCommandConstants.COMMAND_SHUFFLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
    void handle_repliesNothingPlayingWhenNoManagerPresent() {
        long guildId = 42L;
        when(registry.getIfPresent(guildId)).thenReturn(Optional.empty());

        SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class, Answers.RETURNS_DEEP_STUBS);
        Guild guild = mock(Guild.class, Answers.RETURNS_DEEP_STUBS);
        when(guild.getIdLong()).thenReturn(guildId);

        handler.handle(event, guild);

        ArgumentCaptor<MessageEmbed> embedCaptor = ArgumentCaptor.forClass(MessageEmbed.class);
        verify(event).replyEmbeds(embedCaptor.capture());
        assertThat(embedCaptor.getValue().getColorRaw()).isEqualTo(COLOR_WARNING);
        verify(event.replyEmbeds(any(MessageEmbed.class))).setEphemeral(true);
    }

    @Test
    void handle_repliesEphemeralWhenQueueHasOneOrLessTracks() {
        long guildId = 42L;
        GuildMusicManager musicManager = mock(GuildMusicManager.class);
        TrackScheduler scheduler = spy(new TrackScheduler(musicManager));
        GuildMusicManager manager = mock(GuildMusicManager.class);
        when(manager.getTrackScheduler()).thenReturn(scheduler);
        when(registry.getIfPresent(guildId)).thenReturn(Optional.of(manager));

        SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class, Answers.RETURNS_DEEP_STUBS);
        Guild guild = mock(Guild.class, Answers.RETURNS_DEEP_STUBS);
        when(guild.getIdLong()).thenReturn(guildId);

        handler.handle(event, guild);

        ArgumentCaptor<MessageEmbed> embedCaptor = ArgumentCaptor.forClass(MessageEmbed.class);
        verify(event).replyEmbeds(embedCaptor.capture());
        verify(event.replyEmbeds(embedCaptor.getValue())).setEphemeral(true);
        verify(scheduler, never()).shuffle();
    }

    @Test
    void handle_shufflesAndRepliesWhenQueueHasMoreThanOneTrack() {
        long guildId = 42L;
        GuildMusicManager musicManager = mock(GuildMusicManager.class);
        TrackScheduler scheduler = spy(new TrackScheduler(musicManager));
        doNothing().when(scheduler).shuffle();
        doReturn(2).when(scheduler).getQueueSize(); // simulate 2 tracks in the queue
        GuildMusicManager manager = mock(GuildMusicManager.class);
        when(manager.getTrackScheduler()).thenReturn(scheduler);
        when(registry.getIfPresent(guildId)).thenReturn(Optional.of(manager));

        SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class, Answers.RETURNS_DEEP_STUBS);
        Guild guild = mock(Guild.class, Answers.RETURNS_DEEP_STUBS);
        when(guild.getIdLong()).thenReturn(guildId);

        handler.handle(event, guild);

        verify(scheduler).shuffle();
        verify(event).replyEmbeds(any(MessageEmbed.class));
    }
}
