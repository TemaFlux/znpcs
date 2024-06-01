package io.github.gonalez.znpcs.npc.packet;

import com.google.common.collect.ImmutableSet;
import io.github.gonalez.znpcs.utility.Utils;

import java.util.Comparator;

public final class PacketFactory {
  public static final ImmutableSet<Packet> ALL = ImmutableSet.of(new PacketV8(), new PacketV9(),
      new PacketV16(), new PacketV17(), new PacketV18(), new PacketV19());
  
  public static final Packet PACKET_FOR_CURRENT_VERSION = findPacketForVersion(Utils.BUKKIT_VERSION);
  
  public static Packet findPacketForVersion(double version) {
    return ALL.stream()
      .filter(packet -> (version >= packet.version()))
      .max(Comparator.comparing(Packet::version))
      .orElseThrow(() -> new IllegalArgumentException("No packet instance found for version: " + version));
  }
}
