package com.heypixel.heypixelmod.obsoverlay.protocol.spoofer;

public class FakeCPU {
   private static final String[] MODELS = new String[]{"i9", "i7", "i5", "i3"};
   private static final String[] SUB_MODELS = new String[]{
      "12900K", "12900KF", "12600K", "12600KF", "12400K", "12400KF", "9100F", "8100F", "10400F", "10400H", "11400F", "11700K"
   };
   private static final int[] FREQUENCIES = new int[]{2200, 2400, 2600, 2800, 3000, 3200, 3400, 3600, 3800, 4200, 4400, 4600, 4800};

   public static String generate() {
      return "BFEBFBFF000906EB|Intel(R) Core(TM) i3-9100F CPU @ 3.60GHz";
   }
}
