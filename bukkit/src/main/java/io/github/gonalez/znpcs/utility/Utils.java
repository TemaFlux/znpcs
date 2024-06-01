package io.github.gonalez.znpcs.utility;

import io.github.gonalez.znpcs.cache.CacheRegistry;
import io.github.gonalez.znpcs.configuration.ConfigurationConstants;
import io.github.gonalez.znpcs.user.ZUser;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Utils {
  public static final double BUKKIT_VERSION;

  private static final Pattern PATTERN = Pattern.compile("(?i)v(\\d+)_(\\d+)_R(\\d+)");
  
  public static boolean PLACEHOLDER_SUPPORT = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
  
  static {
    BUKKIT_VERSION = getVersionAsDouble(Utils.getBukkitPackage());
  }

  private static double getVersionAsDouble(String input) {
    Matcher matcher = PATTERN.matcher(input);

    if (matcher.find()) {
      // String majorVersion = matcher.group(1);
      String minorVersion = matcher.group(2);
      String revisionNumber = matcher.group(3);
      try {
        return Double.parseDouble(minorVersion + "." + revisionNumber);
      } catch (NumberFormatException e) {
        return 0.0;
      }
    }

    return 0.0;
  }
  
  public static boolean isVersionNew(double version) {
    return (BUKKIT_VERSION >= version);
  }
  
  public static String getBukkitPackage() {
    return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
  }
  
  public static String toColor(String string) {
    return ChatColor.translateAlternateColorCodes('&', string);
  }
  
  public static String getWithPlaceholders(String string, Player player) {
    return PlaceholderAPI.setPlaceholders(player, string).replace(ConfigurationConstants.SPACE_SYMBOL, " ");
  }
  
  public static String randomString(int length) {
    StringBuilder stringBuilder = new StringBuilder();
    for (int index = 0; index < length; index++)
      stringBuilder.append(ThreadLocalRandom.current().nextInt(0, 9)); 
    return stringBuilder.toString();
  }
  
  public static void sendTitle(Player player, String title, String subTitle) {
    player.sendTitle(toColor(title), toColor(subTitle));
  }
  
  public static void setValue(Object fieldInstance, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
    Field f = fieldInstance.getClass().getDeclaredField(fieldName);
    f.setAccessible(true);
    f.set(fieldInstance, value);
  }

  public static void setValue(
      Object fieldInstance, Object value, Class<?> expectedType)
      throws NoSuchFieldException, IllegalAccessException {
    for (Field field : fieldInstance.getClass().getDeclaredFields()) {
      if (field.getType() == expectedType)
        setValue(fieldInstance, field.getName(), value);
    }
  }

  public static Object getValue(Object instance, String fieldName) throws NoSuchFieldException, IllegalAccessException {
    Field f = instance.getClass().getDeclaredField(fieldName);
    f.setAccessible(true);
    return f.get(instance);
  }
  
  public static void sendPackets(ZUser user, Object... packets) {
    try {
      for (Object packet : packets) {
        if (packet != null)
          CacheRegistry.SEND_PACKET_METHOD.load().invoke(user.getPlayerConnection(), packet);
      } 
    } catch (IllegalAccessException|java.lang.reflect.InvocationTargetException e) {
      e.printStackTrace();
    } 
  }
}
