package pacman.controllers.examples.algorithms;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Random;

import pacman.controllers.examples.move.Node;
import pacman.controllers.examples.move.Tree;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class SimulatedAnnealing {
	/*
	 * If heuristic is better then current node's heuristic then move to that node else only move there
	 * with probability e^(-|deltaHeuristic|/T) where T is the temperature.
	 * When T = 0; return MOVE leading to current node
	 */
	public MOVE getMove(EnumMap<GHOST, MOVE> ghostMoves, Tree tree) {
		long start = System.currentTimeMillis();
		float coolingRate = 0.97f;
		Node currentNode = tree.getHeadNode();
		
		Random rand = new Random(System.currentTimeMillis());
		int temperature = 4000000; // must initially be larger than max heuristic
		while (temperature != 0) {
			int currentHeuristic = Evaluation.evaluateGameState(currentNode.getGameState());
			int difference;
			if (currentNode.getPredecessor() != null) {
				int pastHeuristic = Evaluation.evaluateGameState(currentNode.getPredecessor().getGameState());
				difference = pastHeuristic - currentHeuristic;
				if (difference > 0) {
					// pastHeuristic is higher, go to that node
					currentNode = currentNode.getPredecessor();
					temperature *= coolingRate;
					continue;
				} else {
					// pastHeuristic is equal or lower, go to that node if percentage is met
					if (rand.nextFloat() < Math.exp(difference / temperature)) {
						currentNode = currentNode.getPredecessor();
						temperature *= coolingRate;
						continue;
					}
				}
			}
			
			ArrayList<Node> neighbors = currentNode.getNeighbors();	
			if (neighbors == null) {
				temperature *= coolingRate;
				continue; // no neighbors; already checked predecessor; just continue
			}
			
			for (Node neighbor : neighbors) {
				Game neighborState = neighbor.getGameState();
				if (neighborState == null) {
					neighborState = currentNode.getGameState().copy();
					neighborState.advanceGame(neighbor.getMove(), ghostMoves);
					neighbor.setGameState(neighborState);
				}
				
				int neighborHeuristic = Evaluation.evaluateGameState(neighborState);
				difference = neighborHeuristic - currentHeuristic;
				if (difference > 0) {
					// neighborHeuristic is higher, go to that node
					currentNode = neighbor;
					temperature *= coolingRate;
					break;
				} else {
					// neighborHeuristic is equal or lower, go to that node if percentage is met
					if (rand.nextFloat() < Math.exp(difference / temperature)) {
						currentNode = neighbor;
						temperature *= coolingRate;
						break;
					}
				}
			}
		}
		
		// currentNode may be a local maxima
		// return move to the current node
		if (currentNode == tree.getHeadNode()) return MOVE.NEUTRAL;
		while (currentNode.getPredecessor().getMove() != MOVE.NEUTRAL) {
			currentNode = currentNode.getPredecessor();
		}
		
		if (Evaluation.LOG_TIME) System.out.println(System.currentTimeMillis() - start);
		return currentNode.getMove();
	}
}
