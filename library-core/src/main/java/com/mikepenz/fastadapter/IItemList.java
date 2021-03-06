package com.mikepenz.fastadapter;

import java.util.List;

import androidx.annotation.Nullable;

/**
 * The Item list interface
 */

public interface IItemList<Item> {

    int getAdapterIndex(long identifier);

    void remove(int position, int preItemCount);

    ///[UPGRADE#remove(Item item)]
    void remove(Item item, int position);

    ///[UPGRADE#removeByIdentifier(long identifier)]
    void removeByIdentifier(long identifier, int position);

    void removeRange(int position, int itemCount, int preItemCount);

    void move(int fromPosition, int toPosition, int preItemCount);

    int size();

    void clear(int position);

    boolean isEmpty();

    void addAll(List<Item> items, int position);

    void set(int position, Item item, int preItemCount);

    void set(List<Item> items, int preItemCount, @Nullable IAdapterNotifier adapterNotifier);

    void setNewList(List<Item> items, boolean notify);

    void addAll(int position, List<Item> items, int preItemCount);

    List<Item> getItems();

    Item get(int position);
}
