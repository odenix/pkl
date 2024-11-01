/*
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
package org.pkl.core.runtime;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import java.util.Collection;
import org.graalvm.collections.UnmodifiableMapCursor;
import org.pkl.core.ast.member.ObjectMember;

/**
 * A cursor for iterating over the keys and values of {@link VmObject} members.
 *
 * <p>To obtain a cursor, call one of the following methods:
 *
 * <ul>
 *   <li>{@link VmObjectLike#properties}
 *   <li>{@link VmObjectLike#elements}
 *   <li>{@link VmObjectLike#entries}
 *   <li>{@link VmObjectLike#members}
 * </ul>
 *
 * To customize cursor behavior, the above methods optionally accept a {@link CursorOption}.
 *
 * <p>A typical cursor iteration looks as follows:
 *
 * <pre><code>
 *   for (var cursor = object.properties(); cursor.advance(); ) {
 *     var key = cursor.key();
 *     var value = cursor.value();
 *     ...
 *   }
 * </code></pre>
 *
 * <p>Cursors do not visit {@code local}, {@code external}, and {@code hidden} properties. Only
 * {@link VmTyped#members} visits {@linkplain ObjectMember#isClass() classes} and {@linkplain
 * ObjectMember#isTypeAlias() type aliases} of {@linkplain VmObjectLike#isModuleObject() module
 * objects}.
 */
public abstract class VmObjectCursor {
  protected static final VmTyped objectPrototype = BaseModule.getObjectClass().getPrototype();
  
  public enum CursorOption {
    /**
     * Indicates that a cursor may choose any iteration order.
     *
     * <p>Specifying this option may increase cursor performance.
     *
     * <p>If this option is not specified, members are visited in declaration order. For elements,
     * declaration order is equivalent to ascending order.
     */
    ANY_ORDER,
    /**
     * Indicates that the caller will request every value of this cursor unless it encounters an
     * unexpected error.
     *
     * <p>Specifying this option may increase cursor performance.
     */
    ALL_VALUES,
    /**
     * Indicates that a cursor must not evaluate any member before its value is requested. This
     * option should only be specified if a lazyness guarantee is required for correctness.
     */
    LAZY_REQUIRED
  }

  /**
   * Advances this cursor to the next object member.
   *
   * @return {@code true} if a next member exists, {@code false} otherwise
   */
  public abstract boolean advance();

  /**
   * Returns the key of the current object member.
   *
   * <p>The returned key has one of the following types:
   *
   * <ul>
   *   <li>{@link Identifier} for a property
   *   <li>{@link Long} for an element
   *   <li>{@link Boolean}, {@link Long}, {@link Double}, {@link String} or {@link VmValue} for an
   *       entry
   * </ul>
   *
   * @throws RuntimeException if this method is called before the first call to {@link #advance()}
   *     or after {@link #advance()} returned {@code false}
   */
  public abstract Object key();

  /**
   * Returns the value of the current object member.
   *
   * <p>If the member's value is not already cached, the member is evaluated.
   *
   * @throws RuntimeException if this method is called before the first call to {@link #advance()}
   *     or after {@link #advance()} returned {@code false}
   */
  public abstract Object value();

  public boolean isProperty() {
    return false;
  }

  public boolean isElement() {
    return false;
  }

  public boolean isEntry() {
    return false;
  }

  public boolean isType() {
    return member().isType();
  }
  
  protected abstract VmObject iteratee();

  /**
   * Returns the current member.
   *
   * <p>If a member is overridden in the prototype chain, this method returns the original
   * definition, whereas {@link #value()} returns the value corresponding to the overriding
   * definition. As a consequence, {@code member().isElement()} will correctly identify elements
   * overridden with entry syntax.
   *
   * <p>This method is supported by the following cursors:
   *
   * <ul>
   *   <li>{@link VmObjectLike#members}
   * </ul>
   */
  public ObjectMember member() {
    var key = key();
    // find bottom-most member for key
    //noinspection DataFlowIssue
    for (VmObject object = iteratee(); object.parent != objectPrototype; object = object.parent) {
      var member = object.getMember(key);
      if (member != null) return member;
    }
    throw new VmExceptionBuilder().unreachableCode().build();
  }

  public boolean isMemberAlreadyEvaluated() {
    return iteratee().cachedValues.containsKey(key());
  }

  @TruffleBoundary
  public final boolean keyEquals(Object other) {
    return other.equals(key());
  }

  /** Convenience method that calls {@code other.equals(value())} behind a Truffle boundary. */
  @TruffleBoundary
  public final boolean valueEquals(Object other) {
    // Because this method is often called many times in a row with the same argument,
    // other.equals(value()) might (?) perform better than value().equals(other).
    // Behavior should be identical because all Pkl values should have symmetric equals methods.
    return other.equals(value());
  }

  /** Convenience method that calls {@code collection.add(value())} behind a Truffle boundary. */
  @TruffleBoundary
  public final boolean addValueTo(Collection<Object> collection) {
    return collection.add(value());
  }

  /**
   * Convenience method that calls {@code collection.add(value)} behind a Truffle boundary.
   *
   * <p>Not a static method for convenience and symmetry with {@link #addValueTo(Collection)}.
   */
  @TruffleBoundary
  public final boolean addValueTo(Collection<Object> collection, Object value) {
    return collection.add(value);
  }

  /** Convenience method that calls {@code builder.append(value())} behind a Truffle boundary. */
  @TruffleBoundary
  public final void appendValueTo(StringBuilder builder) {
    builder.append(value());
  }

  /**
   * Convenience method that calls {@code builder.append(value)} behind a Truffle boundary.
   *
   * <p>Not a static method for convenience and symmetry with {@link #appendValueTo(StringBuilder)}.
   */
  @TruffleBoundary
  public final void appendValueTo(StringBuilder builder, Object value) {
    builder.append(value);
  }

  static final class EmptyCursor extends VmObjectCursor {
    @Override
    public boolean advance() {
      return false;
    }

    @Override
    public Object key() {
      throw new IllegalStateException("empty cursor");
    }

    @Override
    public Object value() {
      throw new IllegalStateException("empty cursor");
    }

    @Override
    public boolean isProperty() {
      throw new IllegalStateException("empty cursor");
    }

    @Override
    public boolean isElement() {
      throw new IllegalStateException("empty cursor");
    }

    @Override
    public boolean isEntry() {
      throw new IllegalStateException("empty cursor");
    }

    @Override
    protected VmObject iteratee() {
      throw new IllegalStateException("empty cursor");
    }
  }

  abstract static class AbstractOrderedCursor<T extends VmObject> extends VmObjectCursor {
    protected final T iteratee;

    public AbstractOrderedCursor(T iteratee) {
      this.iteratee = iteratee;
    }

    @Override
    @TruffleBoundary
    public final Object value() {
      var key = key();
      var value = iteratee.getCachedValue(key);
      if (value != null) return value;
      // find and evaluate bottom-most member for key (simplified version of VmUtils.readMember())
      //noinspection DataFlowIssue
      for (VmObject object = iteratee; object.parent != objectPrototype; object = object.parent) {
        var member = object.getMember(key);
        if (member != null) {
          return VmUtils.doReadMember(iteratee, object, key, member);
        }
      }
      throw new VmExceptionBuilder().unreachableCode().build();
    }

    @Override
    protected VmObject iteratee() {
      return iteratee;
    }
  }

  abstract static class AbstractCachedMemberCursor<T extends VmObject> extends VmObjectCursor {
    protected final T iteratee;
    private final UnmodifiableMapCursor<Object, Object> cachedValues;

    @TruffleBoundary
    AbstractCachedMemberCursor(T iteratee) {
      this.iteratee = iteratee;
      cachedValues = iteratee.cachedValues.getEntries();
    }

    protected abstract boolean shouldVisit(Object key);

    @Override
    @TruffleBoundary
    public boolean advance() {
      while (true) {
        if (!cachedValues.advance()) return false;
        if (shouldVisit(cachedValues.getKey())) return true;
      }
    }

    @Override
    @TruffleBoundary
    public Object key() {
      return cachedValues.getKey();
    }

    @Override
    @TruffleBoundary
    public Object value() {
      return cachedValues.getValue();
    }

    @Override
    public boolean isMemberAlreadyEvaluated() {
      return true;
    }

    @Override
    protected VmObject iteratee() {
      return iteratee;
    }
  }
}
