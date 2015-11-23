package pacman.controllers.examples.algorithms;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedList;

import pacman.controllers.examples.StarterPacMan;
import pacman.controllers.examples.move.Node;
import pacman.controllers.examples.move.Tree;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class BreadthFirstSearch {
	public MOVE getMove(EnumMap<GHOST, MOVE> ghostMoves, Tree tree) {
		long start = System.currentTimeMillis();
		LinkedList<Node> nodes = new LinkedList<Node>();
		nodes.add(tree.getHeadNode());
		
		int leftValue = Integer.MIN_VALUE;
		int rightValue = Integer.MIN_VALUE;
		int upValue = Integer.MIN_VALUE;
		int downValue = Integer.MIN_VALUE;
		
		while(!nodes.isEmpty()) {
			Node node = nodes.removeFirst();
			node.setVisited(true);
			if (node.getPredecessor() != null) { // regular Node
				// set gameState and advance move based on current node
				Game gameState = node.getPredecessor().getGameState().copy();
				gameState.advanceGame(node.getMove(), ghostMoves);
				node.setGameState(gameState);
			}
			if (node.getNeighbors() == null) { // end of tree branch
				int value = StarterPacMan.evaluateGameState(node.getGameState());
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
				// add neighbors to be searched
				ArrayList<Node> neighbors = node.getNeighbors();
				for (Node neighbor : neighbors) {
					if (!neighbor.isVisited()) nodes.add(neighbor);
				}
			}
		}
		
		if (StarterPacMan.LOG_TIME) System.out.println(System.currentTimeMillis() - start);
		return StarterPacMan.getBestMove(leftValue, rightValue, upValue, downValue);
	}
}
