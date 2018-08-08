package com.teambrella.android.ui.base

import android.content.Context
import androidx.fragment.app.Fragment
import com.teambrella.android.dagger.Dependencies
import com.teambrella.android.image.TeambrellaImageLoader
import com.teambrella.android.ui.base.dagger.ATeambrellaDaggerActivity
import javax.inject.Inject
import javax.inject.Named

/**
 * Teambrella Fragment
 */
open class TeambrellaFragment : Fragment() {

    @Inject
    @field:Named(Dependencies.IMAGE_LOADER)
    protected lateinit var imageLoader: TeambrellaImageLoader

    protected val component: ITeambrellaComponent
        get() = (context as ATeambrellaDaggerActivity<*>).component

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        component.inject(this)
    }
}
