package fr.fumbus.blackflash.discord.slash.utils;

import lombok.experimental.UtilityClass;

/**
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */

@UtilityClass
public class SlashCommandConstants {
    // -------------- COMMAND NAMES ----------------
    public static final String COMMAND_PLAY = "play";
    public static final String COMMAND_SKIP = "skip";
    public static final String COMMAND_STOP = "stop";
    public static final String COMMAND_SHUFFLE = "shuffle";
    public static final String COMMAND_LOOP = "loop";
    public static final String COMMAND_LEAVE = "leave";
    public static final String COMMAND_HELP = "help";
    public static final String COMMAND_JOIN = "join";

    // -------------- COMMAND DESCRIPTIONS ----------------
    public static final String DESCRIPTION_PLAY = "Play a song or a playlist from a URL or search query";
    public static final String DESCRIPTION_SKIP = "Skip the current track";
    public static final String DESCRIPTION_STOP = "Stop playback and clear the queue";
    public static final String DESCRIPTION_SHUFFLE = "Shuffle the queue";
    public static final String DESCRIPTION_LOOP = "Loop the current track or playlist";
    public static final String DESCRIPTION_LEAVE = "Leave the voice channel";
    public static final String DESCRIPTION_HELP = "Show the list of available commands";
    public static final String DESCRIPTION_JOIN = "Join the voice channel";

    // -------------- MESSAGES ----------------
    public static final String MESSAGE_MEMBER_NOT_IN_VOICE_CHANNEL = "You must be in a voice channel to use music commands!";
    public static final String MESSAGE_BOT_NOT_IN_VOICE_CHANNEL = "I'm not in a voice channel! Use /join or /play a song to make me join your voice channel.";
    public static final String MESSAGE_BOT_ALREADY_IN_VOICE_CHANNEL = "I'm already in a voice channel. No need to make me join, I can't duplicate myself!";
}
