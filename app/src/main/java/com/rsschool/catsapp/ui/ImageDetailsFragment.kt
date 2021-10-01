package com.rsschool.catsapp.ui

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.rsschool.catsapp.R
import com.rsschool.catsapp.databinding.FragmentImageDetailBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.os.Environment.DIRECTORY_PICTURES
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.rsschool.catsapp.model.Cat
import java.net.URL
import android.graphics.drawable.BitmapDrawable
import android.view.*
import androidx.navigation.ui.onNavDestinationSelected
import com.bumptech.glide.load.resource.gif.GifDrawable
import kotlinx.coroutines.cancel
import java.io.*
import java.nio.ByteBuffer


class ImageDetailsFragment : Fragment(R.layout.fragment_image_detail) {
    private val args by navArgs<ImageDetailsFragmentArgs>()
    private val image by lazy { args.image }
    private var binding: FragmentImageDetailBinding? = null
    private var permissionLauncher: ActivityResultLauncher<String?>? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                saveImage(image)
            } else {
                Toast.makeText(
                    context,
                    "Permission to access the storage is not granted, please review the permissions",
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
        return binding!!.root //todo
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        binding?.apply {
            Glide.with(this@ImageDetailsFragment).load(image?.url)
                .error(R.drawable.ic_baseline_error_outline_24)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
/*                .listener(object: RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        progressBar.isVisible = false
                        return false
                    }
                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        progressBar.isVisible = false
                        textDescription.isVisible = true
                        catImage.isVisible = true
                        return true
                    }
                })*/
                .into(catImage)

            textDescription.text = image?.id.toString()
        }
    }

    //MENU STUFF
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
                saveImage(image)
            } else {
                permissionLauncher?.launch(permission)
            }
            true
        } else -> {
            super.onOptionsItemSelected(item)
        }
    }


    private fun saveImage(image: Cat?) {
        CoroutineScope(Dispatchers.IO).launch {
            image?.let {
                writeImageToFile(
                    Glide.with(this@ImageDetailsFragment)
                        .asDrawable()
                        .load(it.url)
                        .placeholder(android.R.drawable.progress_indeterminate_horizontal) // need placeholder to avoid issue like glide annotations
                        .error(android.R.drawable.stat_notify_error) // need error to avoid issue like glide annotations
                        .submit()
                        .get(),
                    it.url
                )
            }
        }
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
                    //toast unsupported filetype
                }
            }
            bytes?.let { output.write(it, 0, it.size) }
            output.flush()
            output.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    private fun getFileName(url: String): String? {
        val uri = URL(url)
        val path = this.context?.getExternalFilesDir(DIRECTORY_PICTURES).toString() + File.separator
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
}


/*    private fun getFileName(url : String) : String{
        val uri = URL(url)
        return File(uri.getPath()).getName()
        //val filename = File(uri.getPath()).getName()
    }*/
