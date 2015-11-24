package pacman.controllers.examples.algorithms;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Random;

import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class Perceptron {
	/*
	 * As far as I understand it, a perceptron models a neuron with multiple inputs being summed
	 * and evaluated by some function to result in an output.
	 * 
	 * To apply this to PacMan, for each of the four moves { LEFT, RIGHT, UP, DOWN }, 10 movesets will
	 * be randomly created. Each set belonging to a move will be inputed to a neuron that will output the best
	 * moveset (based on heuristic).
	 * 
	 * The best moveset from each move will be inputed to another neuron that will decide the final &
	 * best MOVE.
	 */
	private class MoveSet {
		public MOVE[] moves;
		public int heuristic;
		public MoveSet(MOVE moveType, int depth) {
			moves = new MOVE[depth];
			moves[0] = moveType;
			Random random = new Random(System.currentTimeMillis());
			for (int i = 1; i < depth; i++) {
				switch(random.nextInt(4)) {
				case 0:
					moves[i] = MOVE.LEFT;
					break;
				case 1:
					moves[i] = MOVE.RIGHT;
					break;
				case 2:
					moves[i] = MOVE.UP;
					break;
				case 3:
					moves[i] = MOVE.DOWN;
					break;
				}
			}
		}
	}
	
	public MOVE getMove(Game gameState, EnumMap<GHOST, MOVE> ghostMoves, int depth) {
		ArrayList<MoveSet> leftMoveSets = new ArrayList<MoveSet>(10);
		ArrayList<MoveSet> rightMoveSets = new ArrayList<MoveSet>(10);
		ArrayList<MoveSet> upMoveSets = new ArrayList<MoveSet>(10);
		ArrayList<MoveSet> downMoveSets = new ArrayList<MoveSet>(10);
		for (int i = 0; i < 10; i++) leftMoveSets.add(new MoveSet(MOVE.LEFT, depth));
		for (int i = 0; i < 10; i++) rightMoveSets.add(new MoveSet(MOVE.RIGHT, depth));
		for (int i = 0; i < 10; i++) upMoveSets.add(new MoveSet(MOVE.UP, depth));
		for (int i = 0; i < 10; i++) downMoveSets.add(new MoveSet(MOVE.DOWN, depth));
		MoveSet bestLeftMoveSet = getBestMoveSet(gameState, ghostMoves, leftMoveSets);
		MoveSet bestRightMoveSet = getBestMoveSet(gameState, ghostMoves, rightMoveSets);
		MoveSet bestUpMoveSet = getBestMoveSet(gameState, ghostMoves, upMoveSets);
		MoveSet bestDownMoveSet = getBestMoveSet(gameState, ghostMoves, downMoveSets);
		
		return getBestMoveSet(bestLeftMoveSet, bestRightMoveSet, bestUpMoveSet, bestDownMoveSet).moves[0];
	}
	
	// mimics a neuron to output (return best moveset)
	MoveSet getBestMoveSet(Game gameState, EnumMap<GHOST, MOVE> ghostMoves, ArrayList<MoveSet> moveSets) {
		int bestHeuristic = Integer.MIN_VALUE;
		MoveSet bestMoveSet = null;
		
		for (MoveSet moveSet : moveSets) {
			Game copy = gameState.copy();
			for (MOVE move : moveSet.moves) {
				copy.advanceGame(move, ghostMoves);
			}
			moveSet.heuristic = Evaluation.evaluateGameState(copy);
			if (moveSet.heuristic > bestHeuristic) {
				bestHeuristic = moveSet.heuristic;
				bestMoveSet = moveSet;
			}
		}
		
		return bestMoveSet;
	}
	
	// mimics a neuron to output the best MoveSet for each of the 4 moves
	MoveSet getBestMoveSet(MoveSet leftMoveSet, MoveSet rightMoveSet, MoveSet upMoveSet, MoveSet downMoveSet) {
		int bestHeuristic = leftMoveSet.heuristic;
		MoveSet bestMoveSet = leftMoveSet;
		
		if (rightMoveSet.heuristic > bestHeuristic) {
			bestHeuristic = rightMoveSet.heuristic;
			bestMoveSet = rightMoveSet;
		}
		if (upMoveSet.heuristic > bestHeuristic) {
			bestHeuristic = upMoveSet.heuristic;
			bestMoveSet = upMoveSet;
		}
		if (downMoveSet.heuristic > bestHeuristic) {
			bestHeuristic = downMoveSet.heuristic;
			bestMoveSet = downMoveSet;
		}
		
		return bestMoveSet;
	}
}
