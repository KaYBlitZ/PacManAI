package pacman.controllers.examples.algorithms;

import java.util.ArrayList;
import java.util.EnumMap;

import pacman.controllers.examples.move.Node;
import pacman.controllers.examples.move.Tree;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class DepthFirstSearch {
	public MOVE getMove(EnumMap<GHOST, MOVE> ghostMoves, Tree tree) {
		long start = System.currentTimeMillis();
		ArrayList<Node> headNeighbors = tree.getHeadNode().getNeighbors();
		
		int leftValue = getBestValue(ghostMoves, headNeighbors.get(0));
		int rightValue = getBestValue(ghostMoves, headNeighbors.get(1));
		int upValue = getBestValue(ghostMoves, headNeighbors.get(2));
		int downValue = getBestValue(ghostMoves, headNeighbors.get(3));
		
		if (Evaluation.LOG_TIME) System.out.println(System.currentTimeMillis() - start);
		return Evaluation.getBestMove(leftValue, rightValue, upValue, downValue);
	}
	
	public int getBestValue(EnumMap<GHOST, MOVE> ghostMoves, Node node) {
		Game gameState = node.getPredecessor().getGameState().copy();
		gameState.advanceGame(node.getMove(), ghostMoves);
		node.setGameState(gameState);
		
		ArrayList<Node> neighbors = node.getNeighbors();
		if (neighbors == null) return Evaluation.evaluateGameState(gameState); // end of branch return heuristic
		
		int bestValue = Integer.MIN_VALUE;
		for (Node neighbor : neighbors) {
			int value = getBestValue(ghostMoves, neighbor);
			if (value > bestValue) bestValue = value;
		}
		
		return bestValue;
	}
}
