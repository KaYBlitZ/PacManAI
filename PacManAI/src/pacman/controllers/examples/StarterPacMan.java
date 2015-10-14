package pacman.controllers.examples;

import java.util.ArrayList;
import java.util.EnumMap;

import pacman.controllers.Controller;
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
	private static final int INITIAL_VALUE = -1000;

	public MOVE getMove(Game game, long timeDue) {
		return getMoveBFS(game);
	}
	
	MOVE getMoveBFS(Game game) {
		// assume ghosts are moving in same direction
		EnumMap<GHOST, MOVE> ghostMoves = new EnumMap<GHOST, MOVE>(GHOST.class);
		ghostMoves.put(GHOST.BLINKY, game.getGhostLastMoveMade(GHOST.BLINKY));
		ghostMoves.put(GHOST.INKY, game.getGhostLastMoveMade(GHOST.INKY));
		ghostMoves.put(GHOST.PINKY, game.getGhostLastMoveMade(GHOST.PINKY));
		ghostMoves.put(GHOST.SUE, game.getGhostLastMoveMade(GHOST.SUE));
		
		int leftValue = INITIAL_VALUE;
		int rightValue = INITIAL_VALUE;
		int upValue = INITIAL_VALUE;
		int downValue = INITIAL_VALUE;
		
		for (MOVE move : game.getPossibleMoves(game.getPacmanCurrentNodeIndex())) {
			Game copy = game.copy();
			if (move == MOVE.LEFT) {
				for (int i = 0; i < 8; i++) copy.advanceGame(MOVE.LEFT, ghostMoves);
				leftValue = evaluateGameState(copy, game.getNumberOfActivePills(), game.getNumberOfActivePowerPills());
			} else if (move == MOVE.RIGHT) {
				for (int i = 0; i < 8; i++) copy.advanceGame(MOVE.RIGHT, ghostMoves);
				rightValue = evaluateGameState(copy, game.getNumberOfActivePills(), game.getNumberOfActivePowerPills());
			} else if (move == MOVE.DOWN) {
				for (int i = 0; i < 8; i++) copy.advanceGame(MOVE.DOWN, ghostMoves);
				downValue = evaluateGameState(copy, game.getNumberOfActivePills(), game.getNumberOfActivePowerPills());
			} else if (move == MOVE.UP) {
				for (int i = 0; i < 8; i++) copy.advanceGame(MOVE.UP, ghostMoves);
				upValue = evaluateGameState(copy, game.getNumberOfActivePills(), game.getNumberOfActivePowerPills());
			}
		}
		
		//System.out.println(String.format("%d, %d, %d, %d", leftValue, rightValue, upValue, downValue));
		
		MOVE bestMove = null;
		int bestValue = -10000;
		if (leftValue != INITIAL_VALUE && leftValue > bestValue) {
			bestMove = MOVE.LEFT;
			bestValue = leftValue;
		}
		if (rightValue != INITIAL_VALUE && rightValue > bestValue) {
			bestMove = MOVE.RIGHT;
			bestValue = rightValue;
		}
		if (upValue != INITIAL_VALUE && upValue > bestValue) {
			bestMove = MOVE.UP;
			bestValue = upValue;
		}
		if (downValue != INITIAL_VALUE && downValue > bestValue) {
			bestMove = MOVE.DOWN;
			bestValue = downValue;
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
	int evaluateGameState(Game gameState, int currentNumPills, int currentNumPowerPills) {
		int pacmanIndex = gameState.getPacmanCurrentNodeIndex();
		int[] ghostIndices = new int[] {
			gameState.getGhostCurrentNodeIndex(GHOST.BLINKY),
			gameState.getGhostCurrentNodeIndex(GHOST.INKY),
			gameState.getGhostCurrentNodeIndex(GHOST.PINKY),
			gameState.getGhostCurrentNodeIndex(GHOST.SUE)
		};
		
		int shortestGhostDistance = Integer.MAX_VALUE;
		for (int ghostIndex : ghostIndices) {
			int distance = gameState.getShortestPathDistance(pacmanIndex, ghostIndex);
			if (distance < shortestGhostDistance) {
				shortestGhostDistance = distance;
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
		
		return gameState.getScore() + 
				100 * gameState.getPacmanNumberOfLivesRemaining() +
				(500 - shortestPillDistance) + 
				shortestGhostDistance;
	}
}