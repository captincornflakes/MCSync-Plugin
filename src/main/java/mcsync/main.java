package mcsync;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class main extends JavaPlugin implements Listener{
FileConfiguration config = getConfig();
PluginDescriptionFile versionNumber = this.getDescription();
FileConfiguration messagesConfig = getCustomConfig();

private File customConfigFile;

String prefix = ChatColor.LIGHT_PURPLE + "[" + ChatColor.BLUE + "MCSync" + ChatColor.LIGHT_PURPLE + "] " + ChatColor.RESET ;


public void onEnable() {
	Bukkit.getPluginManager().registerEvents(this, (Plugin)this); 
	String serverKey = config.getString("serverKEY");
	System.out.println("MCSync is alive, Your server key is " + serverKey);
	this.saveDefaultConfig();
	this.createCustomConfig();
	int pluginId = 14009;
	Metrics metrics = new Metrics(this, pluginId);
	this.getCommand("mcsync").setExecutor(new CommandMcsync());
}

public FileConfiguration getCustomConfig() {
	return this.messagesConfig;
}

public void createCustomConfig() {
	customConfigFile = new File(this.getDataFolder(), "messages.yml");
	if (!customConfigFile.exists()) {
		customConfigFile.getParentFile().mkdirs();
		saveResource("messages.yml", false);
	}
	messagesConfig = new YamlConfiguration();
	try {
		messagesConfig.load(customConfigFile);
	} catch (IOException | InvalidConfigurationException e){
		e.printStackTrace();
	}

}
 
@EventHandler
public void onPlayerJoin(AsyncPlayerPreLoginEvent e) {
     String message = messagesConfig.getString("message-allow");
     String error = messagesConfig.getString("message-error");
     String fail = messagesConfig.getString("message-fail");
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
			 else { message = fail; } 
			 } 
		 catch (IOException ignored) {
		       message = error;
		   } 
	          } 
          if (!authorized) {e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, message); }
}


public class CommandMcsync implements CommandExecutor {
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
	     String serverKey = config.getString("serverKEY");
		 if (args.length == 0 || args[0].equalsIgnoreCase("help")){
			 sender.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.STRIKETHROUGH + "--------------------------------------------");
			 sender.sendMessage(ChatColor.GOLD + "The following are valid commands for MCSync");
			 sender.sendMessage(ChatColor.GOLD + "| " + ChatColor.YELLOW + "/mcs set" + ChatColor.GRAY + ChatColor.ITALIC + " (Used to set server token)");
			 sender.sendMessage(ChatColor.GOLD + "| " + ChatColor.YELLOW + "/mcs get" + ChatColor.GRAY + ChatColor.ITALIC + " (Show your server token)");
			 sender.sendMessage(ChatColor.GOLD + "| " + ChatColor.YELLOW + "/mcs test" + ChatColor.GRAY + ChatColor.ITALIC + " (Test connection to MCSync)");
			 sender.sendMessage(ChatColor.GOLD + "| " + ChatColor.YELLOW + "/mcs mode" + ChatColor.GRAY + ChatColor.ITALIC + " (Show which mode your token is set to)");
			 sender.sendMessage(ChatColor.GOLD + "| " + ChatColor.YELLOW + "/mcs version" + ChatColor.GRAY + ChatColor.ITALIC + " (Check Local Build number and Current Released Build number)");
			 sender.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.STRIKETHROUGH + "--------------------------------------------");
	     	}
		 else if (args.length > 0){
			 if (args[0].equalsIgnoreCase("set")){
				 if (args.length < 2){
					 sender.sendMessage(prefix + ChatColor.RED + "Please supply a server Key");
					 }
				 else {
						config.set("serverKEY", args[1]);
						sender.sendMessage(prefix + ChatColor.AQUA + "Server key set to " + ChatColor.GREEN + args[1]);
						saveConfig();
						}
			 	}
			 else if (args[0].equalsIgnoreCase("get")){
					sender.sendMessage(prefix + ChatColor.AQUA + "Your server key is: " + ChatColor.GREEN + serverKey);
			 	}
			 else if (args[0].equalsIgnoreCase("test")){
				 try {
					 URL url = new URL("https://mcsync.live/api/join.php?serverKEY=" + serverKey + "&test=true");
					 HttpURLConnection connection = (HttpURLConnection)url.openConnection();
					 connection.setRequestMethod("GET");
					 BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
					 String result = reader.readLine();
					 reader.close();
					 if (result.equals("true")) { sender.sendMessage(prefix + ChatColor.GREEN + "Congratz! Your token is valid!");  }
					 else { sender.sendMessage(prefix + ChatColor.RED + "Oops, Your key is invalid!"); } 
					 } 
				 catch (IOException ignored) {sender.sendMessage(prefix + ChatColor.RED + "Something went wrong. (Error: 1)");} 
			 	}
			 else if (args[0].equalsIgnoreCase("mode")){
				 try {
					 URL url = new URL("https://mcsync.live/api/join.php?serverKEY=" + serverKey + "&mode=true");
					 HttpURLConnection connection = (HttpURLConnection)url.openConnection();
					 connection.setRequestMethod("GET");
					 BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
					 String result = reader.readLine();
					 reader.close();
					 sender.sendMessage(prefix + ChatColor.AQUA + "Your server mode is set to: " + ChatColor.GREEN + result);					 } 
				 catch (IOException ignored) {sender.sendMessage(prefix + ChatColor.RED + "Something went wrong. (Error: 2)");} 
			 	}

			 else if (args[0].equalsIgnoreCase("version")){
				 try {
					 URL url = new URL("https://mcsync.live/api/join.php?update=true");
					 HttpURLConnection connection = (HttpURLConnection)url.openConnection();
					 connection.setRequestMethod("GET");
					 BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
					 String result = reader.readLine();
					 reader.close();
					 String pluginVersion = versionNumber.getVersion();
					 sender.sendMessage(prefix + ChatColor.AQUA + "Your current version: " + ChatColor.GREEN + pluginVersion);
					 sender.sendMessage(prefix + ChatColor.AQUA + "The latest version is: " + ChatColor.GREEN + result);					 } 
				 catch (IOException ignored) {sender.sendMessage(prefix + ChatColor.RED + "Something went wrong. (Error: 3)");} 
			 	}
			 else {
				 sender.sendMessage(prefix + ChatColor.RED + "Unknown Command");
		         return true;
			 	}
		 	}
		return true;
		}
	}
}
