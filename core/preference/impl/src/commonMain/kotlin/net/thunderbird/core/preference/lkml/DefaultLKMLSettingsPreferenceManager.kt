package net.thunderbird.core.preference.lkml

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.thunderbird.core.logging.Logger
import net.thunderbird.core.preference.storage.Storage
import net.thunderbird.core.preference.storage.StorageEditor
import net.thunderbird.core.preference.storage.StoragePersister

private const val TAG = "DefaultLKMLSettingsPreferenceManager"

class DefaultLKMLSettingsPreferenceManager(
    private val logger: Logger,
    private val storagePersister: StoragePersister,
    private val storageEditor: StorageEditor,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private var scope: CoroutineScope = CoroutineScope(SupervisorJob()),
) : LKMLSettingsPreferenceManager {
    private val configState: MutableStateFlow<LKMLSettings> = MutableStateFlow(value = loadConfig())
    private val mutex = Mutex()
    private val storage: Storage
        get() = storagePersister.loadValues()

    override fun getConfig(): LKMLSettings = configState.value
    override fun getConfigFlow(): Flow<LKMLSettings> = configState

    override fun save(config: LKMLSettings) {
        logger.debug(TAG) { "save() called with: config = $config" }
        writeConfig(config)
        configState.update { config }
    }

    private fun loadConfig(): LKMLSettings = LKMLSettings(
        isPatchHighlightEnabled = storage.getBoolean(
            key = KEY_LKML_PATCH_HIGHLIGHT,
            defValue = LKML_SETTINGS_DEFAULT_PATCH_HIGHLIGHT,
        ),
        isStripSignatureReplyEnabled = storage.getBoolean(
            key = KEY_LKML_STRIP_SIGNATURE_REPLY,
            defValue = LKML_SETTINGS_DEFAULT_STRIP_SIGNATURE_REPLY,
        ),
        isTagToolbarEnabled = storage.getBoolean(
            key = KEY_LKML_TAG_TOOLBAR,
            defValue = LKML_SETTINGS_DEFAULT_TAG_TOOLBAR,
        ),
        isConfirmTopPostingEnabled = storage.getBoolean(
            key = KEY_LKML_CONFIRM_TOP_POSTING,
            defValue = LKML_SETTINGS_DEFAULT_CONFIRM_TOP_POSTING,
        ),
    )

    private fun writeConfig(config: LKMLSettings) {
        logger.debug(TAG) { "writeConfig() called with: config = $config" }
        scope.launch(ioDispatcher) {
            mutex.withLock {
                storageEditor.putBoolean(KEY_LKML_PATCH_HIGHLIGHT, config.isPatchHighlightEnabled)
                storageEditor.putBoolean(KEY_LKML_STRIP_SIGNATURE_REPLY, config.isStripSignatureReplyEnabled)
                storageEditor.putBoolean(KEY_LKML_TAG_TOOLBAR, config.isTagToolbarEnabled)
                storageEditor.putBoolean(KEY_LKML_CONFIRM_TOP_POSTING, config.isConfirmTopPostingEnabled)
                storageEditor.commit().also { commited ->
                    logger.verbose(TAG) { "writeConfig: storageEditor.commit() resulted in: $commited" }
                }
            }
        }
    }
}
