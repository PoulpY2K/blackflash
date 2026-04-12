package fr.fumbus.blackflash.discord.slash.handlers;

import fr.fumbus.blackflash.discord.slash.SlashCommandHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;

import static fr.fumbus.blackflash.discord.slash.utils.SlashCommandConstants.COMMAND_HELP;
import static fr.fumbus.blackflash.discord.slash.utils.SlashCommandConstants.DESCRIPTION_HELP;

/**
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */

@Component
public class SlashHelpCommandHandler implements SlashCommandHandler {

    private static final CommandData COMMAND_DATA = Commands.slash(COMMAND_HELP, DESCRIPTION_HELP)
            .setContexts(InteractionContextType.GUILD);

    @Override
    public CommandData commandData() {
        return COMMAND_DATA;
    }

    @Override
    public boolean requiresBotInVoiceChannel() {
        return false;
    }

    @Override
    public void handle(SlashCommandInteractionEvent event, Guild guild) {
        event.reply("🚧 The /help command is not implemented yet. Stay tuned!").setEphemeral(true).queue();
    }
}

