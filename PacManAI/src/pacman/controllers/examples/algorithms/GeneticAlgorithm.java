package pacman.controllers.examples.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Random;

import pacman.controllers.examples.StarterPacMan;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class GeneticAlgorithm {
	private class GeneticMove {
		public MOVE[] moves;
		int heuristic = -1;
		public GeneticMove(int length) {
			if (length > 0) moves = new MOVE[length];
		}
		int length() { return moves.length; }
	}
	
	/*
	 * Depth is the number of generations & number of actions in a set
	 * Each generation will have a crossover of the sets
	 * After Depth number of generations, return best set of moves
	 */
	public MOVE getMove(Game game, EnumMap<GHOST, MOVE> ghostMoves, int depth) {
		long start = System.currentTimeMillis();
		ArrayList<GeneticMove> population = new ArrayList<GeneticMove>(25);
		for (int i = 0; i < 5; i++)
			population.add(createGeneticMove(depth));
		GeneticMoveComparator comparator = new GeneticMoveComparator(game, ghostMoves);
		Random random = new Random(System.currentTimeMillis());
		
		for (int i = 0; i < depth; i++) {
			Collections.sort(population, comparator);
			int numTimes = (int) (0.4f * population.size()); // population increases by 40% each generation
			for (int j = 0; j < numTimes; j++) {
				// select best 30% of population to crossover
				int upperBound = (int) (0.3f * population.size()) + 1; // [0, 0.3f * population.size()]
				int parentOneIndex = random.nextInt(upperBound);
				int parentTwoIndex = random.nextInt(upperBound);
				population.add(crossOver(population.get(parentOneIndex), population.get(parentTwoIndex)));
			}
		}
		Collections.sort(population, comparator);
		if (StarterPacMan.LOG_TIME) System.out.println(System.currentTimeMillis() - start);
		return population.get(0).moves[0]; // return best genetic move's first move
	}
	
	private class GeneticMoveComparator implements Comparator<GeneticMove> {
		private Game gameState;
		private EnumMap<GHOST, MOVE> ghostMoves;
		public GeneticMoveComparator(Game gameState, EnumMap<GHOST, MOVE> ghostMoves) {
			this.gameState = gameState;
			this.ghostMoves = ghostMoves;
		}
		
		// higher heuristic values get higher priority
		@Override
		public int compare(GeneticMove firstMove, GeneticMove secondMove) {
			if (firstMove.heuristic == -1) {
				Game copy = gameState.copy();
				for (MOVE move : firstMove.moves) {
					copy.advanceGame(move, ghostMoves);
				}
				firstMove.heuristic = StarterPacMan.evaluateGameState(copy);
			}
			if (secondMove.heuristic == -1) {
				Game copy = gameState.copy();
				for (MOVE move : secondMove.moves) {
					copy.advanceGame(move, ghostMoves);
				}
				secondMove.heuristic = StarterPacMan.evaluateGameState(copy);
			}
			if (firstMove.heuristic < secondMove.heuristic) {
				return 1;
			} else if (firstMove.heuristic == secondMove.heuristic) {
				return 0;
			} else {
				return -1;
			}
		}
	}
	
	public GeneticMove crossOver(GeneticMove parent1, GeneticMove parent2) {
		GeneticMove child = new GeneticMove(parent1.length());
		for (int i = 0; i < parent1.length(); i++)
			child.moves[i] = parent1.moves[i];
		Random random = new Random(System.currentTimeMillis());
		for (int i = 0; i < 2; i++) {
			// crossover twice
			int index = random.nextInt(parent1.length());
			child.moves[index] = parent2.moves[index];
		}
		return child;
	}
	
	public GeneticMove createGeneticMove(int length) {
		GeneticMove move = new GeneticMove(length);
		Random random = new Random(System.currentTimeMillis());
		for (int i = 0; i < length; i++) {
			switch (random.nextInt(4)) {
			case 0:
				move.moves[i] = MOVE.LEFT;
				break;
			case 1:
				move.moves[i] = MOVE.RIGHT;
				break;
			case 2:
				move.moves[i] = MOVE.UP;
				break;
			case 3:
				move.moves[i] = MOVE.DOWN;
				break;
			}
		}		
		return move;
	}
}
