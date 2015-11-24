package pacman.controllers.examples.algorithms;

import java.util.ArrayList;
import java.util.EnumMap;

import pacman.controllers.examples.move.Node;
import pacman.controllers.examples.move.Tree;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class HillClimber {
	public MOVE getMove(EnumMap<GHOST, MOVE> ghostMoves, Tree tree) {
		long start = System.currentTimeMillis();
		
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
					leftValue = Evaluation.evaluateGameState(copy);
					break;
				case 1:
					copy.advanceGame(MOVE.RIGHT, ghostMoves);
					rightValue = Evaluation.evaluateGameState(copy);
					break;
				case 2:
					copy.advanceGame(MOVE.UP, ghostMoves);
					upValue = Evaluation.evaluateGameState(copy);
					break;
				case 3:
					copy.advanceGame(MOVE.DOWN, ghostMoves);
					downValue = Evaluation.evaluateGameState(copy);
					break;
				}
				neighbors.get(i).setGameState(copy);
			}
			
			int currentValue = Evaluation.evaluateGameState(maximaNode.getGameState());
			
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
		
		if (Evaluation.LOG_TIME) System.out.println(System.currentTimeMillis() - start);
		return maximaNode.getMove();
	}
}
