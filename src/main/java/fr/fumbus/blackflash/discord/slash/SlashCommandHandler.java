package fr.fumbus.blackflash.discord.slash;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;


/**
 * Contract for a single slash-command handler.
 *
 * <p>Implementations are discovered by {@link SlashCommandListener} via Spring's
 * {@code List<SlashCommandHandler>} injection and dispatched by command name.
 *
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */
public interface SlashCommandHandler {

    /**
     * The Discord command definition: name, description, options, and context.
     */
    CommandData commandData();

    /**
     * Whether the central dispatcher should reject the interaction with an ephemeral
     * "I'm not in a voice channel" reply before calling {@link #handle}.
     *
     * <p>Defaults to {@code true}. Override to {@code false} for commands that manage
     * the bot's voice-channel presence themselves (e.g. {@code /join}, {@code /play}).
     */
    default boolean requiresBotInVoiceChannel() {
        return true;
    }

    void handle(SlashCommandInteractionEvent event, Guild guild);
}
