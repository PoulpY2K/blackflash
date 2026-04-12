package fr.fumbus.blackflash.discord.slash.utils;

import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;

import java.util.Optional;

/**
 * Shared voice-state utilities.
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
}

