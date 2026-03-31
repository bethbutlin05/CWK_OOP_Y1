package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import com.google.common.collect.ImmutableSet;
import jakarta.annotation.Nonnull;

import org.checkerframework.checker.nullness.qual.NonNull;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * cw-model
 * Stage 2: Complete this class
 */
public final class MyModelFactory implements Factory<Model> {

	@Nonnull
	@Override
	public Model build(GameSetup setup,
					   Player mrX,
					   ImmutableList<Player> detectives) {
		// TODO
		//throw new RuntimeException("Implement me!");
		MyGameStateFactory stateFactory = new MyGameStateFactory();
        GameState initialState = stateFactory.build(setup, mrX, detectives);
		MyModel model = new MyModel(initialState);
		return model;
    }

	public class MyModel implements Model {

		//making the observer list a private attribute of MyModel will make it persist for the whole game
		private List<Model.Observer> observers;
		private GameState gameState;

		//constructor
		public MyModel(GameState initialState){
			// observer pattern meaning:
			// MyModel (the game) is the subject, the GUI is the observer
			// game state is immutable (can't be changed), model allows for people to open the game, close the game, open multiple windows etc.
			// model therefore needs a mutable Observer list to keep track of everyone currently "observing" the game updates
			// when model is first created, the list starts empty:
			this.observers = new ArrayList<>();
			this.gameState = initialState;
		}

		@Nonnull
		@Override
		public Board getCurrentBoard() {
			return gameState;
		}

		@Override
		public void registerObserver(@NonNull Observer observer) {
			if (observer == null) {
				throw new NullPointerException("Empty observer!");
			}
			for (Observer i : observers){
				if (i == observer) throw new IllegalArgumentException("Duplicate observer!");
			}
			observers.add(observer);
		}

		@Override
		public void unregisterObserver(@NonNull Observer observer) {

		}

		@Override
		public @NonNull ImmutableSet<Observer> getObservers() {
			return null;
		}

		@Override
		public void chooseMove(@NonNull Move move) {

		}
	}
}