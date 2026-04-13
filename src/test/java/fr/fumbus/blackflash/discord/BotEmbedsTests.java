package fr.fumbus.blackflash.discord;

import fr.fumbus.blackflash.music.player.LoopMode;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static fr.fumbus.blackflash.discord.BotEmbeds.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */
class BotEmbedsTests {

    // ── help ────────────────────────────────────────────────────────────────────

    @Test
    void help_hasPrimaryColorTitleAndRepoUrl() {
        MessageEmbed embed = help();
        assertThat(embed.getColorRaw()).isEqualTo(COLOR_PRIMARY);
        assertThat(embed.getTitle()).isEqualTo("🎵 Blackflash");
        assertThat(embed.getUrl()).isEqualTo("https://github.com/poulpy2k/blackflash");
        assertThat(embed.getFields()).isNotEmpty();
    }

    // ── Voice ───────────────────────────────────────────────────────────────────

    @Test
    void joined_hasSuccessColorAndContainsChannelName() {
        MessageEmbed embed = joined("Music Lounge");
        assertThat(embed.getColorRaw()).isEqualTo(COLOR_SUCCESS);
        assertThat(embed.getDescription()).contains("Music Lounge");
    }

    @Test
    void alreadyInVoiceChannel_hasErrorColor() {
        assertThat(alreadyInVoiceChannel().getColorRaw()).isEqualTo(COLOR_ERROR);
    }

    @Test
    void left_hasSuccessColor() {
        assertThat(left().getColorRaw()).isEqualTo(COLOR_SUCCESS);
    }

    // ── Playback ────────────────────────────────────────────────────────────────

    @Test
    void trackAdded_hasSuccessColorAndContainsTitleAndMention() {
        MessageEmbed embed = trackAdded("Bohemian Rhapsody", 99L);
        assertThat(embed.getColorRaw()).isEqualTo(COLOR_SUCCESS);
        assertThat(embed.getDescription()).contains("Bohemian Rhapsody");
        assertThat(embed.getDescription()).contains("<@99>");
    }

    @Test
    void playlistAdded_hasSuccessColorAndContainsCountAndName() {
        MessageEmbed embed = playlistAdded(5, "Top Hits");
        assertThat(embed.getColorRaw()).isEqualTo(COLOR_SUCCESS);
        assertThat(embed.getDescription()).contains("5");
        assertThat(embed.getDescription()).contains("Top Hits");
    }

    @Test
    void playlistEmpty_hasWarningColor() {
        assertThat(playlistEmpty().getColorRaw()).isEqualTo(COLOR_WARNING);
    }

    @Test
    void noTracksFound_hasErrorColor() {
        assertThat(noTracksFound().getColorRaw()).isEqualTo(COLOR_ERROR);
    }

    @Test
    void loadFailed_hasErrorColorAndContainsReason() {
        MessageEmbed embed = loadFailed("Connection refused");
        assertThat(embed.getColorRaw()).isEqualTo(COLOR_ERROR);
        assertThat(embed.getDescription()).contains("Connection refused");
    }

    @Test
    void skipped_hasPrimaryColor() {
        assertThat(skipped().getColorRaw()).isEqualTo(COLOR_PRIMARY);
    }

    @Test
    void stopped_hasPrimaryColor() {
        assertThat(stopped().getColorRaw()).isEqualTo(COLOR_PRIMARY);
    }

    @ParameterizedTest
    @EnumSource(LoopMode.class)
    void loopMode_hasPrimaryColorForAllModes(LoopMode mode) {
        assertThat(loopMode(mode).getColorRaw()).isEqualTo(COLOR_PRIMARY);
    }

    @Test
    void loopMode_track_descriptionContainsTrack() {
        assertThat(loopMode(LoopMode.TRACK).getDescription()).containsIgnoringCase("track");
    }

    @Test
    void loopMode_queue_descriptionContainsQueue() {
        assertThat(loopMode(LoopMode.QUEUE).getDescription()).containsIgnoringCase("queue");
    }

    @Test
    void loopMode_disabled_descriptionContainsDisabled() {
        assertThat(loopMode(LoopMode.DISABLED).getDescription()).containsIgnoringCase("disabled");
    }

    @Test
    void shuffled_hasSuccessColor() {
        assertThat(shuffled().getColorRaw()).isEqualTo(COLOR_SUCCESS);
    }

    @Test
    void shuffleNotEnoughTracks_hasErrorColor() {
        assertThat(shuffleNotEnoughTracks().getColorRaw()).isEqualTo(COLOR_ERROR);
    }

    @Test
    void unknownCommand_hasErrorColor() {
        assertThat(unknownCommand().getColorRaw()).isEqualTo(COLOR_ERROR);
    }

    /**
     * Member tried to use a music command while not in a voice channel.
     */
    @Test
    void memberNotInVoiceChannel_hasErrorColor() {
        assertThat(memberNotInVoiceChannel().getColorRaw()).isEqualTo(COLOR_ERROR);
    }

    @Test
    void botNotInVoiceChannel_hasErrorColor() {
        assertThat(botNotInVoiceChannel().getColorRaw()).isEqualTo(COLOR_ERROR);
    }

    // ── Footer ──────────────────────────────────────────────────────────────────

    @SuppressWarnings("DataFlowIssue")
    @Test
    void allEmbeds_haveBlackflashFooter() {
        assertThat(help().getFooter().getText()).isEqualTo("Blackflash");
        assertThat(joined("x").getFooter().getText()).isEqualTo("Blackflash");
        assertThat(left().getFooter().getText()).isEqualTo("Blackflash");
        assertThat(skipped().getFooter().getText()).isEqualTo("Blackflash");
        assertThat(memberNotInVoiceChannel().getFooter().getText()).isEqualTo("Blackflash");
    }
}

