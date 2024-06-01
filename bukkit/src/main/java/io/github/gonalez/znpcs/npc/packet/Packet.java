package io.github.gonalez.znpcs.npc.packet;

import com.google.common.collect.ImmutableList;
import com.mojang.authlib.GameProfile;
import io.github.gonalez.znpcs.cache.CacheRegistry;
import io.github.gonalez.znpcs.npc.FunctionFactory;
import io.github.gonalez.znpcs.npc.ItemSlot;
import io.github.gonalez.znpcs.npc.NPC;
import io.github.gonalez.znpcs.npc.NPCType;
import io.github.gonalez.znpcs.utility.ReflectionUtils;
import io.github.gonalez.znpcs.utility.Utils;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Collections;

public interface Packet {
  int version();
  
  @PacketValue(keyName = "playerPacket")
  Object getPlayerPacket(Object paramObject, GameProfile paramGameProfile) throws ReflectiveOperationException;
  
  @PacketValue(keyName = "spawnPacket")
  Object getSpawnPacket(Object paramObject, boolean paramBoolean) throws ReflectiveOperationException;
  
  Object convertItemStack(int paramInt, ItemSlot paramItemSlot, ItemStack paramItemStack) throws ReflectiveOperationException;
  
  Object getClickType(Object paramObject) throws ReflectiveOperationException;
  
  Object getMetadataPacket(int paramInt, Object paramObject) throws ReflectiveOperationException;
  
  @PacketValue(keyName = "hologramSpawnPacket", valueType = ValueType.ARGUMENTS)
  Object getHologramSpawnPacket(Object paramObject) throws ReflectiveOperationException;
  
  @PacketValue(keyName = "destroyPacket", valueType = ValueType.ARGUMENTS)
  default Object getDestroyPacket(int entityId) throws ReflectiveOperationException {
    return CacheRegistry.PACKET_PLAY_OUT_ENTITY_DESTROY_CONSTRUCTOR.load().newInstance(
        CacheRegistry.PACKET_PLAY_OUT_ENTITY_DESTROY_CONSTRUCTOR.load().getParameterTypes()[0].isArray() ? new int[]{entityId} : entityId);
  }
  
  @PacketValue(keyName = "enumSlot", valueType = ValueType.ARGUMENTS)
  default Object getItemSlot(int slot) {
    return CacheRegistry.ENUM_ITEM_SLOT.getEnumConstants()[slot];
  }
  
  @PacketValue(keyName = "removeTab")
  default Object getTabRemovePacket(Object nmsEntity) throws ReflectiveOperationException {
    try {
      return CacheRegistry.PACKET_PLAY_OUT_PLAYER_INFO_CONSTRUCTOR.load().newInstance(CacheRegistry.REMOVE_PLAYER_FIELD
              .load(),
          Collections.singletonList(nmsEntity));
    } catch (Throwable throwable) {
      boolean useOldMethod = CacheRegistry.PACKET_PLAY_OUT_PLAYER_INFO_REMOVE_CLASS != null;
      if (useOldMethod) {
        return CacheRegistry.PACKET_PLAY_OUT_PLAYER_INFO_REMOVE_CONSTRUCTOR.load()
            .newInstance(Collections.singletonList(CacheRegistry.GET_UNIQUE_ID_METHOD.load().invoke(nmsEntity)));
      } else {
        return CacheRegistry.PACKET_PLAY_OUT_PLAYER_INFO_CONSTRUCTOR.load().newInstance(CacheRegistry.REMOVE_PLAYER_FIELD
                .load(),
            nmsEntity);
      }
    }
  }
  
  @PacketValue(keyName = "equipPackets")
  ImmutableList<Object> getEquipPackets(NPC paramNPC) throws ReflectiveOperationException;
  
  @PacketValue(keyName = "scoreboardPackets")
  default ImmutableList<Object> updateScoreboard(NPC npc) throws ReflectiveOperationException {
    ImmutableList.Builder<Object> builder = ImmutableList.builder();
    boolean isVersion17 = Utils.isVersionNew(17);
    boolean isVersion9 = Utils.isVersionNew(9);
    Object scoreboardTeamPacket = isVersion17 ? CacheRegistry.SCOREBOARD_TEAM_CONSTRUCTOR.load().newInstance(null, npc.getGameProfile().getName()) : CacheRegistry.PACKET_PLAY_OUT_SCOREBOARD_TEAM_CONSTRUCTOR_OLD.load().newInstance();
    if (!isVersion17) {
      Utils.setValue(scoreboardTeamPacket, "a", npc.getGameProfile().getName());
      Utils.setValue(scoreboardTeamPacket, isVersion9 ? "i" : "h", 1);
    } 
    builder.add(isVersion17 ? CacheRegistry.PACKET_PLAY_OUT_SCOREBOARD_TEAM_CREATE_V1.load().invoke(null, scoreboardTeamPacket) : scoreboardTeamPacket);
    if (isVersion17) {
      scoreboardTeamPacket = CacheRegistry.SCOREBOARD_TEAM_CONSTRUCTOR.load().newInstance(null, npc.getGameProfile().getName());
      if (Utils.isVersionNew(18)) {
        Utils.setValue(scoreboardTeamPacket, "d", npc.getGameProfile().getName());
        ReflectionUtils.findFieldForClassAndSet(scoreboardTeamPacket, CacheRegistry.ENUM_TAG_VISIBILITY, CacheRegistry.ENUM_TAG_VISIBILITY_NEVER_FIELD.load());
        try {
          Utils.setValue(scoreboardTeamPacket, "m", CacheRegistry.ENUM_CHAT_FORMAT_FIND.load().invoke(null, "DARK_GRAY"));
        } catch (Throwable exception) {
          Utils.setValue(scoreboardTeamPacket, "m", CacheRegistry.ENUM_CHAT_FORMAT_FIND.load().invoke(null, "darkgray"));
        }
      } else {
        Utils.setValue(scoreboardTeamPacket, "e", npc.getGameProfile().getName());
        Utils.setValue(scoreboardTeamPacket, "l", CacheRegistry.ENUM_TAG_VISIBILITY_NEVER_FIELD.load());
      } 
    } else {
      scoreboardTeamPacket = CacheRegistry.PACKET_PLAY_OUT_SCOREBOARD_TEAM_CONSTRUCTOR_OLD.load().newInstance();
      Utils.setValue(scoreboardTeamPacket, "a", npc.getGameProfile().getName());
      Utils.setValue(scoreboardTeamPacket, "e", "never");
      Utils.setValue(scoreboardTeamPacket, isVersion9 ? "i" : "h", 0);
    }
    Collection<String> collection = (Collection<String>) (isVersion17 ?
        CacheRegistry.SCOREBOARD_PLAYER_LIST.load().invoke(scoreboardTeamPacket) : Utils.getValue(scoreboardTeamPacket, isVersion9 ? "h" : "g"));
    if (npc.getNpcPojo().getNpcType() == NPCType.PLAYER) {
      collection.add(npc.getGameProfile().getName());
    } else {
      collection.add(npc.getUUID().toString());
    } 
    if (allowGlowColor() && FunctionFactory.isTrue(npc, "glow"))
      updateGlowPacket(npc, scoreboardTeamPacket); 
    builder.add(isVersion17 ? CacheRegistry.PACKET_PLAY_OUT_SCOREBOARD_TEAM_CREATE.load().invoke(null, scoreboardTeamPacket, Boolean.TRUE) : scoreboardTeamPacket);
    return builder.build();
  }
  
  void updateGlowPacket(NPC paramNPC, Object paramObject) throws ReflectiveOperationException;
  
  boolean allowGlowColor();
  
  default void update(PacketCache packetCache) throws ReflectiveOperationException {
    packetCache.flushCache("scoreboardPackets");
  }
}
