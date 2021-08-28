package dev.armoury.android.widget.utils

import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.BindingAdapter
import dev.armoury.android.widget.MessageView
import dev.armoury.android.widget.R
import dev.armoury.android.widget.data.INVALID_VALUE
import dev.armoury.android.widget.data.MessageModel

@BindingAdapter("updateState")
fun AppCompatTextView.updateState(messageModel: MessageModel) {
    visibility = View.VISIBLE
    when (id) {
        R.id.text_title -> {
            when {
                messageModel.titleText != null -> text = messageModel.titleText
                messageModel.titleTextRes != INVALID_VALUE -> setText(messageModel.titleTextRes)
                else -> visibility = View.GONE
            }
        }
        R.id.text_description -> {
            when {
                messageModel.descriptionText != null -> text = messageModel.descriptionText
                messageModel.descriptionTextRes != INVALID_VALUE -> setText(messageModel.descriptionTextRes)
                else -> visibility = View.GONE
            }
        }
        else -> {
            throw Exception("This attribute is not applicable for this text view")
        }
    }
}

@BindingAdapter("clickCallbacks")
fun MessageView.setClickCallbacks(callbacks: MessageView.Callbacks?) {
    if (callbacks != null) setCallbacks(callbacks)
}