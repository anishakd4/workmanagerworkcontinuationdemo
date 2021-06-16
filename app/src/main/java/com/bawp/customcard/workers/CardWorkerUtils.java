package com.bawp.customcard.workers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.bawp.customcard.Constants;
import com.bawp.customcard.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.UUID;

final class CardWorkerUtils {

    public static void makeStatusNotification(String message, Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = Constants.VERBOSE_NOTIFICATION_CHANNEL_NAME;
            String description = Constants.VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION;
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(Constants.CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Constants.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(Constants.NOTIFICATION_TITLE)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(new long[0]);

        NotificationManagerCompat.from(context).notify(Constants.NOTIFICATION_ID, builder.build());
    }

    public static void sleep() {
        try {
            Thread.sleep(Constants.DELAY_TIME_MILLIS, 0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @WorkerThread
    public static Bitmap overlayTextOnBitmap(@NonNull Bitmap bitmap, @NonNull Context applicationContext, @NonNull String quote) {
        //create the output bitmap
        Bitmap outputBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        Canvas canvas = new Canvas(outputBitmap);

        float scale = applicationContext.getResources().getDisplayMetrics().density;

        //create paint
        TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.rgb(61, 61, 61));
        paint.setTextSize(28*scale);
        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);
        int textWidth = (int) (canvas.getWidth() - (16*scale));

        //overlay rectangle
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();
        Point centerOfCanvas = new Point(canvasWidth / 2, canvasHeight / 2);

        int left = centerOfCanvas.x - (bitmap.getWidth());
        int top = centerOfCanvas.x - (bitmap.getHeight());
        int right = centerOfCanvas.x + (bitmap.getWidth());
        int bottom = centerOfCanvas.y + (bitmap.getHeight());

        RectF textBg = new RectF(left, top, right, bottom);
        Paint recPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        recPaint.setColor(Color.DKGRAY);
        recPaint.setAlpha(255);
        recPaint.setStyle(Paint.Style.FILL);
        recPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));

        StaticLayout staticLayout = StaticLayout.Builder.obtain(
                quote, 0, quote.length(), paint, textWidth)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setIncludePad(true)
                .setLineSpacing(1.0f, 1.0f)
                .setBreakStrategy(Layout.BREAK_STRATEGY_SIMPLE)
                .setMaxLines(Integer.MAX_VALUE)
                .build();

        int textHeight = staticLayout.getHeight();

        float x = (bitmap.getWidth() - textWidth) / 2;
        float y = (bitmap.getHeight() - textHeight) / 2;
        canvas.save();

        canvas.drawBitmap(bitmap, 0, 0, paint);
        canvas.drawRect(textBg, recPaint);

        canvas.translate(x, y);
        staticLayout.draw(canvas);

        return outputBitmap;
    }

    private CardWorkerUtils() {

    }

    public static Uri writeBitmapToFile(@NonNull Context applicationContext, @NonNull Bitmap bitmap) {
        String name = String.format("card-processed-output%s.png", UUID.randomUUID().toString());
        File outputDir = new File(applicationContext.getFilesDir(), Constants.OUTPUT_PATH);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        File outputFile = new File(outputDir, name);

        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(outputFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, fileOutputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return Uri.fromFile(outputFile);

    }
}
