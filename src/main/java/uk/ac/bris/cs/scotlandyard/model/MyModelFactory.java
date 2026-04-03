package uk.ac.bris.cs.scotlandyard.model;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.HashSet; import java.util.Set;
import javax.annotation.Nonnull;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;
public final class MyModelFactory implements Factory<Model> {
    @Nonnull
    @Override
    public Model build(GameSetup setup, Player mrX, ImmutableList<Player> detectives) { Factory<Board.GameState> stateFactory = new MyGameStateFactory();
        Board.GameState initialState = stateFactory.build(setup, mrX, detectives); return new MyModel(initialState);
    }

    // --- YOUR CUSTOM ANNOUNCER CLASS ---
    private final class MyModel implements Model { private Board.GameState state; private final Set<Model.Observer> observers;
        private MyModel(Board.GameState initialState) { this.state = initialState;
            this.observers = new HashSet<>();
        }
        @Nonnull
        @Override
        public Board getCurrentBoard() {
            return state;
        }
        @Override
        public void registerObserver(@Nonnull Model.Observer observer) {
            if (observer == null) throw new NullPointerException("Observer cannot be null");
            if (observers.contains(observer)) throw new IllegalArgumentException("Observer is already registered!"); observers.add(observer);
        }
        @Override public void unregisterObserver(@Nonnull Model.Observer observer) {
            if (observer == null) throw new NullPointerException("Observer cannot be null");
            if (!observers.contains(observer)) throw new IllegalArgumentException("Observer is not registered!"); observers.remove(observer);
        }
        @Nonnull
        @Override public ImmutableSet<Model.Observer> getObservers() {
            return ImmutableSet.copyOf(observers);
        }
        @Override
        public void chooseMove(@Nonnull Move move) {
        }
    }
}
