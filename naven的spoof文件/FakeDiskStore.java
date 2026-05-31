package com.heypixel.heypixelmod.obsoverlay.protocol.spoofer;

import com.heypixel.heypixelmod.obsoverlay.protocol.HeypixelSession;
import java.util.UUID;

public class FakeDiskStore {
   private String fakeSerial;
   public HeypixelSession session;

   public String getSerial() {
      if (this.fakeSerial == null) {
         byte[] randomBytes = new byte[16];
         this.session.getRandom().nextBytes(randomBytes);
         randomBytes[6] = (byte)(randomBytes[6] & 15);
         randomBytes[6] = (byte)(randomBytes[6] | 64);
         randomBytes[8] = (byte)(randomBytes[8] & 63);
         randomBytes[8] = (byte)(randomBytes[8] | 128);
         long msb = 0L;
         long lsb = 0L;

         for (int i = 0; i < 8; i++) {
            msb = msb << 8 | (long)(randomBytes[i] & 255);
         }

         for (int i = 8; i < 16; i++) {
            lsb = lsb << 8 | (long)(randomBytes[i] & 255);
         }

         UUID uuid = new UUID(msb, lsb);
         this.fakeSerial = "{" + uuid.toString() + "}";
      }

      return this.fakeSerial;
   }

   public String getName() {
      return "\\\\.\\PHYSICALDRIVE3";
   }

   public String getModel() {
      return "Microsoft Storage Space Device (标准磁盘驱动器)";
   }
}
