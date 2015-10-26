package barqsoft.footballscores;

import android.content.res.Resources;

/**
 * Created by yehya khaled on 3/3/2015.
 */
public class Utilies {

    public static final int SERIE_A = 357;
    public static final int PREMIER_LEAGUE = 354;
    public static final int CHAMPIONS_LEAGUE = 362;
    public static final int PRIMERA_DIVISION = 358;
    public static final int BUNDESLIGA = 351;

    public static String getLeague(int leagueNum) {
        Resources res = Resources.getSystem();
        switch (leagueNum) {
            case SERIE_A : return res.getString(R.string.league_serie_a);
            case PREMIER_LEAGUE: return res.getString(R.string.league_premier_league);
            case CHAMPIONS_LEAGUE : return res.getString(R.string.league_uefa_champions_league);
            case PRIMERA_DIVISION : return res.getString(R.string.league_primera_division);
            case BUNDESLIGA : return res.getString(R.string.league_bundesliga);
            default: return res.getString(R.string.league_not_known);
        }
    }

    public static String getMatchDay(int matchDay, int leagueNum) {
        Resources res = Resources.getSystem();
        if (leagueNum == CHAMPIONS_LEAGUE) {
            if (matchDay <= 6) {
                return res.getString(R.string.league_num_group_stages);
            } else if (matchDay == 7 || matchDay == 8) {
                return res.getString(R.string.league_num_first_knockout_round);
            } else if (matchDay == 9 || matchDay == 10) {
                return res.getString(R.string.league_num_quarter_final);
            } else if (matchDay == 11 || matchDay == 12) {
                return res.getString(R.string.league_num_semi_final);
            } else {
                return res.getString(R.string.league_num_final);
            }
        } else {
            return res.getString(R.string.league_num_match_day) + String.valueOf(matchDay);
        }
    }

    public static String getScores(int homeGoals, int awayGoals) {
        String separator = " - ";
        if (homeGoals < 0 || awayGoals < 0) {
            return separator;
        } else {
            return String.valueOf(homeGoals) + separator + String.valueOf(awayGoals);
        }
    }

    public static int getTeamCrestByTeamName (String teamName) {
        Resources res = Resources.getSystem();

        if (teamName == null) {
            return R.drawable.no_icon;
        }

        if (teamName.equals(res.getString(R.string.team_name_arsenal))) {
            return R.drawable.arsenal;
        } else if (teamName.equals(res.getString(R.string.team_name_manchester))) {
            return R.drawable.manchester_united;
        } else if (teamName.equals(res.getString(R.string.team_name_swansea))) {
            return R.drawable.swansea_city_afc;
        } else if (teamName.equals(res.getString(R.string.team_name_leicester))) {
            return R.drawable.leicester_city_fc_hd_logo;
        } else if (teamName.equals(res.getString(R.string.team_name_everton))) {
            return R.drawable.everton_fc_logo1;
        } else if (teamName.equals(res.getString(R.string.team_name_west_ham))) {
            return R.drawable.west_ham;
        } else if (teamName.equals(res.getString(R.string.team_name_tottenham))) {
            return R.drawable.tottenham_hotspur;
        } else if (teamName.equals(res.getString(R.string.team_name_west_bromwich))) {
            return R.drawable.west_bromwich_albion_hd_logo;
        } else if (teamName.equals(res.getString(R.string.team_name_sunderland))) {
            return R.drawable.sunderland;
        } else if (teamName.equals(res.getString(R.string.team_name_stoke_city))) {
            return R.drawable.stoke_city;
        } else {
            return R.drawable.no_icon;
        }
    }
}
