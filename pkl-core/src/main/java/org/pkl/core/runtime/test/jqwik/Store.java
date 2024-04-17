/**
 * Copyright © 2024 Apple Inc. and the Pkl project authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pkl.core.runtime.test.jqwik;

import java.util.Optional;
import java.util.function.*;

/** Experimental feature. Not ready for public usage yet. */
public interface Store<T> {

  T get();

  Lifespan lifespan();

  void update(Function<T, T> updater);

  void reset();

  class StoreFacade {
    private static final Store.StoreFacade implementation;

    static {
      implementation = new StoreFacade();
    }

    public <T> Store<T> create(
        Object identifier, Lifespan lifespan, Supplier<T> initialValueSupplier) {
      TestDescriptor scope = CurrentTestDescriptor.get();
      return StoreRepository.getCurrent().create(scope, identifier, lifespan, initialValueSupplier);
    }

    public <T> Store<T> get(Object identifier) {
      TestDescriptor retriever = CurrentTestDescriptor.get();
      Optional<? extends Store<T>> store = StoreRepository.getCurrent().get(retriever, identifier);
      return store.orElseThrow(
          () -> new CannotFindStoreException(identifier, retriever.getUniqueId().toString()));
    }

    public <T> Store<T> free(Supplier<T> initialValueSupplier) {
      return new Store<T>() {
        T t = initialValueSupplier.get();

        @Override
        public T get() {
          return t;
        }

        @Override
        public Lifespan lifespan() {
          return Lifespan.RUN;
        }

        @Override
        public void update(Function<T, T> updater) {
          t = updater.apply(t);
        }

        @Override
        public void reset() {
          t = initialValueSupplier.get();
        }
      };
    }
  }

  /**
   * Any value that implements this interface will automatically be closed when its store goes out
   * of scope. That scope is defined by the store's {@linkplain #lifespan()}
   */
  interface CloseOnReset {
    void close() throws Exception;
  }

  /**
   * Create a new store for storing and retrieving values and objects in lifecycle hooks and
   * lifecycle-dependent methods.
   *
   * <p>Stores are created with respect to the current test / property. Therefore you _must not save
   * created stores in member variables_, unless the containing object is unique per test /
   * property.
   *
   * @param <T> The type of object to store
   * @param identifier Any object to identify a store. Must be globally unique and stable, i.e.
   *     hashCode and equals must not change.
   * @param lifespan A stored object's lifespan
   * @param initialValueSupplier Supplies the value to be used for initializing the store depending
   *     on its lifespan
   * @return New store instance
   */
  static <T> Store<T> create(
      Object identifier, Lifespan lifespan, Supplier<T> initialValueSupplier) {
    return StoreFacade.implementation.create(identifier, lifespan, initialValueSupplier);
  }

  /**
   * Find an existing store or create a new one if it doesn't exist.
   *
   * <p>Stores are created with respect to the current test / property. Therefore you _must not save
   * created stores in member variables_, unless the containing object is unique per test /
   * property.
   *
   * @param <T> The type of object to store
   * @param identifier Any object to identify a store. Must be globally unique and stable, i.e.
   *     hashCode and equals must not change.
   * @param lifespan A stored object's lifespan
   * @param initialValueSupplier Supplies the value to be used for initializing the store depending
   *     on its lifespan
   * @return New or existing store instance
   */
  static <T> Store<T> getOrCreate(
      Object identifier, Lifespan lifespan, Supplier<T> initialValueSupplier) {
    try {
      Store<T> store = Store.get(identifier);
      if (!store.lifespan().equals(lifespan)) {
        String message =
            String.format(
                "Trying to recreate existing store [%s] with different lifespan [%s]",
                store, lifespan);
        throw new JqwikException(message);
      }
      return store;
    } catch (CannotFindStoreException cannotFindStore) {
      return Store.create(identifier, lifespan, initialValueSupplier);
    }
  }

  /**
   * Retrieve a store that must be created somewhere else.
   *
   * @param identifier Any object to identify a store. Must be globally unique and stable, i.e.
   *     hashCode and equals must not change.
   * @param <T> The type of object to store
   * @return Existing store instance
   * @throws CannotFindStoreException
   */
  static <T> Store<T> get(Object identifier) {
    return StoreFacade.implementation.get(identifier);
  }

  /**
   * Create a "free" store, i.e. one that lives independently from a test run, property or try.
   *
   * @param <T> The type of object to store
   * @param initializer Supplies the value to be used for initializing the store depending on its
   *     lifespan
   * @return New store instance
   */
  static <T> Store<T> free(Supplier<T> initializer) {
    return StoreFacade.implementation.free(initializer);
  }
}
