package pacman.controllers.examples.algorithms;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.PriorityQueue;

import pacman.controllers.examples.StarterPacMan;
import pacman.controllers.examples.move.Node;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class EvolutionStrategy {
	/*
	 * Depth is the number of generations.
	 * Each generation will have the best fitness (best heuristic) game state mutate by advancing
	 * the state with a MOVE. After depth number of generations, return MOVE leading to best fitness game state.
	 */
	public MOVE getMove(Game game, EnumMap<GHOST, MOVE> ghostMoves, int depth) {
		long start = System.currentTimeMillis();
		PriorityQueue<Node> population = new PriorityQueue<Node>(25, new NodeComparator());
		
		// initializing population; counts as one depth
		for (int i = 0; i < 4; i++) {
			Node childNode = new Node();
			Game gameState = game.copy();
			switch(i) {
			case 0:
				gameState.advanceGame(MOVE.LEFT, ghostMoves);
				childNode.setGameState(gameState);
				childNode.setMove(MOVE.LEFT); // sets the move to this node
				break;
			case 1:
				gameState.advanceGame(MOVE.RIGHT, ghostMoves);
				childNode.setGameState(gameState);
				childNode.setMove(MOVE.RIGHT); // sets the move to this node
				break;
			case 2:
				gameState.advanceGame(MOVE.UP, ghostMoves);
				childNode.setGameState(gameState);
				childNode.setMove(MOVE.UP); // sets the move to this node
				break;
			case 3:
				gameState.advanceGame(MOVE.DOWN, ghostMoves);
				childNode.setGameState(gameState);
				childNode.setMove(MOVE.DOWN); // sets the move to this node
				break;
			}
			population.add(childNode);
		}
		
		// mutate for depth - 1 times
		for (int i = 0; i < depth - 1; i++) {
			Node bestNode = population.peek();
			for (int j = 0; j < 4; j++) {
				Node childNode = new Node();
				Game gameState = bestNode.getGameState().copy();
				switch(j) {
				case 0:
					gameState.advanceGame(MOVE.LEFT, ghostMoves);
					childNode.setGameState(gameState);
					childNode.setMove(bestNode.getMove());
					break;
				case 1:
					gameState.advanceGame(MOVE.RIGHT, ghostMoves);
					childNode.setGameState(gameState);
					childNode.setMove(bestNode.getMove());
					break;
				case 2:
					gameState.advanceGame(MOVE.UP, ghostMoves);
					childNode.setGameState(gameState);
					childNode.setMove(bestNode.getMove());
					break;
				case 3:
					gameState.advanceGame(MOVE.DOWN, ghostMoves);
					childNode.setGameState(gameState);
					childNode.setMove(bestNode.getMove());
					break;
				}
				population.add(childNode);
			}
		}
		
		if (StarterPacMan.LOG_TIME) System.out.println(System.currentTimeMillis() - start);
		return population.peek().getMove();
	}
	
	private class NodeComparator implements Comparator<Node> {
		// higher heuristic values get higher priority
		@Override
		public int compare(Node firstNode, Node secondNode) {
			int firstHeuristic = StarterPacMan.evaluateGameState(firstNode.getGameState());
			int secondHeuristic = StarterPacMan.evaluateGameState(secondNode.getGameState());
			
			if (firstHeuristic < secondHeuristic) {
				return 1;
			} else if (firstHeuristic == secondHeuristic) {
				return 0;
			} else {
				return -1;
			}
		}
	}
}
