package fr.fumbus.blackflash.listeners;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class SlashCommandListenerTests {

    @InjectMocks
    private SlashCommandListener listener;

    @Test
    void slashCommandListener_instantiatesWithoutError() {
        assertDoesNotThrow(SlashCommandListener::new);
    }

    @Test
    void slashCommandListener_extendsListenerAdapter() {
        assertThat(listener).isInstanceOf(ListenerAdapter.class);
    }
}

