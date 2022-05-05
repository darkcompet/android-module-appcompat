/*
 * Copyright (c) 2017-2021 DarkCompet. All rights reserved.
 */

package tool.compet.appcompat;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import tool.compet.core.DkLocaleHelper;
import tool.compet.core.DkLogcats;
import tool.compet.core.TheActivity;
import tool.compet.core.TheApp;

/**
 * This is back-compat version which provides features which should be based for all Activity:
 * - Lifecycle event logging.
 * - ViewModel which can survive instance while config-change.
 *
 * Be aware of lifecycle in Activity: if activity is not going to be destroyed and
 * returns to foreground after onStop(), then onRestart() -> onStart() will be called respectively.
 *
 * @param <B> ViewDataBinding
 */
public abstract class DkActivity<B extends ViewDataBinding> extends AppCompatActivity implements TheActivity {
	/**
	 * Implement it to setting language displayed in this activity.
	 *
	 * @return App language to be set. Value is `tool.compet.core4j.DkConst.LANG_*`, for eg,. "en", "vi", "ja",...
	 */
	protected abstract String language();

	/**
	 * Allow init child views via databinding feature.
	 * So we can access to child views via `binder.*` instead of calling findViewById().
	 */
	protected boolean enableDataBinding() {
		return true;
	}

	// Current app
	protected TheApp app;

	// Current context
	protected Context context;

	// Layout of this view (normally is ViewGroup, but sometime, user maybe layout with single view)
	protected View layout;

	// Binder for databinding (find child views instead of findViewById())
	protected B binder;

	/**
	 * Subclass should use `getIntent()` in `onResume()` instead since we have called `setIntent()` here.
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		if (BuildConfig.DEBUG) {
			DkLogcats.info(this, "onNewIntent: " + intent);
		}
		setIntent(intent);
		super.onNewIntent(intent);
	}

	@Override
	protected void attachBaseContext(Context newBase) {
		if (BuildConfig.DEBUG) {
			DkLogcats.info(this, "attachBaseContext(), the language was changed to: " + language());
		}
		super.attachBaseContext(DkLocaleHelper.wrapLocale(newBase, language()));
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		if (BuildConfig.DEBUG) {
			DkLogcats.info(this, "onCreate");
		}

		super.onCreate(savedInstanceState);

		this.app = (TheApp) getApplication();
		this.context = this;

		int layoutId = layoutResourceId();
		if (layoutId > 0) {
			if (enableDataBinding()) {
				// Pass `false` to indicate don't attach this layout to parent
				this.binder = DataBindingUtil.setContentView(this, layoutId);
				this.layout = this.binder.getRoot();
			}
			else {
				setContentView(this.layout = View.inflate(this,layoutId, null));
			}
		}
		else {
			DkLogcats.notice(this, "Activity %s has no layout?", getClass().getName());
		}
	}

	@CallSuper
	@Override
	protected void onPostCreate(@Nullable Bundle savedInstanceState) {
		if (BuildConfig.DEBUG) {
			DkLogcats.info(this, "onPostCreate");
		}
		super.onPostCreate(savedInstanceState);
	}

	@Override // onPostCreate() -> onRestoreInstanceState() -> onStart()
	protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
		if (BuildConfig.DEBUG) {
			DkLogcats.info(this, "onRestoreInstanceState");
		}
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onStart() {
		if (BuildConfig.DEBUG) {
			DkLogcats.info(this, "onStart");
		}
		super.onStart();
	}

	@Override
	protected void onResume() {
		if (BuildConfig.DEBUG) {
			DkLogcats.info(this, "onResume");
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		if (BuildConfig.DEBUG) {
			DkLogcats.info(this, "onPause");
		}
		super.onPause();
	}

	@Override // maybe called before onStop() or onDestroy()
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		if (BuildConfig.DEBUG) {
			DkLogcats.info(this, "onSaveInstanceState");
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onStop() {
		if (BuildConfig.DEBUG) {
			DkLogcats.info(this, "onStop");
		}
		super.onStop();
	}

	// after onStop() is onCreate() or onDestroy()
	@Override
	protected void onRestart() {
		if (BuildConfig.DEBUG) {
			DkLogcats.info(this, "onRestart");
		}
		super.onRestart();
	}

	@Override
	protected void onDestroy() {
		if (BuildConfig.DEBUG) {
			DkLogcats.info(this, "onDestroy");
		}

		this.app = null;
		this.context = null;

		super.onDestroy();
	}

	@Override
	public void onLowMemory() {
		if (BuildConfig.DEBUG) {
			DkLogcats.info(this, "onLowMemory");
		}
		super.onLowMemory();
	}

	@Override
	public void onConfigurationChanged(@NonNull Configuration newConfig) {
		if (BuildConfig.DEBUG) {
			DkLogcats.info(this, "onConfigurationChanged");
		}
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public Activity getActivity() {
		return this;
	}

	/**
	 * Finish this view by tell parent finish this.
	 */
	@Override
	public boolean close() {
		finish();
		return true;
	}

	public Fragment instantiateFragment(Class<? extends Fragment> fragClass) {
		return getSupportFragmentManager().getFragmentFactory().instantiate(getClassLoader(), fragClass.getName());
	}

	// region ViewModel

	// Get or Create new ViewModel instance which be owned by this activity.
	public <VM extends ViewModel> VM obtainOwnViewModel(String key, Class<VM> modelType) {
		return new ViewModelProvider(this).get(key, modelType);
	}

	// Get or Create new ViewModel instance which be owned by current app.
	public <VM extends ViewModel> VM obtainAppViewModel(String key, Class<VM> modelType) {
		Application app = getApplication();

		if (app instanceof ViewModelStoreOwner) {
			return new ViewModelProvider((ViewModelStoreOwner) app).get(key, modelType);
		}

		throw new RuntimeException("App must be subclass of `ViewModelStoreOwner`");
	}

	// endregion ViewModel

	// region Utility

	/**
	 * Listen lifecycle callbacks of descendant fragments managed by this activity.
	 *
	 * @param recursive TRUE to listen all descendant fragments under this host, that is,
	 *                  it includes all child fragments of child fragment-managers and so on.
	 *                  FALSE to listen only child fragments of the child-fragment-manager of this activity.
	 */
	public void registerFragmentLifecycleCallbacks(FragmentManager.FragmentLifecycleCallbacks callback, boolean recursive) {
		getSupportFragmentManager().registerFragmentLifecycleCallbacks(callback, recursive);
	}

	// endregion Utility
}
