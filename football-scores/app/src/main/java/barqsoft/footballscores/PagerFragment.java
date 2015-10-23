package barqsoft.footballscores;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yehya khaled on 2/27/2015.
 */
public class PagerFragment extends Fragment {

    public static final int NUM_PAGES = 5;

    public ViewPager pagerHandler;

    private PagerAdapter mPagerAdapter;
    private MainScreenFragment[] mViewFragments = new MainScreenFragment[5];

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_pager, container, false);
        pagerHandler = (ViewPager) rootView.findViewById(R.id.pager);
        mPagerAdapter = new PagerAdapter(getChildFragmentManager());

        for (int i = 0;i < NUM_PAGES;i++) {
            Date fragmentDate = new Date(System.currentTimeMillis() + ((i - 2) * 86400000));
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            mViewFragments[i] = new MainScreenFragment();
            mViewFragments[i].setFragmentDate(dateFormat.format(fragmentDate));
        }

        pagerHandler.setAdapter(mPagerAdapter);
        pagerHandler.setCurrentItem(MainActivity.currentFragment);

        return rootView;
    }

    private class PagerAdapter extends FragmentStatePagerAdapter {

        @Override
        public Fragment getItem(int i) {
            return mViewFragments[i];
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return getDayName(getActivity(), System.currentTimeMillis() + ((position - 2) * 86400000));
        }

        public String getDayName(Context context, long dateInMillis) {
            // If the date is today, return the localized version of "Today" instead of the actual
            // day name.

            Time time = new Time();
            time.setToNow();
            int julianDay = Time.getJulianDay(dateInMillis, time.gmtoff);
            int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), time.gmtoff);
            if (julianDay == currentJulianDay) {
                return context.getString(R.string.today);
            } else if (julianDay == currentJulianDay + 1) {
                return context.getString(R.string.tomorrow);
            } else if (julianDay == currentJulianDay - 1) {
                return context.getString(R.string.yesterday);
            } else {
                time = new Time();
                time.setToNow();
                // Otherwise, the format is just the day of the week (e.g "Wednesday".
                SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
                return dayFormat.format(dateInMillis);
            }
        }
    }
}
