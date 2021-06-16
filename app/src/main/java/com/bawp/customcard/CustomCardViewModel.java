package com.bawp.customcard;

import android.app.Application;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.bawp.customcard.workers.CardWorker;
import com.bawp.customcard.workers.CleanupWorker;
import com.bawp.customcard.workers.SaveCardToFileWorker;

import java.util.List;

public class CustomCardViewModel extends AndroidViewModel {

    private Uri mImageUri;
    private WorkManager mWorkManager;
    private LiveData<List<WorkInfo>> mSavedWorkInfo;
    private Uri mOutputUri;

    public CustomCardViewModel(@NonNull @org.jetbrains.annotations.NotNull Application application) {
        super(application);
        mWorkManager = WorkManager.getInstance(application);

        mSavedWorkInfo = mWorkManager.getWorkInfosByTagLiveData(Constants.TAG_OUTPUT);
    }

    LiveData<List<WorkInfo>> getOutputWorkInfo() {
        return mSavedWorkInfo;
    }

    void setImageUri(String uri) {
        mImageUri = uriOrNull(uri);
    }

    Uri getImageUri() {
        return mImageUri;
    }

    void setOutputUri(String outputImageUri) {
        Log.i("anisham", "anisham = setOutputUri = " + outputImageUri);
        mOutputUri = uriOrNull(outputImageUri);
    }

    Uri getOutputUri() {
        return mOutputUri;
    }

    void processImageToCard(String quote) {
        WorkContinuation continuation = mWorkManager.beginUniqueWork(Constants.IMAGE_PROCESSING_WORK_NAME,
                ExistingWorkPolicy.REPLACE, OneTimeWorkRequest.from(CleanupWorker.class));

        //Building our card
        OneTimeWorkRequest.Builder cardBuilder = new OneTimeWorkRequest.Builder(CardWorker.class);
        cardBuilder.setInputData(createInputDataForUri(quote));

        continuation = continuation.then(cardBuilder.build());

        //constraints
        Constraints constraints = new Constraints.Builder().setRequiresCharging(true).build();

        //work request to save the image to the filesystem
        OneTimeWorkRequest save = new OneTimeWorkRequest.Builder(SaveCardToFileWorker.class)
                .setConstraints(constraints)
                .addTag(Constants.TAG_OUTPUT)
                .build();

        continuation = continuation.then(save);

        //seal the deal start the work
        continuation.enqueue();
    }

    public void cancelWork() {
        mWorkManager.cancelUniqueWork(Constants.IMAGE_PROCESSING_WORK_NAME);
    }

    private Data createInputDataForUri(String quote) {
        Data.Builder builder = new Data.Builder();
        if (mImageUri != null) {
            builder.putString(Constants.KEY_IMAGE_URI, mImageUri.toString());
            builder.putString(Constants.CUSTOM_QUOTE, quote);
        }
        return builder.build();
    }

    private Uri uriOrNull(String uriString) {
        if (!TextUtils.isEmpty(uriString)) {
            return Uri.parse(uriString);
        }
        return null;
    }
}
