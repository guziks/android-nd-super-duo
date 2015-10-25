package barqsoft.footballscores.appwidget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RemoteViews;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;
import barqsoft.footballscores.service.FetchService;

public class ScoresWidgetProvider extends AppWidgetProvider {

    private static final String TAG = ScoresWidgetProvider.class.getSimpleName();

    // values for record with latest score
    String mLatestHome = "";
    String mLatestAway = "";
    String mLatestScore = "";
    String mLatestTime = "";

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // update scores
        context.startService(new Intent(context, FetchService.class));

        getLatest(context);

        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        getLatest(context);
        updateWidget(context, appWidgetManager, appWidgetId);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(FetchService.ACTION_DATA_FETCHED)) {
            Log.d(TAG, "data fetched");
        }
    }

    private void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        int layoutId = getLayoutId(context, appWidgetManager, appWidgetId);

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        RemoteViews views = new RemoteViews(context.getPackageName(), layoutId);
        views.setOnClickPendingIntent(R.id.widget_background, pendingIntent);
        views.setTextViewText(R.id.home_name, mLatestHome);
        views.setTextViewText(R.id.away_name, mLatestAway);
        views.setTextViewText(R.id.score_textview, mLatestScore);
        views.setTextViewText(R.id.data_textview, mLatestTime);
        views.setImageViewResource(R.id.home_crest, Utilies.getTeamCrestByTeamName(mLatestHome));
        views.setImageViewResource(R.id.away_crest, Utilies.getTeamCrestByTeamName(mLatestAway));

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private void getLatest(Context context) {
        Cursor c = context.getContentResolver().query(DatabaseContract.BASE_CONTENT_URI,
                null, null, null,
                DatabaseContract.ScoresTable.DATE_COL + " DESC, " + DatabaseContract.ScoresTable.TIME_COL + " DESC");

        if (c != null) {
            Log.d(TAG, "Cursor count = " + c.getCount());
            c.moveToFirst();
            do {
                String date      = c.getString(c.getColumnIndex(DatabaseContract.ScoresTable.DATE_COL));
                String time      = c.getString(c.getColumnIndex(DatabaseContract.ScoresTable.TIME_COL));
                String home      = c.getString(c.getColumnIndex(DatabaseContract.ScoresTable.HOME_COL));
                String away      = c.getString(c.getColumnIndex(DatabaseContract.ScoresTable.AWAY_COL));
                int    homeGoals = c.getInt(c.getColumnIndex(DatabaseContract.ScoresTable.HOME_GOALS_COL));
                int    awayGoals = c.getInt(c.getColumnIndex(DatabaseContract.ScoresTable.AWAY_GOALS_COL));
                Log.d(TAG, "DateTime: " + date + " " + time + " " + "Goals: " + homeGoals + "-" + awayGoals);
                if (homeGoals != -1 && awayGoals != -1) {
                    mLatestHome = home;
                    mLatestAway = away;
                    mLatestScore = Utilies.getScores(homeGoals, awayGoals);
                    mLatestTime = time;
                    break;
                }
            } while (c.moveToNext());
            c.close();
        }

        Log.d(TAG, "Latest: " + mLatestHome + " " + mLatestScore + " " + mLatestAway);
    }

    private int getLayoutId(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        int widgetWidth = getWidgetWidth(context, appWidgetManager, appWidgetId);
        int defaultWidth = context.getResources().getDimensionPixelSize(R.dimen.widget_default_width);
        int largeWidth = context.getResources().getDimensionPixelSize(R.dimen.widget_large_width);
        int layoutId;
        if (widgetWidth >= largeWidth) {
            layoutId = R.layout.scores_appwidget_large;
        } else if (widgetWidth >= defaultWidth) {
            layoutId = R.layout.scores_appwidget;
        } else {
            layoutId = R.layout.scores_appwidget_small;
        }
        return layoutId;
    }

    private int getWidgetWidth(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        // Prior to Jelly Bean, widgets were always their default size
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return context.getResources().getDimensionPixelSize(R.dimen.widget_default_width);
        }
        // For Jelly Bean and higher devices, widgets can be resized - the current size can be
        // retrieved from the newly added App Widget Options
        return getWidgetWidthFromOptions(context, appWidgetManager, appWidgetId);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private int getWidgetWidthFromOptions(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        if (options.containsKey(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)) {
            int minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            // The width returned is in dp, but we'll convert it to pixels to match the other widths
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, minWidthDp,
                    displayMetrics);
        }
        return context.getResources().getDimensionPixelSize(R.dimen.widget_default_width);
    }
}
