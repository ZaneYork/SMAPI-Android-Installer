package com.zane.smapiinstaller.utils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;
import com.zane.smapiinstaller.dto.Tuple2;

import net.fornwall.apksigner.zipio.ZioEntry;
import net.fornwall.apksigner.zipio.ZipInput;
import net.fornwall.apksigner.zipio.ZipOutput;
import net.jpountz.lz4.LZ4Factory;

import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Zane
 */
public class ZipUtils {

    private final static String FILE_HEADER_XALZ = "XALZ";

    public static byte[] decompressXALZ(byte[] bytes) {
        if (bytes == null) {
            return new byte[0];
        }
        if (FILE_HEADER_XALZ.equals(new String(ByteUtils.subArray(bytes, 0, 4), StandardCharsets.ISO_8859_1))) {
            byte[] length = ByteUtils.subArray(bytes, 8, 12);
            int len = (length[0] & 0xff) | ((length[1] & 0xff) << 8) | ((length[2] & 0xff) << 16) | ((length[3] & 0xff) << 24);
            bytes = LZ4Factory.fastestJavaInstance().fastDecompressor().decompress(bytes, 12, len);
        }
        return bytes;
    }

    public static Tuple2<byte[], Set<String>> addOrReplaceEntries(String inputZipFilename, List<ZipEntrySource> entrySources, String outputZipFilename, Function<String, Boolean> removePredict, Consumer<Integer> progressCallback) throws IOException {
        File inFile = new File(inputZipFilename).getCanonicalFile();
        File outFile = new File(outputZipFilename).getCanonicalFile();
        if (inFile.equals(outFile)) {
            throw new IllegalArgumentException("Input and output files are the same");
        }
        ImmutableMap<String, ZipEntrySource> entryMap = Maps.uniqueIndex(entrySources, ZipEntrySource::getPath);
        byte[] originManifest = null;
        ConcurrentHashMap<String, Boolean> originEntryName = new ConcurrentHashMap<>();
        try (ZipInput input = new ZipInput(inputZipFilename)) {
            int size = input.entries.values().size();
            AtomicLong count = new AtomicLong();
            int reportInterval = size / 100;
            try (ZipOutput zipOutput = new ZipOutput(new FileOutputStream(outputZipFilename))) {
                ConcurrentHashMap<String, Boolean> replacedFileSet = new ConcurrentHashMap<>(entryMap.size());
                MultiprocessingUtil.TaskBundle<ZioEntry> taskBundle = MultiprocessingUtil.newTaskBundle((zioEntry) -> {
                    try {
                        zipOutput.write(zioEntry);
                        long index = count.incrementAndGet();
                        if (index % reportInterval == 0) {
                            progressCallback.accept((int) (index * 95.0 / size));
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                ZioEntry manifest = input.entries.get("META-INF/MANIFEST.MF");
                if(manifest != null) {
                    originManifest = manifest.getData();
                }
                for (ZioEntry inEntry : input.entries.values()) {
                    if (removePredict != null && removePredict.apply(inEntry.getName())) {
                        continue;
                    }
                    taskBundle.submitTask(()->{
                        if (entryMap.containsKey(inEntry.getName())) {
                            ZipEntrySource source = entryMap.get(inEntry.getName());
                            ZioEntry zioEntry = new ZioEntry(inEntry.getName());
                            zioEntry.setCompression(source.getCompressionMethod());
                            try (InputStream inputStream = source.getDataStream()) {
                                if (inputStream != null) {
                                    ByteStreams.copy(inputStream, zioEntry.getOutputStream());
                                }
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            replacedFileSet.put(inEntry.getName(), true);
                            return zioEntry;
                        } else {
                            originEntryName.put(inEntry.getName(), true);
                            return inEntry;
                        }
                    });
                }
                taskBundle.join();
                Sets.SetView<String> difference = Sets.difference(entryMap.keySet(), replacedFileSet.keySet());
                count.set(0);
                taskBundle = MultiprocessingUtil.newTaskBundle((zioEntry) -> {
                    try {
                        zipOutput.write(zioEntry);
                        long index = count.incrementAndGet();
                        progressCallback.accept(95 + (int) (index * 5.0 / difference.size()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                for (String name : difference) {
                    taskBundle.submitTask(()-> {
                        ZipEntrySource source = entryMap.get(name);
                        ZioEntry zioEntry = new ZioEntry(name);
                        zioEntry.setCompression(source.getCompressionMethod());
                        try (InputStream inputStream = source.getDataStream()) {
                            ByteStreams.copy(inputStream, zioEntry.getOutputStream());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        return zioEntry;
                    });
                }
                taskBundle.join();
                progressCallback.accept(100);
            }
        }
        catch (RuntimeException e) {
            if(e.getCause() != null && e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            }
            throw e;
        }
        return new Tuple2<>(originManifest, originEntryName.keySet());
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
                    if (index % reportInterval == 0) {
                        progressCallback.accept((int) (index * 100.0 / size));
                    }
                }
                progressCallback.accept(100);
            }
        }
    }

    @Data
    @AllArgsConstructor
    @EqualsAndHashCode(of = "path")
    public static class ZipEntrySource {
        private String path;
        private int compressionMethod;
        private Supplier<InputStream> dataSupplier;

        public ZipEntrySource(String path, byte[] bytes, int compressionMethod) {
            this.path = path;
            this.compressionMethod = compressionMethod;
            this.dataSupplier = () -> new ByteArrayInputStream(bytes);
        }

        private InputStream getDataStream() {
            if (dataSupplier != null) {
                return dataSupplier.get();
            }
            return null;
        }
    }
}
