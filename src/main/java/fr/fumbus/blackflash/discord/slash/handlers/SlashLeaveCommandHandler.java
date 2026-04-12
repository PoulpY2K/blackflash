package fr.fumbus.blackflash.discord.slash.handlers;

import fr.fumbus.blackflash.discord.slash.SlashCommandHandler;
import fr.fumbus.blackflash.music.manager.GuildMusicManagerRegistry;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;

import static fr.fumbus.blackflash.discord.slash.utils.SlashCommandConstants.COMMAND_LEAVE;
import static fr.fumbus.blackflash.discord.slash.utils.SlashCommandConstants.DESCRIPTION_LEAVE;

/**
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */

@Component
@RequiredArgsConstructor
public class SlashLeaveCommandHandler implements SlashCommandHandler {

    private static final CommandData COMMAND_DATA = Commands.slash(COMMAND_LEAVE, DESCRIPTION_LEAVE)
            .setContexts(InteractionContextType.GUILD);

    private final GuildMusicManagerRegistry registry;

    @Override
    public CommandData commandData() {
        return COMMAND_DATA;
    }

    @Override
    public void handle(SlashCommandInteractionEvent event, Guild guild) {
        event.getJDA().getDirectAudioController().disconnect(guild);
        registry.remove(guild.getIdLong());
        event.reply("Leaving the channel!").queue();
    }
}

