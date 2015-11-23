package pacman.controllers.examples;

import java.util.EnumMap;

import pacman.controllers.Controller;
import pacman.controllers.examples.algorithms.Alphabeta;
import pacman.controllers.examples.algorithms.BreadthFirstSearch;
import pacman.controllers.examples.algorithms.DepthFirstSearch;
import pacman.controllers.examples.algorithms.EvolutionStrategy;
import pacman.controllers.examples.algorithms.GeneticAlgorithm;
import pacman.controllers.examples.algorithms.HillClimber;
import pacman.controllers.examples.algorithms.IterativeDeepening;
import pacman.controllers.examples.algorithms.SimulatedAnnealing;
import pacman.controllers.examples.move.Tree;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/*
 * Pac-Man controller as part of the starter package - simply upload this file as a zip called
 * MyPacMan.zip and you will be entered into the rankings - as simple as that! Feel free to modify 
 * it or to start from scratch, using the classes supplied with the original software. Best of luck!
 * 
 * This controller utilises 3 tactics, in order of importance:
 * 1. Get away from any non-edible ghost that is in close proximity
 * 2. Go after the nearest edible ghost
 * 3. Go to the nearest pill/power pill
 */
public class StarterPacMan extends Controller<MOVE> {
	// for logging
	public static final boolean LOG_TIME = true;
	private static final boolean LOG_HEURISTICS = false;
	
	// the min ghost distance needs to be balanced
	// too large and pacman will think its trapped when its not and just jiggle in place
	// too small and pacman will not see ghosts and get itself trapped
	private static final int MIN_GHOST_DISTANCE = 15;
	private static final int MIN_EDIBLE_GHOST_DISTANCE = 100;
	private static final int DEPTH = 5;
	
	// algorithms
	private Alphabeta alphabeta;
	private BreadthFirstSearch breadthFirstSearch;
	private DepthFirstSearch depthFirstSearch;
	private EvolutionStrategy evolutionStrategy;
	private GeneticAlgorithm geneticAlgorithm;
	private HillClimber hillClimber;
	private IterativeDeepening iterativeDeepening;
	private SimulatedAnnealing simulatedAnnealing;
	
	public StarterPacMan() {
		alphabeta = new Alphabeta();
		breadthFirstSearch = new BreadthFirstSearch();
		depthFirstSearch = new DepthFirstSearch();
		evolutionStrategy = new EvolutionStrategy();
		geneticAlgorithm = new GeneticAlgorithm();
		hillClimber = new HillClimber();
		iterativeDeepening = new IterativeDeepening();
		simulatedAnnealing = new SimulatedAnnealing();
	}
	
	public MOVE getMove(Game game, long timeDue) {
		// assume ghosts are moving in same direction
		EnumMap<GHOST, MOVE> ghostMoves = new EnumMap<GHOST, MOVE>(GHOST.class);
		ghostMoves.put(GHOST.BLINKY, game.getGhostLastMoveMade(GHOST.BLINKY));
		ghostMoves.put(GHOST.INKY, game.getGhostLastMoveMade(GHOST.INKY));
		ghostMoves.put(GHOST.PINKY, game.getGhostLastMoveMade(GHOST.PINKY));
		ghostMoves.put(GHOST.SUE, game.getGhostLastMoveMade(GHOST.SUE));
		
		Tree tree = new Tree(DEPTH);
		tree.getHeadNode().setGameState(game);
		
		//return breadthFirstSearch.getMove(ghostMoves, tree);
		//return depthFirstSearch.getMove(ghostMoves, tree);
		//return iterativeDeepening.getMove(game, ghostMoves, DEPTH);
		//return hillClimber.getMove(ghostMoves, tree);
		//return alphabeta.getMove(game, ghostMoves, DEPTH);
		//return simulatedAnnealing.getMove(ghostMoves, tree);
		//return evolutionStrategy.getMove(game, ghostMoves, DEPTH);
		return geneticAlgorithm.getMove(game, ghostMoves, DEPTH);
	}
	
	/* Evaluates game state
	 * Higher score when:
	 * score is high
	 * number of lives is high
	 * distance to pill is small
	 * chasing edible ghost
	 * running from nearby non-edible ghost
	 */
	public static int evaluateGameState(Game gameState) {
		int pacmanNode = gameState.getPacmanCurrentNodeIndex();
		
		int heuristic = 0;
		
		int shortestEdibleGhostDistance = Integer.MAX_VALUE;
		int shortestGhostDistance = Integer.MAX_VALUE;
		int secondShortestGhostDistance = Integer.MAX_VALUE;
		
		for (GHOST ghost : GHOST.values()) {
			// ghost still in lair, will return -1 and skew distance results
			if (gameState.getGhostLairTime(ghost) > 0) continue;
			
			int distance = gameState.getShortestPathDistance(pacmanNode,
					gameState.getGhostCurrentNodeIndex(ghost));
			
			if (gameState.isGhostEdible(ghost)) {
				if (distance < shortestEdibleGhostDistance) {
					shortestEdibleGhostDistance = distance;
				}
			} else {
				if (distance < shortestGhostDistance) {
					secondShortestGhostDistance = shortestGhostDistance;
					shortestGhostDistance = distance;
				}
			}
		}
		
		//System.out.println(String.format("SGD/SEGD: %d, %d", shortestGhostDistance, shortestEdibleGhostDistance));
		
		if (shortestGhostDistance != Integer.MAX_VALUE && shortestGhostDistance != -1
				&& shortestGhostDistance < MIN_GHOST_DISTANCE) {
			if (secondShortestGhostDistance != Integer.MAX_VALUE && secondShortestGhostDistance != 1
					&& secondShortestGhostDistance < MIN_GHOST_DISTANCE) {
				// increase heuristic the farther pacman is from the average of the two nearest ghost
				float avgGhostDistance = (shortestGhostDistance + secondShortestGhostDistance) / 2f;
				heuristic += avgGhostDistance * 10000;
			} else {
				// increase heuristic the farther pacman is from the nearest ghost
				heuristic += shortestGhostDistance * 10000;
			}
		} else {
			// add reward for no ghosts nearby
			// this prevents pacman from staying near MIN_GHOST_DISTANCE to increase heuristic
			heuristic += (MIN_GHOST_DISTANCE + 10) * 10000;
		}
		// comment out shortestEdibleGhostDistance code to get level completing pacman
		// leave it to get aggressive pacman
//		if (shortestEdibleGhostDistance != Integer.MAX_VALUE && shortestEdibleGhostDistance != -1
//				&& shortestEdibleGhostDistance < MIN_EDIBLE_GHOST_DISTANCE) {
//			// multiplier needs to be high
//			// otherwise it might be better to be near an edible ghost than to eat it :/
//			heuristic += (MIN_EDIBLE_GHOST_DISTANCE - shortestEdibleGhostDistance) * 130;
//		}
		// no else because there is no incentive to not be near edible ghost
		
		int[] activePillIndices = gameState.getActivePillsIndices();
		int[] activePowerPillIndices = gameState.getActivePowerPillsIndices();
		int[] pillIndices = new int[activePillIndices.length + activePowerPillIndices.length];
		System.arraycopy(activePillIndices, 0, pillIndices, 0, activePillIndices.length);
		System.arraycopy(activePowerPillIndices, 0, pillIndices, activePillIndices.length, activePowerPillIndices.length);
		
		int shortestPillDistance =  gameState.getShortestPathDistance(pacmanNode,
				gameState.getClosestNodeIndexFromNodeIndex(pacmanNode, pillIndices, DM.PATH));
		
		return heuristic + gameState.getScore() * 100 + gameState.getPacmanNumberOfLivesRemaining() * 1000000 + (200 - shortestPillDistance);
	}
	
	public static MOVE getBestMove(int leftValue, int rightValue, int upValue, int downValue) {
		if (LOG_HEURISTICS) System.out.println(String.format("L/R/U/D: %d, %d, %d, %d", leftValue, rightValue, upValue, downValue));
		
		MOVE bestMove = MOVE.NEUTRAL;
		int bestValue = Integer.MIN_VALUE;
		if (leftValue != Integer.MIN_VALUE && leftValue > bestValue) {
			bestMove = MOVE.LEFT;
			bestValue = leftValue;
		}
		if (rightValue != Integer.MIN_VALUE && rightValue > bestValue) {
			bestMove = MOVE.RIGHT;
			bestValue = rightValue;
		}
		if (upValue != Integer.MIN_VALUE && upValue > bestValue) {
			bestMove = MOVE.UP;
			bestValue = upValue;
		}
		if (downValue != Integer.MIN_VALUE && downValue > bestValue) {
			bestMove = MOVE.DOWN;
			bestValue = downValue;
		}
		
		return bestMove;
	}
}