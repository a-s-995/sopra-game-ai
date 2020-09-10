package de.unisaarland.sopra.ai;

import de.unisaarland.sopra.actions.Action;
import de.unisaarland.sopra.model.Model;
import de.unisaarland.sopra.model.Position;
import de.unisaarland.sopra.model.entities.Monster;
import de.unisaarland.sopra.model.fields.BushField;
import de.unisaarland.sopra.model.fields.Field;
import de.unisaarland.sopra.view.Player;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Antoine on 30.09.16.
 * <p>
 * project Anti
 */

class Pumuckl2 extends Player {

	private Monster myMonster;
	private Set<Position> bushFields = new HashSet<>();
	private int distanceToEnemy;
	//id of the enemy
	private int enemyId;
	private int myId;
	private Position closeBush;

	private Collection<Position> healingFields;
	private MyPhase currentPhase = MyPhase.MOVE_TO_ENEMY;

	private enum MyPhase {
		MOVE_TO_ENEMY, TO_BUSH, ATTACK, WAIT
	}

	/**
	 * initialises a list with all bushfields
	 *
	 * @return {@code true} if there are bushfields on the map, else false
	 */
	private boolean bushes() {
		Field[][] fields = model.getBoard().getFields();
		for (int i = 0; i < model.getBoard().getWidth(); i++) {
			for (int j = 0; j < model.getBoard().getHeight(); j++) {
				if (fields[i][j] instanceof BushField) {
					bushFields.add(fields[i][j].getPosition());
				}
			}
		}
		return !bushFields.isEmpty();
	}

	/**
	 * just sets the phase
	 *
	 * @param phase the new phase
	 */
	private void setCurrentPhase(MyPhase phase) {
		this.currentPhase = phase;
	}

	/**
	 * creates a new AI instance that controlls the kobold Pumuckl
	 * initialates the phase
	 *
	 * @param model i need this model
	 */
	Pumuckl2(Model model) {
		super(model);
		System.out.println("PUMUCKL2 wird erstellt");
	}

	@Override
	public Action act() {
		this.myMonster = getModel().getMonster(getActorId());
		this.myId = myMonster.getId();
		List<Monster> monsters = model.getMonsters();
		for (Monster monster : monsters) {
			if (monster.getId() != getActorId()) {
				enemyId = monster.getId();
				break;
			}
		}
		distanceToEnemy = model.getMonster(enemyId).getPosition().getDistanceTo(myMonster.getPosition());
		System.out.println("currentPhase:" + currentPhase);

		return handlePhase();
	}

	/**
	 * diese methode wird von act aufgerufen, und soll der Phase entsprechend eine action zurückgeben
	 *
	 * @return an action
	 */
	private Action handlePhase() {
		switch (currentPhase) {
			case MOVE_TO_ENEMY:
				return moveToEnemyPhase();
			case TO_BUSH:
				return bushPhase();
			case WAIT:
				return waitPhase();
			case ATTACK:
				return healPhase();
			default:
				System.out.println("WTFFFFF NO PHASE IS SET!!  ERROR ERRROR BIB BIEB BIEB ERROR");
				return null;

		}
	}

	private Action healPhase() {
		//handleHEAL
		healingFields = model.getActiveHealingFields();
		if (!healingFields.isEmpty()) {
			Position healingField = closestHealingField();
			PlanHeal healMove = new PlanHeal(model, myId, enemyId, healingField);
			if (((healingField.equals(myMonster.getPosition())) && (myMonster.getHealth() < 80))) {
				return healMove.getAttack();
			}
			if (model.getHealth(enemyId) > myMonster.getHealth() + 30 || myMonster.getHealth() < 33
					|| ((model.getHealth(enemyId) > myMonster.getHealth() + 22)
					&& (model.getEnergy(enemyId) > myMonster.getEnergy()))) {
				return healMove.getMoveAct();
			}
		}
		return attackPhase();
	}

	/**
	 * controlls the moveToEnemy Phase an returns the best Action
	 * this is the initial Phase, myMonster just wants to reach the enemy in this phase
	 * todo maybe make it a better phase,
	 *
	 * @return the best move
	 */
	private Action moveToEnemyPhase() {
		//changes the phase when at enemy
		PlanMoveEnemy move = new PlanMoveEnemy(model, myId, enemyId);
		if ((model.getMonster(enemyId).getPosition().getDistanceTo(myMonster.getPosition()) == 1)) {
			if(myMonster.getEnergy() >= 250) {
				return move.getAttack();
			}
			//are there bushes?
			//bushFields.isEmpty()
			if (!bushes()) {
				System.out.println("there are no bushes");
				setCurrentPhase(MyPhase.ATTACK);
				return null;
			}
			//make it attackphase, if no bushfield close to me
			closeBush = closestBushToMe();
			if (closeBush == null) {
				System.out.println("there is no near bush");
				setCurrentPhase(MyPhase.ATTACK);
				return null;
			}
			//bush phase
			System.out.println("GO TO BUSHPHASE");
			setCurrentPhase(MyPhase.TO_BUSH);
			return null;
		}
		//the normal case in this phase, move to enemy and maybe attack
		return move.getMoveAction();
	}

	/**
	 * this method controlls the bushPhase
	 * in the bushphase, the kobold stabs 2 times in the beginning, then moves to the bush
	 *
	 * @return the best Action
	 */
	private Action bushPhase() {
		//am i already on the bush? then WaitPhase
		if (myMonster.getPosition().equals(closeBush)) {
			if (distanceToEnemy == 1) {
				//when the enemy is beside me, attack and make attackphase
				setCurrentPhase(MyPhase.ATTACK);
				PlanningAttack attacke = new PlanningAttack(model, myId, enemyId, bushFields);
				return attacke.getActionAttack();
			} else {
				setCurrentPhase(MyPhase.WAIT);
				//IST DIES HIER RICHTIG??? null GUT?  in der nächsten phase verlasse ich mich darauf
				return null;
			}
		}
		//the normal case
		PlanMoveBush move = new PlanMoveBush(model, myId, enemyId, closeBush);
		return move.getMoveAct();
	}

	/**
	 * this method waits one round for the enemy on a bushfield and sets on attacking phase
	 * if the kobold may attack the enemy, he does
	 *
	 * @return an action
	 */
	private Action waitPhase() {
		PlanWaitPhase wait = new PlanWaitPhase(model, myId, enemyId);
		setCurrentPhase(MyPhase.ATTACK);
		return wait.getActionAttacke();
	}

	/**
	 * this method is called in the last phase, the action phase
	 * myMonster should run to the enemy and attack, and if a bushfield is around him, he should go on the bushfield
	 * at the end of one round
	 *
	 * @return an action
	 */
	private Action attackPhase() {
		PlanningAttack attacke = new PlanningAttack(model, myId, enemyId, bushFields);
		return attacke.getActionAttack();
	}

	/**
	 * this method is just important in the moveToBushPhase
	 *
	 * @return the position of the closest bush (to me)
	 */
	private Position closestBushToMe() {
		Position nearest = null;
		//the initial distance, the bushField has to have at least the same distance
		int distance = 8;
		for (Position position : bushFields) {
			if (position.getDistanceTo(myMonster.getPosition()) < distance) {
				distance = position.getDistanceTo(myMonster.getPosition());
				nearest = position;
			}
		}
		return nearest;
	}

	//// TODO: 27.09.16 change in healing field, that has  the minimum of costs to reach, ANSEHEN!!
	private Position closestHealingField() {
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

}
