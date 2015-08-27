package com.example.jordanquint.pokerchipsmanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.awakeplace.pokerchipsmanager.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;


public class PickWinner extends ActionBarActivity
{
	JSONArray players = null;
	SharedPreferences db;
	SharedPreferences.Editor database;

	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pick_winner);

		//read in the game id from the Intent
		Intent intent = getIntent();
		int game_id = intent.getIntExtra("game_id", 0);

		//load game settings from DB, initialize variables
		db = getSharedPreferences(getString(R.string.saved_games), Context.MODE_PRIVATE);
		database = db.edit();

		try
		{

			JSONObject game = (new JSONObject(db.getString("json_game_saves", "{\"games\":[]}"))).getJSONArray("games").getJSONObject(game_id);

			//initialize variables using DB info
			players = game.getJSONArray("players");
		} catch (Exception exception)
		{
			Log.e("PickWinner", "Error while reading in the game settings.");
		}
	}

	protected void onStart()
	{
		super.onStart();

		RelativeLayout container = (RelativeLayout) findViewById(R.id.winner_container);
		int previous_row_id = -1;
		LinearLayout row = new LinearLayout(this);

		//load a description for each Player
		for (int i = 0; i < players.length(); i++)
		{
			JSONObject player;

			String player_name = "";
			CheckBox check = new CheckBox(this);
			int chips_in = 0;

			//set variables for this player
			try
			{
				player = players.getJSONObject(i);
				player_name = player.getString("name");
				chips_in = player.getInt("chips_in");
			} catch (Exception exception)
			{
				Log.e("PickWinner", "Error finding player names");
			}

			check.setId(getResources().getIdentifier("player" + i, "id", getPackageName()));

			if (chips_in == -2) //folded
			{
				check.setEnabled(false);
				check.setTextColor(Color.GRAY);
			}

			//place this player where he should go, relative to other players
			if (i % 2 == 0) //create a new row
			{
				row = new LinearLayout(this);
				row.setOrientation(LinearLayout.HORIZONTAL);
				row.setId(getResources().getIdentifier("row" + (int) Math.floor(i / 2), "id", getPackageName()));

				RelativeLayout.LayoutParams row_params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

				if (i > 0)
				{ //odd numbered player, place it to the right of the previous player
					row_params.addRule(RelativeLayout.BELOW, previous_row_id);
					previous_row_id = row.getId();
				}
				else if (previous_row_id == -1)
				{
					previous_row_id = row.getId();
				}

				row.setLayoutParams(row_params);
			}

			//format the checkbox for this player
			LinearLayout.LayoutParams check_params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
			check_params.weight = 1;
			check_params.setMargins(5, 5, 5, 5);

			check.setText(player_name);
			check.setTextSize(18);
			check.setLayoutParams(check_params);
			check.setBackground(getDrawable(R.drawable.black_border));
			row.addView(check);

			if ((i+1) == players.length() || i % 2 == 1)
			{
				container.addView(row);
			}
		}
	}

	public void onWinnerPicked(View button)
	{
		ArrayList<Integer> winners = new ArrayList<>();

		//is at least one player selected?
		for (int i = 0; i < players.length(); i++)
		{
			CheckBox box = (CheckBox) findViewById(getResources().getIdentifier("player" + i, "id", getPackageName()));

			if (box.isChecked())
			{
				winners.add(i);
			}
		}

		Intent result = new Intent();
		result.putIntegerArrayListExtra("winners", winners);

		setResult(Activity.RESULT_OK, result);
		finish();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_pick_winner, menu);
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
