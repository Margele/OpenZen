package com.heypixel.heypixelmod.obsoverlay.protocol;

import com.heypixel.heypixelmod.obsoverlay.utils.ChatUtils;
import com.heypixel.heypixelmod.obsoverlay.utils.NetworkUtils;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.value.Value;
import org.msgpack.value.Variable;

public class HeypixelProtocol {
   public static final String CHANNEL_CHECK_NAME = "heypixelmod:s2cevent";
   public static Logger logger = LogManager.getLogger("Naven");
   public SimpleChannel channel;
   public static HeypixelSession heypixelSession;
   public static HeypixelProtocol INSTANCE = new HeypixelProtocol();
   public static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2);

   public void onJoinServer(GameProfile profile, ServerData info) {
      try {
         logger.info("Joining server.....");
         heypixelSession = new HeypixelSession();
      } catch (Exception var4) {
         var4.printStackTrace();
         logger.error("Error on joining server");
         ChatUtils.addChatMessage("处理协议时发生致命错误");
      }
   }

   private static MessageBufferPacker getDeafultMessagePack(long runtime1) throws IOException {
      MessageBufferPacker bufferPacker = MessagePack.newDefaultBufferPacker();
      bufferPacker.packLong(runtime1);
      return bufferPacker;
   }

   public static void sendSession(ResourceLocation channelName, long runtime, long runtime1, String uuid1, String uuid2, Value gameSession) throws IOException {
      MessageBufferPacker bufferPacker = getDeafultMessagePack(runtime1);
      ChatUtils.addChatMessage("Random UUID: " + uuid1 + "; Player UUID: " + uuid1);
      bufferPacker.packString(uuid1);
      bufferPacker.packString(uuid2);
      bufferPacker.packValue(new Variable().setIntegerValue(runtime));
      bufferPacker.packValue(new Variable().setIntegerValue(0L));
      bufferPacker.packValue(
         new Variable()
            .setStringValue(
               "[minecraft, saturn, entityculling, mixinextras, netease_official, fastload, geckolib, waveycapes, ferritecore, embeddium_extra, heypixelmod, cloth_config, forge, embeddium, rubidium, oculus]"
            )
      );
      bufferPacker.packValue(new Variable().setStringValue("D:\\MCLDownload\\Game\\.minecraft"));
      bufferPacker.packValue(new Variable().setStringValue("D:\\MCLDownload\\ext\\jre-v64-220420\\jdk17"));
      bufferPacker.packValue(heypixelSession.getCpu());
      bufferPacker.packValue(heypixelSession.getBaseboardInfo());
      bufferPacker.packValue(heypixelSession.getNetworkInterfaceInfo());
      bufferPacker.packValue(heypixelSession.getDiskStoreInfo());
      bufferPacker.packValue(heypixelSession.getNeteaseEmails());
      bufferPacker.packValue(gameSession);
      FriendlyByteBuf friendlyByteBuf = new FriendlyByteBuf(Unpooled.buffer());
      method1403(friendlyByteBuf, 2);
      method1719(friendlyByteBuf, bufferPacker.toByteArray());
      NetworkUtils.sendPacketNoEvent(new ServerboundCustomPayloadPacket(channelName, friendlyByteBuf));
   }

   public static void method1403(ByteBuf byteBuf, int n) {
      method1404(byteBuf, (long)n & 4294967295L);
   }

   public static Object method586(Object object, Object object2) {
      if (object == null) {
         throw new NullPointerException(String.valueOf(object2));
      } else {
         return object;
      }
   }

   public static void method1719(ByteBuf byteBuf, byte[] byArray) {
      method586(byArray, "bytes");
      method1403(byteBuf, byArray.length + 1 + 1 - 1);
      byteBuf.writeBytes(byArray);
   }

   public static void method1404(ByteBuf byteBuf, long l) {
      if ((l & -128L) == 0L) {
         byteBuf.writeByte((byte)((int)l));
      } else if ((l & -16384L) == 0L) {
         int n = (int)((l & 127L | 128L) << 8 | l >>> 7);
         byteBuf.writeShort(n);
      } else {
         method1396(byteBuf, l);
      }
   }

   public static void method1396(ByteBuf byteBuf, long l) {
      if ((l & -128L) == 0L) {
         byteBuf.writeByte((byte)((int)l));
      } else if ((l & -16384L) == 0L) {
         int n = (int)((l & 127L | 128L) << 8 | l >>> 7);
         byteBuf.writeShort(n);
      } else if ((l & -2097152L) == 0L) {
         int n = (int)((l & 127L | 128L) << 16 | (l >>> 7 & 127L | 128L) << 8 | l >>> 14);
         byteBuf.writeMedium(n);
      } else if ((l & -268435456L) == 0L) {
         int n = (int)((l & 127L | 128L) << 24 | (l >>> 7 & 127L | 128L) << 16 | (l >>> 14 & 127L | 128L) << 8 | l >>> 21);
         byteBuf.writeInt(n);
      } else if ((l & -34359738368L) == 0L) {
         int n = (int)((l & 127L | 128L) << 24 | (l >>> 7 & 127L | 128L) << 16 | (l >>> 14 & 127L | 128L) << 8 | l >>> 21 & 127L | 128L);
         byteBuf.writeInt(n);
         byteBuf.writeByte((int)(l >>> 28));
      } else if ((l & -4398046511104L) == 0L) {
         int n = (int)((l & 127L | 128L) << 24 | (l >>> 7 & 127L | 128L) << 16 | (l >>> 14 & 127L | 128L) << 8 | l >>> 21 & 127L | 128L);
         int n2 = (int)((l >>> 28 & 127L | 128L) << 8 | l >>> 35);
         byteBuf.writeInt(n);
         byteBuf.writeShort(n2);
      } else if ((l & -562949953421312L) == 0L) {
         int n = (int)((l & 127L | 128L) << 24 | (l >>> 7 & 127L | 128L) << 16 | (l >>> 14 & 127L | 128L) << 8 | l >>> 21 & 127L | 128L);
         int n3 = (int)((l >>> 28 & 127L | 128L) << 16 | (l >>> 35 & 127L | 128L) << 8 | l >>> 42);
         byteBuf.writeInt(n);
         byteBuf.writeMedium(n3);
      } else if ((l & -72057594037927936L) == 0L) {
         long l2 = (l & 127L | 128L) << 56
            | (l >>> 7 & 127L | 128L) << 48
            | (l >>> 14 & 127L | 128L) << 40
            | (l >>> 21 & 127L | 128L) << 32
            | (l >>> 28 & 127L | 128L) << 24
            | (l >>> 35 & 127L | 128L) << 16
            | (l >>> 42 & 127L | 128L) << 8
            | l >>> 49;
         byteBuf.writeLong(l2);
      } else if ((l & Long.MIN_VALUE) == 0L) {
         long l3 = (l & 127L | 128L) << 56
            | (l >>> 7 & 127L | 128L) << 48
            | (l >>> 14 & 127L | 128L) << 40
            | (l >>> 21 & 127L | 128L) << 32
            | (l >>> 28 & 127L | 128L) << 24
            | (l >>> 35 & 127L | 128L) << 16
            | (l >>> 42 & 127L | 128L) << 8
            | l >>> 49 & 127L
            | 128L;
         byteBuf.writeLong(l3);
         byteBuf.writeByte((byte)((int)(l >>> 56)));
      } else {
         long l4 = (l & 127L | 128L) << 56
            | (l >>> 7 & 127L | 128L) << 48
            | (l >>> 14 & 127L | 128L) << 40
            | (l >>> 21 & 127L | 128L) << 32
            | (l >>> 28 & 127L | 128L) << 24
            | (l >>> 35 & 127L | 128L) << 16
            | (l >>> 42 & 127L | 128L) << 8
            | l >>> 49 & 127L
            | 128L;
         long l5 = (l >>> 56 & 127L | 128L) << 8 | l >>> 63;
         byteBuf.writeLong(l4);
         byteBuf.writeShort((int)l5);
      }
   }
}
