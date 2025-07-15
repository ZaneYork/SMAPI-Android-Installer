package org.jf.dexlib2.analysis;

import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.OatFile;
import org.jf.dexlib2.iface.MultiDexContainer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PathEntryLoader {
    Opcodes opcodes;

    public Opcodes getOpcodes() {
        return opcodes;
    }

    final Set<File> loadedFiles = new HashSet<>();
    final List<ClassProvider> classProviders = new ArrayList<>(1);

    public List<ClassProvider> getClassProviders() {
        return classProviders;
    }

    public PathEntryLoader(Opcodes opcodes) {
        this.opcodes = opcodes;
    }


    public List<ClassProvider> getResolvedClassProviders() {
        return classProviders;
    }

    public void loadEntry( File entryFile, boolean loadOatDependencies)
            throws IOException, NoDexException {
        if (loadedFiles.contains(entryFile)) {
            return;
        }

        MultiDexContainer<? extends DexBackedDexFile> container;
        try {
            container = DexFileFactory.loadDexContainer(entryFile, opcodes);
        } catch (DexFileFactory.UnsupportedFileTypeException ex) {
            throw new ClassPathResolver.ResolveException(ex);
        }

        List<String> entryNames = container.getDexEntryNames();

        if (entryNames.isEmpty()) {
            throw new NoDexException("%s contains no dex file", entryFile);
        }

        loadedFiles.add(entryFile);

        for (String entryName : entryNames) {
            classProviders.add(new DexClassProvider(container.getEntry(entryName).getDexFile()));
        }

        if (loadOatDependencies && container instanceof OatFile) {
            List<String> oatDependencies = ((OatFile) container).getBootClassPath();
            if (!oatDependencies.isEmpty()) {
                try {
                    loadOatDependencies(entryFile.getParentFile(), oatDependencies);
                } catch (ClassPathResolver.NotFoundException ex) {
                    throw new ClassPathResolver.ResolveException(ex, "Error while loading oat file %s", entryFile);
                } catch (NoDexException ex) {
                    throw new ClassPathResolver.ResolveException(ex, "Error while loading dependencies for oat file %s", entryFile);
                }
            }
        }
    }

    private void loadOatDependencies( File directory,  List<String> oatDependencies)
            throws IOException, NoDexException, ClassPathResolver.NotFoundException {
        // We assume that all oat dependencies are located in the same directory as the oat file
        for (String oatDependency : oatDependencies) {
            String oatDependencyName = getFilenameForOatDependency(oatDependency);
            File file = new File(directory, oatDependencyName);
            if (!file.exists()) {
                throw new ClassPathResolver.NotFoundException("Cannot find dependency %s in %s", oatDependencyName, directory);
            }

            loadEntry(file, false);
        }
    }


    private String getFilenameForOatDependency(String oatDependency) {
        int index = oatDependency.lastIndexOf('/');

        String dependencyLeaf = oatDependency.substring(index + 1);
        if (dependencyLeaf.endsWith(".art")) {
            return dependencyLeaf.substring(0, dependencyLeaf.length() - 4) + ".oat";
        }
        return dependencyLeaf;
    }

    static class NoDexException extends Exception {
        public NoDexException(String message, Object... formatArgs) {
            super(String.format(message, formatArgs));
        }
    }
}