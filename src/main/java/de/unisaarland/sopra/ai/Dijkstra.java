package de.unisaarland.sopra.ai;

import de.unisaarland.sopra.Direction;
import de.unisaarland.sopra.actions.Action;
import de.unisaarland.sopra.actions.MoveAction;
import de.unisaarland.sopra.commands.ActionCommand;
import de.unisaarland.sopra.commands.Command;
import de.unisaarland.sopra.controller.SimulateController;
import de.unisaarland.sopra.model.Model;
import de.unisaarland.sopra.model.Position;

import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;


/**
 * Created by Antoine on 04.10.16.
 * <p>
 * project Anti
 */
public class Dijkstra {
	private Model model;
	private Model copyModel;
	private int myId;
	private int enemyId;
	private Position start;
	private Set<Position> positions = new HashSet<>();
	private Map<Position, Path> hash = new HashMap<>();

	protected Dijkstra(Model model, int myId, int enemyId) {
		this.model = model;
		this.myId = myId;
		this.start = model.getMonster(myId).getPosition();
		//initialize positions
		for (int i = 0; i < this.model.getBoard().getWidth(); i++) {
			for (int j = 0; j < this.model.getBoard().getHeight(); j++) {
				Position pos = Position.fromNormalizedCoordinates(i, j);
				positions.add(pos);
			}
		}
	}


	private void allgo() {
		initialize();
		//go trough all positions
		while (!positions.isEmpty()) {
			int next = minDist();
			//ein reihen durchlauf
			//get the current position
			// TODO: 05.10.16  break if the current position has distance to enemy of 1, aber nicht direkt, sonst
			//sonst berechne ich nur einen abstand vom gegner, nicht den besten
			for (Position position : positions) {
				if (position.getDistanceTo(model.getMonster(myId).getPosition()) == next) {
					//get the path to this position
					Path currentPath = hash.get(position);
					positions.remove(position);
					//move arraound
					// TODO: 04.10.16 vllt in extra methode, wenn PMD zickt
					Set<Direction> direcionSet = getFiveDirections(currentPath.getLastAction());
					if (direcionSet == null) {
						continue;
					}
					for (Direction dir : direcionSet) {
						//create the current model
						Path temp = currentPath;
						// a queue
						Deque<Action> moves = new LinkedList<>();
						while (temp.getLastAction() != null) {
							// add the last action as first one in the queue
							moves.addFirst(temp.getLastAction());
							temp = temp.getThePath();
						}
						//reset model
						copyModel = model.copy();
						//let the monster move to the current position
						while (!moves.isEmpty()) {
							//take the first object of the queue and removes it
							Command command = new ActionCommand(moves.poll(), myId);
							SimulateController controller = new SimulateController(copyModel);
							controller.step(command);
							copyModel = controller.getModel();
						}
						//let the monster move forward
						Action move = new MoveAction(dir);
						if (move.validate(copyModel, copyModel.getMonster(myId))) {
							Command command = new ActionCommand(move, myId);
							SimulateController controller = new SimulateController(copyModel);
							controller.step(command);
							copyModel = controller.getModel();
							Path newPath = new Path(copyModel.getMonster(myId).getPosition(),
									1000 - copyModel.getMonster(myId).getEnergy(), currentPath, move);
							distanz_update(currentPath, newPath);
						}
					}
				}
			}
		}
	}

	protected Deque<Action> toActionQueue() {
		allgo();
		int min = 11;
		Path nearestPath = null;
		for (Path value : hash.values()) {
			if (value.getCost() > 1000) {
				continue;
			}
			//get the nearest path to enemy, not the cheapest
			if (value.getCurrent().getDistanceTo(model.getMonster(enemyId).getPosition()) < min) {
				min = value.getCurrent().getDistanceTo(model.getMonster(enemyId).getPosition());
				nearestPath = value;
			}
		}
		Deque<Action> moves = new LinkedList<>();
		assert nearestPath != null;
		while (nearestPath.getLastAction() != null) {
			// add the last action as first one in the queue
			moves.addFirst(nearestPath.getLastAction());
			nearestPath = nearestPath.getThePath();
		}
		return moves;
	}

	protected Map<Position, Path> getHashResult() {
		allgo();
		return hash;
	}

	/**
	 * diese methode erstellt jeden pfad, setzt die zugehörige position, setzt die kosten auf unendlich,
	 * setzt die kosten der startposition auf 0,
	 * und die kosten der umliegenden positionen auf die entsprechenden werte sowie lastAction und pfad
	 */
	private void initialize() {
		for (Position pos : positions) {
			Path path = new Path(pos, 16384);
			hash.put(pos, path);
		}
		//set the costs of startPosition to 0
		hash.get(start).setCost(0);
		copyModel = model.copy();
		for (Direction dir : Direction.values()) {
			Action move = new MoveAction(dir);
			if (move.validate(copyModel, copyModel.getMonster(myId))) {
				Command command = new ActionCommand(move, myId);
				SimulateController controller = new SimulateController(copyModel);
				controller.step(command);
				copyModel = controller.getModel();
				//the path for the positions around start

				Path toPath = new Path(copyModel.getMonster(myId).getPosition(),
						1000 - copyModel.getMonster(myId).getEnergy(), hash.get(start), move);
				//replace it in the hashmap
				hash.replace(copyModel.getMonster(myId).getPosition(), toPath);
				//do not remove this position
			}
			//model zurücksetzen
			copyModel = this.model.copy();
		}

	}

	/**
	 * updates a path
	 *
	 * @param from the last path, from where i come
	 * @param to   new path, where i go now
	 */
	private void distanz_update(Path from, Path to) {
		int vonAnachB = to.getCost() - from.getCost();
		int alternativ = from.getCost() + vonAnachB;
		if (alternativ < hash.get(to.getCurrent()).getCost()) {
			//update the costs and path and moveAction
			hash.replace(to.getCurrent(), to);
		}
	}

	private int minDist() {
		int init = 100000;
		for (Position position : positions) {
			if (position.getDistanceTo(model.getMonster(myId).getPosition()) < init) {
				init = position.getDistanceTo(model.getMonster(myId).getPosition());
			}
		}
		return init;
	}

	private Set<Direction> getFiveDirections(Action lastMove) {
		ActionVisitorAi visitor = new ActionVisitorAi();
		Direction direction = lastMove.accept(visitor);
		Set<Direction> direcs = new HashSet<>();
		switch (direction) {
			case EAST:
				direcs.add(Direction.NORTH_EAST);
				direcs.add(Direction.NORTH_WEST);
				direcs.add(Direction.WEST);
				direcs.add(Direction.SOUTH_WEST);
				direcs.add(Direction.SOUTH_EAST);
				break;
			case NORTH_EAST:
				direcs.add(Direction.EAST);
				direcs.add(Direction.NORTH_WEST);
				direcs.add(Direction.WEST);
				direcs.add(Direction.SOUTH_WEST);
				direcs.add(Direction.SOUTH_EAST);
				break;
			case NORTH_WEST:
				direcs.add(Direction.EAST);
				direcs.add(Direction.NORTH_EAST);
				direcs.add(Direction.WEST);
				direcs.add(Direction.SOUTH_WEST);
				direcs.add(Direction.SOUTH_EAST);
				break;
			case WEST:
				direcs.add(Direction.EAST);
				direcs.add(Direction.NORTH_EAST);
				direcs.add(Direction.NORTH_WEST);
				direcs.add(Direction.SOUTH_WEST);
				direcs.add(Direction.SOUTH_EAST);
				break;
			case SOUTH_WEST:
				direcs.add(Direction.EAST);
				direcs.add(Direction.NORTH_EAST);
				direcs.add(Direction.NORTH_WEST);
				direcs.add(Direction.WEST);
				direcs.add(Direction.SOUTH_EAST);
				break;
			case SOUTH_EAST:
				direcs.add(Direction.EAST);
				direcs.add(Direction.NORTH_EAST);
				direcs.add(Direction.NORTH_WEST);
				direcs.add(Direction.WEST);
				direcs.add(Direction.SOUTH_WEST);
				break;
			default:
				return null;
		}
		return direcs;
	}
}
