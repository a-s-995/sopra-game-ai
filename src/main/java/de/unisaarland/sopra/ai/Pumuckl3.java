package de.unisaarland.sopra.ai;

import de.unisaarland.sopra.Direction;
import de.unisaarland.sopra.actions.Action;
import de.unisaarland.sopra.actions.StabAttack;
import de.unisaarland.sopra.model.Model;
import de.unisaarland.sopra.model.entities.Monster;
import de.unisaarland.sopra.view.Player;

import java.util.List;

/**
 * Created by Antoine on 04.10.16.
 * <p>
 * project Anti
 */
class Pumuckl3 extends Player {
	private Monster myMonster;
	private int enemyId;

	Pumuckl3(Model model) {
		super(model);
	}

	@Override
	public Action act() {
		this.myMonster = getModel().getMonster(getActorId());
		List<Monster> monsters = model.getMonsters();
		for (Monster monster : monsters) {
			if (monster.getId() != getActorId()) {
				enemyId = monster.getId();
				break;
			}
		}
		return getAttack();
	}

	private Action getAttack() {
		if (model.getMonster(enemyId).getPosition().getDistanceTo(myMonster.getPosition()) == 1) {
			for (Direction direction : Direction.values()) {
				Action attack = new StabAttack(direction);
				if (attack.validate(model, myMonster)) {
					if (model.getBoard().getNeighbour(myMonster.getPosition(), direction).getPosition()
							.equals(model.getMonster(enemyId).getPosition())) {
						return attack;
					}
				}
			}
		}
		return null;
	}
}
