package com.example.vitaminclicker;

import static com.example.vitaminclicker.VitaminCountContract.CountEntry.COLUMN_NAME_ENTRY_COUNT;
import static com.example.vitaminclicker.VitaminCountContract.CountEntry.COLUMN_NAME_ENTRY_DATE;
import static com.example.vitaminclicker.VitaminDatabase.YYYY_MM_DD_FORMAT;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.chart.TimeChart;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;

public class StatisticsActivity extends Activity {
	public static final String TAG = "StatisticsActivity";

	private static final String MONTH_FORMAT = "MMMMM";

	private static final String DAY_FORMAT = "d";

	private static final String DATASET_KEY = "dataset";
	private static final String RENDERER_KEY = "renderer";
	private static final String SERIES_KEY = "series";

	private static final int X_AXIS_DAYS_COUNT = 10;
	private static final int Y_AXIS_VITAMIN_COUNT = 8;

	private static final int TWELVE_HOURS = 12 * 60 * 60 * 1000;

	private XYMultipleSeriesDataset mDataset;
	private XYMultipleSeriesRenderer mRenderer;
	private TimeSeries mSeries;
	private GraphicalView mGraphicalView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			// restore the current data, for instance when changing the screen
			// orientation
			mDataset = (XYMultipleSeriesDataset) savedInstanceState
					.getSerializable(DATASET_KEY);
			mRenderer = (XYMultipleSeriesRenderer) savedInstanceState
					.getSerializable(RENDERER_KEY);
			mSeries = (TimeSeries) savedInstanceState
					.getSerializable(SERIES_KEY);
		}

		if (mDataset == null || mRenderer == null || mSeries == null) {

			Calendar aCalendar = Calendar.getInstance();
			resetTime(aCalendar);
			aCalendar.set(Calendar.DATE,
					aCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
			Date lastDateOfCurrentMonth = aCalendar.getTime();
			int daysInCurrentMonth = aCalendar.get(Calendar.DAY_OF_MONTH);

			aCalendar.set(Calendar.DAY_OF_MONTH, 1);
			aCalendar.add(Calendar.MONTH, -1);
			Date firstDateOfPreviousMonth = aCalendar.getTime();

			Date date = null;
			Date dayOfMonth = firstDateOfPreviousMonth;
			int maxVitaminCount = 0;

			String title = DateFormat.format(MONTH_FORMAT, new Date())
					.toString();
			mSeries = new TimeSeries(title);

			Calendar now = Calendar.getInstance();
			VitaminDatabase db = new VitaminDatabase(this);
			Cursor vitaminCountsCursor = db.readVitaminCounts(
					firstDateOfPreviousMonth, now.getTime());
			resetTime(now);

			if (vitaminCountsCursor.getCount() > 0) {
				vitaminCountsCursor.moveToFirst();

				do {
					try {
						date = YYYY_MM_DD_FORMAT
								.parse(vitaminCountsCursor.getString(vitaminCountsCursor
										.getColumnIndexOrThrow(COLUMN_NAME_ENTRY_DATE)));
					} catch (ParseException e) {
						Log.w(TAG, e.getMessage(), e);
					}
					int count = vitaminCountsCursor.getInt(vitaminCountsCursor
							.getColumnIndexOrThrow(COLUMN_NAME_ENTRY_COUNT));

					if (date != null) {
						Calendar dayCalendar = Calendar.getInstance();
						dayCalendar.setTime(date);
						Date currentDayOfMonth = dayCalendar.getTime();

						if (compareDays(currentDayOfMonth, dayOfMonth) > 0) {
							// fill the gaps
							for (; compareDays(dayOfMonth, currentDayOfMonth) < 0;) {
								mSeries.add(dayOfMonth, 0);

								dayOfMonth = increaseDay(dayOfMonth, 1);
							}
						}

						if (count > maxVitaminCount) {
							maxVitaminCount = count;
						}

						mSeries.add(dayOfMonth, count);
						dayOfMonth = increaseDay(dayOfMonth, 1);
					}

				} while (vitaminCountsCursor.moveToNext());

			}

			vitaminCountsCursor.close();
			db.close();

			for (; compareDays(dayOfMonth, now.getTime()) <= 0;) {
				// fill the last gaps
				mSeries.add(dayOfMonth, 0);

				dayOfMonth = increaseDay(dayOfMonth, 1);
			}

			mDataset = new XYMultipleSeriesDataset();
			mDataset.addSeries(mSeries);

			int[] colors = new int[] { Color.BLUE };
			PointStyle[] styles = new PointStyle[] { PointStyle.CIRCLE };
			mRenderer = buildRenderer(colors, styles);
			int length = mRenderer.getSeriesRendererCount();
			for (int i = 0; i < length; i++) {
				((XYSeriesRenderer) mRenderer.getSeriesRendererAt(i))
						.setFillPoints(true);
			}

			double minX;
			double maxX;
			Date actualDayOfMonth = now.getTime();
			int actualDayOfMonthIndex = now.get(Calendar.DAY_OF_MONTH);
			if (actualDayOfMonthIndex + (X_AXIS_DAYS_COUNT / 2) > daysInCurrentMonth) {
				minX = increaseDay(lastDateOfCurrentMonth, -X_AXIS_DAYS_COUNT)
						.getTime();
				maxX = lastDateOfCurrentMonth.getTime();
			} else {
				minX = increaseDay(actualDayOfMonth, -(X_AXIS_DAYS_COUNT / 2))
						.getTime();
				maxX = increaseDay(actualDayOfMonth, (X_AXIS_DAYS_COUNT / 2))
						.getTime();
			}

			setChartSettings(mRenderer, title, getString(R.string.day),
					getString(R.string.vitamin_counts), minX, maxX, 0,
					Y_AXIS_VITAMIN_COUNT, Color.BLACK,
					getResources().getColor(R.color.black));

			mRenderer.setPanLimits(new double[] {
					firstDateOfPreviousMonth.getTime() - TWELVE_HOURS,
					lastDateOfCurrentMonth.getTime() + TWELVE_HOURS,
					0,
					maxVitaminCount > Y_AXIS_VITAMIN_COUNT ? maxVitaminCount
							: Y_AXIS_VITAMIN_COUNT });

		}
		
		
		TimeChart chart = new TimeChart(mDataset, mRenderer) {
			public void draw(Canvas canvas, int x, int y, int width,
					int height, Paint paint) {
				Calendar latestDateShown = Calendar.getInstance();
				latestDateShown.setTimeInMillis((long) mRenderer.getXAxisMax());
				String currentMonth = DateFormat.format(MONTH_FORMAT,
						latestDateShown).toString();
				mRenderer.setChartTitle(currentMonth);
				super.draw(canvas, x, y, width, height, paint);
			}
		};
		chart.setDateFormat(DAY_FORMAT);
		mGraphicalView = new GraphicalView(this, chart);

		mGraphicalView.setBackgroundDrawable(getResources().getDrawable(
				R.color.white));

		setContentView(mGraphicalView);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// save the current data, for instance when changing screen orientation
		outState.putSerializable(DATASET_KEY, mDataset);
		outState.putSerializable(RENDERER_KEY, mRenderer);
		outState.putSerializable(SERIES_KEY, mSeries);
	}

	private void resetTime(Calendar c) {
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
	}

	private Date increaseDay(Date date, int value) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DAY_OF_MONTH, value);
		return c.getTime();
	}

	private int compareDays(Date d1, Date d2) {
		Date date1 = new Date(d1.getYear(), d1.getMonth(), d1.getDate());
		Date date2 = new Date(d2.getYear(), d2.getMonth(), d2.getDate());
		return date1.compareTo(date2);
	}

	/**
	 * Builds an XY multiple series renderer.
	 * 
	 * @param colors
	 *            the series rendering colors
	 * @param styles
	 *            the series point styles
	 * @return the XY multiple series renderers
	 */
	protected XYMultipleSeriesRenderer buildRenderer(int[] colors,
			PointStyle[] styles) {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		setRenderer(renderer, colors, styles);

		renderer.setXLabels(X_AXIS_DAYS_COUNT);
		renderer.setYLabels(Y_AXIS_VITAMIN_COUNT);
		renderer.setXLabelsColor(Color.BLACK);
		renderer.setYLabelsColor(0, Color.BLACK);
		renderer.setShowGrid(true);
		renderer.setShowLegend(false);
		renderer.setXLabelsAlign(Align.RIGHT);
		renderer.setYLabelsAlign(Align.RIGHT);
		return renderer;
	}

	/**
	 * @param renderer
	 * @param colors
	 * @param styles
	 */
	protected void setRenderer(XYMultipleSeriesRenderer renderer, int[] colors,
			PointStyle[] styles) {
		renderer.setAxisTitleTextSize(16);
		renderer.setChartTitleTextSize(20);
		renderer.setLabelsTextSize(15);
		renderer.setLegendTextSize(15);
		renderer.setPointSize(5f);
		renderer.setMargins(new int[] { 20, 30, 15, 20 });
		renderer.setMarginsColor(Color.WHITE);
		int length = colors.length;
		for (int i = 0; i < length; i++) {
			XYSeriesRenderer r = new XYSeriesRenderer();
			r.setColor(colors[i]);
			r.setPointStyle(styles[i]);
			renderer.addSeriesRenderer(r);
		}
	}

	/**
	 * Sets a few of the series renderer settings.
	 * 
	 * @param renderer
	 *            the renderer to set the properties to
	 * @param title
	 *            the chart title
	 * @param xTitle
	 *            the title for the X axis
	 * @param yTitle
	 *            the title for the Y axis
	 * @param xMin
	 *            the minimum value on the X axis
	 * @param xMax
	 *            the maximum value on the X axis
	 * @param yMin
	 *            the minimum value on the Y axis
	 * @param yMax
	 *            the maximum value on the Y axis
	 * @param axesColor
	 *            the axes color
	 * @param labelsColor
	 *            the labels color
	 */
	protected void setChartSettings(XYMultipleSeriesRenderer renderer,
			String title, String xTitle, String yTitle, double xMin,
			double xMax, double yMin, double yMax, int axesColor,
			int labelsColor) {
		renderer.setChartTitle(title);
		renderer.setXTitle(xTitle);
		renderer.setYTitle(yTitle);
		renderer.setXAxisMin(xMin);
		renderer.setXAxisMax(xMax);
		renderer.setYAxisMin(yMin);
		renderer.setYAxisMax(yMax);
		renderer.setAxesColor(axesColor);
		renderer.setLabelsColor(labelsColor);
	}
}
