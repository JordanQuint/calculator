package com.example.awakeplace.pokerchipsmanager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.awakeplace.pokerchipsmanager.CreateGame;
import com.example.awakeplace.pokerchipsmanager.PokerGameActivity;
import com.example.awakeplace.pokerchipsmanager.R;
import org.json.JSONObject;
import java.text.SimpleDateFormat;

public class SelectGame extends ActionBarActivity
{
	private JSONObject games = null;
	private SharedPreferences db = null;
	private Context context = this;
	private boolean item_selected = false;
	private Menu menu = null;
	private LinearLayout container = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_game);

		container = (LinearLayout) findViewById(R.id.container);

		Intent intent = getIntent();
	}

	protected void onStart()
	{
		super.onStart();

		//empty the container first, then fill it with updated data for all game slots
		container.removeAllViews();

		//reload game save data
		db = getSharedPreferences(getString(R.string.saved_games), Context.MODE_PRIVATE);

		//load available games
		try
		{
			games = new JSONObject(db.getString("json_game_saves", "{\"games\":[]}"));
		} catch (Exception exception)
		{
			Log.e("SelectGame", "Error while reading in existing games.\n" + exception.getLocalizedMessage());
		}

		//fill out the slots
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(700, 200);
		params.setMargins(0, 0, 0, 10);

		try
		{
			for (int i = 0; i < getResources().getInteger(R.integer.game_slots); i++)
			{
				LinearLayout box = new LinearLayout(this);

				if (i < games.getJSONArray("games").length()) //if there's a game to load
				{
					JSONObject game = games.getJSONArray("games").getJSONObject(i);

					//format the box with all the info in it
					box.setPadding(10, 10, 10, 10);
					box.setBackground(getDrawable(R.drawable.black_border));
					box.setOrientation(LinearLayout.VERTICAL);
					box.setLayoutParams(params);
					box.setId(getResources().getIdentifier("select_game_" + i, "id", getPackageName()));
					box.setOnLongClickListener(new View.OnLongClickListener()
					{
						@Override
						public boolean onLongClick(View v)
						{
							item_selected = true;

							//bold the background
							v.setBackground(getDrawable(R.drawable.bold_black_border));
							v.setSelected(true);

							//show the delete icon
							MenuItem trash_can = menu.findItem(R.id.action_delete);
							trash_can.setVisible(true);

							return true;
						}
					});

					box.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							if (item_selected)
							{
								deselect();
							}
							else
							{
								//load up this game
								Intent intent = new Intent(context, PokerGameActivity.class);
								intent.putExtra("game_id", getResources().getResourceName(v.getId()).split("_")[2]);
								startActivity(intent);
							}
						}
					});


					//set the format for reading dates
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

					//format create date
					TextView created = new TextView(this);
					created.setText("Started " +
						DateUtils.getRelativeDateTimeString(
							this,
							dateFormat.parse(game.getString("last_played")).getTime(),
							DateUtils.MINUTE_IN_MILLIS,
							DateUtils.WEEK_IN_MILLIS,
							DateUtils.FORMAT_ABBREV_ALL));

					//format last played time
					TextView last_played = new TextView(this);
					last_played.setText("Last played " + DateUtils.getRelativeTimeSpanString(dateFormat.parse(game.getString("last_played")).getTime()));

					//format # of players
					TextView players = new TextView(this);
					players.setText(game.getJSONArray("players").length() + " players");

					//format what turn it is
					TextView hands_played = new TextView(this);
					hands_played.setText(game.getInt("hands_played") + " hands played");

					//add everything into the LinearLayout container
					box.addView(last_played);
					box.addView(created);
					box.addView(players);
					box.addView(hands_played);
				}
				else
				{
					box = createEmptyBox(i);

					TextView middle = new TextView(this);
					middle.setText("Game Slot " + (i+1));
					middle.setTextColor(getResources().getColor(R.color.material_blue_grey_800));
					middle.setTextSize(20);

					TextView bottom = new TextView(this);
					bottom.setText("Tap to start new game");
					bottom.setTextSize(14);

					//add everything to the box
					box.addView(middle);
					box.addView(bottom);
				}

				//add to the list
				container.addView(box);
			}
		} catch (Exception exception)
		{
			Log.e("SelectGame", "Error while loading and displaying data from existing games.\n" + exception.getLocalizedMessage());
		}
	}

	private LinearLayout createEmptyBox(int id)
	{
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(700, 200);
		params.setMargins(0, 0, 0, 10);

		LinearLayout box = new LinearLayout(this);

		//load empty game slot
		box.setPadding(10, 10, 10, 10);
		box.setBackground(getDrawable(R.drawable.black_border));
		box.setOrientation(LinearLayout.VERTICAL);
		box.setLayoutParams(params);
		box.setId(getResources().getIdentifier("select_game_" + id, "id", getPackageName()));
		box.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (item_selected)
				{ //deselect item
					deselect();
				}
				else
				{
					//start a new game
					Intent i = new Intent(context, CreateGame.class);
					i.putExtra("game_id", getResources().getResourceName(v.getId()).split("_")[2]);
					startActivity(i);
				}
			}
		});

		return box;
	}

	public void deselect(View v)
	{
		deselect();
	}

	private void deselect()
	{
		//find what item is selected, deselect it
		for (int i = 0; i < getResources().getInteger(R.integer.game_slots); i++)
		{
			LinearLayout box = (LinearLayout) findViewById(getResources().getIdentifier("select_game_" + i, "id", getPackageName()));
			box.setSelected(false);
			box.setBackground(getDrawable(R.drawable.black_border));
		}

		//hide the delete icon
		MenuItem trash_can = menu.findItem(R.id.action_delete);
		trash_can.setVisible(false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_select_game, menu);
		this.menu = menu;
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{

		// Handle presses on the action bar items
		switch (item.getItemId()) {
			case R.id.action_delete:
			{
				int id = -1;

				try
				{
					for (int i = 0; i < games.getJSONArray("games").length(); i++)
					{
						LinearLayout box = (LinearLayout) findViewById(getResources().getIdentifier("select_game_" + i, "id", getPackageName()));
						if (box.isSelected())
						{
							id = i;
							item_selected = false;
						}
						if (id != -1)
						{
							//remove the box
							container.removeView(box);

							if (i == id)
							{
								LinearLayout replacement = createEmptyBox(i);
								container.addView(replacement);
							}
							else
							{ //we're past the one that needs deleting, just shift the others beneath it
								container.addView(box);
							}
						}
					}

					//hide the delete icon
					MenuItem trash_can = menu.findItem(R.id.action_delete);
					trash_can.setVisible(false);

					//remove it from the database
					games.getJSONArray("games").remove(id);
					db.edit().putString("json_game_saves", games.toString()).commit();
				} catch (Exception exception)
				{
					Log.e("SelectGame", "Error while removing game from database.\n" + exception.getLocalizedMessage());
				}

				//reload the game slots
				onStart();

				return true;
			}
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
