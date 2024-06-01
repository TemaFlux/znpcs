package io.github.gonalez.znpcs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.gonalez.znpcs.commands.Command;
import io.github.gonalez.znpcs.commands.list.DefaultCommand;
import io.github.gonalez.znpcs.configuration.Configuration;
import io.github.gonalez.znpcs.configuration.ConfigurationConstants;
import io.github.gonalez.znpcs.listeners.InventoryListener;
import io.github.gonalez.znpcs.listeners.PlayerListener;
import io.github.gonalez.znpcs.npc.NPC;
import io.github.gonalez.znpcs.npc.NPCModel;
import io.github.gonalez.znpcs.npc.NPCPath;
import io.github.gonalez.znpcs.npc.NPCType;
import io.github.gonalez.znpcs.npc.task.NPCManagerTask;
import io.github.gonalez.znpcs.npc.task.NPCSaveTask;
import io.github.gonalez.znpcs.npc.task.NpcRefreshSkinTask;
import io.github.gonalez.znpcs.user.ZUser;
import io.github.gonalez.znpcs.utility.BungeeUtils;
import io.github.gonalez.znpcs.utility.MetricsLite;
import io.github.gonalez.znpcs.utility.SchedulerUtils;
import io.github.gonalez.znpcs.utility.itemstack.ItemStackSerializer;
import io.github.gonalez.znpcs.utility.location.ZLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;

public class ServersNPC extends JavaPlugin {
  public static final String PATH_EXTENSION = ".path";

  public static final File PLUGIN_FOLDER = new File("plugins/ServersNPC");

  public static final Gson GSON =
      (new GsonBuilder())
          .registerTypeAdapter(ZLocation.class, ZLocation.SERIALIZER)
          .registerTypeHierarchyAdapter(ItemStack.class, new ItemStackSerializer())
          .setPrettyPrinting()
          .disableHtmlEscaping()
          .create();

  public static SchedulerUtils SCHEDULER;

  public static BungeeUtils BUNGEE_UTILS;

  @Override public void onEnable() {
    Path pluginPath = getDataFolder().toPath();
    Path pathPath = pluginPath.resolve("paths");

    try {
      loadAllPaths(pathPath);
    } catch (IOException e) {
      getLogger().log(Level.WARNING, "Could not load paths", e);
    }

    getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    new MetricsLite(this, 8054);
    new DefaultCommand(pathPath);
    SCHEDULER = new SchedulerUtils(this);
    BUNGEE_UTILS = new BungeeUtils(this);
    Bukkit.getOnlinePlayers().forEach(ZUser::find);
    new NPCManagerTask(this);
    new NPCSaveTask(this, ConfigurationConstants.SAVE_DELAY);
    new NpcRefreshSkinTask().runTaskTimerAsynchronously(this, 0L, 20L);
    new PlayerListener(this);
    new InventoryListener(this);
  }

  @Override public void onDisable() {
    Command.unregisterAll();
    Configuration.SAVE_CONFIGURATIONS.forEach(Configuration::save);
    Bukkit.getOnlinePlayers().forEach(ZUser::unregister);
  }

  /**
   * Finds all files eligible to be a npc path, which are the ones whose names end with
   * {@link #PATH_EXTENSION}, reads the file to a npc path and initializes it.
   */
  private void loadAllPaths(Path directory) throws IOException {
    if (Files.isDirectory(directory)) {
      Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
          if (!Files.isDirectory(file)
              && file.getFileName().toString().endsWith(PATH_EXTENSION)) {
            loadPath(file.toFile());
          }
          return FileVisitResult.CONTINUE;
        }

        void loadPath(File file) {
          NPCPath.AbstractTypeWriter abstractTypeWriter =
              NPCPath.AbstractTypeWriter.forFile(
                  file, NPCPath.AbstractTypeWriter.TypeWriter.MOVEMENT);
          abstractTypeWriter.load();
        }
      });
    }
    Files.createDirectories(directory);
  }

  public static NPC createNPC(int id, NPCType npcType, Location location, String name) {
    NPC find = NPC.find(id);
    if (find != null) return find;
    NPCModel pojo =
        (new NPCModel(id))
            .withHologramLines(new ArrayList<>(Collections.singletonList(name)))
            .withLocation(new ZLocation(location))
            .withNpcType(npcType);
    ConfigurationConstants.NPC_LIST.add(pojo);
    return new NPC(pojo, true);
  }

  public static void deleteNPC(int npcID) {
    NPC npc = NPC.find(npcID);
    if (npc == null)
      throw new IllegalStateException("can't find npc:  " + npcID);
    NPC.unregister(npcID);
    ConfigurationConstants.NPC_LIST.remove(npc.getNpcPojo());
  }
}
