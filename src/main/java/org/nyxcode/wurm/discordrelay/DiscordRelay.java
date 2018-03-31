package org.nyxcode.wurm.discordrelay;

import com.wurmonline.server.Message;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.villages.PvPAlliance;
import com.wurmonline.server.villages.Village;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.gotti.wurmunlimited.modloader.interfaces.*;

import javax.security.auth.login.LoginException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;


/**
 * Created by whisper2shade on 22.04.2017.
 */
public class DiscordRelay extends ListenerAdapter implements WurmServerMod, PreInitable, Configurable, ChannelMessageListener, PlayerMessageListener {
    public static final Logger logger = Logger.getLogger(DiscordRelay.class.getName());

    private static JDA jda;
    private static String botToken;
    private static String serverName;
    //private String wurmBotName;
    private boolean useUnderscore;

    @Override
    public void preInit() {
        try {
            jda = new JDABuilder(AccountType.BOT).setToken(botToken).addEventListener(this).buildBlocking();
        } catch (LoginException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (RateLimitedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void configure(Properties properties) {
        botToken = properties.getProperty("botToken");
        serverName = properties.getProperty("discordServerName");
        //wurmBotName = properties.getProperty("wurmBotName");
        useUnderscore = Boolean.parseBoolean(properties.getProperty("useUnderscore", "false"));
    }

    private static final DateFormat df = new SimpleDateFormat("HH:mm:ss");
    public static void sendToDiscord(String channel, String message){
        MessageBuilder builder = new MessageBuilder();
        message = "[" + df.format(new Date(System.currentTimeMillis())) + "] "+message; // Add timestamp

        builder.append(message);
        jda.getGuildsByName(serverName, true).get(0).getTextChannelsByName(channel, true).get(0).sendMessage(builder.build()).queue();
    }

    @Override
    public MessagePolicy onKingdomMessage(Message message) {
        if(message.getWindow().startsWith("GL-")){
            byte kingdomId = message.getSender().getKingdomId();
            //Kingdom kingdom = Kingdoms.getKingdom(kingdomId);
            String kingdomName = discordifyName("GL-"+Kingdoms.getChatNameFor(kingdomId));
            sendToDiscord(kingdomName, message.getMessage());
	        /*MessageBuilder builder = new MessageBuilder();

	        builder.append(message.getMessage());
	        jda.getGuildsByName(serverName, true).get(0).getTextChannelsByName(kingdomName, true).get(0).sendMessage(builder.build()).queue();*/
        }

        return MessagePolicy.PASS;
    }

    public void sendToGlobalKingdomChat(final String channel, final String message) {
        List<Kingdom> kingdoms = Arrays.asList(Kingdoms.getAllKingdoms());

        byte kingdomId = -1;

        for (Kingdom kingdom : kingdoms) {
            if (discordifyName("GL-"+Kingdoms.getChatNameFor(kingdom.getId())).equals(channel.toLowerCase())) {
                kingdomId = kingdom.getId();
            }
        }
        if (kingdomId != -1) {
            //long wurmId = -10;

            final Message mess = new Message(null, Message.GLOBKINGDOM, "GL-"+Kingdoms.getChatNameFor(kingdomId), "[D] "//"<" + wurmBotName + "> "
                    + message);
            mess.setSenderKingdom(kingdomId);
            if (message.trim().length() > 1) {
                Server.getInstance().addMessage(mess);
                /*final WcKingdomChat wc = new WcKingdomChat(WurmId.getNextWCCommandId(),
                        wurmId, "[D]", message, false, kingdomId,
                        -1,
                        -1,
                        -1);*/
                /*if (Servers.localServer.LOGINSERVER){
                    wc.sendFromLoginServer();
                }*/
                //else
                //wc.sendToLoginServer();
            }
        }
    }

    @Override
    public MessagePolicy onVillageMessage(Village village, Message message) {
        return MessagePolicy.PASS;
    }

    @Override
    public MessagePolicy onAllianceMessage(PvPAlliance alliance, Message message) {
        return MessagePolicy.PASS;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        super.onMessageReceived(event);
        if (event.isFromType(ChannelType.TEXT) && !event.getAuthor().isBot()) {
            String name = event.getTextChannel().getName();
            sendToGlobalKingdomChat(name, "<" + event.getMember().getNickname() + "> " + event.getMessage().getContent());
        }
    }

    private String discordifyName(String name) {
        name = name.toLowerCase();
        if (useUnderscore) {
            return name.replace(" ", "_");
        } else {
            return name.replace(" ", "");
        }
    }

    @Override
    public MessagePolicy onPlayerMessage(Communicator communicator, String message, String title){
        if(title.equals("Trade")){

        }
        return MessagePolicy.PASS;
    }

    @Override
    public boolean onPlayerMessage(Communicator var1, String var2) {
        return false;
    }
}
