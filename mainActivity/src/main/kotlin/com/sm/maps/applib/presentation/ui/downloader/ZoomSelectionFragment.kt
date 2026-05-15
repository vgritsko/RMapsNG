package com.sm.maps.applib.presentation.ui.downloader

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.sm.maps.applib.R
import com.sm.maps.applib.presentation.viewmodel.MapDownloadViewModel
import com.sm.maps.applib.tileprovider.TileProviderInet
import com.sm.maps.applib.tileprovider.TileSource
import com.sm.maps.applib.utils.Ut
import org.andnav.osm.views.util.Util

class ZoomSelectionFragment : Fragment(R.layout.fragment_zoom_selection) {

    private val viewModel: MapDownloadViewModel by activityViewModels()
    private var tileSource: TileSource? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val state = viewModel.state.value
        try {
            tileSource = TileSource(requireContext(), state.mapId.ifEmpty { TileSource.MAPNIK })
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val src = tileSource ?: run {
            Toast.makeText(requireContext(), R.string.error_other, Toast.LENGTH_SHORT).show()
            return
        }

        populateZoomLevels(view, src, state.selectedArea?.let {
            intArrayOf(it.lat0, it.lon0, it.lat1, it.lon1)
        } ?: intArrayOf(0, 0, 0, 0))

        view.findViewById<EditText>(R.id.name).setText(state.fileName)
        view.findViewById<CheckBox>(R.id.overwritefile).isChecked = state.overwriteFile
        view.findViewById<CheckBox>(R.id.overwritetiles).isChecked = state.overwriteTiles
        view.findViewById<CheckBox>(R.id.online_cache).isChecked = state.isOnlineCache
        updateOnlineCacheVisibility(view, state.isOnlineCache)

        view.findViewById<CheckBox>(R.id.online_cache).setOnCheckedChangeListener { _, isChecked ->
            updateOnlineCacheVisibility(view, isChecked)
        }

        view.findViewById<Button>(R.id.back).setOnClickListener {
            viewModel.backToAreaSelection()
            requireActivity().supportFragmentManager.popBackStack()
        }

        view.findViewById<Button>(R.id.start_download).setOnClickListener {
            doStartDownload(view, src)
        }
    }

    override fun onDestroyView() {
        tileSource?.Free()
        tileSource = null
        super.onDestroyView()
    }

    private fun populateZoomLevels(view: View, src: TileSource, coordArr: IntArray) {
        val ll1 = view.findViewById<LinearLayout>(R.id.LayerArea1)
        val ll2 = view.findViewById<LinearLayout>(R.id.LayerArea2)
        ll1.removeAllViews()
        ll2.removeAllViews()

        val tileLength = (src.tileProvider as? TileProviderInet)?.tileLength ?: 0.0
        val halfCount = ((src.ZOOM_MAXLEVEL - src.ZOOM_MINLEVEL + 1) / 2.0 + 0.5).toInt()

        for (i in src.ZOOM_MINLEVEL..src.ZOOM_MAXLEVEL) {
            val c0 = Util.getMapTileFromCoordinates(coordArr[0], coordArr[1], i, null, src.PROJECTION)
            val c1 = Util.getMapTileFromCoordinates(coordArr[2], coordArr[3], i, null, src.PROJECTION)
            val yMin = minOf(c0[0], c1[0])
            val yMax = maxOf(c0[0], c1[0])
            val xMin = minOf(c0[1], c1[1])
            val xMax = maxOf(c0[1], c1[1])
            val tileCnt = (yMax - yMin + 1) * (xMax - xMin + 1)

            val cb = CheckBox(requireContext()).apply {
                tag = "Layer$i"
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f)
                text = "Zoom ${i + 1}\n$tileCnt tiles, ~${Ut.formatSize(tileCnt * tileLength)}"
            }

            if (i - src.ZOOM_MINLEVEL + 1 > halfCount) ll2.addView(cb) else ll1.addView(cb)
        }
    }

    private fun collectZooms(view: View): IntArray {
        val src = tileSource ?: return intArrayOf()
        val ll = view.findViewById<LinearLayout>(R.id.LayerArea)
        val tempArr = IntArray(src.ZOOM_MAXLEVEL - src.ZOOM_MINLEVEL + 1)
        var count = 0
        for (i in src.ZOOM_MINLEVEL..src.ZOOM_MAXLEVEL) {
            val cb = ll.findViewWithTag<CheckBox>("Layer$i")
            if (cb != null && cb.isChecked) {
                tempArr[count++] = i
            }
        }
        return tempArr.copyOf(count)
    }

    private fun doStartDownload(view: View, src: TileSource) {
        val zooms = collectZooms(view)
        if (zooms.isEmpty()) {
            Toast.makeText(requireContext(), R.string.select_zoom, Toast.LENGTH_LONG).show()
            return
        }

        val isOnlineCache = view.findViewById<CheckBox>(R.id.online_cache).isChecked
        var fileName = view.findViewById<EditText>(R.id.name).text.toString()

        if (!isOnlineCache) {
            if (fileName.isEmpty()) {
                Toast.makeText(requireContext(), "Invalid file name", Toast.LENGTH_LONG).show()
                return
            }
            val folder = Ut.getRMapsMapsDir(requireContext())
            folder?.listFiles()?.forEach { file ->
                if (file.name.equals("$fileName.sqlitedb", ignoreCase = true)) {
                    fileName = file.name.substring(0, file.name.length - 9)
                    return@forEach
                }
            }
        }

        val overwriteFile = view.findViewById<CheckBox>(R.id.overwritefile).isChecked
        val overwriteTiles = view.findViewById<CheckBox>(R.id.overwritetiles).isChecked

        viewModel.startDownload(zooms, fileName, isOnlineCache, overwriteFile, overwriteTiles)

        val state = viewModel.state.value
        val serviceIntent = Intent("com.sm.maps.mapdownloader").apply {
            putExtra("ZOOM", zooms)
            putExtra("COORD", state.selectedArea?.let { intArrayOf(it.lat0, it.lon0, it.lat1, it.lon1) } ?: intArrayOf())
            putExtra("MAPID", src.ID)
            putExtra("ZOOMCUR", state.zoomLevel)
            putExtra("overwritefile", overwriteFile)
            putExtra("overwritetiles", overwriteTiles)
            putExtra("online_cache", isOnlineCache)
            putExtra("OFFLINEMAPNAME", fileName)
            setPackage("com.sm.maps")
        }
        requireActivity().startService(serviceIntent)

        view.findViewById<Button>(R.id.start_download).visibility = View.GONE
        (requireActivity() as DownloaderHostActivity).navigateToProgress()
    }

    private fun updateOnlineCacheVisibility(view: View, isOnlineCache: Boolean) {
        val vis = if (isOnlineCache) View.GONE else View.VISIBLE
        view.findViewById<View>(R.id.name).visibility = vis
        view.findViewById<View>(R.id.overwritefile).visibility = vis
        view.findViewById<TextView>(R.id.fileNameTitle).visibility = vis
    }
}
