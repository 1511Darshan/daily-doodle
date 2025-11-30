package com.example.dailydoodle.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ShareUtils {
    fun shareChain(context: Context, chainId: String, panels: List<com.example.dailydoodle.data.model.Panel>) {
        // TODO: Implement chain stitching and sharing
        // For now, share the first panel
        if (panels.isNotEmpty()) {
            shareImage(context, panels[0].imageUrl, "Check out this Daily Doodle Chain!")
        }
    }

    fun shareImage(context: Context, imageUrl: String, text: String) {
        // TODO: Download image and share
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "$text\n$imageUrl")
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share via"))
    }

    fun stitchPanels(panels: List<com.example.dailydoodle.data.model.Panel>): Bitmap? {
        // TODO: Implement panel stitching
        // This would download all panel images and stitch them vertically
        return null
    }
}
