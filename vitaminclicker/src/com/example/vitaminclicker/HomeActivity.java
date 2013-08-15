package com.example.vitaminclicker;

import static com.example.vitaminclicker.VitaminCountContract.CountEntry.COLUMN_NAME_ENTRY_COUNT;

import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.vitaminclicker.VitaminCountContract.CountEntry;

public class HomeActivity extends Activity {

	private static final int VITAMIN_COUNT_NEEDED = 5;
	private int vitaminCount;

	private long vitaminCountId;

	private VitaminDatabase db;

	private ProgressBar progressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

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

		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		progressBar.setMax(VITAMIN_COUNT_NEEDED);

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

	@Override
	public void onDestroy() {
		super.onDestroy();
		db.close();
	}
}
