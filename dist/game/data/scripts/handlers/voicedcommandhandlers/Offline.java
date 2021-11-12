package handlers.voicedcommandhandlers;

import l2r.gameserver.handler.IVoicedCommandHandler;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.entity.ccphelpers.CCPOffline;

public class Offline implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS = new String[]
	{
		"logout"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String args)
	{
		CCPOffline.setOfflineStore(activeChar);
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}