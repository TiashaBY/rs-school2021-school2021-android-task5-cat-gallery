package com.rsschool.catsapp.ui

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
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
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
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
                    "Permission to access the storage is not granted, " +
                            "please review the permissions in app settings",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentImageDetailBinding.inflate(inflater, container, false)
        return binding!!.root // todo
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        binding?.apply {
            displayImage()
            textDescription.text = viewModel.image?.id.toString()
        }
    }

    private fun saveImage() {
        CoroutineScope(Dispatchers.IO).launch {
            viewModel.image?.let {
                writeImageToFile(
                    Glide.with(this@ImageDetailsFragment)
                        .asDrawable()
                        .load(it.url)
                        .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                        .error(android.R.drawable.stat_notify_error)
                        .submit()
                        .get(),
                    it.url
                )
            }
        }
    }

    private fun displayImage() {
        Glide.with(this@ImageDetailsFragment).load(viewModel.image?.url)
            .error(R.drawable.ic_outline_sentiment_very_dissatisfied_24)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .into(binding?.catImage!!)
    }

    private fun writeImageToFile(drawable: Drawable?, url: String) {
        var file: File? = null
        return try {
            getFileName(url)?.let { file = File(it) }
            file?.createNewFile()
            val output = FileOutputStream(file)
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
                        "Unsupported file type. File is not saved",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            bytes?.let { output.write(it, 0, it.size) }
            output.flush()
            output.close()
        } catch (e: IOException) {
            Toast.makeText(
                context,
                "An error occurred while saving the file",
                Toast.LENGTH_SHORT
            ).show()
            e.printStackTrace()
        }
    }

    private fun getFileName(url: String): String? {
        val uri = URL(url)
        val path = this.context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() + File.separator
        return try {
            val f = File(uri.path)
            val fileName = f.nameWithoutExtension
            val extension = f.extension
            var file = File("$path$fileName.$extension")
            var num = 1
            while (file.exists()) {
                file = File("$path$fileName($num).$extension")
                num++
            }
            file.absolutePath
        } catch (e: IOException) {
            null
        }
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
}
