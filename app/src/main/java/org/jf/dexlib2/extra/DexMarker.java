package org.jf.dexlib2.extra;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DexMarker {

    private final String marker;

    private DexMarker(String marker) {
        this.marker = marker;
    }

    public String getMarker() {
        return marker;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DexMarker dexMarker = (DexMarker) o;
        return marker.equals(dexMarker.marker);
    }

    @Override
    public int hashCode() {
        return marker.hashCode();
    }

    @Override
    public String toString() {
        return marker;
    }

    public static List<DexMarker> listMarkers(Iterator<String> iterator) {
        List<DexMarker> markerList = new ArrayList<>();
        while (iterator.hasNext()){
            DexMarker marker = of(iterator.next());
            if(marker != null){
                markerList.add(marker);
            }
        }
        return markerList;
    }
    public static void writeMarkers(List<DexMarker> markerList, File file) throws IOException {
        StringBuilder builder = new StringBuilder();
        boolean append = false;
        for(DexMarker marker : markerList){
            if(append){
                builder.append('\n');
            }
            builder.append(marker.getMarker());
            append = true;
        }
        byte[] bytes = builder.toString().getBytes(StandardCharsets.UTF_8);
        File dir = file.getParentFile();
        if(dir != null && !dir.exists()){
            dir.mkdirs();
        }
        FileOutputStream outputStream = new FileOutputStream(file);
        outputStream.write(bytes, 0, bytes.length);
        outputStream.close();
    }
    public static List<DexMarker> readMarkers(File file) throws IOException {
        return readMarkers(new FileInputStream(file));
    }
    public static List<DexMarker> readMarkers(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer, 0, buffer.length)) != -1){
            outputStream.write(buffer, 0, length);
        }
        inputStream.close();
        outputStream.close();
        buffer = outputStream.toByteArray();
        String text = new String(buffer, 0, buffer.length, StandardCharsets.UTF_8);
        return parseMarkers(text);
    }
    public static List<DexMarker> parseMarkers(String lineSeparatedMarkers) {
        List<DexMarker> markerList = new ArrayList<>();
        String[] lines = lineSeparatedMarkers.split("\n");
        for(String line : lines){
            DexMarker marker = of(line.trim());
            if(marker != null){
                markerList.add(marker);
            }
        }
        return markerList;
    }
    public static DexMarker of(String text){
        if(isMarker(text)){
            return new DexMarker(text);
        }
        return null;
    }
    public static boolean isMarker(String text){
        if(text == null){
            return false;
        }
        int i = text.length() - 1;
        if(i < 5){
            return false;
        }
        if(text.charAt(0) != '~' || text.charAt(i) != '}'){
            return false;
        }
        return text.startsWith(PREFIX_D8) || text.startsWith(PREFIX_R8);
    }
    public static final String PREFIX_D8 = "~~D8{";
    public static final String PREFIX_R8 = "~~R8{";
    public static final String FILE_NAME = "markers.txt";
}
