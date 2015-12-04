package pacman.controllers.examples;

import java.util.EnumMap;

import pacman.controllers.Controller;
import pacman.controllers.examples.algorithms.AStar;
import pacman.controllers.examples.algorithms.Alphabeta;
import pacman.controllers.examples.algorithms.BreadthFirstSearch;
import pacman.controllers.examples.algorithms.DepthFirstSearch;
import pacman.controllers.examples.algorithms.DepthFirstSearchRevamped;
import pacman.controllers.examples.algorithms.Evaluation;
import pacman.controllers.examples.algorithms.EvolutionStrategy;
import pacman.controllers.examples.algorithms.GeneticAlgorithm;
import pacman.controllers.examples.algorithms.HillClimber;
import pacman.controllers.examples.algorithms.ID3Algorithm;
import pacman.controllers.examples.algorithms.IterativeDeepening;
import pacman.controllers.examples.algorithms.KNearestNeighbor;
import pacman.controllers.examples.algorithms.Perceptron;
import pacman.controllers.examples.algorithms.QLearning;
import pacman.controllers.examples.algorithms.SimulatedAnnealing;
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
	// algorithms
	private Alphabeta alphabeta;
	private BreadthFirstSearch breadthFirstSearch;
	private DepthFirstSearch depthFirstSearch;
	private EvolutionStrategy evolutionStrategy;
	private GeneticAlgorithm geneticAlgorithm;
	private HillClimber hillClimber;
	private IterativeDeepening iterativeDeepening;
	private SimulatedAnnealing simulatedAnnealing;
	private KNearestNeighbor kNearestNeighbor;
	private Perceptron perceptron;
	private ID3Algorithm id3Algorithm;
	private QLearning qLearning;
	private AStar aStar;
	private DepthFirstSearchRevamped depthFirstSearchRevamped;
	
	public StarterPacMan() {
		alphabeta = new Alphabeta();
		breadthFirstSearch = new BreadthFirstSearch();
		depthFirstSearch = new DepthFirstSearch();
		evolutionStrategy = new EvolutionStrategy();
		geneticAlgorithm = new GeneticAlgorithm();
		hillClimber = new HillClimber();
		iterativeDeepening = new IterativeDeepening();
		simulatedAnnealing = new SimulatedAnnealing();
		kNearestNeighbor = new KNearestNeighbor();
		perceptron = new Perceptron();
		id3Algorithm = new ID3Algorithm();
		qLearning = new QLearning();
		aStar = new AStar(Evaluation.DEPTH);
		depthFirstSearchRevamped = new DepthFirstSearchRevamped();
	}
	
	public MOVE getMove(Game game, long timeDue) {
		// assume ghosts are moving in same direction
		EnumMap<GHOST, MOVE> ghostMoves = new EnumMap<GHOST, MOVE>(GHOST.class);
		ghostMoves.put(GHOST.BLINKY, game.getGhostLastMoveMade(GHOST.BLINKY));
		ghostMoves.put(GHOST.INKY, game.getGhostLastMoveMade(GHOST.INKY));
		ghostMoves.put(GHOST.PINKY, game.getGhostLastMoveMade(GHOST.PINKY));
		ghostMoves.put(GHOST.SUE, game.getGhostLastMoveMade(GHOST.SUE));
		
		Tree tree = new Tree(Evaluation.DEPTH);
		tree.getHeadNode().setGameState(game);
		
		//return breadthFirstSearch.getMove(ghostMoves, tree);
		//return depthFirstSearch.getMove(ghostMoves, tree);
		//return iterativeDeepening.getMove(game, ghostMoves, Evaluation.DEPTH);
		//return hillClimber.getMove(ghostMoves, tree);
		//return alphabeta.getMove(game, ghostMoves, Evaluation.DEPTH);
		//return simulatedAnnealing.getMove(ghostMoves, tree);
		//return evolutionStrategy.getMove(game, ghostMoves, Evaluation.DEPTH);
		//return geneticAlgorithm.getMove(game, ghostMoves, Evaluation.DEPTH);
		//return kNearestNeighbor.getMove(Evaluation.DEPTH);
		//return perceptron.getMove(game, ghostMoves, Evaluation.DEPTH);
		//return aStar.getMove(game, ghostMoves);
		return depthFirstSearchRevamped.getMove(ghostMoves, tree);
	}
}