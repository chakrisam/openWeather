package com.synchronoss.openweather.util
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.content.res.ResourcesCompat
import com.synchronoss.openweather.R
import com.synchronoss.openweather.databinding.CustomDialogViewBinding

class CustomProgressDialog {
    lateinit var dialog: CustomDialog
    private lateinit var binding: CustomDialogViewBinding

    fun show(context: Context): Dialog {
        return show(context, null)
    }

    fun initDialog(context: Context) {
        val inflater = (context as Activity).layoutInflater
        binding = CustomDialogViewBinding.inflate(inflater)
        dialog = CustomDialog(context)
        dialog.setContentView(binding.root)
    }

    fun show(context: Context, title: CharSequence?): Dialog {

        if (title != null) {
            binding.cpTitle.text = title
        }

        // Card Color
        binding.cpCardview.setCardBackgroundColor(Color.parseColor("#70000000"))

        // Progress Bar Color
        setColorFilter(
            binding.cpPbar.indeterminateDrawable,
            ResourcesCompat.getColor(context.resources, R.color.purple_500, null)
        )

        // Text Color
        binding.cpTitle.setTextColor(Color.WHITE)

        dialog.show()
        return dialog
    }

    private fun setColorFilter(drawable: Drawable, color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            drawable.colorFilter = BlendModeColorFilter(color, BlendMode.SRC_ATOP)
        } else {
            @Suppress("DEPRECATION")
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        }
    }

    class CustomDialog(context: Context) : Dialog(context, R.style.CustomDialogTheme) {
        init {
            // Set Semi-Transparent Color for Dialog Background
            window?.decorView?.rootView?.setBackgroundResource(R.color.dialogBackground)
            window?.decorView?.setOnApplyWindowInsetsListener { _, insets ->
                insets.consumeSystemWindowInsets()
            }
        }
    }
}