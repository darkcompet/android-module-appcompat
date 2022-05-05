/*
 * Copyright (c) 2017-2021 DarkCompet. All rights reserved.
 */

package tool.compet.appcompat;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import tool.compet.core.DkLogcats;
import tool.compet.core.TheApp;
import tool.compet.core.TheFragment;

/**
 * This is back-compat version which provides features which should be based for all Fragment:
 * - Lifecycle event logging.
 * - ViewModel which can survive instance while config-change.
 *
 * @param <B> ViewDataBinding
 */
public abstract class DkFragment<B extends ViewDataBinding> extends Fragment implements TheFragment {
	/**
	 * Allow init child views via databinding feature.
	 * So we can access to child views via `binder.*` instead of calling findViewById().
	 */
	protected boolean enableDataBinding() {
		return true;
	}

	// Current application
	protected TheApp app;

	// Current fragment activity
	protected FragmentActivity host;

	// Current context
	protected Context context;

	// Layout of this view (normally is ViewGroup, but sometime, user maybe layout with single view)
	protected View layout;

	// Binder for databinding (to initialize child views instead of findViewById())
	public B binder;

	@Override
	public void onAttach(@NonNull Context context) {
		if (BuildConfig.DEBUG) {
			DkLogcats.info(this, "onAttach (context)");
		}
		if (this.host == null) {
			this.host = getActivity();
		}
		if (this.context == null) {
			this.context = context;
		}
		if (this.app == null && this.host != null) {
			this.app = (TheApp) this.host.getApplication();
		}

		super.onAttach(context);
	}

	@Override
	@SuppressWarnings("deprecation") // still work on old OS
	public void onAttach(@NonNull Activity activity) {
		if (BuildConfig.DEBUG) {
			DkLogcats.info(this, "onAttach (activity)");
		}
		if (this.context == null) {
			this.context = getContext();
		}
		if (this.host == null) {
			this.host = (FragmentActivity) activity;
		}
		if (this.app == null) {
			this.app = (TheApp) activity.getApplication();
		}

		super.onAttach(activity);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		if (BuildConfig.DEBUG) {
			DkLogcats.info(this, "onCreate");
		}
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		if (BuildConfig.DEBUG) {
			DkLogcats.info(this, "onCreateView");
		}

		int layoutId = layoutResourceId();
		if (layoutId > 0) {
			if (enableDataBinding()) {
				// Pass `false` to indicate don't attach this layout to parent
				this.binder = DataBindingUtil.inflate(inflater, layoutId, container, false);
				this.layout = this.binder.getRoot();
			}
			else {
				// Pass `false` to indicate don't attach this layout to parent
				this.layout = inflater.inflate(layoutId, container, false);
			}
		}
		else {
			DkLogcats.notice(this, "Fragment %s has no layout?", getClass().getName());
		}

		return this.layout;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		if (BuildConfig.DEBUG) {
			DkLogcats.info(this, "onViewCreated");
		}
		super.onViewCreated(view, savedInstanceState);
	}

	@Override // onViewCreated() -> onViewStateRestored() -> onStart()
	public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
		if (BuildConfig.DEBUG) {
			DkLogcats.info(this, "onViewStateRestored");
		}
		super.onViewStateRestored(savedInstanceState);
	}

	@Override
	public void onStart() {
		if (BuildConfig.DEBUG) {
			DkLogcats.info(this, "onStart");
		}
		super.onStart();
	}

	@Override
	public void onResume() {
		if (BuildConfig.DEBUG) {
			DkLogcats.info(this, "onResume");
		}
		super.onResume();
	}

	@Override
	public void onPause() {
		if (BuildConfig.DEBUG) {
			DkLogcats.info(this, "onPause");
		}
		super.onPause();
	}

	@Override
	public void onStop() {
		if (BuildConfig.DEBUG) {
			DkLogcats.info(this, "onStop");
		}
		super.onStop();
	}

	@Override
	public void onDestroyView() {
		if (BuildConfig.DEBUG) {
			DkLogcats.info(this, "onDestroyView");
		}
		super.onDestroyView();
	}

	@Override // called before onDestroy()
	public void onSaveInstanceState(@NonNull Bundle outState) {
		if (BuildConfig.DEBUG) {
			DkLogcats.info(this, "onSaveInstanceState");
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onDestroy() {
		if (BuildConfig.DEBUG) {
			DkLogcats.info(this, "onDestroy");
		}
		super.onDestroy();
	}

	@Override
	public void onDetach() {
		if (BuildConfig.DEBUG) {
			DkLogcats.info(this, "onDetach");
		}

		this.host = null;
		this.context = null;

		super.onDetach();
	}

	@Override
	public void onLowMemory() {
		if (BuildConfig.DEBUG) {
			DkLogcats.info(this, "onLowMemory");
		}
		super.onLowMemory();
	}

	@Override
	public Fragment getFragment() {
		return this;
	}

	// region ViewModel

	// Get or Create new ViewModel instance which be owned by this Fragment.
	public <VM extends ViewModel> VM obtainOwnViewModel(String key, Class<VM> modelType) {
		return new ViewModelProvider(this).get(key, modelType);
	}

	// Get or Create new ViewModel instance which be owned by Activity which hosts this Fragment.
	public <VM extends ViewModel> VM obtainHostViewModel(String key, Class<VM> modelType) {
		return new ViewModelProvider(host).get(key, modelType);
	}

	// Get or Create new ViewModel instance which be owned by current app.
	public <VM extends ViewModel> VM obtainAppViewModel(String key, Class<VM> modelType) {
		Application app = host.getApplication();

		if (app instanceof ViewModelStoreOwner) {
			return new ViewModelProvider((ViewModelStoreOwner) app).get(key, modelType);
		}

		throw new RuntimeException("App must be subclass of ViewModelStoreOwner");
	}

	// endregion ViewModel

	// region Utility

	public Fragment instantiateFragment(Class<? extends Fragment> fragClass) {
		return getParentFragmentManager().getFragmentFactory().instantiate(context.getClassLoader(), fragClass.getName());
	}

	/**
	 * Listen lifecycle callbacks of descendant fragments managed by this activity.
	 *
	 * @param recursive TRUE to listen all descendant fragments under this host, that is,
	 *                  it includes all child fragments of child fragment-managers and so on.
	 *                  FALSE to listen only child fragments of the child-fragment-manager of this activity.
	 */
	public void registerFragmentLifecycleCallbacks(FragmentManager.FragmentLifecycleCallbacks callback, boolean recursive) {
		getChildFragmentManager().registerFragmentLifecycleCallbacks(callback, recursive);
	}

	// endregion Utility
}
