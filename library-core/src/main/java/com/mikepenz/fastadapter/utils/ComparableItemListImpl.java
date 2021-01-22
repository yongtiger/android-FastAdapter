package com.mikepenz.fastadapter.utils;

import androidx.annotation.Nullable;

import com.mikepenz.fastadapter.IAdapterNotifier;
import com.mikepenz.fastadapter.IItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * The default item list implementation
 */

public class ComparableItemListImpl<Item extends IItem> extends DefaultItemListImpl<Item> {

    private Comparator<Item> mComparator;

    public ComparableItemListImpl(@Nullable Comparator<Item> comparator) {
        this.mItems = new ArrayList<>();
        this.mComparator = comparator;
    }

    public ComparableItemListImpl(@Nullable Comparator<Item> comparator, List<Item> items) {
        this.mItems = items;
        this.mComparator = comparator;
    }

    /**
     * @return the defined Comparator used for this ItemAdaper
     */
    public Comparator<Item> getComparator() {
        return mComparator;
    }

    /**
     * define a comparator which will be used to sort the list "everytime" it is altered
     * NOTE this will only sort if you "set" a new list or "add" new items (not if you provide a position for the add function)
     *
     * @param comparator used to sort the list
     * @return this
     */
    public ComparableItemListImpl<Item> withComparator(@Nullable Comparator<Item> comparator) {
        ///[UPGRADE#ComparableItemListImpl#withComparator(@Nullable Comparator<Item> comparator, boolean sortNow, boolean notify)]
//        return withComparator(comparator, true);
        return withComparator(comparator, true, true);
    }

    /**
     * define a comparator which will be used to sort the list "everytime" it is altered
     * NOTE this will only sort if you "set" a new list or "add" new items (not if you provide a position for the add function)
     *
     * @param comparator used to sort the list
     * @param sortNow    specifies if we use the provided comparator to sort now
     * @return this
     */
    ///[UPGRADE#ComparableItemListImpl#withComparator(@Nullable Comparator<Item> comparator, boolean sortNow, boolean notify)]
    public ComparableItemListImpl<Item> withComparator(@Nullable Comparator<Item> comparator, boolean sortNow) {
        return withComparator(comparator, sortNow, true);
    }
    public ComparableItemListImpl<Item> withComparator(@Nullable Comparator<Item> comparator, boolean sortNow, boolean notify) {
        this.mComparator = comparator;

        //we directly sort the list with the defined comparator
        if (mItems != null && mComparator != null && sortNow) {
            Collections.sort(mItems, mComparator);
            ///[UPGRADE#ComparableItemListImpl#withComparator(@Nullable Comparator<Item> comparator, boolean sortNow, boolean notify)]
//            getFastAdapter().notifyAdapterDataSetChanged();
            if(notify) {
                getFastAdapter().notifyAdapterDataSetChanged();
            }
        }

        return this;
    }

    @Override
    public void move(int fromPosition, int toPosition, int preItemCount) {
        Item item = mItems.get(fromPosition - preItemCount);
        mItems.remove(fromPosition - preItemCount);
        mItems.add(toPosition - preItemCount, item);
        if (mComparator != null) {
            Collections.sort(mItems, mComparator);
        }
        getFastAdapter().notifyAdapterDataSetChanged();
    }

    @Override
    public void addAll(List<Item> items, int preItemCount) {
        mItems.addAll(items);
        if (mComparator != null) {
            Collections.sort(mItems, mComparator);
        }
        getFastAdapter().notifyAdapterDataSetChanged();
    }

    @Override
    public void addAll(int position, List<Item> items, int preItemCount) {
        mItems.addAll(position - preItemCount, items);
        if (mComparator != null) {
            Collections.sort(mItems, mComparator);
        }
        getFastAdapter().notifyAdapterDataSetChanged();
    }

    @Override
    public void setNewList(List<Item> items, boolean notify) {
        mItems = new ArrayList<>(items);
        if (mComparator != null) {
            Collections.sort(mItems, mComparator);
        }
        if(notify) {
            getFastAdapter().notifyAdapterDataSetChanged();
        }
    }

    ///[FIX#ComparableItemListImpl#set(List<Item> items, int preItemCount, @javax.annotation.Nullable IAdapterNotifier adapterNotifier)]
    @Override
    public void set(List<Item> items, int preItemCount, @javax.annotation.Nullable IAdapterNotifier adapterNotifier) {
        //get sizes
        int newItemsCount = items.size();
        int previousItemsCount = mItems.size();

        //make sure the new items list is not a reference of the already mItems list
        if (items != mItems) {
            //remove all previous items
            if (!mItems.isEmpty()) {
                mItems.clear();
            }

            //add all new items to the list
            mItems.addAll(items);
        }
        if (mComparator != null) {
            Collections.sort(mItems, mComparator);
        }
        if (getFastAdapter() == null) return;
        //now properly notify the adapter about the changes
        if (adapterNotifier == null) {
            adapterNotifier = IAdapterNotifier.DEFAULT;
        }
        adapterNotifier.notify(getFastAdapter(), newItemsCount, previousItemsCount, preItemCount);
    }

}
