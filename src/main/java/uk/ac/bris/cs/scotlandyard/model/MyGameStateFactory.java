package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import com.google.common.collect.ImmutableSet;
import jakarta.annotation.Nonnull;

import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import java.util.*;

/**
 * cw-model
 * Stage 1: Complete this class
 */
// hello hello hello
public final class MyGameStateFactory implements Factory<GameState> {

	//create initial state, validate input, set variables
	@Nonnull @Override public GameState build(
			GameSetup setup,
			Player mrX,
			ImmutableList<Player> detectives) {
		// TODO
		//throw new RuntimeException("Implement me!");
		return new MyGameState(
				setup,
				ImmutableSet.of(Piece.MrX.MRX), // only mr x moves at start
				ImmutableList.of(), // travel log starts empty
				mrX,
				detectives
		);

	}
	private final static class MyGameState implements GameState {
		//attributes
		private final GameSetup setup;
		private final ImmutableSet<Piece> remaining;
		private final ImmutableList<LogEntry> log;
		private final Player mrX;
		private final List<Player> detectives;
		private ImmutableSet<Move> moves;
		private final ImmutableSet<Piece> winner;
		private final ImmutableSet<Piece> allPlayers;

		//constructor
		private MyGameState(
				final GameSetup setup,
				final ImmutableSet<Piece> remaining,
				final ImmutableList<LogEntry> log,
				final Player mrX,
				final List<Player> detectives) {

			//constructor builds immutable game snapshot (computes moves and winner)

			this.setup = setup;
			this.remaining = remaining;
			this.log = log;
			this.mrX = mrX;
			this.detectives = detectives;

			if(setup.moves.isEmpty()) throw new IllegalArgumentException("Moves is empty!");
			if(remaining.isEmpty()) throw new IllegalArgumentException("Remaining pieces set is empty!");
			if(mrX == null) throw new NullPointerException("mrX is empty!");
			if (!mrX.isMrX()) throw new IllegalArgumentException("Mr X piece is invalid!");
			if(detectives.isEmpty()) throw new IllegalArgumentException("Detectives is empty!");
			if(setup.graph.edges().isEmpty() || setup.graph.nodes().isEmpty()) throw new IllegalArgumentException("Graph is empty!");
			for (Player i : detectives) {
				if (i.has(ScotlandYard.Ticket.SECRET) || i.has(ScotlandYard.Ticket.DOUBLE)) {
					throw new IllegalArgumentException("Detective cannot have secret or double ticket types!");
				}
				for (Player j : detectives) {
					if ((i.location() == j.location()) && (i != j)){
						throw new IllegalArgumentException("Duplicate detective locations!");
					}
				}
			}

			this.allPlayers = calculateAllPlayers();

			this.moves = calculateMoves();

			this.winner = calculateWinner();

			//if the winner set is empty, no moves left
			if (!this.winner.isEmpty()) {
				this.moves = ImmutableSet.of();
			}
		}

		@Override
		public GameSetup getSetup() {
			return setup;
		}

		@Override
		public ImmutableSet<Piece> getPlayers() {
			return allPlayers;
		}

		@Override
		public Optional<Integer> getDetectiveLocation(Piece.Detective detective) {
				for (Player j : detectives) {
					if (detective.isDetective() && (j.piece().equals(detective))) return Optional.of(j.location());
				}
			return Optional.empty();
		}

		@Override
		public Optional<TicketBoard> getPlayerTickets(Piece piece) {
			//looks in the map for the requested ticket, finds it, returns it.
			if (piece.isMrX()) return Optional.of(requestedTicket -> mrX.tickets().getOrDefault(requestedTicket, 0));
			for (Player i : detectives) {
				if (i.piece() == piece) return Optional.of(requestedTicket -> i.tickets().getOrDefault(requestedTicket, 0));
			}
			return Optional.empty();
		}

		@Override
		public ImmutableList<LogEntry> getMrXTravelLog() {
			return log;
		}

		@Override
		public ImmutableSet<Piece> getWinner() {
			return winner;
		}

		@Override
		public ImmutableSet<Move> getAvailableMoves() {
			return moves;
		}

		private ImmutableSet<Piece> calculateAllPlayers() {
			HashSet<Piece> players = new HashSet<>();
			players.add(mrX.piece());
			for (Player i : detectives) {
				players.add(i.piece());
			}
			return ImmutableSet.copyOf(players);
		}

		private ImmutableSet<Piece> calculateWinner(){
			HashSet<Piece> winnerSet = new HashSet<Piece>();

			//check whether a detective landed on mrX
			boolean mrXWasCaught = false;
			for (Player i : detectives) {
				if (i.location() == mrX.location()) {
					mrXWasCaught = true;
					break;
				}
			}

			//check whether mrX is cornered (surrounding nodes are taken by detectives)
			boolean mrXIsCornered = remaining.contains(mrX.piece()) && this.moves.isEmpty();

			//check whether mrX survived till the end (log is full, and it is his turn again)
			boolean logFull = (log.size() == setup.moves.size() && remaining.contains(mrX.piece()));

			//use singleMoves method to ask if detectives have run out of tickets
			boolean detectivesStuck = true;
			for (Player i : detectives) {
				if (!makeSingleMoves(setup, detectives, i, i.location()).isEmpty() && !i.tickets().isEmpty()) {
					detectivesStuck = false;
					break;
				}
			}

			//detectives win if they catch him or trap him
			if (mrXWasCaught || mrXIsCornered) {
				for (Player j : detectives){
					winnerSet.add(j.piece());
				}
			}

			//mrX wins if he survives to the end OR detectives are stuck
			else if (logFull || (detectivesStuck)){
				winnerSet.add(mrX.piece());
			}

            return ImmutableSet.copyOf(winnerSet);
        }

		private ImmutableSet<Move> calculateMoves() {
			HashSet<Move> moves = new HashSet<>();
			for (Piece i : remaining) {
				Player player = null;
				if (i.isMrX()) player = mrX;
				else {
					for (Player j : detectives){
						if (j.piece() == i) {
							player = j;
							break;
						}
					}
				}
				if (player == null) throw new IllegalArgumentException("Piece not found!");

				int location = player.location();

				moves.addAll(makeSingleMoves(setup, detectives, player, location));
				//check that there are enough move available to make a double move by substracting the remaining moves from the total number of moves allowed.
				if (i.isMrX() && (setup.moves.size() - log.size() >= 2)) {
					moves.addAll(makeDoubleMoves(setup, detectives, player, location));
				}
			}
			return ImmutableSet.copyOf(moves);
		}

		private static ImmutableSet<Move.SingleMove> makeSingleMoves(GameSetup setup, List<Player> detectives, Player player, int source){

			// TODO create an empty collection of some sort, say, HashSet, to store all the SingleMove we generate
			HashSet<Move.SingleMove> singleMoveHashSet = new HashSet<>();

			for(int destination : setup.graph.adjacentNodes(source)) {
				// TODO find out if destination is occupied by a detective
				//  if the location is occupied, don't add to the collection of moves to return
				boolean isLocationOccupied = false;
				for (Player i : detectives) {
					if (i.location() == destination) {
						isLocationOccupied = true;
						break;
					}
				}

				if (isLocationOccupied) continue;

				for (ScotlandYard.Transport t : setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of()) ) {
					// TODO find out if the player has the required tickets
					//  if it does, construct a SingleMove and add it the collection of moves to return
					if (player.has(t.requiredTicket())) {
						singleMoveHashSet.add(new Move.SingleMove(player.piece(), source, t.requiredTicket(), destination));
					}
				}
				// TODO consider the rules of secret moves here
				//  add moves to the destination via a secret ticket if there are any left with the player
				if (player.has(ScotlandYard.Ticket.SECRET)) {
					singleMoveHashSet.add(new Move.SingleMove(player.piece(), source, ScotlandYard.Ticket.SECRET, destination));
				}
			}
			// TODO return the collection of moves
            return ImmutableSet.copyOf(singleMoveHashSet);
        }

		private static ImmutableSet<Move.DoubleMove> makeDoubleMoves(GameSetup setup, List<Player> detectives, Player player, int source){
			//check that player actually has a double ticket
			if (!player.has(ScotlandYard.Ticket.DOUBLE)) return ImmutableSet.of();

			Set<Move.SingleMove> firstMoves = makeSingleMoves(setup, detectives, player, source);

			HashSet<Move.DoubleMove> secondMoveHashSet = new HashSet<>();

			for (Move.SingleMove i : firstMoves){

				//get the destination and ticket of each first move, then use the destination as the 'starting point' for the next move.
				int dest1 = i.destination;
				ScotlandYard.Ticket ticket1 = i.ticket;

				Set<Move.SingleMove> secondMoves = makeSingleMoves(setup, detectives, player, dest1);

				for (Move.SingleMove j : secondMoves) {
					int dest2 = j.destination;
					ScotlandYard.Ticket ticket2 = j.ticket;
					// if ticket1 == ticket2 it means mrX is trying to use for example 2 Taxi tickets in a row.
					// must now make sure that the player has 2 or more of that specific ticket
					if (ticket1 == ticket2){
						if (!player.hasAtLeast(ticket1, 2)) continue;
					}
					Move.DoubleMove doubleMove = new Move.DoubleMove(player.piece(), source, ticket1, dest1, ticket2, dest2);
					secondMoveHashSet.add(doubleMove);
				}
			}
			return ImmutableSet.copyOf(secondMoveHashSet);
		}

		@Override
		public GameState advance(Move move) {
			//advance returns the gamestate for the next turn

			if(!moves.contains(move)) throw new IllegalArgumentException("Illegal move: "+move);

			//pass an anonymous inner class to move.accept(...) to implement Move.Visitor interface
			GameState nextState = move.accept(new Move.Visitor<GameState>() {

				//visitor pattern allows the object to tell you what it is - the move will use dynamic dispatch to route itself to the correct visit method
				@Override
				public GameState visit(Move.SingleMove move) {
					//set up temporary mutable copies of the state to build data for next turn
					List<LogEntry> newLog = new ArrayList<>(log);
					Player newMrX = null;
					HashSet<Piece> remainingSet = new HashSet<>(remaining);

					if (move.commencedBy().isMrX()) {
						//if mrX has moved, his turn is over so we clear the remaining set
						remainingSet.clear();
						//determine whether it is a reveal round or not
						if (setup.moves.get(log.size()) == true){
							//if it's a reveal round, add the ticket and the destination to the log
							LogEntry newEntry = LogEntry.reveal(move.ticket, move.destination);
							newLog.add(newEntry);
						}
						else {
							//add hidden move to the log
							newLog.add(LogEntry.hidden(move.ticket));
						}
						//create new version of player who has one less ticket (.use(ticket)) and is now standing at new location (.at location)
						newMrX = mrX.use(move.ticket).at(move.destination);
						for (Player i : detectives){
							//for each detective, if it has available moves to make (the singleMoves list isn't empty), add its piece to the remainingSet
							if (!makeSingleMoves(setup, detectives, i, i.location()).isEmpty()) {
								remainingSet.add(i.piece());
							}
						}
						//if no detectives have any moves (they are all stuck as no detectives were added to the remaining set in the above loop), add mrX's piece to it (it is now his go)
						if (remainingSet.isEmpty()) {
							remainingSet.add(mrX.piece());
						}
					}
					else if (move.commencedBy().isDetective()) {
						//list of new detectives for next round
						ArrayList<Player> newDetectives = new ArrayList<>();
						for (Player i : detectives) {
							if (i.piece() == move.commencedBy()){
								//for the specific detective that moved, create a new version of the player with one less ticket standing at new location
								Player newDetective = i.use(move.ticket).at(move.destination);
								//give mrX the exact ticket the detective just spent, his location stays the same
								newMrX = mrX.give(move.ticket);
								//add the detective with new location and ticket list to list
								newDetectives.add(newDetective);
							} else {
							//for all other detectives, just add them to the list as they are
							newDetectives.add(i); }
						}
						//remove detective that just played from the 'waiting list'
						remainingSet.remove(move.commencedBy());
						//if they were the last detective to move, the round is over and it's mrX's turn
						if (remainingSet.isEmpty()) {
							remainingSet.add(mrX.piece());
						}

						//determine whether this move blocked any detectives waiting for their go
						HashSet<Piece> trappedDetectives = new HashSet<>();
						for (Piece p : remainingSet) {
							for (Player d : newDetectives) {
								if (d.piece() == p && makeSingleMoves(setup, newDetectives, d, d.location()).isEmpty()) {
									trappedDetectives.add(p);
								}
							}
						}
						//remove trapped detectives from remaining set
						remainingSet.removeAll(trappedDetectives);
						//return new instantiated MyGameState
						return new MyGameState(setup, ImmutableSet.copyOf(remainingSet), ImmutableList.copyOf(newLog), newMrX, ImmutableList.copyOf(newDetectives));
					}
					return new MyGameState(setup, ImmutableSet.copyOf(remainingSet), ImmutableList.copyOf(newLog), newMrX, detectives);
				}

				@Override
				public GameState visit(Move.DoubleMove move) {
					//if it's a double move, we know it will be mrX
					List<LogEntry> newLog = new ArrayList<>(log);
					HashSet<Piece> remainingSet = new HashSet<>(remaining);
					remainingSet.clear();

					//check whether move 1 is a reveal round or not
					if (setup.moves.get(log.size()) == true){
						newLog.add(LogEntry.reveal(move.ticket1, move.destination1));
					}
					else {
						newLog.add(LogEntry.hidden(move.ticket1));
					}

					//check whether move 2 is a reveal round or not
					if (setup.moves.get(log.size() + 1) == true){
						newLog.add(LogEntry.reveal(move.ticket2, move.destination2));
					}
					else {
						newLog.add(LogEntry.hidden(move.ticket2));
					}

					//deduct tickets from old mrX and update his location
					Player newMrX = mrX.use(ScotlandYard.Ticket.DOUBLE).use(move.ticket1).use(move.ticket2).at(move.destination2);

					for (Player i : detectives){
						if (!makeSingleMoves(setup, detectives, i, i.location()).isEmpty()) {
							remainingSet.add(i.piece());
						}
					}
					if (remainingSet.isEmpty()) {
						remainingSet.add(mrX.piece());
					}
					return new MyGameState(setup, ImmutableSet.copyOf(remainingSet), ImmutableList.copyOf(newLog), newMrX, detectives);
				}
			});
            return nextState;
        }

	}


}



