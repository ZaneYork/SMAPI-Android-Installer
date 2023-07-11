package androidx.documentfile.provider;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.text.TextUtils;
import android.util.Log;


import java.util.ArrayList;
import java.util.List;

public class DocumentUtils {
    public interface IFileFilter {
        boolean accept(String name);
    }
    private static TreeDocumentFile findFile(Context context, TreeDocumentFile file, String name) {
        final ContentResolver resolver = context.getContentResolver();
        final Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(file.getUri(), DocumentsContract.getDocumentId(file.getUri()));
        Cursor c = null;
        try {
            c = resolver.query(childrenUri, new String[] {DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME}, null, null, null);
            while (c.moveToNext()) {
                final String documentName = c.getString(1);
                if (TextUtils.equals(name, documentName)) {
                    final String documentId = c.getString(0);
                    final Uri documentUri = DocumentsContract.buildDocumentUriUsingTree(file.getUri(), documentId);
                    return new TreeDocumentFile(file, context, documentUri);
                }
            }
        } catch (Exception e) {
            Log.w("DocumentUtils", "Failed query: " + e);
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return null;
    }
    public static DocumentFile findFile(Context context, DocumentFile documentFile, String name) {
        if (documentFile instanceof TreeDocumentFile)
            return findFile(context, (TreeDocumentFile)documentFile, name);
        return documentFile.findFile(name);
    }
    private static List<DocumentFile> filterFiles(Context context, TreeDocumentFile file, IFileFilter filter) {
        ContentResolver resolver = context.getContentResolver();
        Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(file.getUri(), DocumentsContract.getDocumentId(file.getUri()));
        List<DocumentFile> filtered = new ArrayList<>();
        Cursor c = null;
        try {
            c = resolver.query(childrenUri, new String[] {DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME}, null, null, null);
            while (c.moveToNext()) {
                String documentName = c.getString(1);
                String documentId = c.getString(0);
                Uri documentUri = DocumentsContract.buildDocumentUriUsingTree(file.getUri(), documentId);
                TreeDocumentFile child = new TreeDocumentFile(file, context, documentUri);
                if (child.isDirectory())
                    filtered.addAll(filterFiles(context, child, filter));
                else if (filter.accept(documentName))
                    filtered.add(child);
            }
        } catch (Exception e) {
            Log.w("DocumentUtils", "Failed query: " + e);
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return filtered;
    }
    public static List<DocumentFile> filterFiles(Context context, DocumentFile documentFile, IFileFilter filter) {
        if (documentFile instanceof TreeDocumentFile)
            return filterFiles(context, (TreeDocumentFile)documentFile, filter);
        List<DocumentFile> filtered = new ArrayList<>();
        DocumentFile[] files = documentFile.listFiles();
        if (files != null) {
            for (DocumentFile file : files) {
                if (filter.accept(file.getName()))
                    filtered.add(file);
            }
        }
        return filtered;
    }
}
