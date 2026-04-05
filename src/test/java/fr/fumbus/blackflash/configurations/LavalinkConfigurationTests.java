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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SuppressWarnings("resource")
@ExtendWith(MockitoExtension.class)
class LavalinkConfigurationTests {

    @Spy
    @InjectMocks
    private LavalinkConfiguration lavalinkConfiguration;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(lavalinkConfiguration, "name", "test-node");
        ReflectionTestUtils.setField(lavalinkConfiguration, "uri", "localhost:2333");
        ReflectionTestUtils.setField(lavalinkConfiguration, "password", "youshallnotpass");
    }

    @Test
    void lavalinkClient_extractsUserIdFromToken() {
        try (MockedStatic<Helpers> mockedHelpers = mockStatic(Helpers.class);
             var _ = mockConstructionWithAnswer(LavalinkClient.class, Answers.RETURNS_DEEP_STUBS)) {

            mockedHelpers.when(() -> Helpers.getUserIdFromToken("test-token")).thenReturn(12345L);

            lavalinkConfiguration.lavalinkClient("test-token");

            mockedHelpers.verify(() -> Helpers.getUserIdFromToken("test-token"));
        }
    }

    @Test
    void lavalinkClient_returnsCreatedClient() {
        try (MockedStatic<Helpers> mockedHelpers = mockStatic(Helpers.class);
             MockedConstruction<LavalinkClient> mockedConstruction = mockConstructionWithAnswer(LavalinkClient.class, Answers.RETURNS_DEEP_STUBS)) {

            mockedHelpers.when(() -> Helpers.getUserIdFromToken(anyString())).thenReturn(12345L);

            var client = lavalinkConfiguration.lavalinkClient("test-token");

            assertThat(client).isSameAs(mockedConstruction.constructed().getFirst());
        }
    }

    @Test
    void lavalinkClient_addsVoiceRegionPenaltyProviderToLoadBalancer() {
        try (MockedStatic<Helpers> mockedHelpers = mockStatic(Helpers.class);
             MockedConstruction<LavalinkClient> mockedConstruction = mockConstructionWithAnswer(LavalinkClient.class, Answers.RETURNS_DEEP_STUBS)) {

            mockedHelpers.when(() -> Helpers.getUserIdFromToken(anyString())).thenReturn(12345L);

            lavalinkConfiguration.lavalinkClient("test-token");

            LavalinkClient client = mockedConstruction.constructed().getFirst();
            verify(client.getLoadBalancer()).addPenaltyProvider(any(VoiceRegionPenaltyProvider.class));
        }
    }

    @Test
    void lavalinkClient_addsNodeWithConfiguredProperties() {
        try (MockedStatic<Helpers> mockedHelpers = mockStatic(Helpers.class);
             MockedConstruction<LavalinkClient> mockedConstruction = mockConstructionWithAnswer(LavalinkClient.class, Answers.RETURNS_DEEP_STUBS)) {

            mockedHelpers.when(() -> Helpers.getUserIdFromToken(anyString())).thenReturn(12345L);

            lavalinkConfiguration.lavalinkClient("test-token");

            LavalinkClient client = mockedConstruction.constructed().getFirst();
            ArgumentCaptor<NodeOptions> captor = ArgumentCaptor.forClass(NodeOptions.class);
            verify(client).addNode(captor.capture());
            assertThat(captor.getValue().getName()).isEqualTo("test-node");
            assertThat(captor.getValue().getServerUri()).hasToString("ws://localhost:2333");
            assertThat(captor.getValue().getPassword()).isEqualTo("youshallnotpass");
            assertThat(captor.getValue().getRegionFilter()).isEqualTo(RegionGroup.EUROPE);
        }
    }

    @Test
    void lavalinkClient_registersAllEventSubscriptions() {
        try (MockedStatic<Helpers> mockedHelpers = mockStatic(Helpers.class);
             MockedConstruction<LavalinkClient> mockedConstruction = mockConstructionWithAnswer(LavalinkClient.class, Answers.RETURNS_DEEP_STUBS)) {

            mockedHelpers.when(() -> Helpers.getUserIdFromToken(anyString())).thenReturn(12345L);

            lavalinkConfiguration.lavalinkClient("test-token");

            LavalinkClient client = mockedConstruction.constructed().getFirst();
            verify(client).on(ReadyEvent.class);
            verify(client).on(StatsEvent.class);
            verify(client).on(EmittedEvent.class);
            verify(client.addNode(any())).on(TrackStartEvent.class);
        }
    }

    @Test
    void lavalinkClient_readyEventHandlerAccessesNodeNameAndSessionId() {
        try (MockedStatic<Helpers> mockedHelpers = mockStatic(Helpers.class);
             MockedConstruction<LavalinkClient> mockedConstruction = mockConstructionWithAnswer(LavalinkClient.class, Answers.RETURNS_DEEP_STUBS)) {

            mockedHelpers.when(() -> Helpers.getUserIdFromToken(anyString())).thenReturn(12345L);
            lavalinkConfiguration.lavalinkClient("test-token");

            LavalinkClient client = mockedConstruction.constructed().getFirst();
            ArgumentCaptor<Consumer<ReadyEvent>> captor = ArgumentCaptor.captor();
            verify(client.on(ReadyEvent.class)).subscribe(captor.capture());

            ReadyEvent event = mock(ReadyEvent.class, Answers.RETURNS_DEEP_STUBS);
            captor.getValue().accept(event);

            verify(event).getNode();
            verify(event).getSessionId();
        }
    }

    @Test
    void lavalinkClient_statsEventHandlerAccessesNodeAndPlayerCounts() {
        try (MockedStatic<Helpers> mockedHelpers = mockStatic(Helpers.class);
             MockedConstruction<LavalinkClient> mockedConstruction = mockConstructionWithAnswer(LavalinkClient.class, Answers.RETURNS_DEEP_STUBS)) {

            mockedHelpers.when(() -> Helpers.getUserIdFromToken(anyString())).thenReturn(12345L);
            lavalinkConfiguration.lavalinkClient("test-token");

            LavalinkClient client = mockedConstruction.constructed().getFirst();
            ArgumentCaptor<Consumer<StatsEvent>> captor = ArgumentCaptor.captor();
            verify(client.on(StatsEvent.class)).subscribe(captor.capture());

            StatsEvent event = mock(StatsEvent.class, Answers.RETURNS_DEEP_STUBS);
            captor.getValue().accept(event);

            verify(event).getNode();
            verify(event).getPlayingPlayers();
            verify(event).getPlayers();
        }
    }

    @Test
    void lavalinkClient_nodeTrackStartHandlerAccessesTrackInfo() {
        try (MockedStatic<Helpers> mockedHelpers = mockStatic(Helpers.class);
             MockedConstruction<LavalinkClient> mockedConstruction = mockConstructionWithAnswer(LavalinkClient.class, Answers.RETURNS_DEEP_STUBS)) {

            mockedHelpers.when(() -> Helpers.getUserIdFromToken(anyString())).thenReturn(12345L);
            lavalinkConfiguration.lavalinkClient("test-token");

            LavalinkClient client = mockedConstruction.constructed().getFirst();
            ArgumentCaptor<Consumer<TrackStartEvent>> captor = ArgumentCaptor.captor();
            verify(client.addNode(any()).on(TrackStartEvent.class)).subscribe(captor.capture());

            TrackStartEvent event = mock(TrackStartEvent.class, Answers.RETURNS_DEEP_STUBS);
            captor.getValue().accept(event);

            verify(event).getNode();
            verify(event).getTrack();
        }
    }

    @Test
    void lavalinkClient_emittedEventHandlerAccessesTrackInfoWhenEventIsTrackStartEvent() {
        try (MockedStatic<Helpers> mockedHelpers = mockStatic(Helpers.class);
             MockedConstruction<LavalinkClient> mockedConstruction = mockConstructionWithAnswer(LavalinkClient.class, Answers.RETURNS_DEEP_STUBS)) {

            mockedHelpers.when(() -> Helpers.getUserIdFromToken(anyString())).thenReturn(12345L);
            lavalinkConfiguration.lavalinkClient("test-token");

            LavalinkClient client = mockedConstruction.constructed().getFirst();
            ArgumentCaptor<Consumer<EmittedEvent>> captor = ArgumentCaptor.captor();
            verify(client.on(EmittedEvent.class)).subscribe(captor.capture());

            TrackStartEvent event = mock(TrackStartEvent.class, Answers.RETURNS_DEEP_STUBS);
            captor.getValue().accept(event);

            verify(event).getTrack();
        }
    }
}
