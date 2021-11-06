package handlers.voicedcommandhandlers;

import l2r.Config;
import l2r.gameserver.handler.IVoicedCommandHandler;
import l2r.gameserver.instancemanager.CastleManager;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.entity.Castle;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.SiegeInfo;

/**
 * @author LastWarrior
 * @version 1.0.0
 */

public class SiegeManagerCmd implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"siege",
		"siege_of_gludio",
		"siege_of_dion",
		"siege_of_giran",
		"siege_of_oren",
		"siege_of_aden",
		"siege_of_innadril",
		"siege_of_goddard",
		"siege_of_rune",
		"siege_of_schuttgart",
		"siege_of_"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (Config.ENABLE_GNU_PANEL)
		{
			if (command.startsWith("siege"))
			{
				sendHtml(activeChar);
			}
			
			if (command.startsWith("siege_"))
			{
				if ((activeChar.getClan() != null) && !activeChar.isClanLeader())
				{
					activeChar.sendMessage("Only Clan Leader can be use this command");
					return false;
				}
				
				int castleId = 0;
				
				if (command.startsWith("siege_of_gludio"))
				{
					castleId = 1;
					activeChar.sendMessage("Grettings from Gludio Castle");
				}
				if (command.startsWith("siege_of_dion"))
				{
					castleId = 2;
					activeChar.sendMessage("Grettings from Dion Castle");
				}
				if (command.startsWith("siege_of_giran"))
				{
					castleId = 3;
					activeChar.sendMessage("Grettings from Giran Castle");
				}
				if (command.startsWith("siege_of_oren"))
				{
					castleId = 4;
					activeChar.sendMessage("Grettings from Oren Castle");
				}
				if (command.startsWith("siege_of_aden"))
				{
					castleId = 5;
					activeChar.sendMessage("Grettings from Aden Castle");
				}
				if (command.startsWith("siege_of_innadril"))
				{
					castleId = 6;
					activeChar.sendMessage("Grettings from Innadril Castle");
				}
				if (command.startsWith("siege_of_goddard"))
				{
					castleId = 7;
					activeChar.sendMessage("Grettings from Goddard Castle");
				}
				if (command.startsWith("siege_of_rune"))
				{
					castleId = 8;
					activeChar.sendMessage("Grettings from Rune Castle");
				}
				if (command.startsWith("siege_of_schuttgart"))
				{
					castleId = 9;
					activeChar.sendMessage("Grettings from Schuttgart Castle");
				}
				
				Castle castle = CastleManager.getInstance().getCastleById(castleId);
				if ((castle != null) && (castleId != 0))
				{
					activeChar.sendPacket(new SiegeInfo(castle));
				}
			}
			return true;
		}
		else if (!Config.ENABLE_GNU_PANEL)
		{
			if (command.startsWith("siege"))
			{
				sendErrorHtml(activeChar);
			}
			
			if (command.startsWith("siege_"))
			{
				if ((activeChar.getClan() != null) && !activeChar.isClanLeader())
				{
					activeChar.sendMessage("Only Clan Leader can be use this command");
					return false;
				}
				
				int castleId = 0;
				
				if (command.startsWith("siege_of_gludio"))
				{
					castleId = 1;
					activeChar.sendMessage("Grettings from Gludio Castle");
				}
				if (command.startsWith("siege_of_dion"))
				{
					castleId = 2;
					activeChar.sendMessage("Grettings from Dion Castle");
				}
				if (command.startsWith("siege_of_giran"))
				{
					castleId = 3;
					activeChar.sendMessage("Grettings from Giran Castle");
				}
				if (command.startsWith("siege_of_oren"))
				{
					castleId = 4;
					activeChar.sendMessage("Grettings from Oren Castle");
				}
				if (command.startsWith("siege_of_aden"))
				{
					castleId = 5;
					activeChar.sendMessage("Grettings from Aden Castle");
				}
				if (command.startsWith("siege_of_innadril"))
				{
					castleId = 6;
					activeChar.sendMessage("Grettings from Innadril Castle");
				}
				if (command.startsWith("siege_of_goddard"))
				{
					castleId = 7;
					activeChar.sendMessage("Grettings from Goddard Castle");
				}
				if (command.startsWith("siege_of_rune"))
				{
					castleId = 8;
					activeChar.sendMessage("Grettings from Rune Castle");
				}
				if (command.startsWith("siege_of_schuttgart"))
				{
					castleId = 9;
					activeChar.sendMessage("Grettings from Schuttgart Castle");
				}
				
				Castle castle = CastleManager.getInstance().getCastleById(castleId);
				if ((castle != null) && (castleId != 0))
				{
					activeChar.sendPacket(new SiegeInfo(castle));
				}
			}
		}
		return true;
	}
	
	private void sendHtml(L2PcInstance activeChar)
	{
		String htmFile = "data/html/mods/Castle_Info.htm";
		
		NpcHtmlMessage msg = new NpcHtmlMessage(5);
		msg.setFile(activeChar, htmFile, htmFile);
		activeChar.sendPacket(msg);
	}
	
	private void sendErrorHtml(L2PcInstance activeChar)
	{
		String htmFile = "data/html/mods/Castle_Error_Info.htm";
		
		NpcHtmlMessage msg = new NpcHtmlMessage(5);
		msg.setFile(activeChar, htmFile, htmFile);
		activeChar.sendPacket(msg);
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}