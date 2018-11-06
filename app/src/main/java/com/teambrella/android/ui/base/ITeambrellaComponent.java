package com.teambrella.android.ui.base;

import com.teambrella.android.dagger.ContextModule;
import com.teambrella.android.dagger.TeambrellaImageLoaderModule;
import com.teambrella.android.dagger.TeambrellaServerModule;
import com.teambrella.android.dagger.TeambrellaUserModule;
import com.teambrella.android.data.base.TeambrellaDataLoader;
import com.teambrella.android.data.base.TeambrellaDataPagerLoader;
import com.teambrella.android.data.base.TeambrellaRequestLoader;
import com.teambrella.android.ui.chat.KChatDataPagerLoader;
import com.teambrella.android.ui.chat.TakePhotosDialogFragment;
import com.teambrella.android.ui.claim.ReportClaimActivity;
import com.teambrella.android.ui.widget.TeambrellaAvatarsWidgets;

import javax.inject.Singleton;

import dagger.Component;

/**
 * ITeambrella Component
 */
@Singleton
@Component(modules = {ContextModule.class, TeambrellaUserModule.class, TeambrellaServerModule.class, TeambrellaImageLoaderModule.class})
public interface ITeambrellaComponent {

    void inject(TeambrellaDaggerActivity activity);

    void inject(ATeambrellaDialogFragment fragment);

    void inject(TeambrellaDataLoader loader);

    void inject(TeambrellaDataPagerLoader loader);

    void inject(ATeambrellaActivity activity);

    void inject(TeambrellaRequestLoader loader);

    void inject(KChatDataPagerLoader loader);

    void inject(TeambrellaDataPagerAdapter adapter);

    void inject(TeambrellaFragment fragment);

    void inject(ATeambrellaAdapter adapter);

    void inject(TeambrellaAvatarsWidgets widgets);

    void inject(ReportClaimActivity activity);

    void inject(TakePhotosDialogFragment fragment);
}
