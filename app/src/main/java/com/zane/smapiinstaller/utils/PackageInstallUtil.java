package com.zane.smapiinstaller.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInstaller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class PackageInstallUtil {
    public static final String ACTION_INSTALL_COMPLETE = "com.zane.smapiinstaller.INSTALL_COMPLETE";

    public static boolean installPackage(Context context, String packageName, String apkPath, List<String> signedResourcePacks) {
        PackageInstaller packageInstaller = context.getPackageManager().getPackageInstaller();
        PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                PackageInstaller.SessionParams.MODE_INHERIT_EXISTING);
        params.setAppPackageName(packageName);
        try {
            // set params
            int sessionId = packageInstaller.createSession(params);
            PackageInstaller.Session session = packageInstaller.openSession(sessionId);
            writePackage(session, apkPath);
            for (String pack : signedResourcePacks) {
                writePackage(session, pack);
            }
            session.commit(createIntentSender(context, sessionId));
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writePackage(PackageInstaller.Session session, String apkPath) throws IOException {
        File file = new File(apkPath);
        try(FileInputStream in = new FileInputStream(file)) {
            OutputStream out = session.openWrite(file.getName(), 0, -1);
            byte[] buffer = new byte[65536];
            int c;
            while ((c = in.read(buffer)) != -1) {
                out.write(buffer, 0, c);
            }
            session.fsync(out);
            out.close();
        }
    }


    private static IntentSender createIntentSender(Context context, int sessionId) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                sessionId,
                new Intent(ACTION_INSTALL_COMPLETE),
                0);
        return pendingIntent.getIntentSender();
    }
}
