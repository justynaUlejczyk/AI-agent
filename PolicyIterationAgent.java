package ticTacToe;


//import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
/**
 * A policy iteration agent. You should implement the following methods:
 * (1) {@link PolicyIterationAgent#evaluatePolicy}: this is the policy evaluation step from your lectures
 * (2) {@link PolicyIterationAgent#improvePolicy}: this is the policy improvement step from your lectures
 * (3) {@link PolicyIterationAgent#train}: this is a method that should runs/alternate (1) and (2) until convergence. 
 * 
 * NOTE: there are two types of convergence involved in Policy Iteration: Convergence of the Values of the current policy, 
 * and Convergence of the current policy to the optimal policy.
 * The former happens when the values of the current policy no longer improve by much (i.e. the maximum improvement is less than 
 * some small delta). The latter happens when the policy improvement step no longer updates the policy, i.e. the current policy 
 * is already optimal. The algorithm should stop when this happens.
 * 
 * @author ae187
 *
 */
public class PolicyIterationAgent extends Agent {

	/**
	 * This map is used to store the values of states according to the current policy (policy evaluation). 
	 */
	HashMap<Game, Double> policyValues=new HashMap<Game, Double>();
	
	/**
	 * This stores the current policy as a map from {@link Game}s to {@link Move}. 
	 */
	HashMap<Game, Move> curPolicy=new HashMap<Game, Move>();
	
	double discount=0.9;
	
	/**
	 * The mdp model used, see {@link TTTMDP}
	 */
	TTTMDP mdp;
	
	/**
	 * loads the policy from file if one exists. Policies should be stored in .pol files directly under the project folder.
	 */
	public PolicyIterationAgent() {
		
		super();
		this.mdp=new TTTMDP();
		initValues();
		initRandomPolicy();
		train();
		
		this.policy = new Policy(curPolicy); 
	}
	
	
	/**
	 * Use this constructor to initialise your agent with an existing policy
	 * @param p
	 */
	public PolicyIterationAgent(Policy p) {
		super(p);
		
	}

	/**
	 * Use this constructor to initialise a learning agent with default MDP paramters (rewards, transitions, etc) as specified in 
	 * {@link TTTMDP}
	 * @param discountFactor
	 */
	public PolicyIterationAgent(double discountFactor) {
		
		this.discount=discountFactor;
		this.mdp=new TTTMDP();
		initValues();
		initRandomPolicy();
		train();
	}
	/**
	 * Use this constructor to set the various parameters of the Tic-Tac-Toe MDP
	 * @param discountFactor
	 * @param winningReward
	 * @param losingReward
	 * @param livingReward
	 * @param drawReward
	 */
	public PolicyIterationAgent(double discountFactor, double winningReward, double losingReward, double livingReward, double drawReward)
	{
		this.discount=discountFactor;
		this.mdp=new TTTMDP(winningReward, losingReward, livingReward, drawReward);
		initValues();
		initRandomPolicy();
		train();
	}
	/**
	 * Initialises the {@link #policyValues} map, and sets the initial value of all states to 0 
	 * (V0 under some policy pi ({@link #curPolicy} from the lectures). Uses {@link Game#inverseHash} and {@link Game#generateAllValidGames(char)} to do this. 
	 * 
	 */
	public void initValues()
	{
		List<Game> allGames=Game.generateAllValidGames('X');//all valid games where it is X's turn, or it's terminal.
		for(Game g: allGames)
			this.policyValues.put(g, 0.0);
		
	}
	
	/**
	 *  You should implement this method to initially generate a random policy, i.e. fill the {@link #curPolicy} for every state. Take care that the moves you choose
	 *  for each state ARE VALID. You can use the {@link Game#getPossibleMoves()} method to get a list of valid moves and choose 
	 *  randomly between them. 
	 */
	
	  /**
     * The current player
     */
   
	
	public void initRandomPolicy() {
		
		Random random = new Random();
	    List<Game> allGames = Game.generateAllValidGames('X');

	    for (Game g : allGames) {
	        List<Move> possibleMoves = g.getPossibleMoves();
	        if (!possibleMoves.isEmpty()) {
	            int randomIndex = random.nextInt(possibleMoves.size());
	            Move randomMove = possibleMoves.get(randomIndex);
	            curPolicy.put(g, randomMove);
	        }
	    }
		
	    }



	/**
	 * Performs policy evaluation steps until the maximum change in values is less than {@code delta}, in other words
	 * until the values under the currrent policy converge. After running this method, 
	 * the {@link PolicyIterationAgent#policyValues} map should contain the values of each reachable state under the current policy. 
	 * You should use the {@link TTTMDP} {@link PolicyIterationAgent#mdp} provided to do this.
	 *
	 * @param delta
	 */
	
		protected void evaluatePolicy(double delta) {
		    
			double maxChange;
			maxChange = 0.0;
			
			while (maxChange < delta){
		       
		        for (Game game : policyValues.keySet()) {
		            double oldValue = policyValues.get(game);
		            double newValue = calculateStateValue(game);

		            policyValues.put(game, newValue);

		            maxChange = Math.max(maxChange, Math.abs(newValue - oldValue));
		        }
		    } 
		}


	
	
	// Helper method to calculate the new value of a state under the current policy
		private double calculateStateValue(Game game) {
		   
			Move currentMove = curPolicy.get(game);

		    if (currentMove == null) {
		        return 0.0;
		    }

		    List<TransitionProb> transitions = mdp.generateTransitions(game, currentMove);

		    if (transitions == null) {
		        return 0.0;
		    }

		    double newValue = 0.0;

		    for (TransitionProb transition : transitions) {
		        Outcome outcome = transition.outcome;

		        if (outcome != null) {
		            double probability = transition.prob;

		            if (outcome.sPrime != null) {
		                double nextStateValue = policyValues.getOrDefault(outcome.sPrime, 0.0);
		                newValue += probability * (outcome.localReward + discount * nextStateValue);
		            }
		        }
		    }

		    return newValue;
		}
	
	/**This method should be run AFTER the {@link PolicyIterationAgent#evaluatePolicy} train method to improve the current policy according to 
	 * {@link PolicyIterationAgent#policyValues}. You will need to do a single step of expectimax from each game (state) key in {@link PolicyIterationAgent#curPolicy} 
	 * to look for a move/action that potentially improves the current policy. 
	 * 
	 * @return true if the policy improved. Returns false if there was no improvement, i.e. the policy already returned the optimal actions.
	 */
		protected boolean improvePolicy() {
		    boolean policyImproved = false;

		    for (Game game : curPolicy.keySet()) {
		        Move currentMove = curPolicy.get(game);
		        List<TransitionProb> transitions = mdp.generateTransitions(game, currentMove);
		        double maxExpectedValue = Double.NEGATIVE_INFINITY;
		        Move bestMove = null;

		        for (TransitionProb transition : transitions) {
		            Outcome outcome = transition.outcome;

		            if (outcome != null && outcome.move != null) { 
		                double probability = transition.prob;
		                double nextStateValue = policyValues.get(outcome.sPrime);
		                double expectedValue = probability * (outcome.localReward + discount * nextStateValue);

		                if (expectedValue > maxExpectedValue) {
		                    maxExpectedValue = expectedValue;
		                    bestMove = outcome.move;
		                }
		            }
		        }

		        if (bestMove != null && !bestMove.equals(currentMove)) {
		            curPolicy.put(game, bestMove);
		            policyImproved = true;
		        }
		    }

		    return policyImproved;
		}

	
	/**
	 * The (convergence) delta
	 */
	double delta=0.1;
	
	/**
	 * This method should perform policy evaluation and policy improvement steps until convergence (i.e. until the policy
	 * no longer changes), and so uses your 
	 * {@link PolicyIterationAgent#evaluatePolicy} and {@link PolicyIterationAgent#improvePolicy} methods.
	 */
	public void train() {

        while (improvePolicy()) {
            evaluatePolicy(delta);
        }

        this.policy = new Policy(curPolicy);
    }
	
	public Policy getPolicy() {
        return this.policy;
    }
	
	public static void main(String[] args) throws IllegalMoveException
	{
		/**
		 * Test code to run the Policy Iteration Agent agains a Human Agent.
		 */
		PolicyIterationAgent pi=new PolicyIterationAgent();
		
		HumanAgent h=new HumanAgent();
		
		Game g=new Game(pi, h, h);
		
		g.playOut();
		
		
	}
	

}
