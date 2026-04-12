package fr.fumbus.blackflash.discord.slash.handlers;

import fr.fumbus.blackflash.discord.slash.SlashCommandHandler;
import fr.fumbus.blackflash.music.manager.GuildMusicManagerRegistry;
import fr.fumbus.blackflash.music.player.LoopMode;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;

import static fr.fumbus.blackflash.discord.slash.utils.SlashCommandConstants.COMMAND_LOOP;
import static fr.fumbus.blackflash.discord.slash.utils.SlashCommandConstants.DESCRIPTION_LOOP;

/**
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */

@Component
@RequiredArgsConstructor
public class SlashLoopCommandHandler implements SlashCommandHandler {

    private static final CommandData COMMAND_DATA = Commands.slash(COMMAND_LOOP, DESCRIPTION_LOOP)
            .setContexts(InteractionContextType.GUILD);

    private final GuildMusicManagerRegistry registry;

    @Override
    public CommandData commandData() {
        return COMMAND_DATA;
    }

    @Override
    public void handle(SlashCommandInteractionEvent event, Guild guild) {
        final var scheduler = registry.getOrCreate(guild.getIdLong()).getTrackScheduler();
        final LoopMode newMode = scheduler.getLoopMode().next();
        scheduler.setLoopMode(newMode);

        String message = switch (newMode) {
            case TRACK -> "🔂 Loop track enabled!";
            case QUEUE -> "🔁 Loop queue enabled!";
            default -> "Loop disabled!";
        };

        event.reply(message).queue();
    }
}

