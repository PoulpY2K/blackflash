package fr.fumbus.blackflash.discord.slash.handlers;

import fr.fumbus.blackflash.discord.slash.SlashCommandHandler;
import net.dv8tion.jda.api.EmbedBuilder;
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

    private static final String REPO_URL = "https://github.com/poulpy2k/blackflash";

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
        var embed = new EmbedBuilder()
                .setTitle("🎵 Blackflash", REPO_URL)
                .setDescription("A lightweight Discord music bot powered by **Lavalink v4** for high-quality audio streaming.")
                .addField("Commands", """
                        ✅ `/join`     — Join your voice channel
                        ✅ `/leave`    — Leave the voice channel
                        ✅ `/play`     — Play a song, playlist or search query
                        ✅ `/skip`     — Skip the current track
                        ✅ `/stop`     — Stop playback and clear the queue
                        ✅ `/loop`     — Cycle loop mode: off → track → queue
                        ✅ `/shuffle`  — Shuffle the queue
                        ✅ `/help`     — Show this message""", false)
                .addField("Tech Stack", "**Spring Boot** 4.0.5 · **Java** 25 · **JDA** 6.4.1 · **Lavalink** 4", false)
                .setColor(0x5865F2)
                .setFooter(REPO_URL)
                .build();

        event.replyEmbeds(embed).setEphemeral(true).queue();
    }
}



