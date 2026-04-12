package fr.fumbus.blackflash.discord.slash.handlers;

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

import static fr.fumbus.blackflash.discord.slash.utils.SlashCommandConstants.*;
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
            event.reply(MESSAGE_BOT_ALREADY_IN_VOICE_CHANNEL).setEphemeral(true).queue();
            return;
        }
        joinChannel(event);
    }

    /**
     * Connects the bot to the invoking member's voice channel and initialises the music manager.
     * Called directly by {@link SlashPlayCommandHandler} when the bot is not yet in a channel.
     */
    public void joinChannel(SlashCommandInteractionEvent event) {
        final Member member = event.getMember();
        if (nonNull(member)) {
            final GuildVoiceState memberVoiceState = member.getVoiceState();
            if (nonNull(memberVoiceState) && memberVoiceState.inAudioChannel() && nonNull(memberVoiceState.getChannel())) {
                event.getJDA().getDirectAudioController().connect(memberVoiceState.getChannel());
            }
            registry.getOrCreate(member.getGuild().getIdLong());
        }
        event.reply("Joining your channel!").queue();
    }
}

