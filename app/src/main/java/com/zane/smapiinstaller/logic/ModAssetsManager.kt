package com.zane.smapiinstaller.logic

import android.content.pm.PackageInfo
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import androidx.documentfile.provider.DocumentUtils
import androidx.navigation.Navigation.findNavController
import com.fasterxml.jackson.core.type.TypeReference
import com.google.common.base.Joiner
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableListMultimap
import com.google.common.collect.Lists
import com.google.common.collect.Multimaps
import com.google.common.collect.Queues
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.microsoft.appcenter.crashes.Crashes
import com.zane.smapiinstaller.MobileNavigationDirections
import com.zane.smapiinstaller.R
import com.zane.smapiinstaller.constant.Constants
import com.zane.smapiinstaller.constant.DialogAction
import com.zane.smapiinstaller.dto.ModUpdateCheckRequestDto
import com.zane.smapiinstaller.dto.ModUpdateCheckRequestDto.ModInfo
import com.zane.smapiinstaller.dto.ModUpdateCheckRequestDto.SemanticVersion
import com.zane.smapiinstaller.dto.ModUpdateCheckResponseDto
import com.zane.smapiinstaller.dto.ModUpdateCheckResponseDto.UpdateInfo
import com.zane.smapiinstaller.dto.Tuple2
import com.zane.smapiinstaller.entity.ModManifestEntry
import com.zane.smapiinstaller.logic.CommonLogic.checkDataRootPermission
import com.zane.smapiinstaller.logic.CommonLogic.copyDocument
import com.zane.smapiinstaller.logic.CommonLogic.getActivityFromView
import com.zane.smapiinstaller.logic.CommonLogic.pathToTreeUri
import com.zane.smapiinstaller.utils.DialogUtils
import com.zane.smapiinstaller.utils.DialogUtils.showAlertDialog
import com.zane.smapiinstaller.utils.FileUtils
import com.zane.smapiinstaller.utils.JsonCallback
import com.zane.smapiinstaller.utils.JsonUtil
import com.zane.smapiinstaller.utils.VersionUtil
import org.apache.commons.lang3.StringUtils
import org.zeroturnaround.zip.ZipUtil
import java.io.File
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Mod资源管理器
 *
 * @author Zane
 */
class ModAssetsManager(private val root: View) {
    private val checkUpdating = AtomicBoolean(false)

    /**
     * 安装默认Mod
     *
     * @return 是否安装成功
     */
    fun installDefaultMods(): Boolean {
        val context = getActivityFromView(root)
        val modManifestEntries: List<ModManifestEntry> = context?.let {
            FileUtils.getAssetJson(it,
                "mods_manifest.json",
                object : TypeReference<List<ModManifestEntry>>() {})
        } ?: return false
        val modFolder = File(FileUtils.stadewValleyBasePath, Constants.MOD_PATH)
        val installedModMap = Multimaps.index(findAllInstalledMods()) { obj -> obj!!.uniqueID }
        val unpackedMods: MutableList<File> = ArrayList()
        for (mod in modManifestEntries) {
            if (installedModMap.containsKey(mod.uniqueID)) {
                val installedMods = installedModMap[mod.uniqueID]
                if (installedMods.size > 1) {
                    showAlertDialog(
                        root,
                        R.string.error,
                        String.format(context.getString(R.string.duplicate_mod_found),
                            installedMods.joinToString(",") { item ->
                                FileUtils.toPrettyPath(item.assetPath)
                            })
                    )
                    return false
                }
                if (mod.cleanInstall != null && mod.cleanInstall) {
                    // Delete origin version with different Unique ID
                    if (mod.originUniqueId != null) {
                        installedModMap.keys().filter { id -> mod.originUniqueId.contains(id) }
                            .map { key -> installedModMap[key] }.forEach { list ->
                                for (entry in list) {
                                    try {
                                        org.zeroturnaround.zip.commons.FileUtils.deleteDirectory(
                                            File(entry.assetPath)
                                        )
                                    } catch (e: IOException) {
                                        Log.e(TAG, "Install Mod Error", e)
                                    }
                                }
                            }
                    }
                    // Delete old version
                    if (installedMods.size > 0 && mod.version != null && VersionUtil.compareVersion(
                            installedMods[0].version, mod.version
                        ) < 0
                    ) {
                        try {
                            org.zeroturnaround.zip.commons.FileUtils.deleteDirectory(
                                File(
                                    installedMods[0].assetPath
                                )
                            )
                        } catch (e: IOException) {
                            Log.e(TAG, "Install Mod Error", e)
                        }
                    }
                }
                if (installedMods.size > 0) {
                    try {
                        val targetFile = File(installedMods[0].assetPath)
                        ZipUtil.unpack(
                            context.assets.open(mod.assetPath), targetFile
                        ) { name -> StringUtils.removeStart(name, mod.name + "/") }
                        unpackedMods.add(targetFile)
                    } catch (e: IOException) {
                        Log.e(TAG, "Install Mod Error", e)
                    }
                    continue
                }
            }
            try {
                ZipUtil.unpack(context.assets.open(mod.assetPath), modFolder)
                unpackedMods.add(File(modFolder, mod.name))
            } catch (e: IOException) {
                Log.e(TAG, "Install Mod Error", e)
            }
        }
        if (checkDataRootPermission(context)) {
            val targetDirUri = pathToTreeUri(Constants.TARGET_DATA_FILE_URI)
            val documentFile = DocumentFile.fromTreeUri(context, targetDirUri)
            if (documentFile != null) {
                val filesDoc = DocumentUtils.findFile(context, documentFile, "files")
                val modsDoc = DocumentUtils.findFile(context, filesDoc, "Mods")
                    ?: filesDoc.createDirectory("Mods")
                modsDoc?.let {
                    for (mod in unpackedMods) {
                        copyDocument(context, mod, modsDoc)
                    }
                }
            }
        }
        return true
    }

    /**
     * 检查Mod环境
     *
     * @param returnCallback 回调函数
     */
    fun checkModEnvironment(returnCallback: (Boolean) -> Unit) {
        val installedModMap = Multimaps.index(findAllInstalledMods(true)) { obj -> obj!!.uniqueID }
        checkDuplicateMod(installedModMap) { isConfirm ->
            if (isConfirm) {
                checkUnsatisfiedDependencies(installedModMap) { isConfirm2 ->
                    if (isConfirm2) {
                        checkContentpacks(installedModMap, returnCallback)
                    } else {
                        returnCallback.invoke(false)
                    }
                }
            } else {
                returnCallback.invoke(false)
            }
        }
    }

    /**
     * 检查是否有重复Mod
     *
     * @param installedModMap 已安装Mod集合
     * @param returnCallback  回调函数
     */
    private fun checkDuplicateMod(
        installedModMap: ImmutableListMultimap<String, ModManifestEntry>,
        returnCallback: (Boolean) -> Unit
    ) {
        // Duplicate mod check
        val list = Lists.newArrayList<String?>()
        for (key in installedModMap.keySet()) {
            val installedMods = installedModMap[key]
            if (installedMods.size > 1) {
                list.add(installedMods.joinToString(",") { item -> FileUtils.toPrettyPath(item.assetPath) })
            }
        }
        if (!list.isEmpty()) {
            DialogUtils.showConfirmDialog(
                root,
                R.string.error,
                root.context.getString(R.string.duplicate_mod_found, Joiner.on(";").join(list)),
                R.string.continue_text,
                R.string.abort
            ) { _, which -> returnCallback.invoke(which === DialogAction.POSITIVE) }
        } else {
            returnCallback.invoke(true)
        }
    }

    /**
     * 检查是否有依赖关系缺失
     *
     * @param installedModMap 已安装Mod集合
     * @param returnCallback  回调函数
     */
    private fun checkUnsatisfiedDependencies(
        installedModMap: ImmutableListMultimap<String, ModManifestEntry>,
        returnCallback: (Boolean) -> Unit
    ) {
        val dependencyErrors = installedModMap.values()
            .mapNotNull { mod -> checkModDependencyError(mod, installedModMap) }.toList()
        if (dependencyErrors.isNotEmpty()) {
            DialogUtils.showConfirmDialog(
                root,
                R.string.error,
                dependencyErrors.map { obj -> obj.first }.joinToString(";"),
                R.string.continue_text,
                R.string.menu_download
            ) { _, which ->
                if (which === DialogAction.POSITIVE) {
                    returnCallback.invoke(true)
                } else {
                    val list =
                        dependencyErrors.asSequence().map { obj -> obj.second }.flatten().distinct()
                            .map { item ->
                                val modInfo = ModInfo()
                                modInfo.id = item
                                modInfo
                            }.distinct().toList()
                    redirectModDownload(list)
                    returnCallback.invoke(false)
                }
            }
        } else {
            returnCallback.invoke(true)
        }
    }

    /**
     * 检查是否有资源包依赖Mod没有安装
     *
     * @param installedModMap 已安装Mod集合
     * @param returnCallback  回调函数
     */
    private fun checkContentpacks(
        installedModMap: ImmutableListMultimap<String, ModManifestEntry>,
        returnCallback: (Boolean) -> Unit
    ) {
        val dependencyErrors = installedModMap.values().mapNotNull { mod ->
            checkContentPackDependencyError(
                mod, installedModMap
            )
        }.toList()
        if (dependencyErrors.isNotEmpty()) {
            DialogUtils.showConfirmDialog(
                root,
                R.string.error,
                dependencyErrors.map { obj -> obj.first }.joinToString(";"),
                R.string.continue_text,
                R.string.menu_download
            ) { _, which ->
                if (which === DialogAction.POSITIVE) {
                    returnCallback.invoke(true)
                } else {
                    val list = dependencyErrors.map { item ->
                        val modInfo = ModInfo()
                        modInfo.id = item.second
                        modInfo
                    }.distinct().toList()
                    redirectModDownload(list)
                    returnCallback.invoke(false)
                }
            }
        } else {
            returnCallback.invoke(true)
        }
    }

    private fun redirectModDownload(list: List<ModInfo>) {
        val requestDto = ModUpdateCheckRequestDto(list)
        try {
            requestDto.isIncludeExtendedMetadata = true
            OkGo.post<List<ModUpdateCheckResponseDto>>(Constants.UPDATE_CHECK_SERVICE_URL)
                .upJson(JsonUtil.toJson(requestDto)).execute(object :
                    JsonCallback<List<ModUpdateCheckResponseDto>>(object :
                        TypeReference<List<ModUpdateCheckResponseDto>>() {}) {
                    override fun onError(response: Response<List<ModUpdateCheckResponseDto>>) {
                        super.onError(response)
                    }

                    override fun onSuccess(response: Response<List<ModUpdateCheckResponseDto>>) {
                        val checkResponseDtos = response.body()
                        if (checkResponseDtos != null) {
                            val validList = checkResponseDtos.filter { dto ->
                                dto.metadata != null && dto.metadata.main != null && StringUtils.isNoneBlank(
                                    dto.metadata.main.url
                                )
                            }.toList()
                            try {
                                if (validList.isNotEmpty()) {
                                    for (dto in validList) {
                                        dto.suggestedUpdate = UpdateInfo(
                                            dto.metadata.main.version, dto.metadata.main.url
                                        )
                                    }
                                    getActivityFromView(root)?.let {
                                        val controller =
                                            findNavController(it, R.id.nav_host_fragment)
                                        controller.navigate(
                                            MobileNavigationDirections.actionNavAnyToModUpdateFragment(
                                                JsonUtil.toJson(validList)
                                            )
                                        )
                                    }
                                }
                            } catch (e: Exception) {
                                Crashes.trackError(e)
                            }
                        }
                    }
                })
        } catch (e: Exception) {
            Crashes.trackError(e)
        }
    }

    fun checkModUpdate(callback: (List<ModUpdateCheckResponseDto>) -> Unit) {
        if (checkUpdating.get()) {
            return
        }
        val list = findAllInstalledMods(false).asSequence()
            .filter { mod -> mod.updateKeys != null && mod.updateKeys.isNotEmpty() }
            .map { mod -> ModInfo.fromModManifestEntry(mod) }.distinct()
            .filter { modInfo -> modInfo.installedVersion != null }.toList()
        val context = getActivityFromView(root) ?: return
        val gamePackageInfo: PackageInfo = GameLauncher.getGamePackageInfo(context) ?: return
        context.runOnUiThread {
            Toast.makeText(
                context, R.string.mod_version_update_checking, Toast.LENGTH_SHORT
            ).show()
        }
        checkUpdating.set(true)
        try {
            val requestDto =
                ModUpdateCheckRequestDto(list, SemanticVersion(gamePackageInfo.versionName))
            OkGo.post<List<ModUpdateCheckResponseDto>>(Constants.UPDATE_CHECK_SERVICE_URL)
                .upJson(JsonUtil.toJson(requestDto)).execute(object :
                    JsonCallback<List<ModUpdateCheckResponseDto>>(object :
                        TypeReference<List<ModUpdateCheckResponseDto>>() {}) {
                    override fun onError(response: Response<List<ModUpdateCheckResponseDto>>) {
                        super.onError(response)
                        checkUpdating.set(false)
                    }

                    override fun onSuccess(response: Response<List<ModUpdateCheckResponseDto>>) {
                        checkUpdating.set(false)
                        val checkResponseDtos = response.body()
                        checkResponseDtos?.filter { dto -> dto.suggestedUpdate != null }?.toList()
                            ?.apply(callback)
                    }
                })
        } catch (e: Exception) {
            checkUpdating.set(false)
            Crashes.trackError(e)
        }
    }

    private fun checkModDependencyError(
        mod: ModManifestEntry, installedModMap: ImmutableListMultimap<String, ModManifestEntry>
    ): Tuple2<String?, List<String?>>? {
        if (mod.dependencies != null) {
            val unsatisfiedDependencies = mod.dependencies.filter { dependency ->
                isDependencyIsExist(
                    dependency, installedModMap
                )
            }.toList()
            if (unsatisfiedDependencies.isNotEmpty()) {
                return Tuple2(
                    root.context.getString(R.string.error_depends_on_mod,
                        mod.uniqueID,
                        unsatisfiedDependencies.joinToString(",") { obj -> obj.uniqueID }),
                    unsatisfiedDependencies.map { obj -> obj.uniqueID }.toList()
                )
            }
        }
        return null
    }

    private fun isDependencyIsExist(
        dependency: ModManifestEntry,
        installedModMap: ImmutableListMultimap<String, ModManifestEntry>
    ): Boolean {
        if (dependency.isRequired != null && !dependency.isRequired) {
            return false
        }
        var entries = installedModMap[dependency.uniqueID]
        if (entries.size == 0) {
            for (key in installedModMap.keySet()) {
                if (StringUtils.equalsIgnoreCase(key, dependency.uniqueID)) {
                    dependency.uniqueID = key
                    entries = installedModMap[dependency.uniqueID]
                    break
                }
            }
        }
        if (entries.size != 1) {
            return true
        }
        val version = entries[0].version
        if (StringUtils.isBlank(version)) {
            return true
        }
        return if (StringUtils.isBlank(dependency.minimumVersion)) {
            false
        } else VersionUtil.compareVersion(version, dependency.minimumVersion) < 0
    }

    private fun checkContentPackDependencyError(
        mod: ModManifestEntry, installedModMap: ImmutableListMultimap<String, ModManifestEntry>
    ): Tuple2<String?, String?>? {
        val dependency = mod.contentPackFor
        if (dependency != null) {
            if (dependency.isRequired != null && !dependency.isRequired) {
                return null
            }
            var entries = installedModMap[dependency.uniqueID]
            if (entries.size == 0) {
                for (key in installedModMap.keySet()) {
                    if (StringUtils.equalsIgnoreCase(key, dependency.uniqueID)) {
                        dependency.uniqueID = key
                        entries = installedModMap[dependency.uniqueID]
                        break
                    }
                }
            }
            if (entries.size != 1) {
                return Tuple2(
                    root.context.getString(
                        R.string.error_depends_on_mod, mod.uniqueID, dependency.uniqueID
                    ), dependency.uniqueID
                )
            }
            val version = entries[0].version
            if (!StringUtils.isBlank(version)) {
                if (StringUtils.isBlank(dependency.minimumVersion)) {
                    return null
                }
                if (VersionUtil.compareVersion(version, dependency.minimumVersion) < 0) {
                    return Tuple2(
                        root.context.getString(
                            R.string.error_depends_on_mod_version,
                            mod.uniqueID,
                            dependency.uniqueID,
                            dependency.minimumVersion
                        ), dependency.uniqueID
                    )
                }
            }
            return null
        }
        return null
    }

    companion object {
        private const val TAG = "MANAGER"

        /**
         * 查找第一个匹配的Mod
         *
         * @param filter 过滤规则
         * @return Mod信息
         */
        @JvmStatic
        fun findFirstModIf(filter: (ModManifestEntry) -> Boolean): ModManifestEntry? {
            val files = Queues.newConcurrentLinkedQueue<File>()
            files.add(File(FileUtils.stadewValleyBasePath, Constants.MOD_PATH))
            do {
                val currentFile = files.poll()
                if (currentFile != null && currentFile.exists()) {
                    var foundManifest = false
                    val listFiles = currentFile.listFiles { obj -> obj.isFile } ?: continue
                    for (file in listFiles) {
                        if (StringUtils.equalsIgnoreCase(file.name, "manifest.json")) {
                            val manifest = FileUtils.getFileJson(file, ModManifestEntry::class.java)
                            foundManifest = true
                            if (manifest != null) {
                                manifest.assetPath = file.parentFile?.absolutePath
                                if (filter.invoke(manifest)) {
                                    return manifest
                                }
                            }
                            break
                        }
                    }
                    if (!foundManifest) {
                        val subFolder = currentFile.listFiles { obj -> obj.isDirectory }
                        subFolder?.let { files.addAll(Lists.newArrayList(*it)) }
                    }
                }
            } while (!files.isEmpty())
            return null
        }

        /**
         * 查找全部已识别Mod
         *
         * @param ignoreDisabledMod 是否忽略禁用的mod
         * @return Mod信息列表
         */
        @JvmOverloads
        fun findAllInstalledMods(ignoreDisabledMod: Boolean = false): MutableList<ModManifestEntry> {
            val files = Queues.newConcurrentLinkedQueue<File>()
            files.add(File(FileUtils.stadewValleyBasePath, Constants.MOD_PATH))
            val mods: MutableList<ModManifestEntry> = ArrayList(30)
            do {
                val currentFile = files.poll()
                if (currentFile != null && currentFile.exists()) {
                    var foundManifest = false
                    val listFiles = currentFile.listFiles { obj -> obj.isFile }
                    if (listFiles != null) {
                        for (file in listFiles) {
                            if (StringUtils.equalsIgnoreCase(file.name, "manifest.json")) {
                                val manifest =
                                    FileUtils.getFileJson(file, ModManifestEntry::class.java)
                                foundManifest = true
                                if (manifest != null && StringUtils.isNoneBlank(manifest.uniqueID)) {
                                    if (ignoreDisabledMod && StringUtils.startsWith(
                                            file.parentFile?.name, "."
                                        )
                                    ) {
                                        break
                                    }
                                    manifest.assetPath = file.parentFile?.absolutePath
                                    manifest.lastModified = file.lastModified()
                                    mods.add(manifest)
                                }
                                break
                            }
                        }
                    }
                    if (!foundManifest) {
                        val listDirectories = currentFile.listFiles { obj -> obj.isDirectory }
                        if (listDirectories != null) {
                            files.addAll(Lists.newArrayList(*listDirectories))
                        }
                    }
                }
            } while (!files.isEmpty())
            return mods
        }
    }
}