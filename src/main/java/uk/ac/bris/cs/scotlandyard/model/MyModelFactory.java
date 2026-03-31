package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import com.google.common.collect.ImmutableSet;
import jakarta.annotation.Nonnull;

import org.checkerframework.checker.nullness.qual.NonNull;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import java.util.*;

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
		private Set<Model.Observer> observers;
		private GameState gameState;

		//constructor
		public MyModel(GameState initialState){
			// observer pattern meaning:
			// MyModel (the game) is the subject, the GUI is the observer
			// game state is immutable (can't be changed), model allows for people to open the game, close the game, open multiple windows etc.
			// model therefore needs a mutable Observer list to keep track of everyone currently "observing" the game updates
			// when model is first created, the list starts empty:
			this.observers = new HashSet<>();
			this.gameState = initialState;
		}

		@Nonnull
		@Override
		public Board getCurrentBoard() {
			return gameState;
		}

		@Override
		public void registerObserver(@NonNull Observer observer) {
			// null observer should throw
			if (observer == null) {
				throw new NullPointerException("Empty observer!");
			}
			//same observer twice should throw
			for (Observer i : observers){
				if (i == observer) throw new IllegalArgumentException("Duplicate observer!");
			}
			observers.add(observer);
		}

		@Override
		public void unregisterObserver(@NonNull Observer observer) {
			// null observer should throw
			if (observer == null) throw new NullPointerException("Empty observer!");

			// can't unregister a spectator that has never been registered before
			boolean isRegistered = false;
			for (Observer i : observers){
				if (i == observer) isRegistered = true;
			}
			if (!isRegistered) throw new IllegalArgumentException("Can't unregister if not registered");
			observers.remove(observer);
		}

		@Override
		public @NonNull ImmutableSet<Observer> getObservers() {
			return ImmutableSet.copyOf(observers);
		}

		@Override
		public void chooseMove(@NonNull Move move) {

		}
	}
}