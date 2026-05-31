package com.heypixel.heypixelmod.obsoverlay.protocol.spoofer;

import com.heypixel.heypixelmod.obsoverlay.protocol.HeypixelSession;
import org.apache.commons.lang3.RandomStringUtils;
import oshi.hardware.common.AbstractBaseboard;

public class FakeBaseboard extends AbstractBaseboard {
   private String fakeSerial;
   public HeypixelSession session;

   public String getManufacturer() {
      return "LENOVO";
   }

   public String getModel() {
      return "unknown";
   }

   public String getVersion() {
      return "SDK0T" + this.session.getRandom().nextDouble(760.0, 820.0) * 100.0 + " WIN";
   }

   public String getSerialNumber() {
      if (this.fakeSerial == null) {
         this.fakeSerial = RandomStringUtils.random(8, 0, 0, true, true, null, this.session.getRandom()).toUpperCase();
      }

      return this.fakeSerial;
   }
}
