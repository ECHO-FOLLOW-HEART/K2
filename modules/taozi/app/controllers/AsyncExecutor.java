package controllers;

import controllers.aspectj.CatchException;
import exception.AizouException;
import exception.ErrorCode;
import play.libs.F;
import play.mvc.Result;

/**
 * 异步non-blocking处理的Helper类。主要用于生成Promise对象。
 *
 * Created by zephyre on 2/12/15.
 */
public class AsyncExecutor {

    public static <T> F.Promise<Result> execute(final F.Function0<T> func, final F.Function<T, Result> func2) {
        F.Promise<F.Either<T, Throwable>> promise = F.Promise.promise(
                new F.Function0<F.Either<T, Throwable>>() {
                    @Override
                    public F.Either<T, Throwable> apply() throws Throwable {
                        try {
                            return F.Either.Left(func.apply());
                        } catch (Throwable e) {
                            return F.Either.Right(e);
                        }
                    }
                }
        );

        return promise.map(new F.Function<F.Either<T, Throwable>, Result>() {
            @Override
            @CatchException
            public Result apply(F.Either<T, Throwable> item) throws Throwable {
                if (item.left != null && item.left.isDefined()) {
                    return func2.apply(item.left.get());
                } else if (item.right != null && item.right.isDefined()) {
                    throw item.right.get();
                } else
                    throw new AizouException(ErrorCode.ILLEGAL_STATE);
            }
        });
    }
}
