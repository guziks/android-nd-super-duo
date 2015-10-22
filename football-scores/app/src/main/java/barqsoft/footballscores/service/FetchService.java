package barqsoft.footballscores.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;

/**
 * Created by yehya khaled on 3/2/2015.
 */
public class FetchService extends IntentService {
    
    public static final String LOG_TAG = "FetchService";

    public FetchService() {
        super("FetchService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        getData("n2");
        getData("p2");

        return;
    }

    private void getData (String timeFrame) {
        //Creating fetch URL
        final String BASE_URL = "http://api.football-data.org/alpha/fixtures"; //Base URL
        final String QUERY_TIME_FRAME = "timeFrame"; //Time Frame parameter to determine days
        //final String QUERY_MATCH_DAY = "matchday";

        Uri fetchUri = Uri.parse(BASE_URL).buildUpon().
                appendQueryParameter(QUERY_TIME_FRAME, timeFrame).build();
        //Log.v(LOG_TAG, "The url we are looking at is: "+fetchUri.toString()); //log spam
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        String jsonData = null;
        //Opening Connection
        try {
            URL fetch = new URL(fetchUri.toString());
            connection = (HttpURLConnection) fetch.openConnection();
            connection.setRequestMethod("GET");
            connection.addRequestProperty("X-Auth-Token", getString(R.string.api_key));
            connection.connect();

            // Read the input stream into a String
            InputStream inputStream = connection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }
            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return;
            }
            jsonData = buffer.toString();
        } catch (Exception e) {
            Log.e(LOG_TAG,"Exception here" + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            } if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG,"Error Closing Stream");
                }
            }
        }
        try {
            if (jsonData != null) {
                //This bit is to check if the data contains any matches. If not, we call processJson on the dummy data
                JSONArray matches = new JSONObject(jsonData).getJSONArray("fixtures");
                if (matches.length() == 0) {
                    //if there is no data, call the function on dummy data
                    //this is expected behavior during the off season.
                    processJSONdata(getString(R.string.dummy_data), getApplicationContext(), false);
                    return;
                }

                processJSONdata(jsonData, getApplicationContext(), true);
            } else {
                //Could not Connect
                Log.d(LOG_TAG, "Could not connect to server.");
            }
        } catch(Exception e) {
            Log.e(LOG_TAG,e.getMessage());
        }
    }

    private void processJSONdata (String JSONdata,Context mContext, boolean isReal) {
        //JSON data
        // This set of league codes is for the 2015/2016 season. In fall of 2016, they will need to
        // be updated. Feel free to use the codes
        final String BUNDESLIGA1 = "394";
        final String BUNDESLIGA2 = "395";
        final String LIGUE1 = "396";
        final String LIGUE2 = "397";
        final String PREMIER_LEAGUE = "398";
        final String PRIMERA_DIVISION = "399";
        final String SEGUNDA_DIVISION = "400";
        final String SERIE_A = "401";
        final String PRIMERA_LIGA = "402";
        final String Bundesliga3 = "403";
        final String EREDIVISIE = "404";

        final String SEASON_LINK = "http://api.football-data.org/alpha/soccerseasons/";
        final String MATCH_LINK = "http://api.football-data.org/alpha/fixtures/";
        final String FIXTURES = "fixtures";
        final String LINKS = "_links";
        final String SOCCER_SEASON = "soccerseason";
        final String SELF = "self";
        final String MATCH_DATE = "date";
        final String HOME_TEAM = "homeTeamName";
        final String AWAY_TEAM = "awayTeamName";
        final String RESULT = "result";
        final String HOME_GOALS = "goalsHomeTeam";
        final String AWAY_GOALS = "goalsAwayTeam";
        final String MATCH_DAY = "matchday";

        //Match data
        String league = null;
        String date = null;
        String time = null;
        String home = null;
        String away = null;
        String homeGoals = null;
        String awayGoals = null;
        String matchId = null;
        String matchDay = null;

        try {
            JSONArray matches = new JSONObject(JSONdata).getJSONArray(FIXTURES);

            //ContentValues to be inserted
            Vector<ContentValues> values = new Vector <ContentValues> (matches.length());
            for (int i = 0; i < matches.length(); i++) {
                JSONObject matchData = matches.getJSONObject(i);
                league = matchData.getJSONObject(LINKS)
                        .getJSONObject(SOCCER_SEASON)
                        .getString("href");
                league = league.replace(SEASON_LINK, "");

                //This if statement controls which leagues we're interested in the data from.
                //add leagues here in order to have them be added to the DB.
                // If you are finding no data in the app, check that this contains all the leagues.
                // If it doesn't, that can cause an empty DB, bypassing the dummy data routine.
                if ( league.equals(PREMIER_LEAGUE)  ||
                     league.equals(SERIE_A)         ||
                     league.equals(BUNDESLIGA1)     ||
                     league.equals(BUNDESLIGA2)     ||
                     league.equals(PRIMERA_DIVISION) ) {

                    matchId = matchData.getJSONObject(LINKS)
                            .getJSONObject(SELF)
                            .getString("href");
                    matchId = matchId.replace(MATCH_LINK, "");
                    if (!isReal) {
                        //This if statement changes the match ID of the dummy data so that it all goes into the database
                        matchId=matchId+Integer.toString(i);
                    }

                    date = matchData.getString(MATCH_DATE);
                    time = date.substring(date.indexOf("T") + 1, date.indexOf("Z"));
                    date = date.substring(0, date.indexOf("T"));
                    SimpleDateFormat matchDate = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
                    matchDate.setTimeZone(TimeZone.getTimeZone("UTC"));
                    try {
                        Date parsedDate = matchDate.parse(date+time);
                        SimpleDateFormat newDate = new SimpleDateFormat("yyyy-MM-dd:HH:mm");
                        newDate.setTimeZone(TimeZone.getDefault());
                        date = newDate.format(parsedDate);
                        time = date.substring(date.indexOf(":") + 1);
                        date = date.substring(0, date.indexOf(":"));

                        if (!isReal) {
                            //This if statement changes the dummy data's date to match our current date range.
                            Date fragmentDate = new Date(System.currentTimeMillis()+((i-2)*86400000));
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                            date = format.format(fragmentDate);
                        }
                    } catch (Exception e) {
                        Log.d(LOG_TAG, "error here!");
                        Log.e(LOG_TAG,e.getMessage());
                    }

                    home = matchData.getString(HOME_TEAM);
                    away = matchData.getString(AWAY_TEAM);
                    homeGoals = matchData.getJSONObject(RESULT).getString(HOME_GOALS);
                    awayGoals = matchData.getJSONObject(RESULT).getString(AWAY_GOALS);
                    matchDay = matchData.getString(MATCH_DAY);

                    ContentValues match_values = new ContentValues();
                    match_values.put(DatabaseContract.scores_table.MATCH_ID,matchId);
                    match_values.put(DatabaseContract.scores_table.DATE_COL,date);
                    match_values.put(DatabaseContract.scores_table.TIME_COL,time);
                    match_values.put(DatabaseContract.scores_table.HOME_COL,home);
                    match_values.put(DatabaseContract.scores_table.AWAY_COL,away);
                    match_values.put(DatabaseContract.scores_table.HOME_GOALS_COL,homeGoals);
                    match_values.put(DatabaseContract.scores_table.AWAY_GOALS_COL,awayGoals);
                    match_values.put(DatabaseContract.scores_table.LEAGUE_COL,league);
                    match_values.put(DatabaseContract.scores_table.MATCH_DAY,matchDay);

                    //log spam
                    //Log.v(LOG_TAG,matchId);
                    //Log.v(LOG_TAG,date);
                    //Log.v(LOG_TAG,time);
                    //Log.v(LOG_TAG,home);
                    //Log.v(LOG_TAG,away);
                    //Log.v(LOG_TAG,homeGoals);
                    //Log.v(LOG_TAG,awayGoals);

                    values.add(match_values);
                }
            }

            int insertedData = 0;
            ContentValues[] insertData = new ContentValues[values.size()];
            values.toArray(insertData);
            insertedData = mContext.getContentResolver().bulkInsert(
                    DatabaseContract.BASE_CONTENT_URI, insertData);

            //Log.v(LOG_TAG,"Succesfully Inserted : " + String.valueOf(insertedData));
        } catch (JSONException e) {
            Log.e(LOG_TAG,e.getMessage());
        }
    }
}

