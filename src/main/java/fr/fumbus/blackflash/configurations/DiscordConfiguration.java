package fr.fumbus.blackflash.configurations;

import fr.fumbus.blackflash.listeners.SlashCommandListener;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */

@Log4j2
@Configuration
public class DiscordConfiguration {

    @Value("${discord.token}")
    private String token;

    @Value("${discord.activity}")
    private String activity;

    @PostConstruct
    public void initializeJDA() {
        try {
            log.info("Initializing JDA, registering listeners and commands...");

            JDA jda = JDABuilder.createDefault(token)
                    .setActivity(Activity.listening(activity))
                    .setAutoReconnect(true)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_VOICE_STATES)
                    .addEventListeners(new SlashCommandListener())
                    .build();


            jda.updateCommands()
                    .addCommands(List.of())
                    .queue();

            log.info("JDA initialized successfully.");
        } catch (InvalidTokenException e) {
            log.error("Failed to initialize JDA: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("An unexpected error occurred while initializing JDA: {}", e.getMessage(), e);
            throw e;
        }
    }
}
