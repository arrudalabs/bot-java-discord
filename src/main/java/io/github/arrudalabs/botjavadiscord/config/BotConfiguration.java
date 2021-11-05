package io.github.arrudalabs.botjavadiscord.config;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.security.auth.login.LoginException;

@Configuration
public class BotConfiguration {

    private static Logger logger = LoggerFactory.getLogger(BotConfiguration.class);

    private final String token;
    private JDA jda;

    public BotConfiguration(Environment environment) {
        this.token = environment.getProperty("BOT_TOKEN", "");
    }

    @PostConstruct
    public void start() throws LoginException {

        JDABuilder builder = JDABuilder.createDefault(token);

        // Disable cache for member activities (streaming/games/spotify)
        builder.disableCache(CacheFlag.ACTIVITY);

        // Only cache members who are either in a voice channel or owner of the guild
        builder.setMemberCachePolicy(MemberCachePolicy.VOICE.or(MemberCachePolicy.OWNER));

        this.jda = builder.build();
        jda.addEventListener(new ReadyListener());
        jda.addEventListener(new MessageListener());
    }


    @PreDestroy
    public void onShutdown(){
        jda.shutdownNow();
    }

    class ReadyListener implements EventListener {

        @Override
        public void onEvent(@NotNull GenericEvent event) {
            if (event instanceof ReadyEvent)
                System.out.println("API is ready!");
        }
    }

    class MessageListener extends ListenerAdapter{
        @Override
        public void onMessageReceived(MessageReceivedEvent event)
        {
            if (event.isFromType(ChannelType.PRIVATE))
            {
                logger.info(String.format("[PM] %s: %s", event.getAuthor().getName(),
                        event.getMessage().getContentDisplay()));
            }
            else
            {
                logger.info(String.format("[%s][%s] %s: %s", event.getGuild().getName(),
                        event.getTextChannel().getName(), event.getMember().getEffectiveName(),
                        event.getMessage().getContentDisplay()));
            }

            Message msg = event.getMessage();
            if (msg.getContentRaw().equals("!ping"))
            {
                MessageChannel channel = event.getChannel();
                long time = System.currentTimeMillis();
                channel.sendMessage("Pong!") /* => RestAction<Message> */
                        .queue(response /* => Message */ -> {
                            response.editMessageFormat("Pong: %d ms", System.currentTimeMillis() - time).queue();
                        });
            }
        }
    }

}
