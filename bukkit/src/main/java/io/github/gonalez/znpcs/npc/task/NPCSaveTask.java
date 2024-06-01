package io.github.gonalez.znpcs.npc.task;

import io.github.gonalez.znpcs.ServersNPC;
import io.github.gonalez.znpcs.configuration.Configuration;
import org.bukkit.scheduler.BukkitRunnable;

public class NPCSaveTask extends BukkitRunnable {
  public NPCSaveTask(ServersNPC serversNPC, int seconds) {
    runTaskTimerAsynchronously(serversNPC, seconds, seconds);
  }
  
  public void run() {
    Configuration.SAVE_CONFIGURATIONS.forEach(Configuration::save);
  }
}
