package de.unisaarland.sopra.ai;

import de.unisaarland.sopra.Direction;
import de.unisaarland.sopra.actions.Action;

import de.unisaarland.sopra.actions.MoveAction;

import de.unisaarland.sopra.model.Model;
import de.unisaarland.sopra.model.Position;
import de.unisaarland.sopra.model.PositionOutOfBoardException;
import de.unisaarland.sopra.model.entities.Monster;
import de.unisaarland.sopra.model.fields.CursedField;
import de.unisaarland.sopra.model.fields.LavaField;
import de.unisaarland.sopra.model.fields.WaterField;

import java.util.Random;

/**
 * Created by Antoine on 30.09.16.
 * <p>
 * project Anti
 */
abstract class Planning {

	Model model;
	int enemyId;
	Monster myMonster;

	Planning(Model model, int myID, int enemyId) {
		this.model = model;
		this.enemyId = enemyId;
		this.myMonster = model.getMonster(myID);
	}

	/**
	 * look, if myMonster would stop on this position, return false when water or lava field
	 * this method is called for my monster does not stop on water or lava
	 *
	 * @param position the position where myMonster wants to go
	 * @return {@code} true if mymonster may go/stop there
	 */
	private boolean wouldStopOnThis(Position position) {
		if (myMonster.getEnergy() < 200 && (model.getField(position) instanceof LavaField)) {
			return false;
		}
		//wenn ich weniger 500 energy hab und nicht auf wasserfeld bin-> return false
		return !(myMonster.getEnergy() < 500 && (model.getField(position) instanceof WaterField));
	}

	/**
	 * wenn ich aufs anvisierte Feld wirklich gehen sollte, da es kein cursed oder wasserfeld ist,
	 * returnt diese methode true
	 *
	 * @param direction the direction where i like to go
	 * @return true
	 */
	private boolean mayReallyGoThere(Direction direction) {
		try {
			return !(model.getBoard().getNeighbour(myMonster.getPosition(), direction) instanceof CursedField
					|| model.getBoard().getNeighbour(myMonster.getPosition(), direction) instanceof WaterField
					|| model.getBoard().getNeighbour(myMonster.getPosition(), direction) instanceof LavaField);
		} catch (PositionOutOfBoardException e) {
			return true;
		}
	}

	/**
	 * diese methode wird aufgerufen, um zu sehen ob ich von der anvisierten position aus angreifen kann
	 * und es sich um ein schlechtes feld handelt für den angriff
	 *
	 * @param position the position where i want to go
	 * @return {@code false} when this position is a cursed field and the enemy is around, else true
	 */
	private boolean wouldAttackFromThis(Position position) {
		//is this position a cursedField (maybe a tree field)
		if (model.getField(position) instanceof CursedField || model.getField(position) instanceof WaterField
				|| model.getField(position) instanceof LavaField) {
			for (Direction direction : Direction.values()) {
				//is around this position the enemy?
				try{
					if (model.getBoard().getNeighbour(position, direction).getPosition()
							.equals(model.getMonster(enemyId).getPosition())) {
						return false;
					}
				}
				catch (PositionOutOfBoardException e) {
					continue;
				}
			}
		}
		return true;
	}

	/**
	 * bla
	 *
	 * @param whereIWantToGo bla
	 * @return bla
	 */
	Action getBestMove(Position whereIWantToGo) {
		//the distance to whereIWantToGo, if myMonster moves to it, this has to decrease
		int oldDistance = whereIWantToGo.getDistanceTo(myMonster.getPosition());

		//go through all directions
		for (Direction direction : Direction.values()) {
			Action move = new MoveAction(direction);

			//first, is a move in this direction valid?
			if (move.validate(model, myMonster)) {

				//if its valid, then get the neighbourFields position in the direction
				Position position = model.getBoard().getNeighbour(myMonster.getPosition(), direction).getPosition();

				/*//dont stop on this, if its an lava or waterfield
				if (!wouldStopOnThis(position)) {
					continue;
				}
				if (!wouldAttackFromThis(position)) {
					continue;
				}*/
				//and from this position, get the distance to where i want to go, and look if it is lower than before
				if (position.getDistanceTo(whereIWantToGo) < oldDistance) {
					//if its lower, return this move from which myMonster is closer to the enemy
					return move;
				}
			}
		}
		//800 is the energy, he needs to cross 2 waterFields
		if (myMonster.getEnergy() >= 800 || model.getField(myMonster.getPosition()) instanceof WaterField) {
			//go through all directions
			for (Direction direction : Direction.values()) {
				Action move = new MoveAction(direction);
				//look, if move keeps the same distance to enemy
				if (move.validate(model, myMonster)) {
					//if its valid, then get the neighbourFields position in the direction
					Position position = model.getBoard().getNeighbour(myMonster.getPosition(), direction).getPosition();
					if (position.getDistanceTo(whereIWantToGo) == oldDistance) {
						return move;
					}
				}
			}
		}
		Random random = new Random();
		//just go in this if Statement, if the method is called to move to the enemy
		if (myMonster.getEnergy() == 1000) {
			Action[] possibleMoves = myMonster.planMoves(model).toArray(new Action[0]);
			if (possibleMoves.length == 0) {
				return null;
			}
			return possibleMoves[random.nextInt(possibleMoves.length)];
		}
		return null;
	}

	/**
	 * bla
	 *
	 * @return bla
	 */
	Action getAttack() {
		//avoide dass der moved wenn er einfach nur auf cursed steht
		//if (model.getMonster(enemyId).getPosition().getDistanceTo(myMonster.getPosition()) == 1) {
			for (Direction direction : Direction.values()) {
				//should i better move to an adjacent position to attack (if i am on cursed Field or water)
				/*Action moveadjacent = getMoveToAdjacentDirection(direction);
				if (moveadjacent != null) {
					return moveadjacent;
				}*/
				Action attack = whichAttack(direction);
				if (attack.validate(model, myMonster)) {
					if (model.getBoard().getNeighbour(myMonster.getPosition(), direction).getPosition()
							.equals(model.getMonster(enemyId).getPosition())) {
						return attack;
					}
				}

		}
		return null;
	}

	protected abstract Action whichAttack(Direction direction);

	/**
	 * schaut, ob es sich lohnt auf eins der beiden felder daneben zu gehen
	 * return null falls nicht, ansonsten den move
	 *
	 * @param direction where the enemy is, from my point of view
	 * @return a moveAction to one of the 2 adjacent fields, or null
	 */
	private Action getMoveToAdjacentDirection(Direction direction) {
		if (model.getField(myMonster.getPosition()) instanceof CursedField
				|| model.getField(myMonster.getPosition()) instanceof WaterField) {
			switch (direction) {
				case WEST:
					if (mayReallyGoThere(Direction.NORTH_EAST)) {
						if (new MoveAction(Direction.NORTH_EAST).validate(model, myMonster)) {
							return new MoveAction(Direction.NORTH_EAST);
						}
					}
					if (mayReallyGoThere(Direction.SOUTH_EAST)) {
						if (new MoveAction(Direction.SOUTH_EAST).validate(model, myMonster)) {
							return new MoveAction(Direction.SOUTH_EAST);
						}
					}
				case SOUTH_WEST:
					if (mayReallyGoThere(Direction.NORTH_WEST)) {
						if (new MoveAction(Direction.NORTH_WEST).validate(model, myMonster)) {
							return new MoveAction(Direction.NORTH_WEST);
						}
					}
					if (mayReallyGoThere(Direction.EAST)) {
						if (new MoveAction(Direction.EAST).validate(model, myMonster)) {
							return new MoveAction(Direction.EAST);
						}
					}

				case SOUTH_EAST:
					if (mayReallyGoThere(Direction.WEST)) {
						if (new MoveAction(Direction.WEST).validate(model, myMonster)) {
							return new MoveAction(Direction.WEST);
						}
					}
					if (mayReallyGoThere(Direction.NORTH_EAST)) {
						if (new MoveAction(Direction.NORTH_EAST).validate(model, myMonster)) {
							return new MoveAction(Direction.NORTH_EAST);
						}
					}

				case EAST:
					if (mayReallyGoThere(Direction.SOUTH_WEST)) {
						if (new MoveAction(Direction.SOUTH_WEST).validate(model, myMonster)) {
							return new MoveAction(Direction.SOUTH_WEST);
						}
					}
					if (mayReallyGoThere(Direction.NORTH_WEST)) {
						if (new MoveAction(Direction.NORTH_WEST).validate(model, myMonster)) {
							return new MoveAction(Direction.NORTH_WEST);
						}
					}

				case NORTH_EAST:
					if (mayReallyGoThere(Direction.SOUTH_EAST)) {
						if (new MoveAction(Direction.SOUTH_EAST).validate(model, myMonster)) {
							return new MoveAction(Direction.SOUTH_EAST);
						}
					}
					if (mayReallyGoThere(Direction.WEST)) {
						if (new MoveAction(Direction.WEST).validate(model, myMonster)) {
							return new MoveAction(Direction.WEST);
						}
					}

				case NORTH_WEST:
					if (mayReallyGoThere(Direction.EAST)) {
						if (new MoveAction(Direction.EAST).validate(model, myMonster)) {
							return new MoveAction(Direction.EAST);
						}
					}
					if (mayReallyGoThere(Direction.SOUTH_WEST)) {
						if (new MoveAction(Direction.SOUTH_WEST).validate(model, myMonster)) {
							return new MoveAction(Direction.SOUTH_WEST);
						}
					}
				default:
					return null;
			}
		}
		return null;
	}


	protected Boolean canMoveToBush(Position whereIWantToGo) {
		//the distance to whereIWantToGo, if myMonster moves to it, this has to decrease
		int oldDistance = whereIWantToGo.getDistanceTo(myMonster.getPosition());

		//go through all directions
		for (Direction direction : Direction.values()) {
			Action move = new MoveAction(direction);

			//first, is a move in this direction valid?
			if (move.validate(model, myMonster)) {

				//if its valid, then get the neighbourFields position in the direction
				Position position = model.getBoard().getNeighbour(myMonster.getPosition(), direction).getPosition();

				//and from this position, get the distance to where i want to go, and look if it is lower than before
				if (position.getDistanceTo(whereIWantToGo) <= oldDistance) {
					//if its lower, return this move from which myMonster is closer to the enemy
					return true;
				}
			}
		}
		return false;
	}

}

