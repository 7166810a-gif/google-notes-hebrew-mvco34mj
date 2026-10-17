package com.generated.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NoteEditActivity extends Activity {

    public static final String EXTRA_NOTE_ID = "extra_note_id";
    public static final String EXTRA_ACTION_QUICK = "extra_action_quick";
    public static final String ACTION_IMAGE = "action_image";
    public static final String ACTION_VOICE = "action_voice";

    private static final int REQUEST_IMAGE_PICK = 101;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 201;

    // Google Keep pastel palette
    public static final int[] NOTE_COLORS = new int[]{
            0xFFFFFFFF, // Default White
            0xFFFFF8E1, // Light Yellow
            0xFFE8F5E9, // Light Green
            0xFFE1F5FE, // Light Blue
            0xFFFCE4EC, // Light Pink
            0xFFF3E5F5, // Light Purple
            0xFFFFF3E0, // Light Orange
            0xFFE0F2F1  // Light Teal
    };

    private NotesDatabaseHelper dbHelper;
    private Note currentNote;
    private long noteId = -1;

    private RelativeLayout editRoot;
    private EditText etTitle;
    private EditText etContent;
    private ImageButton btnPin;
    private ImageButton btnDelete;
    private ImageButton btnPickColor;
    private ImageButton btnAddImage;
    private ImageButton btnAddVoice;
    private View colorPaletteBar;
    private LinearLayout colorChipsContainer;
    private TextView tvEditedTime;

    // Image preview
    private RelativeLayout imagePreviewLayout;
    private ImageView ivNoteImage;
    private ImageButton btnRemoveImage;

    // Audio Player
    private LinearLayout audioPlayerLayout;
    private ImageButton btnPlayPause;
    private TextView tvAudioDuration;
    private ImageButton btnDeleteAudio;
    private MediaPlayer mediaPlayer;
    private boolean isPlayingAudio = false;

    // Audio Recorder
    private LinearLayout recordingBanner;
    private TextView tvRecordingTimer;
    private Button btnStopRecording;
    private MediaRecorder mediaRecorder;
    private String tempAudioFilePath;
    private boolean isRecording = false;
    private long recordStartTime = 0;
    private Handler recordHandler = new Handler();
    private Runnable recordTimerRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);

        dbHelper = new NotesDatabaseHelper(this);
        initViews();

        noteId = getIntent().getLongExtra(EXTRA_NOTE_ID, -1);
        if (noteId > 0) {
            currentNote = dbHelper.getNoteById(noteId);
        }

        if (currentNote == null) {
            currentNote = new Note();
            currentNote.setColorHex(NOTE_COLORS[0]);
        } else {
            btnDelete.setVisibility(View.VISIBLE);
        }

        populateUI();
        setupColorPalette();

        String quickAction = getIntent().getStringExtra(EXTRA_ACTION_QUICK);
        if (ACTION_IMAGE.equals(quickAction)) {
            openImagePicker();
        } else if (ACTION_VOICE.equals(quickAction)) {
            checkAndStartRecording();
        }
    }

    private void initViews() {
        editRoot = (RelativeLayout) findViewById(R.id.edit_root);
        etTitle = (EditText) findViewById(R.id.et_title);
        etContent = (EditText) findViewById(R.id.et_content);
        btnPin = (ImageButton) findViewById(R.id.btn_pin);
        btnDelete = (ImageButton) findViewById(R.id.btn_delete);
        btnPickColor = (ImageButton) findViewById(R.id.btn_pick_color);
        btnAddImage = (ImageButton) findViewById(R.id.btn_add_image);
        btnAddVoice = (ImageButton) findViewById(R.id.btn_add_voice);
        colorPaletteBar = findViewById(R.id.color_palette_bar);
        colorChipsContainer = (LinearLayout) findViewById(R.id.color_chips_container);
        tvEditedTime = (TextView) findViewById(R.id.tv_edited_time);

        imagePreviewLayout = (RelativeLayout) findViewById(R.id.image_preview_layout);
        ivNoteImage = (ImageView) findViewById(R.id.iv_note_image);
        btnRemoveImage = (ImageButton) findViewById(R.id.btn_remove_image);

        audioPlayerLayout = (LinearLayout) findViewById(R.id.audio_player_layout);
        btnPlayPause = (ImageButton) findViewById(R.id.btn_play_pause);
        tvAudioDuration = (TextView) findViewById(R.id.tv_audio_duration);
        btnDeleteAudio = (ImageButton) findViewById(R.id.btn_delete_audio);

        recordingBanner = (LinearLayout) findViewById(R.id.recording_banner);
        tvRecordingTimer = (TextView) findViewById(R.id.tv_recording_timer);
        btnStopRecording = (Button) findViewById(R.id.btn_stop_recording);

        ImageButton btnBack = (ImageButton) findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAndFinish();
            }
        });

        btnPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentNote.setPinned(!currentNote.isPinned());
                updatePinIcon();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDelete();
            }
        });

        btnPickColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (colorPaletteBar.getVisibility() == View.VISIBLE) {
                    colorPaletteBar.setVisibility(View.GONE);
                } else {
                    colorPaletteBar.setVisibility(View.VISIBLE);
                }
            }
        });

        btnAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });

        btnAddVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording) {
                    stopRecording(true);
                } else {
                    checkAndStartRecording();
                }
            }
        });

        btnRemoveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentNote.setImagePath(null);
                imagePreviewLayout.setVisibility(View.GONE);
            }
        });

        btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleAudioPlayback();
            }
        });

        btnDeleteAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAudioPlayback();
                currentNote.setAudioPath(null);
                currentNote.setAudioDuration(0);
                audioPlayerLayout.setVisibility(View.GONE);
            }
        });

        btnStopRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording(true);
            }
        });
    }

    private void populateUI() {
        etTitle.setText(currentNote.getTitle());
        etContent.setText(currentNote.getContent());
        applyBackgroundColor(currentNote.getColorHex());
        updatePinIcon();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        tvEditedTime.setText("עודכן: " + sdf.format(new Date(currentNote.getUpdatedAt())));

        // Display image if available
        if (currentNote.getImagePath() != null && new File(currentNote.getImagePath()).exists()) {
            loadImage(currentNote.getImagePath());
        }

        // Display audio if available
        if (currentNote.getAudioPath() != null && new File(currentNote.getAudioPath()).exists()) {
            setupAudioPlayerView();
        }
    }

    private void updatePinIcon() {
        if (currentNote.isPinned()) {
            btnPin.setColorFilter(0xFF1A73E8); // Active blue
        } else {
            btnPin.setColorFilter(0xFF5F6368); // Inactive gray
        }
    }

    private void applyBackgroundColor(int color) {
        editRoot.setBackgroundColor(color);
        currentNote.setColorHex(color);
    }

    private void setupColorPalette() {
        colorChipsContainer.removeAllViews();
        int chipSize = dpToPx(36);
        int margin = dpToPx(6);

        for (final int color : NOTE_COLORS) {
            View chip = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(chipSize, chipSize);
            params.setMargins(margin, margin, margin, margin);
            chip.setLayoutParams(params);

            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(color);
            drawable.setStroke(dpToPx(1), 0xFFBDBDBD);
            chip.setBackground(drawable);

            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    applyBackgroundColor(color);
                }
            });
            colorChipsContainer.addView(chip);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, getString(R.string.add_image)), REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null && data.getData() != null) {
            handleImageUri(data.getData());
        }
    }

    private void handleImageUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return;

            File storageDir = new File(getFilesDir(), "note_images");
            if (!storageDir.exists()) storageDir.mkdirs();

            File destFile = new File(storageDir, "IMG_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(destFile);

            byte[] buffer = new byte[4096];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();

            currentNote.setImagePath(destFile.getAbsolutePath());
            loadImage(destFile.getAbsolutePath());
        } catch (Exception e) {
            Toast.makeText(this, R.string.image_load_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void loadImage(String path) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2; // scale down for memory safety
            Bitmap bitmap = BitmapFactory.decodeFile(path, options);
            if (bitmap != null) {
                ivNoteImage.setImageBitmap(bitmap);
                imagePreviewLayout.setVisibility(View.VISIBLE);
            }
        } catch (Exception ignored) {
        }
    }

    // Audio recording implementation
    private void checkAndStartRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
                return;
            }
        }
        startRecording();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording();
            } else {
                Toast.makeText(this, R.string.permission_audio_needed, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startRecording() {
        try {
            File audioDir = new File(getFilesDir(), "note_audios");
            if (!audioDir.exists()) audioDir.mkdirs();

            File audioFile = new File(audioDir, "REC_" + System.currentTimeMillis() + ".m4a");
            tempAudioFilePath = audioFile.getAbsolutePath();

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setOutputFile(tempAudioFilePath);
            mediaRecorder.prepare();
            mediaRecorder.start();

            isRecording = true;
            recordStartTime = SystemClock.elapsedRealtime();
            recordingBanner.setVisibility(View.VISIBLE);

            recordTimerRunnable = new Runnable() {
                @Override
                public void run() {
                    if (isRecording) {
                        long elapsed = (SystemClock.elapsedRealtime() - recordStartTime) / 1000;
                        long minutes = elapsed / 60;
                        long seconds = elapsed % 60;
                        tvRecordingTimer.setText(String.format(Locale.getDefault(), "מקליט... %02d:%02d", minutes, seconds));
                        recordHandler.postDelayed(this, 1000);
                    }
                }
            };
            recordHandler.post(recordTimerRunnable);

        } catch (Exception e) {
            Toast.makeText(this, "שגיאה בהפעלת הקלטה", Toast.LENGTH_SHORT).show();
            isRecording = false;
            recordingBanner.setVisibility(View.GONE);
        }
    }

    private void stopRecording(boolean save) {
        if (!isRecording) return;
        try {
            recordHandler.removeCallbacks(recordTimerRunnable);
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        } catch (Exception ignored) {
        }
        isRecording = false;
        recordingBanner.setVisibility(View.GONE);

        if (save && tempAudioFilePath != null) {
            long durationSec = (SystemClock.elapsedRealtime() - recordStartTime) / 1000;
            currentNote.setAudioPath(tempAudioFilePath);
            currentNote.setAudioDuration((int) durationSec);
            setupAudioPlayerView();
            Toast.makeText(this, R.string.recording_saved, Toast.LENGTH_SHORT).show();
        }
    }

    private void setupAudioPlayerView() {
        audioPlayerLayout.setVisibility(View.VISIBLE);
        int duration = currentNote.getAudioDuration();
        int minutes = duration / 60;
        int seconds = duration % 60;
        tvAudioDuration.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
        btnPlayPause.setImageResource(R.drawable.ic_play);
    }

    private void toggleAudioPlayback() {
        if (isPlayingAudio) {
            stopAudioPlayback();
        } else {
            startAudioPlayback();
        }
    }

    private void startAudioPlayback() {
        if (currentNote.getAudioPath() == null) return;
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(currentNote.getAudioPath());
            mediaPlayer.prepare();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopAudioPlayback();
                }
            });
            mediaPlayer.start();
            isPlayingAudio = true;
            btnPlayPause.setImageResource(R.drawable.ic_pause);
        } catch (Exception e) {
            Toast.makeText(this, "לא ניתן להשמיע את ההקלטה", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopAudioPlayback() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.release();
            } catch (Exception ignored) {
            }
            mediaPlayer = null;
        }
        isPlayingAudio = false;
        btnPlayPause.setImageResource(R.drawable.ic_play);
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete)
                .setMessage(R.string.delete_confirm)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (currentNote.getId() > 0) {
                            dbHelper.deleteNote(currentNote.getId());
                            Toast.makeText(NoteEditActivity.this, R.string.note_deleted, Toast.LENGTH_SHORT).show();
                        }
                        finish();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void saveAndFinish() {
        if (isRecording) {
            stopRecording(true);
        }
        stopAudioPlayback();

        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        // If completely empty and no media, delete or don't save
        boolean hasMedia = currentNote.getImagePath() != null || currentNote.getAudioPath() != null;
        if (title.isEmpty() && content.isEmpty() && !hasMedia) {
            if (currentNote.getId() > 0) {
                dbHelper.deleteNote(currentNote.getId());
            }
            finish();
            return;
        }

        currentNote.setTitle(title);
        currentNote.setContent(content);
        dbHelper.saveOrUpdateNote(currentNote);
        finish();
    }

    @Override
    public void onBackPressed() {
        saveAndFinish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isRecording) {
            stopRecording(true);
        }
        stopAudioPlayback();
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }
}
