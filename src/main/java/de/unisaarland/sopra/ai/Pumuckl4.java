package de.unisaarland.sopra.ai;

import de.unisaarland.sopra.Direction;
import de.unisaarland.sopra.actions.Action;
import de.unisaarland.sopra.actions.MoveAction;
import de.unisaarland.sopra.actions.StabAttack;
import de.unisaarland.sopra.commands.ActionCommand;
import de.unisaarland.sopra.commands.Command;
import de.unisaarland.sopra.controller.SimulateController;
import de.unisaarland.sopra.model.Model;
import de.unisaarland.sopra.model.Position;
import de.unisaarland.sopra.model.entities.Monster;
import de.unisaarland.sopra.model.fields.BushField;
import de.unisaarland.sopra.model.fields.Field;
import de.unisaarland.sopra.model.fields.HealingField;
import de.unisaarland.sopra.model.fields.LavaField;
import de.unisaarland.sopra.model.fields.WaterField;
import de.unisaarland.sopra.view.Player;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Created by Antoine on 05.10.16.
 * <p>
 * project Anti
 */
class Pumuckl4 extends Player {
	private Queue<Action> actions = new LinkedList<>();
	private int myId;
	private int enemyId;
	private Monster myMonster;
	private Position healingFieldPos;
	private Position bestDestPos;
	private int howLong = 0;
	private Set<Position> bushFields = new HashSet<>();

	private boolean slashhim = true;
	private boolean onceOnBestField = true;
	private thePhase currentPhase = thePhase.MOVE_TO_ENEMY;

	private enum thePhase {
		MOVE_TO_ENEMY, ATTACK, WAIT, HEAL, BEST_POS, NOT_BOREDOM
	}


//	private int distanceToEnemy;
	//id of the enemy
//	private Position closeBush;

//	private Collection<Position> healingFields;
	private MyPhase currentPhase2 = MyPhase.MOVE_TO_ENEMY;

	private enum MyPhase {
		MOVE_TO_ENEMY, TO_BUSH, ATTACK, WAIT
	}





	Pumuckl4(Model model) {
		super(model);
	}

	@Override
	public Action act() {
		long startTime = System.nanoTime();
//		System.out.println("\n");
		// TODO: 05.10.16  wait the first 2 rounds , dran denken in moveToEnemy den roundcount:D
		if (model.getRoundCount() == 1) {
			return getAttack();
		}

		myId = getActorId();
		myMonster = model.getMonster(myId);
		List<Monster> monsters = model.getMonsters();
		for (Monster monster : monsters) {
			if (monster.getId() != getActorId()) {
				enemyId = monster.getId();
				break;
			}
		}
		if (model.getBoredom() >= 30) {
			this.currentPhase = thePhase.NOT_BOREDOM;
		}
		//to here, nothing to change

		//send done acting if enemy has less than 29 health, to be the first in next round
		if (slashhim) {
			if (amINext()) {
				if ((model.getMonster(enemyId).getHealth() < 29 && myMonster.getEnergy() <= 500) &&
						(model.getMonster(myId).getPosition().getDistanceTo(model.getMonster(enemyId).getPosition()) == 1)
						&& (myMonster.getHealth() < 80)) {
					slashhim = false;
					return null;
				}
			}
		}
		System.out.println("currentPhase:" + currentPhase);
//		System.out.println("slashhim:" + slashhim);
//		System.out.println("onceOnField:" + onceOnBestField);

		Action giveback = handlePhase();
		long endTime = System.nanoTime();
		long duration = (endTime - startTime) / 1000000;
		System.out.println("the time" + duration);
//				+ "\n");
		return giveback;
	}


	private Action handlePhase() {
		switch (currentPhase) {
			case MOVE_TO_ENEMY:
				return moveToEnemyPhase();
			case HEAL:
				return healPhase();
			case BEST_POS:
				return bestPosPhase();
			case WAIT:
				return waitPhase();
			case ATTACK:
				return attackPhase();
			case NOT_BOREDOM:
				return handleBoredom();
			default:
				System.out.println("WTFFFFF NO PHASE IS SET!!  ERROR ERRROR BIB BIEB BIEB ERROR");
				return null;
		}
	}





	private Action moveToEnemyPhase() {
		if (model.getRoundCount() >= 3) {
			this.currentPhase = thePhase.ATTACK;
			actions.clear();
			return attackPhase();
		}
		if (model.getMonster(enemyId).getPosition().getDistanceTo(myMonster.getPosition()) == 1) {
			actions.clear();
			return getAttack();
		}
		if (model.getMonster(myId).getEnergy() == 1000) {
			Dijkstra dijkstra = new Dijkstra(this.model, this.myId, 9);
			BestDestination bestDestination = new BestDestination(dijkstra.getHashMap(), model, enemyId);
			actions = bestDestination.toActionQueue();
		}
		return actions.poll();
	}

	/**
	 * diese phase soll folgendes können:
	 * zum gegner rennen, ihn angreifen, vor dass er dran ist ein feld zurücklaufen
	 * ab 80 hp zum nächstbesten feld rennen, WAIT_Phase (1runde), dann wieder angreifen und das obrige
	 * ab 40 leben aufs nächste heilfeld, dort bleiben bis 60 leben (wait PHase, 3 runden)
	 * das obige wiederholen
	 *
	 * @return an action
	 */
	private Action attackPhase() {
		if (onceOnBestField && (myMonster.getHealth() < 80) && (model.getMonster(myId).getEnergy() == 1000)) {
			onceOnBestField = false;
			Collection<Position> bushAndHeal = new LinkedList<>();
			bushAndHeal.addAll(model.getActiveHealingFields());
			if (bushes()) {
				bushAndHeal.addAll(bushFields);
			}
			if (!bushAndHeal.isEmpty()) {
				Dijkstra dijkstra = new Dijkstra(this.model, this.myId, 8);
				BestDestination bestDestination = new BestDestination(dijkstra.getHashMap(), model,
						bushAndHeal, enemyId);
				actions.clear();
				actions = bestDestination.toActionQueueNotBeside();
				bestDestPos = bestDestination.getDestination();
				if (!(model.getMonster(enemyId).getPosition().equals(bestDestPos))) {
					this.currentPhase = thePhase.BEST_POS;
					return bestPosPhase();
				}
			}
		}
		if (myMonster.getHealth() < 40 && model.getMonster(myId).getEnergy() == 1000) {
			if (!model.getActiveHealingFields().isEmpty()) {
				Dijkstra dijkstra = new Dijkstra(this.model, this.myId, 11);
				BestDestination justHeal = new BestDestination(dijkstra.getHashMap(), model,
						model.getActiveHealingFields(), enemyId);
				actions.clear();
				actions = justHeal.toActionQueueNotBeside();
				healingFieldPos = justHeal.getDestination();
				if (!(model.getMonster(enemyId).getPosition().equals(bestDestPos))) {
					this.currentPhase = thePhase.HEAL;
					return healPhase();
				}
			}
		}
		//if i am already on a good field, and enemy beside me, shure i have to attack
		if (model.getMonster(enemyId).getPosition().getDistanceTo(myMonster.getPosition()) == 1
				&& (model.getField(myMonster.getPosition()) instanceof BushField
				|| model.getField(myMonster.getPosition()) instanceof HealingField)) {
			return getAttack();
		}
		if (model.getMonster(enemyId).getPosition().getDistanceTo(myMonster.getPosition()) == 1) {
			return attackMoveBack();
		}
		//else move to enemy, but not if you already moved back
		else if (model.getMonster(myId).getEnergy() == 1000) {
			Dijkstra dijkstra = new Dijkstra(this.model, this.myId, 5);
			BestDestination bestDestination = new BestDestination(dijkstra.getHashMap(), model, enemyId);
			actions.clear();
			actions = bestDestination.toActionQueue();

		}
		return actions.poll();
	}

	private Action bestPosPhase() {
		if (myMonster.getPosition().equals(bestDestPos)) {
			this.currentPhase = thePhase.WAIT;
			howLong = 2;
			return waitPhase();
		}
		return actions.poll();
	}


	private Action healPhase() {
		if (myMonster.getPosition().equals(healingFieldPos)) {
			this.currentPhase = thePhase.WAIT;
			howLong = 3;
			return waitPhase();
		}
		return actions.poll();
	}

	/**
	 * this method may only be called, if the distance to enemy == 1
	 *
	 * @return an attack or moveback action
	 */
	private Action attackMoveBack() {
		//get shure, i will be the next
//		System.out.println("iAmNext " + amINext());
		if (amINext()) {
			Action attack = getAttack();
//			System.out.println("attack the attack " + attack);
			if (attack != null) {
				Model copyModel = model.copy();
				Command command = new ActionCommand(attack, myId);
				SimulateController controller = new SimulateController(copyModel);
				controller.step(command);
				copyModel = controller.getModel();
				if (model.getMonster(enemyId).getEnergy() < 250) {
					if (copyModel.getMonster(myId).getEnergy() >= model.getMonster(enemyId).getEnergy()) {
						return attack;
					} else {
						return null;
					}
				}
			}
		}
		//if i will not be next, get shure i move back
		Action attack = getAttack();
//		System.out.println("attack the attack " + attack);
		if (attack != null) {
			Model copyModel = model.copy();
			Command command = new ActionCommand(attack, myId);
			SimulateController controller = new SimulateController(copyModel);
			controller.step(command);
			copyModel = controller.getModel();
			if ((copyModel.getMonster(myId).getEnergy() > 100)) {
				return attack;
			}
		}
//		System.out.println("sadly i got to the end of moveBack");
		return moveBack();
	}

	private Action moveBack() {
		// TODO: 07.10.16 not just back, not on WATER, LAVA, if possible on bush/heal
		for (Direction direction : Direction.values()) {
			Action move = new MoveAction(direction);
			//first, is a move in this direction valid?
			if (move.validate(model, myMonster)) {
				//if its valid, then get the neighbourFields position in the direction
				Position position = model.getBoard().getNeighbour(myMonster.getPosition(), direction).getPosition();
				if (model.getField(position) instanceof WaterField || model.getField(position) instanceof LavaField) {
					continue;
				}
				if (position.getDistanceTo(model.getMonster(enemyId).getPosition()) > 1) {
					return move;
				}
			}
		}
		return null;
	}

	private Action waitPhase() {
		if (howLong == 0) {
			this.currentPhase = thePhase.ATTACK;
			return attackPhase();
		}
		howLong--;
		return getAttack();
	}


	private Action getAttack() {
		//avoide dass der moved wenn er einfach nur auf cursed steht
		for (Direction direction : Direction.values()) {
			//should i better move to an adjacent position to attack (if i am on cursed Field or water)
			Action attack = whichAttack(direction);
			if (attack.validate(model, model.getEntity(myId))) {
				if (model.getBoard().getNeighbour(model.getEntity(myId).getPosition(), direction).getPosition()
						.equals(model.getMonster(enemyId).getPosition())) {
					return attack;
				}
			}
		}
		return null;
	}

	private Action whichAttack(Direction direction) {
	/*	if (model.getMonster(enemyId).getHealth() <= 54 && model.getMonster(enemyId).getHealth() >= 42
				&& (myMonster.getEnergy() == 1000 || myMonster.getEnergy() == 580)) {
			return new SlashAttack(direction);
		}
		if (myMonster.getEnergy() == 900 || myMonster.getEnergy() == 480 || myMonster.getEnergy() == 700) {
			return new SlashAttack(direction);
		}*/
		return new StabAttack(direction);
	}

	// TODO: 05.10.16 adding phases : moveEnemy -> MoveBush at distance of 5 to enemy; wait one round on bush;
	// todo attack; heal,
	// // done
	// TODO: 06.10.16  vllt noch, zuu allerletzt, falls unbedingt nötig, cursedFields adden


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
	 * diese methode prüft nur, ob ihc in der acting order in der darauffolgenden Runde die initiative bekomme
	 *
	 * @return true if i can be first
	 */
	private boolean amINext() {
		//falls wir gleichviel energy verbraucht haben,
		List<Integer> actingOrder = model.getActingOrder();
		return actingOrder.indexOf(myId) > actingOrder.indexOf(enemyId);
	}

	private Action handleBoredom() {
		//distanceToEnemy = model.getMonster(enemyId).getPosition().getDistanceTo(myMonster.getPosition());
		System.out.println("currentPhase:" + currentPhase);
		return handlePhase2();
	}












































	/**
	 * just sets the phase
	 *
	 * @param phase the new phase
	 */
	private void setCurrentPhase2(MyPhase phase) {
		this.currentPhase2 = phase;
	}


	/**
	 * diese methode wird von act aufgerufen, und soll der Phase entsprechend eine action zurückgeben
	 *
	 * @return an action
	 */
	private Action handlePhase2() {
		switch (currentPhase2) {
			case MOVE_TO_ENEMY:
				return moveToEnemyPhase2();

			case ATTACK:
				return attackPhase2();
			default:
				System.out.println("WTFFFFF NO PHASE IS SET!!  ERROR ERRROR BIB BIEB BIEB ERROR");
				return null;

		}
	}

	/*
	private Action healPhase2() {
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
		return attackPhase2();
	}*/

	/**
	 * controlls the moveToEnemy Phase an returns the best Action
	 * this is the initial Phase, myMonster just wants to reach the enemy in this phase
	 * todo maybe make it a better phase,
	 *
	 * @return the best move
	 */
	private Action moveToEnemyPhase2() {
		//changes the phase when at enemy
		PlanMoveEnemy move = new PlanMoveEnemy(model, myId, enemyId);
		if ((model.getMonster(enemyId).getPosition().getDistanceTo(myMonster.getPosition()) == 1)) {
			if(myMonster.getEnergy() >= 250) {
				return move.getAttack();
			}
			setCurrentPhase2(MyPhase.ATTACK);
			return null;
			//are there bushes?
			//bushFields.isEmpty()
			/*if (!bushes()) {
				System.out.println("there are no bushes");
				setCurrentPhase2(MyPhase.ATTACK);
				return null;
			}
			//make it attackphase, if no bushfield close to me
			closeBush = closestBushToMe();
			if (closeBush == null) {
				System.out.println("there is no near bush");
				setCurrentPhase2(MyPhase.ATTACK);
				return null;
			}
			//bush phase
			System.out.println("GO TO BUSHPHASE");
			setCurrentPhase2(MyPhase.TO_BUSH);
			return null;*/
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
	/*private Action bushPhase2() {
		//am i already on the bush? then WaitPhase
		if (myMonster.getPosition().equals(closeBush)) {
			if (distanceToEnemy == 1) {
				//when the enemy is beside me, attack and make attackphase
				setCurrentPhase2(MyPhase.ATTACK);
				PlanningAttack attacke = new PlanningAttack(model, myId, enemyId, bushFields);
				return attacke.getActionAttack();
			} else {
				setCurrentPhase2(MyPhase.WAIT);
				//IST DIES HIER RICHTIG??? null GUT?  in der nächsten phase verlasse ich mich darauf
				return null;
			}
		}
		//the normal case
		PlanMoveBush move = new PlanMoveBush(model, myId, enemyId, closeBush);
		return move.getMoveAct();
	}*/

	/**
	 * this method waits one round for the enemy on a bushfield and sets on attacking phase
	 * if the kobold may attack the enemy, he does
	 *
	 * @return an action
	 */
	/*private Action waitPhase2() {
		PlanWaitPhase wait = new PlanWaitPhase(model, myId, enemyId);
		setCurrentPhase2(MyPhase.ATTACK);
		return wait.getActionAttacke();
	}
*/
	/**
	 * this method is called in the last phase, the action phase
	 * myMonster should run to the enemy and attack, and if a bushfield is around him, he should go on the bushfield
	 * at the end of one round
	 *
	 * @return an action
	 */
	private Action attackPhase2() {
		PlanningAttack attacke = new PlanningAttack(model, myId, enemyId, bushFields);
		return attacke.getActionAttack();
	}

	/**
	 * this method is just important in the moveToBushPhase
	 *
	 * @return the position of the closest bush (to me)
	 */
	/*private Position closestBushToMe() {
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
	}*/


}
