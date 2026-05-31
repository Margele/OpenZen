package com.heypixel.heypixelmod.obsoverlay.protocol.spoofer;

import com.heypixel.heypixelmod.obsoverlay.protocol.HeypixelSession;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class FakeMac {
   private static final Map<String, List<String>> EQUIPMENTS = new HashMap<>();
   private static final List<String> RANDOM_MACS = new ArrayList<>();
   public static final Map<String, String> MACS = new LinkedHashMap<>();
   public static final List<String> allIpAddresses = new ArrayList<>();

   public static void refresh(HeypixelSession session) {
      RANDOM_MACS.clear();
      EQUIPMENTS.clear();
      String v = randomMAC(session);
      RANDOM_MACS.add(v);
      EQUIPMENTS.put(
         v,
         List.of(
            "Intel(R) Wi-Fi 6 AX201 160MHz",
            "Intel(R) Wireless-AC 9560",
            "Realtek RTL8822BE Wireless LAN 802.11ac PCI-E NIC",
            "Realtek PCIe GbE Family Controller",
            "Intel(R) Ethernet Connection (7) I219-V",
            "Realtek Gaming 2.5GbE Family Controller"
         )
      );
      String v2 = randomMAC(session);
      RANDOM_MACS.add(v2);
      EQUIPMENTS.put(
         v2,
         List.of(
            "Intel(R) Ethernet Connection (14) I219-V",
            "Dell Wireless 1820A 802.11ac",
            "MediaTek MT7921 Wi-Fi 6 Adapter",
            "Qualcomm QCA61x4A Wireless Network Adapter",
            "Killer Wi-Fi 6E AX1675x 160MHz Wireless Network Adapter",
            "Broadcom 802.11ac Network Adapter",
            "Realtek PCIe 2.5GbE Family Controller"
         )
      );
      String v3 = randomMAC(session);
      RANDOM_MACS.add(v3);
      EQUIPMENTS.put(
         v3,
         List.of(
            "Intel(R) Dual Band Wireless-AC 8265",
            "Realtek RTL8125 2.5GbE Controller",
            "Killer E3000 2.5 Gigabit Ethernet Controller",
            "Intel(R) Wi-Fi 6E AX210 160MHz"
         )
      );
      MACS.clear();
      String s = randomMAC(session);
      MACS.put("Realtek PCIe GbE Family Controller", s);

      for (int i = 0; i < session.getRandom().nextInt(2, EQUIPMENTS.values().stream().mapToInt(List::size).sum()); i++) {
         addMac(session);
      }

      MACS.put("Intel(R) Wi-Fi 6 AX201 160MHz", randomMAC(session));

      for (int i = 0; i < 20; i++) {
         String ipv4 = generateInternalIPv4(session.getRandom());
         allIpAddresses.add(ipv4);
      }

      for (int i = 0; i < 20; i++) {
         String ipv6 = generateInternalIPv6(session.getRandom());
         allIpAddresses.add(ipv6);
      }
   }

   private static void addMac(HeypixelSession session) {
      int i = session.getRandom().nextInt(0, RANDOM_MACS.size() - 1);
      String randomMac = RANDOM_MACS.get(i);
      List<String> strings = EQUIPMENTS.get(randomMac);
      String s = strings.get(session.getRandom().nextInt(0, strings.size() - 1));
      if (MACS.containsKey(s)) {
         addMac(session);
      } else {
         MACS.put(s, randomMac);
      }
   }

   private static String generateInternalIPv4(Random random) {
      int type = random.nextInt(3);
      byte[] bytes = new byte[4];
      switch (type) {
         case 0:
            bytes[0] = 10;
            random.nextBytes(new byte[]{bytes[1], bytes[2], bytes[3]});
            break;
         case 1:
            bytes[0] = -84;
            bytes[1] = (byte)(16 + random.nextInt(16));
            random.nextBytes(new byte[]{bytes[2], bytes[3]});
            break;
         case 2:
         default:
            bytes[0] = -64;
            bytes[1] = -88;
            bytes[2] = (byte)random.nextInt(256);
            bytes[3] = (byte)(1 + random.nextInt(254));
      }

      try {
         InetAddress address = Inet4Address.getByAddress(bytes);
         return address.getHostAddress();
      } catch (UnknownHostException var4) {
         return "192.168.1.100";
      }
   }

   private static String generateInternalIPv6(Random random) {
      byte[] bytes = new byte[16];
      random.nextBytes(bytes);
      if (random.nextBoolean()) {
         bytes[0] = -3;
      } else {
         bytes[0] = -2;
         bytes[1] = (byte)(bytes[1] & 192 | 128);
      }

      try {
         InetAddress address = Inet6Address.getByAddress(bytes);
         return address.getHostAddress();
      } catch (UnknownHostException var3) {
         return "fd00::1";
      }
   }

   public static String randomMAC(HeypixelSession session) {
      Random rand = session.getRandom();
      byte[] macAddr = new byte[6];
      rand.nextBytes(macAddr);
      macAddr[0] &= -2;
      int vendor = rand.nextInt(5);
      switch (vendor) {
         case 0:
            macAddr[0] = 0;
            macAddr[1] = 31;
            macAddr[2] = 59;
            break;
         case 1:
            macAddr[0] = 0;
            macAddr[1] = -32;
            macAddr[2] = 76;
            break;
         case 2:
            macAddr[0] = 0;
            macAddr[1] = 20;
            macAddr[2] = 34;
            break;
         case 3:
            macAddr[0] = 0;
            macAddr[1] = 26;
            macAddr[2] = -110;
            break;
         case 4:
            macAddr[0] = 0;
            macAddr[1] = 38;
            macAddr[2] = 55;
      }

      StringBuilder sb = new StringBuilder(18);

      for (byte b : macAddr) {
         if (sb.length() > 0) {
            sb.append(":");
         }

         sb.append(String.format("%02x", b));
      }

      return sb.toString();
   }
}
