package dev.armoury.android.widget

import android.animation.Animator
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.IntDef
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.databinding.DataBindingUtil
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import dev.armoury.android.widget.data.INVALID_VALUE
import dev.armoury.android.widget.data.MessageModel
import dev.armoury.android.widget.databinding.ViewMessageBinding
import dev.armoury.android.widget.utils.SimpleAnimatorListener

// TODO Show loading indicator
class MessageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayoutCompat(context, attrs, defStyleAttr) {

    private var binding: ViewMessageBinding

    private var externalCallbacks : Callbacks? = null

    private var animationRepeatCount : Int = LottieDrawable.INFINITE

    private var messageModel: MessageModel? = null

    //    Images
    @DrawableRes
    private var normalImageRes: Int = INVALID_VALUE
    @DrawableRes
    private var loadingImageRes: Int = INVALID_VALUE
    @DrawableRes
    private var errorImageRes: Int = INVALID_VALUE

    //    Texts
    //    Titles
    private var titleNormal: CharSequence? = null
    private var titleLoading: CharSequence? = null
    private var titleError: CharSequence? = null
    //    Descriptions
    private var descNormal: CharSequence? = null
    private var descLoading: CharSequence? = null
    private var descError: CharSequence? = null
    //    Lottie File Name
    private var lottieFileName: String? = null
    //    Button Text
    private var buttonText: String? = null

    //    Colors
    //    Titles
    private var titleNormalColor = 0xFF_F0_F0_F0.toInt()
    private var titleLoadingColor = titleNormalColor
    private var titleErrorColor = titleNormalColor
    //    Descriptions
    private var descNormalColor = 0xFF_E9_E9_E9.toInt()
    private var descLoadingColor = descNormalColor
    private var descErrorColor = descNormalColor
    //    Button
    private var buttonTextColor = 0xFF_FF_FF_FF.toInt()


    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.view_message,
            this,
            true
        )
        visibility = View.GONE

        setAttributes(attrs)

        binding.messageView = this
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        binding.animation.cancelAnimation()
    }

    fun updateState(messageModel: MessageModel?) {
        this.messageModel = messageModel
        messageModel?.let {
            var visibility = View.VISIBLE
            var loadingIndicatorVisibility = View.GONE
            when (messageModel.state) {
                States.NORMAL -> {
                    updateTextColors(titleNormalColor, descNormalColor)
                    updateIcon(messageModel.imageRes, normalImageRes)
                }
                States.LOADING -> {
                    updateTextColors(titleLoadingColor, descLoadingColor)
                    updateIcon(messageModel.imageRes, loadingImageRes)
                    if (loadingImageRes == INVALID_VALUE && lottieFileName.isNullOrEmpty()){
                        loadingIndicatorVisibility = View.VISIBLE
                    }
                }
                States.ERROR -> {
                    updateTextColors(titleErrorColor, descErrorColor)
                    updateIcon(messageModel.imageRes, errorImageRes)
                }
                States.HIDE -> visibility = View.GONE
            }
            binding.textTitle.updateState(messageModel.titleText, messageModel.titleTextRes)
            binding.textDescription.updateState(
                messageModel.descriptionText,
                messageModel.descriptionTextRes
            )
            binding.button.updateState(messageModel.buttonText, messageModel.buttonTextRes)
            binding.animation.updateState(messageModel.state, lottieFileName)
            this.visibility = visibility
            binding.progress.visibility = loadingIndicatorVisibility
        }
    }

    fun setCallbacks(externalCallbacks : Callbacks) {
        this.externalCallbacks = externalCallbacks
    }

    fun onClick(view : View) {
        when (view.id) {
            R.id.button -> externalCallbacks?.onButtonClicked(messageModel = messageModel)
        }
    }

    private fun updateTextColors(titleColor: Int, descriptionColor: Int) {
        binding.textTitle.setTextColor(titleColor)
        binding.textDescription.setTextColor(descriptionColor)
    }

    private fun updateIcon(@DrawableRes imageRes: Int, @DrawableRes defaultRes: Int) {
        binding.image.updateState(if (imageRes != INVALID_VALUE) imageRes else defaultRes)
    }

    private fun LottieAnimationView.updateState(@State state: Int, lottieFileName: String?) {
        when {
            state == States.LOADING &&
                    lottieFileName != null -> {
                visibility = View.VISIBLE
                setAnimation(lottieFileName)
                repeatCount = animationRepeatCount
                binding.animation.addAnimatorListener(object : SimpleAnimatorListener() {
                    override fun onAnimationEnd(animation: Animator) {
                        externalCallbacks?.onAnimationEnd()
                    }
                })
                binding.animation.addAnimatorUpdateListener { valueAnimator ->
                    externalCallbacks?.onAnimationProgress(valueAnimator.animatedValue as Float)
                }
                playAnimation()
            }
            else -> visibility = View.GONE
        }
    }

    private fun AppCompatImageView.updateState(@DrawableRes imageRes: Int) {
        when (imageRes) {
            INVALID_VALUE -> visibility = View.GONE
            else -> {
                visibility = View.VISIBLE
                setImageResource(imageRes)
            }
        }
    }

    private fun TextView.updateState(text: CharSequence? = null, textRes: Int = INVALID_VALUE) {
        var visibility = View.VISIBLE
        when {
            text != null -> this.text = text
            textRes != INVALID_VALUE -> setText(textRes)
            else -> visibility = View.GONE
        }
        this.visibility = visibility
    }

    private fun setAttributes(attrs: AttributeSet?) {
        attrs?.let {
            val a: TypedArray = context.obtainStyledAttributes(it, R.styleable.MessageView)
            val indexCount = a.indexCount
            @State var state : Int? = null

            for (i in 0 until indexCount) {
                when (val attr = a.getIndex(i)) {
                    R.styleable.MessageView_mv_animation_file -> lottieFileName = a.getString(attr)
                    // Texts
                    // Titles
                    R.styleable.MessageView_mv_title_normal -> titleNormal = a.getString(attr)
                    R.styleable.MessageView_mv_title_loading -> titleLoading = a.getString(attr)
                    R.styleable.MessageView_mv_title_error -> titleError = a.getString(attr)
                    // Descriptions
                    R.styleable.MessageView_mv_desc_normal -> descNormal = a.getString(attr)
                    R.styleable.MessageView_mv_desc_loading -> descLoading = a.getString(attr)
                    R.styleable.MessageView_mv_desc_error -> descError = a.getString(attr)
                    // Button
                    R.styleable.MessageView_mv_button_text -> buttonText = a.getString(attr)
                    // Colors
                    // Titles' Colors
                    R.styleable.MessageView_mv_title_color_normal ->
                        titleNormalColor = a.getColor(attr, titleNormalColor)
                    R.styleable.MessageView_mv_title_color_loading ->
                        titleLoadingColor = a.getColor(attr, titleLoadingColor)
                    R.styleable.MessageView_mv_title_color_error ->
                        titleErrorColor = a.getColor(attr, titleErrorColor)
                    // Descriptions' Colors
                    R.styleable.MessageView_mv_desc_color_normal ->
                        descNormalColor = a.getColor(attr, descNormalColor)
                    R.styleable.MessageView_mv_desc_color_loading ->
                        descLoadingColor = a.getColor(attr, descLoadingColor)
                    R.styleable.MessageView_mv_desc_color_error ->
                        descErrorColor = a.getColor(attr, descErrorColor)
                    // Button Text Color
                    R.styleable.MessageView_mv_button_text_color ->
                        buttonTextColor = a.getColor(attr, buttonTextColor)
                    // Icons
                    R.styleable.MessageView_mv_image_normal ->
                        normalImageRes = a.getResourceId(attr,
                            INVALID_VALUE
                        )
                    R.styleable.MessageView_mv_image_loading ->
                        loadingImageRes = a.getResourceId(attr,
                            INVALID_VALUE
                        )
                    R.styleable.MessageView_mv_image_error ->
                        errorImageRes = a.getResourceId(attr,
                            INVALID_VALUE
                        )
                    // Background
                    R.styleable.MessageView_mv_button_background ->
                        binding.button.setBackgroundResource(a.getResourceId(attr,
                            INVALID_VALUE
                        ))
                    //  Button Customization
                    R.styleable.MessageView_mv_button_width ->
                        binding.button.width = a.getDimensionPixelOffset(attr, 100)
                    R.styleable.MessageView_mv_button_height ->
                        binding.button.height = a.getDimensionPixelOffset(attr, 40)
                    //  State
                    R.styleable.MessageView_mv_state -> state = a.getInt(attr, States.HIDE)
                    //  Repeat
                    R.styleable.MessageView_mv_repeat_animation -> animationRepeatCount = if (a.getBoolean(attr, true)) LottieDrawable.INFINITE else 0
                }
            }
            binding.button.setTextColor(buttonTextColor)
            state?.let { updateState(it) }
            a.recycle()
        }
    }

    private fun updateState(@State state: Int) {
        var visibility = View.VISIBLE
        var loadingIndicatorVisibility = View.GONE
        when (state) {
            States.NORMAL -> {
                updateTextColors(titleNormalColor, descNormalColor)
                binding.textTitle.updateState(text = titleNormal)
                binding.textDescription.updateState(text = descNormal)
                binding.image.updateState(normalImageRes)
            }
            States.LOADING -> {
                updateTextColors(titleLoadingColor, descLoadingColor)
                if (loadingImageRes == INVALID_VALUE && lottieFileName.isNullOrEmpty()){
                    loadingIndicatorVisibility = View.VISIBLE
                }
                binding.textTitle.updateState(text = titleLoading)
                binding.textDescription.updateState(text = descLoading)
                binding.image.updateState(loadingImageRes)
            }
            States.ERROR -> {
                updateTextColors(titleErrorColor, descErrorColor)
                binding.textTitle.updateState(text = titleError)
                binding.textDescription.updateState(text = descError)
                binding.image.updateState(errorImageRes)
            }
            States.HIDE -> visibility = View.GONE
        }
        binding.animation.updateState(state, lottieFileName)
        binding.button.updateState() // TODO
        this.visibility = visibility
        binding.progress.visibility = loadingIndicatorVisibility
    }

    interface Callbacks {

        fun onButtonClicked(messageModel: MessageModel? = null)

        fun onAnimationEnd()

        fun onAnimationProgress(progress: Float)
    }

    open class SimpleCallbacks : Callbacks {

        override fun onButtonClicked(messageModel: MessageModel?) {}

        override fun onAnimationEnd() {}

        override fun onAnimationProgress(progress: Float) {}
    }

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(States.NORMAL, States.LOADING, States.ERROR, States.HIDE)
    annotation class State

    class States {

        companion object {

            const val NORMAL = 0
            const val LOADING = 1
            const val ERROR = 2
            const val HIDE = 3

        }

    }

}