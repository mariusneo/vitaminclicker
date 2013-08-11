package com.example.vitaminclicker;

import static com.example.vitaminclicker.VitaminCountContract.CountEntry.COLUMN_NAME_ENTRY_COUNT;
import static com.example.vitaminclicker.VitaminCountContract.CountEntry.COLUMN_NAME_ENTRY_DATE;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.example.vitaminclicker.VitaminCountContract.CountEntry;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class VitaminDatabase extends SQLiteAssetHelper {

	private static final String DATABASE_NAME = "vitamin";
	private static final int DATABASE_VERSION = 1;

	public static final SimpleDateFormat YYYY_MM_DD_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd");

	public VitaminDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);

		// you can use an alternate constructor to specify a database location
		// (such as a folder on the sd card)
		// you must ensure that this folder is available and you have permission
		// to write to it
		// super(context, DATABASE_NAME,
		// context.getExternalFilesDir(null).getAbsolutePath(), null,
		// DATABASE_VERSION);

	}

	public Cursor readVitaminCounts() {

		SQLiteDatabase db = getReadableDatabase();
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		String[] projection = { CountEntry._ID, COLUMN_NAME_ENTRY_DATE,
				COLUMN_NAME_ENTRY_COUNT };
		String sqlTables = CountEntry.TABLE_NAME;

		qb.setTables(sqlTables);
		Cursor c = qb.query(db, projection, null, null, null, null, null);

		c.moveToFirst();
		return c;

	}

	public Cursor readVitaminCounts(Date startDate, Date endDate) {

		SQLiteDatabase db = getReadableDatabase();
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		String[] projection = { CountEntry._ID, COLUMN_NAME_ENTRY_DATE,
				COLUMN_NAME_ENTRY_COUNT };
		String sqlTables = CountEntry.TABLE_NAME;

		qb.setTables(sqlTables);
		Cursor c = qb.query(db, projection, COLUMN_NAME_ENTRY_DATE
				+ " BETWEEN ? AND ? ",
				new String[] { YYYY_MM_DD_FORMAT.format(startDate),
						YYYY_MM_DD_FORMAT.format(endDate) }, null, null,
				CountEntry.DEFAULT_SORT_ORDER);

		c.moveToFirst();
		return c;

	}

	public Cursor readVitaminCount(Date date) {
		// Constructs a new query builder and sets its table name
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(CountEntry.TABLE_NAME);

		// Opens the database object in "read" mode, since no writes need to be
		// done.
		SQLiteDatabase db = getReadableDatabase();
		String[] projection = { CountEntry._ID, COLUMN_NAME_ENTRY_DATE,
				COLUMN_NAME_ENTRY_COUNT };
		/*
		 * Performs the query. If no problems occur trying to read the database,
		 * then a Cursor object is returned; otherwise, the cursor variable
		 * contains null. If no records were selected, then the Cursor object is
		 * empty, and Cursor.getCount() returns 0.
		 */
		Cursor c = qb.query(
				db, // The database to query
				projection, // The columns to return from the query
				COLUMN_NAME_ENTRY_DATE + " LIKE ? ",
				new String[] { YYYY_MM_DD_FORMAT.format(date) }, null, null,
				null);

		return c;

	}

	public long insertVitaminCount(Date date, int count) {
		// Gets the data repository in write mode
		SQLiteDatabase db = getWritableDatabase();

		// Create a new map of values, where column names are the keys
		ContentValues values = new ContentValues();
		values.put(COLUMN_NAME_ENTRY_DATE, YYYY_MM_DD_FORMAT.format(date));
		values.put(COLUMN_NAME_ENTRY_COUNT, count);

		// Insert the new row, returning the primary key value of the new row
		return db.insert(CountEntry.TABLE_NAME, COLUMN_NAME_ENTRY_DATE, values);
	}

	public int updateVitaminCount(long id, int count) {
		// Gets the data repository in write mode
		SQLiteDatabase db = getWritableDatabase();

		// Create a new map of values, where column names are the keys
		ContentValues values = new ContentValues();
		values.put(COLUMN_NAME_ENTRY_COUNT, count);

		// Which row to update, based on the ID
		String selection = CountEntry._ID + " = ?";
		String[] selectionArgs = { String.valueOf(id) };

		return db.update(CountEntry.TABLE_NAME, values, selection,
				selectionArgs);
	}

}
