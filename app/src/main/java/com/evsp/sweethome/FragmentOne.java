package com.evsp.sweethome;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class FragmentOne extends Fragment {

    private WebView mWebView;
	public FragmentOne() {}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_main, container, false);
        setHasOptionsMenu(true);
        mWebView = (WebView) view.findViewById(R.id.webview);
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);

        mWebView.loadUrl("http://10.10.91.91/home/home.html");
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
		return view;
	}

    @Override
    public void onResume() {
        super.onResume();
        ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.webview).toUpperCase());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.menuitem_refresh).setVisible(true);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean canGoBack() {
        if(mWebView == null) {
            //error log
        } else {
            return mWebView.canGoBack();
        }
        return false;
    }

    public void goBack() {
        if(mWebView == null) {
            //error log
        } else {
            mWebView.goBack();
        }
    }

    public void refresh() {
        mWebView.reload();
    }
}
