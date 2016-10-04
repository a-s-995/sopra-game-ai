package de.unisaarland.sopra.ai;

import de.unisaarland.sopra.Direction;
import de.unisaarland.sopra.actions.*;
import de.unisaarland.sopra.model.Model;
import de.unisaarland.sopra.model.Position;
import de.unisaarland.sopra.model.entities.Monster;
import de.unisaarland.sopra.model.fields.BushField;
import de.unisaarland.sopra.model.fields.Field;
import de.unisaarland.sopra.model.fields.WaterField;
import de.unisaarland.sopra.view.Player;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Antoine on 26.09.16.
 * <p>
 * this class represents my KI, which controls the kobold
 * <p>
 * project Anti
 */
public class Pumuckl extends Player {

	private Monster myMonster;
	private Set<Position> bushFields = new HashSet<>();
	private int distanceToEnemy;
	//id of the enemy
	private int enemyId;
//	private Collection<Position> healingFields;

	/**
	 * initialate the bushes set with all bushes of the map
	 */
	private void bushes() {
		Field[][] fields = model.getBoard().getFields();
		for (int i = 0; i < model.getBoard().getWidth(); i++) {
			for (int j = 0; j < model.getBoard().getHeight(); j++) {
				if (fields[i][j] instanceof BushField) {
					bushFields.add(fields[i][j].getPosition());
				}
			}
		}
	}

	/**
	 * the constructor method
	 * it initialises the array bushFields with all bushFields
	 *
	 * @param model the model we need
	 */
	public Pumuckl(Model model) {
		super(model);
	}

	@Override
	public Action act() {
		//i need the id of the enemy
		this.myMonster = getModel().getMonster(getActorId());
		List<Monster> monsters = model.getMonsters();
		for (Monster monster : monsters) {
			if (monster.getId() != getActorId()) {
				enemyId = monster.getId();
				break;
			}
		}
		//the current distance to the enemy
		distanceToEnemy = model.getMonster(enemyId).getPosition().getDistanceTo(myMonster.getPosition());
		//first look, if must go on healing field
		/*healingFields = model.getActiveHealingFields();
		Position healingField = closestHealingField();
		//until here, nothing to change

		if (healingField != null) {
			if ((healingField.equals(myMonster.getPosition())) && (myMonster.getHealth() < 80)) {
				return getEnemyAttack();
			}
		}
		if (myMonster.getHealth() < 70) {
			if (healingField != null) {
				Action goToHeal = goToHealingField(healingField);
				if (goToHeal != null) {
					return goToHeal;
				}
			}
		}*/
		//second, may i attack?
		Action doIt = getEnemyAttack();
		if (doIt != null) {
			//if if i can attack, but have less than 250 energy, move one adverse field
			if (myMonster.getEnergy() <= 250) {

				ActionVisitorAi visitor = new ActionVisitorAi();
				Direction direction = doIt.accept(visitor);
				return getMoveToAdverseDirection(direction);

			}
			return doIt;
		}
		if (myMonster.getEnergy() <= 150) {
			return null;
		}
		//move to the bushfield
		Position nearestBushField = closestBushToEnemy();
		if (nearestBushField != null) {
			if (distanceToEnemy < 6) {
				return getBestMove(nearestBushField);
			}
		}
		//move to the enemy
		return getBestMove(model.getMonster(enemyId).getPosition());
	}


	/**
	 * returns an attack, if i may attack the enemy
	 *
	 * @return the attack to the enemy
	 */
	private Action getEnemyAttack() {
		for (Direction direction : Direction.values()) {
			Attack attack = new StabAttack(direction);
			if (attack.validate(model, myMonster)) {
				if (model.getBoard().getNeighbour(myMonster.getPosition(), direction).getPosition()
						.equals(model.getMonster(enemyId).getPosition())) {
					return attack;
				}
			}
		}
		return null;
	}

	/**
	 * returns the best current move, bad method if the monster gets stuck
	 * if the distance to the enemy is less than 6, myMonster moves to the next bush field
	 *
	 * @return the best current move
	 */
	private Action getBestMove(Position whereIWantToGo) {

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
				if (position.getDistanceTo(whereIWantToGo) < oldDistance) {

					//if its lower, return this move from which myMonster is closer to the enemy
					return move;
				}
			}
		}

		return null;
	}


		//800 is the energy, he needs to cross 2 waterFields
		/*if (myMonster.getEnergy() >= 800 || model.getField(myMonster.getPosition()) instanceof WaterField) {
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
		if (oldDistance == distanceToEnemy) {
			if (myMonster.getEnergy() == 1000) {
				Action[] possibleMoves = myMonster.planMoves(model).toArray(new Action[0]);
				if (possibleMoves.length == 0) {
					return null;
				}
				return possibleMoves[random.nextInt(possibleMoves.length)];
			}
		}
		return null;
	}

	/**
	 * if the monster has to heal himself, this method returns a moveAction to the next healing Field
	 *
	 * @return moveAction to nearest healing field
	 */
	/*private Action goToHealingField(Position nearestHealingField) {
		int myMonstersHealth = myMonster.getHealth();
		int enemiesHealth = model.getHealth(enemyId);
		int myMonstersEnergy = myMonster.getEnergy();
		int enemiesEnergy = model.getEnergy(enemyId);
		//if the onwn health is under 33, go to healingField
		//if the enemy has 9 live more, go to healing field and more energy, or if the enemy has 22 life more
		//if the enemy is on bushfield, go to healing field
		if (enemiesHealth > myMonstersHealth + 30 || myMonstersHealth < 33
				|| ((enemiesHealth > myMonstersHealth + 22) && (enemiesEnergy > myMonstersEnergy))
				|| model.getField(model.getMonster(enemyId).getPosition()) instanceof BushField) {
			return getBestMove(nearestHealingField);
		}
		return null;
	}

	/**
	 * returns the closest bushfield, that has a distance less than 6 to myMonster and the enemy
	 *
	 * @return the closest bushfield between the 2 monsters
	 */
	private Position closestBushToEnemy() {
		bushes();
		Position nearest = null;
		//the initial distance, the bushField has to have at least the same distance
		int distance = 6;
		for (Position position : bushFields) {
			if (position.getDistanceTo(model.getMonster(enemyId).getPosition()) <= distance
					&& position.getDistanceTo(myMonster.getPosition()) < distance) {
				distance = position.getDistanceTo(model.getMonster(enemyId).getPosition());
				nearest = position;
			}
		}
		return nearest;
	}

	//// TODO: 27.09.16 change in healing field, that has  the minimum of costs to reach, ANSEHEN!!
	/*private Position closestHealingField() {
		if (healingFields.toArray().length == 0) {
			return null;
		}
		Position nearest = null;
		int distance = 50;
		for (Position position : healingFields) {
			if (position.getDistanceTo(model.getMonster(getActorId()).getPosition()) < distance) {
				distance = position.getDistanceTo(model.getMonster(getActorId()).getPosition());
				nearest = position;
			}
		}
		return nearest;
	}


	/**
	 * long, but simple method to get a movement to the adverse direction
	 *
	 * @param direction the direction where i need the adverse
	 * @return adverse direction
	 */
	private Action getMoveToAdverseDirection(Direction direction) {
		switch (direction) {
			case EAST:
				if (new MoveAction(Direction.WEST).validate(model, myMonster)) {
					return new MoveAction(Direction.WEST);
				}
				if (new MoveAction(Direction.NORTH_WEST).validate(model, myMonster)) {
					return new MoveAction(Direction.NORTH_WEST);
				}
				if (new MoveAction(Direction.SOUTH_WEST).validate(model, myMonster)) {
					return new MoveAction(Direction.SOUTH_WEST);
				}

			case NORTH_EAST:
				if (new MoveAction(Direction.SOUTH_WEST).validate(model, myMonster)) {
					return new MoveAction(Direction.SOUTH_WEST);
				}
				if (new MoveAction(Direction.WEST).validate(model, myMonster)) {
					return new MoveAction(Direction.WEST);
				}
				if (new MoveAction(Direction.SOUTH_EAST).validate(model, myMonster)) {
					return new MoveAction(Direction.SOUTH_EAST);
				}

			case NORTH_WEST:
				if (new MoveAction(Direction.SOUTH_EAST).validate(model, myMonster)) {
					return new MoveAction(Direction.SOUTH_EAST);
				}
				if (new MoveAction(Direction.EAST).validate(model, myMonster)) {
					return new MoveAction(Direction.EAST);
				}
				if (new MoveAction(Direction.SOUTH_WEST).validate(model, myMonster)) {
					return new MoveAction(Direction.SOUTH_WEST);
				}

			case WEST:
				if (new MoveAction(Direction.EAST).validate(model, myMonster)) {
					return new MoveAction(Direction.EAST);
				}
				if (new MoveAction(Direction.NORTH_EAST).validate(model, myMonster)) {
					return new MoveAction(Direction.NORTH_EAST);
				}
				if (new MoveAction(Direction.SOUTH_EAST).validate(model, myMonster)) {
					return new MoveAction(Direction.SOUTH_EAST);
				}

			case SOUTH_WEST:
				if (new MoveAction(Direction.NORTH_EAST).validate(model, myMonster)) {
					return new MoveAction(Direction.NORTH_EAST);
				}
				if (new MoveAction(Direction.NORTH_WEST).validate(model, myMonster)) {
					return new MoveAction(Direction.NORTH_WEST);
				}
				if (new MoveAction(Direction.EAST).validate(model, myMonster)) {
					return new MoveAction(Direction.EAST);
				}

			case SOUTH_EAST:
				if (new MoveAction(Direction.NORTH_WEST).validate(model, myMonster)) {
					return new MoveAction(Direction.NORTH_WEST);
				}
				if (new MoveAction(Direction.WEST).validate(model, myMonster)) {
					return new MoveAction(Direction.WEST);
				}
				if (new MoveAction(Direction.NORTH_EAST).validate(model, myMonster)) {
					return new MoveAction(Direction.NORTH_EAST);
				}

			default:
				return null;
		}
	}


	private int minimalMoveCost() {
		return 0;
	}
	// TODO: 29.09.16 do not rest on lava
}


