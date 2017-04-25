package com.teambrella.android.content;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;

/**
 * Teambrella Content Provider
 */
public class TeambrellaContentProvider extends ContentProvider {

    /**
     * DB Helper
     */
    private TeambrellaSQLiteOpenHelper mDbHelper;


    @Override
    public boolean onCreate() {
        mDbHelper = new TeambrellaSQLiteOpenHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        return db.query(getTableName(uri), projection, selection, selectionArgs, null, null, sortOrder);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long rowId;
        switch (TeambrellaRepository.sUriMatcher.match(uri)) {
            case TeambrellaRepository.TEAMMATE:
                rowId = db.insertWithOnConflict(TeambrellaRepository.TEAMMATE_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                break;
            case TeambrellaRepository.BTC_ADDRESS:
                rowId = db.insertOrThrow(TeambrellaRepository.BTC_ADDRESS_TABLE, null, values);
                break;
            default:
                rowId = db.insertOrThrow(getTableName(uri), null, values);
                break;
        }
        return Uri.withAppendedPath(uri, Long.toString(rowId));
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        return db.delete(getTableName(uri), selection, selectionArgs);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        return db.update(getTableName(uri), values, selection, selectionArgs);
    }


    @NonNull
    @Override
    public ContentProviderResult[] applyBatch(@NonNull ArrayList<ContentProviderOperation> operations) throws OperationApplicationException {
        ContentProviderResult[] results = null;
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            results = super.applyBatch(operations);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return results;
    }

    private String getTableName(Uri uri) {
        switch (TeambrellaRepository.sUriMatcher.match(uri)) {
            case TeambrellaRepository.BTC_ADDRESS:
                return TeambrellaRepository.BTC_ADDRESS_TABLE;
            case TeambrellaRepository.CONNECTION:
                return TeambrellaRepository.CONNECTION_TABLE;
            case TeambrellaRepository.COSIGNER:
                return TeambrellaRepository.COSIGNER_TABLE;
            case TeambrellaRepository.PAY_TO:
                return TeambrellaRepository.PAY_TO_TABLE;
            case TeambrellaRepository.TEAM:
                return TeambrellaRepository.TEAM_TABLE;
            case TeambrellaRepository.TEAMMATE:
                return TeambrellaRepository.TEAMMATE_TABLE;
            case TeambrellaRepository.TX:
                return TeambrellaRepository.TX_TABLE;
            case TeambrellaRepository.TX_INPUT:
                return TeambrellaRepository.TX_INPUT_TABLE;
            case TeambrellaRepository.TX_OUTPUT:
                return TeambrellaRepository.TX_OUTPUT_TABLE;
            case TeambrellaRepository.TX_SIGNATURE:
                return TeambrellaRepository.TX_SIGNATURE_TABLE;
            default:
                throw new RuntimeException("Unknown uri ->" + uri);
        }
    }


    private static class TeambrellaSQLiteOpenHelper extends SQLiteOpenHelper {

        private static final int VERSION = 1;
        private static final String NAME = "teambrella";


        public TeambrellaSQLiteOpenHelper(Context context) {
            super(context, NAME, null, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL("CREATE TABLE BTCAddress (" +
                    "Address TEXT PRIMARY KEY UNIQUE NOT NULL, " +
                    "TeammateId INTEGER NOT NULL REFERENCES Teammate (Id) DEFERRABLE INITIALLY DEFERRED, " +
                    "Status INTEGER NOT NULL, " +
                    "DateCreated DATETIME" +
                    ")");

            db.execSQL("CREATE TABLE Connection (" +
                    "Id INTEGER PRIMARY KEY, " +
                    "LastConnected DATETIME, " +
                    "LastUpdated TEXT, " +
                    "NeedShowBrowser BOOL" +
                    ")");

            db.execSQL("CREATE TABLE Cosigner (" +
                    "TeammateId INTEGER DEFAULT (0) NOT NULL REFERENCES Teammate (Id) DEFERRABLE INITIALLY DEFERRED, " +
                    "AddressId TEXT NOT NULL REFERENCES BTCAddress (Address) DEFERRABLE INITIALLY DEFERRED, " +
                    "KeyOrder INTEGER NOT NULL DEFAULT (0), " +
                    "PRIMARY KEY (AddressId, KeyOrder)" +
                    ")");

            db.execSQL("CREATE TABLE PayTo (" +
                    "Id VARCHAR UNIQUE NOT NULL PRIMARY KEY, " +
                    "TeammateId INTEGER NOT NULL REFERENCES Teammate (Id) DEFERRABLE INITIALLY DEFERRED, " +
                    "KnownSince DATETIME NOT NULL, " +
                    "Address TEXT NOT NULL, " +
                    "IsDefault BOOLEAN NOT NULL" +
                    ")");

            db.execSQL("CREATE TABLE [Team] ( " +
                    "[Id] integer PRIMARY KEY NOT NULL, " +
                    "[Name] text NOT NULL, " +
                    "[PayToAddressOkAge] integer NOT NULL DEFAULT 14, " +
                    "[AutoApprovalMyGoodAddress] integer NOT NULL DEFAULT 3, " +
                    "[AutoApprovalCosignGoodAddress] integer NOT NULL DEFAULT 3, " +
                    "[AutoApprovalMyNewAddress] integer NOT NULL DEFAULT 3, " +
                    "[AutoApprovalCosignNewAddress] integer NOT NULL DEFAULT 3, " +
                    "[DisbandState] integer NOT NULL DEFAULT 0, " +
                    "[Testnet] boolean NOT NULL DEFAULT 1 " +
                    ")");

            db.execSQL("CREATE TABLE Teammate (" +
                    "Id INTEGER NOT NULL UNIQUE DEFAULT (0) PRIMARY KEY, " +
                    "TeamId INTEGER NOT NULL REFERENCES Team (Id) DEFERRABLE INITIALLY DEFERRED, " +
                    "Name TEXT, " +
                    "FBName TEXT, " +
                    "PublicKey TEXT" +
                    ")");


            db.execSQL("CREATE TABLE [Tx] ( " +
                    "[Id] varchar PRIMARY KEY NOT NULL, " +
                    "[TeammateId] integer NOT NULL, " +
                    "[AmountBTC] decimal, " +
                    "[FeeBTC] decimal, " +
                    "[WithdrawReqId] integer, " +
                    "[ClaimId] integer, " +
                    "[ClaimTeammateId] integer, " +
                    "[MoveToAddressId] string, " +
                    "[Kind] integer NOT NULL, " +
                    "[State] integer NOT NULL, " +
                    "[InitiatedTime] datetime NOT NULL, " +
                    "[UpdateTime] datetime, " +
                    "[ReceivedTime] datetime NOT NULL, " +
                    "[ResolutionTime] datetime, " +
                    "[ProcessedTime] datetime, " +
                    "[NeedUpdateServer] boolean NOT NULL, " +
                    "[Resolution] integer NOT NULL, " +
                    "[ClientResolutionTime] datetime, " +
                    "CONSTRAINT [FK_Tx_0_0] FOREIGN KEY ([TeammateId]) REFERENCES [Teammate] ([Id]) MATCH NONE ON UPDATE NO ACTION ON DELETE NO ACTION, " +
                    "CONSTRAINT [FK_Tx_1_0] FOREIGN KEY ([ClaimTeammateId]) REFERENCES [Teammate] ([Id]) MATCH NONE ON UPDATE NO ACTION ON DELETE NO ACTION, " +
                    "CONSTRAINT [FK_Tx_2_0] FOREIGN KEY ([MoveToAddressId]) REFERENCES [BTCAddress] ([Address]) MATCH NONE ON UPDATE NO ACTION ON DELETE NO ACTION " +
                    ")");

            db.execSQL("CREATE TABLE TxInput (" +
                    "Id VARCHAR PRIMARY KEY UNIQUE NOT NULL, " +
                    "TxId VARCHAR NOT NULL REFERENCES Tx (Id) DEFERRABLE INITIALLY DEFERRED, " +
                    "PrevTxId TEXT NOT NULL, " +
                    "PrevTxIndex INTEGER NOT NULL, " +
                    "AmountBTC DECIMAL NOT NULL" +
                    ")");

            db.execSQL("CREATE TABLE TxOutput (" +
                    "Id VARCHAR PRIMARY KEY UNIQUE NOT NULL, " +
                    "TxId VARCHAR NOT NULL REFERENCES Tx (Id) DEFERRABLE INITIALLY DEFERRED, " +
                    "PayToId VARCHAR NOT NULL REFERENCES PayTo (Id) DEFERRABLE INITIALLY DEFERRED, " +
                    "AmountBTC DECIMAL NOT NULL" +
                    ")");

            db.execSQL("CREATE TABLE TxSignature (" +
                    "Id VARCHAR PRIMARY KEY UNIQUE NOT NULL, " +
                    "TxInputId VARCHAR NOT NULL REFERENCES TxInput (Id) DEFERRABLE INITIALLY DEFERRED, " +
                    "TeammateId INTEGER NOT NULL REFERENCES Teammate (Id) DEFERRABLE INITIALLY DEFERRED, " +
                    "Signature BLOB (80), " +
                    "NeedUpdateServer BOOLEAN NOT NULL" +
                    ")");

            db.execSQL("CREATE TABLE User (" +
                    "Id INTEGER PRIMARY KEY, " +
                    "PrivateKey TEXT, " +
                    "AuxWalletAmount DECIMAL NOT NULL DEFAULT (0), " +
                    "AuxWalletChecked DATETIME" +
                    ")");

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // nothing to do
        }
    }
}
