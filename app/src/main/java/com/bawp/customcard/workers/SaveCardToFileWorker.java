package com.bawp.customcard.workers;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.bawp.customcard.Constants;
import com.google.common.util.concurrent.ListenableFuture;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SaveCardToFileWorker extends Worker {
    public static final String TAG = SaveCardToFileWorker.class.getSimpleName();
    public static final String TITLE = "Card Image";
    public static final SimpleDateFormat DATE_FORMATTER =
            new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z", Locale.getDefault());

    public SaveCardToFileWorker(@NonNull @NotNull Context context, @NonNull @NotNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @NotNull
    @Override
    public Result doWork() {
        Context applicationContext = getApplicationContext();
        CardWorkerUtils.makeStatusNotification("Saving image", applicationContext);

        ContentResolver contentResolver = applicationContext.getContentResolver();

        try{
            String resourceUri = getInputData().getString(Constants.KEY_IMAGE_URI);
            Bitmap bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(Uri.parse(resourceUri)));
            String outputUri = MediaStore.Images.Media.insertImage(contentResolver, bitmap, TITLE, DATE_FORMATTER.format(new Date()));

            if(TextUtils.isEmpty(outputUri)){
                Log.i(TAG, "Writing to mediaStore failed");
                return Result.failure();
            }
            Data outputData = new Data.Builder().putString(Constants.KEY_IMAGE_URI, outputUri).build();
            return Result.success(outputData);
        }catch (Exception e){
            Log.i(TAG, "Unable to save image to gallery", e);
            return Result.failure();
        }
    }
}
