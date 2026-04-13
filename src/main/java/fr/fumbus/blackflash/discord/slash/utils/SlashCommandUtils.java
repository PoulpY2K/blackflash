package fr.fumbus.blackflash.discord.slash.utils;

import fr.fumbus.blackflash.discord.BotEmbeds;
import fr.fumbus.blackflash.music.manager.GuildMusicManager;
import fr.fumbus.blackflash.music.manager.GuildMusicManagerRegistry;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Shared voice-state and music-manager utilities for slash command handlers.
 *
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */
@UtilityClass
public class SlashCommandUtils {

    public static boolean isBotInVoiceChannel(Guild guild) {
        return Optional.ofNullable(guild.getSelfMember().getVoiceState())
                .map(GuildVoiceState::inAudioChannel)
                .orElse(false);
    }

    /**
     * Resolves the active {@link GuildMusicManager} for the given guild and runs
     * {@code action} with it. If no manager is registered (nothing is playing),
     * replies with an ephemeral {@link BotEmbeds#nothingPlaying()} embed instead.
     *
     * <p>Use this to eliminate the repetitive {@code getIfPresent} + empty-check
     * boilerplate in music-command handlers.
     *
     * @param registry the guild music manager registry
     * @param guild    the current Discord guild
     * @param event    the interaction event used for the error reply
     * @param action   the handler logic to execute when a manager is present
     */
    public static void withActiveManager(GuildMusicManagerRegistry registry,
                                         Guild guild,
                                         SlashCommandInteractionEvent event,
                                         Consumer<GuildMusicManager> action) {
        registry.getIfPresent(guild.getIdLong()).ifPresentOrElse(
                action,
                () -> event.replyEmbeds(BotEmbeds.nothingPlaying()).setEphemeral(true).queue()
        );
    }
}
