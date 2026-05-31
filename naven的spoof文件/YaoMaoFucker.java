package com.heypixel.heypixelmod.obsoverlay.protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YaoMaoFucker {
   private static final String[] CORE_I9 = new String[]{
      "i9-11900K",
      "i9-10900K",
      "i9-12900K",
      "i9-11900KF",
      "i9-10900KF",
      "i9-11950H",
      "i9-12950H",
      "i9-12900XE",
      "i9-10980XE",
      "i9-10940X",
      "i9-10920X",
      "i9-10900X",
      "i9-7980XE",
      "i9-7960X",
      "i9-7940X",
      "i9-7920X",
      "i9-9900KS",
      "i9-9900K",
      "i9-9900KF",
      "i9-9900",
      "i9-9900T",
      "i9-9900B"
   };
   private static final String[] CORE_I7 = new String[]{
      "i7-11700K",
      "i7-10700K",
      "i7-12700K",
      "i7-10700F",
      "i7-9700K",
      "i7-9700KF",
      "i7-9700",
      "i7-9700F",
      "i7-10700",
      "i7-9700T",
      "i7-8700K",
      "i7-8700",
      "i7-8700T",
      "i7-10750H",
      "i7-10850H",
      "i7-10870H",
      "i7-10875H",
      "i7-10810U",
      "i7-10710U",
      "i7-10610U",
      "i7-10750U",
      "i7-10600G7",
      "i7-1060G7"
   };
   private static final String[] CORE_I5 = new String[]{
      "i5-11600K",
      "i5-10600K",
      "i5-12600K",
      "i5-9600K",
      "i5-9600KF",
      "i5-10400",
      "i5-10400F",
      "i5-9500",
      "i5-9500F",
      "i5-9500T",
      "i5-11400F",
      "i5-11400",
      "i5-11400H",
      "i5-1135G7",
      "i5-11300H",
      "i5-10300H",
      "i5-1035G1",
      "i5-10210U",
      "i5-10200H",
      "i5-10210Y",
      "i5-9400",
      "i5-9400F",
      "i5-9400T"
   };
   private static final String[] CORE_I3 = new String[]{
      "i3-11100",
      "i3-10100",
      "i3-12100F",
      "i3-9100",
      "i3-9100F",
      "i3-9300",
      "i3-9300T",
      "i3-8100",
      "i3-8100T",
      "i3-10100F",
      "i3-10100T",
      "i3-10100E",
      "i3-10100H",
      "i3-1005G1",
      "i3-1005G4",
      "i3-10110U",
      "i3-1000NG4",
      "i3-1000NG4",
      "i3-1000NG4",
      "i3-1000NG4",
      "i3-9350KF",
      "i3-9350K",
      "i3-9100T"
   };
   private static final String[] MANUFACTURER = new String[]{
      "Alienware", "Micro-Star International Co., Ltd.", "COLORFUL", "HUAWEI", "ASUS", "Gigabyte", "Not Applicable", "HASEE"
   };
   private static final String[] VERSIONS = new String[]{"P??I", "A??", "B??", "C??", "m??", "R??", "Not Applicable"};
   private static final String[] MODEL = new String[]{"x64-based-pc", "unknown"};
   private static final String[] HD = new String[]{
      "NVMe PM??? NVMe Samsung XXXX", "Seagate FireCuda ??? SSD ZPXXXXGMXXXX", "WD PC SN??? SDDPNPF-1T00", "NVMe P???PL NVMe SOLIDIGM"
   };
   private static final String[] CPUIDS = new String[]{
      "Intel64 Family 6 Model 183 Stepping 1",
      "Intel64 Family 6 Model 158 Stepping 10",
      "Intel64 Family 6 Model 158 Stepping 11",
      "Intel64 Family 6 Model 154 Stepping 3",
      "Intel64 Family 6 Model 151 Stepping 2",
      "Intel64 Family 6 Model 183 Stepping 1",
      "Intel64 Family 6 Model 158 Stepping 10",
      "Intel64 Family 6 Model 140 Stepping 2",
      "Intel64 Family 6 Model 42 Stepping 7"
   };
   private static final HashSet<String> NETWORK_INTERFACES = new HashSet<>();
   private static String name;
   private static String cpuId;
   private static String manufacturer;
   private static String version;
   private static String bbid;
   private static String model;
   private static String hardDisk;
   private static String hdId;
   private static NetworkInterface networkInterface;
   private static final List<Object> fakeInterfaces = new ArrayList<>();
   private static Random random;
   private static Object fakeDisk;

   public static String[] getBaseboardInfo() {
      return new String[]{manufacturer, bbid, version, model};
   }

   public static void init(Random random1) throws Exception {
      try {
         random = random1;
         String ni = "Famatech Radmin VPN Ethernet Adapter-WFP Native MAC Layer LightWeight Filter-0000\nFamatech Radmin VPN Ethernet Adapter-Kaspersky Lab NDIS 6 Filter-0000\nFamatech Radmin VPN Ethernet Adapter-QoS Packet Scheduler-0000\nFamatech Radmin VPN Ethernet Adapter-WFP 802.3 MAC Layer LightWeight Filter-0000\nRealtek PCIe GbE Family Controller-WFP Native MAC Layer LightWeight Filter-0000\nRealtek PCIe GbE Family Controller-Kaspersky Lab NDIS 6 Filter-0000\nRealtek PCIe GbE Family Controller-QoS Packet Scheduler-0000\nRealtek PCIe GbE Family Controller-WFP 802.3 MAC Layer LightWeight Filter-0000\nHyper-V Virtual Ethernet Adapter-WFP Native MAC Layer LightWeight Filter-0000\nHyper-V Virtual Ethernet Adapter-Kaspersky Lab NDIS 6 Filter-0000\nHyper-V Virtual Ethernet Adapter-QoS Packet Scheduler-0000\nHyper-V Virtual Ethernet Adapter-WFP 802.3 MAC Layer LightWeight Filter-0000\nRealtek PCIe GbE Family Controller\nFamatech Radmin VPN Ethernet Adapter\nHyper-V Virtual Ethernet Adapter\nTAP-Windows Adapter V9-WFP Native MAC Layer LightWeight Filter-0000\nTAP-Windows Adapter V9-Kaspersky Lab NDIS 6 Filter-0000\nTAP-Windows Adapter V9-QoS Packet Scheduler-0000\nTAP-Windows Adapter V9-WFP 802.3 MAC Layer LightWeight Filter-0000\nTAP-Windows Adapter V9\nIntel(R) Wi-Fi 6E AX211 160MHz-WFP Native MAC Layer LightWeight Filter-0000\nIntel(R) Wi-Fi 6E AX211 160MHz-Virtual WiFi Filter Driver-0000\nIntel(R) Wi-Fi 6E AX211 160MHz-Native WiFi Filter Driver-0000\nIntel(R) Wi-Fi 6E AX211 160MHz-Kaspersky Lab NDIS 6 Filter-0000\nIntel(R) Wi-Fi 6E AX211 160MHz-QoS Packet Scheduler-0000\nIntel(R) Wi-Fi 6E AX211 160MHz-WFP 802.3 MAC Layer LightWeight Filter-0000\nMicrosoft Wi-Fi Direct Virtual Adapter-WFP Native MAC Layer LightWeight Filter-0000\nMicrosoft Wi-Fi Direct Virtual Adapter-Native WiFi Filter Driver-0000\nMicrosoft Wi-Fi Direct Virtual Adapter-Kaspersky Lab NDIS 6 Filter-0000\nMicrosoft Wi-Fi Direct Virtual Adapter-QoS Packet Scheduler-0000\nMicrosoft Wi-Fi Direct Virtual Adapter-WFP 802.3 MAC Layer LightWeight Filter-0000\nIntel(R) Wi-Fi 6E AX211 160MHz\nMicrosoft Wi-Fi Direct Virtual Adapter\nMicrosoft Teredo Tunneling Adapter\nVMware Virtual Ethernet Adapter for VMnet1\nRealtek PCIe 2.5GbE Family Controller\nVMware Virtual Ethernet Adapter for VMnet8\nTAP-Windows Adapter V9\nFamatech Radmin VPN Ethernet Adapter\nMicrosoft Wi-Fi Direct Virtual Adapter\nIntel(R) Wi-Fi 6 AX200 160MHz\nTAP-Windows Adapter V9\nVMware Virtual Ethernet Adapter for VMnet8\nKiller E3100G 2.5 Gigabit Ethernet Controller\nVMware Virtual Ethernet Adapter for VMnet1\nKiller(R) Wi-Fi 6E AX1675i 160MHz Wireless Network Adapter (211NGW)\nMicrosoft Wi-Fi Direct Virtual Adapter\nBluetooth Device (Personal Area Network)";
         NETWORK_INTERFACES.addAll(Arrays.asList(ni.split("\n")));
         cpuId = getRealId();
         name = getFakeCpu();

         for (String s : generateRealAddr()) {
            fakeInterfaces.add(makeFake(getFakeName(), s));
         }

         int randomVersion = random.nextInt(90) + 10;
         int randomVersion2 = random.nextInt(900) + 100;
         int randomVersion3 = random.nextInt(9000) + 1000;
         bbid = getRealBaseboard().trim();
         manufacturer = MANUFACTURER[random.nextInt(MANUFACTURER.length)];
         model = MODEL[random.nextInt(MODEL.length)];
         version = VERSIONS[random.nextInt(VERSIONS.length)].replace("??", String.valueOf(randomVersion));
         hardDisk = HD[random.nextInt(HD.length)].replace("???", String.valueOf(randomVersion2)).replace("XXXX", String.valueOf(randomVersion3)) + " (标准磁盘驱动器)";
         hdId = getRealHD();
         fakeDisk = makeDisk();
      } catch (Throwable var5) {
         throw var5;
      }
   }

   public static Object getFakeDisk() {
      return fakeDisk;
   }

   private static Object makeDisk() throws Exception {
      try {
         Class<?> clazz = Class.forName("oshi.hardware.platform.windows.WindowsHWDiskStore");
         Constructor<?> constructor = clazz.getDeclaredConstructor(String.class, String.class, String.class, long.class);
         constructor.setAccessible(true);
         return constructor.newInstance("\\\\.\\PHYSICALDRIVE0", hardDisk, getRealHD(), 3918074L);
      } catch (Throwable var2) {
         throw var2;
      }
   }

   public static List<Object> getFakeInterfaces() {
      return fakeInterfaces;
   }

   public static String getRealHD() throws Exception {
      Process process = Runtime.getRuntime().exec(new String[]{"wmic", "diskdrive", "get", "serialnumber"});
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

      String string;
      while ((string = bufferedReader.readLine()) != null) {
         if (!string.trim().toLowerCase(Locale.ROOT).equals("serialnumber")) {
            return string.trim();
         }
      }

      bufferedReader.close();
      return "";
   }

   private static String getFakeName() {
      List<String> list = new ArrayList<>(NETWORK_INTERFACES);
      String peek = list.get(random.nextInt(list.size()));
      int randomNumber = random.nextInt(9000) + 1000;
      return peek.replace("0000", String.valueOf(randomNumber));
   }

   public static Object makeFake(String displayName, String macAddr) throws NoSuchFieldException, ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
      try {
         Class<?> clazz = Class.forName("oshi.hardware.platform.windows.WindowsNetworkIF");
         Object obj = clazz.getConstructor(NetworkInterface.class).newInstance(networkInterface);
         Field name = Class.forName("oshi.hardware.common.AbstractNetworkIF").getDeclaredField("displayName");
         Field mac = Class.forName("oshi.hardware.common.AbstractNetworkIF").getDeclaredField("mac");
         name.setAccessible(true);
         mac.setAccessible(true);
         name.set(obj, displayName);
         mac.set(obj, macAddr);
         return obj;
      } catch (Throwable var6) {
         throw var6;
      }
   }

   public static List<String> generateRealAddr() throws Exception {
      Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
      List<String> address = new ArrayList<>();

      while (enumeration.hasMoreElements()) {
         NetworkInterface networkInterface = enumeration.nextElement();
         YaoMaoFucker.networkInterface = networkInterface;
         byte[] byArray = networkInterface.getHardwareAddress();
         if (byArray != null) {
            StringBuilder stringBuilder = new StringBuilder();

            for (int i = 0; i < byArray.length; i++) {
               stringBuilder.append(String.format("%02X%s", byArray[i], i < byArray.length - 1 ? ":" : ""));
            }

            address.add(stringBuilder.toString().toLowerCase());
         }
      }

      return address;
   }

   public static String getRealBaseboard() throws IOException {
      try {
         StringBuilder stringBuilder = new StringBuilder();
         Process process = Runtime.getRuntime().exec("wmic baseboard get SerialNumber");
         BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

         String string;
         while ((string = bufferedReader.readLine()) != null) {
            if (!string.contains("SerialNumber")) {
               stringBuilder.append(string);
            }
         }

         bufferedReader.close();
         return stringBuilder.toString();
      } catch (Throwable var4) {
         throw var4;
      }
   }

   public static String getFakeCpu() {
      int seriesIndex = random.nextInt(4);
      switch (seriesIndex) {
         case 0: {
            int modelIndex = random.nextInt(CORE_I9.length);
            return formatCore(CORE_I9[modelIndex]);
         }
         case 1: {
            int modelIndex = random.nextInt(CORE_I7.length);
            return formatCore(CORE_I7[modelIndex]);
         }
         case 2: {
            int modelIndex = random.nextInt(CORE_I5.length);
            return formatCore(CORE_I5[modelIndex]);
         }
         case 3: {
            int modelIndex = random.nextInt(CORE_I3.length);
            return formatCore(CORE_I3[modelIndex]);
         }
         default:
            return "";
      }
   }

   public static String getFakeCpuIdf() {
      int modelIndex = random.nextInt(CPUIDS.length);
      return CPUIDS[modelIndex];
   }

   public static String formatCore(String abbreviation) {
      String[] parts = abbreviation.split("-");
      String generation = getGeneration(parts[0]);
      String model = getModel(parts[0]);
      return findGeneration(abbreviation) + "th Gen Intel(R) Core(TM) " + model + generation + "-" + parts[1];
   }

   public static String findGeneration(String cpuModel) {
      String regex = "i[0-9]+-(\\d+)";
      Pattern pattern = Pattern.compile(regex);
      Matcher matcher = pattern.matcher(cpuModel);
      if (matcher.find()) {
         int gen = Integer.parseInt(matcher.group(1).substring(0, 2));
         return gen > 15 ? String.valueOf(gen).substring(0, 1) : String.valueOf(gen);
      } else {
         return "1";
      }
   }

   public static String getGeneration(String abbreviation) {
      String pattern = "[A-Za-z]+(\\d+)";
      Pattern r = Pattern.compile(pattern);
      Matcher m = r.matcher(abbreviation);
      if (m.find()) {
         int gen = Integer.parseInt(m.group(1));
         return String.valueOf(gen);
      } else {
         return "Unknown";
      }
   }

   public static String getModel(String abbreviation) {
      String pattern = "([A-Za-z]+)\\d+";
      Pattern r = Pattern.compile(pattern);
      Matcher m = r.matcher(abbreviation);
      return m.find() ? m.group(1) : "Unknown";
   }

   public static String getRealId() {
      StringBuilder stringBuilder = new StringBuilder();

      try {
         Process process = Runtime.getRuntime().exec("wmic cpu get ProcessorId");
         BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

         String string;
         while ((string = bufferedReader.readLine()) != null) {
            if (!string.contains("ProcessorId")) {
               stringBuilder.append(string);
            }
         }

         bufferedReader.close();
      } catch (Exception var4) {
         return "";
      }

      return stringBuilder.toString().trim();
   }

   public static String[] cpu() {
      return new String[]{name, cpuId};
   }
}
