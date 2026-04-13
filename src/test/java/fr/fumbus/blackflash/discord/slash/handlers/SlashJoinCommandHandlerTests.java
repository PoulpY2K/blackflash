package fr.fumbus.blackflash.discord.slash.handlers;

import fr.fumbus.blackflash.music.manager.GuildMusicManagerRegistry;
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

import static fr.fumbus.blackflash.discord.BotEmbeds.COLOR_ERROR;
import static fr.fumbus.blackflash.discord.slash.utils.SlashCommandConstants.COMMAND_JOIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SuppressWarnings("DataFlowIssue")
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

        ArgumentCaptor<MessageEmbed> embedCaptor = ArgumentCaptor.forClass(MessageEmbed.class);
        verify(event).replyEmbeds(embedCaptor.capture());
        verify(event.replyEmbeds(embedCaptor.getValue())).setEphemeral(true);
        verify(event.getJDA().getDirectAudioController(), never()).connect(any());
    }

    @Test
    void handle_connectsToMemberChannelAndRepliesWhenBotNotInVoiceChannel() {
        SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class, Answers.RETURNS_DEEP_STUBS);
        Guild guild = mock(Guild.class, Answers.RETURNS_DEEP_STUBS);
        when(event.getMember().getVoiceState().inAudioChannel()).thenReturn(true);

        handler.handle(event, guild);

        verify(event.getJDA().getDirectAudioController()).connect(event.getMember().getVoiceState().getChannel());
        verify(event).replyEmbeds(any(MessageEmbed.class));
    }

    @Test
    void joinChannel_returnsFalseAndRepliesEphemeralWhenMemberNotInVoiceChannel() {
        SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class, Answers.RETURNS_DEEP_STUBS);
        when(event.getMember().getVoiceState().inAudioChannel()).thenReturn(false);

        boolean result = handler.joinChannel(event);

        assertThat(result).isFalse();
        ArgumentCaptor<MessageEmbed> embedCaptor = ArgumentCaptor.forClass(MessageEmbed.class);
        verify(event).replyEmbeds(embedCaptor.capture());
        assertThat(embedCaptor.getValue().getColorRaw()).isEqualTo(COLOR_ERROR);
        verify(event.replyEmbeds(any(MessageEmbed.class))).setEphemeral(true);
        verify(registry, never()).getOrCreate(anyLong());
    }

    @Test
    void joinChannel_createsManagerForMembersGuild() {
        SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class, Answers.RETURNS_DEEP_STUBS);
        long guildId = 42L;
        when(event.getMember().getVoiceState().inAudioChannel()).thenReturn(true);
        when(event.getMember().getGuild().getIdLong()).thenReturn(guildId);

        handler.joinChannel(event);

        verify(registry).getOrCreate(guildId);
    }
}

