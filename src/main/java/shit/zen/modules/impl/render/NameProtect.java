package shit.zen.modules.impl.render;

import java.util.ArrayList;
import java.util.Random;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.StringUtils;
import shit.zen.ZenClient;
import shit.zen.event.impl.ChatReceiveEvent;
import shit.zen.event.impl.DisconnectEvent;
import shit.zen.modules.Category;
import shit.zen.modules.Module;
import shit.zen.settings.impl.ModeSetting;
import shit.zen.event.EventTarget;

public class NameProtect
extends Module {
    public static NameProtect INSTANCE;
    private final ModeSetting modeSetting = new ModeSetting("Mode", "Fixed", "Random").withDefault("Fixed");
    private String cachedRandomName = null;
    private final Random random = new Random();

    public NameProtect() {
        super("NameProtect", Category.RENDER);
        INSTANCE = this;
    }

    @EventTarget
    public void onDisconnect(DisconnectEvent disconnectEvent) {
        if (this.modeSetting.is("Random")) {
            this.cachedRandomName = null;
        }
    }

    public static String replacePlayerName(String string) {
        if (INSTANCE == null || !INSTANCE.isEnabled()) {
            return string;
        }
        if (mc.player == null) {
            return string;
        }
        String string2 = mc.player.getName().getString();
        String string3 = INSTANCE.generateRandomName();
        if (string3 != null && !string3.equals(string2) && string.contains(string2)) {
            return StringUtils.replace(string, string2, string3);
        }
        return string;
    }

    public static String getProtectedName() {
        if (mc.player == null) {
            return "Player";
        }
        if (INSTANCE == null || !INSTANCE.isEnabled()) {
            return mc.player.getName().getString();
        }
        String string = mc.player.getName().getString();
        String string2 = INSTANCE.generateRandomName();
        if (string2 != null && !string2.equals(string)) {
            return string2;
        }
        return string;
    }

    private String generateRandomName() {
        if (mc.getConnection() == null) {
            return null;
        }
        ArrayList<PlayerInfo> arrayList = new ArrayList<>(mc.getConnection().getOnlinePlayers());
        ArrayList<String> arrayList2 = new ArrayList<>();
        String string = mc.player.getName().getString();
        for (PlayerInfo playerInfo : arrayList) {
            String string2 = playerInfo.getProfile().getName();
            if (string2.equals(string)) continue;
            arrayList2.add(string2);
        }
        if (arrayList2.isEmpty()) {
            return null;
        }
        if (this.cachedRandomName == null || !arrayList2.contains(this.cachedRandomName)) {
            this.cachedRandomName = arrayList2.get(this.random.nextInt(arrayList2.size()));
        }
        return this.cachedRandomName;
    }

    @EventTarget
    public void onChatReceive(ChatReceiveEvent chatReceiveEvent) {
        chatReceiveEvent.setComponent(Component.literal(NameProtect.replacePlayerName(chatReceiveEvent.getComponent().getString())));
    }
}