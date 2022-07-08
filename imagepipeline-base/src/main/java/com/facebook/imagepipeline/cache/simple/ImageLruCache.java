/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.imagepipeline.cache.simple;

import com.facebook.common.internal.Predicate;
import com.facebook.common.references.CloseableReference;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.infer.annotation.Nullsafe;
import java.util.Iterator;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
@Nullsafe(Nullsafe.Mode.LOCAL)
class ImageLruCache<K> extends ExtendedLruCache<K, SizedEntry> {

  /**
   * @param maxSize for caches that do not override {@link #sizeOf}, this is the maximum number of
   *     entries in the cache. For all other caches, this is the maximum sum of the sizes of the
   *     entries in this cache.
   */
  public ImageLruCache(int maxSize) {
    super(maxSize);
  }

  @Override
  protected int sizeOf(K key, SizedEntry value) {
    return value.size;
  }

  /** @return number of elements currently in cache */
  public synchronized int count() {
    return putCount() - evictionCount();
  }

  public synchronized int removeAll(Predicate<K> predicate) {
    int count = 0;
    Iterator<K> iter = map.keySet().iterator();
    while (iter.hasNext()) {
      K key = iter.next();
      if (predicate.apply(key)) {
        iter.remove();
        count++;
      }
    }
    return count;
  }

  public synchronized boolean contains(Predicate<K> predicate) {
    for (K key : map.keySet()) {
      if (predicate.apply(key)) {
        return true;
      }
    }
    return false;
  }

  public synchronized @Nullable CloseableImage inspect(K key) {
    for (Map.Entry<K, SizedEntry> entry : map.entrySet()) {
      if (entry.getKey().equals(key)) {
        CloseableReference<CloseableImage> ref = entry.getValue().value;
        return ref.get();
      }
    }
    return null;
  }
}