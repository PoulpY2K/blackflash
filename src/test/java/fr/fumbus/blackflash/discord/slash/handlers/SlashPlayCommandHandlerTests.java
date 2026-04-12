package fr.fumbus.blackflash.discord.slash.handlers;

import dev.arbjerg.lavalink.client.LavalinkClient;
import fr.fumbus.blackflash.music.manager.GuildMusicManager;
import fr.fumbus.blackflash.music.manager.GuildMusicManagerRegistry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static fr.fumbus.blackflash.discord.slash.utils.SlashCommandConstants.COMMAND_PLAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SuppressWarnings("DataFlowIssue")
@ExtendWith(MockitoExtension.class)
class SlashPlayCommandHandlerTests {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    LavalinkClient lavalink;

    @Mock
    GuildMusicManagerRegistry registry;

    @Mock
    SlashJoinCommandHandler slashJoinCommandHandler;

    @InjectMocks
    SlashPlayCommandHandler handler;

    @Test
    void commandData_isGuildScopedWithRequiredQueryStringOption() {
        SlashCommandData data = (SlashCommandData) handler.commandData();
        assertThat(data.getName()).isEqualTo(COMMAND_PLAY);
        assertThat(data.getContexts()).contains(InteractionContextType.GUILD);
        assertThat(data.getOptions()).hasSize(1);
        assertThat(data.getOptions().getFirst())
                .matches(opt -> "query".equals(opt.getName())
                        && opt.getType() == OptionType.STRING
                        && opt.isRequired());
    }

    @Test
    void requiresBotInVoiceChannel_returnsFalse() {
        assertThat(handler.requiresBotInVoiceChannel()).isFalse();
    }

    @Test
    void handle_defersReplyAndLoadsTrackWhenBotAlreadyInVoiceChannel() {
        long guildId = 42L;
        GuildMusicManager manager = mock(GuildMusicManager.class, Answers.RETURNS_DEEP_STUBS);
        when(registry.getOrCreate(guildId)).thenReturn(manager);

        SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class, Answers.RETURNS_DEEP_STUBS);
        Guild guild = mock(Guild.class, Answers.RETURNS_DEEP_STUBS);
        when(guild.getIdLong()).thenReturn(guildId);
        when(guild.getSelfMember().getVoiceState().inAudioChannel()).thenReturn(true);
        when(event.getOption("query").getAsString()).thenReturn("ytsearch:test");

        handler.handle(event, guild);

        verify(event).deferReply(false);
        verify(slashJoinCommandHandler, never()).joinChannel(event);
        verify(lavalink).getOrCreateLink(guildId);
        verify(lavalink.getOrCreateLink(guildId)).loadItem("ytsearch:test");
    }

    @Test
    void handle_joinsChannelAndLoadsTrackWhenBotNotInVoiceChannel() {
        long guildId = 42L;
        GuildMusicManager manager = mock(GuildMusicManager.class, Answers.RETURNS_DEEP_STUBS);
        when(registry.getOrCreate(guildId)).thenReturn(manager);

        SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class, Answers.RETURNS_DEEP_STUBS);
        Guild guild = mock(Guild.class, Answers.RETURNS_DEEP_STUBS);
        when(guild.getIdLong()).thenReturn(guildId);
        // bot not in voice (default for deep stub)
        when(event.getOption("query").getAsString()).thenReturn("ytsearch:test");

        handler.handle(event, guild);

        verify(slashJoinCommandHandler).joinChannel(event);
        verify(event, never()).deferReply(anyBoolean());
        verify(lavalink.getOrCreateLink(guildId)).loadItem("ytsearch:test");
    }
}

