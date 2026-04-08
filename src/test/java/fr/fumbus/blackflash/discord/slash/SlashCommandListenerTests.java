package fr.fumbus.blackflash.discord.slash;

import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.event.TrackEndEvent;
import dev.arbjerg.lavalink.client.event.TrackStartEvent;
import fr.fumbus.blackflash.lavalink.GuildMusicManager;
import fr.fumbus.blackflash.lavalink.LoopMode;
import fr.fumbus.blackflash.lavalink.TrackScheduler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.function.Consumer;

import static fr.fumbus.blackflash.discord.slash.SlashCommandConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlashCommandListenerTests {

    // RETURNS_DEEP_STUBS lets the reactive chain .on(...).subscribe(...) work without NPE
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    LavalinkClient lavalinkClient;

    @Mock
    SlashCommandRegistry slashCommandRegistry;

    @InjectMocks
    SlashCommandListener listener;

    // ─── initial state ────────────────────────────────────────────────────────

    @Test
    void listener_hasExpectedInitialState() {
        assertThat(listener).isInstanceOf(ListenerAdapter.class);
        assertThat(listener.getMusicManagers()).isEmpty();
    }

    // ─── @PostConstruct init() ────────────────────────────────────────────────

    @Test
    void init_doesNotThrow() {
        assertDoesNotThrow(() -> listener.init());
    }

    @Test
    void init_trackStartEventHandler_delegatesToManagerOrDoesNothingWhenAbsent() {
        listener.init();

        ArgumentCaptor<Consumer<TrackStartEvent>> captor = ArgumentCaptor.captor();
        verify(lavalinkClient.on(TrackStartEvent.class)).subscribe(captor.capture());

        // absent manager — must not throw
        assertDoesNotThrow(() -> captor.getValue().accept(mock(TrackStartEvent.class, Answers.RETURNS_DEEP_STUBS)));

        // present manager — must delegate
        long guildId = 42L;
        TrackStartEvent event = mock(TrackStartEvent.class, Answers.RETURNS_DEEP_STUBS);
        when(event.getGuildId()).thenReturn(guildId);
        try (MockedConstruction<TrackScheduler> ctor = mockConstruction(TrackScheduler.class)) {
            listener.getMusicManagers().put(guildId, new GuildMusicManager(guildId, lavalinkClient));
            captor.getValue().accept(event);
            verify(ctor.constructed().getFirst()).onTrackStart(event);
        }
    }

    @Test
    void init_trackEndEventHandler_delegatesToManagerOrDoesNothingWhenAbsent() {
        listener.init();

        ArgumentCaptor<Consumer<TrackEndEvent>> captor = ArgumentCaptor.captor();
        verify(lavalinkClient.on(TrackEndEvent.class)).subscribe(captor.capture());

        // absent manager — must not throw
        assertDoesNotThrow(() -> captor.getValue().accept(mock(TrackEndEvent.class, Answers.RETURNS_DEEP_STUBS)));

        // present manager — must delegate
        long guildId = 42L;
        TrackEndEvent event = mock(TrackEndEvent.class, Answers.RETURNS_DEEP_STUBS);
        when(event.getGuildId()).thenReturn(guildId);
        try (MockedConstruction<TrackScheduler> ctor = mockConstruction(TrackScheduler.class)) {
            listener.getMusicManagers().put(guildId, new GuildMusicManager(guildId, lavalinkClient));
            captor.getValue().accept(event);
            verify(ctor.constructed().getFirst()).onTrackEnd(event);
        }
    }

    // ─── onReady ──────────────────────────────────────────────────────────────

    @Test
    void onReady_registersCommandsFromRegistryViaJda() {
        ReadyEvent readyEvent = mock(ReadyEvent.class, Answers.RETURNS_DEEP_STUBS);
        List<CommandData> commands = List.of(mock(CommandData.class));
        when(slashCommandRegistry.getCommands()).thenReturn(commands);

        listener.onReady(readyEvent);

        verify(readyEvent.getJDA().updateCommands()).addCommands(commands);
    }

    // ─── onSlashCommandInteraction ────────────────────────────────────────────

    @Test
    void onSlashCommandInteraction_throwsWhenCalledOutsideGuildContext() {
        SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class, Answers.RETURNS_DEEP_STUBS);
        when(event.getGuild()).thenReturn(null);

        assertThrows(IllegalStateException.class, () -> listener.onSlashCommandInteraction(event));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @SuppressWarnings("DataFlowIssue")
    void onSlashCommandInteraction_joinCommand_alwaysRepliesAndConnectsOnlyWhenInVoiceChannel(boolean inAudioChannel) {
        SlashCommandInteractionEvent event = mockGuildSlashEvent(COMMAND_JOIN);
        when(event.getMember().getVoiceState().inAudioChannel()).thenReturn(inAudioChannel);

        listener.onSlashCommandInteraction(event);

        if (inAudioChannel) {
            verify(event.getJDA().getDirectAudioController()).connect(event.getMember().getVoiceState().getChannel());
        } else {
            verify(event.getJDA().getDirectAudioController(), never()).connect(any());
        }
        verify(event).reply("Joining your channel!");
    }

    @Test
    @SuppressWarnings("DataFlowIssue")
    void onSlashCommandInteraction_stopCommand_stopsPlaybackAndReplies() {
        long guildId = 42L;
        GuildMusicManager manager = mock(GuildMusicManager.class);
        SlashCommandInteractionEvent event = mockGuildSlashEvent(COMMAND_STOP);
        when(event.getGuild().getIdLong()).thenReturn(guildId);
        listener.getMusicManagers().put(guildId, manager);

        listener.onSlashCommandInteraction(event);

        verify(manager).stop();
        verify(event).reply("Stopped the current track!");
    }

    @Test
    @SuppressWarnings("DataFlowIssue")
    void onSlashCommandInteraction_leaveCommand_disconnectsRemovesMusicManagerAndReplies() {
        long guildId = 42L;
        SlashCommandInteractionEvent event = mockGuildSlashEvent(COMMAND_LEAVE);
        when(event.getGuild().getIdLong()).thenReturn(guildId);
        listener.getMusicManagers().put(guildId, mock(GuildMusicManager.class));

        listener.onSlashCommandInteraction(event);

        assertThat(listener.getMusicManagers()).doesNotContainKey(guildId);
        verify(event.getJDA().getDirectAudioController()).disconnect(event.getGuild());
        verify(event).reply("Leaving the channel!");
    }

    @Test
    @SuppressWarnings("DataFlowIssue")
    void onSlashCommandInteraction_playCommand_defersReplyWhenBotIsAlreadyInVoiceChannel() {
        SlashCommandInteractionEvent event = mockGuildSlashEvent(COMMAND_PLAY);
        when(event.getGuild().getSelfMember().getVoiceState().inAudioChannel()).thenReturn(true);
        listener.getMusicManagers().put(event.getGuild().getIdLong(), mock(GuildMusicManager.class));

        listener.onSlashCommandInteraction(event);

        verify(event).deferReply(false);
    }

    @Test
    @SuppressWarnings("DataFlowIssue")
    void onSlashCommandInteraction_playCommand_joinsChannelWhenBotIsNotInVoiceChannel() {
        SlashCommandInteractionEvent event = mockGuildSlashEvent(COMMAND_PLAY);
        when(event.getGuild().getSelfMember().getVoiceState().inAudioChannel()).thenReturn(false);

        listener.onSlashCommandInteraction(event);

        verify(event).reply("Joining your channel!");
    }

    @Test
    @SuppressWarnings("DataFlowIssue")
    void onSlashCommandInteraction_playCommand_loadsQueryViaLavalink() {
        SlashCommandInteractionEvent event = mockGuildSlashEvent(COMMAND_PLAY);
        when(event.getGuild().getSelfMember().getVoiceState().inAudioChannel()).thenReturn(true);
        when(event.getOption("query").getAsString()).thenReturn("ytsearch:hello");
        listener.getMusicManagers().put(event.getGuild().getIdLong(), mock(GuildMusicManager.class));

        listener.onSlashCommandInteraction(event);

        verify(lavalinkClient).getOrCreateLink(event.getGuild().getIdLong());
        verify(lavalinkClient.getOrCreateLink(event.getGuild().getIdLong())).loadItem("ytsearch:hello");
    }

    @Test
    @SuppressWarnings("DataFlowIssue")
    void onSlashCommandInteraction_loopCommand_cyclesLoopModeFromDisabledToTrackAndReplies() {
        long guildId = 42L;
        TrackScheduler scheduler = mock(TrackScheduler.class);
        when(scheduler.getLoopMode()).thenReturn(LoopMode.DISABLED);
        GuildMusicManager manager = mock(GuildMusicManager.class);
        when(manager.getTrackScheduler()).thenReturn(scheduler);

        SlashCommandInteractionEvent event = mockGuildSlashEvent(COMMAND_LOOP);
        when(event.getGuild().getIdLong()).thenReturn(guildId);
        listener.getMusicManagers().put(guildId, manager);

        listener.onSlashCommandInteraction(event);

        verify(scheduler).setLoopMode(LoopMode.TRACK);
        verify(event).reply("🔂 Loop track enabled!");
    }

    @Test
    @SuppressWarnings("DataFlowIssue")
    void onSlashCommandInteraction_loopCommand_cyclesLoopModeFromTrackToQueueAndReplies() {
        long guildId = 42L;
        TrackScheduler scheduler = mock(TrackScheduler.class);
        when(scheduler.getLoopMode()).thenReturn(LoopMode.TRACK);
        GuildMusicManager manager = mock(GuildMusicManager.class);
        when(manager.getTrackScheduler()).thenReturn(scheduler);

        SlashCommandInteractionEvent event = mockGuildSlashEvent(COMMAND_LOOP);
        when(event.getGuild().getIdLong()).thenReturn(guildId);
        listener.getMusicManagers().put(guildId, manager);

        listener.onSlashCommandInteraction(event);

        verify(scheduler).setLoopMode(LoopMode.QUEUE);
        verify(event).reply("🔁 Loop queue enabled!");
    }

    @Test
    @SuppressWarnings("DataFlowIssue")
    void onSlashCommandInteraction_loopCommand_cyclesLoopModeFromQueueToDisabledAndReplies() {
        long guildId = 42L;
        TrackScheduler scheduler = mock(TrackScheduler.class);
        when(scheduler.getLoopMode()).thenReturn(LoopMode.QUEUE);
        GuildMusicManager manager = mock(GuildMusicManager.class);
        when(manager.getTrackScheduler()).thenReturn(scheduler);

        SlashCommandInteractionEvent event = mockGuildSlashEvent(COMMAND_LOOP);
        when(event.getGuild().getIdLong()).thenReturn(guildId);
        listener.getMusicManagers().put(guildId, manager);

        listener.onSlashCommandInteraction(event);

        verify(scheduler).setLoopMode(LoopMode.DISABLED);
        verify(event).reply("Loop disabled!");
    }

    @Test
    void onSlashCommandInteraction_unknownCommand_repliesWithEphemeralUnknownMessage() {
        SlashCommandInteractionEvent event = mockGuildSlashEvent("nonexistent");

        listener.onSlashCommandInteraction(event);

        verify(event).reply("Unknown command!");
        verify(event.reply("Unknown command!")).setEphemeral(true);
    }

    // ─── helpers ──────────────────────────────────────────────────────────────

    /**
     * Creates a deep-stub slash event already scoped to a guild with the given command name.
     */
    private SlashCommandInteractionEvent mockGuildSlashEvent(String commandName) {
        SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class, Answers.RETURNS_DEEP_STUBS);
        when(event.getGuild()).thenReturn(mock(Guild.class, Answers.RETURNS_DEEP_STUBS));
        when(event.getFullCommandName()).thenReturn(commandName);
        return event;
    }
}
