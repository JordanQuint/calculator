package com.example.awakeplace.pokerchipsmanager;

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

import org.json.JSONObject;


public class MainActivity extends ActionBarActivity
{
	private int games = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	protected void onStart()
	{
		super.onStart();

		//refresh how many games are available
		SharedPreferences db = getSharedPreferences(getString(R.string.saved_games), Context.MODE_PRIVATE);

		try
		{
			games = (new JSONObject(db.getString("json_game_saves", "{\"games\":[]}")).getJSONArray("games").length());
		} catch (Exception exception)
		{
			Log.e("MainActivity", "Error while reading in existing games.\n" + exception.getLocalizedMessage());
		}

		Button games_button = (Button) findViewById(R.id.games_button);

		if (games == 0)
		{ //there aren't any current games, disable continue button, only show new game button
			games_button.setText("START NEW GAME");
		}
		else
		{
			games_button.setText("GAMES (" + games + ")");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
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
		return super.onOptionsItemSelected(item);
	}

	public void selectGame(View v)
	{
		if (games == 0)
		{ //if there are no current games, just start a new one
			//transition to the new game settings screen
			Intent i = new Intent(this, CreateGame.class);
			startActivity(i);
		}
		else
		{
			//otherwise, transition to the select games screen
			Intent i = new Intent(this, SelectGame.class);
			startActivity(i);
		}
	}
}
