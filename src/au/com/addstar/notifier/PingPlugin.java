package au.com.addstar.notifier;

import java.lang.reflect.Method;
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
	private int mMaxPlayers;
	private int mStartupDelay;
	
	private List<String> mMessages;
	private String mLegacyMessage;
	private Random mRandom = new Random();
	
	private String mOriginalMOTD;
	
	private String process(String message)
	{
		return message.replace("%motd%", mOriginalMOTD);
	}
	
	@EventHandler(priority=EventPriority.LOWEST, ignoreCancelled = true)
	private void onPing(ServerListPingEvent event)
	{
		if(!mHasLoaded)
		{
			event.setMotd(process(mLoadingMessage));
			event.setMaxPlayers(mMaxPlayers);
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
		
		mMaxPlayers = getConfig().getInt("maxplayers", 100);
		if(mMaxPlayers < 1)
			mMaxPlayers = 1;

		mMessages = getConfig().getStringList("messages");
		
		if(mMessages.isEmpty())
			mMessages.add("%motd%");
		
		for(int i = 0; i < mMessages.size(); ++i)
			mMessages.set(i, ChatColor.translateAlternateColorCodes('&', mMessages.get(i)).replace("\\n", "\n"));
		
		mLegacyMessage = ChatColor.translateAlternateColorCodes('&', getConfig().getString("legacyMessage", "%motd%"));
	}
	
	@Override
	public void onEnable()
	{
		getDataFolder().mkdirs();
		loadConfig();
		mOriginalMOTD = ChatColor.translateAlternateColorCodes('&', Bukkit.getMotd());
		
		mHasLoaded = false;
		setServerMotd(process(mLoadingMessage));
		Bukkit.getPluginManager().registerEvents(this, this);
		Bukkit.getScheduler().runTaskLater(this, new Runnable()
		{
			@Override
			public void run()
			{
				setServerMotd(process(mLegacyMessage));
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
			setServerMotd(process(mLegacyMessage));
			sender.sendMessage(ChatColor.GREEN + "Config reloaded");
			return true;
		}
		
		return false;
	}
	
	private static Object mServer;
	private static Method mSetMotdMethod;
	
	public static void setServerMotd(String motd)
	{
		if(mServer == null)
		{
			try
			{
				Class<?> clazz = Bukkit.getServer().getClass();
				Method getServer = clazz.getMethod("getServer");
				mServer = getServer.invoke(Bukkit.getServer());
				
				clazz = mServer.getClass();
				mSetMotdMethod = clazz.getMethod("setMotd", String.class);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				mServer = false;
				return;
			}
		}
		
		if(!(mServer instanceof Boolean))
		{
			try
			{
				mSetMotdMethod.invoke(mServer, motd);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
