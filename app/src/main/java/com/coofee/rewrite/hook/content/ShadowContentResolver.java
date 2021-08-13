package com.coofee.rewrite.hook.content;

import android.content.*;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.*;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Log;
import androidx.annotation.RequiresApi;

import java.io.FileNotFoundException;
import java.util.ArrayList;

public class ShadowContentResolver {

    public static Cursor query(ContentResolver contentResolver, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) throws RemoteException {
        check(uri.getAuthority());
        return contentResolver.query(uri, projection, selection, selectionArgs, sortOrder);
    }

    public static Cursor query(ContentResolver contentResolver, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder, CancellationSignal cancellationSignal) throws RemoteException {
        check(uri.getAuthority());
        return contentResolver.query(uri, projection, selection, selectionArgs, sortOrder, cancellationSignal);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static Cursor query(ContentResolver contentResolver, Uri uri, String[] projection,
                               Bundle queryArgs, CancellationSignal cancellationSignal) throws RemoteException {
        check(uri.getAuthority());
        return contentResolver.query(uri, projection, queryArgs, cancellationSignal);
    }

    public static String getType(ContentResolver contentResolver, Uri uri) throws RemoteException {
        check(uri.getAuthority());
        return contentResolver.getType(uri);
    }

    public static String[] getStreamTypes(ContentResolver contentResolver, Uri uri, String mimeTypeFilter) throws RemoteException {
        check(uri.getAuthority());
        return contentResolver.getStreamTypes(uri, mimeTypeFilter);
    }

    public static Uri canonicalize(ContentResolver contentResolver, Uri uri) throws RemoteException {
        check(uri.getAuthority());
        return contentResolver.canonicalize(uri);
    }

    public static Uri uncanonicalize(ContentResolver contentResolver, Uri uri) throws RemoteException {
        check(uri.getAuthority());
        return contentResolver.uncanonicalize(uri);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static boolean refresh(ContentResolver contentResolver, Uri uri, Bundle args,
                                  CancellationSignal cancellationSignal) throws RemoteException {
        check(uri.getAuthority());
        return contentResolver.refresh(uri, args, cancellationSignal);
    }

    public static Uri insert(ContentResolver contentResolver, Uri uri, ContentValues initialValues)
            throws RemoteException {
        check(uri.getAuthority());
        return contentResolver.insert(uri, initialValues);
    }

    public static int bulkInsert(ContentResolver contentResolver, Uri uri, ContentValues[] initialValues)
            throws RemoteException {
        check(uri.getAuthority());
        return contentResolver.bulkInsert(uri, initialValues);
    }

    public static int delete(ContentResolver contentResolver, Uri uri, String selection,
                             String[] selectionArgs) throws RemoteException {
        check(uri.getAuthority());
        return contentResolver.delete(uri, selection, selectionArgs);
    }

    public static int update(ContentResolver contentResolver, Uri uri, ContentValues values, String selection,
                             String[] selectionArgs) throws RemoteException {
        check(uri.getAuthority());
        return contentResolver.update(uri, values, selection, selectionArgs);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static ParcelFileDescriptor openFile(ContentResolver contentResolver, Uri uri, String mode,
                                                CancellationSignal signal) throws RemoteException, FileNotFoundException {
        check(uri.getAuthority());
        return contentResolver.openFile(uri, mode, signal);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static AssetFileDescriptor openAssetFile(ContentResolver contentResolver, Uri uri, String mode,
                                                    CancellationSignal signal) throws RemoteException, FileNotFoundException {
        check(uri.getAuthority());
        return contentResolver.openAssetFile(uri, mode, signal);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static AssetFileDescriptor openTypedAssetFile(ContentResolver contentResolver, Uri uri,
                                                         String mimeTypeFilter, Bundle opts,
                                                         CancellationSignal signal) throws RemoteException, FileNotFoundException {
        check(uri.getAuthority());
        return contentResolver.openTypedAssetFile(uri, mimeTypeFilter, opts, signal);
    }

    public static ContentProviderResult[] applyBatch(ContentResolver contentResolver, String authority, ArrayList<ContentProviderOperation> operations)
            throws RemoteException, OperationApplicationException {
        check(authority);
        return contentResolver.applyBatch(authority, operations);
    }

    public static Bundle call(ContentResolver contentResolver, Uri uri, String method,
                              String arg, Bundle extras) throws RemoteException {
        check(uri.getAuthority());
        return contentResolver.call(uri, method, arg, extras);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static Bundle call(ContentResolver contentResolver, String authority, String method,
                              String arg, Bundle extras) throws RemoteException {
        check(authority);
        return contentResolver.call(authority, method, arg, extras);
    }

    public static boolean check(String authority) {
        switch (authority) {
            case ContactsContract.AUTHORITY:
                Log.e("ShadowContentResolver", "联系人.", new Throwable());
                return false;

            case "sms":
                Log.e("ShadowContentResolver", "短信.", new Throwable());
                return false;

            case CalendarContract.AUTHORITY:
                Log.e("ShadowContentResolver", "日历.", new Throwable());
                return false;

            case Settings.AUTHORITY:
                Log.e("ShadowContentResolver", "系统设置.", new Throwable());
                return false;
        }
        return true;
    }

}
