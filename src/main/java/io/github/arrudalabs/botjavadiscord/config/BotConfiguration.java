package io.github.arrudalabs.botjavadiscord.config;

import io.github.arrudalabs.botjavadiscord.listeners.HelloSlashCommandListener;
import io.github.arrudalabs.botjavadiscord.listeners.MessageListener;
import io.github.arrudalabs.botjavadiscord.listeners.ReadyListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.security.auth.login.LoginException;
import java.util.EnumSet;

@Configuration
public class BotConfiguration {

    private static Logger logger = LoggerFactory.getLogger(BotConfiguration.class);

    private final String token;
    private final HelloSlashCommandListener helloSlashCommandListener;
    private JDA jda;

    public BotConfiguration(Environment environment, HelloSlashCommandListener helloSlashCommandListener) {
        this.token = environment.getProperty("BOT_TOKEN", "");
        this.helloSlashCommandListener = helloSlashCommandListener;
    }

    @PostConstruct
    public void start() throws LoginException, InterruptedException {
        createJDA();
        addListeners();
        addCommands();
        // optionally block until JDA is ready
        jda.awaitReady();
    }

    private void addCommands() {

        // These commands take up to an hour to be activated after creation/update/delete
        CommandListUpdateAction commands = jda.updateCommands();

//        /hello who: Max
//        → Hi Max!
        commands.addCommands(
                new CommandData("hello", "Comando para teste")
                        .addOptions(
                                new OptionData(
                                        OptionType.STRING,
                                        "who",
                                        "Informe um nome?")
                                        .setRequired(true)
                        )
        );

        //        /quem-sou
        //        → @Maximillian
        commands.addCommands(
                new CommandData("quem-sou", "Exibe vc")
        );
        // Send the new set of commands to discord, this will override any existing global commands with the new set provided here
        commands.queue();
    }

    private void addListeners() {
        jda.addEventListener(new ReadyListener());
        //jda.addEventListener(new MessageListener());
        jda.addEventListener(helloSlashCommandListener);
    }

    private void createJDA() throws LoginException {
        JDABuilder builder = JDABuilder
                .createLight(token, EnumSet.noneOf(GatewayIntent.class))
                // slash commands don't need any intents
                ;
        this.jda = builder.build();
    }


    @PreDestroy
    public void onShutdown() {
        jda.shutdownNow();
    }


}
