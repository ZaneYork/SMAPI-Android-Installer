package com.fom.storage.media;

import android.app.PendingIntent;
import android.app.RecoverableSecurityException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.IntentSender;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;

import com.starry.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;

/**
 * 9th July 2021.
 * <p>
 * A class to read write external shared storage for android R.
 * Since Android 11 you can only access the android specified directories such as
 * DCIM, Download, Documents, Pictures, Movies, Music etc.
 * <p>
 * This class is just for an example class.
 *
 * @author <a href="https://github.com/fiftyonemoon">hardkgosai</a>.
 * @since 1.0.3.2
 */
public class AndroidXI {

    private Context context;

    public static AndroidXI getInstance() {
        return new AndroidXI();
    }

    public AndroidXI with(Context context) {
        this.context = context;
        return this;
    }

    /**
     * Create new media uri.
     */
    public Uri create(String directory, String filename, String mimetype) {

        ContentResolver contentResolver = context.getContentResolver();

        ContentValues contentValues = new ContentValues();

        //Set filename, if you don't system automatically use current timestamp as name
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);

        //Set mimetype if you want
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimetype);

        //To create folder in Android directories use below code
        //pass your folder path here, it will create new folder inside directory
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, directory);
        }

        //pass new ContentValues() for no values.
        //Specified uri will save object automatically in android specified directories.
        //ex. MediaStore.Images.Media.EXTERNAL_CONTENT_URI will save object into android Pictures directory.
        //ex. MediaStore.Videos.Media.EXTERNAL_CONTENT_URI will save object into android Movies directory.
        //if content values not provided, system will automatically add values after object was written.
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
    }

    /**
     * Delete file.
     * <p>
     * If {@link ContentResolver} failed to delete the file, use trick,
     * SDK version is >= 29(Q)? use {@link SecurityException} and again request for delete.
     * SDK version is >= 30(R)? use {@link MediaStore#createDeleteRequest(ContentResolver, Collection)}.
     */
    public void delete(ActivityResultLauncher<IntentSenderRequest> launcher, Uri uri) {

        ContentResolver contentResolver = context.getContentResolver();

        try {

            //delete object using resolver
            contentResolver.delete(uri, null, null);

        } catch (SecurityException e) {

            PendingIntent pendingIntent = null;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

                ArrayList<Uri> collection = new ArrayList<>();
                collection.add(uri);
                pendingIntent = MediaStore.createDeleteRequest(contentResolver, collection);

            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {

                //if exception is recoverable then again send delete request using intent
                if (e instanceof RecoverableSecurityException) {
                    RecoverableSecurityException exception = (RecoverableSecurityException) e;
                    pendingIntent = exception.getUserAction().getActionIntent();
                }
            }

            if (pendingIntent != null) {
                IntentSender sender = pendingIntent.getIntentSender();
                IntentSenderRequest request = new IntentSenderRequest.Builder(sender).build();
                launcher.launch(request);
            }
        }
    }

    /**
     * Rename file.
     *
     * @param uri    - filepath.
     * @param rename - the name you want to replace with original.
     */
    public void rename(Uri uri, String rename) {

        //create content values with new name and update
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, rename);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.getContentResolver().update(uri, contentValues, null);
        }
    }

    /**
     * Duplicate file.
     *
     * @param uri - filepath.
     */
    public Uri duplicate(Uri uri) throws IOException {

        ContentResolver contentResolver = context.getContentResolver();

        Uri output = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());

        String input = getPathFromUri(uri);

        try (InputStream inputStream = FileUtils.getInputStream(input); OutputStream out = FileUtils.getOutputStream(output, context)) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                out.write(buf, 0, len); //write input file data to output file
            }

        }
        return output;
    }

    /**
     * Get file path from uri.
     */
    private String getPathFromUri(Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);

        String text = null;

        if (cursor.moveToNext()) {
            text = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
        }

        cursor.close();

        return text;
    }
}