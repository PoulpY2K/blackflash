package fr.fumbus.blackflash.discord.jda.slash;

import lombok.Getter;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;

import static fr.fumbus.blackflash.discord.jda.slash.SlashCommandConstants.*;

/**
 * Registry that builds and holds all Discord slash commands.
 * Commands are built once at instantiation and cached for reuse.
 *
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */

@Getter
@Component
public class SlashCommandRegistry {

    private final List<CommandData> commands;

    public SlashCommandRegistry() {
        commands = List.of(
                buildHelpCommand(),
                buildJoinCommand(),
                buildPlayCommand(),
                buildSkipCommand(),
                buildLoopCommand(),
                buildShuffleCommand(),
                buildLeaveCommand(),
                buildStopCommand()
        );
    }

    private static @NonNull SlashCommandData buildPlayCommand() {
        return Commands.slash(COMMAND_PLAY, "Play a song or a playlist from a URL or search query")
                .setContexts(InteractionContextType.GUILD)
                .addOption(OptionType.STRING, "query", "URL or search query", true);
    }

    private static @NonNull SlashCommandData buildSkipCommand() {
        return Commands.slash(COMMAND_SKIP, "Skip the current track")
                .setContexts(InteractionContextType.GUILD);
    }

    private static @NonNull SlashCommandData buildStopCommand() {
        return Commands.slash(COMMAND_STOP, "Stop playback and clear the queue")
                .setContexts(InteractionContextType.GUILD);
    }

    private static @NonNull SlashCommandData buildShuffleCommand() {
        return Commands.slash(COMMAND_SHUFFLE, "Shuffle the playlist")
                .setContexts(InteractionContextType.GUILD);
    }

    private static @NonNull SlashCommandData buildLoopCommand() {
        return Commands.slash(COMMAND_LOOP, "Loop the current track or playlist")
                .setContexts(InteractionContextType.GUILD);
    }

    private static @NonNull SlashCommandData buildHelpCommand() {
        return Commands.slash(COMMAND_HELP, "Display help information")
                .setContexts(InteractionContextType.GUILD);
    }

    private static @NonNull SlashCommandData buildLeaveCommand() {
        return Commands.slash(COMMAND_LEAVE, "Leave the voice channel")
                .setContexts(InteractionContextType.GUILD);
    }

    private static @NonNull SlashCommandData buildJoinCommand() {
        return Commands.slash(COMMAND_JOIN, "Join the voice channel")
                .setContexts(InteractionContextType.GUILD);
    }
}

