package de.unisaarland.sopra.ai;

import de.unisaarland.sopra.Direction;
import de.unisaarland.sopra.actions.Action;
import de.unisaarland.sopra.actions.SlashAttack;
import de.unisaarland.sopra.actions.StabAttack;
import de.unisaarland.sopra.model.Model;
import de.unisaarland.sopra.model.entities.Monster;
import de.unisaarland.sopra.view.Player;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by Antoine on 05.10.16.
 * <p>
 * project Anti
 */
public class Pumuckl4 extends Player{
	private Queue<Action> actions = new LinkedList<>();
	private int myId;
	private int enemyId;
	private Monster myMonster;



	public Pumuckl4(Model model) {
		super(model);
	}

	@Override
	public Action act() {
		myId = getActorId();
		myMonster = model.getMonster(myId);
		List<Monster> monsters = model.getMonsters();
		for (Monster monster : monsters) {
			if (monster.getId() != getActorId()) {
				enemyId = monster.getId();
				break;
			}
		}
		if(model.getMonster(myId).getEnergy() == 1000
				&& !(model.getMonster(myId).getPosition().getDistanceTo(model.getMonster(enemyId).getPosition()) == 1)) {
			Dijkstra dijkstra = new Dijkstra(this.model, this.myId, this.enemyId);
			actions = dijkstra.toActionQueue();
		}
		if(model.getMonster(myId).getPosition().getDistanceTo(model.getMonster(enemyId).getPosition()) == 1) {
			return getAttack();
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
}
