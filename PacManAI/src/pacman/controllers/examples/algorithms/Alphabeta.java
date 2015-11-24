package pacman.controllers.examples.algorithms;

import java.util.EnumMap;

import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class Alphabeta {
	int getBestHeuristicAlphabeta(Game gameState, EnumMap<GHOST, MOVE> ghostMoves, boolean maximizingPlayer, int alpha, int beta, int depth) {
		if (depth == 0) return Evaluation.evaluateGameState(gameState);
		
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
	
	public MOVE getMove(Game game, EnumMap<GHOST, MOVE> ghostMoves, int depth) {
		// this is the driver function and the first maximizing step, since the
		// player is choosing the highest value here to get the best move
		long start = System.currentTimeMillis();
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
		
		if (Evaluation.LOG_TIME) System.out.println(System.currentTimeMillis() - start);
		return Evaluation.getBestMove(leftValue, rightValue, upValue, downValue);
	}
}
