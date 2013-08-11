package com.example.vitaminclicker;

import static com.example.vitaminclicker.VitaminCountContract.CountEntry.COLUMN_NAME_ENTRY_COUNT;
import static com.example.vitaminclicker.VitaminCountContract.CountEntry.COLUMN_NAME_ENTRY_DATE;
import static com.example.vitaminclicker.VitaminDatabase.YYYY_MM_DD_FORMAT;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;

public class StatisticsActivity extends Activity {
	public static final String TAG = "StatisticsActivity";

	private Cursor vitaminCountsCursor;
	private VitaminDatabase db;

	private static final String MONTH_FORMAT = "MMMMM";

	private static final int X_AXIS_DAYS_COUNT = 10;
	private static final int Y_AXIS_VITAMIN_COUNT = 8;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		db = new VitaminDatabase(this);
		Calendar aCalendar = Calendar.getInstance();
		// set DATE to 1, so first date of previous month
		aCalendar.set(Calendar.DATE, 1);
		Date firstDateOfCurrentMonth = aCalendar.getTime();
		// set actual maximum date of previous month
		aCalendar.set(Calendar.DATE,
				aCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
		// read it
		Date lastDateOfCurrentMonth = aCalendar.getTime();
		int daysInCurrentMonth = aCalendar.get(Calendar.DAY_OF_MONTH);
		Calendar now = Calendar.getInstance();
		vitaminCountsCursor = db.readVitaminCounts(firstDateOfCurrentMonth,
				lastDateOfCurrentMonth);

		Date date = null;
		int dayOfMonth = 1;
		int maxVitaminCount = 0;

		double[] days = new double[now.get(Calendar.DAY_OF_MONTH)];
		double[] vitaminCounts = new double[now.get(Calendar.DAY_OF_MONTH)];
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
					int currentDayOfMonth = dayCalendar
							.get(Calendar.DAY_OF_MONTH);

					if (currentDayOfMonth > dayOfMonth) {
						// fill the gaps
						for (; dayOfMonth < currentDayOfMonth; dayOfMonth++) {
							days[dayOfMonth - 1] = dayOfMonth;
							vitaminCounts[dayOfMonth - 1] = 0;
						}
					}

					if (count > maxVitaminCount) {
						maxVitaminCount = count;
					}

					vitaminCounts[currentDayOfMonth - 1] = count;
					days[currentDayOfMonth - 1] = currentDayOfMonth;
					dayOfMonth = currentDayOfMonth + 1;
				}

			} while (vitaminCountsCursor.moveToNext());

		}

		for (; dayOfMonth <= now.get(Calendar.DAY_OF_MONTH); dayOfMonth++) {
			// fill the last gaps
			days[dayOfMonth - 1] = dayOfMonth;
			vitaminCounts[dayOfMonth - 1] = 0;
		}

		String currentMonth = DateFormat.format(MONTH_FORMAT, new Date())
				.toString();

		String[] titles = new String[] { currentMonth };
		List<double[]> xValues = new ArrayList<double[]>();
		xValues.add(days);
		List<double[]> yValues = new ArrayList<double[]>();
		yValues.add(vitaminCounts);
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		addXYSeries(dataset, titles, xValues, yValues, 0);

		int[] colors = new int[] { Color.BLUE };
		PointStyle[] styles = new PointStyle[] { PointStyle.CIRCLE };
		XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);
		int length = renderer.getSeriesRendererCount();
		for (int i = 0; i < length; i++) {
			((XYSeriesRenderer) renderer.getSeriesRendererAt(i))
					.setFillPoints(true);
		}
		setChartSettings(renderer, currentMonth, getString(R.string.day),
				getString(R.string.vitamin_counts), 0.9,
				X_AXIS_DAYS_COUNT + 0.1, 0, Y_AXIS_VITAMIN_COUNT, Color.BLACK,
				getResources().getColor(R.color.black));
		renderer.setXLabels(X_AXIS_DAYS_COUNT);
		renderer.setYLabels(Y_AXIS_VITAMIN_COUNT);
		renderer.setXLabelsColor(Color.BLACK);
		renderer.setYLabelsColor(0,Color.BLACK);
		renderer.setShowGrid(true);
		renderer.setShowLegend(false);
		renderer.setXLabelsAlign(Align.RIGHT);
		renderer.setYLabelsAlign(Align.RIGHT);
		renderer.setPanLimits(new double[] {
				0.9,
				daysInCurrentMonth + 0.1,
				0,
				maxVitaminCount > Y_AXIS_VITAMIN_COUNT ? maxVitaminCount
						: Y_AXIS_VITAMIN_COUNT});

		GraphicalView graphicalView = ChartFactory.getCubeLineChartView(this, dataset,
				renderer, 0.01f);
		graphicalView.setBackgroundDrawable(getResources().getDrawable(R.color.white));
		setContentView(graphicalView);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		vitaminCountsCursor.close();
		db.close();
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

	public void addXYSeries(XYMultipleSeriesDataset dataset, String[] titles,
			List<double[]> xValues, List<double[]> yValues, int scale) {
		int length = titles.length;
		for (int i = 0; i < length; i++) {
			XYSeries series = new XYSeries(titles[i], scale);
			double[] xV = xValues.get(i);
			double[] yV = yValues.get(i);
			int seriesLength = xV.length;
			for (int k = 0; k < seriesLength; k++) {
				series.add(xV[k], yV[k]);
			}
			dataset.addSeries(series);
		}
	}
}
