package instances.Valefor.BehemothKing;

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

public class BehemothKing extends Quest
{
	private class DHSWorld extends InstanceWorld
	{
		public Map<L2Npc, Boolean> npcList = new HashMap<>();
		public L2Npc BEHEMOTH_BOSS = null;
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
	private static final int FINAL_NPC = 60001;
	
	// mobs
	private static final int BEHEMOTH_BOSS = 38009;
	private static final int BEHEMOTH_CRYSTAL = 38008;
	// private static final int CRYSTAL_DEAD = 0;
	
	//@formatter:off
	private static final int[] BEHEMOTH_MOBIDS = {38005,38006,38007};
	
	// Doors/Walls/Zones
	private static final int[][] ROOM_1_MOBS = {
		{38007,83983,-17645,-1833}, {38006,84056,-17272,-1855}, {38005,84671,-17102,-1840},
		{38007,84214,-16852,-1848}, {38006,84383,-16311,-1834}, {38005,83981,-16022,-1834},
		{38007,83665,-15509,-1834}, {38006,83231,-15198,-1841}, {38005,82802,-15650,-1898},
		{38007,82266,-15698,-1888}, {38006,81799,-15189,-1839}, {38005,81484,-15807,-1850},
		{38007,81199,-16456,-1837}, {38006,81649,-16542,-1857}, {38005,81894,-17002,-1830},
		{38007,82530,-17265,-1824}, {38006,83199,-17547,-1828}, {38005,83680,-17273,-1834},
		{38007,83536,-16772,-1869}, {38006,83484,-16354,-1887}, {38005,82975,-16486,-1893},
		{38007,82709,-16910,-1880}, {38006,83491,-16783,-1870}, {38005,82179,-15588,-1878},
		{38007,81387,-15969,-1856}, {38006,81642,-16958,-1827}, {38005,82207,-17312,-1826},
		{38007,82870,-17059,-1853}, {38006,83656,-16482,-1864}, {38005,83433,-15646,-1853},
		{38007,82223,-16111,-1897}, {38006,81123,-15785,-1834}, {38005,81097,-15028,-1835},
		{38007,81967,-14654,-1819}, {38006,82484,-16545,-1888}, {38005,85307,-16420,-1834},
		{38007,85065,-16952,-1824}, {38006,84495,-17306,-1860}, {38005,84519,-16822,-1832},
		{38007,84213,-16617,-1833}, {38006,83728,-17001,-1846}, {38005,83260,-17210,-1824},
		{38007,83085,-16792,-1886}, {38006,82758,-16210,-1891}, {38005,82300,-16299,-1894},
		{38007,81363,-16081,-1857}, {38006,81511,-15471,-1836}, {38005,82077,-15051,-1837},
		{38007,82753,-15427,-1871}, {38006,83194,-15980,-1895}, {38005,83866,-15793,-1833},
		{38007,84348,-15795,-1832}, {38006,84879,-16523,-1824}, {38005,84589,-16717,-1829},
		{38007,83536,-16772,-1869}, {38006,83484,-16354,-1887}, {38005,82975,-16486,-1893},
		{38007,82709,-16910,-1880}, {38006,83491,-16783,-1870}, {38005,82179,-15588,-1878},
		{38007,81387,-15969,-1856}, {38006,81642,-16958,-1827}, {38005,82207,-17312,-1826},
		{38007,82870,-17059,-1853}, {38006,83656,-16482,-1864}, {38005,83433,-15646,-1853},
		{38007,82223,-16111,-1897}, {38006,81123,-15785,-1834}, {38005,81097,-15028,-1835},
		{38007,81967,-14654,-1819}, {38006,82484,-16545,-1888}, {38005,85307,-16420,-1834},
		{38007,82447,-16303,-1898}
	};
	private static final int[][] CRYSTAL_SPAWNS = {
		{81000,-15440,-1810}
	};
	private static final int[][] BOSS_SPAWN = {{38009,81561,-15432,-1837}};
	private static final int[] FINAL_NPC_SPAWN = {79367,-16617,-819};
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
	
	public BehemothKing()
	{
		super(-1, BehemothKing.class.getSimpleName(), "gracia/instances");
		
		addStartNpc(ENTRANCE_NPC);
		addTalkId(ENTRANCE_NPC);
		addStartNpc(FINAL_NPC);
		addTalkId(FINAL_NPC);
		addKillId(BEHEMOTH_CRYSTAL);
		addKillId(BEHEMOTH_BOSS);
		addAttackId(BEHEMOTH_BOSS);
		addSkillSeeId(BEHEMOTH_MOBIDS);
		addKillId(BEHEMOTH_MOBIDS);
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
			if ((partyMember.getLevel() < 80) || (partyMember.getLevel() > 85))
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
			_log.info("Epic Mission as been started " + template + " Instance: " + world.getInstanceId() + " created by player: " + player.getName());
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
		L2Npc mob = addSpawn(BEHEMOTH_CRYSTAL, CRYSTAL_SPAWNS[world.getStatus()][0], CRYSTAL_SPAWNS[world.getStatus()][1], CRYSTAL_SPAWNS[world.getStatus()][2], 0, false, 0, false, world.getInstanceId());
		mob.disableCoreAI(true);
		mob.setIsImmobilized(true);
		mob.setCurrentHp(mob.getMaxHp() * 1.0);
		world.npcList.put(mob, false);
		world.setStatus(world.getStatus() + 1);
	}
	
	protected void runTwins(DHSWorld world)
	{
		world.setStatus(world.getStatus() + 1);
		world.BEHEMOTH_BOSS = addSpawn(BOSS_SPAWN[0][0], BOSS_SPAWN[0][1], BOSS_SPAWN[0][2], BOSS_SPAWN[0][3], 0, false, 0, false, world.getInstanceId());
		world.BEHEMOTH_BOSS.setIsMortal(true);
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
				if (((DHSWorld) tmpworld).BEHEMOTH_BOSS.isDead())
				{
					((DHSWorld) tmpworld).BEHEMOTH_BOSS.setIsDead(false);
					((DHSWorld) tmpworld).BEHEMOTH_BOSS.doDie(attacker);
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
			else if ((world.getStatus() == 2) && ((npc.getId() == BEHEMOTH_BOSS)))
			{
				if (world.BEHEMOTH_BOSS.isDead())
				{
					world.setStatus(world.getStatus() + 1);
					// instance end
					world.storeTime[1] = System.currentTimeMillis();
					world.BEHEMOTH_BOSS = null;
					Broadcast.toAllOnlinePlayers("King Behemoth as been defeated by brave heroes!");
					addSpawn(FINAL_NPC, FINAL_NPC_SPAWN[0], FINAL_NPC_SPAWN[1], FINAL_NPC_SPAWN[2], 0, false, 0, false, world.getInstanceId());
					
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
			tele.x = 85133;
			tele.y = -17938;
			tele.z = -1863;
			enterInstance(player, "BehemothKing.xml", tele);
			return "";
		}
		else if (npcId == FINAL_NPC)
		{
			InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
			Long finishDiff = ((DHSWorld) world).storeTime[1] - ((DHSWorld) world).storeTime[0];
			if (finishDiff < 1260000)
			{
				st.giveItems(13777, 1);
			}
			else if (finishDiff < 1380050)
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