package com.bawp.customcard.workers;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.bawp.customcard.Constants;

import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;

public class CardWorker extends Worker {
    public static final String TAG = CardWorker.class.getSimpleName();

    public CardWorker(@NonNull @NotNull Context context, @NonNull @NotNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @NotNull
    @Override
    public Result doWork() {
        Context applicationContext = getApplicationContext();
        CardWorkerUtils.makeStatusNotification("Writing quote onto image", applicationContext);
        CardWorkerUtils.sleep();

        String imageResourceUri = getInputData().getString(Constants.KEY_IMAGE_URI);
        String quote = getInputData().getString(Constants.CUSTOM_QUOTE);

        ContentResolver contentResolver = applicationContext.getContentResolver();

        try {
            Bitmap photo = BitmapFactory.decodeStream(contentResolver.openInputStream(Uri.parse(imageResourceUri)));

            //write text to image
            Bitmap output = CardWorkerUtils.overlayTextOnBitmap(photo, applicationContext, quote);

            //write bitmap to a temp file
            Uri outputUri = CardWorkerUtils.writeBitmapToFile(applicationContext, output);

            Data outputData = new Data.Builder().putString(Constants.KEY_IMAGE_URI, outputUri.toString()).build();

            return Result.success(outputData);
        } catch (FileNotFoundException e) {
            Log.i(TAG, "Error writing quote onto image");
            return Result.failure();
        }
    }
}
