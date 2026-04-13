package fr.fumbus.blackflash.discord;

import fr.fumbus.blackflash.music.player.LoopMode;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

/**
 * Central factory for every Discord embed sent by the bot.
 *
 * <p>All methods return a ready-to-use {@link MessageEmbed}.
 * Send them via {@code event.replyEmbeds(BotEmbeds.xxx())} or
 * {@code hook.sendMessageEmbeds(BotEmbeds.xxx())}.
 *
 * <p>Styling is fully centralised here: change the colours or footer once
 * and every message in the bot updates automatically.
 *
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */
@UtilityClass
public class BotEmbeds {

    /**
     * Discord blurple — informational / neutral.
     */
    public static final int COLOR_PRIMARY = 0x5865F2;
    /**
     * Green — action succeeded.
     */
    public static final int COLOR_SUCCESS = 0x57F287;
    /**
     * Yellow — non-critical warning.
     */
    public static final int COLOR_WARNING = 0xFEE75C;
    /**
     * Red — error or rejected action.
     */
    public static final int COLOR_ERROR = 0xED4245;
    private static final String REPO_URL = "https://github.com/poulpy2k/blackflash";
    private static final String FOOTER_TEXT = "Blackflash";

    /**
     * Full help overview with command list and tech stack.
     */
    public static MessageEmbed help() {
        return base(COLOR_PRIMARY)
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
                .build();
    }

    /**
     * Bot successfully joined {@code channelName}.
     */
    public static MessageEmbed joined(String channelName) {
        return base(COLOR_SUCCESS)
                .setDescription("✅ Joined **" + channelName + "**!")
                .build();
    }

    /**
     * /join rejected — bot is already in a voice channel.
     */
    public static MessageEmbed alreadyInVoiceChannel() {
        return base(COLOR_ERROR)
                .setDescription("❌ I'm already in a voice channel. No need to make me join, I can't duplicate myself!")
                .build();
    }

    /**
     * Bot left the voice channel.
     */
    public static MessageEmbed left() {
        return base(COLOR_SUCCESS)
                .setDescription("👋 Left the voice channel.")
                .build();
    }

    /**
     * A single track was added to the queue.
     */
    public static MessageEmbed trackAdded(String title, long requesterId) {
        return base(COLOR_SUCCESS)
                .setDescription("🎵 Added to queue: **" + title + "**\nRequested by: <@" + requesterId + ">")
                .build();
    }

    /**
     * An entire playlist was added to the queue.
     */
    public static MessageEmbed playlistAdded(int count, String playlistName) {
        return base(COLOR_SUCCESS)
                .setDescription("🎵 Added **" + count + "** tracks to the queue from **" + playlistName + "**!")
                .build();
    }

    /**
     * The requested playlist was empty.
     */
    public static MessageEmbed playlistEmpty() {
        return base(COLOR_WARNING)
                .setDescription("⚠️ The playlist is empty!")
                .build();
    }

    /**
     * No tracks matched the search query.
     */
    public static MessageEmbed noTracksFound() {
        return base(COLOR_ERROR)
                .setDescription("❌ No tracks found for your query.")
                .build();
    }

    /**
     * Track loading failed.
     */
    public static MessageEmbed loadFailed(String reason) {
        return base(COLOR_ERROR)
                .setDescription("❌ Failed to load track: " + reason)
                .build();
    }

    /**
     * Current track was skipped.
     */
    public static MessageEmbed skipped() {
        return base(COLOR_PRIMARY)
                .setDescription("⏭️ Skipped the current track.")
                .build();
    }

    /**
     * Playback was stopped and the queue was cleared.
     */
    public static MessageEmbed stopped() {
        return base(COLOR_PRIMARY)
                .setDescription("⏹️ Stopped playback and cleared the queue.")
                .build();
    }

    /**
     * Loop mode was changed.
     */
    public static MessageEmbed loopMode(LoopMode mode) {
        String description = switch (mode) {
            case TRACK -> "🔂 Loop track enabled!";
            case QUEUE -> "🔁 Loop queue enabled!";
            case DISABLED -> "▶️ Loop disabled.";
        };
        return base(COLOR_PRIMARY)
                .setDescription(description)
                .build();
    }

    /**
     * Queue was shuffled.
     */
    public static MessageEmbed shuffled() {
        return base(COLOR_SUCCESS)
                .setDescription("🔀 Queue shuffled!")
                .build();
    }

    /**
     * Not enough tracks in the queue to shuffle.
     */
    public static MessageEmbed shuffleNotEnoughTracks() {
        return base(COLOR_ERROR)
                .setDescription("❌ There needs to be at least 2 tracks in the queue to shuffle!")
                .build();
    }

    /**
     * Command requires something to be playing, but nothing is.
     */
    public static MessageEmbed nothingPlaying() {
        return base(COLOR_WARNING)
                .setDescription("⚠️ Nothing is currently playing!")
                .build();
    }

    /** The dispatched command name did not match any registered handler. */
    public static MessageEmbed unknownCommand() {
        return base(COLOR_ERROR)
                .setDescription("❌ Unknown command!")
                .build();
    }

    /**
     * Member tried to use a music command while not in a voice channel.
     */
    public static MessageEmbed memberNotInVoiceChannel() {
        return base(COLOR_ERROR)
                .setDescription("❌ You must be in a voice channel to use music commands!")
                .build();
    }

    /**
     * Command requires the bot to already be in a voice channel.
     */
    public static MessageEmbed botNotInVoiceChannel() {
        return base(COLOR_ERROR)
                .setDescription("❌ I'm not in a voice channel! Use `/join` or `/play` to make me join.")
                .build();
    }

    private static EmbedBuilder base(int color) {
        return new EmbedBuilder()
                .setColor(color)
                .setFooter(FOOTER_TEXT);
    }
}

