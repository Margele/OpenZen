package com.heypixel.heypixelmod.obsoverlay.protocol;

import com.heypixel.heypixelmod.obsoverlay.protocol.spoofer.FakeBaseboard;
import com.heypixel.heypixelmod.obsoverlay.protocol.spoofer.FakeDiskStore;
import com.heypixel.heypixelmod.obsoverlay.protocol.spoofer.FakeMac;
import java.io.File;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.Map.Entry;
import javax.swing.JOptionPane;
import net.minecraft.client.Minecraft;
import org.msgpack.value.Value;
import org.msgpack.value.ValueFactory;
import org.msgpack.value.Variable;
import oshi.hardware.Baseboard;

public class HeypixelSession {
   private final Variable cpu;
   private final Variable baseboardInfo;
   private final Variable diskStoreInfo;
   private final Variable networkInterfaceInfo;
   private final Variable neteaseEmails;
   private final FakeDiskStore fakeDiskStore;
   private final Baseboard baseboard;
   private final Random random = new Random((long)Minecraft.getInstance().getUser().getName().trim().hashCode());
   public static final String OFFSET3 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz";
   private static final String HEX_CHARS = "0123456789ABCDEF";
   private static final String[] CHINA_TELECOM = new String[]{"133", "149", "153", "173", "177", "180", "181", "189", "199"};

   public HeypixelSession() throws Exception {
      FakeMac.refresh(this);
      YaoMaoFucker.init(this.random);
      this.fakeDiskStore = new FakeDiskStore();
      this.fakeDiskStore.session = this;
      this.baseboard = new FakeBaseboard();
      ((FakeBaseboard)this.baseboard).session = this;
      this.cpu = this.getCPUVar();
      this.baseboardInfo = this.getBaseboardInfoVar();
      this.diskStoreInfo = this.getDiskStoreInfoVar();
      this.networkInterfaceInfo = this.getNetworkInterfaceInfoVar();
      this.neteaseEmails = this.getNeteaseEmailsVar();
   }

   private Variable getCPUVar() {
      String cpu = YaoMaoFucker.getFakeCpu();
      List<Value> values = new ArrayList<>();
      values.add(ValueFactory.newString(this.generateCPUID(cpu)));
      values.add(ValueFactory.newString(cpu));
      values.add(ValueFactory.newString(YaoMaoFucker.getFakeCpuIdf()));
      return new Variable().setArrayValue(values);
   }

   public Variable getDiskStoreInfoVar() {
      List<Value> values = new ArrayList<>();
      List<Value> valueMap = new ArrayList<>();
      valueMap.add(ValueFactory.newString(this.fakeDiskStore.getSerial()));
      valueMap.add(ValueFactory.newString(this.fakeDiskStore.getName()));
      valueMap.add(ValueFactory.newString(this.fakeDiskStore.getModel()));
      values.add(ValueFactory.newArray(valueMap));
      return new Variable().setArrayValue(values);
   }

   public Variable getBaseboardInfoVar() {
      String[] info = YaoMaoFucker.getBaseboardInfo();
      String manufacturer = info[0];
      String model = info[3];
      String serialNumber = info[1];
      String version = info[2];
      List<Value> valueArray = new ArrayList<>();
      valueArray.add(ValueFactory.newString(manufacturer));
      valueArray.add(ValueFactory.newString(model));
      valueArray.add(ValueFactory.newString(serialNumber));
      valueArray.add(ValueFactory.newString("1.0"));
      valueArray.add(ValueFactory.newString(this.genUUID().toString().toUpperCase()));
      return new Variable().setArrayValue(valueArray);
   }

   public Variable getNetworkInterfaceInfoVar() {
      List<Value> arrayList = new ArrayList<>();

      for (Entry<String, String> entry : FakeMac.MACS.entrySet()) {
         List<Value> hashMap = new ArrayList<>();
         hashMap.add(ValueFactory.newString("wlan" + arrayList.size()));
         hashMap.add(ValueFactory.newString(entry.getKey()));
         hashMap.add(ValueFactory.newString(entry.getValue()));
         hashMap.add(ValueFactory.newArray(new ArrayList<>()));
         List<String> strings = List.of(FakeMac.allIpAddresses.get(this.random.nextInt(FakeMac.allIpAddresses.size())));
         hashMap.add(ValueFactory.newString(strings.toString()));
         arrayList.add(ValueFactory.newArray(hashMap));
      }

      return new Variable().setArrayValue(arrayList);
   }

   public Variable getNeteaseEmailsVar() {
      boolean enable = false;

      for (File var5 : Minecraft.getInstance().gameDirectory.getAbsoluteFile().getParentFile().getParentFile().listFiles()) {
         if (var5.isDirectory() && !var5.getName().equals(".minecraft") && var5.getName().contains("@")) {
            enable = true;
         }
      }

      List<Value> list = new ArrayList<>();
      if (enable) {
         try {
            list.add(ValueFactory.newString(Base64.getEncoder().encodeToString((this.createPhoneNumber() + "@163.com").getBytes())));
         } catch (Exception var6) {
            list.add(ValueFactory.newString("ODk2NDMzMzMzMzNAMTYzLmNvbQ=="));
            JOptionPane.showConfirmDialog(null, var6.getMessage());
         }
      }

      return new Variable().setArrayValue(list);
   }

   public String createPhoneNumber() {
      StringBuilder builder = new StringBuilder();
      String mobilePrefix = null;
      mobilePrefix = CHINA_TELECOM[this.random.nextInt(CHINA_TELECOM.length)];
      builder.append(mobilePrefix);

      for (int i = 0; i < 8; i++) {
         int temp = this.random.nextInt(10);
         builder.append(temp);
      }

      return builder.toString();
   }

   public String generateCPUID(String cpu) {
      StringBuilder cpuid = new StringBuilder();
      if (cpu.contains("Intel")) {
         cpuid.append("BFEBFBFF");
      } else {
         for (int i = 0; i < 8; i++) {
            cpuid.append("0123456789ABCDEF".charAt(this.random.nextInt(16)));
         }
      }

      cpuid.append("000");

      for (int i = 0; i < 5; i++) {
         cpuid.append("0123456789ABCDEF".charAt(this.random.nextInt(16)));
      }

      return cpuid.toString();
   }

   public UUID genUUID() {
      long mostSigBits = this.random.nextLong();
      long leastSigBits = this.random.nextLong();
      return new UUID(mostSigBits, leastSigBits);
   }

   @Override
   public String toString() {
      return "HeypixelSession{\n, cpu="
         + this.cpu
         + "\n, baseboardInfo="
         + this.baseboardInfo
         + "\n, diskStoreInfo="
         + this.diskStoreInfo
         + "\n, networkInterfaceInfo="
         + this.networkInterfaceInfo
         + "\n, neteaseEmails="
         + this.neteaseEmails
         + "}";
   }

   public Variable getCpu() {
      return this.cpu;
   }

   public Variable getBaseboardInfo() {
      return this.baseboardInfo;
   }

   public Variable getDiskStoreInfo() {
      return this.diskStoreInfo;
   }

   public Variable getNetworkInterfaceInfo() {
      return this.networkInterfaceInfo;
   }

   public Variable getNeteaseEmails() {
      return this.neteaseEmails;
   }

   public FakeDiskStore getFakeDiskStore() {
      return this.fakeDiskStore;
   }

   public Baseboard getBaseboard() {
      return this.baseboard;
   }

   public Random getRandom() {
      return this.random;
   }
}
