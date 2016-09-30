package de.unisaarland.sopra.ai;

import de.unisaarland.sopra.Direction;
import de.unisaarland.sopra.actions.Action;
import de.unisaarland.sopra.actions.Attack;
import de.unisaarland.sopra.actions.MoveAction;
import de.unisaarland.sopra.actions.StabAttack;
import de.unisaarland.sopra.model.Model;
import de.unisaarland.sopra.model.Position;
import de.unisaarland.sopra.model.entities.Monster;
import de.unisaarland.sopra.model.fields.WaterField;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Created by Antoine on 30.09.16.
 * <p>
 * project Anti
 */
public class Pumuckl2 extends Pumuckl {

	private Monster myMonster;;
	private int distanceToEnemy;
	//id of the enemy
	private int enemyId;


	/**
	 * the constructor method
	 * it initialises the array bushFields with all bushFields
	 *
	 * @param model the model we need
	 */
	public Pumuckl2(Model model) {
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


		Action doIt = getEnemyAttack();
		if (doIt != null) {
			return doIt;
		}


		//move to the enemy
		return getBestMove(model.getMonster(enemyId).getPosition());

	}

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

}
