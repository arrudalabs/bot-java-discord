package io.github.arrudalabs.botjavadiscord.listeners;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class HelloSlashCommandListener extends ListenerAdapter {

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        if ("quem-sou".equalsIgnoreCase(event.getName())){
            event.getHook()
                    .getInteraction().deferReply(true)
                    .queue(hook->{
                       hook.sendMessage(event.getUser().getAsMention()).queue();
                    });
            return;
        }
        if (!"hello".equalsIgnoreCase(event.getName()))
            return;
        event.getHook()
                .getInteraction().deferReply(true)
                .queue(hook->{
                    hook.sendMessage(
                            "Hi, " + event.getOption("who").getAsString()
                    ).queue();
                });
    }
}
