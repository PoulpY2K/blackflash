package fr.fumbus.blackflash.discord.slash.handlers;

import fr.fumbus.blackflash.discord.slash.SlashCommandHandler;
import fr.fumbus.blackflash.music.manager.GuildMusicManagerRegistry;
import fr.fumbus.blackflash.music.player.TrackScheduler;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;

import static fr.fumbus.blackflash.discord.slash.utils.SlashCommandConstants.COMMAND_SHUFFLE;
import static fr.fumbus.blackflash.discord.slash.utils.SlashCommandConstants.DESCRIPTION_SHUFFLE;

/**
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */

@Component
@RequiredArgsConstructor
public class SlashShuffleCommandHandler implements SlashCommandHandler {

    private static final CommandData COMMAND_DATA = Commands.slash(COMMAND_SHUFFLE, DESCRIPTION_SHUFFLE)
            .setContexts(InteractionContextType.GUILD);

    private final GuildMusicManagerRegistry registry;

    @Override
    public CommandData commandData() {
        return COMMAND_DATA;
    }

    @Override
    public void handle(SlashCommandInteractionEvent event, Guild guild) {
        var scheduler = registry.getOrCreate(guild.getIdLong()).getTrackScheduler();

        if (queueHasOnlyOneTrack(scheduler)) {
            event.reply("There needs to be at least 2 tracks in the queue to shuffle!").setEphemeral(true).queue();
            return;
        }

        scheduler.shuffle();
        event.reply("🔀 Queue shuffled!").queue();
    }

    private static boolean queueHasOnlyOneTrack(TrackScheduler scheduler) {
        return scheduler.queue.size() <= 1;
    }
}

