package com.example.awakeplace.pokerchipsmanager;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.awakeplace.pokerchipsmanager.R;

public class RaiseBet extends ActionBarActivity
{
	private int low_bet;
	private int high_bet;
	private int interval;
	private String name;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_poker_raise_bet);

		low_bet = getIntent().getIntExtra("low_bet", 0);
		high_bet = getIntent().getIntExtra("high_bet", 5);
		interval = getIntent().getIntExtra("interval", 1);
		name = getIntent().getStringExtra("name");
	}

	protected void onStart()
	{
		super.onStart();

		//set up all the Layout stuff
		TextView text = (TextView) findViewById(R.id.low_bet);
		text.setText(Integer.toString(low_bet));

		text = (TextView) findViewById(R.id.high_bet);
		text.setText(Integer.toString(high_bet));

		text = (TextView) findViewById(R.id.player_name);
		text.setText(name + ",");

		text = (TextView) findViewById(R.id.current);
		text.setText(low_bet + " chips");

		SeekBar seekBar = (SeekBar) findViewById(R.id.raise_bet_seekbar);
		seekBar.setMax((high_bet - low_bet) / interval + 1);
		seekBar.setProgress(0);
		seekBar.setSecondaryProgress(0);

		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
		{
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
			{
				if (fromUser)
				{
					TextView t = (TextView) findViewById(R.id.current);
					if (progress == seekBar.getMax())
						t.setText(high_bet + " chips");
					else
						t.setText((progress * interval + low_bet) + " chips");
				}
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar)
			{

			}
		});
	}

	public void onRaiseBetClick(View button)
	{
		SeekBar seekBar = (SeekBar) findViewById(R.id.raise_bet_seekbar);

		Intent result = new Intent();

		if (seekBar.getProgress() == seekBar.getMax()) //is this player all in?
			result.putExtra("bet_amount", high_bet);
		else
			result.putExtra("bet_amount", seekBar.getProgress() * interval + low_bet);

		setResult(Activity.RESULT_OK, result);
		finish();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_poker_raise_bet, menu);
		return true;
	}

	public void raise(View button)
	{
		SeekBar seekBar = (SeekBar) findViewById(R.id.raise_bet_seekbar);

		if (seekBar.getProgress() < seekBar.getMax())
		{
			seekBar.setProgress(seekBar.getProgress() + 1);
			seekBar.setSecondaryProgress(seekBar.getProgress() + 1);
		}

		//update the current bet text
		TextView t = (TextView) findViewById(R.id.current);

		if (seekBar.getProgress() == seekBar.getMax())
			t.setText(high_bet + " chips");
		else
			t.setText((low_bet + interval * seekBar.getProgress()) + " chips");
	}

	public void lower(View button)
	{
		SeekBar seekBar = (SeekBar) findViewById(R.id.raise_bet_seekbar);

		if (seekBar.getProgress() > 0)
		{
			seekBar.setProgress(seekBar.getProgress() - 1);
			seekBar.setSecondaryProgress(seekBar.getProgress() - 1);
		}

		//update the current bet text
		TextView t = (TextView) findViewById(R.id.current);
		t.setText((low_bet + interval * seekBar.getProgress()) + " chips");
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
