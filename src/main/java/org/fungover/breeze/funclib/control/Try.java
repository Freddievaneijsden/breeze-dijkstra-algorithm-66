package org.fungover.breeze.funclib.control;


import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Represents a computation that may either result in a value (Success) or an exception (Failure).
 *
 * @param <T> the type of the result value
 */
public abstract class Try<T> implements Serializable {
    /**
     * Checks if the computation was successful.
     *
     * @return true if successful, false otherwise.
     */
    public abstract boolean isSuccess();

    /**
     * Checks if the computation failed.
     *
     * @return true if failed, false otherwise.
     */
    public abstract boolean isFailure();

    /**
     * Retrieves the computed value or throws the exception if failed.
     *
     * @return the computed value.
     * @throws Exception if the computation failed.
     */
    public abstract T get() throws Exception;

    /**
     * Retrieves the value if present, otherwise returns the default value.
     *
     * @param defaultValue the value to return if the computation failed.
     * @return the computed value or the default value.
     */
    public abstract T getOrElse(T defaultValue);

    /**
     * Retrieves the value if present, otherwise computes and returns a default value.
     *
     * @param supplier the supplier function to compute the default value.
     * @return the computed value or the supplied default value.
     */
    public abstract T getOrElseGet(Supplier<? extends T> supplier);

    /**
     * Retrieves the value if present, otherwise throws a mapped exception.
     *
     * @param exceptionMapper function to map the Throwable to an exception of type X.
     * @param <X>             the exception type to be thrown.
     * @return the computed value.
     * @throws X if the computation failed.
     */
    public abstract <X extends Throwable> T getOrElseThrow(Function<? super Throwable, ? extends X> exceptionMapper) throws X;

    /**
     * Creates a successful Try instance with the given value.
     *
     * @param <T>   the type of the successful value
     * @param value the value to wrap in a successful Try
     * @return a Success instance containing the value
     */
    public static <T> Try<T> success(T value) {
        return new Success<>(value);
    }

    /**
     * Creates a failed Try instance with the given exception.
     *
     * @param <T>        the type of the expected value
     * @param exception  the exception causing the failure
     * @return a Failure instance containing the exception
     */
    public static <T> Try<T> failure(Exception exception) {
        Objects.requireNonNull(exception, "Throwable must not be null");
        return new Failure<>(exception);
    }

    /**
     * Wraps the execution of a supplier in a Try, capturing any thrown exceptions.
     *
     * @param <T>      the type of the value produced by the supplier
     * @param supplier the supplier function to execute
     * @return a Success if the supplier executes successfully, otherwise a Failure
     */
    public static <T> Try<T> of(Supplier<T> supplier) {
        try {
            return success(supplier.get());
        } catch (Exception e) {
            return failure(e);
        } catch (Error e) {
            return failure(new Exception("Critical error occurred: " + e.getClass().getSimpleName(), e));
        }
    }


    /**
     * Wraps the execution of a callable in a Try, capturing any thrown exceptions.
     *
     * @param <T>      the type of the value produced by the callable
     * @param callable the callable function to execute
     * @return a Success if the callable executes successfully, otherwise a Failure
     */
    public static <T> Try<T> ofCallable(Callable<T> callable) {
        try {
            return success(callable.call());
        } catch (Exception e) {
            return failure(e);
        } catch (Error e) {
            return failure(new Exception("Critical error occurred", e));
        }
    }

    /**
     * Transforms the value inside Try using the provided function if it is a success.
     *
     * @param mapper the function to apply.
     * @param <U>    the new type of the transformed value.
     * @return a new Try instance containing the transformed value or the original Failure.
     */
    public <U> Try<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        if (isSuccess()) {
            try {
                return success(mapper.apply(get()));
            } catch (Exception e) {
                return failure(e);
            }
        } else {
            return failure(((Failure<T>) this).exception);
        }
    }

    /**
     * Applies a function that returns a Try instance if the computation was successful.
     *
     * @param mapper the function to apply.
     * @param <U>    the new type of the transformed value.
     * @return the Try instance returned by the function or the original failure.
     */
    public <U> Try<U> flatMap(Function<? super T, Try<U>> mapper) {
        Objects.requireNonNull(mapper);

        if (isSuccess()) {
            try {
                return mapper.apply(get());
            } catch (Exception e) {
                return failure(e);
            }
        } else {
            return failure(((Failure<T>) this).exception);
        }
    }

    /**
     * Filters the value inside Try using the provided predicate.
     * If the value does not satisfy the predicate, it returns a Failure.
     *
     * @param predicate the condition to check.
     * @return the same Try instance if the value satisfies the predicate, otherwise a Failure.
     */
    public Try<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        if (isSuccess()) {
            try {
                T value = get();
                if (!predicate.test(value))
                    return failure(new NoSuchElementException("Value " + value + " does not satisfy the predicate"));
                return this;
            } catch (Exception e) {
                return failure(e);
            }
        } else {
            return this;
        }
    }

    /**
     * Handles the failure case by recovering from the Throwable and returning a successful Try.
     * If the Try is a success, it returns the same instance.
     *
     * @param recoverFunction the function that handles the failure and returns a value.
     * @return a Try instance containing the recovered value or the same success.
     */
    public Try<T> recover(Function<? super Exception, ? extends T> recoverFunction) {
        Objects.requireNonNull(recoverFunction);

        if (isSuccess()) {
            return this;
        } else {
            try {
                Exception cause = ((Failure<T>) this).exception;

                    T recoveredValue = recoverFunction.apply((cause));
                    return success(recoveredValue);
            } catch (Exception e) {
                return failure(e);
            }
        }
    }

    /**
     * Monadic failure handling: If the Try is a failure, this method applies a function
     * that returns another Try. If it is a success, it returns the same instance.
     *
     * @param recoverWithFunction the function that handles the failure and returns a new Try.
     * @return a new Try instance containing the recovered value or the same success.
     */
    public Try<T> recoverWith(Function<? super Throwable, Try<T>> recoverWithFunction) {
        Objects.requireNonNull(recoverWithFunction);

        if (isSuccess()) {
            return this;
        } else {
            try {
                Exception cause = ((Failure<T>) this).exception;
                return recoverWithFunction.apply(cause);
            } catch (Exception e) {
                return failure(e);
            }
        }
    }


    /**
     * Handles both the failure and success cases by applying different functions to each.
     *
     * @param failureFunction the function to apply to the Throwable in case of failure.
     * @param successFunction the function to apply to the value in case of success.
     * @param <U>             the result type after folding.
     * @return the result of applying the corresponding function based on the Try instance.
     */
    public <U> U fold(Function<? super Throwable, ? extends U> failureFunction, Function<? super T, ? extends U> successFunction) {
        Objects.requireNonNull(failureFunction);
        Objects.requireNonNull(successFunction);

        if (isSuccess()) {
            try {
                return successFunction.apply(get());
            } catch (Exception e) {
                return failureFunction.apply(e);
            }
        } else {
            try {
                Exception cause = ((Failure<T>) this).exception;
                return failureFunction.apply(cause);
            } catch (Exception e) {
                return failureFunction.apply(e);
            }
        }
    }

    /**
     * Converts this Try instance into an Either type.
     *
     * @param <L> the left type representing the failure case
     * @param <R> the right type representing the success case
     * @return an Either representing the Try outcome
     */
    @SuppressWarnings("unchecked")
    public <L extends Serializable, R extends Serializable> Either<L, R> toEither() {
        if (this instanceof Success<T> success) {
            return Either.right((R) success.getValue());
        } else if (this instanceof Failure<T> failure) {
            return Either.left((L) failure.exception);
        }
        throw new IllegalStateException("Unknown Try state");
    }

    /**
     * Converts this Try into an Optional, where Success contains a value and Failure results in an empty Optional.
     *
     * @return an Optional containing the success value or empty if it's a failure.
     */
    public Optional<T> toOptional() {
        if (isSuccess()) {
            try {
                return Optional.ofNullable(get());
            } catch (Exception e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    /**
     * Applies one of the provided functions based on whether this {@code Try} represents a success or a failure.
     * <p>
     * If this instance is a {@code Success}, the {@code successCase} function is applied to the value inside it.
     * If this instance is a {@code Failure}, the {@code failureCase} function is applied to the contained exception.
     * </p>
     *
     * @param <R>         the result type of the function application
     * @param successCase a function to apply if this is a {@code Success}
     * @param failureCase a function to apply if this is a {@code Failure}
     * @return the result of applying the corresponding function based on the type of {@code Try}
     * @throws NullPointerException  if either {@code successCase} or {@code failureCase} is {@code null}
     * @throws IllegalStateException if the {@code Try} instance is in an unexpected state (should never occur due to sealed class enforcement)
     */
    public <R> R match(Function<? super T, ? extends R> successCase, Function<? super Exception, ? extends R> failureCase) {
        Objects.requireNonNull(successCase);
        Objects.requireNonNull(failureCase);

        if (this instanceof Success<T> success) {
            return successCase.apply(success.get());
        } else if (this instanceof Failure<T> failure) {
            return failureCase.apply(failure.exception);
        }
        throw new IllegalStateException("Unexpected Try state");
    }

    /**
     * Represents a failed computation within a Try.
     *
     * @param <T> the type of the expected successful value
     */
    static final class Failure<T> extends Try<T> implements Serializable {
        final Exception exception;

        public Failure(Exception exception) {
            this.exception = exception;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public boolean isFailure() {
            return true;
        }

        @Override
        public T get() throws Exception {
            throw exception;
        }

        @Override
        public T getOrElse(T defaultValue) {
            return defaultValue;
        }

        @Override
        public T getOrElseGet(Supplier<? extends T> supplier) {
            return supplier.get();
        }

        @Override
        public <X extends Throwable> T getOrElseThrow(Function<? super Throwable, ? extends X> exceptionMapper) throws X {
            throw exceptionMapper.apply(exception);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Failure<?> other = (Failure<?>) obj;
            return Objects.equals(exception, other.exception);
        }

        @Override
        public int hashCode() {
            return Objects.hash(exception);
        }

        @Override
        public String toString() {
            return "Failure[exception=" + exception + "]";
        }
    }

    /**
     * Represents a successful computation within a Try.
     *
     * @param <T> the type of the successful value
     */
     static final class Success<T> extends Try<T> implements Serializable {
        private final T value;

        public Success(T value) {
            this.value = value;
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public boolean isFailure() {
            return false;
        }

        @Override
        public T get() {
            return this.value;
        }

        @Override
        public T getOrElse(T defaultValue) {
            return value;
        }

        @Override
        public T getOrElseGet(Supplier<? extends T> supplier) {
            return value;
        }

        @Override
        public <X extends Throwable> T getOrElseThrow(Function<? super Throwable, ? extends X> exceptionMapper) throws X {
            return value;
        }

        public T getValue() {
            return value;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Success<?> other = (Success<?>) obj;
            return Objects.equals(value, other.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return "Success[" + value + "]";
        }
    }
}

