package au.com.addstar.notifier;

import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PingPlugin extends JavaPlugin implements Listener
{
	private boolean mHasLoaded = false;
	private String mLoadingMessage;
	private String mKickMessage;
	private int mStartupDelay;
	
	private List<String> mMessages;
	private Random mRandom = new Random();
	
	private String process(String message)
	{
		return message.replace("%motd%", ChatColor.translateAlternateColorCodes('&', getServer().getMotd()));
	}
	
	@EventHandler(priority=EventPriority.LOWEST, ignoreCancelled = true)
	private void onPing(ServerListPingEvent event)
	{
		if(!mHasLoaded)
		{
			event.setMotd(process(mLoadingMessage));
			event.setMaxPlayers(1);
		}
		else
			event.setMotd(process(mMessages.get(mRandom.nextInt(mMessages.size()))));
	}
	
	@EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=true)
	private void onPlayerLoad(AsyncPlayerPreLoginEvent event)
	{
		if(!mHasLoaded)
			event.disallow(Result.KICK_OTHER, mKickMessage);
	}

	private void loadConfig()
	{
		saveDefaultConfig();
		reloadConfig();
		
		mLoadingMessage = getConfig().getString("loading");
		mLoadingMessage = ChatColor.translateAlternateColorCodes('&', mLoadingMessage);
		mLoadingMessage = mLoadingMessage.replace("\\n", "\n");
		
		mKickMessage = getConfig().getString("kickMessage", "The server is starting up. Please try again soon.");
		mKickMessage = ChatColor.translateAlternateColorCodes('&', mKickMessage);
		mKickMessage = mKickMessage.replace("\\n", "\n");
		
		mStartupDelay = getConfig().getInt("startupDelay", 300);
		if(mStartupDelay < 0)
			mStartupDelay = 300;
		
		mMessages = getConfig().getStringList("messages");
		
		if(mMessages.isEmpty())
			mMessages.add("%motd%");
		
		for(int i = 0; i < mMessages.size(); ++i)
			mMessages.set(i, ChatColor.translateAlternateColorCodes('&', mMessages.get(i)).replace("\\n", "\n"));
	}
	
	@Override
	public void onEnable()
	{
		getDataFolder().mkdirs();
		loadConfig();
		
		mHasLoaded = false;
		Bukkit.getPluginManager().registerEvents(this, this);
		Bukkit.getScheduler().runTaskLater(this, new Runnable()
		{
			@Override
			public void run()
			{
				mHasLoaded = true;
			}
		}, mStartupDelay);
	}
	
	@Override
	public boolean onCommand( CommandSender sender, Command command, String label, String[] args )
	{
		if(command.getName().equals("motdreload"))
		{
			loadConfig();
			sender.sendMessage(ChatColor.GREEN + "Config reloaded");
			return true;
		}
		
		return false;
	}
}
