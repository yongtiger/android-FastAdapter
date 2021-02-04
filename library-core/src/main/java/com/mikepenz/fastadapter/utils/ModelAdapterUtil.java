package com.mikepenz.fastadapter.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArraySet;
import androidx.recyclerview.widget.RecyclerView;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.adapters.ModelAdapter;
import com.mikepenz.fastadapter.select.SelectExtension;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

public abstract class ModelAdapterUtil {

    @NonNull
    public static <Item extends IItem> List<Item> getAdapterItems(@Nullable List<ItemAdapter<Item>> mItemAdapters, @Nullable ItemAdapter<Item> adapter) {
        if (mItemAdapters == null && adapter == null) {
            return new ArrayList<>();
        }

        if (adapter == null) {
            return getAllAdapterItems(mItemAdapters);
        } else {
            return adapter.getAdapterItems();
        }
    }

    @NonNull
    public static <Item extends IItem> Set<Integer> getAdapterItemPositions(@Nullable List<ItemAdapter<Item>> mItemAdapters, @Nullable ItemAdapter<Item> adapter) {
        final Set<Integer> positions = new ArraySet<>();
        if (mItemAdapters == null && adapter == null) {
            return positions;
        }

        if (adapter == null) {
            positions.addAll(getAllAdapterItemPositions(mItemAdapters));
        } else {
            for (Item item : adapter.getAdapterItems()) {
                positions.add(adapter.getGlobalPosition(adapter.getAdapterPosition(item))); ///[时间复杂度]O(adapter个数 + adapter中item个数)
            }
        }

        return positions;
    }

    @NonNull
    public static <Item extends IItem> List<Item> getAllAdapterItems(@NonNull List<ItemAdapter<Item>> mItemAdapters) {
        final List<Item> items = new ArrayList<>();
        for (ItemAdapter<Item> itemAdapter : mItemAdapters) {
            items.addAll(itemAdapter.getAdapterItems());
        }
        return items;
    }

    @NonNull
    public static <Item extends IItem> Set<Integer> getAllAdapterItemPositions(@NonNull List<ItemAdapter<Item>> mItemAdapters) {
        final Set<Integer> positions = new ArraySet<>();
        for (ItemAdapter<Item> itemAdapter : mItemAdapters) {
            for (Item item : itemAdapter.getAdapterItems()) {
                positions.add(itemAdapter.getGlobalPosition(itemAdapter.getAdapterPosition(item)));
            }
        }
        return positions;
    }

    public static <Item extends IItem> void clear(boolean isPublishResults, @Nullable List<ItemAdapter<Item>> mItemAdapters, @Nullable ModelAdapter<Item, Item> adapter) {
        if (mItemAdapters == null && adapter == null) {
            return;
        }

        if (adapter == null) {
            for (ItemAdapter<Item> itemAdapter : mItemAdapters) {
                clear(isPublishResults, itemAdapter);
            }
        } else {
            clear(isPublishResults, adapter);
        }
    }

    public static <Item extends IItem> void clear(@Nullable List<ItemAdapter<Item>> mItemAdapters, @Nullable ModelAdapter<Item, Item> adapter) {
        clear(true, mItemAdapters, adapter);
    }

    public static <Item extends IItem> void remove(boolean isPublishResults, @Nullable List<ItemAdapter<Item>> mItemAdapters, @Nullable ModelAdapter<Item, Item> adapter, Item item) {
        if (mItemAdapters == null && adapter == null) {
            return;
        }

        if (adapter == null) {
            for (ItemAdapter<Item> itemAdapter : mItemAdapters) {
                remove(isPublishResults, itemAdapter, item);
            }
        } else {
            remove(isPublishResults, adapter, item);
        }
    }
    public static <Item extends IItem> void remove(@Nullable List<ItemAdapter<Item>> mItemAdapters, @Nullable ModelAdapter<Item, Item> adapter, Item item) {
        remove(true, mItemAdapters, adapter, item);
    }

    public static <Item extends IItem> void removeItems(boolean isPublishResults, @Nullable List<ItemAdapter<Item>> mItemAdapters, @Nullable ModelAdapter<Item, Item> adapter, @Nullable List<Item> items) {
        if (mItemAdapters == null && adapter == null || items == null || items.isEmpty()) {
            return;
        }

        if (adapter == null) {
            for (ItemAdapter<Item> itemAdapter : mItemAdapters) {
                removeItems(isPublishResults, mItemAdapters, itemAdapter, items);
            }
        } else {
            for (Item item : items) {
                remove(isPublishResults,mItemAdapters, adapter, item);
            }
        }
    }
    public static <Item extends IItem> void removeItems(@Nullable List<ItemAdapter<Item>> mItemAdapters, @Nullable ModelAdapter<Item, Item> adapter, @Nullable List<Item> items) {
        removeItems(false, mItemAdapters, adapter, items);
    }


    @NonNull
    public static <Item extends IItem> List<Item> getSelectedItems(@Nullable ModelAdapter<Item, Item> adapter, @NonNull FastAdapter<Item> fastAdapter) {
        final List<Item> selectedItems;
        if (adapter == null) {
            selectedItems = fastAdapter.getSelectedItems();
        } else {
            if (adapter.getItemFilter() == null) {
                ///[FIX#ItemFilter/ModelAdapter#getSelections()/getSelectedItems()]
                selectedItems = adapter.getSelectedItems();
            } else {
                selectedItems = adapter.getItemFilter().getSelectedItems();
            }
        }

        return selectedItems;
    }

    @NonNull
    public static <Item extends IItem> Set<Integer> getSelections(@Nullable ModelAdapter<Item, Item> adapter, @NonNull FastAdapter<Item> fastAdapter) {
        final Set<Integer> selections;
        if (adapter == null) {
            selections = fastAdapter.getSelections();
        } else {
            if (adapter.getItemFilter() == null) {
                ///[FIX#ItemFilter/ModelAdapter#getSelections()/getSelectedItems()]
                selections = adapter.getSelections();
            } else {
                selections = adapter.getItemFilter().getSelections();
            }
        }

        return selections;
    }

    public static <Item extends IItem> void selectAll(@Nullable ModelAdapter<Item, Item> adapter, @NonNull FastAdapter<Item> fastAdapter) {
        final SelectExtension<Item> selectExtension = fastAdapter.getExtension(SelectExtension.class);
        if (selectExtension == null) {
            return;
        }

        if (adapter == null) {
            selectExtension.select(true);
        } else {
            for (Item item : adapter.getAdapterItems()) {
                selectExtension.select(fastAdapter.getPosition(item));
            }
        }
    }

    public static <Item extends IItem> void deselectAll(@Nullable ModelAdapter<Item, Item> adapter, @NonNull FastAdapter<Item> fastAdapter) {
        final SelectExtension<Item> selectExtension = fastAdapter.getExtension(SelectExtension.class);
        if (selectExtension == null) {
            return;
        }

        if (adapter == null) {
            selectExtension.deselect();
        } else {
            for (Item item : adapter.getAdapterItems()) {
                selectExtension.deselect(fastAdapter.getPosition(item));
            }
        }
    }

    public static <Item extends IItem> void toggleAll(@Nullable ModelAdapter<Item, Item> adapter, @NonNull FastAdapter<Item> fastAdapter) {
        final SelectExtension<Item> selectExtension = fastAdapter.getExtension(SelectExtension.class);
        if (selectExtension == null) {
            return;
        }

        if (adapter == null) {
            selectExtension.toggleSelection();
        } else {
            for (Item item : adapter.getAdapterItems()) {
                selectExtension.toggleSelection(fastAdapter.getPosition(item));
            }
        }
    }


    public static <Item extends IItem> void compare(@NonNull ModelAdapter<Item, Item> adapter, @Nullable Comparator<Item> comparator) {
        if (adapter.getItemList() == null || !(adapter.getItemList() instanceof ComparableItemListImpl)) {
            return;
        }

        ///注意：withComparator(getComparator(), true, false)的第三个参数！为了不重复进行FastAdapter().notifyAdapterDataSetChanged()
        if (adapter.getItemFilter() == null || adapter.getItemFilter().getOriginalItems() == null) {
            ((ComparableItemListImpl<Item>) adapter.getItemList()).withComparator(comparator, true, true);
        } else {
            ((ComparableItemListImpl<Item>) adapter.getItemList()).withComparator(comparator, false, false);
            adapter.getItemFilter().sortOriginalItems();
            adapter.getItemFilter().publishResults();
        }
    }


    public static <Item extends IItem> List<Item> getAdapterOriginalItems(@NonNull ModelAdapter<Item, Item> adapter) {
        if (adapter.getItemFilter() == null || adapter.getItemFilter().getOriginalItems() == null) {
            return adapter.getAdapterItems();
        } else {
            return adapter.getItemFilter().getOriginalItems();
        }
    }


    public static <Item extends IItem> void add(boolean isPublishResults, @Nullable ModelAdapter<Item, Item> adapter, Item... items) {
        if (adapter == null || items.length == 0) {
            return;
        }

        add(isPublishResults, adapter, asList(items));
    }
    public static <Item extends IItem> void add(@Nullable ModelAdapter<Item, Item> adapter, Item... items) {
        if (adapter == null || items.length == 0) {
            return;
        }

        add(true, adapter, asList(items));
    }

    public static <Item extends IItem> void add(boolean isPublishResults, @Nullable ModelAdapter<Item, Item> adapter, @Nullable List<Item> items) {
        if (adapter == null || items == null || items.size() == 0) {
            return;
        }

        ///[FastAdapter#Filter]用mItemAdapter.getItemFilter()的add/remove方法替代mItemAdapter的方法
        if (adapter.getItemFilter() == null) {
            adapter.add(items);
        } else {
            ///注意：FastAdapter必须升级为v0.10.7#[FIX#ItemFilter#Sort]以上！否则mOriginalItems无法排序
            adapter.getItemFilter().add(isPublishResults, items);
        }
    }
    public static <Item extends IItem> void add(@Nullable ModelAdapter<Item, Item> adapter, @Nullable List<Item> items) {
        add(true, adapter, items);
    }

    public static <Item extends IItem> void add(boolean isPublishResults, @Nullable ModelAdapter<Item, Item> adapter, int position, Item... items) {
        if (adapter == null || position == RecyclerView.NO_POSITION || items.length == 0) {
            return;
        }

        add(isPublishResults, adapter, position, asList(items));
    }
    public static <Item extends IItem> void add(@Nullable ModelAdapter<Item, Item> adapter, int position, Item... items) {
        if (adapter == null || position == RecyclerView.NO_POSITION || items.length == 0) {
            return;
        }

        add(true, adapter, position, asList(items));
    }

    public static <Item extends IItem> void add(boolean isPublishResults, @Nullable ModelAdapter<Item, Item> adapter, int position, @Nullable List<Item> items) {
        if (adapter == null || position == RecyclerView.NO_POSITION || items == null || items.size() == 0) {
            return;
        }

        ///[FastAdapter#Filter]用mItemAdapter.getItemFilter()的add/remove方法替代mItemAdapter的方法
        if (adapter.getItemFilter() == null) {
            adapter.add(position, items);
        } else {
            ///注意：FastAdapter必须升级为v0.10.7#[FIX#ItemFilter#Sort]以上！否则mOriginalItems无法排序
            adapter.getItemFilter().add(isPublishResults, position, items);
        }
    }
    public static <Item extends IItem> void add(@Nullable ModelAdapter<Item, Item> adapter, int position, @Nullable List<Item> items) {
        add(true, adapter, position, items);
    }

    @SafeVarargs
    public static <Item extends IItem> void addInAdapter(boolean isPublishResults, @Nullable ModelAdapter<Item, Item> adapter, int index, Item... items) {
        if (adapter == null || items.length == 0) {
            return;
        }

        addInAdapter(isPublishResults, adapter, index, asList(items));
    }
    @SafeVarargs
    public static <Item extends IItem> void addInAdapter(@Nullable ModelAdapter<Item, Item> adapter, int index, Item... items) {
        if (adapter == null || items.length == 0) {
            return;
        }

        addInAdapter(true, adapter, index, asList(items));
    }

    public static <Item extends IItem> void addInAdapter(boolean isPublishResults, @Nullable ModelAdapter<Item, Item> adapter, int index, @Nullable List<Item> items) {
        if (adapter == null || items == null || items.size() == 0) {
            return;
        }

        ///[FastAdapter#Filter]用mItemAdapter.getItemFilter()的add/remove方法替代mItemAdapter的方法
        if (adapter.getItemFilter() == null) {
            adapter.addInAdapter(index, items);
        } else {
            ///注意：FastAdapter必须升级为v0.10.7#[FIX#ItemFilter#Sort]以上！否则mOriginalItems无法排序
            adapter.getItemFilter().addInAdapter(isPublishResults, index, items);
        }
    }
    public static <Item extends IItem> void addInAdapter(@Nullable ModelAdapter<Item, Item> adapter, int index, @Nullable List<Item> items) {
        addInAdapter(true, adapter, index, items);
    }

    public static <Item extends IItem> void set(boolean isPublishResults, @Nullable ModelAdapter<Item, Item> adapter, int position, @Nullable Item item) {
        if (adapter == null || position == RecyclerView.NO_POSITION) {
            return;
        }

        if (item == null) {
            remove(adapter, position);
            return;
        }

        ///[FastAdapter#Filter]用mItemAdapter.getItemFilter()的add/remove方法替代mItemAdapter的方法
        if (adapter.getItemFilter() == null) {
            adapter.set(position, item);
        } else {
            ///注意：FastAdapter必须升级为v0.10.7#[FIX#ItemFilter#Sort]以上！否则mOriginalItems无法排序
            adapter.getItemFilter().set(isPublishResults, position, item);
        }
    }
    public static <Item extends IItem> void set(@Nullable ModelAdapter<Item, Item> adapter, int position, @Nullable Item item) {
        set(true, adapter, position, item);
    }

    public static <Item extends IItem> void setInAdapter(boolean isPublishResults, @Nullable ModelAdapter<Item, Item> adapter, int index, @Nullable Item item) {
        if (adapter == null) {
            return;
        }

        if (item == null) {
            remove(isPublishResults, adapter, index);
            return;
        }

        ///[FastAdapter#Filter]用mItemAdapter.getItemFilter()的add/remove方法替代mItemAdapter的方法
        if (adapter.getItemFilter() == null) {
            adapter.setInAdapter(index, item);
        } else {
            ///注意：FastAdapter必须升级为v0.10.7#[FIX#ItemFilter#Sort]以上！否则mOriginalItems无法排序
            adapter.getItemFilter().setInAdapter(isPublishResults, index, item);
        }
    }
    public static <Item extends IItem> void setInAdapter(@Nullable ModelAdapter<Item, Item> adapter, int index, @Nullable Item item) {
        setInAdapter(true, adapter, index, item);
    }

    public static <Item extends IItem> void move(@Nullable ModelAdapter<Item, Item> adapter, int fromPosition, int toPosition) {
        if (adapter == null
                || fromPosition == RecyclerView.NO_POSITION
                || toPosition == RecyclerView.NO_POSITION
                || fromPosition == toPosition) {
            return;
        }

        ///[FastAdapter#Filter]用mItemAdapter.getItemFilter()的add/remove方法替代mItemAdapter的方法
        if (adapter.getItemFilter() == null) {
            adapter.move(fromPosition, toPosition);
        } else {
            ///注意：FastAdapter必须升级为v0.10.7#[FIX#ItemFilter#Sort]以上！否则mOriginalItems无法排序
            adapter.getItemFilter().move(fromPosition, toPosition);
        }
    }

    public static <Item extends IItem> void clear(boolean isPublishResults, @NonNull ModelAdapter<Item, Item> adapter) {
        if (adapter.getItemFilter() == null) {
            adapter.clear();
        } else {
            adapter.getItemFilter().clear(isPublishResults);
        }
    }
    public static <Item extends IItem> void clear(@NonNull ModelAdapter<Item, Item> adapter) {
        clear(true, adapter);
    }

    public static <Item extends IItem> void remove(boolean isPublishResults, @Nullable ModelAdapter<Item, Item> adapter, int position) {
        if (adapter == null || position == RecyclerView.NO_POSITION) {
            return;
        }

        if (adapter.getItemFilter() == null) {
            adapter.remove(position);
        } else {
            adapter.getItemFilter().remove(isPublishResults, position);
        }
    }
    public static <Item extends IItem> void remove(@Nullable ModelAdapter<Item, Item> adapter, int position) {
        remove(true, adapter, position);
    }

    public static <Item extends IItem> void removeInAdapter(boolean isPublishResults, @Nullable ModelAdapter<Item, Item> adapter, int index) {
        if (adapter == null) {
            return;
        }

        if (adapter.getItemFilter() == null) {
            adapter.removeInAdapter(index);
        } else {
            adapter.getItemFilter().removeInAdapter(isPublishResults, index);
        }
    }
    public static <Item extends IItem> void removeInAdapter(@Nullable ModelAdapter<Item, Item> adapter, int index) {
        removeInAdapter(true, adapter, index);
    }

    public static <Item extends IItem> void remove(boolean isPublishResults, @Nullable ModelAdapter<Item, Item> adapter, Item item) {
        if (adapter == null) {
            return;
        }

        if (adapter.getItemFilter() == null) {
            adapter.remove(item);
        } else {
            adapter.getItemFilter().remove(isPublishResults, item);
        }
    }
    public static <Item extends IItem> void remove(@Nullable ModelAdapter<Item, Item> adapter, Item item) {
        remove(true, adapter, item);
    }

    public static <Item extends IItem> void removeByIdentifier(boolean isPublishResults, @Nullable ModelAdapter<Item, Item> adapter, final long identifier) {
        if (adapter == null || identifier == -1L) {
            return;
        }

        if (adapter.getItemFilter() == null) {
            adapter.removeByIdentifier(identifier);
        } else {
            adapter.getItemFilter().removeByIdentifier(isPublishResults, identifier);
        }
    }
    public static <Item extends IItem> void removeByIdentifier(@Nullable ModelAdapter<Item, Item> adapter, final long identifier) {
        removeByIdentifier(true, adapter, identifier);
    }

    public static <Item extends IItem> void removeRange(boolean isPublishResults, @Nullable ModelAdapter<Item, Item> adapter, int position, int itemCount) {
        if (adapter == null || position == RecyclerView.NO_POSITION || itemCount <= 0) {
            return;
        }

        if (adapter.getItemFilter() == null) {
            adapter.removeRange(position, itemCount);
        } else {
            adapter.getItemFilter().removeRange(isPublishResults, position, itemCount);
        }
    }
    public static <Item extends IItem> void removeRange(@Nullable ModelAdapter<Item, Item> adapter, int position, int itemCount) {
        removeRange(true, adapter, position, itemCount);
    }

    public static <Item extends IItem> void removeRangeInAdapter(boolean isPublishResults, @Nullable ModelAdapter<Item, Item> adapter, int index, int itemCount) {
        if (adapter == null || itemCount <= 0) {
            return;
        }

        if (adapter.getItemFilter() == null) {
            adapter.removeRangeInAdapter(index, itemCount);
        } else {
            adapter.getItemFilter().removeRangeInAdapter(isPublishResults, index, itemCount);
        }
    }
    public static <Item extends IItem> void removeRangeInAdapter(@Nullable ModelAdapter<Item, Item> adapter, int index, int itemCount) {
        removeRangeInAdapter(true, adapter,index, itemCount);
    }

}
