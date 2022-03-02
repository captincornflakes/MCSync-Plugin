package mcsync;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
 
public class main extends JavaPlugin implements Listener{
FileConfiguration config = getConfig();

public void onEnable() {
    Bukkit.getPluginManager().registerEvents(this, (Plugin)this); 
	 String serverKey = config.getString("serverKEY");
     System.out.println("[MC-Sync] MCSync is alive, Your server key is " + serverKey);
     this.saveDefaultConfig();
     int pluginId = 14009;
     Metrics metrics = new Metrics(this, pluginId);
     }
 
@EventHandler
public void onPlayerJoin(AsyncPlayerPreLoginEvent e) {
     String message = config.getString("message-allow");
     String serverKey = config.getString("serverKEY");
     boolean authorized = false;
     if (getServer().getWhitelistedPlayers().stream().anyMatch(player -> player.getUniqueId().equals(e.getUniqueId()))) {
          authorized = true;
     } 
     else {
		 try {
			 URL url = new URL("https://mcsync.live/api/join.php?serverKEY=" + serverKey + "&UUID=" + e.getUniqueId().toString().replace("-", ""));
			 HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			 connection.setRequestMethod("GET");
			 BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			 String result = reader.readLine();
			 reader.close();
			 if (result.equals("true")) { authorized = true; }
			 else { message = config.getString("message-fail"); } 
			 } 
		 catch (IOException ignored) {
		       message = config.getString("message-error");
		   } 
	          } 
	          if (!authorized) {e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, message); }
          }
     }



