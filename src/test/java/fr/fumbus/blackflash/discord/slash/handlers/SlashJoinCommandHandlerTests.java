package fr.fumbus.blackflash.discord.slash.handlers;

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

import static fr.fumbus.blackflash.discord.slash.utils.SlashCommandConstants.COMMAND_JOIN;
import static fr.fumbus.blackflash.discord.slash.utils.SlashCommandConstants.MESSAGE_BOT_ALREADY_IN_VOICE_CHANNEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlashJoinCommandHandlerTests {

    @Mock
    GuildMusicManagerRegistry registry;

    @InjectMocks
    SlashJoinCommandHandler handler;

    @Test
    void commandData_isGuildScopedWithCorrectName() {
        SlashCommandData data = (SlashCommandData) handler.commandData();
        assertThat(data.getName()).isEqualTo(COMMAND_JOIN);
        assertThat(data.getContexts()).contains(InteractionContextType.GUILD);
        assertThat(data.getOptions()).isEmpty();
    }

    @Test
    void requiresBotInVoiceChannel_returnsFalse() {
        assertThat(handler.requiresBotInVoiceChannel()).isFalse();
    }

    @Test
    void handle_repliesEphemeralWhenBotAlreadyInVoiceChannel() {
        SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class, Answers.RETURNS_DEEP_STUBS);
        Guild guild = mock(Guild.class, Answers.RETURNS_DEEP_STUBS);
        when(guild.getSelfMember().getVoiceState().inAudioChannel()).thenReturn(true);

        handler.handle(event, guild);

        verify(event).reply(MESSAGE_BOT_ALREADY_IN_VOICE_CHANNEL);
        verify(event.reply(MESSAGE_BOT_ALREADY_IN_VOICE_CHANNEL)).setEphemeral(true);
        verify(event.getJDA().getDirectAudioController(), never()).connect(any());
    }

    @Test
    void handle_connectsToMemberChannelAndRepliesWhenBotNotInVoiceChannel() {
        SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class, Answers.RETURNS_DEEP_STUBS);
        Guild guild = mock(Guild.class, Answers.RETURNS_DEEP_STUBS);
        // bot not in voice (default for deep stub — inAudioChannel() returns false)
        when(event.getMember().getVoiceState().inAudioChannel()).thenReturn(true);

        handler.handle(event, guild);

        verify(event.getJDA().getDirectAudioController()).connect(event.getMember().getVoiceState().getChannel());
        verify(event).reply("Joining your channel!");
    }

    @Test
    void joinChannel_createsManagerForMembersGuild() {
        SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class, Answers.RETURNS_DEEP_STUBS);
        long guildId = 42L;
        when(event.getMember().getGuild().getIdLong()).thenReturn(guildId);

        handler.joinChannel(event);

        verify(registry).getOrCreate(guildId);
    }
}

