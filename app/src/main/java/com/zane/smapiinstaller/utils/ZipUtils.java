package com.zane.smapiinstaller.utils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.fornwall.apksigner.zipio.ZioEntry;
import net.fornwall.apksigner.zipio.ZipInput;
import net.fornwall.apksigner.zipio.ZipOutput;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;

import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * @author Zane
 */
public class ZipUtils {

    public static byte[] decompressXALZ(byte[] bytes){
        if("XALZ".equals(new String(ByteUtils.subArray(bytes, 0, 4), StandardCharsets.ISO_8859_1))) {
            LZ4Factory factory = LZ4Factory.fastestInstance();
            LZ4FastDecompressor lz4FastDecompressor = factory.fastDecompressor();
            byte[] length = ByteUtils.subArray(bytes, 8, 12);
            int len = (length[0] & 0xff) | ((length[1] & 0xff) << 8) | ((length[2] & 0xff) << 16) | ((length[3] & 0xff) << 24);
            bytes = lz4FastDecompressor.decompress(bytes, 12, len);
        }
        return bytes;
    }

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

    public static void removeEntries(String inputZipFilename, String prefix, String outputZipFilename, Consumer<Integer> progressCallback) throws IOException {
        File inFile = new File(inputZipFilename).getCanonicalFile();
        File outFile = new File(outputZipFilename).getCanonicalFile();
        if (inFile.equals(outFile)) {
            throw new IllegalArgumentException("Input and output files are the same");
        }
        try (ZipInput input = new ZipInput(inputZipFilename)) {
            int size = input.entries.values().size();
            int index = 0;
            int reportInterval = size / 100;
            try (ZipOutput zipOutput = new ZipOutput(new FileOutputStream(outFile))) {
                for (ZioEntry inEntry : input.entries.values()) {
                    if (!inEntry.getName().startsWith(prefix)) {
                        zipOutput.write(inEntry);
                    }
                    index++;
                    if(index % reportInterval == 0) {
                        progressCallback.accept((int) (index * 100.0 / size));
                    }
                }
                progressCallback.accept(100);
            }
        }
    }

    @Data
    @RequiredArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(of = "path")
    public static class ZipEntrySource {
        @NonNull
        private String path;
        @NonNull
        private byte[] data;
        @NonNull
        private int compressionMethod;
        private Supplier<byte[]> dataSupplier;

        public ZipEntrySource(@NonNull String path, @NonNull int compressionMethod, Supplier<byte[]> dataSupplier) {
            this.path = path;
            this.compressionMethod = compressionMethod;
            this.dataSupplier = dataSupplier;
        }

        private byte[] getData() {
            if(data != null) {
                return data;
            }
            // Optimize: read only once
            if(dataSupplier != null) {
                byte[] bytes = dataSupplier.get();
                dataSupplier = null;
                return bytes;
            }
            return null;
        }
    }
}
