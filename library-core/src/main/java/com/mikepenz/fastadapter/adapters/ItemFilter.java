package com.mikepenz.fastadapter.adapters;

import androidx.annotation.Nullable;
import android.widget.Filter;

import com.mikepenz.fastadapter.IAdapterExtension;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.IItemAdapter;
import com.mikepenz.fastadapter.listeners.ItemFilterListener;
import com.mikepenz.fastadapter.utils.ComparableItemListImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * ItemFilter which extends the Filter api provided by Android
 * This calls automatically all required methods, just overwrite the filterItems method
 */
public class ItemFilter<Model, Item extends IItem> extends Filter {
    private List<Item> mOriginalItems;

    ///[ADD#ItemFilter#mOriginalItems]
    public List<Item> getOriginalItems() {
        return mOriginalItems;
    }
    public void setOriginalItems(List<Item> originalItems) {
        this.mOriginalItems = originalItems;
    }
    public void sortOriginalItems() {
        if (mItemAdapter.getItemList() instanceof ComparableItemListImpl && mOriginalItems != null && mOriginalItems.size() > 0) {
            Comparator<Item> comparator = ((ComparableItemListImpl<Item>) mItemAdapter.getItemList()).getComparator();
            if (comparator != null) {
                Collections.sort(mOriginalItems, comparator);
            }
        }
    }

    private CharSequence mConstraint;
    private ModelAdapter<?, Item> mItemAdapter;

    public ItemFilter(ModelAdapter<?, Item> itemAdapter) {
        this.mItemAdapter = itemAdapter;
    }

    //the listener which will be called after the items were filtered
    protected ItemFilterListener<Item> mItemFilterListener;

    /**
     * @param listener which will be called after the items were filtered
     * @return this
     */
    public ItemFilter<Model, Item> withItemFilterListener(ItemFilterListener<Item> listener) {
        mItemFilterListener = listener;
        return this;
    }

    //the filter predicate which is used in the ItemFilter
    private IItemAdapter.Predicate<Item> mFilterPredicate;

    /**
     * define the predicate used to filter the list inside the ItemFilter
     *
     * @param filterPredicate the predicate used to filter the list inside the ItemFilter
     * @return this
     */
    public ItemFilter<Model, Item> withFilterPredicate(IItemAdapter.Predicate<Item> filterPredicate) {
        this.mFilterPredicate = filterPredicate;
        return this;
    }

    @Override
    public FilterResults performFiltering(@Nullable CharSequence constraint) {
        FilterResults results = new FilterResults();

        //return nothing
        if (mOriginalItems == null && (constraint == null || constraint.length() == 0)) {
            return results;
        }

        //call extensions
        for (IAdapterExtension<Item> ext : mItemAdapter.getFastAdapter().getExtensions()) {
            ext.performFiltering(constraint);
        }

        mConstraint = constraint;

        if (mOriginalItems == null) {
            mOriginalItems = new ArrayList<>(mItemAdapter.getAdapterItems());
        }

        // We implement here the filter logic
        if (constraint == null || constraint.length() == 0) {
            // No filter implemented we return all the list
            results.values = mOriginalItems;
            results.count = mOriginalItems.size();
            ///[FIX#ItemFilter#mOriginalItems = null]
//            //our filter was cleared we can now forget the old OriginalItems
//            mOriginalItems = null;

            if (mItemFilterListener != null) {
                mItemFilterListener.onReset();
            }
        } else {
            List<Item> filteredItems = new ArrayList<>();

            // We perform filtering operation
            if (mFilterPredicate != null) {
                for (Item item : mOriginalItems) {
                    if (mFilterPredicate.filter(item, constraint)) {
                        filteredItems.add(item);
                    }
                }
            } else {
                filteredItems = mItemAdapter.getAdapterItems();
            }

            results.values = filteredItems;
            results.count = filteredItems.size();
        }
        return results;
    }

    public CharSequence getConstraint() {
        return mConstraint;
    }

    ///[UPGRADE#ItemFilter#setConstraint(CharSequence constraint)]
    public void setConstraint(CharSequence constraint) {
        mConstraint = constraint;
    }

    @Override
    protected void publishResults(@Nullable CharSequence constraint, FilterResults results) {
        // Now we have to inform the adapter about the new list filtered
        if (results.values != null) {
            mItemAdapter.setInternal((List<Item>) results.values, false, null);
        }

        ///[FIX#ItemFilter#mOriginalItems = null]
//        //only fire when we are filtered, not in onReset()
//        if (mItemFilterListener != null && mOriginalItems != null) {
        if (mItemFilterListener != null) {
            mItemFilterListener.itemsFiltered(constraint, (List<Item>) results.values);
        }
    }

    ///[isPublishResults]
    public ModelAdapter<?, Item> publishResults() {
        publishResults(mConstraint, performFiltering(mConstraint));
        return mItemAdapter;
    }

    /**
     * helper method to get all selections from the ItemAdapter's original item list
     *
     * @return a Set with the global positions of all selected Items
     */
    public Set<Integer> getSelections() {
        if (mOriginalItems != null) {
            Set<Integer> selections = new HashSet<>();
            int adapterOffset = mItemAdapter.getFastAdapter().getPreItemCountByOrder(mItemAdapter.getOrder());
            for (int i = 0, size = mOriginalItems.size(); i < size; i++) {
                Item item = mOriginalItems.get(i);
                if (item.isSelected()) {
                    selections.add(i + adapterOffset);
                }
            }
            return selections;
        } else {
            ///[FIX#ItemFilter/ModelAdapter#getSelections()/getSelectedItems()]
//            return mItemAdapter.getFastAdapter().getSelections();
            return mItemAdapter.getSelections();
        }
    }

    /**
     * helper method to get all selections from the ItemAdapter's original item list
     *
     * @return a Set with the selected items out of all items in this itemAdapter (not the listed ones)
     */
    public List<Item> getSelectedItems() {
        if (mOriginalItems != null) {
            List<Item> selections = new ArrayList<>();
            for (int i = 0, size = mOriginalItems.size(); i < size; i++) {
                Item item = mOriginalItems.get(i);
                if (item.isSelected()) {
                    selections.add(item);
                }
            }
            return selections;
        } else {
            ///[FIX#ItemFilter/ModelAdapter#getSelections()/getSelectedItems()]
//            return mItemAdapter.getFastAdapter().getSelectedItems();
            return mItemAdapter.getSelectedItems();
        }
    }

    /**
     * Searches for the given item and calculates its relative position
     *
     * @param item the item which is searched for
     * @return the relative position
     */
    public int getAdapterPosition(Item item) {
        return getAdapterPosition(item.getIdentifier());
    }

    /**
     * Searches for the given identifier and calculates its relative position
     *
     * @param identifier the identifier of an item which is searched for
     * @return the relative position
     */
    public int getAdapterPosition(long identifier) {
        for (int i = 0, size = mOriginalItems.size(); i < size; i++) {
            if (mOriginalItems.get(i).getIdentifier() == identifier) {
                return i;
            }
        }
        return -1;
    }

    /**
     * add an array of items to the end of the existing items
     *
     * @param items the items to add
     */
    ///[isPublishResults]
    @SafeVarargs
    public final ModelAdapter<?, Item> add(boolean isPublishResults, Item... items) {
        return add(isPublishResults, asList(items));
    }
    @SafeVarargs
    public final ModelAdapter<?, Item> add(Item... items) {
        return add(true, asList(items));
    }

    /**
     * add a list of items to the end of the existing items
     * will prior check if we are currently filtering
     *
     * @param items the items to add
     */
    ///[isPublishResults]
    public ModelAdapter<?, Item> add(boolean isPublishResults, List<Item> items) {
        if (mOriginalItems != null && items.size() > 0) {
            if (mItemAdapter.isUseIdDistributor()) {
                mItemAdapter.getIdDistributor().checkIds(items);
            }

            mOriginalItems.addAll(items);

            if (isPublishResults) {
                publishResults(mConstraint, performFiltering(mConstraint));
            }

            return mItemAdapter;
        } else {
            return mItemAdapter.addInternal(items);
        }
    }
    public ModelAdapter<?, Item> add(List<Item> items) {
        return add(true, items);
    }

    /**
     * add an array of items at the given position within the existing items
     *
     * @param position the global position
     * @param items    the items to add
     */
    ///[isPublishResults]
    @SafeVarargs
    public final ModelAdapter<?, Item> add(boolean isPublishResults, int position, Item... items) {
        return add(isPublishResults, position, asList(items));
    }
    @SafeVarargs
    public final ModelAdapter<?, Item> add(int position, Item... items) {
        return add(true, position, asList(items));
    }

    /**
     * add a list of items at the given position within the existing items
     *
     * @param position the global position
     * @param items    the items to add
     */
    ///[isPublishResults]
    public ModelAdapter<?, Item> add(boolean isPublishResults, int position, List<Item> items) {
        if (mOriginalItems != null && items.size() > 0) {
            if (mItemAdapter.isUseIdDistributor()) {
                mItemAdapter.getIdDistributor().checkIds(items);
            }

            ///[FIX#ItemFilter#global position]
//            mOriginalItems.addAll(getAdapterPosition(mItemAdapter.getAdapterItems().get(position)) - mItemAdapter.getFastAdapter().getPreItemCount(position), items);
            mOriginalItems.addAll(getAdapterPosition(mItemAdapter.getAdapterItems().get(position - mItemAdapter.getFastAdapter().getPreItemCount(position))), items);

            if (isPublishResults) {
                publishResults(mConstraint, performFiltering(mConstraint));
            }

            return mItemAdapter;
        } else {
            return mItemAdapter.addInternal(position, items);
        }
    }
    public ModelAdapter<?, Item> add(int position, List<Item> items) {
        return add(true, position, items);
    }

    ///[UPGRADE#xxxInAdapter()]
    /**
     * add a list of items at the given position within the existing items
     *
     * @param index the relative position
     * @param items    the items to add
     */
    ///[isPublishResults]
    public ModelAdapter<?, Item> addInAdapter(boolean isPublishResults, int index, List<Item> items) {
        if (mOriginalItems != null && items.size() > 0) {
            if (mItemAdapter.isUseIdDistributor()) {
                mItemAdapter.getIdDistributor().checkIds(items);
            }

            mOriginalItems.addAll(index, items);

            if (isPublishResults) {
                publishResults(mConstraint, performFiltering(mConstraint));
            }

            return mItemAdapter;
        } else {
            return mItemAdapter.addInternalInAdapter(index, items);
        }
    }
    public ModelAdapter<?, Item> addInAdapter(int index, List<Item> items) {
        return addInAdapter(true, index, items);
    }

    /**
     * sets an item at the given position, overwriting the previous item
     *
     * @param position the global position
     * @param item     the item to set
     */
    ///[isPublishResults]
    public ModelAdapter<?, Item> set(boolean isPublishResults, int position, Item item) {
        if (mOriginalItems != null) {
            if (mItemAdapter.isUseIdDistributor()) {
                mItemAdapter.getIdDistributor().checkId(item);
            }

            ///[FIX#ItemFilter#global position]
//            mOriginalItems.set(getAdapterPosition(mItemAdapter.getAdapterItems().get(position)) - mItemAdapter.getFastAdapter().getPreItemCount(position), item);
            mOriginalItems.set(getAdapterPosition(mItemAdapter.getAdapterItems().get(position - mItemAdapter.getFastAdapter().getPreItemCount(position))), item);

            if (isPublishResults) {
                publishResults(mConstraint, performFiltering(mConstraint));
            }

            return mItemAdapter;
        } else {
            return mItemAdapter.setInternal(position, item);
        }
    }
    public ModelAdapter<?, Item> set(int position, Item item) {
        return set(true, position, item);
    }

    ///[UPGRADE#xxxInAdapter()]
    /**
     * sets an item at the given position, overwriting the previous item
     *
     * @param index the relative position
     * @param item     the item to set
     */
    ///[isPublishResults]
    public ModelAdapter<?, Item> setInAdapter(boolean isPublishResults, int index, Item item) {
        if (mOriginalItems != null) {
            if (mItemAdapter.isUseIdDistributor()) {
                mItemAdapter.getIdDistributor().checkId(item);
            }

            mOriginalItems.set(index, item);

            if (isPublishResults) {
                publishResults(mConstraint, performFiltering(mConstraint));
            }

            return mItemAdapter;
        } else {
            return mItemAdapter.setInternalInAdapter(index, item);
        }
    }
    public ModelAdapter<?, Item> setInAdapter(int index, Item item) {
        return setInAdapter(true, index, item);
    }

    /**
     * moves an item within the list from a position to a position
     *
     * @param fromPosition the position global from which we want to move
     * @param toPosition   the global position to which to move
     * @return this
     */
    ///[isPerformFiltering]
    public ModelAdapter<?, Item> move(boolean isPerformFiltering, int fromPosition, int toPosition) {
        if (mOriginalItems != null) {
            int preItemCount = mItemAdapter.getFastAdapter().getPreItemCount(fromPosition);

            ///[FIX#ItemFilter#global position]
//            int adjustedFrom = getAdapterPosition(mItemAdapter.getAdapterItems().get(fromPosition));
//            int adjustedTo = getAdapterPosition(mItemAdapter.getAdapterItems().get(toPosition));
//            Item item = mOriginalItems.get(adjustedFrom - preItemCount);
//            mOriginalItems.remove(adjustedFrom - preItemCount);
//            mOriginalItems.add(adjustedTo - preItemCount, item);
            int adjustedFrom = getAdapterPosition(mItemAdapter.getAdapterItems().get(fromPosition - preItemCount));
            int adjustedTo = getAdapterPosition(mItemAdapter.getAdapterItems().get(toPosition - preItemCount));
            Item item = mOriginalItems.get(adjustedFrom);
            mOriginalItems.remove(adjustedFrom);
            mOriginalItems.add(adjustedTo, item);

            if (isPerformFiltering) {
                performFiltering(mConstraint);
            }

            return mItemAdapter;
        } else {
            return mItemAdapter.move(fromPosition, toPosition);
        }
    }
    public ModelAdapter<?, Item> move(int fromPosition, int toPosition) {
        return move(true, fromPosition, toPosition);
    }

    /**
     * removes an item at the given position within the existing icons
     *
     * @param position the global position
     */
    ///[isPublishResults]
    public ModelAdapter<?, Item> remove(boolean isPublishResults, int position) {
        if (mOriginalItems != null) {

            ///[FIX#ItemFilter#global position]
//            mOriginalItems.remove(getAdapterPosition(mItemAdapter.getAdapterItems().get(position)) - mItemAdapter.getFastAdapter().getPreItemCount(position));
            mOriginalItems.remove(getAdapterPosition(mItemAdapter.getAdapterItems().get(position - mItemAdapter.getFastAdapter().getPreItemCount(position))));

            if (isPublishResults) {
                publishResults(mConstraint, performFiltering(mConstraint));
            }

            return mItemAdapter;
        } else {
            return mItemAdapter.remove(position);
        }
    }
    public ModelAdapter<?, Item> remove(int position) {
        return remove(true, position);
    }

    ///[UPGRADE#xxxInAdapter()]
    /**
     * removes an item at the given position within the existing icons
     *
     * @param index the relative position
     */
    ///[isPublishResults]
    public ModelAdapter<?, Item> removeInAdapter(boolean isPublishResults, int index) {
        if (mOriginalItems != null) {
            mOriginalItems.remove(index);

            if (isPublishResults) {
                publishResults(mConstraint, performFiltering(mConstraint));
            }

            return mItemAdapter;
        } else {
            return mItemAdapter.removeInAdapter(index);
        }
    }
    public ModelAdapter<?, Item> removeInAdapter(int index) {
        return removeInAdapter(true, index);
    }

    ///[UPGRADE#remove(Item item)]
    /**
     * removes an item
     *
     * @param item     the item to remove
     */
    ///[isPublishResults]
    public ModelAdapter<?, Item> remove(boolean isPublishResults, Item item) {
        if (mOriginalItems != null) {
            mOriginalItems.remove(item);

            if (isPublishResults) {
                publishResults(mConstraint, performFiltering(mConstraint));
            }

            return mItemAdapter;
        } else {
            return mItemAdapter.remove(item);
        }
    }
    public ModelAdapter<?, Item> remove(Item item) {
        return remove(true, item);
    }

    ///[UPGRADE#removeByIdentifier(final long identifier)]
    /**
     * remvoes an item by it's identifier
     *
     * @param identifier the identifier to search for
     * @return this
     */
    ///[isPublishResults]
    public ModelAdapter<?, Item> removeByIdentifier(boolean isPublishResults, final long identifier) {
        if (mOriginalItems != null) {
            for (Item item : mOriginalItems) {
                if (item.getIdentifier() == identifier) {
                    mOriginalItems.remove(item);
                    break;
                }
            }

            if (isPublishResults) {
                publishResults(mConstraint, performFiltering(mConstraint));
            }
        } else {
            mItemAdapter.removeByIdentifier(identifier);
        }

        return mItemAdapter;
    }
    public ModelAdapter<?, Item> removeByIdentifier(final long identifier) {
        return removeByIdentifier(true, identifier);
    }

    /**
     * removes a range of items starting with the given position within the existing icons
     *
     * @param position  the global position
     * @param itemCount the count of items which were removed
     */
    ///[isPublishResults]
    public ModelAdapter<?, Item> removeRange(boolean isPublishResults, int position, int itemCount) {
        if (mOriginalItems != null) {
            int length = mOriginalItems.size();
            int preItemCount = mItemAdapter.getFastAdapter().getPreItemCount(position);
            //make sure we do not delete to many items
            int saveItemCount = Math.min(itemCount, length - position + preItemCount);
            for (int i = 0; i < saveItemCount; i++) {

                ///[FIX#ItemFilter#global position]
//                mOriginalItems.remove(position - preItemCount);
                mOriginalItems.remove(mItemAdapter.getAdapterItems().get(position - preItemCount));

            }

            if (isPublishResults) {
                publishResults(mConstraint, performFiltering(mConstraint));
            }

            return mItemAdapter;
        } else {
            return mItemAdapter.removeRange(position, itemCount);
        }
    }
    public ModelAdapter<?, Item> removeRange(int position, int itemCount) {
        return removeRange(true, position, itemCount);
    }

    /**
     * removes a range of items starting with the given position within the existing icons
     *
     * @param index  the relative position
     * @param itemCount the count of items which were removed
     */
    ///[isPublishResults]
    public ModelAdapter<?, Item> removeRangeInAdapter(boolean isPublishResults, int index, int itemCount) {
        if (mOriginalItems != null) {
            int length = mOriginalItems.size();
            //make sure we do not delete to many items
            int saveItemCount = Math.min(itemCount, length - index);
            for (int i = 0; i < saveItemCount; i++) {
                mOriginalItems.remove(index + i);
            }

            if (isPublishResults) {
                publishResults(mConstraint, performFiltering(mConstraint));
            }

            return mItemAdapter;
        } else {
            return mItemAdapter.removeRangeInAdapter(index, itemCount);
        }
    }
    public ModelAdapter<?, Item> removeRangeInAdapter(int index, int itemCount) {
        return removeRangeInAdapter(true, index, itemCount);
    }

    /**
     * removes all items of this adapter
     */
    ///[isPublishResults]
    public ModelAdapter<?, Item> clear(boolean isPublishResults) {
        if (mOriginalItems != null) {
            mOriginalItems.clear();

            if (isPublishResults) {
                publishResults(mConstraint, performFiltering(mConstraint));
            }

            return mItemAdapter;
        } else {
            return mItemAdapter.clear();
        }
    }
    public ModelAdapter<?, Item> clear() {
        return clear(true);
    }

    ///[UPGRADE#ItemFilter#reset()]
    public void reset() {
        mOriginalItems = null;
        mConstraint = null;
    }

}
