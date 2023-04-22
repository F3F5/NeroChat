package me.softik.nerochat.events;

import lombok.RequiredArgsConstructor;
import me.softik.nerochat.NeroChat;
import me.softik.nerochat.api.NeroChatEvent;
import me.softik.nerochat.api.NeroChatReceiveEvent;
import me.softik.nerochat.utils.CommonTool;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

@RequiredArgsConstructor
public class ChatEvent implements Listener {
    private final NeroChat plugin;

    // Mute plugins should have a lower priority to work!
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player chatter = event.getPlayer();
        NeroChatEvent pistonChatEvent = new NeroChatEvent(chatter, event.getMessage(), event.isAsynchronous());

        event.getRecipients().clear();

        Bukkit.getPluginManager().callEvent(pistonChatEvent);

        event.setCancelled(pistonChatEvent.isCancelled());

        if (!pistonChatEvent.isCancelled()) {
            String message = pistonChatEvent.getMessage();

            if (plugin.getTempDataTool().isChatEnabled(chatter)) {
                for (Player receiver : Bukkit.getOnlinePlayers()) {
                    if (!plugin.getIgnoreTool().isIgnored(chatter, receiver) && plugin.getTempDataTool().isChatEnabled(receiver)) {
                        NeroChatReceiveEvent perPlayerEvent = new NeroChatReceiveEvent(chatter, receiver, message);

                        Bukkit.getPluginManager().callEvent(perPlayerEvent);

                        if (perPlayerEvent.isCancelled())
                            continue;

                        message = perPlayerEvent.getMessage();

                        CommonTool.sendChatMessage(chatter, message, receiver);
                    }
                }
            } else {
                chatter.sendMessage(NeroChat.getLang(chatter).chat_is_off);
                event.setCancelled(true);
            }
        }
    }
}
