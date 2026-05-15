package com.sm.maps.applib.presentation.ui.downloader

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sm.maps.applib.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DownloaderHostActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_downloader_host)

        if (savedInstanceState == null) {
            val mapId = intent.getStringExtra("MAPID") ?: ""
            val lat = intent.getIntExtra("Latitude", 0)
            val lon = intent.getIntExtra("Longitude", 0)
            val zoom = intent.getIntExtra("ZoomLevel", 0)

            val fragment = AreaSelectorFragment.newInstance(mapId, lat, lon, zoom)
            supportFragmentManager.beginTransaction()
                .replace(R.id.downloader_fragment_container, fragment)
                .commit()
        }
    }

    fun navigateToZoomSelection() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.downloader_fragment_container, ZoomSelectionFragment())
            .addToBackStack(null)
            .commit()
    }

    fun navigateToProgress() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.downloader_fragment_container, DownloadProgressFragment())
            .addToBackStack(null)
            .commit()
    }
}
