package fr.fumbus.blackflash.discord.slash;

import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.event.TrackEndEvent;
import dev.arbjerg.lavalink.client.event.TrackStartEvent;
import fr.fumbus.blackflash.music.manager.GuildMusicManagerRegistry;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static fr.fumbus.blackflash.discord.slash.utils.SlashCommandConstants.MESSAGE_BOT_NOT_IN_VOICE_CHANNEL;
import static fr.fumbus.blackflash.discord.slash.utils.SlashCommandConstants.MESSAGE_MEMBER_NOT_IN_VOICE_CHANNEL;
import static fr.fumbus.blackflash.discord.slash.utils.SlashCommandUtils.isBotInVoiceChannel;
import static java.util.Objects.isNull;

/**
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */

@Log4j2
@Component
public class SlashCommandListener extends ListenerAdapter {

    private final LavalinkClient lavalink;
    private final GuildMusicManagerRegistry musicManagerRegistry;
    private final Map<String, SlashCommandHandler> handlersByName;

    public SlashCommandListener(LavalinkClient lavalink,
                                GuildMusicManagerRegistry musicManagerRegistry,
                                List<SlashCommandHandler> slashCommandHandlers) {
        this.lavalink = lavalink;
        this.musicManagerRegistry = musicManagerRegistry;
        this.handlersByName = slashCommandHandlers
                .stream()
                .collect(Collectors.toUnmodifiableMap(
                        commandHandler -> commandHandler.commandData().getName(),
                        commandHandler -> commandHandler)
                );
    }

    @PostConstruct
    void init() {
        lavalink.on(TrackStartEvent.class).subscribe(event -> {
            log.trace("{}: track started: {}", event.getNode().getName(), event.getTrack().getInfo());
            musicManagerRegistry.getIfPresent(event.getGuildId())
                    .ifPresent(manager -> manager.getTrackScheduler().onTrackStart(event));
        });

        lavalink.on(TrackEndEvent.class).subscribe(event -> {
            log.trace("{}: track ended: {}, reason: {}", event.getNode().getName(), event.getTrack().getInfo(), event.getEndReason());
            musicManagerRegistry.getIfPresent(event.getGuildId())
                    .ifPresent(manager -> manager.getTrackScheduler().onTrackEnd(event));
        });
    }

    @Override
    public void onReady(@NonNull ReadyEvent event) {
        log.info("{} is ready!", event.getJDA().getSelfUser().getAsTag());
        event.getJDA().updateCommands()
                .addCommands(handlersByName.values().stream()
                        .map(SlashCommandHandler::commandData).toList())
                .queue();
    }

    @Override
    public void onSlashCommandInteraction(@NonNull SlashCommandInteractionEvent event) {
        Guild guild = getGuild(event);
        Member member = getMember(event);
        if (checkIfMemberNotInVoiceChannel(member)) {
            event.reply(MESSAGE_MEMBER_NOT_IN_VOICE_CHANNEL).setEphemeral(true).queue();
            return;
        }

        String commandName = event.getFullCommandName();
        SlashCommandHandler handler = handlersByName.get(commandName);

        if (isNull(handler)) {
            log.warn("Received an unknown slash command interaction: {}", commandName);
            event.reply("Unknown command!").setEphemeral(true).queue();
            return;
        } else if (handler.requiresBotInVoiceChannel() && !isBotInVoiceChannel(guild)) {
            event.reply(MESSAGE_BOT_NOT_IN_VOICE_CHANNEL).setEphemeral(true).queue();
            return;
        }

        handler.handle(event, guild);
    }

    private static @NonNull Member getMember(SlashCommandInteractionEvent event) {
        return Optional
                .ofNullable(event.getMember())
                .orElseThrow(() -> {
                    log.error("Received a slash command interaction without a member context!");
                    return new IllegalStateException("Received a slash command interaction without a member context!");
                });
    }

    private static boolean checkIfMemberNotInVoiceChannel(Member member) {
        return isNull(member.getVoiceState()) || !member.getVoiceState().inAudioChannel();
    }

    private static @NonNull Guild getGuild(SlashCommandInteractionEvent event) {
        return Optional
                .ofNullable(event.getGuild())
                .orElseThrow(() -> {
                    log.error("Received a slash command interaction without a guild context!");
                    return new IllegalStateException("Received a slash command interaction without a guild context!");
                });
    }
}
