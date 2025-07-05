package com.example.simplenoteapp.data;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.EntityUpsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class NoteDao_Impl implements NoteDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Note> __insertionAdapterOfNote;

  private final EntityDeletionOrUpdateAdapter<Note> __deletionAdapterOfNote;

  private final SharedSQLiteStatement __preparedStmtOfDeleteNoteById;

  private final EntityUpsertionAdapter<Note> __upsertionAdapterOfNote;

  public NoteDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfNote = new EntityInsertionAdapter<Note>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `notes` (`id`,`title`,`content`,`isChecklist`,`timestamp`,`serverId`,`isSynced`,`needsSync`,`lastSyncTime`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Note entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        statement.bindString(3, entity.getContent());
        final int _tmp = entity.isChecklist() ? 1 : 0;
        statement.bindLong(4, _tmp);
        statement.bindLong(5, entity.getTimestamp());
        if (entity.getServerId() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getServerId());
        }
        final int _tmp_1 = entity.isSynced() ? 1 : 0;
        statement.bindLong(7, _tmp_1);
        final int _tmp_2 = entity.getNeedsSync() ? 1 : 0;
        statement.bindLong(8, _tmp_2);
        statement.bindLong(9, entity.getLastSyncTime());
      }
    };
    this.__deletionAdapterOfNote = new EntityDeletionOrUpdateAdapter<Note>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `notes` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Note entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteNoteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM notes WHERE id = ?";
        return _query;
      }
    };
    this.__upsertionAdapterOfNote = new EntityUpsertionAdapter<Note>(new EntityInsertionAdapter<Note>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `notes` (`id`,`title`,`content`,`isChecklist`,`timestamp`,`serverId`,`isSynced`,`needsSync`,`lastSyncTime`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Note entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        statement.bindString(3, entity.getContent());
        final int _tmp = entity.isChecklist() ? 1 : 0;
        statement.bindLong(4, _tmp);
        statement.bindLong(5, entity.getTimestamp());
        if (entity.getServerId() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getServerId());
        }
        final int _tmp_1 = entity.isSynced() ? 1 : 0;
        statement.bindLong(7, _tmp_1);
        final int _tmp_2 = entity.getNeedsSync() ? 1 : 0;
        statement.bindLong(8, _tmp_2);
        statement.bindLong(9, entity.getLastSyncTime());
      }
    }, new EntityDeletionOrUpdateAdapter<Note>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `notes` SET `id` = ?,`title` = ?,`content` = ?,`isChecklist` = ?,`timestamp` = ?,`serverId` = ?,`isSynced` = ?,`needsSync` = ?,`lastSyncTime` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Note entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        statement.bindString(3, entity.getContent());
        final int _tmp = entity.isChecklist() ? 1 : 0;
        statement.bindLong(4, _tmp);
        statement.bindLong(5, entity.getTimestamp());
        if (entity.getServerId() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getServerId());
        }
        final int _tmp_1 = entity.isSynced() ? 1 : 0;
        statement.bindLong(7, _tmp_1);
        final int _tmp_2 = entity.getNeedsSync() ? 1 : 0;
        statement.bindLong(8, _tmp_2);
        statement.bindLong(9, entity.getLastSyncTime());
        statement.bindLong(10, entity.getId());
      }
    });
  }

  @Override
  public Object insertNote(final Note note, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfNote.insert(note);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteNote(final Note note, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfNote.handle(note);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteNoteById(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteNoteById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteNoteById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object upsertNote(final Note note, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfNote.upsert(note);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Note>> getAllNotes() {
    final String _sql = "SELECT * FROM notes ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"notes"}, new Callable<List<Note>>() {
      @Override
      @NonNull
      public List<Note> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfIsChecklist = CursorUtil.getColumnIndexOrThrow(_cursor, "isChecklist");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfServerId = CursorUtil.getColumnIndexOrThrow(_cursor, "serverId");
          final int _cursorIndexOfIsSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "isSynced");
          final int _cursorIndexOfNeedsSync = CursorUtil.getColumnIndexOrThrow(_cursor, "needsSync");
          final int _cursorIndexOfLastSyncTime = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSyncTime");
          final List<Note> _result = new ArrayList<Note>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Note _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpContent;
            _tmpContent = _cursor.getString(_cursorIndexOfContent);
            final boolean _tmpIsChecklist;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsChecklist);
            _tmpIsChecklist = _tmp != 0;
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final Long _tmpServerId;
            if (_cursor.isNull(_cursorIndexOfServerId)) {
              _tmpServerId = null;
            } else {
              _tmpServerId = _cursor.getLong(_cursorIndexOfServerId);
            }
            final boolean _tmpIsSynced;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsSynced);
            _tmpIsSynced = _tmp_1 != 0;
            final boolean _tmpNeedsSync;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfNeedsSync);
            _tmpNeedsSync = _tmp_2 != 0;
            final long _tmpLastSyncTime;
            _tmpLastSyncTime = _cursor.getLong(_cursorIndexOfLastSyncTime);
            _item = new Note(_tmpId,_tmpTitle,_tmpContent,_tmpIsChecklist,_tmpTimestamp,_tmpServerId,_tmpIsSynced,_tmpNeedsSync,_tmpLastSyncTime);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Note> getNoteById(final long id) {
    final String _sql = "SELECT * FROM notes WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"notes"}, new Callable<Note>() {
      @Override
      @Nullable
      public Note call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfIsChecklist = CursorUtil.getColumnIndexOrThrow(_cursor, "isChecklist");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfServerId = CursorUtil.getColumnIndexOrThrow(_cursor, "serverId");
          final int _cursorIndexOfIsSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "isSynced");
          final int _cursorIndexOfNeedsSync = CursorUtil.getColumnIndexOrThrow(_cursor, "needsSync");
          final int _cursorIndexOfLastSyncTime = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSyncTime");
          final Note _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpContent;
            _tmpContent = _cursor.getString(_cursorIndexOfContent);
            final boolean _tmpIsChecklist;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsChecklist);
            _tmpIsChecklist = _tmp != 0;
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final Long _tmpServerId;
            if (_cursor.isNull(_cursorIndexOfServerId)) {
              _tmpServerId = null;
            } else {
              _tmpServerId = _cursor.getLong(_cursorIndexOfServerId);
            }
            final boolean _tmpIsSynced;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsSynced);
            _tmpIsSynced = _tmp_1 != 0;
            final boolean _tmpNeedsSync;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfNeedsSync);
            _tmpNeedsSync = _tmp_2 != 0;
            final long _tmpLastSyncTime;
            _tmpLastSyncTime = _cursor.getLong(_cursorIndexOfLastSyncTime);
            _result = new Note(_tmpId,_tmpTitle,_tmpContent,_tmpIsChecklist,_tmpTimestamp,_tmpServerId,_tmpIsSynced,_tmpNeedsSync,_tmpLastSyncTime);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
