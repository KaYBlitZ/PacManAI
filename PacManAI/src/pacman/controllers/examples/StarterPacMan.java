package pacman.controllers.examples;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedList;

import pacman.controllers.Controller;
import pacman.controllers.examples.move.Node;
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
	// the min ghost distance needs to be balanced
	// too large and pacman will think its trapped when its not and just jiggle in place
	// too small and pacman will not see ghosts and get itself trapped
	private static final int MIN_GHOST_DISTANCE = 15;
	private static final int MIN_EDIBLE_GHOST_DISTANCE = 100;
	private static final int DEPTH = 5;
	
	public MOVE getMove(Game game, long timeDue) {
		// assume ghosts are moving in same direction
		EnumMap<GHOST, MOVE> ghostMoves = new EnumMap<GHOST, MOVE>(GHOST.class);
		ghostMoves.put(GHOST.BLINKY, game.getGhostLastMoveMade(GHOST.BLINKY));
		ghostMoves.put(GHOST.INKY, game.getGhostLastMoveMade(GHOST.INKY));
		ghostMoves.put(GHOST.PINKY, game.getGhostLastMoveMade(GHOST.PINKY));
		ghostMoves.put(GHOST.SUE, game.getGhostLastMoveMade(GHOST.SUE));
		
		//return getMoveBFS(game, ghostMoves, DEPTH);
		//return getMoveDepthFirstSearch(game, ghostMoves, DEPTH);
		//return getMoveIterativeDeepening(game, ghostMoves, DEPTH);
		//return getMoveHillClimber(game, ghostMoves, DEPTH);
		return getMoveAlphabeta(game, ghostMoves, DEPTH);
	}
	
	int getBestHeuristicAlphabeta(Game gameState, EnumMap<GHOST, MOVE> ghostMoves, boolean maximizingPlayer, int alpha, int beta, int depth) {
		if (depth == 0) return evaluateGameState(gameState);
		
		for (int i = 0; i < 4; i++) {			
			Game copy = gameState.copy();
			switch(i) {
			case 0:
				copy.advanceGame(MOVE.LEFT, ghostMoves);
				break;
			case 1:
				copy.advanceGame(MOVE.RIGHT, ghostMoves);
				break;
			case 2:
				copy.advanceGame(MOVE.UP, ghostMoves);
				break;
			case 3:
				copy.advanceGame(MOVE.DOWN, ghostMoves);
				break;
			}
			int heuristic = getBestHeuristicAlphabeta(copy, ghostMoves, !maximizingPlayer, alpha, beta, depth - 1);
			if (maximizingPlayer) {
				if (heuristic > alpha) alpha = heuristic;
				if (beta <= alpha) {
					// simply return, minimizing player will not choose this since alpha is greater than beta already
					return alpha;
				}
			} else { /// minimizingPlayer
				if (heuristic < beta) beta = heuristic;
				if (beta <= alpha) {
					// simply return, maximizing player will not choose this since beta is less than alpha
					return beta;
				}
			}
		}
		
		return (maximizingPlayer ? alpha : beta);
	}
	
	MOVE getMoveAlphabeta(Game game, EnumMap<GHOST, MOVE> ghostMoves, int depth) {
		// this is the driver function and the first maximizing step, since the
		// player is choosing the highest value here to get the best move
		int leftValue = 0, rightValue = 0, upValue = 0, downValue = 0;
		
		for (int i = 0; i < 4; i++) {			
			Game copy = game.copy();
			switch(i) {
			// pass false because this is the first maximizing step, so the next step is the minimizing player
			case 0:
				copy.advanceGame(MOVE.LEFT, ghostMoves);
				leftValue = getBestHeuristicAlphabeta(copy, ghostMoves, false, Integer.MIN_VALUE, Integer.MAX_VALUE, depth - 1);
				break;
			case 1:
				copy.advanceGame(MOVE.RIGHT, ghostMoves);
				rightValue = getBestHeuristicAlphabeta(copy, ghostMoves, false, Integer.MIN_VALUE, Integer.MAX_VALUE, depth - 1);
				break;
			case 2:
				copy.advanceGame(MOVE.UP, ghostMoves);
				upValue = getBestHeuristicAlphabeta(copy, ghostMoves, false, Integer.MIN_VALUE, Integer.MAX_VALUE, depth - 1);
				break;
			case 3:
				copy.advanceGame(MOVE.DOWN, ghostMoves);
				downValue = getBestHeuristicAlphabeta(copy, ghostMoves, false, Integer.MIN_VALUE, Integer.MAX_VALUE, depth - 1);
				break;
			}
		}
		
		return getBestMove(leftValue, rightValue, upValue, downValue);
	}
	
	MOVE getMoveHillClimber(Game game, EnumMap<GHOST, MOVE> ghostMoves, int depth) {
		Tree tree = new Tree(depth);
		tree.getHeadNode().setGameState(game);
		
		boolean isMaxima = false;
		Node maximaNode = tree.getHeadNode();
		while (!isMaxima) {
			int leftValue = 0, rightValue = 0, upValue = 0, downValue = 0;
			ArrayList<Node> neighbors = maximaNode.getNeighbors();
			if (neighbors == null) {
				// this is the largest depth and therefore the local maxima
				break;
			}
			
			for (int i = 0; i < 4; i++) {
				Game copy = maximaNode.getGameState().copy();
				switch(i) {
				case 0:
					copy.advanceGame(MOVE.LEFT, ghostMoves);
					leftValue = evaluateGameState(copy);
					break;
				case 1:
					copy.advanceGame(MOVE.RIGHT, ghostMoves);
					rightValue = evaluateGameState(copy);
					break;
				case 2:
					copy.advanceGame(MOVE.UP, ghostMoves);
					upValue = evaluateGameState(copy);
					break;
				case 3:
					copy.advanceGame(MOVE.DOWN, ghostMoves);
					downValue = evaluateGameState(copy);
					break;
				}
				neighbors.get(i).setGameState(copy);
			}
			
			int currentValue = evaluateGameState(maximaNode.getGameState());
			
			if (currentValue > leftValue && currentValue > rightValue &&
					currentValue > upValue && currentValue > downValue) {
				isMaxima = true;
			} else {
				if (leftValue > currentValue) {
					currentValue = leftValue;
					maximaNode = neighbors.get(0);
				}
				if (rightValue > currentValue) {
					currentValue = rightValue;
					maximaNode = neighbors.get(1);
				}
				if (upValue > currentValue) {
					currentValue = upValue;
					maximaNode = neighbors.get(2);
				}
				if (downValue > currentValue) {
					currentValue = downValue;
					maximaNode = neighbors.get(3);
				}
			}
		}
		
		// currentNode is a local maxima
		// get move to this local maxima value
		if (maximaNode == tree.getHeadNode()) return MOVE.NEUTRAL;
		while (maximaNode.getPredecessor().getMove() != MOVE.NEUTRAL) {
			maximaNode = maximaNode.getPredecessor();
		}
		return maximaNode.getMove();
	}
	
	// simulates iterative deepening; true iterative deepening is not possible
	// since we are not searching for a goal node
	MOVE getMoveIterativeDeepening(Game game, EnumMap<GHOST, MOVE> ghostMoves, int depth) {
		for (int i = 1; i <= depth; i++) {
			Tree tree = new Tree(i);
			tree.getHeadNode().setGameState(game);
			ArrayList<Node> headNeighbors = tree.getHeadNode().getNeighbors();
			
			int leftValue = getBestValueDepthFirstSearch(ghostMoves, headNeighbors.get(0));
			int rightValue = getBestValueDepthFirstSearch(ghostMoves, headNeighbors.get(1));
			int upValue = getBestValueDepthFirstSearch(ghostMoves, headNeighbors.get(2));
			int downValue = getBestValueDepthFirstSearch(ghostMoves, headNeighbors.get(3));
			
			if (i == depth) {
				return getBestMove(leftValue, rightValue, upValue, downValue);
			}
		}
		
		return null; // should never reach this point
	}
	
	int getBestValueDepthFirstSearch(EnumMap<GHOST, MOVE> ghostMoves, Node node) {
		Game gameState = node.getPredecessor().getGameState().copy();
		gameState.advanceGame(node.getMove(), ghostMoves);
		node.setGameState(gameState);
		
		ArrayList<Node> neighbors = node.getNeighbors();
		if (neighbors == null) return evaluateGameState(gameState); // end of branch return heuristic
		
		int bestValue = Integer.MIN_VALUE;
		for (Node neighbor : neighbors) {
			int value = getBestValueDepthFirstSearch(ghostMoves, neighbor);
			if (value > bestValue) bestValue = value;
		}
		
		return bestValue;
	}
	
	MOVE getMoveDepthFirstSearch(Game game, EnumMap<GHOST, MOVE> ghostMoves, int depth) {
		Tree tree = new Tree(depth);
		tree.getHeadNode().setGameState(game);
		ArrayList<Node> headNeighbors = tree.getHeadNode().getNeighbors();
		
		int leftValue = getBestValueDepthFirstSearch(ghostMoves, headNeighbors.get(0));
		int rightValue = getBestValueDepthFirstSearch(ghostMoves, headNeighbors.get(1));
		int upValue = getBestValueDepthFirstSearch(ghostMoves, headNeighbors.get(2));
		int downValue = getBestValueDepthFirstSearch(ghostMoves, headNeighbors.get(3));
		
		return getBestMove(leftValue, rightValue, upValue, downValue);
	}
	
	MOVE getMoveBFS(Game game, EnumMap<GHOST, MOVE> ghostMoves, int depth) {
		Tree tree = new Tree(depth);
		
		LinkedList<Node> nodes = new LinkedList<Node>();
		nodes.add(tree.getHeadNode());
		
		int leftValue = Integer.MIN_VALUE;
		int rightValue = Integer.MIN_VALUE;
		int upValue = Integer.MIN_VALUE;
		int downValue = Integer.MIN_VALUE;
		
		while(!nodes.isEmpty()) {
			Node node = nodes.removeFirst();
			node.setVisited(true);
			if (node.getMove() != MOVE.NEUTRAL) { // regular Node
				// set gameState and advance move based on current node
				Game gameState = node.getPredecessor().getGameState().copy();
				gameState.advanceGame(node.getMove(), ghostMoves);
				node.setGameState(gameState);
			} else { // must be head node
				// set the current game state
				node.setGameState(game);
			}
			if (node.getNeighbors() == null) { // end of tree branch
				int value = evaluateGameState(node.getGameState());
				// get head node move type
				Node nodeType = node.getPredecessor();
				while (nodeType.getPredecessor().getMove() != MOVE.NEUTRAL) {
					nodeType = nodeType.getPredecessor();
				}
				switch (nodeType.getMove()) {
				case LEFT:
					if (value > leftValue) leftValue = value;
					break;
				case RIGHT:
					if (value > rightValue) rightValue = value;
					break;
				case UP:
					if (value > upValue) upValue = value;
					break;
				case DOWN:
					if (value > downValue) downValue = value;
					break;
				case NEUTRAL:
					break;
				}
			} else { // regular node
				// prune invalid moves (neighbors)
//				MOVE[] moves = game.getPossibleMoves(node.getGameState().getPacmanCurrentNodeIndex());
//				ArrayList<Node> neighbors = node.getNeighbors();
//				for (MOVE move : moves) {
//					if (move == MOVE.LEFT && !neighbors.get(0).isVisited()) {
//						nodes.add(neighbors.get(0));
//					} else if (move == MOVE.RIGHT && !neighbors.get(1).isVisited()) {
//						nodes.add(neighbors.get(1));
//					} else if (move == MOVE.UP && !neighbors.get(2).isVisited()) {
//						nodes.add(neighbors.get(2));
//					} else if (move == MOVE.DOWN && !neighbors.get(3).isVisited()) {
//						nodes.add(neighbors.get(3));
//					}
//				}
				// add neighbors to be searched
				ArrayList<Node> neighbors = node.getNeighbors();
				for (Node neighbor : neighbors) {
					if (!neighbor.isVisited()) nodes.add(neighbor);
				}
			}
		}
		
		//System.out.println(String.format("L/R/U/D: %d, %d, %d, %d", leftValue, rightValue, upValue, downValue));
		
		return getBestMove(leftValue, rightValue, upValue, downValue);
	}
	
	/* Evaluates game state
	 * Higher score when:
	 * score is high
	 * number of lives is high
	 * distance to pill is small
	 * chasing edible ghost
	 * running from nearby non-edible ghost
	 */
	int evaluateGameState(Game gameState) {
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
	
	MOVE getBestMove(int leftValue, int rightValue, int upValue, int downValue) {
		//System.out.println(String.format("L/R/U/D: %d, %d, %d, %d", leftValue, rightValue, upValue, downValue));
		
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