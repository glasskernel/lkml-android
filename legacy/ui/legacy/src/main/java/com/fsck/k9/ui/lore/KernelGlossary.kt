package com.fsck.k9.ui.lore

import android.content.Context
import com.fsck.k9.ui.R
import org.json.JSONObject
import java.io.InputStream

/**
 * Provides access to a local dictionary of Linux Kernel terminology.
 */
class KernelGlossary(private val context: Context) {

    private val glossary: Map<String, String> by lazy {
        loadGlossary()
    }

    private fun loadGlossary(): Map<String, String> {
        val resultMap = mutableMapOf<String, String>()
        try {
            val inputStream: InputStream = context.resources.openRawResource(R.raw.kernel_glossary)
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)
            
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                resultMap[key.lowercase()] = jsonObject.getString(key)
            }
        } catch (e: Exception) {
            // Log error
        }
        return resultMap
    }

    /**
     * Looks up a term in the glossary. Returns null if not found.
     */
    fun lookup(term: String): String? {
        return glossary[term.trim().lowercase()]
    }
}
