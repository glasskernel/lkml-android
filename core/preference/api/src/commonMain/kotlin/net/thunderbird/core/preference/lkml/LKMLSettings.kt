package net.thunderbird.core.preference.lkml

const val LKML_SETTINGS_DEFAULT_PATCH_HIGHLIGHT = true
const val LKML_SETTINGS_DEFAULT_STRIP_SIGNATURE_REPLY = false
const val LKML_SETTINGS_DEFAULT_TAG_TOOLBAR = true
const val LKML_SETTINGS_DEFAULT_CONFIRM_TOP_POSTING = false

data class LKMLSettings(
    val isPatchHighlightEnabled: Boolean = LKML_SETTINGS_DEFAULT_PATCH_HIGHLIGHT,
    val isStripSignatureReplyEnabled: Boolean = LKML_SETTINGS_DEFAULT_STRIP_SIGNATURE_REPLY,
    val isTagToolbarEnabled: Boolean = LKML_SETTINGS_DEFAULT_TAG_TOOLBAR,
    val isConfirmTopPostingEnabled: Boolean = LKML_SETTINGS_DEFAULT_CONFIRM_TOP_POSTING,
)
