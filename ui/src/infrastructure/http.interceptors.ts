import {
  HttpErrorResponse,
  HttpHandlerFn,
  HttpInterceptorFn,
  HttpRequest
} from '@angular/common/http';

interface ApiErrorResponse {
  platform?: string;
  message?: string;
  code?: string;
}

export const httpErrorInterceptor: HttpInterceptorFn = (
  req: HttpRequest<unknown>,
  next: HttpHandlerFn
) => {
  const notificationService =
    inject(NotificationService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      const response =
        error.error as ApiErrorResponse | undefined;

      const message =
        response?.message ??
        error.message ??
        'An unexpected error occurred';

      const errorMessage =
        response?.platform
          ? `${response.platform}: ${message}`
          : message;

      notificationService.error(errorMessage);

      return throwError(() => error);
    })
  );
};