package fr.fumbus.blackflash.music.player;

/**
 * Represents the loop state for a guild's music queue.
 *
 * <ul>
 *   <li>{@link #DISABLED} – No looping; the next track in the queue is played when the current one ends.</li>
 *   <li>{@link #TRACK}    – The current track is replayed indefinitely when it ends.</li>
 *   <li>{@link #QUEUE}    – All tracks cycle: the ended track is appended to the tail of the queue
 *                           and the head is started next.</li>
 * </ul>
 *
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */
public enum LoopMode {
    DISABLED,
    TRACK,
    QUEUE;

    /**
     * Returns the next loop mode in the cycle: {@code DISABLED → TRACK → QUEUE → DISABLED}.
     */
    public LoopMode next() {
        LoopMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}


