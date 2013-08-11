package com.example.vitaminclicker;

import android.provider.BaseColumns;

public class VitaminCountContract {
	private VitaminCountContract(){}
	
	public static abstract class CountEntry implements BaseColumns{
		public static final String TABLE_NAME = "vitamincount";
		public static final String COLUMN_NAME_ENTRY_DATE = "entrydate";
		public static final String COLUMN_NAME_ENTRY_COUNT = "entrycount";
		
		/**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "entrydate ASC";
	}
}