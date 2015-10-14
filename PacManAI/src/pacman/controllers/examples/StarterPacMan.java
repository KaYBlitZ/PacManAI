package pacman.controllers.examples;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Random;

import pacman.controllers.Controller;
import pacman.controllers.examples.move.Node;
import pacman.controllers.examples.move.Tree;
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
	private static final int INVALID_MOVE = -10000;
	private static final int MIN_GHOST_DISTANCE = 30;

	public MOVE getMove(Game game, long timeDue) {
		return getMoveBFS(game, 5);
	}
	
	MOVE getMoveBFS(Game game, int depth) {
		Tree tree = new Tree(depth);
		
		LinkedList<Node> nodes = new LinkedList<Node>();
		nodes.add(tree.getHeadNode());
		
		// assume ghosts are moving in same direction
		EnumMap<GHOST, MOVE> ghostMoves = new EnumMap<GHOST, MOVE>(GHOST.class);
		ghostMoves.put(GHOST.BLINKY, game.getGhostLastMoveMade(GHOST.BLINKY));
		ghostMoves.put(GHOST.INKY, game.getGhostLastMoveMade(GHOST.INKY));
		ghostMoves.put(GHOST.PINKY, game.getGhostLastMoveMade(GHOST.PINKY));
		ghostMoves.put(GHOST.SUE, game.getGhostLastMoveMade(GHOST.SUE));
		
		int leftValue = INVALID_MOVE;
		int rightValue = INVALID_MOVE;
		int upValue = INVALID_MOVE;
		int downValue = INVALID_MOVE;
		
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
			} else { // head node
				// prune invalid moves (neighbors)
				MOVE[] moves = game.getPossibleMoves(game.getPacmanCurrentNodeIndex());
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
		
		System.out.println(String.format("L/R/U/D: %d, %d, %d, %d", leftValue, rightValue, upValue, downValue));
		
		Random rand = new Random();
		MOVE bestMove = MOVE.LEFT;
		int bestValue = Integer.MIN_VALUE;
		if (leftValue != INVALID_MOVE) {
			if (leftValue > bestValue) {
				bestMove = MOVE.LEFT;
				bestValue = leftValue;
			} else if (leftValue == bestValue) {
				if (rand.nextInt(2) == 0) {
					bestMove = MOVE.LEFT;
					bestValue = leftValue;
				}
			}
		}
		if (rightValue != INVALID_MOVE) {
			if (rightValue > bestValue) {
				bestMove = MOVE.RIGHT;
				bestValue = rightValue;
			} else if (rightValue == bestValue) {
				if (rand.nextInt(2) == 0) {
					bestMove = MOVE.RIGHT;
					bestValue = rightValue;
				}
			}
		}
		if (upValue != INVALID_MOVE) {
			if (upValue > bestValue) {
				bestMove = MOVE.UP;
				bestValue = upValue;
			} else if (upValue == bestValue) {
				if (rand.nextInt(2) == 0) {
					bestMove = MOVE.UP;
					bestValue = upValue;
				}
			}
		}
		if (downValue != INVALID_MOVE) {
			if (downValue > bestValue) {
				bestMove = MOVE.DOWN;
				bestValue = downValue;
			} else if (downValue == bestValue) {
				if (rand.nextInt(2) == 0) {
					bestMove = MOVE.DOWN;
					bestValue = downValue;
				}
			}
		}
		
		return bestMove;
	}
	
	/* Evaluates game state
	 * Higher score when:
	 * more pills eaten
	 * more power pills eaten
	 * more remaining lives
	 * closer to nearest edible ghost
	 * closer to nearest pill/power pill
	 */
	int evaluateGameState(Game gameState) {
		int heuristic = 0;
		int pacmanIndex = gameState.getPacmanCurrentNodeIndex();
		int[] ghostIndices = new int[] {
			gameState.getGhostCurrentNodeIndex(GHOST.BLINKY),
			gameState.getGhostCurrentNodeIndex(GHOST.INKY),
			gameState.getGhostCurrentNodeIndex(GHOST.PINKY),
			gameState.getGhostCurrentNodeIndex(GHOST.SUE)
		};
		
		int shortestGhostDistance = Integer.MAX_VALUE;
		GHOST nearestGhost = null;
		for (int ghostIndex : ghostIndices) {
			int distance = gameState.getShortestPathDistance(pacmanIndex, ghostIndex);
			if (distance < shortestGhostDistance) {
				shortestGhostDistance = distance;
				switch (ghostIndex) {
				case 0:
					nearestGhost = GHOST.BLINKY;
					break;
				case 1:
					nearestGhost = GHOST.INKY;
					break;
				case 2:
					nearestGhost = GHOST.PINKY;
					break;
				case 3:
					nearestGhost = GHOST.SUE;
					break;
				}
			}
		}
		
		if (shortestGhostDistance < MIN_GHOST_DISTANCE) {
			if (nearestGhost != null) {
				if (gameState.isGhostEdible(nearestGhost)) {
					heuristic += 200;
				} else {
					heuristic += -200;
				}
			}
		}
		
		int[] activePillIndices = gameState.getActivePillsIndices();
		int[] activePowerPillIndices = gameState.getActivePowerPillsIndices();
		int[] pillIndices = new int[activePillIndices.length + activePowerPillIndices.length];
		System.arraycopy(activePillIndices, 0, pillIndices, 0, activePillIndices.length);
		System.arraycopy(activePowerPillIndices, 0, pillIndices, activePillIndices.length, activePowerPillIndices.length);
		
		int shortestPillDistance = Integer.MAX_VALUE;
		for (int pillIndex : pillIndices) {
			int distance = gameState.getShortestPathDistance(pacmanIndex, pillIndex);
			if (distance < shortestPillDistance) {
				shortestPillDistance = distance;
			}
		}
		
		return heuristic + gameState.getScore() + 
				2000 * gameState.getPacmanNumberOfLivesRemaining() +
				(300 - shortestPillDistance);
	}
}