package de.unisaarland.sopra.ai;

import de.unisaarland.sopra.Direction;
import de.unisaarland.sopra.actions.Action;
import de.unisaarland.sopra.actions.SlashAttack;
import de.unisaarland.sopra.actions.StabAttack;
import de.unisaarland.sopra.model.Model;
import de.unisaarland.sopra.model.Position;
import de.unisaarland.sopra.model.entities.Monster;
import de.unisaarland.sopra.model.fields.BushField;
import de.unisaarland.sopra.model.fields.Field;
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
	private Set<Position> bushFields = new HashSet<>();


	Pumuckl4(Model model) {
		super(model);
	}

	@Override
	public Action act() {
		// TODO: 05.10.16  wait the first 2 rounds :D
		if(model.getRoundCount() == 1) {
			return null;
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
		//to here, nothing to change
		if(myMonster.getHealth() < 40 && model.getMonster(myId).getEnergy() == 1000) {
			Dijkstra dijkstra = new Dijkstra(this.model, this.myId);
			BestDestination goooo = new BestDestination(dijkstra.getHashMap(), model,
					model.getActiveHealingFields(), enemyId);
			actions = goooo.toActionQueueNotBeside();

		}
		else if(myMonster.getHealth() < 80 && model.getMonster(myId).getEnergy() == 1000) {
			Dijkstra dijkstra = new Dijkstra(this.model, this.myId);
			Collection<Position> potitions = new LinkedList<>();
			potitions.addAll(model.getActiveHealingFields());
			if(bushes()) {
				potitions.addAll(bushFields);
			}
			BestDestination bestDestination = new BestDestination(dijkstra.getHashMap(), model,
					potitions, enemyId);
			actions = bestDestination.toActionQueueNotBeside();
		}
		Action act = actions.poll();
		if(act != null) {
			return act;
		}
		if (model.getMonster(myId).getPosition().getDistanceTo(model.getMonster(enemyId).getPosition()) == 1) {
			return getAttack();
		}
		if (model.getMonster(myId).getEnergy() == 1000) {
			Dijkstra dijkstra = new Dijkstra(this.model, this.myId);
			BestDestination bestDestination = new BestDestination(dijkstra.getHashMap(), model, enemyId);
			actions = bestDestination.toActionQueue();
		}
		return actions.poll();

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
		if (model.getMonster(enemyId).getHealth() <= 54 && model.getMonster(enemyId).getHealth() >= 42
				&& (myMonster.getEnergy() == 1000 || myMonster.getEnergy() == 580)) {
			return new SlashAttack(direction);
		}
		if (myMonster.getEnergy() == 900 || myMonster.getEnergy() == 480 || myMonster.getEnergy() == 700) {
			return new SlashAttack(direction);
		}
		return new StabAttack(direction);
	}

	// TODO: 05.10.16 adding phases : moveEnemy -> MoveBush at distance of 5 to enemy; wait one round on bush; attack; heal
	// //





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

}
