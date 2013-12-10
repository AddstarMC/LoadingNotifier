package au.com.addstar.notifier;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PingPlugin extends JavaPlugin implements Listener
{
	private boolean mHasLoaded = false;
	private String mMessage;
	
	@EventHandler(priority=EventPriority.LOWEST, ignoreCancelled = true)
	private void onPing(ServerListPingEvent event)
	{
		if(!mHasLoaded)
		{
			event.setMotd(mMessage);
		}
	}

	@Override
	public void onLoad()
	{
		saveDefaultConfig();
		
		mMessage = getConfig().getString("message");
		mMessage = ChatColor.translateAlternateColorCodes('&', mMessage);
		mMessage = mMessage.replace("\\n", "\n");
	}
	
	@Override
	public void onEnable()
	{
		mHasLoaded = false;
		Bukkit.getPluginManager().registerEvents(this, this);
		Bukkit.getScheduler().runTask(this, new Runnable()
		{
			@Override
			public void run()
			{
				mHasLoaded = true;
			}
		});
	}
}
