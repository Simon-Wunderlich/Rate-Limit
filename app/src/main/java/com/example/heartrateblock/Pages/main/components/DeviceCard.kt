package com.example.heartrateblock.Pages.main.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import com.example.heartrateblock.R

class DeviceCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private val nameTextView : TextView
    private val connectionTextView : TextView
    private val image : ImageView
    private var connected : Boolean = false

    private var root : ConstraintLayout

    init {
        LayoutInflater.from(context).inflate(R.layout.device_card, this)
        nameTextView = findViewById(R.id.name)
        connectionTextView = findViewById(R.id.connection)
        image = findViewById(R.id.image)
        root = findViewById(R.id.root)

        attrs?.let {
            context.withStyledAttributes(it, R.styleable.DeviceCard, 0, 0) {
                connected = getBoolean(R.styleable.DeviceCard_connected, false)
                if (connected) {
                    nameTextView.text = getString(R.styleable.DeviceCard_deviceName)
                    connectionTextView.text = getString(R.styleable.DeviceCard_connectionStrength)
                }
                else {
                    setErrorState()
                }
            }
        }
    }
    fun setErrorState() {
        nameTextView.text = "Device not connected"
        connectionTextView.visibility = GONE
        image.setImageDrawable(
            AppCompatResources.getDrawable(
                context,
                R.drawable.not_connected_icon
            )
        )
        root.background = AppCompatResources.getDrawable(context, R.drawable.device_card_error)
    }
}