package com.teambrella.android.ui.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.view.View

class FadeInFadeOutViewController(private val view: View?, private val duration: Long) {

    enum class State {
        HIDDEN,
        HIDING,
        SHOWING,
        SHOWN
    }

    private var state: State = if (view?.visibility == View.VISIBLE)
        State.SHOWN else State.HIDDEN

    fun hide() {
        when (state) {
            State.SHOWN -> {
                val animator = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f)
                animator.duration = duration
                animator.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        state = State.HIDDEN
                        view?.visibility = View.GONE
                    }
                })
                state = State.HIDING
                animator.start()
            }
            else -> {

            }
        }
    }

    fun show() {
        when (state) {
            State.HIDDEN -> {
                val animator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
                animator.duration = duration
                view?.visibility = View.VISIBLE
                animator.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        state = State.SHOWN
                    }
                })
                state = State.SHOWING
                animator.start()
            }
            else -> {

            }
        }
    }


    fun toggle() {
        when (state) {
            State.SHOWN -> hide()
            State.HIDDEN -> show()
            else -> Unit
        }
    }

}