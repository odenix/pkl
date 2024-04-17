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
import java.util.Set;

/**
 * JqwikSession is the abstraction to give users of {@linkplain Arbitrary#sample()} and {@linkplain
 * Arbitrary#sampleStream()} outside the jqwik lifecycle more control over the lifecycle. This has
 * also influence on memory heap usage since an un-finished session will aggregate state, e.g.
 * through caching and other {@linkplain Store stores}.
 */
public class JqwikSession {
  private static final TestDescriptor SESSION_DESCRIPTOR =
      new TestDescriptor() {
        private final UniqueId uniqueId = new UniqueId();

        @Override
        public Optional<TestDescriptor> getParent() {
          return Optional.empty();
        }

        @Override
        public UniqueId getUniqueId() {
          return uniqueId;
        }

        @Override
        public Set<? extends TestDescriptor> getDescendants() {
          return Set.of();
        }
      };

  @FunctionalInterface
  public interface Runnable {
    void run() throws Throwable;
  }

  public static class JqwikSessionFacade {
    private static final JqwikSession.JqwikSessionFacade implementation;

    static {
      implementation = new JqwikSessionFacade();
    }

    public void startSession() {
      if (!CurrentTestDescriptor.isEmpty()) {
        if (CurrentTestDescriptor.get() == SESSION_DESCRIPTOR) {
          throw new JqwikException("JqwikSession.start() cannot be nested");
        } else {
          throw new JqwikException(
              "JqwikSession.start() must only be used outside jqwik's standard lifecycle");
        }
      }
      CurrentTestDescriptor.push(SESSION_DESCRIPTOR);
    }

    public void finishSession() {
      if (!isSessionOpen()) {
        throw new IllegalStateException("JqwikSession.finish() should be called within a session");
      }
      finishSessionLifecycle();
      CurrentTestDescriptor.pop();
    }

    public void finishTry() {
      if (!isSessionOpen()) {
        throw new JqwikException(
            "JqwikSession.finishTry() must only be used within a JqwikSession");
      }
      StoreRepository.getCurrent().finishTry(SESSION_DESCRIPTOR);
    }

    public boolean isSessionOpen() {
      return !CurrentTestDescriptor.isEmpty() && CurrentTestDescriptor.get() == SESSION_DESCRIPTOR;
    }

    public void runInSession(JqwikSession.Runnable runnable) {
      try {
        JqwikSession.start();
        runnable.run();
      } catch (Throwable t) {
        JqwikExceptionSupport.throwAsUncheckedException(t);
      } finally {
        JqwikSession.finish();
      }
    }

    private void finishSessionLifecycle() {
      StoreRepository.getCurrent().finishTry(SESSION_DESCRIPTOR);
      StoreRepository.getCurrent().finishProperty(SESSION_DESCRIPTOR);
      StoreRepository.getCurrent().finishScope(SESSION_DESCRIPTOR);
    }
  }

  public static synchronized void start() {
    JqwikSessionFacade.implementation.startSession();
  }

  public static boolean isActive() {
    return JqwikSessionFacade.implementation.isSessionOpen();
  }

  public static synchronized void finish() {
    JqwikSessionFacade.implementation.finishSession();
  }

  public static synchronized void finishTry() {
    JqwikSessionFacade.implementation.finishTry();
  }

  public static synchronized void run(Runnable runnable) {
    JqwikSessionFacade.implementation.runInSession(runnable);
  }
}
