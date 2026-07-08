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

  copyCalendarToCalendarCompleted(
    response: any,
    sourcePlatformTitle?: string,
    targetPlatformTitle?: string
  ): void {
    const copied = response.copied ?? 0;
    const skippedByType = response.skippedByType ?? 0;
    const skippedAlreadySynced = response.skippedAlreadySynced ?? 0;

    const direction = sourcePlatformTitle && targetPlatformTitle
      ? ` from ${sourcePlatformTitle} to ${targetPlatformTitle}`
      : '';

    const lines: string[] = [];

    if (copied > 0) {
      lines.push(`${this.formatCount(copied, 'workout')} synced${direction}.`);
    } else if (skippedAlreadySynced > 0 || skippedByType > 0) {
      lines.push('No workouts copied.');
    } else {
      lines.push('No workouts found for the selected period.');
    }

    if (skippedAlreadySynced > 0) {
      lines.push(`${this.formatCount(skippedAlreadySynced, 'already synced workout')} skipped.`);
    }

    if (skippedByType > 0) {
      lines.push(`${this.formatCount(skippedByType, 'workout')} skipped by type filter.`);
    }

    this.success(this.joinLines([
      ...lines,
      this.formatPeriod(response.startDate, response.endDate)
    ]));
  }

  copyCalendarToLibraryCompleted(response: any, libraryName?: string): void {
    const copied = response.copied ?? 0;
    const skippedByType = response.skippedByType ?? 0;

    const destination = libraryName
      ? ` to "${libraryName}"`
      : '';

    const lines: string[] = [];

    if (copied > 0) {
      lines.push(`${this.formatCount(copied, 'workout')} copied${destination}.`);
    } else if (skippedByType > 0) {
      lines.push('No workouts copied.');
    } else {
      lines.push('No workouts found for the selected period.');
    }

    if (skippedByType > 0) {
      lines.push(`${this.formatCount(skippedByType, 'workout')} skipped by type filter.`);
    }

    this.success(this.joinLines([
      ...lines,
      this.formatPeriod(response.startDate, response.endDate)
    ]));
  }

  singleWorkoutCopied(workoutName?: string, destinationName?: string): void {
    const workout = workoutName
      ? `"${workoutName}"`
      : 'Workout';

    const destination = destinationName
      ? ` to "${destinationName}"`
      : '';

    this.success(`${workout} copied successfully${destination}.`);
  }

  libraryContainerCopied(planName: string, workouts: number): void {
    this.success(this.joinLines([
      `"${planName}" copied successfully.`,
      `${this.formatCount(workouts, 'workout')} included.`
    ]));
  }

  configurationSaved(): void {
    this.success('Configuration saved successfully.');
  }

  scheduledSyncCreated(): void {
    this.success('Scheduled sync created. It will run every 20 minutes for today.');
  }

  scheduledSyncDeleted(): void {
    this.success('Scheduled sync deleted.');
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

  private formatCount(count: number, singular: string, plural?: string): string {
    return `${count} ${count === 1 ? singular : plural ?? `${singular}s`}`;
  }

  private formatPeriod(startDate?: string, endDate?: string): string | undefined {
    if (!startDate || !endDate) {
      return undefined;
    }

    if (startDate === endDate) {
      return `Date: ${startDate}`;
    }

    return `Period: ${startDate} to ${endDate}`;
  }

  private joinLines(lines: Array<string | undefined | null>): string {
    return lines
      .filter((line): line is string => !!line)
      .join('\n');
  }
}