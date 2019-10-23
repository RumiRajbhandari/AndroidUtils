package com.evolve.rosiautils

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.view.Gravity
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.valdesekamdem.library.mdtoast.MDToast
import java.text.SimpleDateFormat
import java.util.*

const val TYPE_INFO = MDToast.TYPE_INFO
const val TYPE_ERROR = MDToast.TYPE_ERROR
const val TYPE_SUCCESS = MDToast.TYPE_SUCCESS
const val TYPE_WARNING = MDToast.TYPE_WARNING

fun checkNetworkAvailability(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || capabilities.hasTransport(
                    NetworkCapabilities.TRANSPORT_WIFI
                )
            ) {
                return true
            }
        }
        return false
    } else {
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }
}

fun loadImage(imageView: ImageView, url: String, imageLoadedListener: (success: Boolean) -> Unit) {
    val requestOptions = RequestOptions()
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .skipMemoryCache(false)
        .centerInside()
    val target = object : RequestListener<Drawable> {

        override fun onResourceReady(
            resource: Drawable?,
            model: Any?,
            target: Target<Drawable>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            imageView.setImageDrawable(resource)
            imageLoadedListener(true)
            return false
        }

        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable>?,
            isFirstResource: Boolean
        ): Boolean {
            imageLoadedListener(false)
            return false
        }
    }
    Glide.with(imageView.context)
        .load(url)
        .apply(requestOptions)
        .listener(target)
        .into(imageView)
}

fun getCurrentDateTime(): String {
    val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return df.format(Calendar.getInstance().time)
}

fun formatDate(inputDate: String, inputFormat: String, outputFormat: String): String {
    val outputDateFormat = SimpleDateFormat(outputFormat, Locale.getDefault())
    val inputDateFormat = SimpleDateFormat(inputFormat, Locale.getDefault())
    val date = inputDateFormat.parse(inputDate)
    return outputDateFormat.format(date)
}

fun Activity.showToast(message: String?, type: Int, duration: Int? = null) {
    if (isValidContext(this)) {
        val toast = MDToast.makeText(this, message, duration ?: MDToast.LENGTH_SHORT, type)
        toast.setGravity(Gravity.TOP, 0, 100)
        toast.show()
    }
}

fun Fragment.showToast(message: String?, type: Int, duration: Int? = null) {
    if (isValidContext(context)) {
        val toast = MDToast.makeText(context, message, duration ?: MDToast.LENGTH_SHORT, type)
        toast.setGravity(Gravity.TOP, 0, 100)
        toast.show()
    }
}

fun isValidContext(context: Context?): Boolean {
    if (context == null) {
        return false
    }
    if (context is Activity) {
        val activity: Activity = context
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            !activity.isDestroyed && !activity.isFinishing
        } else {
            !activity.isFinishing
        }
    }
    return true
}

fun initPieGraph(progress: Float, pieGraph: PieGraph) {

    val progressDifference = Math.abs(100 - progress) / 10
    var totalProgress = progress / 10
    if (progress > 100) {
        totalProgress = 100.0f
    }
    pieGraph.removeSlices()
    if (totalProgress == 0f) {
        val slice = PieSlice()
        slice.color = Color.parseColor("#FFF3F5F8")
        slice.value = progressDifference
        pieGraph.addSlice(slice)
        pieGraph.thickness = 50
    } else {
        var slice = PieSlice()
        slice.color = Color.parseColor("#FF1294F7")
        slice.value = totalProgress
        pieGraph.addSlice(slice)
        slice = PieSlice()
        slice.color = Color.parseColor("#FFF3F5F8")
        slice.value = progressDifference
        pieGraph.addSlice(slice)
        pieGraph.thickness = 50
    }
}
