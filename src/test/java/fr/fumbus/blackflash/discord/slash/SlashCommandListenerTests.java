package fr.fumbus.blackflash.discord.slash;

import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.event.TrackEndEvent;
import dev.arbjerg.lavalink.client.event.TrackStartEvent;
import fr.fumbus.blackflash.music.manager.GuildMusicManager;
import fr.fumbus.blackflash.music.manager.GuildMusicManagerRegistry;
import fr.fumbus.blackflash.music.player.TrackScheduler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static fr.fumbus.blackflash.discord.slash.utils.SlashCommandConstants.MESSAGE_BOT_NOT_IN_VOICE_CHANNEL;
import static fr.fumbus.blackflash.discord.slash.utils.SlashCommandConstants.MESSAGE_MEMBER_NOT_IN_VOICE_CHANNEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SuppressWarnings("DataFlowIssue")
@ExtendWith(MockitoExtension.class)
class SlashCommandListenerTests {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    LavalinkClient lavalinkClient;


    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    GuildMusicManagerRegistry musicManagerRegistry;

    // Created manually so we can control the handler list per test
    SlashCommandListener listener;

    // ─── initial state ────────────────────────────────────────────────────────

    /**
     * Stub a handler with the given command name and requiresBotInVoiceChannel value.
     */
    private static SlashCommandHandler handlerMock(boolean requiresBot) {
        SlashCommandHandler handler = mock(SlashCommandHandler.class);
        CommandData commandData = mock(CommandData.class);
        when(commandData.getName()).thenReturn("mycommand");
        when(handler.commandData()).thenReturn(commandData);
        when(handler.requiresBotInVoiceChannel()).thenReturn(requiresBot);
        return handler;
    }

    // ─── @PostConstruct init() ────────────────────────────────────────────────

    /**
     * Creates a deep-stub slash event scoped to a guild with the given command name.
     * The member is stubbed as being in a voice channel so the voice-channel guard
     * in {@code onSlashCommandInteraction} does not short-circuit unrelated tests.
     */
    private static SlashCommandInteractionEvent mockGuildSlashEvent(String commandName) {
        SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class, Answers.RETURNS_DEEP_STUBS);
        when(event.getGuild()).thenReturn(mock(Guild.class, Answers.RETURNS_DEEP_STUBS));
        when(event.getFullCommandName()).thenReturn(commandName);
        when(event.getMember().getVoiceState().inAudioChannel()).thenReturn(true);
        return event;
    }

    @Test
    void listener_isInstanceOfListenerAdapter() {
        listener = listenerWithNoHandlers();
        assertThat(listener).isInstanceOf(ListenerAdapter.class);
    }

    @Test
    void init_doesNotThrow() {
        listener = listenerWithNoHandlers();
        assertDoesNotThrow(() -> listener.init());
    }

    // ─── onReady ──────────────────────────────────────────────────────────────

    @Test
    void init_trackStartEventHandler_delegatesToManagerOrDoesNothingWhenAbsent() {
        when(musicManagerRegistry.getIfPresent(anyLong())).thenReturn(Optional.empty());
        listener = listenerWithNoHandlers();
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
            GuildMusicManager manager = new GuildMusicManager(guildId, lavalinkClient);
            when(musicManagerRegistry.getIfPresent(guildId)).thenReturn(Optional.of(manager));
            captor.getValue().accept(event);
            verify(ctor.constructed().getFirst()).onTrackStart(event);
        }
    }

    // ─── onSlashCommandInteraction — guards ───────────────────────────────────

    @Test
    void init_trackEndEventHandler_delegatesToManagerOrDoesNothingWhenAbsent() {
        when(musicManagerRegistry.getIfPresent(anyLong())).thenReturn(Optional.empty());
        listener = listenerWithNoHandlers();
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
            GuildMusicManager manager = new GuildMusicManager(guildId, lavalinkClient);
            when(musicManagerRegistry.getIfPresent(guildId)).thenReturn(Optional.of(manager));
            captor.getValue().accept(event);
            verify(ctor.constructed().getFirst()).onTrackEnd(event);
        }
    }

    @Test
    void onReady_registersCommandDataFromHandlers() {
        CommandData commandData = mock(CommandData.class);
        when(commandData.getName()).thenReturn("testcmd");
        SlashCommandHandler handler = mock(SlashCommandHandler.class);
        when(handler.commandData()).thenReturn(commandData);
        listener = listenerWithHandlers(handler);
        listener.init();
        ReadyEvent readyEvent = mock(ReadyEvent.class, Answers.RETURNS_DEEP_STUBS);

        listener.onReady(readyEvent);

        verify(readyEvent.getJDA().updateCommands()).addCommands(List.of(commandData));
    }

    @Test
    void onSlashCommandInteraction_throwsWhenGuildContextIsNull() {
        listener = listenerWithNoHandlers();
        SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class, Answers.RETURNS_DEEP_STUBS);
        when(event.getGuild()).thenReturn(null);

        assertThrows(IllegalStateException.class, () -> listener.onSlashCommandInteraction(event));
    }

    // ─── onSlashCommandInteraction — dispatch ─────────────────────────────────

    @Test
    void onSlashCommandInteraction_repliesEphemeralWhenMemberNotInVoiceChannel() {
        listener = listenerWithNoHandlers();
        SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class, Answers.RETURNS_DEEP_STUBS);
        when(event.getMember().getVoiceState()).thenReturn(null);

        listener.onSlashCommandInteraction(event);

        verify(event).reply(MESSAGE_MEMBER_NOT_IN_VOICE_CHANNEL);
        verify(event.reply(MESSAGE_MEMBER_NOT_IN_VOICE_CHANNEL)).setEphemeral(true);
        verify(event.getJDA().getDirectAudioController(), never()).connect(any());
    }

    @Test
    void onSlashCommandInteraction_unknownCommand_repliesWithEphemeralUnknownMessage() {
        listener = listenerWithNoHandlers();
        listener.init();
        SlashCommandInteractionEvent event = mockGuildSlashEvent("nonexistent");

        listener.onSlashCommandInteraction(event);

        verify(event).reply("Unknown command!");
        verify(event.reply("Unknown command!")).setEphemeral(true);
    }

    @Test
    void onSlashCommandInteraction_dispatchesToRegisteredHandler() {
        SlashCommandHandler handler = handlerMock(false);
        listener = listenerWithHandlers(handler);
        listener.init();

        listener.onSlashCommandInteraction(mockGuildSlashEvent("mycommand"));

        verify(handler).handle(any(), any(Guild.class));
    }

    // ─── helpers ──────────────────────────────────────────────────────────────

    @Test
    void onSlashCommandInteraction_repliesEphemeralWhenHandlerRequiresBotInVoiceChannel() {
        SlashCommandHandler handler = handlerMock(true);
        listener = listenerWithHandlers(handler);
        listener.init();
        // bot is NOT in voice channel (default for Guild deep stub)
        SlashCommandInteractionEvent event = mockGuildSlashEvent("mycommand");

        listener.onSlashCommandInteraction(event);

        verify(event).reply(MESSAGE_BOT_NOT_IN_VOICE_CHANNEL);
        verify(event.reply(MESSAGE_BOT_NOT_IN_VOICE_CHANNEL)).setEphemeral(true);
        verify(handler, never()).handle(any(), any());
    }

    @Test
    void onSlashCommandInteraction_skipsVoiceGuardForHandlerThatOptsOut() {
        SlashCommandHandler handler = handlerMock(false);
        listener = listenerWithHandlers(handler);
        listener.init();
        // bot is NOT in voice channel — guard must be bypassed
        SlashCommandInteractionEvent event = mockGuildSlashEvent("mycommand");

        listener.onSlashCommandInteraction(event);

        verify(handler).handle(any(), any(Guild.class));
        verify(event, never()).reply(MESSAGE_BOT_NOT_IN_VOICE_CHANNEL);
    }

    @Test
    void constructor_throwsWhenDuplicateCommandNamesExist() {
        CommandData cd1 = mock(CommandData.class);
        when(cd1.getName()).thenReturn("duplicate");
        SlashCommandHandler h1 = mock(SlashCommandHandler.class);
        when(h1.commandData()).thenReturn(cd1);

        CommandData cd2 = mock(CommandData.class);
        when(cd2.getName()).thenReturn("duplicate");
        SlashCommandHandler h2 = mock(SlashCommandHandler.class);
        when(h2.commandData()).thenReturn(cd2);

        assertThrows(IllegalStateException.class, () -> listenerWithHandlers(h1, h2));
    }

    private SlashCommandListener listenerWithNoHandlers() {
        return new SlashCommandListener(lavalinkClient, musicManagerRegistry, List.of());
    }

    private SlashCommandListener listenerWithHandlers(SlashCommandHandler... handlers) {
        return new SlashCommandListener(lavalinkClient, musicManagerRegistry, List.of(handlers));
    }
}
