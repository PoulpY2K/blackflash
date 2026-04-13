package fr.fumbus.blackflash.discord.slash.handlers;

import fr.fumbus.blackflash.discord.BotEmbeds;
import fr.fumbus.blackflash.discord.slash.SlashCommandHandler;
import fr.fumbus.blackflash.discord.slash.utils.SlashCommandUtils;
import fr.fumbus.blackflash.music.manager.GuildMusicManagerRegistry;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;

import static fr.fumbus.blackflash.discord.slash.utils.SlashCommandConstants.COMMAND_JOIN;
import static fr.fumbus.blackflash.discord.slash.utils.SlashCommandConstants.DESCRIPTION_JOIN;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */

@Component
@RequiredArgsConstructor
public class SlashJoinCommandHandler implements SlashCommandHandler {

    private static final CommandData COMMAND_DATA = Commands.slash(COMMAND_JOIN, DESCRIPTION_JOIN)
            .setContexts(InteractionContextType.GUILD);

    private final GuildMusicManagerRegistry registry;

    @Override
    public CommandData commandData() {
        return COMMAND_DATA;
    }

    /**
     * Join manages its own bot-presence check (bot must NOT already be in a channel).
     */
    @Override
    public boolean requiresBotInVoiceChannel() {
        return false;
    }

    @Override
    public void handle(SlashCommandInteractionEvent event, Guild guild) {
        if (SlashCommandUtils.isBotInVoiceChannel(guild)) {
            event.replyEmbeds(BotEmbeds.alreadyInVoiceChannel()).setEphemeral(true).queue();
            return;
        }
        joinChannel(event);
    }

    /**
     * Joins the member's current voice channel.
     *
     * @return {@code true} if the bot successfully joined; {@code false} if the member
     * is not in a voice channel (an ephemeral error embed is sent in that case).
     */
    public boolean joinChannel(SlashCommandInteractionEvent event) {
        return joinChannel(event, null);
    }

    /**
     * Joins the member's current voice channel and, once Discord has acknowledged the
     * reply, invokes {@code onSuccess} so callers can chain further work (e.g. loading
     * a track) without risking an {@code Unknown interaction} race.
     *
     * @param onSuccess optional callback to run inside the reply's {@code queue()}
     *                  success handler; {@code null} is accepted.
     * @return {@code true} if the bot successfully joined; {@code false} if the member
     * is not in a voice channel (an ephemeral error embed is sent in that case).
     */
    public boolean joinChannel(SlashCommandInteractionEvent event, Runnable onSuccess) {
        final Member member = event.getMember();
        if (isNull(member)) {
            event.replyEmbeds(BotEmbeds.memberNotInVoiceChannel()).setEphemeral(true).queue();
            return false;
        }

        final GuildVoiceState memberVoiceState = member.getVoiceState();
        if (isNull(memberVoiceState) || !memberVoiceState.inAudioChannel() || isNull(memberVoiceState.getChannel())) {
            event.replyEmbeds(BotEmbeds.memberNotInVoiceChannel()).setEphemeral(true).queue();
            return false;
        }

        final String channelName = memberVoiceState.getChannel().getName();
        event.getJDA().getDirectAudioController().connect(memberVoiceState.getChannel());
        registry.getOrCreate(member.getGuild().getIdLong());

        if (nonNull(onSuccess)) {
            event.replyEmbeds(BotEmbeds.joined(channelName)).queue(_ -> onSuccess.run());
        } else {
            event.replyEmbeds(BotEmbeds.joined(channelName)).queue();
        }

        return true;
    }
}
