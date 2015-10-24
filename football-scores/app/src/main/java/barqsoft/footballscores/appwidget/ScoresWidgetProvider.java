package barqsoft.footballscores.appwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViews;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;

public class ScoresWidgetProvider extends AppWidgetProvider {

    private static final String TAG = ScoresWidgetProvider.class.getSimpleName();

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // update scores
//        context.startService(new Intent(context, FetchService.class));

        Cursor c = context.getContentResolver().query(DatabaseContract.BASE_CONTENT_URI,
                null, null, null,
                DatabaseContract.ScoresTable.DATE_COL + " DESC, " + DatabaseContract.ScoresTable.TIME_COL + " DESC");

        // values for record with latest score
        String latestHome = "";
        String latestAway = "";
        String latestScore = "";
        String latestTime = "";

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
                    latestHome = home;
                    latestAway = away;
                    latestScore = Utilies.getScores(homeGoals, awayGoals);
                    latestTime = time;
                    break;
                }
            } while (c.moveToNext());
            c.close();
        }

        Log.d(TAG, "Latest: " + latestHome + " " + latestScore + " " + latestAway);

        final int N = appWidgetIds.length;
        for (int appWidgetId : appWidgetIds) {
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.scores_appwidget);
            views.setOnClickPendingIntent(R.id.widget_background, pendingIntent);
            views.setTextViewText(R.id.home_name, latestHome);
            views.setTextViewText(R.id.away_name, latestAway);
            views.setTextViewText(R.id.score_textview, latestScore);
            views.setTextViewText(R.id.data_textview, latestTime);
            views.setImageViewResource(R.id.home_crest, Utilies.getTeamCrestByTeamName(latestHome));
            views.setImageViewResource(R.id.away_crest, Utilies.getTeamCrestByTeamName(latestAway));

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
