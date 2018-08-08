package com.teambrella.android.ui.base.dagger;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.teambrella.android.ui.base.ITeambrellaComponent;

/**
 * Teambrella Activity
 */
public abstract class ATeambrellaDaggerActivity<T extends ITeambrellaComponent> extends AppCompatActivity implements IDaggerActivity<T> {

    private T mComponent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        mComponent = createComponent();
        super.onCreate(savedInstanceState);
    }

    @Override
    public T getComponent() {
        return mComponent;
    }

    protected abstract T createComponent();
}
