package com.example.awakeplace.pokerchipsmanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.jordanquint.pokerchipsmanager.PickWinner;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PokerGameActivity extends ActionBarActivity
{

	private SharedPreferences.Editor database = null;
	private static final int RAISE_BET_REQ_CODE = 1;
	private static final int PICK_WINNER_REQ_CODE = 2;
	private SharedPreferences db = null;
	private JSONObject game = null; //all the data for this game
	private int game_id = -1;
	private JSONArray players = null;
	private RelativeLayout container = null;
	private int dealer = -1;
	private int bigBlind = -1;
	private int smallBlind = -1;
	private int bigBlindPlayer = -1;
	private int smallBlindPlayer = -1;
	private JSONArray pots = null;
	private int player_turn = -1;
	private int hands_played = -1;
	private int turn = -1;
	private int current_bet = -1;
	private boolean already_created = false;

	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		//display the screen
		setContentView(R.layout.activity_poker_game);

		//read in the game id from the Intent
		Intent intent = getIntent();
		game_id = intent.getIntExtra("game_id", 0);

		//load game settings from DB, initialize variables
		db = getSharedPreferences(getString(R.string.saved_games), Context.MODE_PRIVATE);
		database = db.edit();

		try
		{
			game = (new JSONObject(db.getString("json_game_saves", "{\"games\":[]}"))).getJSONArray("games").getJSONObject(game_id);

			//initialize variables using DB info
			players = game.getJSONArray("players");
			bigBlindPlayer = game.getInt("bigBlindPlayer");
			smallBlindPlayer = game.getInt("smallBlindPlayer");
			dealer = game.getInt("dealer");
			player_turn = game.getInt("player_turn");
			hands_played = game.getInt("hands_played");
			turn = game.getInt("turn");
			bigBlind = game.getInt("bigBlind");
			smallBlind = game.getInt("smallBlind");
			current_bet = game.getInt("current_bet");
			pots = game.getJSONArray("pots");
		} catch (Exception exception)
		{
			handleException(exception, "Error while reading in the game settings.");
		}
	}

	protected void onStart()
	{
		super.onStart();

		if (!already_created)
		{
			container = (RelativeLayout) findViewById(R.id.player_container);

			int previous_row_id = -1;

			RelativeLayout.LayoutParams row_params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			LinearLayout row = new LinearLayout(this);
			row.setOrientation(LinearLayout.HORIZONTAL);
			row.setLayoutParams(row_params);
			row.setId(getResources().getIdentifier("row0", "id", getPackageName()));

			//load a description for each Player
			for (int i = 0; i < players.length(); i++)
			{
				JSONObject player = null;
				int player_chips = 0;
				int chips_in_amt = 0;
				String player_name = "";

				//set variables for this player
				try
				{
					player = players.getJSONObject(i);
					player_name = player.getString("name");
					player_chips = player.getInt("chips");
					chips_in_amt = player.getInt("chips_in");
				} catch (Exception exception)
				{
					handleException(exception, "Error while setting up player " + i);
				}

				//create the row, if necessary
				if (i % 2 == 0 && i > 0)
				{
					row = new LinearLayout(this);
					row.setOrientation(LinearLayout.HORIZONTAL);
					row.setId(getResources().getIdentifier("row" + (i / 2), "id", getPackageName()));
				}

				//create the box holding player info
				LinearLayout player_box = new LinearLayout(this);
				player_box.setOrientation(LinearLayout.VERTICAL);
				player_box.setId(getResources().getIdentifier("player" + i, "id", getPackageName()));
				player_box.setBackground(getDrawable(R.drawable.black_border));
				player_box.setPadding(8, 5, 8, 5);

				if (i % 2 == 0 && i > 0)
				{ //odd numbered player, place it to the right of the previous player
					row_params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
					row_params.addRule(RelativeLayout.BELOW, previous_row_id);
					previous_row_id = row.getId();
					row.setLayoutParams(row_params);
				} else if (previous_row_id == -1)
				{
					previous_row_id = row.getId();
				}

				//highlight the player if it's their turn
				if ((turn == 0 || turn == 2 || turn == 4 || turn == 6 || turn == 8) && i == dealer)
				{ //highlight dealer
					player_box.setBackground(getDrawable(R.drawable.highlighted_border));
				} else if ((turn == 1 || turn == 3 || turn == 5 || turn == 7) && player_turn == i)
				{ //highlight the player whose turn it is
					player_box.setBackground(getDrawable(R.drawable.highlighted_border));
				}

				//place the player in the right spot
				LinearLayout.LayoutParams player_params = new LinearLayout.LayoutParams(0, RelativeLayout.LayoutParams.WRAP_CONTENT);
				player_params.setMargins(5, 10, 5, 0);
				player_params.weight = 1;
				player_box.setLayoutParams(player_params);

				//format the text for the name
				TextView name = new TextView(this);
				name.setText(player_name);
				player_box.addView(name);

				//format the text for the chip amount
				TextView chips = new TextView(this);
				chips.setId(getResources().getIdentifier("chips" + i, "id", getPackageName()));
				chips.setText(Integer.toString(player_chips));
				player_box.addView(chips);

				//format the text for the chips_in for the current turn
				TextView chips_in = new TextView(this);
				chips_in.setId(getResources().getIdentifier("chips_in" + i, "id", getPackageName()));

				if (chips_in_amt == -2) //if the player has folded, display that
					chips_in.setText("Folded");
				else if (chips_in_amt == -1)
					chips_in.setText("0");
				else
					chips_in.setText(Integer.toString(chips_in_amt));

				player_box.addView(chips_in);

				//show it on screen
				row.addView(player_box);

				if (i % 2 == 1 || i == players.length() - 1)
				{
					container.addView(row);
				}
			}
			//create the pots
			LinearLayout pot_layout = new LinearLayout(this);

			RelativeLayout.LayoutParams relative_params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			relative_params.addRule(RelativeLayout.BELOW, previous_row_id);
			pot_layout.setLayoutParams(relative_params);
			pot_layout.setOrientation(LinearLayout.VERTICAL);
			pot_layout.setId(getResources().getIdentifier("pots", "id", getPackageName()));

			try
			{
				//add each of the pots into the list
				for (int i = 0; i < pots.length(); i++)
				{
					JSONObject this_pot = pots.getJSONObject(i);
					TextView pot = new TextView(this);

					if (i == 0)
					{
						pot.setText("Main Pot: " + this_pot.getInt("chips") + " chips");
					}
					else
					{
						pot.setText("Pot " + (i + 1) + ": " + this_pot.getInt("chips") + " chips");
					}

					//add the pot into the view
					pot_layout.addView(pot);
				}
			} catch (Exception exception)
			{
				handleException(exception, "Error while reading in the game settings.");
			}

			container.addView(pot_layout);

			//set the dealer
			addToken(dealer, "Dealer", R.color.dealer);

			//set the big blind
			addToken(bigBlindPlayer, "Big Blind", R.color.bigBlind);

			//set the small blind token
			addToken(smallBlindPlayer, "Small Blind", R.color.smallBlind);

			//set the buttons to the correct text
			setButtons();

			already_created = true;
		}

	}

	/*** HELPER METHODS ***/
	private void setButtons()
	{
		/*Rounds:
		* 0 = Deal cards to players
		* 1 = Place initial bets
		* 2 = Play 3 cards
		* 3 = Place bets
		* 4 = Play 1 card
		* 5 = Place bets
		* 6 = Play final card
		* 7 = Place final bets
		* 8 = Ask for results
		* */
		Button b;
		switch (turn)
		{
			case 0:
				b = (Button) findViewById(R.id.deal_button);
				b.setVisibility(Button.VISIBLE);
				b.setText("Burn 1, deal 2 cards to players");
				break;
			case 1:
				b = (Button) findViewById(R.id.fold_button);
				b.setVisibility(Button.VISIBLE);

				b = (Button) findViewById(R.id.call_button);
				b.setVisibility(Button.VISIBLE);

				b = (Button) findViewById(R.id.raise_button);
				b.setVisibility(Button.VISIBLE);
				break;
			case 2:
				b = (Button) findViewById(R.id.deal_button);
				b.setText("Burn 1, deal 3 \"The Flop\"");
				b.setVisibility(Button.VISIBLE);
				break;
			case 3:
				b = (Button) findViewById(R.id.fold_button);
				b.setVisibility(Button.VISIBLE);

				b = (Button) findViewById(R.id.call_button);
				b.setVisibility(Button.VISIBLE);

				b = (Button) findViewById(R.id.raise_button);
				b.setVisibility(Button.VISIBLE);
				break;
			case 4:
				b = (Button) findViewById(R.id.deal_button);
				b.setText("Burn 1, deal 1 \"The Turn\"");
				b.setVisibility(Button.VISIBLE);
				break;
			case 5:
				b = (Button) findViewById(R.id.fold_button);
				b.setVisibility(Button.VISIBLE);

				b = (Button) findViewById(R.id.call_button);
				b.setVisibility(Button.VISIBLE);

				b = (Button) findViewById(R.id.raise_button);
				b.setVisibility(Button.VISIBLE);
				break;
			case 6:
				b = (Button) findViewById(R.id.deal_button);
				b.setText("Burn 1, deal 1 \"The River\"");
				b.setVisibility(Button.VISIBLE);
				break;
			case 7:
				b = (Button) findViewById(R.id.fold_button);
				b.setVisibility(Button.VISIBLE);

				b = (Button) findViewById(R.id.call_button);
				b.setVisibility(Button.VISIBLE);

				b = (Button) findViewById(R.id.raise_button);
				b.setVisibility(Button.VISIBLE);
				break;
			case 8:
				Intent intent = new Intent(this, PickWinner.class);
				startActivityForResult(intent, PICK_WINNER_REQ_CODE);

				break;
			default:
				break;
		}
	}

	private void setBettingTurn()
	{
		//unhighlight the currently active player
		LinearLayout current_player = (LinearLayout) findViewById(getResources().getIdentifier("player" + player_turn, "id", getPackageName()));
		current_player.setBackground(getDrawable(R.drawable.black_border));

		//set the number after the dealer who bets first
		int players_left_of_dealer = 0;

		if (turn == 0)
			players_left_of_dealer = 2;

		try
		{
			JSONObject player = null;
			while (player == null || player.getInt("chips_in") == -2)
			{
				players_left_of_dealer++;
				//set the first player to bet
				if ((dealer + players_left_of_dealer) >= players.length())
				{
					player_turn = (dealer + players_left_of_dealer) - (players.length());
				}
				else
				{
					player_turn = dealer + players_left_of_dealer;
				}

				player = players.getJSONObject(player_turn);
			}
		}
		catch (Exception e)
		{
			handleException(e, "Error while setting next player's bet.");
		}

		//highlight the first player
		LinearLayout new_player = (LinearLayout) findViewById(getResources().getIdentifier("player" + player_turn, "id", getPackageName()));
		new_player.setBackground(getDrawable(R.drawable.highlighted_border));

		//reset every player's chips_in
		try
		{
			for (int i = 0; i < players.length(); i++)
			{
				JSONObject player = players.getJSONObject(i);

				//only reset chips_in if they haven't folded already
				if (player.getInt("chips_in") >= 0)
				{
					player.put("chips_in", -1);

					//update the screen
					TextView chips_in = (TextView) findViewById(getResources().getIdentifier("chips_in" + i, "id", getPackageName()));
					chips_in.setText("0");

					//save the player
					players = players.put(i, player);
				}
			}
		}
		catch (Exception exception)
		{
			handleException(exception, "Error while reading in the game settings.");
		}
	}

	private void addBet(int player_id, int bet_amount)
	{
		try
		{
			//update the player record
			JSONObject player = players.getJSONObject(player_id);

			//make sure the player's chips_in are at least 0, or else they were just waiting to play
			int chips_in = (player.getInt("chips_in") >= 0 ? player.getInt("chips_in") : 0) + bet_amount;
			int chips = player.getInt("chips") - bet_amount;

			if (player.getInt("chips") >= bet_amount)
			{
				player.put("chips_in", chips_in);
				player.put("chips", chips);

				//update the total chips
				TextView chips_text = (TextView) findViewById(getResources().getIdentifier("chips" + player_id, "id", getPackageName()));
				chips_text.setText(Integer.toString(chips));

				//update chips_in for this turn
				TextView chips_in_text = (TextView) findViewById(getResources().getIdentifier("chips_in" + player_id, "id", getPackageName()));
				chips_in_text.setText(Integer.toString(chips_in));

				//add chips to pot
				pots.put(0, pots.getJSONObject(0).put("chips", pots.getJSONObject(0).getInt("chips") + bet_amount));

				//update all the pots on the screen
				for (int i = 0; i < pots.length(); i++)
				{
					JSONObject this_pot = pots.getJSONObject(i);

					TextView pot_view = (TextView) ((LinearLayout) findViewById(getResources().getIdentifier("pots", "id", getPackageName()))).getChildAt(i);
					if (i == 0)
					{
						pot_view.setText("Main Pot: " + this_pot.getInt("chips") + " chips");
					}
					else
					{
						pot_view.setText("Pot " + (i + 1) + ": " + this_pot.getInt("chips") + " chips");
					}
				}

				if (current_bet < bet_amount)
					current_bet = bet_amount;

				//update the player data in the players list
				players = players.put(player_id, player);
			}
			else
			{
				Log.e("PokerGame", "Player " + player_id + " can't increase their bet by " + bet_amount);
			}
		}
		catch (Exception exception)
		{
			handleException(exception, "Error while changing bet amount for " + player_id + " to " + bet_amount + ".");
		}
	}

	private void nextTurn()
	{
		//set everything for next turn
		current_bet = 0;

		Button b;
		switch(turn)
		{
			/*Rounds:
			* 0 = Deal cards to players
			* 1 = Place initial bets
			* 2 = Play 3 cards
			* 3 = Place bets
			* 4 = Play 1 card
			* 5 = Place bets
			* 6 = Play final card
			* 7 = Place final bets
			* 8 = Ask for results
			* */
			case 0:
				//set the minimum bet
				current_bet = bigBlind;

				//hide the deal button
				b = (Button) findViewById(R.id.deal_button);
				b.setVisibility(Button.GONE);

				//set the player_turn for betting
				setBettingTurn();

				//pay the small and big blinds
				addBet(smallBlindPlayer, smallBlind);
				addBet(bigBlindPlayer, bigBlind);

				break;
			case 1:
				b = (Button) findViewById(R.id.call_button);
				b.setVisibility(Button.GONE);

				b = (Button) findViewById(R.id.fold_button);
				b.setVisibility(Button.GONE);

				b = (Button) findViewById(R.id.raise_button);
				b.setVisibility(Button.GONE);
				break;
			case 2:
				//hide the deal button
				b = (Button) findViewById(R.id.deal_button);
				b.setVisibility(Button.GONE);

				//set the player_turn for betting
				setBettingTurn();
				break;
			case 3:

				b = (Button) findViewById(R.id.call_button);
				b.setVisibility(Button.GONE);

				b = (Button) findViewById(R.id.fold_button);
				b.setVisibility(Button.GONE);

				b = (Button) findViewById(R.id.raise_button);
				b.setVisibility(Button.GONE);
				break;
			case 4:
				//hide the deal button
				b = (Button) findViewById(R.id.deal_button);
				b.setVisibility(Button.GONE);

				//set the player_turn for betting
				setBettingTurn();
				break;
			case 5:

				b = (Button) findViewById(R.id.call_button);
				b.setVisibility(Button.GONE);

				b = (Button) findViewById(R.id.fold_button);
				b.setVisibility(Button.GONE);

				b = (Button) findViewById(R.id.raise_button);
				b.setVisibility(Button.GONE);
				break;
			case 6:
				//hide the deal button
				b = (Button) findViewById(R.id.deal_button);
				b.setVisibility(Button.GONE);

				//set the player_turn for betting
				setBettingTurn();
				break;
			case 7:
				try
				{
					for (int i = 0; i < pots.length(); i++)
					{
						JSONObject pot = pots.getJSONObject(i);
						TextView pot_view = (TextView) ((LinearLayout) findViewById(getResources().getIdentifier("pots", "id", getPackageName()))).getChildAt(i);

						if (i == 0)
						{
							pot_view.setText("Main Pot: " + pot.getInt("chips") + " chips");
						}
						else
						{
							pot_view.setText("Pot " + (i + 1) + ": " + pot.getInt("chips") + " chips");
						}
					}
				}
				catch (Exception exception)
				{

				}

				b = (Button) findViewById(R.id.call_button);
				b.setVisibility(Button.GONE);

				b = (Button) findViewById(R.id.fold_button);
				b.setVisibility(Button.GONE);

				b = (Button) findViewById(R.id.raise_button);
				b.setVisibility(Button.GONE);
				break;
			case 8:
				hands_played += 1;

				//reset all players
				try
				{
					for (int i = 0; i < players.length(); i++)
					{
						JSONObject player = players.getJSONObject(i);

						//set this player as waiting to play
						player.put("chips_in", -1);

						//update the screen
						TextView chips_in = (TextView) findViewById(getResources().getIdentifier("chips_in" + i, "id", getPackageName()));
						chips_in.setText("0");

						//save the player
						players = players.put(i, player);
					}
				}
				catch (Exception exception)
				{
					handleException(exception, "Error resetting everyone's hands for the next round.");
				}

				//TODO: remove cards


				//rotate bigBlind, smallBlind, and dealer
				if (bigBlindPlayer + 1 >= players.length())
					bigBlindPlayer = 0;
				else
					bigBlindPlayer++;

				if (smallBlindPlayer + 1 >= players.length())
					smallBlindPlayer = 0;
				else
					smallBlindPlayer++;

				if (dealer + 1 >= players.length())
					dealer = 0;
				else
					dealer++;
				break;
			default:
				break;
		}

		if (turn == 8)
			turn = 0;
		else
			turn++;

		//set the buttons for this turn
		setButtons();
	}

	private void highlightNextPlayer()
	{
		//see if we need to move to the next player, or if we can end the turn
		try
		{ //are all players' chips_in equal to the bet amount, or have they folded?
			int next_player = -1;
			ArrayList<Integer> active_players = new ArrayList<>();

			for (int i=0; i < players.length(); i++)
			{
				int this_player = i + player_turn;

				if (this_player >= players.length())
					this_player -= players.length();

				JSONObject player = players.getJSONObject(this_player);

				if (player.getInt("chips_in") != -2)
				{
					active_players.add(this_player);
				}

				/* chips_in
				 * -2 = folded
				 * -1 = hasn't taken a turn yet
				 * 0+ = checked or bet already
				 */
				if (next_player == -1 && (player.getInt("chips_in") == -1 || (player.getInt("chips_in") != -2 && current_bet > player.getInt("chips_in"))))
				{ //if a player hasn't played yet, or they haven't folded and don't meet the minimum bet, switch to that player
					next_player = this_player;
				}
			}

			//if there's only 1 player in the round and everyone else has folded, award that player the win
			if (active_players.size() == 1)
			{
				award_winners(active_players);
				nextTurn();
			}
			else
			{
				//unhighlight this player
				LinearLayout current_player = (LinearLayout) findViewById(getResources().getIdentifier("player" + player_turn, "id", getPackageName()));
				current_player.setBackground(getDrawable(R.drawable.black_border));

				if (next_player > -1) //move to the next player
				{
					//move to the next player who hasn't folded
					player_turn = next_player;

					LinearLayout new_player = (LinearLayout) findViewById(getResources().getIdentifier("player" + player_turn, "id", getPackageName()));
					new_player.setBackground(getDrawable(R.drawable.highlighted_border));
				} else
				{
					//move to the next turn
					nextTurn();
				}
			}
		}
		catch (Exception exception)
		{
			handleException(exception, "Error while reading in the game settings.");
		}
	}

	private void addToken(int player_id, String text, int color_id)
	{
		LinearLayout player = (LinearLayout) findViewById(getResources().getIdentifier("player" + player_id, "id", getPackageName()));

		//remove the player who has the token currently
		TextView delete = (TextView) findViewById(getResources().getIdentifier(text.replaceAll(" ", ""), "id", getPackageName()));
		if (delete != null)
		{
			player.removeView(delete);
		}

		//add the token to the correct player
		TextView tv = new TextView(this);
		tv.setText(text);
		tv.setTextColor(color_id);
		tv.setId(getResources().getIdentifier(text.replaceAll(" ", ""), "id", getPackageName()));
		player.addView(tv);
	}

	private void handleException(Exception exception, String description)
	{
		String stackTrace = "";
		for (StackTraceElement e : exception.getStackTrace())
		{
			stackTrace += e.toString() + "\n";
		}

		Log.e("PokerGameActivity", description + "\n" + exception.getLocalizedMessage() + "\n" + stackTrace);
	}

	private void save()
	{
		try
		{
			JSONArray games = (new JSONObject(db.getString("json_game_saves", "{\"games\":[]}"))).getJSONArray("games");

			//update the players in the game
			game = game.put("players", players);
			game = game.put("dealer", dealer);
			game = game.put("bigBlindPlayer", bigBlindPlayer);
			game = game.put("smallBlindPlayer", smallBlindPlayer);
			game = game.put("hands_played", hands_played);
			game = game.put("player_turn", player_turn);
			game = game.put("last_played", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
			game = game.put("turn", turn);
			game = game.put("pots", pots);
			game = game.put("bigBlind", player_turn);
			game = game.put("player_turn", player_turn);
			game = game.put("current_bet", current_bet);

			//update the game in the game list
			games = games.put(game_id, game);

			//update the database and commit the changes
			database.putString("json_game_saves", (new JSONObject(db.getString("json_game_saves", "{\"games\":[]}"))).put("games", games).toString());
			database.commit();
		}
		catch (Exception exception)
		{
			handleException(exception, "Error while reading in the game settings.");
		}
	}

	/***** PUBLIC METHODS *****/
	public void dealButtonClicked(View button)
	{
		//do whatever the deal button is supposed to do at this point
		//TODO: animate cards getting handed out

		//set things up for the first betting turn
		nextTurn();

		save();
	}

	public void foldButtonClicked(View button)
	{
		//TODO: add ability to award chips to winner if everyone else has folded
		//this player has folded
		/* change their chips_in status
		* -2 = folded
		* -1 = waiting to play
		* 0 = no bet
		* 1+ = how many chips they've played this round
		*/
		try
		{
			JSONObject player = players.getJSONObject(player_turn);

			//save this for the player
			player = player.put("chips_in", -2);

			//update the player in the player list
			players = players.put(player_turn, player);

			//update the screen
			TextView chips_in = (TextView) findViewById(getResources().getIdentifier("chips_in" + player_turn, "id", getPackageName()));
			chips_in.setText("Folded");
		}
		catch (Exception exception)
		{
			handleException(exception, "Error while reading in the game settings.");
		}

		//move to the next player's turn
		highlightNextPlayer();

		save();
	}

	public void raiseButtonClicked(View button)
	{
		//See how many chips this player has left
		int chips_left = 0;
		String name = "";

		try
		{
			chips_left = players.getJSONObject(player_turn).getInt("chips");
			name = players.getJSONObject(player_turn).getString("name");
		} catch (Exception exception)
		{
			handleException(exception, "Error while getting chips for player: " + player_turn + " while creating RaiseBet fragment.");
		}

		//Start the RaiseBet activity
		Intent i = new Intent(this, RaiseBet.class);
		i.putExtra("low_bet", current_bet + bigBlind);
		i.putExtra("high_bet", chips_left);
		i.putExtra("interval", bigBlind);
		i.putExtra("name", name);

		startActivityForResult(i, RAISE_BET_REQ_CODE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode)
		{
			case (RAISE_BET_REQ_CODE):
			{
				if (resultCode == Activity.RESULT_OK)
				{
					addBet(player_turn, data.getIntExtra("bet_amount", current_bet + bigBlind));

					highlightNextPlayer();

					save();
				}
				break;
			}
			case (PICK_WINNER_REQ_CODE):
			{
				//if the activity didn't have any winners, close the game
				if (!data.hasExtra("winners"))
					finish();

				ArrayList<Integer> winners = data.getIntegerArrayListExtra("winners");

				award_winners(winners);

				nextTurn();

				save();

				break;
			}
		}
	}

	private void award_winners(ArrayList<Integer> winners)
	{
		int leftover_chips = 0;

		try
		{
			//split the pot(s) evenly
			for (int pot_index = 0; pot_index < pots.length(); pot_index++)
			{
				JSONObject pot = pots.getJSONObject(pot_index);
				JSONArray these_players = pot.getJSONArray("players");
				ArrayList<Integer> these_winners = new ArrayList<>();

				//find how many of the players in this pot are winners
				for (int pi = 0; pi < these_players.length(); pi++)
				{
					if (winners.indexOf(these_players.getInt(pi)) >= 0)
					{
						these_winners.add(these_players.getInt(pi));
					}
				}

				//award chips for these winners in this pot
				for (int win = 0; win < these_winners.size(); win++)
				{
					addChips(these_winners.get(win), pot.getInt("chips") / these_winners.size());
				}

				//leave any leftover chips in the pot for the next round
				leftover_chips += pot.getInt("chips") % these_winners.size();

				//reset the pot
				pot.put("chips", 0);
				pots.put(pot_index, pot);

				//remove the pot's text
				TextView pot_view = (TextView) ((LinearLayout) findViewById(getResources().getIdentifier("pots", "id", getPackageName()))).getChildAt(pot_index);
				pot_view.setText("");
			}

			//add the leftover chips to the main pot for next round
			pots.put(0, pots.getJSONObject(0).put("chips", leftover_chips));

			//update the main pot's chips
			TextView pot_view = (TextView) ((LinearLayout) findViewById(getResources().getIdentifier("pots", "id", getPackageName()))).getChildAt(0);
			pot_view.setText("Main Pot:" + Integer.toString(leftover_chips) + " chips");

			//get ready to reset the round
			turn = 0;
		}
		catch (Exception exception)
		{
			Log.e("PickWinner Result", "Error while awarding chips to the winners\n" + exception.getMessage() + "\n\n" + exception.getStackTrace());
		}
	}

	private void addChips(int player_id, int chips)
	{
		try
		{
			JSONObject player = players.getJSONObject(player_id);
			player.put("chips", player.getInt("chips") + chips);
			players.put(player_id, player);

			//update player's chips
			TextView t = (TextView) findViewById(getResources().getIdentifier("chips_in" + player_id, "id", getPackageName()));
			t.setText(Integer.toString(player.getInt("chips")));
		}
		catch (Exception exception)
		{
			handleException(exception, "Error while adding chips to player " + player_id + " bankroll");
		}
	}

	public void callButtonClicked(View button)
	{
		//this player has called, adjust chips if necessary and move on to the next player
		try
		{
			JSONObject player = players.getJSONObject(player_turn);

			//save this for the player
			if (player.getInt("chips_in") == -1)
			{
				addBet(player_turn, current_bet);
			}
			else if (player.getInt("chips_in") < current_bet)
			{
				int chips_to_add = current_bet - player.getInt("chips_in");
				addBet(player_turn, chips_to_add);
			}
		}
		catch (Exception exception)
		{
			handleException(exception, "Error while reading in the game settings.");
		}

		//move to the next player's turn
		highlightNextPlayer();

		save();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_poker_game, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings)
		{
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
