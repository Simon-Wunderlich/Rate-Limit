package com.sorryhorse.RateLimit.Pages.main.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.withStyledAttributes
import com.sorryhorse.RateLimit.R
import androidx.core.view.isVisible
import com.sorryhorse.RateLimit.models.App

class AppCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private val nameTextView : TextView
    private val icon : ImageView
    private val tick : ImageView
    private var id: String? = null

    init {
        orientation = HORIZONTAL

        LayoutInflater.from(context).inflate(R.layout.app_card, this)
        nameTextView = findViewById(R.id.name)
        tick = findViewById(R.id.selectedIcon)
        icon = findViewById(R.id.image)

        attrs?.let {
            context.withStyledAttributes(it, R.styleable.AppCard, 0, 0) {
                nameTextView.text = getString(R.styleable.AppCard_name)
            }
        }
    }

    fun setData(app: App, selected: Boolean) {
        nameTextView.text = app.title
        if (selected)
            tick.visibility = VISIBLE
        icon.setImageDrawable(app.icon)
        this.id = app.id
    }

    fun toggleSelected() {
        if (tick.isVisible)
            tick.visibility = GONE
        else
            tick.visibility = VISIBLE
    }
    fun getName() : String { return nameTextView.text as String }
}