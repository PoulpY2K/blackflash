package fr.fumbus.blackflash.configurations;

import dev.arbjerg.lavalink.client.Helpers;
import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.NodeOptions;
import dev.arbjerg.lavalink.client.event.EmittedEvent;
import dev.arbjerg.lavalink.client.event.ReadyEvent;
import dev.arbjerg.lavalink.client.event.StatsEvent;
import dev.arbjerg.lavalink.client.event.TrackStartEvent;
import dev.arbjerg.lavalink.client.loadbalancing.RegionGroup;
import dev.arbjerg.lavalink.client.loadbalancing.builtin.VoiceRegionPenaltyProvider;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author Jérémy Laurent <poulpy2k>
 * @see "https://github.com/poulpy2k"
 */

@Log4j2
@Configuration
public class LavalinkConfiguration {

    @Value("${lavalink.name}")
    private String name;

    @Value("${lavalink.uri}")
    private String uri;

    @Value("${lavalink.password}")
    private String password;

    @Bean
    @Primary
    public LavalinkClient lavalinkClient(@Value("${discord.token}") String token) {
        LavalinkClient client = new LavalinkClient(Helpers.getUserIdFromToken(token));
        client.getLoadBalancer().addPenaltyProvider(new VoiceRegionPenaltyProvider());
        registerNodes(client);
        registerInfrastructureListeners(client);
        return client;
    }

    private void registerNodes(LavalinkClient client) {
        client.addNode(
                        new NodeOptions.Builder()
                                .setName(name)
                                .setServerUri("ws://" + uri)
                                .setPassword(password)
                                .setRegionFilter(RegionGroup.EUROPE)
                                .build()
                ).on(TrackStartEvent.class)
                .subscribe(event -> log.info("Node '{}' started track: {}",
                        event.getNode().getName(), event.getTrack().getInfo().getTitle()));
    }

    private void registerInfrastructureListeners(LavalinkClient client) {
        client.on(ReadyEvent.class)
                .subscribe(event ->
                        log.info("Node '{}' is ready, session id is '{}'!", event.getNode().getName(), event.getSessionId())
                );

        client.on(StatsEvent.class)
                .subscribe(event ->
                        log.info("Node '{}' has stats, current players: {}/{} (link count {})",
                                event.getNode().getName(),
                                event.getPlayingPlayers(),
                                event.getPlayers(),
                                client.getLinks().size()
                        )
                );

        client.on(EmittedEvent.class).subscribe(event -> {
            log.info("Node '{}' emitted event: {}", event.getNode().getName(), event);
            if (event instanceof TrackStartEvent trackStartEvent) {
                log.debug("Emitted track start: {}", trackStartEvent.getTrack().getInfo().getTitle());
            }
        });
    }
}
