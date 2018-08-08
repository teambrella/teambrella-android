package com.teambrella.android.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.teambrella.android.R

class TeammateVoteRisk : ConstraintLayout {


    lateinit var title: TextView
    lateinit var risk: TextView
    lateinit var avgDifference: TextView

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
        View.inflate(context, R.layout.teammate_vote_risk, this)
        risk = findViewById(R.id.risk)
        title = findViewById(R.id.title)
        avgDifference = findViewById(R.id.avg_difference)

        attrs?.let { _attrs ->
            val a = context?.theme?.obtainStyledAttributes(
                    _attrs,
                    R.styleable.TeammateVoteRisk,
                    0, 0)

            try {
                title.text = a?.getString(R.styleable.TeammateVoteRisk_voteTitle)
            } finally {
                a?.recycle()
            }
        }
    }
}