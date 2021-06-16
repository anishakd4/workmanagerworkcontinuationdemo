package com.bawp.customcard.workers;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.bawp.customcard.Constants;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public class CleanupWorker extends Worker {
    public static final String TAG = CleanupWorker.class.getSimpleName();

    public CleanupWorker(@NonNull @NotNull Context context, @NonNull @NotNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @NotNull
    @Override
    public Result doWork() {
        Context applicationContext = getApplicationContext();
        CardWorkerUtils.makeStatusNotification("Cleaning up old temporary files", applicationContext);
        CardWorkerUtils.sleep();

        try{
            File outputDirectory = new File(applicationContext.getFilesDir(), Constants.OUTPUT_PATH);
            if(outputDirectory.exists()){
                File[] entries = outputDirectory.listFiles();
                if(entries != null && entries.length > 0){
                    for(File entry: entries){
                        String name = entry.getName();
                        if(!TextUtils.isEmpty(name) && name.endsWith(".png")){
                            boolean deleted = entry.delete();
                            Log.i(TAG, String.format("Deleted %s - %s" , name, deleted));
                        }
                    }
                }
            }
            return Result.success();

        }catch (Exception exception){
            Log.i(TAG, "Error cleaning up: ", exception);
            return Result.failure();
        }
    }
}
