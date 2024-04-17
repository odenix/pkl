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

import static org.pkl.core.runtime.test.jqwik.JqwikStringSupport.displayString;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ScopedStore<T> implements Store<T> {

  private static final Logger LOG = Logger.getLogger(ScopedStore.class.getName());

  private final Object identifier;
  private final Lifespan lifespan;
  private final TestDescriptor scope;
  private final Supplier<T> initialValueSupplier;

  private T value;
  private boolean initialized = false;

  public ScopedStore(
      Object identifier,
      Lifespan lifespan,
      TestDescriptor scope,
      Supplier<T> initialValueSupplier) {
    this.identifier = identifier;
    this.lifespan = lifespan;
    this.scope = scope;
    this.initialValueSupplier = initialValueSupplier;
  }

  @Override
  public synchronized T get() {
    if (!initialized) {
      value = initialValueSupplier.get();
      initialized = true;
    }
    return value;
  }

  @Override
  public Lifespan lifespan() {
    return lifespan;
  }

  @Override
  public synchronized void update(Function<T, T> updater) {
    value = updater.apply(get());
  }

  @Override
  public synchronized void reset() {
    close();
    initialized = false;

    // Free memory as soon as possible, the store object might go live on for a while:
    value = null;
  }

  public Object getIdentifier() {
    return identifier;
  }

  public TestDescriptor getScope() {
    return scope;
  }

  public boolean isVisibleFor(TestDescriptor retriever) {
    return isInScope(retriever);
  }

  private boolean isInScope(TestDescriptor retriever) {
    if (retriever == scope) {
      return true;
    }
    return retriever.getParent().map(this::isInScope).orElse(false);
  }

  @Override
  public String toString() {
    return String.format(
        "Store(%s, %s, %s): [%s]",
        displayString(identifier), lifespan.name(), scope.getUniqueId(), displayString(value));
  }

  public void close() {
    if (!initialized) {
      return;
    }
    closeOnReset();
  }

  private void closeOnReset() {
    if (value instanceof Store.CloseOnReset) {
      try {
        ((Store.CloseOnReset) value).close();
      } catch (Throwable throwable) {
        JqwikExceptionSupport.rethrowIfBlacklisted(throwable);
        String message = String.format("Exception while closing store [%s]", this);
        LOG.log(Level.SEVERE, message, throwable);
      }
    }
  }
}
