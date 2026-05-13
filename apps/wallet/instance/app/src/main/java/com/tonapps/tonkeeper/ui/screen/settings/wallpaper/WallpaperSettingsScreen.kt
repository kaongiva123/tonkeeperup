package com.tonapps.tonkeeper.ui.screen.settings.wallpaper

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import com.tonapps.tonkeeper.manager.wallpaper.WallpaperManager
import com.tonapps.tonkeeperx.R
import uikit.base.BaseFragment

class WallpaperSettingsScreen : BaseFragment(R.layout.fragment_wallpaper_settings), BaseFragment.SwipeBack {

    override val fragmentName: String = "WallpaperSettingsScreen"

    private val wallpaperManager by lazy { WallpaperManager(requireContext()) }

    private var previewImage: ImageView? = null
    private var dimOverlay: View? = null
    private var dimLabel: TextView? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                wallpaperManager.setWallpaper(uri)
                updatePreview()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val headerView = view.findViewById<uikit.widget.HeaderView>(R.id.header)
        headerView.setIcon(com.tonapps.uikit.icon.UIKitIcon.ic_chevron_left_16)
        headerView.doOnCloseClick = { finish() }
        headerView.title = "Wallpaper"

        previewImage = view.findViewById(R.id.wallpaper_preview)
        dimOverlay = view.findViewById(R.id.dim_overlay)
        dimLabel = view.findViewById(R.id.dim_label)

        val dimSeekBar = view.findViewById<SeekBar>(R.id.dim_seekbar)
        dimSeekBar.max = 90
        dimSeekBar.progress = (wallpaperManager.dimLevel * 100).toInt()
        updateDimLabel(dimSeekBar.progress)

        dimSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                wallpaperManager.dimLevel = progress / 100f
                updateDimLabel(progress)
                updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val pickButton = view.findViewById<View>(R.id.btn_pick_wallpaper)
        pickButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            pickImageLauncher.launch(intent)
        }

        val removeButton = view.findViewById<View>(R.id.btn_remove_wallpaper)
        removeButton.setOnClickListener {
            wallpaperManager.removeWallpaper()
            updatePreview()
        }

        updatePreview()
    }

    private fun updateDimLabel(progress: Int) {
        dimLabel?.text = "Dim: $progress%"
    }

    private fun updatePreview() {
        val bitmap = wallpaperManager.getWallpaperBitmap()
        if (bitmap != null) {
            previewImage?.setImageBitmap(bitmap)
            previewImage?.visibility = View.VISIBLE
            dimOverlay?.visibility = View.VISIBLE
            val dim = wallpaperManager.dimLevel
            dimOverlay?.setBackgroundColor(Color.argb((255 * dim).toInt(), 0, 0, 0))
        } else {
            previewImage?.visibility = View.GONE
            dimOverlay?.visibility = View.GONE
        }
    }

    companion object {
        fun newInstance() = WallpaperSettingsScreen()
    }
}
