package ticTacToe;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Value Iteration Agent, only very partially implemented. The methods to implement are: 
 * (1) {@link ValueIterationAgent#iterate}
 * (2) {@link ValueIterationAgent#extractPolicy}
 * 
 * You may also want/need to edit {@link ValueIterationAgent#train} - feel free to do this, but you probably won't need to.
 * @author ae187
 *
 */
public class ValueIterationAgent extends Agent {

	/**
	 * This map is used to store the values of states
	 */
	Map<Game, Double> valueFunction=new HashMap<Game, Double>();
	
	/**
	 * the discount factor
	 */
	double discount=0.9;
	
	/**
	 * the MDP model
	 */
	TTTMDP mdp=new TTTMDP();
	
	/**
	 * the number of iterations to perform - feel free to change this/try out different numbers of iterations
	 */
	int k=10;
	
	
	/**
	 * This constructor trains the agent offline first and sets its policy
	 */
	public ValueIterationAgent()
	{
		super();
		mdp=new TTTMDP();
		this.discount=0.9;
		initValues();
		train();
	}
	
	
	/**
	 * Use this constructor to initialise your agent with an existing policy
	 * @param p
	 */
	public ValueIterationAgent(Policy p) {
		super(p);
		
	}

	public ValueIterationAgent(double discountFactor) {
		
		this.discount=discountFactor;
		mdp=new TTTMDP();
		initValues();
		train();
	}
	
	/**
	 * Initialises the {@link ValueIterationAgent#valueFunction} map, and sets the initial value of all states to 0 
	 * (V0 from the lectures). Uses {@link Game#inverseHash} and {@link Game#generateAllValidGames(char)} to do this. 
	 * 
	 */
	public void initValues()
	{
		
		List<Game> allGames=Game.generateAllValidGames('X');//all valid games where it is X's turn, or it's terminal.
		for(Game g: allGames)
			this.valueFunction.put(g, 0.0);
		
		
		
	}
	
	
	
	public ValueIterationAgent(double discountFactor, double winReward, double loseReward, double livingReward, double drawReward)
	{
		this.discount=discountFactor;
		mdp=new TTTMDP(winReward, loseReward, livingReward, drawReward);
	}
	
	/**
	 
	
	/*
	 * Performs {@link #k} value iteration steps. After running this method, the {@link ValueIterationAgent#valueFunction} map should contain
	 * the (current) values of each reachable state. You should use the {@link TTTMDP} provided to do this.
	 * 
	 *V(s)=max_a Sum_target [ T(s,a,a, target) * (R+gamma *V(target)]
	 */
	public void iterate() {
	    // Value iteration algorithm to compute k-step estimates of the optimal values
	    for (int i = 0; i < k; i++) {
	        Map<Game, Double> newValues = new HashMap<>();

	        for (Game state : valueFunction.keySet()) {
	            double maxQValue = valueFunction.get(state);

	            for (Move action : state.getPossibleMoves()) {
	                List<TransitionProb> transitions = mdp.generateTransitions(state, action);
	                double qValue = calculateQValue(state, action, transitions);

	                if (qValue > maxQValue) {
	                    maxQValue = qValue;
	                }
	            }

	            newValues.put(state, maxQValue);
	        }

	        valueFunction = newValues;
	    }
	}

	// calculate Q 

		    private double calculateQValue(Game state, Move action, List<TransitionProb> transitions) {
		        double qValue = 0.0;

		        for (TransitionProb transitionProb : transitions) {
		            Outcome outcome = transitionProb.outcome;
		            double transitionProbability = transitionProb.prob;
		            double reward = outcome.localReward;
		            double discountedFutureValue = discount * valueFunction.get(outcome.sPrime);

		            // Q(s, a) = Sum_target [ T(s, a, target) * (R + gamma * V(target)) ]
		            qValue += transitionProbability * (reward + discountedFutureValue);
		        }

		        return qValue;
		    }
		





	
	/**This method should be run AFTER the train method to extract a policy according to {@link ValueIterationAgent#valueFunction}
	 * You will need to do a single step of expectimax from each game (state) key in {@link ValueIterationAgent#valueFunction} 
	 * to extract a policy.
	 * 
	 * @return the policy according to {@link ValueIterationAgent#valueFunction}
	 */
	public Policy extractPolicy()
	{
		
		
		
		    Policy policy = new Policy();

		    // Initialize a random policy
		    for (Game state : valueFunction.keySet()) {
		        List<Move> possibleMoves = state.getPossibleMoves();
		        if (!possibleMoves.isEmpty()) {
		            // Select a random move for the initial policy
		            Move randomMove = possibleMoves.get((int) (Math.random() * possibleMoves.size()));
		            policy.policy.put(state, randomMove);
		        }
		    }

		    boolean policyChanged = true;

		    while (policyChanged) {
		        // Flag to check if the policy has changed in this iteration
		        policyChanged = false;

		        

		        for (Game state : valueFunction.keySet()) {
		            List<Move> possibleMoves = state.getPossibleMoves();
		            if (!possibleMoves.isEmpty()) {
		                Move currentBestMove = policy.getMove(state);
		                double maxQValue = calculateQValue(state, currentBestMove, mdp.generateTransitions(state, currentBestMove));

		                // Iterate through possible moves and find the move with the highest Q value
		                for (Move action : possibleMoves) {
		                    double qValue = calculateQValue(state, action, mdp.generateTransitions(state, action));

		                    if (qValue > maxQValue) {
		                        maxQValue = qValue;
		                        policy.policy.put(state, action);
		                        policyChanged = true; // Policy changed in this iteration
		                    }
		                }
		            }
		        }
		    }

		    return policy;
		}

		
		


	/**
	 * This method solves the mdp using your implementation of {@link ValueIterationAgent#extractPolicy} and
	 * {@link ValueIterationAgent#iterate}. 
	 */
	public void train()
	{
		/**
		 * First run value iteration
		 */
		this.iterate();
		/**
		 * now extract policy from the values in {@link ValueIterationAgent#valueFunction} and set the agent's policy 
		 *  
		 */
		
		super.policy=extractPolicy();
		
		if (this.policy==null)
		{
			System.out.println("Unimplemented methods! First implement the iterate() & extractPolicy() methods");
			//System.exit(1);
		}
		
		
		
	}

	public static void main(String a[]) throws IllegalMoveException
	{
		//Test method to play the agent against a human agent.
		ValueIterationAgent agent=new ValueIterationAgent();
		HumanAgent d=new HumanAgent();
		
		Game g=new Game(agent, d, d);
		g.playOut();
		
		
		

		
		
	}
}
