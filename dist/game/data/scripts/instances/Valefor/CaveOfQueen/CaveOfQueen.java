package instances.Valefor.CaveOfQueen;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import l2r.gameserver.enums.CtrlEvent;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.instancemanager.InstanceManager;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.L2Party;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.L2Summon;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.entity.Instance;
import l2r.gameserver.model.instancezone.InstanceWorld;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ExSendUIEvent;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.util.Util;
import l2r.gameserver.util.Broadcast;

public class CaveOfQueen extends Quest
{
	private class DHSWorld extends InstanceWorld
	{
		public Map<L2Npc, Boolean> npcList = new HashMap<>();
		public L2Npc QUEEN = null;
		public boolean isBossesAttacked = false;
		public long[] storeTime =
		{
			0,
			0
		}; // 0: instance start, 1: finish time
		
		public DHSWorld()
		{
			// InstanceManager.getInstance().super();
		}
	}
	
	private static final int INSTANCEID = 161; // this is the client number
	private static final boolean debug = false;
	
	// Items
	
	// NPCs
	private static final int ENTRANCE_NPC = 50000;
	private static final int ENDING_NPC = 50008;
	
	// mobs
	private static final int QUEEN = 38004;
	private static final int EGG_ALIVE = 38003;
	// private static final int EGG_DEAD = 0;
	
	//@formatter:off
	private static final int[] QUEEN_MOBIDS = {38002,38001,38000};
	
	// Doors/Walls/Zones
	private static final int[][] ROOM_1_MOBS = {
		{38002,-113695,87329,-12884}, {38001,-113465,87388,-12884}, {38000,-113314,87131,-12884},
		{38002,-113432,86948,-12884}, {38001,-113811,87155,-12884}, {38000,-115236,87401,-12792},
		{38002,-115278,87001,-12787}, {38001,-115475,86935,-12787}, {38000,-115698,87173,-12787},
		{38002,-115605,87384,-12787}, {38001,-115449,87184,-12787}, {38000,-117168,87364,-12692},
		{38002,-117212,87054,-12692}, {38001,-117079,87208,-12692}, {38000,-117504,87096,-12692},
		{38002,-117481,87380,-12692}
	};
	private static final int[][] EGG_SPAWNS = {
		{-119541,87177,-12595}
	};
	private static final int[][] QUEEN_SPAWNS = {{38004,-119901,87177,-12595}};
	private static final int[] ENDING_NPC_SPAWN = {-119541,87177,-12595};
	//@formatter:on
	
	// Instance reenter time
	// default: 24h
	private static final int INSTANCEPENALTY = 24;
	
	public class teleCoord
	{
		int instanceId;
		int x;
		int y;
		int z;
	}
	
	public CaveOfQueen()
	{
		super(-1, CaveOfQueen.class.getSimpleName(), "gracia/instances");
		
		addStartNpc(ENTRANCE_NPC);
		addTalkId(ENTRANCE_NPC);
		addStartNpc(ENDING_NPC);
		addTalkId(ENDING_NPC);
		addKillId(EGG_ALIVE);
		addKillId(QUEEN);
		addAttackId(QUEEN);
		addSkillSeeId(QUEEN_MOBIDS);
		addKillId(QUEEN_MOBIDS);
	}
	
	private boolean checkConditions(L2PcInstance player)
	{
		if (debug || player.isGM())
		{
			return true;
		}
		L2Party party = player.getParty();
		if (party == null)
		{
			player.sendPacket(SystemMessage.getSystemMessage(2101));
			return false;
		}
		if (party.getLeader() != player)
		{
			player.sendPacket(SystemMessage.getSystemMessage(2185));
			return false;
		}
		for (L2PcInstance partyMember : party.getMembers())
		{
			if ((partyMember.getLevel() < 50) || (partyMember.getLevel() > 56))
			{
				SystemMessage sm = SystemMessage.getSystemMessage(2097);
				sm.addPcName(partyMember);
				party.broadcastPacket(sm);
				return false;
			}
			if (!Util.checkIfInRange(1000, player, partyMember, true))
			{
				SystemMessage sm = SystemMessage.getSystemMessage(2096);
				sm.addPcName(partyMember);
				party.broadcastPacket(sm);
				return false;
			}
			Long reentertime = InstanceManager.getInstance().getInstanceTime(partyMember.getObjectId(), INSTANCEID);
			if (System.currentTimeMillis() < reentertime)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(2100);
				sm.addPcName(partyMember);
				party.broadcastPacket(sm);
				return false;
			}
		}
		return true;
	}
	
	private void teleportplayer(L2PcInstance player, teleCoord teleto)
	{
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(teleto.instanceId);
		player.teleToLocation(teleto.x, teleto.y, teleto.z);
		return;
	}
	
	protected void enterInstance(L2PcInstance player, String template, teleCoord teleto)
	{
		// check for existing instances for this player
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		// existing instance
		if (world != null)
		{
			if (!(world instanceof DHSWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_ENTERED_ANOTHER_INSTANT_ZONE_THEREFORE_YOU_CANNOT_ENTER_CORRESPONDING_DUNGEON));
				return;
			}
			teleto.instanceId = world.getInstanceId();
			teleportplayer(player, teleto);
			return;
		}
		
		if (checkConditions(player))
		{
			world = new DHSWorld();
			world.setInstanceId(InstanceManager.getInstance().createDynamicInstance(template));
			world.setTemplateId(INSTANCEID);
			world.setStatus(0);
			((DHSWorld) world).storeTime[0] = System.currentTimeMillis();
			InstanceManager.getInstance().addWorld(world);
			_log.info("Cave of the Queen Ant started " + template + " Instance: " + world.getInstanceId() + " created by player: " + player.getName());
			runTumors((DHSWorld) world);
			// teleport players
			teleto.instanceId = world.getInstanceId();
			
			L2Party party = player.getParty();
			if (party == null)
			{
				teleportplayer(player, teleto);
				world.addAllowed(player.getObjectId());
			}
			else
			{
				for (L2PcInstance partyMember : party.getMembers())
				{
					teleportplayer(partyMember, teleto);
					world.addAllowed(partyMember.getObjectId());
				}
			}
		}
	}
	
	protected void exitInstance(L2PcInstance player, teleCoord tele)
	{
		player.setInstanceId(0);
		player.teleToLocation(tele.x, tele.y, tele.z);
		L2Summon pet = player.getSummon();
		if (pet != null)
		{
			pet.setInstanceId(0);
			pet.teleToLocation(tele.x, tele.y, tele.z);
		}
	}
	
	protected boolean checkKillProgress(L2Npc mob, DHSWorld world)
	{
		if (world.npcList.containsKey(mob))
		{
			world.npcList.put(mob, true);
		}
		for (boolean isDead : world.npcList.values())
		{
			if (!isDead)
			{
				return false;
			}
		}
		return true;
	}
	
	protected int[][] getRoomSpawns(int room)
	{
		switch (room)
		{
			case 0:
				return ROOM_1_MOBS;
		}
		_log.warn("");
		return new int[][] {};
	}
	
	protected void runTumors(DHSWorld world)
	{
		for (int[] mob : getRoomSpawns(world.getStatus()))
		{
			L2Npc npc = addSpawn(mob[0], mob[1], mob[2], mob[3], 0, false, 0, false, world.getInstanceId());
			world.npcList.put(npc, false);
		}
		L2Npc mob = addSpawn(EGG_ALIVE, EGG_SPAWNS[world.getStatus()][0], EGG_SPAWNS[world.getStatus()][1], EGG_SPAWNS[world.getStatus()][2], 0, false, 0, false, world.getInstanceId());
		mob.disableCoreAI(true);
		mob.setIsImmobilized(true);
		mob.setCurrentHp(mob.getMaxHp() * 1.0);
		world.npcList.put(mob, false);
		world.setStatus(world.getStatus() + 1);
	}
	
	protected void runTwins(DHSWorld world)
	{
		world.setStatus(world.getStatus() + 1);
		world.QUEEN = addSpawn(QUEEN_SPAWNS[0][0], QUEEN_SPAWNS[0][1], QUEEN_SPAWNS[0][2], QUEEN_SPAWNS[0][3], 0, false, 0, false, world.getInstanceId());
		world.QUEEN.setIsMortal(true);
	}
	
	protected void bossSimpleDie(L2Npc boss)
	{
		// killing is only possible one time
		synchronized (this)
		{
			if (boss.isDead())
			{
				return;
			}
			// now reset currentHp to zero
			boss.setCurrentHp(0);
			boss.setIsDead(true);
		}
		
		// Set target to null and cancel Attack or Cast
		boss.setTarget(null);
		
		// Stop movement
		boss.stopMove(null);
		
		// Stop HP/MP/CP Regeneration task
		boss.getStatus().stopHpMpRegeneration();
		
		boss.stopAllEffectsExceptThoseThatLastThroughDeath();
		
		// Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
		boss.broadcastStatusUpdate();
		
		// Notify L2Character AI
		boss.getAI().notifyEvent(CtrlEvent.EVT_DEAD);
		
		if (boss.getWorldRegion() != null)
		{
			boss.getWorldRegion().onDeath(boss);
		}
	}
	
	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if (skill.hasEffectType(L2EffectType.REBALANCE_HP, L2EffectType.HEAL, L2EffectType.HEAL_PERCENT))
		{
			int hate = 2 * skill.getAggroPoints();
			if (hate < 2)
			{
				hate = 1000;
			}
			((L2Attackable) npc).addDamageHate(caster, 0, hate);
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet, L2Skill skill)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof DHSWorld)
		{
			if (!((DHSWorld) tmpworld).isBossesAttacked)
			{
				((DHSWorld) tmpworld).isBossesAttacked = true;
				Calendar reenter = Calendar.getInstance();
				reenter.add(Calendar.HOUR, INSTANCEPENALTY);
				
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.INSTANT_ZONE_FROM_HERE_S1_S_ENTRY_HAS_BEEN_RESTRICTED);
				sm.addString(InstanceManager.getInstance().getInstanceIdName(tmpworld.getTemplateId()));
				
				// set instance reenter time for all allowed players
				for (int objectId : tmpworld.getAllowed())
				{
					L2PcInstance player = L2World.getInstance().getPlayer(objectId);
					if ((player != null) && player.isOnline())
					{
						InstanceManager.getInstance().setInstanceTime(objectId, tmpworld.getTemplateId(), reenter.getTimeInMillis());
						player.sendPacket(sm);
					}
				}
			}
			else if (damage >= npc.getCurrentHp())
			{
				if (((DHSWorld) tmpworld).QUEEN.isDead())
				{
					((DHSWorld) tmpworld).QUEEN.setIsDead(false);
					((DHSWorld) tmpworld).QUEEN.doDie(attacker);
				}
			}
		}
		return null;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof DHSWorld)
		{
			DHSWorld world = (DHSWorld) tmpworld;
			if (world.getStatus() < 1)
			{
				if (checkKillProgress(npc, world))
				{
					runTumors(world);
				}
			}
			else if (world.getStatus() == 1)
			{
				if (checkKillProgress(npc, world))
				{
					runTwins(world);
				}
			}
			else if ((world.getStatus() == 2) && ((npc.getId() == QUEEN)))
			{
				if (world.QUEEN.isDead())
				{
					world.setStatus(world.getStatus() + 1);
					// instance end
					world.storeTime[1] = System.currentTimeMillis();
					world.QUEEN = null;
					Broadcast.toAllOnlinePlayers("The Cave of the Queen as been completed by brave heroes!");
					addSpawn(ENDING_NPC, ENDING_NPC_SPAWN[0], ENDING_NPC_SPAWN[1], ENDING_NPC_SPAWN[2], 0, false, 0, false, world.getInstanceId());
					
					for (Integer pc : world.getAllowed())
					{
						L2PcInstance killer = L2World.getInstance().getPlayer(pc);
						if (killer != null)
						{
							killer.sendPacket(new ExSendUIEvent(killer, true, true, 0, 0, ""));
						}
					}
					
					Instance inst = InstanceManager.getInstance().getInstance(world.getInstanceId());
					inst.setDuration(5 * 60000);
					inst.setEmptyDestroyTime(0);
				}
			}
		}
		return "";
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = getQuestState(player, true);
		if (st == null)
		{
			return htmltext;
		}
		
		int npcId = npc.getId();
		if (npcId == ENTRANCE_NPC)
		{
			teleCoord tele = new teleCoord();
			tele.x = -111833;
			tele.y = 87179;
			tele.z = -12975;
			enterInstance(player, "CaveOfQueen.xml", tele);
			return "";
		}
		else if (npcId == ENDING_NPC)
		{
			InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
			Long finishDiff = ((DHSWorld) world).storeTime[1] - ((DHSWorld) world).storeTime[0];
			if (finishDiff < 1260000)
			{
				st.giveItems(13777, 1);
			}
			else if (finishDiff < 1380000)
			{
				st.giveItems(13778, 1);
			}
			else if (finishDiff < 1500000)
			{
				st.giveItems(13779, 1);
			}
			else if (finishDiff < 1620000)
			{
				st.giveItems(13780, 1);
			}
			else if (finishDiff < 1740000)
			{
				st.giveItems(13781, 1);
			}
			else if (finishDiff < 1860000)
			{
				st.giveItems(13782, 1);
			}
			else if (finishDiff < 1980000)
			{
				st.giveItems(13783, 1);
			}
			else if (finishDiff < 2100000)
			{
				st.giveItems(13784, 1);
			}
			else if (finishDiff < 2220000)
			{
				st.giveItems(13785, 1);
			}
			else
			{
				st.giveItems(13786, 1);
			}
			world.removeAllowed(player.getObjectId());
			teleCoord tele = new teleCoord();
			tele.instanceId = 0;
			tele.x = 83316;
			tele.y = 148000;
			tele.z = -3404;
			exitInstance(player, tele);
		}
		return "";
	}
}