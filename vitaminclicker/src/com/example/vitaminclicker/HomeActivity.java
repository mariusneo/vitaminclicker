package com.example.vitaminclicker;

import static com.example.vitaminclicker.VitaminCountContract.CountEntry.COLUMN_NAME_ENTRY_COUNT;

import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.vitaminclicker.VitaminCountContract.CountEntry;

public class HomeActivity extends Activity {

	private static final int VITAMIN_COUNT_NEEDED = 5;
	
	private static final int MENU_INFO = 1;
	
	private int vitaminCount;

	private long vitaminCountId;

	private VitaminDatabase db;

	private ProgressBar progressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		progressBar.setMax(VITAMIN_COUNT_NEEDED);

	}
	
	/**
     * Invoked during init to give the Activity a chance to set up its Menu.
     *
     * @param menu the Menu to which entries may be added
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, MENU_INFO, 0, R.string.menu_info);

        return true;
    }

	
	@Override
	public void onStart(){
		super.onStart();
		
		db = new VitaminDatabase(this);
		Cursor todaysVitaminCursor = db.readVitaminCount(new Date());

		if (todaysVitaminCursor.getCount() > 0) {
			todaysVitaminCursor.moveToFirst();

			vitaminCountId = todaysVitaminCursor.getInt(todaysVitaminCursor
					.getColumnIndexOrThrow(CountEntry._ID));
			vitaminCount = todaysVitaminCursor.getInt(todaysVitaminCursor
					.getColumnIndexOrThrow(COLUMN_NAME_ENTRY_COUNT));
		} else {
			vitaminCount = 0;
		}

	}
	
	@Override
	public void onResume(){
		super.onResume();
		int progress = vitaminCount > VITAMIN_COUNT_NEEDED ? VITAMIN_COUNT_NEEDED
				: vitaminCount;
		progressBar.setProgress(progress);

		TextView vitaminCountText = (TextView) findViewById(R.id.vitaminCount);
		vitaminCountText.setText(Integer.toString(vitaminCount));
	}

	public void increaseVitaminCount(View view) {
		TextView vitaminCountText = (TextView) findViewById(R.id.vitaminCount);
		vitaminCount++;
		vitaminCountText.setText(Integer.toString(vitaminCount));

		if (vitaminCount == 1) {
			vitaminCountId = db.insertVitaminCount(new Date(), vitaminCount);
		} else {
			db.updateVitaminCount(vitaminCountId, vitaminCount);
		}

		int progress = vitaminCount > VITAMIN_COUNT_NEEDED ? VITAMIN_COUNT_NEEDED
				: vitaminCount;
		progressBar.setProgress(progress);
	}

	public void showStatistics(View view) {
		Intent intent = new Intent(HomeActivity.this, StatisticsActivity.class);
		startActivity(intent);
	}

	
	 /**
     * Invoked when the user selects an item from the Menu.
     *
     * @param item the Menu entry which was selected
     * @return true if the Menu item was legit (and we consumed it), false
     *         otherwise
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_INFO:
            	Intent intent = new Intent(HomeActivity.this, InfoActivity.class);
        		startActivity(intent);
                return true;
        }
        return false;
    }
	
	@Override
	public void onStop() {
		super.onStop();
		db.close();
	}
}
