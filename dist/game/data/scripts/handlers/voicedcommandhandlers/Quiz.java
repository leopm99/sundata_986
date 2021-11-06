package handlers.voicedcommandhandlers;

import l2r.gameserver.handler.IVoicedCommandHandler;
import l2r.gameserver.model.actor.instance.L2PcInstance;

import custom.quizevent.QuizEvent;

/**
 * @author Bellatrix
 */
public class Quiz implements IVoicedCommandHandler
{
	private static final String[] _voicedCommands =
	{
		"quiz",
		"1",
		"2",
		"3"
	};
	
	/**
	 * @see Bellatrix
	 */
	@SuppressWarnings("javadoc")
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String params)
	{
		
		if (command.equalsIgnoreCase("1") && QuizEvent._quizRunning)
		{
			QuizEvent.setAnswer(activeChar, 1);
		}
		
		if (command.equalsIgnoreCase("2") && QuizEvent._quizRunning)
		{
			QuizEvent.setAnswer(activeChar, 2);
		}
		
		if (command.equalsIgnoreCase("3") && QuizEvent._quizRunning)
		{
			QuizEvent.setAnswer(activeChar, 3);
		}
		return true;
	}
	
	/**
	 * @see Bellatrix
	 */
	@SuppressWarnings("javadoc")
	@Override
	public String[] getVoicedCommandList()
	{
		new QuizEvent();
		return _voicedCommands;
	}
}