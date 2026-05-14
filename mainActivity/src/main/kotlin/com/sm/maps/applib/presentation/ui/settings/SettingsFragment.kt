@file:Suppress("DEPRECATION")
package com.sm.maps.applib.presentation.ui.settings

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.PreferenceGroup
import android.preference.PreferenceManager
import com.sm.maps.applib.R
import com.sm.maps.applib.constants.PrefConstants
import com.sm.maps.applib.constants.PrefConstants.PREF_USERMAPS_
import com.sm.maps.applib.kml.XMLparser.PredefMapsParser
import com.sm.maps.applib.preference.MixedMapsPreference
import com.sm.maps.applib.preference.UserMapsPrefActivity
import com.sm.maps.applib.tileprovider.TileSourceBase
import com.sm.maps.applib.utils.CheckBoxPreferenceExt
import com.sm.maps.applib.utils.Ut
import org.openintents.filemanager.FileManagerActivity
import org.openintents.filemanager.intents.FileManagerIntents
import java.io.File
import javax.xml.parsers.SAXParserFactory

class SettingsFragment : PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val ctx = activity
        val aPref = PreferenceManager.getDefaultSharedPreferences(ctx)

        if (aPref.getString("pref_dir_main", "NO").equals("NO", ignoreCase = true)) {
            val base = Ut.getExternalStorageDirectory(ctx) + "/rmaps/"
            aPref.edit()
                .putString("pref_dir_main", base)
                .putString("pref_dir_maps", "${base}maps/")
                .putString("pref_dir_import", "${base}import/")
                .putString("pref_dir_export", "${base}export/")
                .apply()
        }

        addPreferencesFromResource(R.xml.mainpreferences)

        val arrEntry = mutableListOf("Default")
        val arrEntryValues = mutableListOf("")
        val folderCursors = Ut.getRMapsMainDir(ctx, "icons/cursors")
        folderCursors?.listFiles()
            ?.filter { it.name.lowercase().endsWith(".png") }
            ?.forEach { arrEntry.add(it.name); arrEntryValues.add(it.name) }

        val entries = arrEntry.toTypedArray()
        val entryValues = arrEntryValues.toTypedArray()
        (findPreference("pref_person_icon") as? ListPreference)?.apply {
            this.entries = entries; this.entryValues = entryValues
        }
        (findPreference("pref_arrow_icon") as? ListPreference)?.apply {
            this.entries = entries; this.entryValues = entryValues
        }

        findPreference("pref_dir_main")?.apply {
            summary = aPref.getString("pref_dir_main", "")
            setOnPreferenceClickListener {
                pickDir(R.string.pref_dir_main, Uri.parse(aPref.getString("pref_dir_main", ""))); true
            }
        }
        findPreference("pref_dir_maps")?.apply {
            summary = aPref.getString("pref_dir_maps", "")
            setOnPreferenceClickListener {
                pickDir(R.string.pref_dir_maps, Uri.parse(aPref.getString("pref_dir_maps", ""))); true
            }
        }
        findPreference("pref_dir_import")?.apply {
            summary = aPref.getString("pref_dir_import", "")
            setOnPreferenceClickListener {
                pickDir(R.string.pref_dir_import, Uri.parse(aPref.getString("pref_dir_import", ""))); true
            }
        }
        findPreference("pref_dir_export")?.apply {
            summary = aPref.getString("pref_dir_export", "")
            setOnPreferenceClickListener {
                pickDir(R.string.pref_dir_export, Uri.parse(aPref.getString("pref_dir_export", ""))); true
            }
        }

        findPreference("pref_main_usermaps")?.summary =
            "Maps from " + aPref.getString("pref_dir_maps", "")

        val prefMapsGroup = findPreference("pref_predefmaps_mapsgroup") as? PreferenceGroup
        val prefOverlaysGroup = findPreference("pref_predefmaps_overlaysgroup") as? PreferenceGroup
        if (prefMapsGroup != null && prefOverlaysGroup != null) {
            try {
                val parser = SAXParserFactory.newInstance().newSAXParser()
                val input = resources.openRawResource(R.raw.predefmaps)
                parser.parse(input, PredefMapsParser(prefMapsGroup, prefOverlaysGroup, activity))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        LoadUserMaps(Ut.getRMapsMapsDir(ctx))

        findPreference("pref_main_mixmaps")?.intent =
            Intent(ctx, MixedMapsPreference::class.java)

        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroy() {
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    override fun onSharedPreferenceChanged(aPref: SharedPreferences, aKey: String?) {
        if (aKey == null) return
        val ctx = activity
        when {
            aKey.equals("pref_dir_maps", ignoreCase = true) -> {
                val mapsDir = aPref.getString("pref_dir_maps", "") ?: ""
                findPreference("pref_main_usermaps")?.summary = "Maps from $mapsDir"
                findPreference(aKey)?.summary = mapsDir
                val dir = File(mapsDir.plus("/").replace("//", "/"))
                if (!dir.exists() && Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) dir.mkdirs()
                if (dir.exists()) LoadUserMaps(dir)
            }
            Ut.equalsIgnoreCase(aKey, 0, 9, "pref_dir_") -> {
                findPreference("pref_dir_main")?.summary = aPref.getString("pref_dir_main", "")
                findPreference("pref_dir_import")?.summary = aPref.getString("pref_dir_import", "")
                findPreference("pref_dir_export")?.summary = aPref.getString("pref_dir_export", "")
            }
            aKey.equals("pref_locale", ignoreCase = true) -> {
                activity.finish()
                startActivity(Intent(activity, SettingsActivity::class.java))
            }
            Ut.equalsIgnoreCase(aKey, 0, 14, PrefConstants.PREF_USERMAPS_) -> {
                if (aKey.endsWith("name") && findPreference(aKey.replace("_name", "")) != null) {
                    findPreference(aKey.replace("_name", "")).title = aPref.getString(aKey, "")
                } else if (aKey.endsWith("_enabled") && findPreference(aKey.replace("_enabled", "")) != null) {
                    (findPreference(aKey.replace("_enabled", "")) as? CheckBoxPreferenceExt)
                        ?.setChecked(aPref.getBoolean(aKey, true))
                }
            }
            Ut.equalsIgnoreCase(aKey, 0, 16, PrefConstants.PREF_PREDEFMAPS_) -> {
                (findPreference("${aKey}_screen") as? CheckBoxPreferenceExt)
                    ?.setChecked(aPref.getBoolean(aKey, true))
            }
        }
    }

    private fun pickDir(id: Int, uri: Uri) {
        val intent = Intent(activity, FileManagerActivity::class.java).apply {
            action = FileManagerIntents.ACTION_PICK_DIRECTORY
            data = uri
        }
        startActivityForResult(intent, id and 0xFFFF)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val dirKeys = mapOf(
            R.string.pref_dir_main to "pref_dir_main",
            R.string.pref_dir_maps to "pref_dir_maps",
            R.string.pref_dir_import to "pref_dir_import",
            R.string.pref_dir_export to "pref_dir_export"
        )
        val prefName = dirKeys.entries.firstOrNull { (id, _) -> requestCode == (id and 0xFFFF) }?.value
            ?: return

        if (resultCode == android.app.Activity.RESULT_OK && data != null) {
            var filename = Uri.decode(data.dataString) ?: return
            if (filename.startsWith("file://")) filename = filename.substring(7)
            val aPref = PreferenceManager.getDefaultSharedPreferences(activity)
            aPref.edit().putString(prefName, filename).apply()
            onSharedPreferenceChanged(aPref, prefName)
        }
    }

    private fun LoadUserMaps(folder: File?) {
        val prefUserMapsGroup = findPreference("pref_usermaps_mapsgroup") as? PreferenceGroup ?: return
        prefUserMapsGroup.removeAll()
        val ctx = activity
        val aPref = PreferenceManager.getDefaultSharedPreferences(ctx)
        val prefEditor = aPref.edit()

        folder?.listFiles()?.forEach { file ->
            val name = file.name.lowercase()
            if (name.endsWith(getString(R.string.mnm)) || name.endsWith(getString(R.string.tar)) ||
                name.endsWith(getString(R.string.sqlitedb)) || name.endsWith(getString(R.string.mbtiles))) {
                val id = Ut.FileName2ID(file.name)
                prefEditor.putString("${PREF_USERMAPS_}${id}_baseurl", file.absolutePath)

                val pref = CheckBoxPreferenceExt(ctx, "${PREF_USERMAPS_}${id}_enabled", false)
                pref.key = "${PREF_USERMAPS_}$id"
                pref.title = aPref.getString("${PREF_USERMAPS_}${id}_name", file.name)
                pref.summary = file.absolutePath
                pref.intent = Intent(ctx, UserMapsPrefActivity::class.java)
                    .putExtra("Key", "${PREF_USERMAPS_}$id")
                    .putExtra("ID", "${TileSourceBase.USERMAP_}$id")
                    .putExtra("Name", file.name)
                    .putExtra("AbsolutePath", file.absolutePath)
                prefUserMapsGroup.addPreference(pref)
            }
        }
        prefEditor.apply()
    }
}
