package com.generated.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class NotesDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "notes_keep.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NOTES = "notes";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_COLOR = "color";
    public static final String COLUMN_IMAGE_PATH = "image_path";
    public static final String COLUMN_AUDIO_PATH = "audio_path";
    public static final String COLUMN_AUDIO_DURATION = "audio_duration";
    public static final String COLUMN_IS_PINNED = "is_pinned";
    public static final String COLUMN_UPDATED_AT = "updated_at";

    public NotesDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_NOTES + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_TITLE + " TEXT, "
                + COLUMN_CONTENT + " TEXT, "
                + COLUMN_COLOR + " INTEGER, "
                + COLUMN_IMAGE_PATH + " TEXT, "
                + COLUMN_AUDIO_PATH + " TEXT, "
                + COLUMN_AUDIO_DURATION + " INTEGER, "
                + COLUMN_IS_PINNED + " INTEGER, "
                + COLUMN_UPDATED_AT + " INTEGER);";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        onCreate(db);
    }

    public long saveOrUpdateNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_TITLE, note.getTitle());
        cv.put(COLUMN_CONTENT, note.getContent());
        cv.put(COLUMN_COLOR, note.getColorHex());
        cv.put(COLUMN_IMAGE_PATH, note.getImagePath());
        cv.put(COLUMN_AUDIO_PATH, note.getAudioPath());
        cv.put(COLUMN_AUDIO_DURATION, note.getAudioDuration());
        cv.put(COLUMN_IS_PINNED, note.isPinned() ? 1 : 0);
        cv.put(COLUMN_UPDATED_AT, System.currentTimeMillis());

        if (note.getId() > 0) {
            db.update(TABLE_NOTES, cv, COLUMN_ID + " = ?", new String[]{String.valueOf(note.getId())});
            return note.getId();
        } else {
            return db.insert(TABLE_NOTES, null, cv);
        }
    }

    public Note getNoteById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NOTES, null, COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            Note note = parseNote(cursor);
            cursor.close();
            return note;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    public List<Note> getAllNotes(String query) {
        List<Note> list = new ArrayList<Note>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor;
        if (query != null && query.trim().length() > 0) {
            String filter = "%" + query.trim() + "%";
            cursor = db.query(TABLE_NOTES, null,
                    COLUMN_TITLE + " LIKE ? OR " + COLUMN_CONTENT + " LIKE ?",
                    new String[]{filter, filter}, null, null, COLUMN_UPDATED_AT + " DESC");
        } else {
            cursor = db.query(TABLE_NOTES, null, null, null, null, null, COLUMN_UPDATED_AT + " DESC");
        }

        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(parseNote(cursor));
            }
            cursor.close();
        }
        return list;
    }

    public void deleteNote(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NOTES, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    private Note parseNote(Cursor cursor) {
        Note note = new Note();
        note.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
        note.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
        note.setContent(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT)));
        note.setColorHex(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COLOR)));
        note.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_PATH)));
        note.setAudioPath(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AUDIO_PATH)));
        note.setAudioDuration(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AUDIO_DURATION)));
        note.setPinned(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_PINNED)) == 1);
        note.setUpdatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_UPDATED_AT)));
        return note;
    }
}
