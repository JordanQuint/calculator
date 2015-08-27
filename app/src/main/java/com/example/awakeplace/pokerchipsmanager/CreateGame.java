package com.example.awakeplace.pokerchipsmanager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;


public class CreateGame extends ActionBarActivity
{

	private int min_players = 2;
	private int max_players = 10;
	private int playerNum = 4;
	private int game_id = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_game);

		//get the game ID of this game, see if it's larger than the allowed game slots
		Intent i = getIntent();
		game_id = i.getIntExtra("game_id",0);

		if (game_id > getResources().getInteger(R.integer.game_slots))
		{
			Toast.makeText(this, "You can't create another new game! Delete a game to make room.", Toast.LENGTH_LONG);
			finish(); //exit this activity, we can't create a new game
		}

		//create the seekbar and its listener
		SeekBar players = (SeekBar) findViewById(R.id.players);
		players.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
		{
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
			{
				//move the secondary progress too
				seekBar.setSecondaryProgress(progress);

				//add minimum # of players
				progress += min_players;
				int new_rows = (int) Math.floor((progress - 1.0) / 2.0) + 1;
				int current_rows = (int) Math.floor((playerNum - 1.0) / 2.0) + 1;

				//Update title
				TextView players_title = (TextView) findViewById(R.id.players_title);
				players_title.setText("Players (" + Integer.toString(progress) + ")");

				//Add or remove name fields
				if (progress > playerNum)
				{
					//show the rows containing names
					for (int i = 1; i <= new_rows - current_rows; i++)
					{
						LinearLayout layout = (LinearLayout) findViewById(getResources().getIdentifier("name_row" + Integer.toString(i + current_rows), "id", getPackageName()));
						layout.setVisibility(View.VISIBLE);
					}

					//display the names
					for (int i = playerNum; i <= progress; i++)
					{
						LinearLayout name = (LinearLayout) findViewById(getResources().getIdentifier("player_name" + Integer.toString(i), "id", getPackageName()));
						name.setVisibility(View.VISIBLE);
					}
				} else if (progress < playerNum)
				{
					//hide extra rows
					for (int i = 1; i <= current_rows - new_rows; i++) //how many rows to remove
					{
						LinearLayout layout = (LinearLayout) findViewById(getResources().getIdentifier("name_row" + Integer.toString(i + new_rows), "id", getPackageName()));
						layout.setVisibility(View.GONE);
					}

					//hide the names that aren't useful
					for (int i = progress + 1; i <= playerNum; i++)
					{
						LinearLayout name = (LinearLayout) findViewById(getResources().getIdentifier("player_name" + Integer.toString(i), "id", getPackageName()));
						name.setVisibility(View.GONE);
					}
				}

				playerNum = progress;
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar)
			{

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{

			}
		});

		//create the spinner for starting chips
		Spinner starting_chips = (Spinner) findViewById(R.id.starting_chips);
		ArrayAdapter<CharSequence> spinner_values = ArrayAdapter.createFromResource(this, R.array.starting_chips, android.R.layout.simple_spinner_item);
		spinner_values.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		starting_chips.setAdapter(spinner_values);

		//set a listener to adjust the available blinds every time start chips are changed
		starting_chips.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				TextView bigBlind = (TextView) findViewById(R.id.bigBlind);
				TextView smallBlind = (TextView) findViewById(R.id.smallBlind);
				Integer starting_chips = Integer.parseInt(parent.getSelectedItem().toString().replaceAll(",", ""));

				//update with 1/100th of the starting chips
				bigBlind.setText("Big Blind\n" + starting_chips / 100 + " chips");

				//update small blinds with 1/200th of the starting chips
				smallBlind.setText("Small Blind\n" + (int) Math.floor(starting_chips / 200.0) + " chips");
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{

			}
		});
	}

	public void startGame(View v)
	{
		//open a connection to the DB
		SharedPreferences db = getSharedPreferences(getString(R.string.saved_games), Context.MODE_PRIVATE);
		SharedPreferences.Editor e = db.edit();

		int game_id = -1;

		//read in existing games
		JSONObject data = null;
		try
		{
			data = new JSONObject(db.getString("json_game_saves", "{\"games\":[]}"));
			game_id = data.getJSONArray("games").length();
		} catch (Exception exception)
		{
			Log.e("CreateGame", "Error while reading in existing game saves.\n" + exception.getLocalizedMessage());
		}

		String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

		//convert settings into a JSONObject
		JSONObject new_game = null;
		try
		{
			//get # of starting chips
			Spinner chips = (Spinner) findViewById(R.id.starting_chips);
			Integer starting_chips = Integer.parseInt(chips.getSelectedItem().toString().replaceAll(",", ""));

			new_game = new JSONObject("{" +
				"game_id:" + game_id +
				",last_played:\"" + currentTime + "\"" +
				",created:\"" + currentTime + "\"" +
				",players:[]" +
				",turn:0" +
				",player_turn:0" +
				",hands_played:0" +
				",pots:[{players:[],chips:0}]" +
				",bigBlindPlayer:0" +
				",smallBlindPlayer:0" +
				",bigBlind:" + Integer.toString(starting_chips/100) +
				",smallBlind:" + Integer.toString(starting_chips/200) +
				",dealer:0" +
				",current_bet:0" +
				"}");

			//add players
			for (int i = 1; i <= playerNum; i++)
			{
				//add the player to the first pot
				JSONArray pots = new_game.getJSONArray("pots");
				JSONObject pot = pots.getJSONObject(0);
				pot.accumulate("players", i);

				pots.put(0, pot);
				new_game.put("pots", pots);

				//get name of the player
				TextView layout = (TextView) findViewById(getResources().getIdentifier("player" + Integer.toString(i), "id", getPackageName()));
				layout.getText();

				//read in a the player's name
				String player_name = layout.getText().toString().replaceAll("\"", "\\\"");

				if (player_name == "")
					player_name = "Player " + i;

				//save data for each player
				new_game.accumulate("players", new JSONObject("{name:\"" + player_name + "\"" +
					",chips:" + starting_chips +
					",chips_in:0" +
					"}"));
			}

			//set who's the big blind, small blind, and dealer
			if (playerNum > 2)
			{
				new_game.put("bigBlindPlayer", 2);
				new_game.put("smallBlindPlayer", 1);
				new_game.put("dealer", 0);
			} else if (playerNum == 2)
			{
				new_game.put("bigBlindPlayer", 1);
				new_game.put("smallBlindPlayer", 0);
				new_game.put("dealer", 0);
			}

			//save the game
			data.accumulate("games", new_game);
		} catch (Exception exception)
		{
			Log.e("CreateGame", "Exception while creating JSON object with given settings.\n" + exception.getLocalizedMessage());
		}

		//commit the save to memory
		e.putString("json_game_saves", data.toString());
		e.commit();

		//transition to the new game settings screen
		Intent i = new Intent(this, PokerGameActivity.class);
		i.putExtra("game_id", game_id);

		//when the user hits back, go to the main menu, don't reload this activity
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);


		startActivity(i);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement


		return super.onOptionsItemSelected(item);
	}
}
