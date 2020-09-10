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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;


/**
 * Created by Antoine on 04.10.16.
 * <p>
 * project Anti
 */
class Dijkstra {
	private Model model;
	private Model copyModel;
	private int myId;
	private int deepnessAlgortighm;
	//	private int enemyId;
	private Set<Position> positions = new HashSet<>();
	private Map<Position, Path> hash = new HashMap<>();


	Dijkstra(Model model, int myId, int deepnessAlgortighm) {
		this.model = model;
		this.myId = myId;
		this.deepnessAlgortighm = deepnessAlgortighm;
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
			// TODO: 06.10.16  add a place for the 6, an integer in constructor 
			if (next == deepnessAlgortighm) {
				break;
			}
			//ein reihen durchlauf
			//get the current position
			Iterator<Position> iterator = positions.iterator();
			while (iterator.hasNext()) {
				Position position = iterator.next();
				if (position.getDistanceTo(model.getMonster(myId).getPosition()) == next) {
					iterator.remove();
					//get the path to this position
					Path currentPath = hash.get(position);
					//move arraound
					//wurde die position ever erreicht?
					if (currentPath.getLastAction() == null) {
						continue;
					}
					updateUpdate(currentPath);
					//get all directions, exept the one which "inverts" the lastAction

				}
			}
		}
	}

	Map<Position, Path> getHashMap() {
		allgo();
		return hash;
	}

	/**
	 * diese methode erstellt jeden pfad, setzt die zugehörige position, setzt die kosten auf unendlich,
	 * setzt die kosten der startposition auf 0,
	 * und die kosten der umliegenden positionen auf die entsprechenden werte sowie lastAction und pfad
	 */
	private void initialize() {
		Position start = model.getMonster(myId).getPosition();
		Iterator<Position> iterator = positions.iterator();
		while (iterator.hasNext()) {
			Position pos = iterator.next();
			if (pos.getDistanceTo(model.getMonster(myId).getPosition()) > 10) {
				iterator.remove();
			}
			Path path = new Path(pos, 16384);
			hash.put(pos, path);
		}
		//set the costs of startPosition to 0
		hash.get(start).setCost(0);
		positions.remove(start);
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

				//model zurücksetzen
				copyModel = this.model.copy();
			}
		}
	}

	/**
	 * updates a path
	 *
	 * @param to new path, where i go now
	 */
	private void distanz_update(Path to) {
		int alternativ = to.getCost();
		if (alternativ < hash.get(to.getCurrent()).getCost()) {
			//update the costs and path and moveAction
			hash.get(to.getCurrent()).setCost(to.getCost());
			hash.get(to.getCurrent()).setLastAction(to.getLastAction());
			hash.get(to.getCurrent()).setThePath(to.getThePath());
//			hash.replace(to.getCurrent(), to);
			updateUpdate(to);
		}
	}

	private void updateUpdate(Path prochain) {
		//get all directions, exept the one which "inverts" the lastAction
		Set<Direction> direcionSet = getFiveDirections(prochain.getLastAction());
		for (Direction dir : direcionSet) {
			//create the current model
			Path temp = prochain;

			// a queue
			//get a queue of actions to reset the model
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
						1000 - copyModel.getMonster(myId).getEnergy(), prochain, move);
				distanz_update(newPath);
			}
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
		if (lastMove == null) {
			return null;
		}
		ActionVisitorAi visitor = new ActionVisitorAi();
		Direction direction = lastMove.accept(visitor);
		Set<Direction> direcs = new HashSet<>();
		switch (direction) {
			case EAST:
				direcs.add(Direction.EAST);
				direcs.add(Direction.NORTH_EAST);
				direcs.add(Direction.NORTH_WEST);
				direcs.add(Direction.SOUTH_WEST);
				direcs.add(Direction.SOUTH_EAST);
				break;
			case NORTH_EAST:
				direcs.add(Direction.EAST);
				direcs.add(Direction.NORTH_EAST);
				direcs.add(Direction.NORTH_WEST);
				direcs.add(Direction.WEST);
				direcs.add(Direction.SOUTH_EAST);
				break;
			case NORTH_WEST:
				direcs.add(Direction.EAST);
				direcs.add(Direction.NORTH_EAST);
				direcs.add(Direction.NORTH_WEST);
				direcs.add(Direction.WEST);
				direcs.add(Direction.SOUTH_WEST);
				break;
			case WEST:
				direcs.add(Direction.NORTH_EAST);
				direcs.add(Direction.NORTH_WEST);
				direcs.add(Direction.WEST);
				direcs.add(Direction.SOUTH_WEST);
				direcs.add(Direction.SOUTH_EAST);
				break;
			case SOUTH_WEST:
				direcs.add(Direction.EAST);
				direcs.add(Direction.NORTH_WEST);
				direcs.add(Direction.WEST);
				direcs.add(Direction.SOUTH_WEST);
				direcs.add(Direction.SOUTH_EAST);
				break;
			case SOUTH_EAST:
				direcs.add(Direction.EAST);
				direcs.add(Direction.NORTH_EAST);
				direcs.add(Direction.WEST);
				direcs.add(Direction.SOUTH_WEST);
				direcs.add(Direction.SOUTH_EAST);
				break;
			default:
				return null;
		}
		return direcs;
	}
}
