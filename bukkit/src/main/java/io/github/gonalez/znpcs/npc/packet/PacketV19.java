package io.github.gonalez.znpcs.npc.packet;

import com.mojang.authlib.GameProfile;
import io.github.gonalez.znpcs.cache.CacheRegistry;
import org.bukkit.Bukkit;

public class PacketV19 extends PacketV18 {
  public int version() {
    return 19;
  }


  public Object getPlayerPacket(Object nmsWorld, GameProfile gameProfile) throws ReflectiveOperationException {
    try {
      return CacheRegistry.PLAYER_CONSTRUCTOR_NEW_3.load().newInstance(CacheRegistry.GET_SERVER_METHOD
        .load().invoke(Bukkit.getServer()), nmsWorld, gameProfile, CacheRegistry.CREATE_DEFAULT_CLIENT_INFORMATION.load().invoke(null));
    } catch (Throwable e) {
      try {
        return CacheRegistry.PLAYER_CONSTRUCTOR_NEW_1.load().newInstance(CacheRegistry.GET_SERVER_METHOD
          .load().invoke(Bukkit.getServer()), nmsWorld, gameProfile, null);
      } catch (Throwable e2) {
        return CacheRegistry.PLAYER_CONSTRUCTOR_NEW_2.load().newInstance(CacheRegistry.GET_SERVER_METHOD
          .load().invoke(Bukkit.getServer()), nmsWorld, gameProfile);
      }
    }
  }
}
