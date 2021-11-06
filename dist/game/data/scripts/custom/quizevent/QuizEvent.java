package custom.quizevent;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import l2r.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.util.Broadcast;
import l2r.util.Rnd;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author Bellatrix
 */
public class QuizEvent
{
	public static boolean _quizRunning;
	private static String _question;
	private static String _answer1;
	private static String _answer2;
	private static String _answer3;
	private static int _rightanswer;
	private static Map<L2PcInstance, Integer> _players;
	private static int status;
	private static int announced;
	private static ThreadPoolManager tpm;
	private static AutoEventTask task;
	private static String[][] _questions;
	private static int i = 0;
	private static final int STATUS_NOT_IN_PROGRESS = 0;
	private static final int STATUS_ASK = 1;
	private static final int STATUS_ANSWER = 2;
	private static final int STATUS_END = 3;
	
	// ----------------------------------------------------------------------------
	// ------------------------------ CONFIG --------------------------------------
	// ----------------------------------------------------------------------------
	
	// Number of questions per event
	private static int _questionNumber = 3;
	
	// The Item ID of the reward
	private static int _rewardID = 9627;
	
	// The ammount of the reward
	private static int _rewardCount = 1;
	
	// Wait for the first event after the server start (in seconds)
	private static int _initWait = 300;
	
	// Time for answer the question (in seconds)
	private static int _answerTime = 30;
	
	// Time between two event (2 hours in seconds)
	private static int _betweenTime = 7200;
	
	public QuizEvent()
	{
		tpm = ThreadPoolManager.getInstance();
		status = STATUS_NOT_IN_PROGRESS;
		task = new AutoEventTask();
		announced = 0;
		_quizRunning = false;
		_question = "";
		_answer1 = "";
		_answer2 = "";
		_answer3 = "";
		_rightanswer = 0;
		_players = new HashMap<>(100);
		_questions = new String[20][];
		includeQuestions();
		tpm.scheduleGeneral(task, _initWait * 1000);
		
	}
	
	private void includeQuestions()
	{
		
		File questionFile = new File(Config.DATAPACK_ROOT, "data/scripts/custom/QuizEvent/QuizEvent.xml");
		Document doc = null;
		try
		{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setIgnoringComments(true);
			dbf.setValidating(false);
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(questionFile);
			
			for (Node root = doc.getFirstChild(); root != null; root = root.getNextSibling())
			{
				if ("list".equalsIgnoreCase(root.getNodeName()))
				{
					
					for (Node child = root.getFirstChild(); child != null; child = child.getNextSibling())
					{
						
						if ("question".equalsIgnoreCase(child.getNodeName()))
						{
							int id, correct;
							String ask, answer1, answer2, answer3;
							NamedNodeMap attrs = child.getAttributes();
							
							id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
							correct = Integer.parseInt(attrs.getNamedItem("correct").getNodeValue());
							ask = attrs.getNamedItem("ask").getNodeValue();
							answer1 = attrs.getNamedItem("answer1").getNodeValue();
							answer2 = attrs.getNamedItem("answer2").getNodeValue();
							answer3 = attrs.getNamedItem("answer3").getNodeValue();
							
							_questions[id] = new String[]
							{
								ask,
								answer1,
								answer2,
								answer3,
								"" + correct
							};
							i++;
							
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			
		}
	}
	
	private class AutoEventTask implements Runnable
	{
		/**
		 * 
		 */
		public AutoEventTask()
		{
			// TODO Auto-generated constructor stub
		}
		
		@SuppressWarnings("synthetic-access")
		@Override
		public void run()
		{
			switch (status)
			{
				case STATUS_NOT_IN_PROGRESS:
					announceStart();
					break;
				case STATUS_ASK:
					if (announced < _questionNumber)
					{
						announceQuestion();
					}
					else
					{
						status = STATUS_END;
						tpm.scheduleGeneral(task, 3000);
					}
					break;
				case STATUS_ANSWER:
					announceCorrect();
					break;
				case STATUS_END:
					endEvent();
					break;
				default:
					break;
				
			}
		}
	}
	
	// Get a random question from the quiz_event table
	private static void selectQuestion()
	{
		int id = Rnd.get(i) + 1;
		_question = _questions[id][0];
		_answer1 = _questions[id][1];
		_answer2 = _questions[id][2];
		_answer3 = _questions[id][3];
		_rightanswer = Integer.parseInt("" + _questions[id][4]);
	}
	
	// Announce the question
	private static void announceQuestion()
	{
		
		selectQuestion();
		Broadcast.toAllOnlinePlayers("-----------------");
		Broadcast.toAllOnlinePlayers("Question: " + _question);
		Broadcast.toAllOnlinePlayers("-----------------");
		Broadcast.toAllOnlinePlayers("1: " + _answer1);
		Broadcast.toAllOnlinePlayers("2: " + _answer2);
		Broadcast.toAllOnlinePlayers("3: " + _answer3);
		Broadcast.toAllOnlinePlayers("-----------------");
		
		status = STATUS_ANSWER;
		tpm.scheduleGeneral(task, _answerTime * 1000);
	}
	
	// Announce the correct answer
	private static void announceCorrect()
	{
		Broadcast.toAllOnlinePlayers("-----------------");
		Broadcast.toAllOnlinePlayers("The correct answer was: " + _rightanswer);
		Broadcast.toAllOnlinePlayers("-----------------");
		announced++;
		giveReward();
		status = STATUS_ASK;
		tpm.scheduleGeneral(task, 5000);
	}
	
	private static void announceStart()
	{
		_quizRunning = true;
		_players.clear();
		Broadcast.toAllOnlinePlayers("Quiz Event begins! " + _questionNumber + " questions. " + _answerTime + " secs for answer each. ");
		Broadcast.toAllOnlinePlayers("Type . and the number of the correct answer to the chat. (Like: .1)");
		Broadcast.toAllOnlinePlayers("Get Ready!");
		
		status = STATUS_ASK;
		tpm.scheduleGeneral(task, 5000);
	}
	
	// Add a player and its answer
	public static void setAnswer(L2PcInstance player, int answer)
	{
		if (_players.containsKey(player))
		{
			player.sendMessage("You already choosen an aswer!: " + _players.get(player));
		}
		else
		{
			_players.put(player, answer);
		}
	}
	
	private static void endEvent()
	{
		_quizRunning = false;
		Broadcast.toAllOnlinePlayers("The Quiz Event is over!");
		announced = 0;
		status = STATUS_NOT_IN_PROGRESS;
		tpm.scheduleGeneral(task, _betweenTime * 1000);
	}
	
	private static void giveReward()
	{
		for (L2PcInstance p : _players.keySet())
		{
			if (_players.get(p) == _rightanswer)
			{
				p.sendMessage("Your answer was correct!");
				p.addItem("Quiz", _rewardID, _rewardCount, p, true);
			}
			else
			{
				p.sendMessage("Your answer was not correct!");
			}
			
		}
		_players.clear();
	}
}