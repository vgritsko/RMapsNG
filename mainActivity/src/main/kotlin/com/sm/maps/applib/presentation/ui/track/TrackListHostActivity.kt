package com.sm.maps.applib.presentation.ui.track

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sm.maps.applib.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrackListHostActivity : AppCompatActivity(), TrackListFragment.OnTrackSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_list)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TrackListFragment())
                .commit()
        }
    }

    override fun onTrackSelected(trackId: Int) {
        setResult(RESULT_OK, Intent().putExtra("trackid", trackId))
        finish()
    }
}
