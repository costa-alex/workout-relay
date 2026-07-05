import { Injectable } from '@angular/core';
import {
  MatSnackBar,
  MatSnackBarConfig,
  MatSnackBarHorizontalPosition,
  MatSnackBarVerticalPosition
} from '@angular/material/snack-bar';
import { BreakpointObserver } from '@angular/cdk/layout';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {

  private static readonly successDuration = 5 * 1000;
  private static readonly errorDuration = 12 * 1000;

  constructor(
    private snackBar: MatSnackBar,
    private breakpointObserver: BreakpointObserver
  ) {
  }

  success(message: string): void {
    this.open(message, 'success');
  }

  error(message: string): void {
    this.open(message, 'error');
  }

  private open(message: string, type: 'success' | 'error'): void {
    const isMobile = this.breakpointObserver.isMatched('(max-width: 768px)');

    const horizontalPosition: MatSnackBarHorizontalPosition = isMobile ? 'center' : 'right';
    const verticalPosition: MatSnackBarVerticalPosition = isMobile ? 'bottom' : 'top';

    const config: MatSnackBarConfig = {
      duration: type === 'success'
        ? NotificationService.successDuration
        : NotificationService.errorDuration,
      horizontalPosition,
      verticalPosition,
      panelClass: [
        'app-notification',
        type === 'success'
          ? 'app-notification-success'
          : 'app-notification-error'
      ]
    };

    this.snackBar.dismiss();
    this.snackBar.open(message, 'Close', config);
  }
}