package com.zane.smapiinstaller.utils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.fornwall.apksigner.zipio.ZioEntry;
import net.fornwall.apksigner.zipio.ZipInput;
import net.fornwall.apksigner.zipio.ZipOutput;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java9.util.function.Consumer;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Zane
 */
public class ZipUtils {

    public static void addOrReplaceEntries(String inputZipFilename, List<ZipEntrySource> entrySources, String outputZipFilename, Consumer<Integer> progressCallback) throws IOException {
        File inFile = new File(inputZipFilename).getCanonicalFile();
        File outFile = new File(outputZipFilename).getCanonicalFile();
        if (inFile.equals(outFile)) {
            throw new IllegalArgumentException("Input and output files are the same");
        }
        ImmutableMap<String, ZipEntrySource> entryMap = Maps.uniqueIndex(entrySources, ZipEntrySource::getPath);
        try (ZipInput input = new ZipInput(inputZipFilename)) {
            int size = input.entries.values().size();
            int index = 0;
            int reportInterval = size / 100;
            try (ZipOutput zipOutput = new ZipOutput(new FileOutputStream(outputZipFilename))) {
                HashSet<String> replacedFileSet = new HashSet<>(entryMap.size());
                for (ZioEntry inEntry : input.entries.values()) {
                    if (entryMap.containsKey(inEntry.getName())) {
                        ZipEntrySource source = entryMap.get(inEntry.getName());
                        ZioEntry zioEntry = new ZioEntry(inEntry.getName());
                        zioEntry.setCompression(source.getCompressionMethod());
                        zioEntry.getOutputStream().write(source.getData());
                        zipOutput.write(zioEntry);
                        replacedFileSet.add(inEntry.getName());
                    } else {
                        zipOutput.write(inEntry);
                    }
                    index++;
                    if(index % reportInterval == 0) {
                        progressCallback.accept((int) (index * 95.0 / size));
                    }
                }
                Sets.SetView<String> difference = Sets.difference(entryMap.keySet(), replacedFileSet);
                index = 0;
                for (String name : difference) {
                    ZipEntrySource source = entryMap.get(name);
                    ZioEntry zioEntry = new ZioEntry(name);
                    zioEntry.setCompression(source.getCompressionMethod());
                    zioEntry.getOutputStream().write(source.getData());
                    zipOutput.write(zioEntry);
                    progressCallback.accept(95 + (int)(index * 5.0 / difference.size()));
                }
                progressCallback.accept(100);
            }
        }
    }

    @Data
    @AllArgsConstructor
    public static class ZipEntrySource {
        private String path;
        private byte[] data;
        private int compressionMethod;
    }
}
