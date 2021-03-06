package com.mikepenz.fastadapter_extensions.swipe;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

import android.util.Log;
import android.view.View;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IItem;

/**
 * Created by Mattias on 2016-02-13.
 */
public class SimpleSwipeCallback extends ItemTouchHelper.SimpleCallback {

    public interface ItemSwipeCallback {

        ///[FIX#SimpleSwipeCallback#itemSwiped(long identifier, int direction)]
//        /**
//         * Called when an item has been swiped
//         *
//         * @param position  position of item in the adapter
//         * @param direction direction the item was swiped
//         */
//        void itemSwiped(int position, int direction);
//        void itemSwiped(int position, long identifier, int direction);
        void itemSwiped(int position, IItem item, int direction);

        ///[UPGRADE#onSwiping()]
        void onSwiping(SimpleSwipeCallback simpleSwipeCallback, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                       float dX, float dY, int actionState, boolean isCurrentlyActive);
    }

    private final ItemSwipeCallback itemSwipeCallback;

    private int bgColorLeft;
    private int bgColorRight;
    private Drawable leaveBehindDrawableLeft;
    private Drawable leaveBehindDrawableRight;

    private Paint bgPaint;
    private int horizontalMargin = Integer.MAX_VALUE;

    public SimpleSwipeCallback(SimpleSwipeCallback.ItemSwipeCallback itemSwipeCallback, Drawable leaveBehindDrawableLeft) {
        this(itemSwipeCallback, leaveBehindDrawableLeft, ItemTouchHelper.LEFT);
    }

    public SimpleSwipeCallback(SimpleSwipeCallback.ItemSwipeCallback itemSwipeCallback, Drawable leaveBehindDrawableLeft, int swipeDirs) {
        this(itemSwipeCallback, leaveBehindDrawableLeft, swipeDirs, Color.RED);
    }

    public SimpleSwipeCallback(ItemSwipeCallback itemSwipeCallback, Drawable leaveBehindDrawableLeft, int swipeDirs, @ColorInt int bgColor) {
        super(0, swipeDirs);
        this.itemSwipeCallback = itemSwipeCallback;
        this.leaveBehindDrawableLeft = leaveBehindDrawableLeft;
        this.bgColorLeft = bgColor;
    }

    ///[UPGRADE#onSwiping()]
    public Drawable getLeaveBehindSwipeLeft() {
        return this.leaveBehindDrawableLeft;
    }
    public SimpleSwipeCallback withLeaveBehindSwipeLeft(Drawable d) {
        this.leaveBehindDrawableLeft = d;
        setDefaultSwipeDirs(super.getSwipeDirs(null, null) | ItemTouchHelper.LEFT);
        return this;
    }

    ///[UPGRADE#onSwiping()]
    public Drawable getLeaveBehindSwipeRight() {
        return this.leaveBehindDrawableRight;
    }
    public SimpleSwipeCallback withLeaveBehindSwipeRight(Drawable d) {
        this.leaveBehindDrawableRight = d;
        setDefaultSwipeDirs(super.getSwipeDirs(null, null) | ItemTouchHelper.RIGHT);
        return this;
    }

    public SimpleSwipeCallback withHorizontalMarginDp(@NonNull Context ctx, int dp) {
        return withHorizontalMarginPx((int) (ctx.getResources().getDisplayMetrics().density * dp));
    }

    public SimpleSwipeCallback withHorizontalMarginPx(int px) {
        horizontalMargin = px;
        return this;
    }

    ///[UPGRADE#onSwiping()]
    public @ColorInt int getBackgroundSwipeLeft() {
        return bgColorLeft;
    }
    public SimpleSwipeCallback withBackgroundSwipeLeft(@ColorInt int bgColor) {
        bgColorLeft = bgColor;
        return this;
    }

    ///[UPGRADE#onSwiping()]
    public @ColorInt int getBackgroundSwipeRight() {
        return bgColorRight;
    }
    public SimpleSwipeCallback withBackgroundSwipeRight(@ColorInt int bgColor) {
        bgColorRight = bgColor;
        return this;
    }

    @Override
    public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        IItem item = FastAdapter.getHolderAdapterItem(viewHolder);

        ///[FIX#getSwipeDirs()]
        if (item instanceof ISwipeable && item.isEnabled() && ((ISwipeable) item).isSwipeable()) {
            return super.getSwipeDirs(recyclerView, viewHolder);
        } else {
            return 0;
        }
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        viewHolder.itemView.setTranslationX(0);
        viewHolder.itemView.setTranslationY(0);
        ///[FIX#SimpleSwipeCallback#itemSwiped(long identifier, int direction)]
        int position = viewHolder.getAdapterPosition();
        //        long itemId = viewHolder.getItemId();   ///注意：当hasStableIds为false时，只能得到-1L，因此弃用此方法！
        IItem item = FastAdapter.getHolderAdapterItemTag(viewHolder);
        if (item == null) {
            Log.e("FastAdapter", "The bindView method of this item should set the `Tag` on its itemView (https://github.com/mikepenz/FastAdapter/blob/develop/library-core/src/main/java/com/mikepenz/fastadapter/items/AbstractItem.java#L189)");
        }
        if (position != RecyclerView.NO_POSITION && item instanceof ISwipeable && item.getIdentifier() != -1L) {
            itemSwipeCallback.itemSwiped(position, item, direction);
        }
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        // not enabled
        return false;
    }

    //Inspired/modified from: https://github.com/nemanja-kovacevic/recycler-view-swipe-to-delete/blob/master/app/src/main/java/net/nemanjakovacevic/recyclerviewswipetodelete/MainActivity.java
    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        View itemView = viewHolder.itemView;
        if (viewHolder.getAdapterPosition() == RecyclerView.NO_POSITION) {
            return;
        }
        if (Math.abs(dX) > Math.abs(dY)) {

            ///[UPGRADE#onSwiping()]
            itemSwipeCallback.onSwiping(this, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            boolean isLeft = dX < 0;

            if (bgPaint == null) {
                bgPaint = new Paint();
                if (horizontalMargin == Integer.MAX_VALUE) {
                    withHorizontalMarginDp(recyclerView.getContext(), 16);
                }
            }
            bgPaint.setColor(isLeft ? bgColorLeft : bgColorRight);

            if (bgPaint.getColor() != Color.TRANSPARENT) {
                int left = isLeft ? itemView.getRight() + (int) dX : itemView.getLeft();
                int right = isLeft ? itemView.getRight() : (itemView.getLeft() + (int) dX);
                c.drawRect(left, itemView.getTop(), right, itemView.getBottom(), bgPaint);
            }

            Drawable drawable = isLeft ? leaveBehindDrawableLeft : leaveBehindDrawableRight;
            if (drawable != null) {
                int itemHeight = itemView.getBottom() - itemView.getTop();
                int intrinsicWidth = drawable.getIntrinsicWidth();
                int intrinsicHeight = drawable.getIntrinsicWidth();

                int left;
                int right;
                if (isLeft) {
                    left = itemView.getRight() - horizontalMargin - intrinsicWidth;
                    right = itemView.getRight() - horizontalMargin;
                } else {
                    left = itemView.getLeft() + horizontalMargin;
                    right = itemView.getLeft() + horizontalMargin + intrinsicWidth;
                }
                int top = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
                int bottom = top + intrinsicHeight;
                drawable.setBounds(left, top, right, bottom);

                drawable.draw(c);
            }
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
}
