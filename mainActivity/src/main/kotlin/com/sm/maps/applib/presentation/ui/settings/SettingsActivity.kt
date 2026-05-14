@file:Suppress("DEPRECATION")
package com.sm.maps.applib.presentation.ui.settings

import android.app.Activity
import android.os.Bundle
import com.sm.maps.applib.R

class SettingsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        if (savedInstanceState == null) {
            fragmentManager.beginTransaction()
                .replace(R.id.settings_container, SettingsFragment())
                .commit()
        }
    }
}
