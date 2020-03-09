package com.zane.smapiinstaller.utils;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import java.util.List;

public class VersionUtil {
    private static int parseVersionSection(String version) {
        try {
            return Integer.parseInt(version);
        } catch (Exception ignored) {
        }
        List<String> list = Splitter.on("-").splitToList(version);
        switch (list.get(0).toLowerCase()) {
            case "alpha":
                return -2;
            case "beta":
                return -1;
        }
        return 0;
    }
    private static boolean isZero(List<String> versionSections) {
        return !Iterables.filter(versionSections, version -> {
            try {
                int i = Integer.parseInt(version);
                if (i == 0) {
                    return false;
                }
            } catch (Exception ignored) {
            }
            return true;
        }).iterator().hasNext();
    }

    public static int compareVersion(String versionA, String versionB) {
        List<String> versionSectionsA = Splitter.on(".").splitToList(versionA);
        List<String> versionSectionsB = Splitter.on(".").splitToList(versionB);
        for (int i = 0; i < versionSectionsA.size(); i++) {
            if (versionSectionsB.size() <= i) {
                if(isZero(versionSectionsA.subList(i, versionSectionsA.size()))) {
                    return 0;
                }
                return 1;
            }
            int compare = Integer.compare(parseVersionSection(versionSectionsA.get(i)), parseVersionSection(versionSectionsB.get(i)));
            if(compare != 0)
                return compare;
        }
        return 0;
    }
}
