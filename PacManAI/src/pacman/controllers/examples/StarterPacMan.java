package pacman.controllers.examples;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Random;

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
	private static final int MIN_GHOST_DISTANCE = 20;
	private static final int MIN_EDIBLE_GHOST_DISTANCE = 100;
	
	public MOVE getMove(Game game, long timeDue) {
		return getMoveBFS(game, 8);
	}
	
	MOVE getMoveBFS(Game game, int depth) {
		Tree tree = new Tree(depth);
		
		LinkedList<Node> nodes = new LinkedList<Node>();
		nodes.add(tree.getHeadNode());
		
		// assume ghosts are moving in same direction
		EnumMap<GHOST, MOVE> ghostMoves = new EnumMap<GHOST, MOVE>(GHOST.class);
		ghostMoves.put(GHOST.BLINKY, MOVE.NEUTRAL);
		ghostMoves.put(GHOST.INKY, MOVE.NEUTRAL);
		ghostMoves.put(GHOST.PINKY, MOVE.NEUTRAL);
		ghostMoves.put(GHOST.SUE, MOVE.NEUTRAL);
		
		int leftValue = Integer.MIN_VALUE;
		int rightValue = Integer.MIN_VALUE;
		int upValue = Integer.MIN_VALUE;
		int downValue = Integer.MIN_VALUE;
		
		while(!nodes.isEmpty()) {
			Node node = nodes.removeFirst();
			if (node.getMove() != MOVE.NEUTRAL) { // regular Node
				// set gameState and advance move based on current node
				Game gameState = node.getPredecessor().getGameState().copy();
				gameState.advanceGame(node.getMove(), ghostMoves);
				node.setGameState(gameState);
			} else { // must be head node
				// set the current game state
				node.setGameState(game);
			}
			if (node.getNeighbors() == null) {
				// end of tree branch
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
				MOVE[] moves = game.getPossibleMoves(node.getGameState().getPacmanCurrentNodeIndex());
				ArrayList<Node> neighbors = node.getNeighbors();
				for (MOVE move : moves) {
					if (move == MOVE.LEFT) {
						nodes.add(neighbors.get(0));
					} else if (move == MOVE.RIGHT) {
						nodes.add(neighbors.get(1));
					} else if (move == MOVE.UP) {
						nodes.add(neighbors.get(2));
					} else if (move == MOVE.DOWN) {
						nodes.add(neighbors.get(3));
					}
				}
			}
		}
		
		//System.out.println(String.format("L/R/U/D: %d, %d, %d, %d", leftValue, rightValue, upValue, downValue));
		
		MOVE bestMove = MOVE.NEUTRAL;
		int bestValue = Integer.MIN_VALUE;
		if (leftValue != Integer.MIN_VALUE) {
			if (leftValue > bestValue) {
				bestMove = MOVE.LEFT;
				bestValue = leftValue;
			}
		}
		if (rightValue != Integer.MIN_VALUE) {
			if (rightValue > bestValue) {
				bestMove = MOVE.RIGHT;
				bestValue = rightValue;
			}
		}
		if (upValue != Integer.MIN_VALUE) {
			if (upValue > bestValue) {
				bestMove = MOVE.UP;
				bestValue = upValue;
			}
		}
		if (downValue != Integer.MIN_VALUE) {
			if (downValue > bestValue) {
				bestMove = MOVE.DOWN;
				bestValue = downValue;
			}
		}
		
		return bestMove;
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
					shortestGhostDistance = distance;
				}
			}
		}
		
		//System.out.println(String.format("SGD/SEGD: %d, %d", shortestGhostDistance, shortestEdibleGhostDistance));
		
		if (shortestGhostDistance != Integer.MAX_VALUE && shortestGhostDistance != -1
				&& shortestGhostDistance < MIN_GHOST_DISTANCE) {
				// increase heuristic the farther pacman is from the nearest ghost
				heuristic += shortestGhostDistance * 10000;
		} else {
			// add reward for no ghosts nearby
			// this prevents pacman from staying near MIN_GHOST_DISTANCE to increase heuristic
			heuristic += MIN_GHOST_DISTANCE * 10000;
		}
		if (shortestEdibleGhostDistance != Integer.MAX_VALUE && shortestEdibleGhostDistance != -1
				&& shortestEdibleGhostDistance < MIN_EDIBLE_GHOST_DISTANCE) {
			// multiplier needs to be high
			// otherwise it might be better to be near an edible ghost than to eat it :/
			heuristic -= shortestEdibleGhostDistance * 1300;
		} else {
			// decrease for no edible ghosts nearby
			// this adds incentive to get closer to edible ghosts
			heuristic -= MIN_EDIBLE_GHOST_DISTANCE * 1300;
		}
		
		int[] activePillIndices = gameState.getActivePillsIndices();
		int[] activePowerPillIndices = gameState.getActivePowerPillsIndices();
		int[] pillIndices = new int[activePillIndices.length + activePowerPillIndices.length];
		System.arraycopy(activePillIndices, 0, pillIndices, 0, activePillIndices.length);
		System.arraycopy(activePowerPillIndices, 0, pillIndices, activePillIndices.length, activePowerPillIndices.length);
		
		int shortestPillDistance =  gameState.getShortestPathDistance(pacmanNode,
				gameState.getClosestNodeIndexFromNodeIndex(pacmanNode, pillIndices, DM.PATH));
		
		return heuristic + gameState.getScore() * 1000 + gameState.getPacmanNumberOfLivesRemaining() * 100000000 - shortestPillDistance;
	}
}