package mcsync;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class main extends JavaPlugin implements Listener{
FileConfiguration config = getConfig();
FileConfiguration messagesConfig = getCustomConfig();

private File customConfigFile;
private FileConfiguration customConfig;

String prefix = "[MCS] ";


public void onEnable() {
	Bukkit.getPluginManager().registerEvents(this, (Plugin)this); 
	String serverKey = config.getString("serverKEY");
	System.out.println(prefix + "MCSync is alive, Your server key is " + serverKey);
	this.saveDefaultConfig();
	
	try {
		this.createCustomConfig();
	} catch (InvalidConfigurationException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	int pluginId = 14009;
	Metrics metrics = new Metrics(this, pluginId);
	this.getCommand("mcsync").setExecutor(new CommandMcsync());
}

public FileConfiguration getCustomConfig() {
	return this.customConfig;
}

private void createCustomConfig() throws InvalidConfigurationException {
	customConfigFile = new File(getDataFolder(), "messages.yml");
	if (!customConfigFile.exists()) {
		customConfigFile.getParentFile().mkdirs();
		saveResource("messages.yml", false);
	}
	
	customConfig = new YamlConfiguration();
	try {
		customConfig.load(customConfigFile);
	} catch (IOException | InvalidConfigurationException e){
		e.printStackTrace();
	}
}
 
@EventHandler
public void onPlayerJoin(AsyncPlayerPreLoginEvent e) {
     String message = messagesConfig.getString("message-allow");
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
			 else { message = messagesConfig.getString(prefix + "message-fail"); } 
			 } 
		 catch (IOException ignored) {
		       message = messagesConfig.getString(prefix + "message-error");
		   } 
	          } 
          if (!authorized) {e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, message); }
}


public class CommandMcsync implements CommandExecutor {
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
	     String serverKey = config.getString("serverKEY");
		 if (args.length == 0){
			 sender.sendMessage(prefix + "Correct usage: /mcsync <Argument>");
	     	}
		 if (args.length > 0){
			 if (args[0].equalsIgnoreCase("set")){
					config.set("serverKEY", args[1]);
					sender.sendMessage(prefix + "Server key set to " + args[1]);
					saveConfig();
			 	}
			 else if (args[0].equalsIgnoreCase("get")){
					sender.sendMessage(prefix + "Server key is: " + serverKey);
			 	}
			 else if (args[0].equalsIgnoreCase("test")){
				 try {
					 URL url = new URL("https://mcsync.live/api/join.php?serverKEY=" + serverKey + "&test=true");
					 HttpURLConnection connection = (HttpURLConnection)url.openConnection();
					 connection.setRequestMethod("GET");
					 BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
					 String result = reader.readLine();
					 reader.close();
					 if (result.equals("true")) { sender.sendMessage(prefix + "Congradulations your Key is valid.");  }
					 else { sender.sendMessage(prefix + "Oops, Your key is invalid."); } 
					 } 
				 catch (IOException ignored) {sender.sendMessage(prefix + "Something went wrong.");} 
			 	}
			 else if (args[0].equalsIgnoreCase("mode")){
				 try {
					 URL url = new URL("https://mcsync.live/api/join.php?serverKEY=" + serverKey + "&mode=true");
					 HttpURLConnection connection = (HttpURLConnection)url.openConnection();
					 connection.setRequestMethod("GET");
					 BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
					 String result = reader.readLine();
					 reader.close();
					 sender.sendMessage(prefix + "Your server mode is: " + result);					 } 
				 catch (IOException ignored) {sender.sendMessage(prefix + "Something went wrong.");} 
			 	}

			 else if (args[0].equalsIgnoreCase("version")){
				 try {
					 URL url = new URL("https://mcsync.live/api/join.php?update=true");
					 HttpURLConnection connection = (HttpURLConnection)url.openConnection();
					 connection.setRequestMethod("GET");
					 BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
					 String result = reader.readLine();
					 reader.close();
					 sender.sendMessage(prefix + "The latest version is : " + result);					 } 
				 catch (IOException ignored) {sender.sendMessage(prefix + "Something went wrong.");} 
			 	}
			 else {
				 sender.sendMessage(prefix + "Unknown Command");
		         return false;
			 	}
		 	}
		return true;
		}
	}
}
