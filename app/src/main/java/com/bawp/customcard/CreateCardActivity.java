package com.bawp.customcard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.Data;
import androidx.work.WorkInfo;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.picasso.Picasso;

import java.util.List;

import static com.bawp.customcard.Constants.KEY_IMAGE_URI;

public class CreateCardActivity extends AppCompatActivity {

    public static final String TAG = CreateCardActivity.class.getSimpleName();
    private CustomCardViewModel customCardViewModel;
    private Button processCard, cancelProcess, seeCardButton;
    private ImageView imageView;
    private EditText quoteEditText;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_card);

        imageView = findViewById(R.id.image_view);
        quoteEditText = findViewById(R.id.custom_quote_edtx);
        seeCardButton = findViewById(R.id.see_card_button);
        cancelProcess = findViewById(R.id.cancel_button);
        processCard = findViewById(R.id.process_button);
        progressBar = findViewById(R.id.progress_bar);

        customCardViewModel = new ViewModelProvider.AndroidViewModelFactory((Application) getApplicationContext())
                .create(CustomCardViewModel.class);


        Intent intent = getIntent();
        String imageUriExtra = intent.getStringExtra(Constants.KEY_IMAGE_URI);
        Log.i("anisham", "anisham imageUriExtra = " + imageUriExtra);
        customCardViewModel.setImageUri(imageUriExtra);

        if (customCardViewModel.getImageUri() != null) {
            Picasso.get().load(customCardViewModel.getImageUri()).into(imageView);
        }

        customCardViewModel.getOutputWorkInfo().observe(this, workInfos -> {
            //if we can't find matching workinfo, do nothing
            Log.i("anisham", "anisham getOutputWorkInfo.observe");
            if (workInfos == null || workInfos.isEmpty()) {
                return;
            }
            Log.i("anisham", "anisham workInfos.get(0).getId() = " + workInfos.get(0).getId());

            WorkInfo workInfo = workInfos.get(0);
            Log.i("anisham", "anisham workInfo.getState() = " + workInfo.getState());
            boolean finished = workInfo.getState().isFinished();
            if (!finished) {
                showWorkInProgress();
            } else {
                showWorkFinished();
                Data outputData = workInfo.getOutputData();
                String outputImageUri = outputData.getString(KEY_IMAGE_URI);
                Log.i("anisham", "anisham showWorkFinished outputImageUri = " + outputImageUri);

                // If we have the output file, then show the "checkout image" button
                if (!TextUtils.isEmpty(outputImageUri)) {
                    customCardViewModel.setOutputUri(outputImageUri);
                    seeCardButton.setVisibility(View.VISIBLE);
                }
            }
        });

        processCard.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(quoteEditText.getText().toString())) {
                String quote = quoteEditText.getText().toString().trim();
                customCardViewModel.processImageToCard(quote);
            }
        });

        seeCardButton.setOnClickListener(v -> {
            Log.i(TAG, "onClick: see card");
            Uri currentUri = customCardViewModel.getOutputUri();
            if (currentUri != null) {
                Intent actionView = new Intent(Intent.ACTION_VIEW, currentUri);
                if (actionView.resolveActivity(getPackageManager()) != null) {
                    startActivity(actionView);
                }
                processCard.setVisibility(View.VISIBLE);
            }
        });

        cancelProcess.setOnClickListener(view -> customCardViewModel.cancelWork());
    }

    private void showWorkFinished() {
        progressBar.setVisibility(View.GONE);
        cancelProcess.setVisibility(View.GONE);
        seeCardButton.setVisibility(View.VISIBLE);
    }

    private void showWorkInProgress() {
        progressBar.setVisibility(View.VISIBLE);
        cancelProcess.setVisibility(View.VISIBLE);
        processCard.setVisibility(View.GONE);
        seeCardButton.setVisibility(View.GONE);
    }
}
