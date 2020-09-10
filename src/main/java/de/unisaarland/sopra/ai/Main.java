package de.unisaarland.sopra.ai;


import de.unisaarland.sopra.model.Model;
import de.unisaarland.sopra.view.Player;
import de.unisaarland.sopra.view.PlayerFactory;

/**
 * The entry point for your ai implementation.
 */
public final class Main {
	private Main() {
	}

	/**
	 * This method is called when the ai is started.
	 *
	 * @param args Command line arguments
	 */
	public static void main(final String[] args) {
		de.unisaarland.sopra.Main.setPlayerFactory(new PlayerFactory() {

			@Override
			public Player create(Model model) {
				return new Pumuckl4(model);
			}
		});
		de.unisaarland.sopra.Main.main(args);
	}

}
