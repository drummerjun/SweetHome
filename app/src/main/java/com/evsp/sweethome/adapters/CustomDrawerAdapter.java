package com.evsp.sweethome.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.evsp.sweethome.Constants;
import com.evsp.sweethome.R;

import java.util.List;

public class CustomDrawerAdapter extends ArrayAdapter<DrawerItem> {

	Context context;
	List<DrawerItem> drawerItemList;
	int layoutResID;
    private int selectedItem;

	public CustomDrawerAdapter(Context cntx, int layoutid,
			List<DrawerItem> listItems) {
		super(cntx, layoutid, listItems);
		context = cntx;
		drawerItemList = listItems;
		layoutResID = layoutid;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		DrawerItemHolder drawerHolder;
		View view = convertView;

        RelativeLayout homeLayout;
        ImageView homeIcon;
        TextView homeStatus;

        if (view == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			drawerHolder = new DrawerItemHolder();

			view = inflater.inflate(layoutResID, parent, false);
			drawerHolder.ItemName = (TextView) view.findViewById(R.id.drawer_itemName);
			drawerHolder.icon = (ImageView) view.findViewById(R.id.drawer_icon);
			drawerHolder.title = (TextView) view.findViewById(R.id.drawerTitle);
            drawerHolder.alert = (TextView) view.findViewById(R.id.alertnum);
			drawerHolder.headerLayout = (LinearLayout) view.findViewById(R.id.headerLayout);
			drawerHolder.itemLayout = (LinearLayout) view.findViewById(R.id.itemLayout);
			view.setTag(drawerHolder);
		} else {
			drawerHolder = (DrawerItemHolder) view.getTag();
        }

        homeLayout = (RelativeLayout)view.findViewById(R.id.homeLayout);
        homeStatus = (TextView)view.findViewById(R.id.homeStatus);
        if(position == selectedItem) {
            drawerHolder.ItemName.setTypeface(null, Typeface.BOLD);
            int color = 0;
            switch(selectedItem) {
                case Constants.INDEX_HOME:
                    color = android.R.color.holo_green_dark;
                    break;
                case Constants.INDEX_GUI:
                    color = android.R.color.holo_orange_dark;
                    break;
                case Constants.INDEX_ALARM:
                    color = android.R.color.holo_purple;
                    break;
                case Constants.INDEX_CONSOLE:
                    color = android.R.color.holo_green_dark;
                    break;
                case Constants.INDEX_ALERTS:
                    color = android.R.color.holo_red_dark;
                    break;
                case Constants.INDEX_WEB:
                    color = android.R.color.holo_blue_dark;
                    break;
                default:
                    color = android.R.color.black;
                    break;
            }
            drawerHolder.ItemName.setTextColor(view.getResources().getColor(color));
        } else {
            drawerHolder.ItemName.setTextColor(view.getResources().getColor(android.R.color.black));
            drawerHolder.ItemName.setTypeface(null, Typeface.NORMAL);
        }

		DrawerItem dItem = drawerItemList.get(position);

        if(position == Constants.INDEX_HOME) {
            homeLayout.setVisibility(View.VISIBLE);
            drawerHolder.headerLayout.setVisibility(View.GONE);
            drawerHolder.itemLayout.setVisibility(View.GONE);
            homeIcon = (ImageView)view.findViewById(R.id.homeIcon);
            Bitmap bitmap = BitmapFactory.decodeResource(view.getResources(), R.drawable.profile);
            CircularDrawable circularDrawable = new CircularDrawable(bitmap);
            homeIcon.setImageDrawable(circularDrawable);
            if(dItem.getGatewayStatus()) {
                homeStatus.setText(view.getResources().getString(R.string.online));
                homeStatus.setTextColor(view.getResources().getColor(android.R.color.holo_green_light));
            } else {
                homeStatus.setText(view.getResources().getString(R.string.offline));
                homeStatus.setTextColor(view.getResources().getColor(android.R.color.holo_red_dark));
            }
        } else if (dItem.getTitle() != null) {
            homeLayout.setVisibility(View.GONE);
			drawerHolder.headerLayout.setVisibility(LinearLayout.VISIBLE);
			drawerHolder.itemLayout.setVisibility(LinearLayout.GONE);
			drawerHolder.title.setText(dItem.getTitle());
		} else {
            homeLayout.setVisibility(View.GONE);
			drawerHolder.headerLayout.setVisibility(LinearLayout.GONE);
			drawerHolder.itemLayout.setVisibility(LinearLayout.VISIBLE);
			drawerHolder.icon.setImageDrawable(view.getResources().getDrawable(dItem.getImgResID()));
			drawerHolder.ItemName.setText(dItem.getItemName());
            if(dItem.getDeviceID() == Constants.INDEX_ALERTS) {
                int notifications = dItem.getNotifications();
                if(notifications > 0) {
                    drawerHolder.alert.setVisibility(View.VISIBLE);
                    if(notifications > Constants.MAX_RECORD_NUM) {
                        drawerHolder.alert.setText(String.valueOf(Constants.MAX_RECORD_NUM) + "+");
                    } else {
                        drawerHolder.alert.setText(String.valueOf(dItem.getNotifications()));
                    }
                } else {
                    drawerHolder.alert.setVisibility(View.INVISIBLE);
                }
            } else {
                drawerHolder.alert.setVisibility(View.INVISIBLE);
            }
		}
		return view;
	}

	private static class DrawerItemHolder {
		TextView ItemName, title, alert;
		ImageView icon;
		LinearLayout headerLayout, itemLayout;
	}

    public void selectItem(int item) {
        selectedItem = item;
        notifyDataSetChanged();
    }

    private class CircularDrawable extends Drawable {
        private final Bitmap mBitmap;
        private final Paint mPaint;
        private final RectF mRectF;
        private final int mBitmapWidth;
        private final int mBitmapHeight;

        public CircularDrawable(Bitmap bitmap) {
            mBitmap = bitmap;
            mRectF = new RectF();
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setDither(true);
            final BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            mPaint.setShader(shader);

            mBitmapWidth = mBitmap.getWidth();
            mBitmapHeight = mBitmap.getHeight();
        }

        @Override
        public void draw(Canvas canvas) {
            canvas.drawOval(mRectF, mPaint);
        }

        @Override
        protected void onBoundsChange(Rect bounds) {
            super.onBoundsChange(bounds);
            mRectF.set(bounds);
        }

        @Override
        public void setAlpha(int alpha) {
            if (mPaint.getAlpha() != alpha) {
                mPaint.setAlpha(alpha);
                invalidateSelf();
            }
        }

        @Override
        public void setColorFilter(ColorFilter cf) {
            mPaint.setColorFilter(cf);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }

        @Override
        public int getIntrinsicWidth() {
            return mBitmapWidth;
        }

        @Override
        public int getIntrinsicHeight() {
            return mBitmapHeight;
        }

        public void setAntiAlias(boolean aa) {
            mPaint.setAntiAlias(aa);
            invalidateSelf();
        }

        @Override
        public void setFilterBitmap(boolean filter) {
            mPaint.setFilterBitmap(filter);
            invalidateSelf();
        }

        @Override
        public void setDither(boolean dither) {
            mPaint.setDither(dither);
            invalidateSelf();
        }

        public Bitmap getBitmap() {
            return mBitmap;
        }
    }
}