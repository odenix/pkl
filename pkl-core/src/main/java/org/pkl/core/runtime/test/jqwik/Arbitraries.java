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

import static org.pkl.core.runtime.test.jqwik.ArbitrariesSupport.maxNumberOfElements;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import org.pkl.core.runtime.test.jqwik.Tuple.Tuple2;
import org.pkl.core.util.Nullable;

public class Arbitraries {

  public static class ArbitrariesFacade {
    private static final ArbitrariesFacade implementation;

    static {
      implementation = new ArbitrariesFacade();
    }

    public <T> Arbitrary<T> just(T value) {
      return new JustArbitrary<>(value);
    }

    public <T> Arbitrary<T> oneOf(Collection<Arbitrary<? extends T>> choices) {
      return new OneOfArbitrary<>(choices);
    }

    public <T> Arbitrary<T> frequencyOf(List<Tuple.Tuple2<Integer, Arbitrary<T>>> frequencies) {
      List<Tuple.Tuple2<Integer, Arbitrary<T>>> aboveZeroFrequencies =
          frequencies.stream().filter(f -> f.get1() > 0).collect(Collectors.toList());

      if (aboveZeroFrequencies.size() == 1) {
        return aboveZeroFrequencies.get(0).get2();
      }
      if (aboveZeroFrequencies.isEmpty()) {
        String message =
            "frequencyOf() must be called with at least one choice with a frequency > 0";
        throw new JqwikException(message);
      }

      return new FrequencyOfArbitrary<>(aboveZeroFrequencies);
    }

    public IntegerArbitrary integers() {
      return new DefaultIntegerArbitrary();
    }

    public LongArbitrary longs() {
      return new DefaultLongArbitrary();
    }

    public DoubleArbitrary doubles() {
      return new DefaultDoubleArbitrary();
    }

    public StringArbitrary strings() {
      return new DefaultStringArbitrary();
    }

    public CharacterArbitrary chars() {
      return new DefaultCharacterArbitrary();
    }

    public <T> Arbitrary<T> lazy(Supplier<Arbitrary<T>> arbitrarySupplier) {
      return new LazyArbitrary<>(arbitrarySupplier);
    }

    public <T> Arbitrary<T> lazyOf(List<Supplier<Arbitrary<T>>> suppliers) {
      int hashIdentifier = calculateIdentifier(suppliers.size());
      return LazyOfArbitrary.of(hashIdentifier, suppliers);
    }

    public Arbitrary<Character> of(char[] chars) {
      return new ChooseCharacterArbitrary(chars);
    }

    public <T> Arbitrary<T> of(Collection<T> values) {
      List<T> valueList = values instanceof List ? (List<T>) values : new ArrayList<>(values);
      return new ChooseValueArbitrary<>(valueList);
    }

    public <T> Arbitrary<T> create(Supplier<T> supplier) {
      return new CreateArbitrary<>(supplier);
    }

    public <T> Arbitrary<List<T>> shuffle(List<T> values) {
      return new ShuffleArbitrary<>(values);
    }

    public <T> Arbitrary<T> fromGenerator(IntFunction<RandomGenerator<T>> generatorSupplier) {
      return new FromGeneratorWithSizeArbitrary<>(generatorSupplier);
    }

    public <T> Arbitrary<T> frequency(List<Tuple.Tuple2<Integer, T>> frequencies) {
      List<Tuple.Tuple2<Integer, T>> frequenciesAbove0 =
          frequencies.stream().filter(f -> f.get1() > 0).collect(Collectors.toList());

      if (frequenciesAbove0.isEmpty()) {
        String message = "frequency() must be called with at least one value with a frequency > 0";
        throw new JqwikException(message);
      }
      if (frequenciesAbove0.size() == 1) {
        return new JustArbitrary<>(frequenciesAbove0.get(0).get2());
      }

      return new FrequencyArbitrary<>(frequenciesAbove0);
    }

    /**
     * The calculated hash is supposed to be the same for the same callers of Arbitraries.lazyOf()
     * This is important to have a single instance of LazyOfArbitrary for the same code.
     */
    private static int calculateIdentifier(int numberOfSuppliers) {
      try {
        throw new RuntimeException();
      } catch (RuntimeException rte) {
        Optional<Integer> optionalHash =
            Arrays.stream(rte.getStackTrace())
                .filter(
                    stackTraceElement ->
                        !stackTraceElement.getClassName().equals(ArbitrariesFacade.class.getName()))
                .filter(
                    stackTraceElement ->
                        !stackTraceElement.getClassName().equals(Arbitraries.class.getName()))
                .findFirst()
                .map(
                    stackTraceElement ->
                        HashCodeSupport.hash(
                            stackTraceElement.getClassName(),
                            stackTraceElement.getMethodName(),
                            stackTraceElement.getLineNumber(),
                            numberOfSuppliers));
        return optionalHash.orElse(0);
      }
    }

    public <K, V> MapArbitrary<K, V> maps(
        Arbitrary<K> keysArbitrary, Arbitrary<V> valuesArbitrary) {
      // The map cannot be larger than the max number of possible keys
      return new DefaultMapArbitrary<>(keysArbitrary, valuesArbitrary)
          .ofMaxSize(maxNumberOfElements(keysArbitrary, RandomGenerators.DEFAULT_COLLECTION_SIZE));
    }

    public <K, V> Arbitrary<Map.Entry<K, V>> entries(
        Arbitrary<K> keysArbitrary, Arbitrary<V> valuesArbitrary) {
      return Combinators.combine(keysArbitrary, valuesArbitrary).as(AbstractMap.SimpleEntry::new);
    }

    public <T> Arbitrary<T> recursive(
        Supplier<Arbitrary<T>> base,
        Function<Arbitrary<T>, Arbitrary<T>> recur,
        int minDepth,
        int maxDepth) {
      if (minDepth < 0) {
        String message = String.format("minDepth <%s> must be >= 0.", minDepth);
        throw new IllegalArgumentException(message);
      }
      if (minDepth > maxDepth) {
        String message =
            String.format("minDepth <%s> must not be > maxDepth <%s>", minDepth, maxDepth);
        throw new IllegalArgumentException(message);
      }

      if (minDepth == maxDepth) {
        return recursive(base, recur, minDepth);
      } else {
        Arbitrary<Integer> depths =
            Arbitraries.integers()
                .between(minDepth, maxDepth)
                .withDistribution(RandomDistribution.uniform())
                .edgeCases(c -> c.includeOnly(minDepth, maxDepth));
        return depths.flatMap(depth -> recursive(base, recur, depth));
      }
    }

    private <T> Arbitrary<T> recursive(
        Supplier<Arbitrary<T>> base, Function<Arbitrary<T>, Arbitrary<T>> recur, int depth) {
      return new RecursiveArbitrary<>(base, recur, depth);
    }
  }

  private Arbitraries() {}

  /**
   * Create an arbitrary of type T from a corresponding generator of type T.
   *
   * @param generator The generator to be used for generating the values
   * @param <T> The type of values to generate
   * @return a new arbitrary instance
   */
  public static <T> Arbitrary<T> fromGenerator(RandomGenerator<T> generator) {
    return fromGeneratorWithSize(ignore -> generator);
  }

  /**
   * Create an arbitrary of type T by supplying a corresponding generator of type T.
   *
   * @param generatorSupplier A function to supply a generator instance given the "size" of a
   *     generation attempt
   * @param <T> The type of values to generate
   * @return a new arbitrary instance
   */
  public static <T> Arbitrary<T> fromGeneratorWithSize(
      IntFunction<RandomGenerator<T>> generatorSupplier) {
    return ArbitrariesFacade.implementation.fromGenerator(generatorSupplier);
  }

  /**
   * Create an arbitrary that will generate values of type T using a generator function. The
   * generated values are unshrinkable.
   *
   * @param generator The generator function to be used for generating the values
   * @param <T> The type of values to generate
   * @return a new arbitrary instance
   */
  public static <T> Arbitrary<T> randomValue(Function<Random, T> generator) {
    IntFunction<RandomGenerator<T>> generatorSupplier =
        ignore -> random -> Shrinkable.unshrinkable(generator.apply(random));
    return fromGeneratorWithSize(generatorSupplier);
  }

  /**
   * Create an arbitrary for Random objects.
   *
   * @return a new arbitrary instance
   */
  public static Arbitrary<Random> randoms() {
    return randomValue(random -> new Random(random.nextLong()));
  }

  /**
   * Create an arbitrary that will randomly choose from a given array of values. A generated value
   * will be shrunk towards the start of the array.
   *
   * <p>Use this method only for immutable arrays of immutable values. Changing a value will change
   * subsequently generated values as well. For mutable values use {@linkplain
   * #ofSuppliers(Supplier[])} instead. Modifying the array may cause erratic behavior, things may
   * halt and catch fire.
   *
   * @param values The array of values to choose from
   * @param <T> The type of values to generate
   * @return a new arbitrary instance
   */
  @SafeVarargs
  public static <T> Arbitrary<T> of(T... values) {
    return of(Arrays.asList(values));
  }

  /**
   * Create an arbitrary that will randomly choose from a given collection of values. A generated
   * value will be shrunk towards the start of the collection.
   *
   * <p>Use this method only for immutable collections of immutable values. Changing a value will
   * change subsequently generated values as well. For mutable values use {@linkplain
   * #ofSuppliers(Collection)} instead. Modifying the collection may cause erratic behavior, kittens
   * may die.
   *
   * @param values The collection of values to choose from
   * @param <T> The type of values to generate
   * @return a new arbitrary instance
   */
  public static <@Nullable T> Arbitrary<T> of(Collection<T> values) {
    return ArbitrariesFacade.implementation.of(values);
  }

  /**
   * Create an arbitrary that will randomly choose from a given array of value suppliers and then
   * get the value from the supplier. A generated value will be shrunk towards the start of the
   * array.
   *
   * <p>Use this method instead of {@linkplain #of(Object[])} for mutable objects to make sure that
   * changing a generated object will not influence other generated objects.
   *
   * @param valueSuppliers The array of values to choose from
   * @param <T> The type of values to generate
   * @return a new arbitrary instance
   */
  @SafeVarargs
  public static <T> Arbitrary<T> ofSuppliers(Supplier<T>... valueSuppliers) {
    return of(valueSuppliers).map(Supplier::get);
  }

  /**
   * Create an arbitrary that will randomly choose from a given collection of value suppliers and
   * then get the value from the supplier. A generated value will be shrunk towards the start of the
   * collection.
   *
   * <p>Use this method instead of {@linkplain #of(Collection)} for mutable objects to make sure
   * that changing a generated object will not influence other generated objects.
   *
   * @param valueSuppliers The collection of values to choose from
   * @param <T> The type of values to generate
   * @return a new arbitrary instance
   */
  public static <T> Arbitrary<T> ofSuppliers(Collection<Supplier<T>> valueSuppliers) {
    return of(valueSuppliers).map(Supplier::get);
  }

  /**
   * Create an arbitrary of character values.
   *
   * @param values The array of characters to choose from.
   * @return a new arbitrary instance
   */
  public static Arbitrary<Character> of(char[] values) {
    return ArbitrariesFacade.implementation.of(values);
  }

  /**
   * Create an arbitrary for enum values of type T.
   *
   * @param enumClass The enum class.
   * @param <T> The type of values to generate
   * @return a new arbitrary instance
   */
  public static <T extends Enum<T>> Arbitrary<T> of(Class<T> enumClass) {
    List<T> values = Arrays.asList(enumClass.getEnumConstants());
    return of(values);
  }

  /**
   * Create an arbitrary that will randomly choose between all given arbitraries of the same type T.
   *
   * @param first The first arbitrary to choose form
   * @param rest An array of arbitraries to choose from
   * @param <T> The type of values to generate
   * @return a new arbitrary instance
   */
  @SuppressWarnings("unchecked")
  @SafeVarargs
  public static <T> Arbitrary<T> oneOf(
      Arbitrary<? extends T> first, Arbitrary<? extends T>... rest) {
    List<Arbitrary<? extends T>> all = new ArrayList<>();
    all.add(first);
    for (Arbitrary<?> arbitrary : rest) {
      all.add((Arbitrary<? extends T>) arbitrary);
    }
    return oneOf(all);
  }

  /**
   * Create an arbitrary that will randomly choose between all given arbitraries of the same type T.
   *
   * @param choices A collection of arbitraries to choose from
   * @param <T> The type of values to generate
   * @return a new arbitrary instance
   */
  @SuppressWarnings("unchecked")
  public static <T> Arbitrary<T> oneOf(Collection<Arbitrary<? extends T>> choices) {
    if (choices.isEmpty()) {
      String message = "oneOf() must not be called with no choices";
      throw new JqwikException(message);
    }
    if (choices.size() == 1) {
      return (Arbitrary<T>) choices.iterator().next();
    }
    // Simple flatMapping is not enough because of configurations
    return ArbitrariesFacade.implementation.oneOf(choices);
  }

  /**
   * Create an arbitrary that will randomly choose between all given values of the same type T. The
   * probability distribution is weighted with the first parameter of the tuple.
   *
   * @param frequencies An array of tuples of which the first parameter gives the weight and the
   *     second the value.
   * @param <T> The type of values to generate
   * @return a new arbitrary instance
   */
  @SafeVarargs
  public static <T> Arbitrary<T> frequency(Tuple2<Integer, T>... frequencies) {
    return frequency(Arrays.asList(frequencies));
  }

  /**
   * Create an arbitrary that will randomly choose between all given values of the same type T. The
   * probability distribution is weighted with the first parameter of the tuple.
   *
   * @param frequencies A list of tuples of which the first parameter gives the weight and the
   *     second the value.
   * @param <T> The type of values to generate
   * @return a new arbitrary instance
   */
  public static <@Nullable T> Arbitrary<T> frequency(List<Tuple2<Integer, T>> frequencies) {
    return ArbitrariesFacade.implementation.frequency(frequencies);
  }

  /**
   * Create an arbitrary that will randomly choose between all given arbitraries of the same type T.
   * The probability distribution is weighted with the first parameter of the tuple.
   *
   * @param frequencies An array of tuples of which the first parameter gives the weight and the
   *     second the arbitrary.
   * @param <T> The type of values to generate
   * @return a new arbitrary instance
   */
  @SuppressWarnings("unchecked")
  @SafeVarargs
  public static <T> Arbitrary<T> frequencyOf(
      Tuple2<Integer, Arbitrary<? extends T>>... frequencies) {
    List<Tuple2<Integer, Arbitrary<T>>> all = new ArrayList<>();
    for (Tuple2<Integer, Arbitrary<? extends T>> frequency : frequencies) {
      all.add(Tuple.of(frequency.get1(), (Arbitrary<T>) frequency.get2()));
    }
    return frequencyOf(all);
  }

  /**
   * Create an arbitrary that will randomly choose between all given arbitraries of the same type T.
   * The probability distribution is weighted with the first parameter of the tuple.
   *
   * @param frequencies A list of tuples of which the first parameter gives the weight and the
   *     second the arbitrary.
   * @param <T> The type of values to generate
   * @return a new arbitrary instance
   */
  public static <@Nullable T> Arbitrary<T> frequencyOf(
      List<Tuple2<Integer, Arbitrary<T>>> frequencies) {
    // Simple flatMapping is not enough because of configurations
    return ArbitrariesFacade.implementation.frequencyOf(frequencies);
  }

  /**
   * Create an arbitrary that generates values of type Integer.
   *
   * @return a new arbitrary instance
   */
  public static IntegerArbitrary integers() {
    return ArbitrariesFacade.implementation.integers();
  }

  /**
   * Create an arbitrary that generates values of type Long.
   *
   * @return a new arbitrary instance
   */
  public static LongArbitrary longs() {
    return ArbitrariesFacade.implementation.longs();
  }

  /**
   * Create an arbitrary that generates values of type Double.
   *
   * @return a new arbitrary instance
   */
  public static DoubleArbitrary doubles() {
    return ArbitrariesFacade.implementation.doubles();
  }

  /**
   * Create an arbitrary that generates values of type String.
   *
   * @return a new arbitrary instance
   */
  public static StringArbitrary strings() {
    return ArbitrariesFacade.implementation.strings();
  }

  /**
   * Create an arbitrary that generates values of type Character.
   *
   * @return a new arbitrary instance
   */
  public static CharacterArbitrary chars() {
    return ArbitrariesFacade.implementation.chars();
  }

  /**
   * Create an arbitrary that will always generate the same value.
   *
   * @param value The value to "generate".
   * @param <T> The type of the value
   * @return a new arbitrary instance
   */
  public static <@Nullable T> Arbitrary<T> just(@Nullable T value) {
    return ArbitrariesFacade.implementation.just(value);
  }

  /**
   * Create an arbitrary that will use a supplier to generate a value. The difference to {@linkplain
   * Arbitraries#just(Object)} is that the value is freshly generated for each try of a property.
   *
   * <p>Mind that within a {@code supplier} you should never use other arbitraries or do anything
   * non-deterministic.
   *
   * <p>For exhaustive shrinking all generated values are supposed to have identical behaviour, i.e.
   * that means that only one value is generated per combination.
   *
   * @param supplier The supplier use to generate a value
   * @param <T> The type of values to generate
   * @return a new arbitrary instance
   */
  public static <T> Arbitrary<T> create(Supplier<T> supplier) {
    return ArbitrariesFacade.implementation.create(supplier);
  }

  /**
   * Create an arbitrary that will always generate a list which is a permutation of the values
   * handed to it. Permutations will not be shrunk.
   *
   * @param values The values to permute
   * @param <T> The type of values to generate
   * @return a new arbitrary instance
   */
  @SafeVarargs
  public static <T> Arbitrary<List<T>> shuffle(T... values) {
    return shuffle(Arrays.asList(values));
  }

  /**
   * Create an arbitrary that will always generate a list which is a permutation of the values
   * handed to it. Permutations will not be shrunk.
   *
   * @param values The values to permute
   * @param <T> The type of values to generate
   * @return a new arbitrary instance
   */
  public static <T> Arbitrary<List<T>> shuffle(List<T> values) {
    return ArbitrariesFacade.implementation.shuffle(values);
  }

  /**
   * Create an arbitrary that will evaluate arbitrarySupplier as soon as it is used for generating
   * values.
   *
   * <p>This is useful (and necessary) when arbitrary providing functions use other arbitrary
   * providing functions in a recursive way. Without the use of lazy() this would result in a stack
   * overflow. Most of the time, however, using {@linkplain #lazyOf(Supplier, Supplier[])} is the
   * better choice because it has significantly better shrinking behaviour.
   *
   * @param arbitrarySupplier The supplier function being used to generate an arbitrary
   * @param <T> The type of values to generate
   * @return a new arbitrary instance
   * @see #recursive(Supplier, Function, int)
   * @see #lazyOf(Supplier, Supplier[])
   */
  public static <T> Arbitrary<T> lazy(Supplier<Arbitrary<T>> arbitrarySupplier) {
    return ArbitrariesFacade.implementation.lazy(arbitrarySupplier);
  }

  /**
   * Create an arbitrary by deterministic recursion.
   *
   * <p>Mind that the arbitrary will be created by invoking recursion at arbitrary creation time.
   * Using {@linkplain #lazyOf(Supplier, Supplier[])} or {@linkplain #lazy(Supplier)} instead will
   * recur at value generation time.
   *
   * @param base The supplier returning the recursion's base case
   * @param recur The function to extend the base case
   * @param depth The number of times to invoke recursion
   * @param <T> The type of values to generate
   * @return a new arbitrary instance
   * @see #lazy(Supplier)
   */
  public static <T> Arbitrary<T> recursive(
      Supplier<Arbitrary<T>> base, Function<Arbitrary<T>, Arbitrary<T>> recur, int depth) {
    return ArbitrariesFacade.implementation.recursive(base, recur, depth, depth);
  }

  /**
   * Create an arbitrary by deterministic recursion.
   *
   * <p>Mind that the arbitrary will be created by invoking recursion at arbitrary creation time.
   * Using {@linkplain #lazyOf(Supplier, Supplier[])} or {@linkplain #lazy(Supplier)} instead will
   * recur at value generation time.
   *
   * @param base The supplier returning the recursion's base case
   * @param recur The function to extend the base case
   * @param minDepth The minimum number of times to invoke recursion
   * @param maxDepth The maximum number of times to invoke recursion
   * @param <T> The type of values to generate
   * @return a new arbitrary instance
   * @see #lazy(Supplier)
   */
  public static <T> Arbitrary<T> recursive(
      Supplier<Arbitrary<T>> base,
      Function<Arbitrary<T>, Arbitrary<T>> recur,
      int minDepth,
      int maxDepth) {
    return ArbitrariesFacade.implementation.recursive(base, recur, minDepth, maxDepth);
  }

  /**
   * Create an arbitrary by lazy supplying one of several arbitraries. The main use of this function
   * is to allow recursive generation of structured values without overflowing the stack.
   *
   * <p>One alternative is to use {@linkplain #lazy(Supplier)} combined with {@linkplain
   * Arbitraries#oneOf(Arbitrary, Arbitrary[])} or {@linkplain
   * Arbitraries#frequencyOf(Tuple.Tuple2[])}. But {@code lazyOf()} has considerably better
   * shrinking behaviour with recursion.
   *
   * <p><em>Caveat:</em> Never use this construct if suppliers make use of variable state like
   * method parameters or changing instance members. In those cases use {@linkplain #lazy(Supplier)}
   * instead.
   *
   * @param first The first supplier to choose from
   * @param rest The rest of suppliers to choose from
   * @param <T> The type of values to generate
   * @return a (potentially cached) arbitrary instance
   * @see #lazy(Supplier)
   * @see #recursive(Supplier, Function, int)
   */
  @SuppressWarnings("unchecked")
  @SafeVarargs
  public static <T> Arbitrary<T> lazyOf(
      Supplier<Arbitrary<? extends T>> first, Supplier<Arbitrary<? extends T>>... rest) {
    List<Supplier<Arbitrary<T>>> all = new ArrayList<>();
    all.add(() -> (Arbitrary<T>) first.get());
    for (Supplier<Arbitrary<? extends T>> arbitrarySupplier : rest) {
      all.add(() -> (Arbitrary<T>) arbitrarySupplier.get());
    }
    return ArbitrariesFacade.implementation.lazyOf(all);
  }

  /**
   * Create an arbitrary to create instances of {@linkplain Map}. The generated maps are mutable.
   *
   * @param keysArbitrary The arbitrary to generate the keys
   * @param valuesArbitrary The arbitrary to generate the values
   * @param <K> type of keys
   * @param <V> type of values
   * @return a new arbitrary instance
   */
  public static <K, V> MapArbitrary<K, V> maps(
      Arbitrary<K> keysArbitrary, Arbitrary<V> valuesArbitrary) {
    return ArbitrariesFacade.implementation.maps(keysArbitrary, valuesArbitrary);
  }

  /**
   * Create an arbitrary to create instances of {@linkplain java.util.Map.Entry}. The generated
   * entries are mutable.
   *
   * @param keysArbitrary The arbitrary to generate the keys
   * @param valuesArbitrary The arbitrary to generate the values
   * @param <K> type of keys
   * @param <V> type of values
   * @return a new arbitrary instance
   */
  public static <K, V> Arbitrary<Map.Entry<K, V>> entries(
      Arbitrary<K> keysArbitrary, Arbitrary<V> valuesArbitrary) {
    return ArbitrariesFacade.implementation.entries(keysArbitrary, valuesArbitrary);
  }

  /**
   * Create an arbitrary that never creates anything. Sometimes useful when generating arbitraries
   * of "functions" that have void as return type.
   *
   * @return arbitrary instance that will generate nothing
   */
  public static Arbitrary<Void> nothing() {
    return just(null);
  }

  /**
   * Create a new arbitrary of element type {@code Set<T>} using the handed in values as elements of
   * the set.
   *
   * @return a new arbitrary instance
   */
  public static <@Nullable T> SetArbitrary<T> subsetOf(Collection<T> values) {
    return of(values).set();
  }

  /**
   * Create a new arbitrary of element type {@code Set<T>} using the handed in values as elements of
   * the set.
   *
   * @return a new arbitrary instance
   */
  @SafeVarargs
  public static <T> SetArbitrary<T> subsetOf(T... values) {
    return subsetOf(Arrays.asList(values));
  }
}
