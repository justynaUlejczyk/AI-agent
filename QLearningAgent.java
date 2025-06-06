package ticTacToe;

//import java.util.HashMap;
import java.util.List;

/**
 * A Q-Learning agent with a Q-Table, i.e. a table of Q-Values. This table is implemented in the {@link QTable} class.
 * 
 *  The methods to implement are: 
 * (1) {@link QLearningAgent#train}
 * (2) {@link QLearningAgent#extractPolicy}
 * 
 * Your agent acts in a {@link TTTEnvironment} which provides the method {@link TTTEnvironment#executeMove} which returns an {@link Outcome} object, in other words
 * an [s,a,r,s']: source state, action taken, reward received, and the target state after the opponent has played their move. You may want/need to edit
 * {@link TTTEnvironment} - but you probably won't need to. 
 * @author ae187
 */

public class QLearningAgent extends Agent {
	
	/**
	 * The learning rate, between 0 and 1.
	 */
	double alpha=0.1;
	
	/**
	 * The number of episodes to train for
	 */
	int numEpisodes=100000;
	
	/**
	 * The discount factor (gamma)
	 */
	double discount=0.9;
	
	
	/**
	 * The epsilon in the epsilon greedy policy used during training.
	 */
	double epsilon=0.1;
	
	/**
	 * This is the Q-Table. To get an value for an (s,a) pair, i.e. a (game, move) pair.
	 * 
	 */
	
	QTable qTable=new QTable();
	
	
	/**
	 * This is the Reinforcement Learning environment that this agent will interact with when it is training.
	 * By default, the opponent is the random agent which should make your q learning agent learn the same policy 
	 * as your value iteration and policy iteration agents.
	 */
	TTTEnvironment env=new TTTEnvironment();
	
	
	/**
	 * Construct a Q-Learning agent that learns from interactions with {@code opponent}.
	 * @param opponent the opponent agent that this Q-Learning agent will interact with to learn.
	 * @param learningRate This is the rate at which the agent learns. Alpha from your lectures.
	 * @param numEpisodes The number of episodes (games) to train for
	 */
	public QLearningAgent(Agent opponent, double learningRate, int numEpisodes, double discount)
	{
		env=new TTTEnvironment(opponent);
		this.alpha=learningRate;
		this.numEpisodes=numEpisodes;
		this.discount=discount;
		initQTable();
		train();
	}
	
	/**
	 * Initialises all valid q-values -- Q(g,m) -- to 0.
	 *  
	 */
	
	protected void initQTable()
	{
		List<Game> allGames=Game.generateAllValidGames('X');//all valid games where it is X's turn, or it's terminal.
		for(Game g: allGames)
		{
			List<Move> moves=g.getPossibleMoves();
			for(Move m: moves)
			{
				this.qTable.addQValue(g, m, 0.0);
				//System.out.println("initing q value. Game:"+g);
				//System.out.println("Move:"+m);
			}
			
		}
		
	}
	
	/**
	 * Uses default parameters for the opponent (a RandomAgent) and the learning rate (0.2). Use other constructor to set these manually.
	 */
	public QLearningAgent()
	{
		this(new RandomAgent(), 0.1, 100, 0.9);
		
	}
	
	
	/**
	 *  Implement this method. It should play {@code this.numEpisodes} episodes of Tic-Tac-Toe with the TTTEnvironment, updating q-values according 
	 *  to the Q-Learning algorithm as required. The agent should play according to an epsilon-greedy policy where with the probability {@code epsilon} the
	 *  agent explores, and with probability {@code 1-epsilon}, it exploits. 
	 *  
	 *  At the end of this method you should always call the {@code extractPolicy()} method to extract the policy from the learned q-values. This is currently
	 *  done for you on the last line of the method.
	 */
	 
	
	public void train()
	{
		for (int episode = 0; episode < numEpisodes; episode++) {
	        
	        env.reset();
	        
	        while (!env.isTerminal()) {
	            
	            Game currentState = env.getCurrentGameState();
	            
	            Move selectedMove = epsilonGreedyPolicy(currentState);

	            try {
	               
	                Outcome outcome = env.executeMove(selectedMove);
	                updateQValue(currentState, selectedMove, outcome);
	            
	            } 
	            
	            catch (IllegalMoveException e) {
	                
	                e.printStackTrace();
	            }
	        }
	        
	       
	        this.policy=extractPolicy();
			
	        if (this.policy==null)
			{
				System.out.println("Unimplemented methods! First implement the train() & extractPolicy methods");
				//System.exit(1);
			}

	    }

	    
	}
	
	
	private Move epsilonGreedyPolicy(Game currentState) {
	    List<Move> possibleMoves = currentState.getPossibleMoves();

	    // Explore with probability epsilon
	    if (Math.random() < epsilon) {
	        return possibleMoves.get((int) (Math.random() * possibleMoves.size()));
	    } else {
	        // Exploit by selecting the move with the highest Q-value
	        Move bestMove = null;
	        double bestQValue = Double.NEGATIVE_INFINITY;

	        for (Move move : possibleMoves) {
	            double qValue = qTable.getQValue(currentState, move);
	            if (qValue > bestQValue) {
	                bestQValue = qValue;
	                bestMove = move;
	            }
	        }

	        return bestMove;
	    }
	}
	
	public double calculateMaxQValue(Game gameState) {
	   
		List<Move> moves = gameState.getPossibleMoves();
	    double maxQValue = Double.NEGATIVE_INFINITY;

	    for (Move move : moves) {
	        double qValue = qTable.getQValue(gameState, move);
	        if (qValue > maxQValue) {
	            maxQValue = qValue;
	        }
	    }

	    return maxQValue;
	}
	

	private void updateQValue(Game currentState, Move selectedMove, Outcome outcome) {
	    double maxNextQValue = calculateMaxQValue(outcome.sPrime);

	    Double currentQValueObj = qTable.getQValue(currentState, selectedMove);

	    double currentQValue = currentQValueObj != null ? currentQValueObj : 0.0;

	    double newQValue = (1 - alpha) * currentQValue + alpha * (outcome.localReward + discount * maxNextQValue);

	    qTable.addQValue(currentState, selectedMove, newQValue);
	}

	
	/** Implement this method. It should use the q-values in the {@code qTable} to extract a policy and return it.
	 *
	 * @return the policy currently inherent in the QTable
	 */
	public Policy extractPolicy() {
	    Policy policy = new Policy();

	    for (Game state : qTable.keySet()) {
	        List<Move> possibleMoves = state.getPossibleMoves();
	        
	        if (!possibleMoves.isEmpty()) {
	          
	            Move bestMove = null;
	            double maxQValue = Double.NEGATIVE_INFINITY;

	            for (Move action : possibleMoves) {
	                double qValue = qTable.getQValue(state, action);

	                if (qValue > maxQValue) {
	                    maxQValue = qValue;
	                    bestMove = action;
	                }
	            }

	            policy.policy.put(state, bestMove);
	        }
	    }

	    return policy;
	}

	
	public static void main(String a[]) throws IllegalMoveException
	{
		//Test method to play your agent against a human agent (yourself).
		QLearningAgent agent=new QLearningAgent();
		
		HumanAgent d=new HumanAgent();
		
		Game g=new Game(agent, d, d);
		g.playOut();
		
		
		

		
		
	}
	
	
	


	
}
