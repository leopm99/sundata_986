package custom.RewardForTimeOnline;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import l2r.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.events.Containers;
import l2r.gameserver.model.events.EventType;
import l2r.gameserver.model.events.impl.character.player.OnPlayerLogin;
import l2r.gameserver.model.events.impl.character.player.OnPlayerLogout;
import l2r.gameserver.model.events.listeners.ConsumerEventListener;
import l2r.gameserver.model.quest.Quest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author LifeGame32
 */
public final class RewardForTimeOnline extends Quest
{
	static final Logger LOG = LoggerFactory.getLogger(RewardForTimeOnline.class);
	
	// on / off
	static final boolean LOAD = true;
	
	final Map<Integer, PlayerHolder> players;
	
	final List<ItemHolder> rewardItem;
	
	// unik.var db (saving), id, quantity, time in miliesek. save time?
	private void addItem()
	{
		rewardItem.add(new ItemHolder("rfto_1", Config.ID_REWARD, Config.COUNT_REWARD, Config.TIME_REWARD * 1000, false)); // Reward 01
		rewardItem.add(new ItemHolder("rfto_2", Config.ID_REWARD_2, Config.COUNT_REWARD_2, Config.TIME_REWARD2 * 1000, false));// Reward 02
		rewardItem.add(new ItemHolder(getVar(), Config.ID_REWARD_3, Config.COUNT_REWARD_3, Config.TIME_REWARD3 * 1000, false));// Reward 03
	}
	
	private String getVar()
	{
		return getClass().getSimpleName() + "_" + rewardItem.size();
	}
	
	private final class PlayerHolder
	{
		final L2PcInstance player;
		final List<RewardTask> rewardTask = new ArrayList<>();
		
		public PlayerHolder(L2PcInstance player)
		{
			this.player = player;
		}
		
		public final PlayerHolder startRewardTask()
		{
			for (ItemHolder item : rewardItem)
			{
				rewardTask.add(new RewardTask(this, item));
			}
			
			return this;
		}
		
		public final void onPlayerLogout()
		{
			for (RewardTask t : rewardTask)
			{
				t.onPlayerLogout();
			}
		}
	}
	
	private final class RewardTask implements Runnable
	{
		private final PlayerHolder ph;
		private final ItemHolder item;
		private ScheduledFuture<?> task = null;
		
		private long lastTime;
		
		public RewardTask(PlayerHolder playerHolder, ItemHolder item)
		{
			this.ph = playerHolder;
			this.item = item;
			this.lastTime = System.currentTimeMillis();
			
			long initialDelay;
			
			if (item.isSaveTime)
			{
				initialDelay = ph.player.getVariables().getLong(item.var, 0);
				if ((initialDelay == 0) || (initialDelay > item.time))
				{
					initialDelay = item.time;
				}
			}
			else
			{
				initialDelay = item.time;
			}
			
			this.task = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(this, initialDelay, item.time);
		}
		
		@Override
		public void run()
		{
			if ((ph.player == null) || (ph.player.getClient() == null) || ph.player.getClient().isDetached())
			{
				return;
			}
			
			if (ph.player.isOnline())
			{
				if (item.isSaveTime)
				{
					ph.player.getVariables().set(item.var, 0);
				}
				
				lastTime = System.currentTimeMillis();
				
				ph.player.addItem(RewardForTimeOnline.class.getSimpleName(), item.id, item.count, null, true);
			}
		}
		
		public final void onPlayerLogout()
		{
			stopTask();
			
			if (item.isSaveTime)
			{
				ph.player.getVariables().set(item.var, (item.time - (System.currentTimeMillis() - lastTime)));
			}
		}
		
		public final void stopTask()
		{
			if (task != null)
			{
				task.cancel(false);
				task = null;
			}
		}
	}
	
	private final class ItemHolder
	{
		final String var;
		final int id;
		final long count;
		final long time;
		final boolean isSaveTime;
		
		public ItemHolder(String var, int id, long count, long time, boolean isSaveTime)
		{
			this.var = var;
			this.id = id;
			this.count = count;
			this.time = time;
			this.isSaveTime = isSaveTime;
		}
	}
	
	public RewardForTimeOnline()
	{
		super(-1, RewardForTimeOnline.class.getSimpleName(), "custom/RewardForTimeOnline");
		Containers.Global().addListener(new ConsumerEventListener(Containers.Global(), EventType.ON_PLAYER_LOGIN, (OnPlayerLogin event) -> onPlayerLogin(event), this));
		Containers.Global().addListener(new ConsumerEventListener(Containers.Global(), EventType.ON_PLAYER_LOGOUT, (OnPlayerLogout event) -> onPlayerLogout(event), this));
		players = new ConcurrentHashMap<>();
		rewardItem = new ArrayList<>();
		addItem();
	}
	
	private final void onPlayerLogin(OnPlayerLogin event)
	{
		PlayerHolder task = players.get(event.getActiveChar().getObjectId());
		if (task == null)
		{
			players.put(event.getActiveChar().getObjectId(), new PlayerHolder(event.getActiveChar()).startRewardTask());
		}
	}
	
	private final void onPlayerLogout(OnPlayerLogout event)
	{
		PlayerHolder task = players.remove(event.getActiveChar().getObjectId());
		if (task != null)
		{
			task.onPlayerLogout();
		}
	}
	
	public static void main(String[] args)
	{
		if (LOAD)
		{
			new RewardForTimeOnline();
		}
		
		LOG.info("{}: load {}.", RewardForTimeOnline.class.getSimpleName(), LOAD);
	}
}