package asynchronous;

import exception.AizouException;
import exception.ErrorCode;
import play.libs.F;
import play.mvc.Result;
import scala.concurrent.Promise;

import java.util.ArrayList;
import java.util.List;

import static play.libs.F.Either;

/**
 * 异步non-blocking处理的Helper类。主要用于生成Promise对象。
 * <p>
 * Created by zephyre on 2/12/15.
 */
public class AsyncExecutor {

    public static <T> F.Promise<Result> execute(final F.Function0<T> func, final F.Function<T, Result> func2) {
        F.Promise<Either<T, Throwable>> promise = F.Promise.promise(
                new F.Function0<Either<T, Throwable>>() {
                    @Override
                    public Either<T, Throwable> apply() throws Throwable {
                        try {
                            return Either.Left(func.apply());
                        } catch (Throwable e) {
                            return Either.Right(e);
                        }
                    }
                }
        );

        return promise.map(new F.Function<Either<T, Throwable>, Result>() {
            @Override
            public Result apply(Either<T, Throwable> item) throws Throwable {
                if (item.left != null && item.left.isDefined()) {
                    return func2.apply(item.left.get());
                } else if (item.right != null && item.right.isDefined()) {
                    throw item.right.get();
                } else
                    throw new AizouException(ErrorCode.UNKOWN_ERROR);
            }
        });
    }

    public static <T> F.Promise<T> creatPromise(final F.Function0<T> func) {
        return (F.Promise) F.Promise.promise(
                new F.Function0<Either<T, Throwable>>() {
                    @Override
                    public Either<T, Throwable> apply() throws Throwable {
                        try {
                            return Either.Left(func.apply());
                        } catch (Throwable e) {
                            return Either.Right(e);
                        }
                    }
                }
        );
    }

    public static <T> List<F.Promise<T>> creatPromises(final F.Function0<T>... funcs) {
        List<F.Promise<T>> result = new ArrayList<>();
        for (final F.Function0<T> func : funcs) {
            result.add((F.Promise) F.Promise.promise(
                            new F.Function0<Either<T, Throwable>>() {
                                @Override
                                public Either<T, Throwable> apply() throws Throwable {
                                    try {
                                        return Either.Left(func.apply());
                                    } catch (Throwable e) {
                                        return Either.Right(e);
                                    }
                                }
                            }
                    )
            );
        }
        return result;
    }


}
