package shit.zen.patch;

import asm.patchify.annotation.At;
import asm.patchify.annotation.Inject;
import asm.patchify.annotation.Patch;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.util.thread.BlockableEventLoop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shit.zen.ZenClient;
import shit.zen.network.PacketHandlerUtil;

@Patch(PacketUtils.class)
public class PacketUtilsPatch {
    private static final Logger LOGGER = LoggerFactory.getLogger(PacketUtils.class);

    @Inject(
            method = "ensureRunningOnSameThread",
            desc = "(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/util/thread/BlockableEventLoop;)V",
            at = @At(At.Type.HEAD)
    )
    public static <T extends PacketListener> void onEnsureRunningOnSameThread(Packet<T> packet, T listener, BlockableEventLoop<?> loop, CallbackInfo callbackInfo) throws RunningOnDifferentThreadException {
        if (ZenClient.isReady()) {
            // Bypass vanilla filter injection / forge network pipeline packets that cause duplicate handler exception
            // We should let ClientboundLoginPacket and Dimension/Respawn packets pass through normally to Forge
            String packetName = packet.getClass().getSimpleName();
            if (packetName.equals("ClientboundLoginPacket") || 
                packetName.equals("ClientboundRespawnPacket") || 
                packetName.equals("ClientboundChangeDifficultyPacket")) {
                return; // Let the original method handle these packets synchronously
            }
            
            callbackInfo.cancel();
            PacketHandlerUtil.processPacket(LOGGER, packet, listener, loop);
        }
    }
}
