package com.sm.maps.applib.presentation.ui.downloader

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Paint
import android.net.Uri
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.RemoteException
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.sm.maps.applib.MainActivity
import com.sm.maps.applib.R
import com.sm.maps.applib.downloader.DownloadedAreaOverlay
import com.sm.maps.applib.downloader.IDownloaderCallback
import com.sm.maps.applib.downloader.IRemoteService
import com.sm.maps.applib.presentation.viewmodel.MapDownloadViewModel
import com.sm.maps.applib.tileprovider.TileSource
import com.sm.maps.applib.utils.Ut
import com.sm.maps.applib.view.MapView
import com.sm.maps.applib.view.TileViewOverlay
import org.andnav.osm.util.GeoPoint
import java.io.File

class DownloadProgressFragment : Fragment(R.layout.fragment_download_progress) {

    private val viewModel: MapDownloadViewModel by activityViewModels()

    private lateinit var map: MapView
    private lateinit var progress: ProgressBar
    private lateinit var textTileCnt: TextView
    private lateinit var textError: TextView
    private lateinit var textTime: TextView
    private lateinit var btnPause: Button
    private lateinit var btnOpen: Button
    private lateinit var downloadedAreaOverlay: DownloadedAreaOverlay

    private var tileSource: TileSource? = null
    private var remoteService: IRemoteService? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            remoteService = IRemoteService.Stub.asInterface(service)
            try {
                remoteService?.registerCallback(downloadCallback)
            } catch (e: RemoteException) {
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            remoteService = null
        }
    }

    private val downloadCallback = object : IDownloaderCallback.Stub() {
        override fun downloadDone() {
            mainHandler.post { onDownloadFinished() }
        }

        override fun downloadStart(tileCnt: Int, startTime: Long, fileName: String, mapID: String, zoom: Int, lat0: Int, lon0: Int, lat1: Int, lon1: Int) {
            mainHandler.post {
                if (!isAdded) return@post
                viewModel.onDownloadStarted(tileCnt, startTime)
                downloadedAreaOverlay.Init(requireContext(), lat0, lon0, lat1, lon1)
                progress.max = tileCnt
                textTileCnt.text = tileCnt.toString()
                textTime.text = "00:00"
                setupTileSource(mapID, zoom, lat0 + (lat1 - lat0) / 2, lon0 + (lon1 - lon0) / 2)
                map.invalidate()
            }
        }

        override fun downloadTileDone(tileCnt: Int, errorCnt: Int, x: Int, y: Int, z: Int) {
            mainHandler.post {
                if (!isAdded) return@post
                viewModel.updateProgress(tileCnt, errorCnt)
                downloadedAreaOverlay.setLastDowloadedTile(x, y, z, map.tileView)
                val state = viewModel.state.value
                progress.progress = tileCnt
                textTileCnt.text = "$tileCnt/${state.tileCntTotal}"
                if (errorCnt > 0) textError.text = "ERRORS: $errorCnt"
                val elapsed = System.currentTimeMillis() - state.startTime
                textTime.text = if (elapsed > 5_000 && tileCnt > 0)
                    "${Ut.formatTime(elapsed)} / ${Ut.formatTime((elapsed / (tileCnt.toDouble() / state.tileCntTotal)).toLong())}"
                else
                    Ut.formatTime(elapsed)
                map.invalidate()
            }
        }
    }

    override fun onViewCreated(view: android.view.View, savedInstanceState: android.os.Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        map = view.findViewById(R.id.map)
        progress = view.findViewById(R.id.progress)
        textTileCnt = view.findViewById(R.id.textTileCnt)
        textError = view.findViewById(R.id.textError)
        textTime = view.findViewById(R.id.textTime)
        btnPause = view.findViewById(R.id.pause)
        btnOpen = view.findViewById(R.id.open)

        textError.setTextColor(textError.linkTextColors)
        textError.paintFlags = textError.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        downloadedAreaOverlay = DownloadedAreaOverlay()
        map.overlays.add(downloadedAreaOverlay)

        val state = viewModel.state.value
        map.getController().setCenter(GeoPoint(state.centerLat, state.centerLon))
        map.isLongClickable = false

        textError.setOnClickListener {
            val file = File("${Ut.getRMapsMainDir(requireContext(), "").absolutePath}/cache/mapdownloaderlog.txt")
            startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(Uri.fromFile(file), "text/plain"))
        }

        btnPause.setOnClickListener {
            val stopIntent = Intent("com.sm.maps.mapdownloader").apply { setPackage("com.sm.maps") }
            requireActivity().stopService(stopIntent)
        }

        btnOpen.setOnClickListener { openMap() }
    }

    override fun onResume() {
        super.onResume()
        val serviceIntent = Intent(IRemoteService::class.java.name).apply { setPackage("com.sm.maps") }
        requireActivity().bindService(serviceIntent, serviceConnection, 0)

        val state = viewModel.state.value
        try {
            tileSource = TileSource(requireContext(), state.mapId.ifEmpty { TileSource.MAPNIK })
        } catch (e: Exception) {
        }
        map.setTileSource(tileSource)
        map.getController().setZoom(state.zoomLevel)
    }

    override fun onPause() {
        requireActivity().unbindService(serviceConnection)
        tileSource?.Free()
        tileSource = null
        super.onPause()
    }

    override fun onDestroyView() {
        for (overlay: TileViewOverlay in map.overlays) overlay.Free()
        super.onDestroyView()
    }

    private fun setupTileSource(mapId: String, zoom: Int, lat: Int, lon: Int) {
        if (tileSource?.ID != mapId) {
            tileSource?.Free()
            try {
                tileSource = TileSource(requireContext(), mapId)
            } catch (e: Exception) {
            }
            map.setTileSource(tileSource)
        }
        map.getController().setZoom(zoom)
        map.getController().setCenter(GeoPoint(lat, lon))
    }

    private fun onDownloadFinished() {
        if (!isAdded) return
        viewModel.onDownloadDone()
        btnOpen.visibility = View.VISIBLE
        progress.visibility = View.GONE
        btnPause.visibility = View.GONE
        downloadedAreaOverlay.downloadDone()
        map.invalidate()

        val state = viewModel.state.value
        textTileCnt.text = state.tileCntTotal.toString()
        textTime.text = Ut.formatTime(System.currentTimeMillis() - state.startTime)
    }

    private fun openMap() {
        val state = viewModel.state.value
        val mapName = if (state.isOnlineCache) {
            tileSource?.ID ?: state.mapId
        } else {
            "usermap_${Ut.FileName2ID("${state.fileName}.sqlitedb")}"
        }
        startActivity(
            Intent(requireContext(), MainActivity::class.java)
                .setAction("SHOW_MAP_ID")
                .putExtra("MapName", mapName)
                .setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        )
        requireActivity().finish()
    }
}
