package fr.fumbus.blackflash.music.manager;

import dev.arbjerg.lavalink.client.LavalinkClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockConstruction;

@ExtendWith(MockitoExtension.class)
class GuildMusicManagerRegistryTests {

    @Mock
    LavalinkClient lavalink;

    @InjectMocks
    GuildMusicManagerRegistry registry;

    @Test
    void getOrCreate_createsNewManagerForNewGuildId() {
        try (MockedConstruction<GuildMusicManager> ctor = mockConstruction(GuildMusicManager.class)) {
            GuildMusicManager manager = registry.getOrCreate(42L);

            assertThat(ctor.constructed()).hasSize(1);
            assertThat(registry.getIfPresent(42L)).isPresent();
            assertThat(manager).isNotNull();
        }
    }

    @Test
    void getOrCreate_returnsSameInstanceForSameGuildId() {
        try (MockedConstruction<GuildMusicManager> ctor = mockConstruction(GuildMusicManager.class)) {
            GuildMusicManager first = registry.getOrCreate(42L);
            GuildMusicManager second = registry.getOrCreate(42L);

            assertThat(ctor.constructed()).hasSize(1);
            assertThat(first).isSameAs(second);
        }
    }

    @Test
    void remove_removesManagerFromMap() {
        try (var _ = mockConstruction(GuildMusicManager.class)) {
            registry.getOrCreate(42L);
            registry.remove(42L);

            assertThat(registry.getIfPresent(42L)).isEmpty();
        }
    }
}

