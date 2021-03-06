package app.rootstock.adapters

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.Layout
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import app.rootstock.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.http.Url
import java.text.SimpleDateFormat
import java.util.*


@BindingAdapter("error")
fun bindingError(view: TextInputLayout, valid: Boolean) {
    if (view.editText?.text.isNullOrEmpty()) return
    if (valid) {
        if (!view.error.isNullOrEmpty()) view.error = null
    } else {
        view.error = "Invalid data"
    }
}

@BindingAdapter("loading")
fun bindingLoading(view: View, loading: Boolean) {
    if (!loading) {
        if (view.visibility == View.VISIBLE) view.visibility = View.GONE
        return
    }
    view.visibility = View.VISIBLE
}

@BindingAdapter("overlay_visible")
fun bindingLoadingOverlay(view: View, loading: Boolean) {
    if (!loading) {
        if (view.visibility == View.VISIBLE) view.visibility = View.INVISIBLE
        return
    }
    view.visibility = View.VISIBLE
}

@BindingAdapter("lastMessage")
fun bindingChannelLastMessage(textView: TextView, lastMessage: String?) {
    if (lastMessage == null) {
        textView.text = textView.context.getString(R.string.channels_no_last_message)
    } else textView.text = lastMessage
}

@BindingAdapter("drawableInt")
fun bindDrawableInt(imageView: ImageView, id: Int) {
    imageView.setImageResource(id)
}


@BindingAdapter("colorFilter")
fun bindColorFilter(imageView: ImageView, color: String?) {
    color ?: return
    try {
        val c = Color.parseColor(color)
        imageView.setColorFilter(c)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@BindingAdapter("backgroundColor")
fun bindColorBackground(view: View, color: String?) {
    color ?: return
    try {
        val c = Color.parseColor(color)
        view.setBackgroundColor(c)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@BindingAdapter("bindDate")
fun bindDate(text: TextView, date: Date?) {
    date ?: return
    try {
        val month = SimpleDateFormat("MMM dd")
        val dateText: String = month.format(date.time).toLowerCase(Locale.ROOT)
        text.text = dateText
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@BindingAdapter(value = ["imageUrl", "placeholder", "circle"], requireAll = false)
fun imageUrl(
    imageView: ImageView,
    imageUrl: String?,
    placeholder: Drawable?,
    circle: Boolean? = true
) {
    when (imageUrl) {
        null -> {
            Glide.with(imageView)
                .load(placeholder)
                .into(imageView)
        }
        else -> {
            val options = RequestOptions().placeholder(placeholder)
            if (circle == false)
                options.centerCrop() else options.circleCrop()
            Glide.with(imageView)
                .load(imageUrl)
                .error(placeholder)
                .apply(options)
                .into(imageView)
        }
    }
}