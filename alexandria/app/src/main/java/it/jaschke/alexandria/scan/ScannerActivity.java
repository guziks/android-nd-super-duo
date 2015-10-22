package it.jaschke.alexandria.scan;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

import it.jaschke.alexandria.R;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class ScannerActivity extends Activity implements ZBarScannerView.ResultHandler {

    private static final String TAG = ScannerActivity.class.getSimpleName();

    public static final String RESULT_BOOK_NUMBER_KEY = "book_number";

    private final String[] supportedFormatsArray = {"ISBN10", "EAN_8", "EAN_13"};
    private final List<String> supportedFormats = Arrays.asList(supportedFormatsArray);

    private ZBarScannerView mScannerView;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZBarScannerView(this);
        setContentView(mScannerView);
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        String format = rawResult.getBarcodeFormat().getName();
        String number = rawResult.getContents();
        Log.v(TAG, number);
        Log.v(TAG, format);

        boolean goodQuality = false;
        try {
            Long.parseLong(number);
            goodQuality = true;
        } catch (NumberFormatException e) {
            Toast.makeText(this, getString(R.string.unsupported_characters), Toast.LENGTH_SHORT).show();
        }

        if (goodQuality && supportedFormats.contains(format)) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(RESULT_BOOK_NUMBER_KEY, number);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        } else {
            Toast.makeText(this, getString(R.string.code_is_not_supported), Toast.LENGTH_SHORT).show();
        }

        mScannerView.startCamera();
    }
}