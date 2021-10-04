package com.rsschool.catsapp.ui

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.rsschool.catsapp.R
import com.rsschool.catsapp.databinding.FragmentImageDetailBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.net.URL
import java.nio.ByteBuffer

class ImageDetailsFragment : Fragment(R.layout.fragment_image_detail) {
    private val viewModel by viewModels<ImageDetailViewModel>()
    private var binding: FragmentImageDetailBinding? = null
    private var permissionLauncher: ActivityResultLauncher<String?>? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                saveImage()
            } else {
                Toast.makeText(
                    context,
                    getString(R.string.permission_not_granted),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentImageDetailBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        binding?.apply {
            displayImage()
            textDescription.text = "URL: ${viewModel.image?.url}"
        }
    }

    private fun saveImage() {
        CoroutineScope(Dispatchers.IO).launch {
            viewModel.image?.let {
                writeImageToFile(
                    Glide.with(this@ImageDetailsFragment)
                        .asDrawable()
                        .load(it.url)
                        .submit()
                        .get(),
                    it.url
                )
            }
        }
    }

    private fun displayImage() {
        binding?.catImage?.let {
            Glide.with(this@ImageDetailsFragment).load(viewModel.image?.url)
                .error(R.drawable.ic_outline_sentiment_very_dissatisfied_24)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(it)
        }
    }

    private suspend fun writeImageToFile(drawable: Drawable?, url: String) {
        val path = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        var bytes: ByteArray? = null
        when (drawable) {
            is GifDrawable -> {
                val byteBuffer = drawable.buffer
                bytes = ByteArray(byteBuffer.capacity())
                (byteBuffer.duplicate().clear() as ByteBuffer).get(bytes)
            }
            is BitmapDrawable -> {
                val bos = ByteArrayOutputStream()
                val bitmap = drawable.bitmap
                bitmap?.compress(Bitmap.CompressFormat.PNG, 0, bos)
                bytes = bos.toByteArray()
            }
            else -> {
                Toast.makeText(
                    context,
                    getString(R.string.unsupported_file_type),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        // Save the image.
        val contentUri: Uri? = context?.contentResolver?.insert(path, getImageDetails(url))
        val outputStream: FileOutputStream? = null
        try {
            contentUri?.let { uri ->
                if (bytes == null) context?.contentResolver?.delete(uri, null, null)
                val outputStream: OutputStream? = context?.contentResolver?.openOutputStream(uri)
                bytes?.let { outputStream?.write(it, 0, it.size) }
            } ?: throw NullPointerException("Content is null")
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    getString(R.string.error_saving),
                    Toast.LENGTH_SHORT
                ).show()
                e.printStackTrace()
            }
        } finally {
            withContext(Dispatchers.IO) {
                outputStream?.flush()
                outputStream?.close()
            }
        }
    }

    private fun getImageDetails(url: String): ContentValues? {
        val uri = URL(url)
        return try {
            val f = File(uri.path)
            val fileName = f.nameWithoutExtension
            val extension = f.extension
            val file = getFileWithName(fileName, extension)
            ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, file.absolutePath)
                put(MediaStore.Images.Media.MIME_TYPE, "image/$extension")
                put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis())
            }
        } catch (e: IOException) {
            null
        }
    }

    private fun getFileWithName(fileName: String, extension: String): File {
        var file = File("$fileName.$extension")
        var num = 1
        while (file.exists()) {
            file = File("$fileName($num).$extension")
            num++
        }
        return file
    }

    // MENU STUFF
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.save -> {
            val permission = WRITE_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(
                    this@ImageDetailsFragment.requireContext(),
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                saveImage()
            } else {
                permissionLauncher?.launch(permission)
            }
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}
