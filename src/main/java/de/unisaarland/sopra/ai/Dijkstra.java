package de.unisaarland.sopra.ai;

import de.unisaarland.sopra.Direction;
import de.unisaarland.sopra.actions.Action;
import de.unisaarland.sopra.actions.MoveAction;
import de.unisaarland.sopra.commands.ActionCommand;
import de.unisaarland.sopra.commands.Command;
import de.unisaarland.sopra.controller.SimulateController;
import de.unisaarland.sopra.model.Model;
import de.unisaarland.sopra.model.Position;
import de.unisaarland.sopra.model.entities.Monster;
import de.unisaarland.sopra.model.fields.Field;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * Created by Antoine on 04.10.16.
 * <p>
 * project Anti
 */
public class Dijkstra {
	Model model;
	Monster myMonster;
	int myId = myMonster.getId();
	Field[][] fields = model.getBoard().getFields();
	Set<Path> pathes = new HashSet<>();
	Position start;

	private void allgo() {
		for (Direction dir : Direction.values()) {
			Action move = new MoveAction(dir);
			move.validate(model, myMonster);
			Command command = new ActionCommand(move, myMonster.getId());
			SimulateController controller = new SimulateController(model);
			controller.step(command);
			Model copyModel = controller.getModel();
			//setze die kosten neu, falls sie niedriger sind
			for (Path pfad : pathes) {
				if (copyModel.getMonster(myId).getPosition().equals(pfad.getCurrent())) {
					//sind die kosten des neuen monsters geringer als die des alten?
					if (copyModel.getMonster(myId).getEnergy() < pfad.getCost()) {
						//setze die kosten auf die benötigte energie, um auf das feld zu kommen
						pfad.setCost(1000 - copyModel.getMonster(myId).getEnergy());
					}
				}
			}
		}
	}

	private void fromTo() {

		//initialisiere paths
		//Path[][] paths = new Path[model.getBoard().getWidth()][model.getBoard().getHeight()];
		//paths[myMonster.getPosition().getX()][myMonster.getPosition().getY()].setCost(0);


	}

	private void distanz_update(Path u, Path v) {

	}


	private void initialize() {
		Path[] pfad = (Path[]) pathes.toArray();
		for (int i = 0; i < model.getBoard().getWidth(); i++) {
			for (int j = 0; j < model.getBoard().getHeight(); j++) {
				Position pos = new Position(i, j);
				pfad[model.getBoard().getWidth() * i + j].setCurrent(pos);
				pfad[model.getBoard().getWidth() * i + j].setCost(16384);
			}
		}
		pfad[model.getBoard().getWidth() * myMonster.getPosition().getX() + myMonster.getPosition().getY()].setCost(0);
		pathes.clear();
		pathes = new HashSet<Path>(Arrays.asList(pfad));
	}

	private Path getMin() {
		Path path = new Path();
		path.setCost(16384);
		int i = 0;
		int j = 0;
		for (; i < model.getBoard().getWidth(); i++) {
			for (; j < model.getBoard().getHeight(); j++) {
				if (paths[i][j].getCost() < path.getCost()) {
					path = paths[i][j];
				}
			}
		}
		//enferne path[i][j]
		return path;
	}
}
