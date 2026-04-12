package fr.fumbus.blackflash.configurations;

import club.minnced.discord.jdave.interop.JDaveSessionFactory;
import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.libraries.jda.JDAVoiceUpdateListener;
import fr.fumbus.blackflash.discord.slash.SlashCommandListener;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.audio.AudioModuleConfig;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.Executors;


/**
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */

@Log4j2
@Configuration
@RequiredArgsConstructor
public class DiscordConfiguration {

    private final List<GatewayIntent> enabledIntents = List.of(
            GatewayIntent.GUILD_MESSAGE_REACTIONS,
            GatewayIntent.GUILD_MEMBERS,
            GatewayIntent.GUILD_PRESENCES,
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_VOICE_STATES
    );

    private final SlashCommandListener listener;
    private final LavalinkClient lavalink;

    @Value("${discord.token}")
    private String token;

    @Value("${discord.activity}")
    private String activity;

    @PostConstruct
    public void initializeJDA() {
        try {
            log.info("Initializing JDA, registering listeners...");

            JDABuilder.createDefault(token)
                    .setEventPool(Executors.newVirtualThreadPerTaskExecutor(), true)
                    .setVoiceDispatchInterceptor(new JDAVoiceUpdateListener(lavalink))
                    .enableIntents(enabledIntents)
                    .enableCache(CacheFlag.VOICE_STATE, CacheFlag.ONLINE_STATUS, CacheFlag.ACTIVITY)
                    .setActivity(Activity.listening(activity))
                    .setAutoReconnect(true)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .setChunkingFilter(ChunkingFilter.include(100))
                    .addEventListeners(listener)
                    .setAudioModuleConfig(new AudioModuleConfig()
                            .withDaveSessionFactory(new JDaveSessionFactory()))
                    .build()
                    .awaitReady();

            log.info("JDA initialized successfully.");
        } catch (InvalidTokenException e) {
            log.error("Invalid token, JDA could not initialize: {}", e.getMessage(), e);
        } catch (InterruptedException e) {
            log.error("JDA initialization was interrupted: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("An unexpected error occurred while initializing JDA: {}", e.getMessage(), e);
            throw e;
        }
    }
}
