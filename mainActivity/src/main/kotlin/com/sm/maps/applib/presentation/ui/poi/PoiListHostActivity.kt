package com.sm.maps.applib.presentation.ui.poi

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sm.maps.applib.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PoiListHostActivity : AppCompatActivity(), PoiListFragment.OnPoiSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_poi_list)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PoiListFragment())
                .commit()
        }
    }

    override fun onPoiSelected(poiId: Int) {
        setResult(RESULT_OK, Intent().putExtra("pointid", poiId))
        finish()
    }
}
