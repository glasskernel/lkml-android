package net.thunderbird.core.preference.lkml

import net.thunderbird.core.preference.PreferenceManager

const val KEY_LKML_PATCH_HIGHLIGHT = "lkml_patch_highlight"
const val KEY_LKML_STRIP_SIGNATURE_REPLY = "lkml_strip_signature_reply"
const val KEY_LKML_TAG_TOOLBAR = "lkml_tag_toolbar"
const val KEY_LKML_CONFIRM_TOP_POSTING = "lkml_confirm_top_posting"

interface LKMLSettingsPreferenceManager : PreferenceManager<LKMLSettings>
