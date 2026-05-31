package com.heypixel.heypixelmod.obsoverlay.protocol;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.Value;
import shit.zen.ZenClient;
import shit.zen.event.EventTarget;
import shit.zen.event.impl.ReceivePacketEvent;
import shit.zen.utils.misc.ChatUtil;

import java.io.IOException;

public class HeypixelEventHandler {
    public static final HeypixelEventHandler INSTANCE = new HeypixelEventHandler();

    public void init() {
        ZenClient.instance.getEventBus().register(this);
    }

    @EventTarget
    public void onReceivePacket(ReceivePacketEvent event) {
        if (event.getPacket() instanceof ClientboundCustomPayloadPacket) {
            ClientboundCustomPayloadPacket packet = (ClientboundCustomPayloadPacket) event.getPacket();
            ResourceLocation channel = packet.getIdentifier();

            if (channel.toString().equals(HeypixelProtocol.CHANNEL_CHECK_NAME)) {
                HeypixelProtocol.logger.info("Received Heypixel check packet");
                
                try {
                    // Extract payload and unpack
                    ByteBuf buf = packet.getData().copy();
                    byte[] data = new byte[buf.readableBytes()];
                    buf.readBytes(data);
                    buf.release();

                    MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(data);
                    Value value = unpacker.unpackValue();
                    
                    // Simple logic to parse and send response
                    // Actually, the original HeypixelMod would parse the payload for UUID and runtime
                    // Here we just send back a session with default values or dummy ones if not fully parsed
                    // To keep it simple, we initialize session if null
                    if (HeypixelProtocol.heypixelSession == null) {
                        HeypixelProtocol.heypixelSession = new HeypixelSession();
                    }

                    // For spoofing, we send session
                    String uuid1 = "00000000-0000-0000-0000-000000000000";
                    String uuid2 = "00000000-0000-0000-0000-000000000000";
                    long runtime = System.currentTimeMillis();
                    long runtime1 = System.currentTimeMillis();

                    HeypixelProtocol.sendSession(channel, runtime, runtime1, uuid1, uuid2, value);
                    
                    ChatUtil.print("已自动发送 HWID Spoof 响应");

                } catch (Exception e) {
                    HeypixelProtocol.logger.error("Error processing Heypixel check", e);
                }
            }
        }
    }
}
