import {
  HttpHandlerFn,
  HttpInterceptorFn,
  HttpRequest
} from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { NotificationService } from 'infrastructure/notification.service';

export const httpErrorInterceptor: HttpInterceptorFn = (
  req: HttpRequest<unknown>,
  next: HttpHandlerFn
) => {
  const notificationService = inject(NotificationService);

  return next(req).pipe(
    catchError(err => {
      const message =
        err.error?.message ??
        err.message ??
        'An unexpected error occurred';

      const platform = err.error?.platform;

      const errorMessage = platform
        ? `${platform}: ${message}`
        : message;

      notificationService.error(errorMessage);

      return throwError(() => err);
    })
  );
};