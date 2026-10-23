package com.sorryhorse.RateLimit.Pages.deviceBrowser.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import com.sorryhorse.RateLimit.R

class BtDeviceCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private val nameTextView : TextView
    private val connectionTextView : TextView
    private val image : ImageView
    private var selected : Boolean = false
    private val selectedIcon : ImageView

    private var root : ConstraintLayout

    init {
        LayoutInflater.from(context).inflate(R.layout.bt_device_card, this)
        nameTextView = findViewById(R.id.name)
        connectionTextView = findViewById(R.id.connection)
        image = findViewById(R.id.image)
        root = findViewById(R.id.root)
        selectedIcon = findViewById(R.id.selectedIcon)

        attrs?.let {
            context.withStyledAttributes(it, R.styleable.BtDeviceCard, 0, 0) {
                selected = getBoolean(R.styleable.BtDeviceCard_selected, false)
                nameTextView.text = getString(R.styleable.BtDeviceCard_btDeviceName)
                connectionTextView.text = getString(R.styleable.BtDeviceCard_btConnectionStrength)
                if (selected) {
                    selectedIcon.visibility = VISIBLE
                }
            }
        }
    }
}