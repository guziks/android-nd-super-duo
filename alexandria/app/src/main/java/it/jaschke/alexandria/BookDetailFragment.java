package it.jaschke.alexandria;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.service.BookService;
import it.jaschke.alexandria.service.DownloadImageTask;


public class BookDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String ARGUMENTS_KEY_EAN = "EAN";

    private final int LOADER_ID = 10;

    private View mRootView;
    private String mBookNumber;
    private String mBookTitle;
    private ShareActionProvider mShareActionProvider;

    public BookDetailFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mBookNumber = arguments.getString(BookDetailFragment.ARGUMENTS_KEY_EAN);
            getLoaderManager().restartLoader(LOADER_ID, null, this);
        }

        mRootView = inflater.inflate(R.layout.fragment_book_detail, container, false);
        mRootView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.INTENT_EXTRA_EAN, mBookNumber);
                bookIntent.setAction(BookService.INTENT_ACTION_DELETE_BOOK);
                getActivity().startService(bookIntent);
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        return mRootView;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.book_detail, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(mBookNumber)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        mBookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        ((TextView) mRootView.findViewById(R.id.fullBookTitle)).setText(mBookTitle);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text) + mBookTitle);

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }

        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        ((TextView) mRootView.findViewById(R.id.fullBookSubTitle)).setText(bookSubTitle);

        String desc = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.DESC));
        ((TextView) mRootView.findViewById(R.id.fullBookDesc)).setText(desc);

        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        String[] authorsArr = authors.split(",");
        ((TextView) mRootView.findViewById(R.id.authors)).setLines(authorsArr.length);
        ((TextView) mRootView.findViewById(R.id.authors)).setText(authors.replace(",", "\n"));

        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        if(Patterns.WEB_URL.matcher(imgUrl).matches()) {
            new DownloadImageTask((ImageView) mRootView.findViewById(R.id.fullBookCover)).execute(imgUrl);
            mRootView.findViewById(R.id.fullBookCover).setVisibility(View.VISIBLE);
        }

        String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        ((TextView) mRootView.findViewById(R.id.categories)).setText(categories);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

    @Override
    public void onPause() {
        super.onDestroyView();
        if (MainActivity.isTablet && mRootView.findViewById(R.id.right_container) == null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }
}