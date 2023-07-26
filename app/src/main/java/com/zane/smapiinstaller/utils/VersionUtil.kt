package com.zane.smapiinstaller.utils

import com.google.common.base.Splitter
import org.apache.commons.lang3.StringUtils

/**
 * 版本比较工具
 * @author Zane
 */
object VersionUtil {
    /**
     * 比较单个版本段
     * @param sectionA sectionA
     * @param sectionB sectionB
     * @return 比较结果
     */
    private fun compareVersionSection(sectionA: String, sectionB: String): Int {
        try {
            return Integer.compare(sectionA.toInt(), sectionB.toInt())
        } catch (ignored: Exception) {
        }
        val listA = Splitter.on("-").splitToList(sectionA)
        val listB = Splitter.on("-").splitToList(sectionB)
        var i: Int
        i = 0
        while (i < listA.size && i < listB.size) {
            var intA: Int? = null
            var intB: Int? = null
            try {
                intA = listA[i].toInt()
                return Integer.compare(intA, listB[i].toInt())
            } catch (ignored: Exception) {
                try {
                    intB = listB[i].toInt()
                } catch (ignored2: Exception) {
                }
            }
            if (StringUtils.equals(listA[i], listB[i])) {
                i++
                continue
            }
            if (intA != null && intB == null) {
                return 1
            } else if (intA == null) {
                return -1
            }
            return listA[i].compareTo(listB[i])
        }
        return Integer.compare(listA.size, listB.size)
    }

    /**
     * 判断是否为空版本段
     * @param versionSections 版本段列表
     * @return 是否为空版本段
     */
    private fun isZero(versionSections: List<String>): Boolean {
        return versionSections.none { version ->
            try {
                val i = version.toInt()
                if (i == 0) {
                    return false
                }
            } catch (ignored: Exception) {
            }
            true
        }
    }

    /**
     * 比较两个版本
     * @param versionA versionA
     * @param versionB versionB
     * @return 比较结果
     */
    fun compareVersion(versionA: String, versionB: String): Int {
        val versionSectionsA = Splitter.on(".").splitToList(versionA)
        val versionSectionsB = Splitter.on(".").splitToList(versionB)
        for (i in versionSectionsA.indices) {
            if (versionSectionsB.size <= i) {
                return if (isZero(
                        versionSectionsA.subList(
                            i,
                            versionSectionsA.size
                        )
                    )
                ) {
                    0
                } else 1
            }
            val compare = compareVersionSection(
                versionSectionsA[i], versionSectionsB[i]
            )
            if (compare != 0) {
                return compare
            }
        }
        return if (versionSectionsA.size < versionSectionsB.size) {
            if (isZero(
                    versionSectionsB.subList(
                        versionSectionsA.size,
                        versionSectionsB.size
                    )
                )
            ) {
                0
            } else -1
        } else 0
    }
}