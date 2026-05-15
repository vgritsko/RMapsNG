package com.sm.maps.applib.presentation.ui.downloader

import android.database.Cursor
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.sm.maps.applib.R
import com.sm.maps.applib.downloader.AreaSelectorOverlay
import com.sm.maps.applib.kml.PoiManager
import com.sm.maps.applib.kml.XMLparser.PredefMapsParser
import com.sm.maps.applib.presentation.viewmodel.AreaCoords
import com.sm.maps.applib.presentation.viewmodel.MapDownloadViewModel
import com.sm.maps.applib.tileprovider.TileSource
import com.sm.maps.applib.tileprovider.TileSourceBase
import com.sm.maps.applib.utils.RException
import com.sm.maps.applib.view.IMoveListener
import com.sm.maps.applib.view.MapView
import com.sm.maps.applib.view.TileViewOverlay
import org.andnav.osm.util.GeoPoint
import javax.xml.parsers.SAXParserFactory

class AreaSelectorFragment : Fragment(R.layout.fragment_area_selector) {

    private val viewModel: MapDownloadViewModel by activityViewModels()

    private lateinit var map: MapView
    private lateinit var areaSelectorOverlay: AreaSelectorOverlay
    private var tileSource: TileSource? = null

    companion object {
        private const val ARG_MAP_ID = "mapId"
        private const val ARG_LAT = "lat"
        private const val ARG_LON = "lon"
        private const val ARG_ZOOM = "zoom"

        fun newInstance(mapId: String, lat: Int, lon: Int, zoom: Int) = AreaSelectorFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_MAP_ID, mapId)
                putInt(ARG_LAT, lat)
                putInt(ARG_LON, lon)
                putInt(ARG_ZOOM, zoom)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapId = arguments?.getString(ARG_MAP_ID) ?: ""
        val lat = arguments?.getInt(ARG_LAT, 0) ?: 0
        val lon = arguments?.getInt(ARG_LON, 0) ?: 0
        val zoom = arguments?.getInt(ARG_ZOOM, 0) ?: 0

        viewModel.initDownload(mapId, "", lat, lon, zoom)

        map = view.findViewById(R.id.map)
        map.setMoveListener(object : IMoveListener {
            override fun onMoveDetected() {}
            override fun onZoomDetected() { updateTitle() }
            override fun onCenterDetected() {}
        })

        val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
        map.displayZoomControls(pref.getString("pref_zoomctrl", "1")?.toIntOrNull() ?: 1)
        map.getController().setCenter(GeoPoint(lat, lon))
        map.isLongClickable = false

        areaSelectorOverlay = AreaSelectorOverlay()
        map.overlays.add(areaSelectorOverlay)

        registerForContextMenu(view.findViewById(R.id.maps))
        view.findViewById<Button>(R.id.maps).setOnClickListener { it.showContextMenu() }
        view.findViewById<Button>(R.id.clear).setOnClickListener {
            areaSelectorOverlay.clearArea(map.tileView)
        }
        view.findViewById<Button>(R.id.next).setOnClickListener { doNext() }
    }

    override fun onResume() {
        super.onResume()
        val state = viewModel.state.value
        if (tileSource != null) tileSource!!.Free()

        try {
            tileSource = TileSource(requireContext(), state.mapId.ifEmpty { TileSource.MAPNIK })
            if (tileSource!!.MAP_TYPE != TileSourceBase.PREDEF_ONLINE && tileSource!!.MAP_TYPE != TileSourceBase.MIXMAP_CUSTOM) {
                tileSource!!.Free()
                tileSource = TileSource(requireContext(), TileSource.MAPNIK)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        map.setTileSource(tileSource)
        map.getController().setZoom(state.zoomLevel)
        map.getController().setCenter(GeoPoint(state.centerLat, state.centerLon))
        areaSelectorOverlay.Init(requireContext(), map.tileView)
        updateTitle()
    }

    override fun onDestroyView() {
        for (overlay: TileViewOverlay in map.overlays) overlay.Free()
        map.setMoveListener(null)
        tileSource?.Free()
        tileSource = null
        super.onDestroyView()
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        if (v.id == R.id.maps) {
            menu.clear()
            val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

            val poiManager = PoiManager(requireContext())
            val c: Cursor? = poiManager.geoDatabase.mixedMaps
            c?.use {
                if (it.moveToFirst()) {
                    do {
                        if (pref.getBoolean("PREF_MIXMAPS_" + it.getInt(0) + "_enabled", true) && it.getInt(2) == 2) {
                            val item = menu.add(it.getString(1))
                            item.titleCondensed = "mixmap_" + it.getInt(0)
                        }
                    } while (it.moveToNext())
                }
            }
            poiManager.FreeDatabases()

            try {
                val parser = SAXParserFactory.newInstance().newSAXParser()
                val inStream = resources.openRawResource(R.raw.predefmaps)
                parser.parse(inStream, PredefMapsParser(menu, pref))
                inStream.close()

                val inStream2 = resources.openRawResource(R.raw.predefmaps)
                parser.parse(inStream2, PredefMapsParser(menu, pref, true, tileSource?.PROJECTION ?: 1))
                inStream2.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        super.onCreateContextMenu(menu, v, menuInfo)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val mapId = item.titleCondensed?.toString() ?: return false
        tileSource?.Free()
        try {
            tileSource = TileSource(requireContext(), mapId)
        } catch (e: RException) {
        }
        map.setTileSource(tileSource)
        updateTitle()
        return true
    }

    private fun doNext() {
        val coords = areaSelectorOverlay.coordArr
        viewModel.proceedToZoomSelection(AreaCoords(coords[0], coords[1], coords[2], coords[3]))
        (requireActivity() as DownloaderHostActivity).navigateToZoomSelection()
    }

    private fun updateTitle() {
        try {
            view?.findViewById<TextView>(R.id.left_text)?.text = map.tileSource?.NAME ?: ""
            view?.findViewById<TextView>(R.id.gps_text)?.text = ""
            val zoom = map.zoomLevel
            val ts = map.touchScale
            view?.findViewById<TextView>(R.id.right_text)?.text = when {
                ts == 1.0 -> "${1 + zoom}"
                ts < 1.0 -> "${1 + zoom}-"
                else -> "${1 + zoom}+"
            }
        } catch (e: Exception) {
        }
    }
}
