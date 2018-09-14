package com.teambrella.android.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.teambrella.android.R

class BottomBarItemView : ConstraintLayout {

    lateinit var icon: ImageView
    lateinit var title: TextView
    lateinit var attentionIndicator: View

    constructor(context: Context?) : super(context) {
        init(context, null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }


    private fun init(context: Context?, attrs: AttributeSet?) {
        View.inflate(context, R.layout.bottom_bar_item_content, this)
        icon = findViewById(R.id.icon)
        title = findViewById(R.id.title)
        attentionIndicator = findViewById(R.id.attention_indicator)

        attrs?.let { _attrs ->
            val a = context?.theme?.obtainStyledAttributes(
                    _attrs,
                    R.styleable.BottomBarItemView,
                    0, 0)

            try {
                icon.setImageDrawable(a?.getDrawable(R.styleable.BottomBarItemView_itemIcon))
                title.text = a?.getString(R.styleable.BottomBarItemView_itemTitle)
                attentionIndicator.visibility = if (a?.getBoolean(R.styleable.BottomBarItemView_attention, false) == true)
                    View.VISIBLE else View.GONE
            } finally {
                a?.recycle()
            }
        }
    }

}