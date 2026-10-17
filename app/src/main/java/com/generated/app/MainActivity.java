package com.generated.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private NotesDatabaseHelper dbHelper;
    private EditText etSearch;
    private View btnClearSearch;
    private LinearLayout pinnedList;
    private LinearLayout othersList;
    private TextView tvPinnedHeader;
    private TextView tvOthersHeader;
    private View emptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new NotesDatabaseHelper(this);
        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes(etSearch.getText().toString());
    }

    private void initViews() {
        etSearch = (EditText) findViewById(R.id.et_search);
        btnClearSearch = findViewById(R.id.btn_clear_search);
        pinnedList = (LinearLayout) findViewById(R.id.pinned_list);
        othersList = (LinearLayout) findViewById(R.id.others_list);
        tvPinnedHeader = (TextView) findViewById(R.id.tv_pinned_header);
        tvOthersHeader = (TextView) findViewById(R.id.tv_others_header);
        emptyState = findViewById(R.id.empty_state);

        ImageButton fabAdd = (ImageButton) findViewById(R.id.fab_add);
        ImageButton btnQuickImage = (ImageButton) findViewById(R.id.btn_quick_image);
        ImageButton btnQuickVoice = (ImageButton) findViewById(R.id.btn_quick_voice);

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NoteEditActivity.class);
                startActivity(intent);
            }
        });

        btnQuickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NoteEditActivity.class);
                intent.putExtra(NoteEditActivity.EXTRA_ACTION_QUICK, NoteEditActivity.ACTION_IMAGE);
                startActivity(intent);
            }
        });

        btnQuickVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NoteEditActivity.class);
                intent.putExtra(NoteEditActivity.EXTRA_ACTION_QUICK, NoteEditActivity.ACTION_VOICE);
                startActivity(intent);
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    btnClearSearch.setVisibility(View.VISIBLE);
                } else {
                    btnClearSearch.setVisibility(View.GONE);
                }
                loadNotes(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnClearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etSearch.setText("");
            }
        });
    }

    private void loadNotes(String query) {
        List<Note> allNotes = dbHelper.getAllNotes(query);

        pinnedList.removeAllViews();
        othersList.removeAllViews();

        if (allNotes.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            tvPinnedHeader.setVisibility(View.GONE);
            tvOthersHeader.setVisibility(View.GONE);
            return;
        }

        emptyState.setVisibility(View.GONE);

        List<Note> pinned = new ArrayList<Note>();
        List<Note> others = new ArrayList<Note>();

        for (Note note : allNotes) {
            if (note.isPinned()) {
                pinned.add(note);
            } else {
                others.add(note);
            }
        }

        boolean hasPinned = !pinned.isEmpty();
        boolean hasOthers = !others.isEmpty();

        if (hasPinned) {
            tvPinnedHeader.setVisibility(View.VISIBLE);
            for (Note note : pinned) {
                pinnedList.addView(createNoteCardView(note));
            }
        } else {
            tvPinnedHeader.setVisibility(View.GONE);
        }

        if (hasOthers) {
            if (hasPinned) {
                tvOthersHeader.setVisibility(View.VISIBLE);
            } else {
                tvOthersHeader.setVisibility(View.GONE);
            }
            for (Note note : others) {
                othersList.addView(createNoteCardView(note));
            }
        } else {
            tvOthersHeader.setVisibility(View.GONE);
        }
    }

    private View createNoteCardView(final Note note) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(8));
        card.setLayoutParams(params);

        // Google Keep card style: rounded background with subtle border
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dpToPx(12));
        bg.setColor(note.getColorHex());
        bg.setStroke(dpToPx(1), 0xFFE0E0E0);
        card.setBackground(bg);
        card.setPadding(dpToPx(14), dpToPx(14), dpToPx(14), dpToPx(14));

        // Image attachment thumbnail if present
        if (note.getImagePath() != null && new File(note.getImagePath()).exists()) {
            ImageView ivThumb = new ImageView(this);
            LinearLayout.LayoutParams ivParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(140));
            ivParams.bottomMargin = dpToPx(10);
            ivThumb.setLayoutParams(ivParams);
            ivThumb.setScaleType(ImageView.ScaleType.CENTER_CROP);

            try {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inSampleSize = 3;
                Bitmap bm = BitmapFactory.decodeFile(note.getImagePath(), opts);
                if (bm != null) {
                    ivThumb.setImageBitmap(bm);
                    card.addView(ivThumb);
                }
            } catch (Exception ignored) {}
        }

        // Title
        if (!TextUtils.isEmpty(note.getTitle())) {
            TextView tvTitle = new TextView(this);
            tvTitle.setText(note.getTitle());
            tvTitle.setTextSize(16);
            tvTitle.setTextColor(0xFF202124);
            tvTitle.getPaint().setFakeBoldText(true);
            tvTitle.setMaxLines(2);
            tvTitle.setEllipsize(TextUtils.TruncateAt.END);
            card.addView(tvTitle);
        }

        // Content
        if (!TextUtils.isEmpty(note.getContent())) {
            TextView tvContent = new TextView(this);
            tvContent.setText(note.getContent());
            tvContent.setTextSize(14);
            tvContent.setTextColor(0xFF424242);
            tvContent.setMaxLines(5);
            tvContent.setEllipsize(TextUtils.TruncateAt.END);
            tvContent.setLineSpacing(dpToPx(2), 1.0f);
            if (!TextUtils.isEmpty(note.getTitle())) {
                LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                contentParams.topMargin = dpToPx(6);
                tvContent.setLayoutParams(contentParams);
            }
            card.addView(tvContent);
        }

        // Voice recording indicator pill
        if (note.getAudioPath() != null && new File(note.getAudioPath()).exists()) {
            LinearLayout audioBadge = new LinearLayout(this);
            audioBadge.setOrientation(LinearLayout.HORIZONTAL);
            audioBadge.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, dpToPx(28));
            badgeParams.topMargin = dpToPx(10);
            audioBadge.setLayoutParams(badgeParams);

            GradientDrawable badgeBg = new GradientDrawable();
            badgeBg.setCornerRadius(dpToPx(14));
            badgeBg.setColor(0xFFE8F0FE);
            badgeBg.setStroke(dpToPx(1), 0xFFD2E3FC);
            audioBadge.setBackground(badgeBg);
            audioBadge.setPadding(dpToPx(8), dpToPx(2), dpToPx(10), dpToPx(2));

            ImageView micIcon = new ImageView(this);
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dpToPx(16), dpToPx(16));
            micIcon.setLayoutParams(iconParams);
            micIcon.setImageResource(R.drawable.ic_mic);
            micIcon.setColorFilter(0xFF1A73E8);
            audioBadge.addView(micIcon);

            TextView tvBadgeText = new TextView(this);
            int duration = note.getAudioDuration();
            String durationStr = String.format("%02d:%02d", duration / 60, duration % 60);
            tvBadgeText.setText("הקלטה (" + durationStr + ")");
            tvBadgeText.setTextSize(12);
            tvBadgeText.setTextColor(0xFF1A73E8);
            tvBadgeText.setPadding(dpToPx(6), 0, 0, 0);
            audioBadge.addView(tvBadgeText);

            card.addView(audioBadge);
        }

        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NoteEditActivity.class);
                intent.putExtra(NoteEditActivity.EXTRA_NOTE_ID, note.getId());
                startActivity(intent);
            }
        });

        return card;
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }
}
