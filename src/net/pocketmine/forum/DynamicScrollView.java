package net.pocketmine.forum;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

public class DynamicScrollView extends ScrollView {

	public static interface LoadMoreListener {
		public void onLoadMore();
	}

	private LoadMoreListener loadMoreListener;

	public DynamicScrollView(Context context) {
		super(context);
	}

	public DynamicScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setOnLoadMore(LoadMoreListener listener) {
		loadMoreListener = listener;
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		if ((((View) getChildAt(getChildCount() - 1)).getBottom() - (getHeight() + getScrollY())) == 0) {
			if (loadMoreListener != null) {
				loadMoreListener.onLoadMore();
			}
		}

		super.onScrollChanged(l, t, oldl, oldt);
	}
}
