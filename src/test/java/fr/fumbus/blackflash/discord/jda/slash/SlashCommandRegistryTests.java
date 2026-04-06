package fr.fumbus.blackflash.discord.jda.slash;

import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SlashCommandRegistryTests {

    private final SlashCommandRegistry registry = new SlashCommandRegistry();

    @Test
    void commands_containsAllExpectedCommandNames() {
        assertThat(registry.getCommands())
                .extracting(CommandData::getName)
                .containsExactlyInAnyOrder("help", "join", "play", "skip", "loop", "shuffle", "leave", "stop");
    }

    @Test
    void allCommands_areGuildScoped() {
        registry.getCommands().stream()
                .map(SlashCommandData.class::cast)
                .forEach(cmd -> assertThat(cmd.getContexts()).contains(InteractionContextType.GUILD));
    }

    @Test
    void play_commandHasExactlyOneRequiredQueryStringOption() {
        SlashCommandData playCommand = (SlashCommandData) registry.getCommands().stream()
                .filter(cmd -> "play".equals(cmd.getName()))
                .findFirst()
                .orElseThrow();

        assertThat(playCommand.getOptions()).hasSize(1);
        assertThat(playCommand.getOptions().getFirst())
                .matches(opt -> "query".equals(opt.getName())
                        && opt.getType() == OptionType.STRING
                        && opt.isRequired());
    }

    @Test
    void commandsWithoutOptions_haveEmptyOptionsList() {
        registry.getCommands().stream()
                .map(SlashCommandData.class::cast)
                .filter(cmd -> !"play".equals(cmd.getName()))
                .forEach(cmd -> assertThat(cmd.getOptions()).isEmpty());
    }

    @Test
    void commands_listIsStableAcrossMultipleCalls() {
        var first = registry.getCommands();
        assertThat(registry.getCommands()).isSameAs(first);
    }
}



