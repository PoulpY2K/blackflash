package fr.fumbus.blackflash.discord.slash.handlers;

import dev.arbjerg.lavalink.client.LavalinkClient;
import fr.fumbus.blackflash.discord.slash.SlashCommandHandler;
import fr.fumbus.blackflash.discord.slash.utils.SlashCommandUtils;
import fr.fumbus.blackflash.music.manager.GuildMusicManagerRegistry;
import fr.fumbus.blackflash.music.player.AudioLoader;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static fr.fumbus.blackflash.discord.slash.utils.SlashCommandConstants.COMMAND_PLAY;
import static fr.fumbus.blackflash.discord.slash.utils.SlashCommandConstants.DESCRIPTION_PLAY;

/**
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */

@Component
@RequiredArgsConstructor
public class SlashPlayCommandHandler implements SlashCommandHandler {

    private static final CommandData COMMAND_DATA = Commands.slash(COMMAND_PLAY, DESCRIPTION_PLAY)
            .setContexts(InteractionContextType.GUILD)
            .addOption(OptionType.STRING, "query", "URL or search query", true);

    private final LavalinkClient lavalink;
    private final GuildMusicManagerRegistry registry;
    private final SlashJoinCommandHandler slashJoinCommandHandler;

    @Override
    public CommandData commandData() {
        return COMMAND_DATA;
    }

    /**
     * Play joins the channel automatically when the bot is not present yet.
     */
    @Override
    public boolean requiresBotInVoiceChannel() {
        return false;
    }

    @Override
    public void handle(SlashCommandInteractionEvent event, Guild guild) {
        if (SlashCommandUtils.isBotInVoiceChannel(guild)) {
            event.deferReply(false).queue();
        } else {
            slashJoinCommandHandler.joinChannel(event);
        }

        final String query = Optional.ofNullable(event.getOption("query"))
                .map(OptionMapping::getAsString)
                .orElse("");
        final long guildId = guild.getIdLong();
        lavalink.getOrCreateLink(guildId)
                .loadItem(query)
                .subscribe(new AudioLoader(event, registry.getOrCreate(guildId)));
    }
}

