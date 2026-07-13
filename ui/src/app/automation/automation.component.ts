import { DatePipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  Validators
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { finalize, forkJoin } from 'rxjs';
import {
  ScheduledSync,
  SyncExecution,
  WorkoutClient
} from 'infrastructure/client/workout.client';
import { NotificationService } from 'infrastructure/notification.service';
import { Platform } from 'infrastructure/platform';
import { TrainingTypes } from 'infrastructure/training-types';

const MIN_OFFSET_DAYS = -365;
const MAX_OFFSET_DAYS = 365;


function integerValidator(
  control: AbstractControl
): ValidationErrors | null {
  const value = control.value;

  if (value === null || value === undefined) {
    return null;
  }

  return Number.isInteger(value)
    ? null
    : { integer: true };
}

function periodValidator(
  control: AbstractControl
): ValidationErrors | null {
  const startOffset = control.get('startOffsetDays')?.value;
  const endOffset = control.get('endOffsetDays')?.value;

  if (
    startOffset === null ||
    startOffset === undefined ||
    endOffset === null ||
    endOffset === undefined
  ) {
    return null;
  }

  return startOffset <= endOffset
    ? null
    : { invalidPeriod: true };
}

@Component({
  selector: 'app-automation',
  standalone: true,
  imports: [
    DatePipe,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressBarModule,
    MatSelectModule
  ],
  templateUrl: './automation.component.html',
  styleUrl: './automation.component.scss'
})
export class AutomationComponent implements OnInit {

  readonly Platform = Platform;

  readonly directions = [
    {
      title: 'TrainerRoad → TrainingPeaks',
      value: Platform.DIRECTION_TR_TP
    },
    {
      title: 'TrainerRoad → Intervals.icu',
      value: Platform.DIRECTION_TR_INT
    },
    {
      title: 'TrainingPeaks → Intervals.icu',
      value: Platform.DIRECTION_TP_INT
    },
    {
      title: 'Intervals.icu → TrainingPeaks',
      value: Platform.DIRECTION_INT_TP
    }
  ];

  readonly trainingTypes = [
    { title: 'Ride', value: 'BIKE' },
    { title: 'Virtual Ride', value: 'VIRTUAL_BIKE' },
    { title: 'MTB', value: 'MTB' },
    { title: 'Run', value: 'RUN' },
    { title: 'Swim', value: 'SWIM' },
    { title: 'Walk', value: 'WALK' },
    { title: 'Weight Training', value: 'WEIGHT' },
    { title: 'Any other', value: 'UNKNOWN' }
  ];

  formGroup: FormGroup;
  schedules: ScheduledSync[] = [];
  executions: SyncExecution[] = [];
  loading = false;

  constructor(
    private formBuilder: FormBuilder,
    private workoutClient: WorkoutClient,
    private notificationService: NotificationService
  ) {
    this.formGroup = this.formBuilder.group(
      {
        direction: [
          Platform.DIRECTION_TR_TP,
          Validators.required
        ],
        trainingTypes: [
          ['BIKE', 'VIRTUAL_BIKE'],
          Validators.required
        ],
        startOffsetDays: [
          0,
          [
            Validators.required,
            integerValidator,
            Validators.min(MIN_OFFSET_DAYS),
            Validators.max(MAX_OFFSET_DAYS)
          ]
        ],
        endOffsetDays: [
          0,
          [
            Validators.required,
            integerValidator,
            Validators.min(MIN_OFFSET_DAYS),
            Validators.max(MAX_OFFSET_DAYS)
          ]
        ],
        skipSynced: [true]
      },
      {
        validators: periodValidator
      }
    );
  }

  ngOnInit(): void {
    this.loadData();
  }

  createSchedule(): void {
    if (this.formGroup.invalid) {
      this.formGroup.markAllAsTouched();
      return;
    }

    const {
      direction,
      trainingTypes,
      skipSynced,
      startOffsetDays,
      endOffsetDays
    } = this.formGroup.getRawValue();

    this.loading = true;

    this.workoutClient
      .scheduleCopyCalendarToCalendar(
        trainingTypes,
        skipSynced,
        startOffsetDays,
        endOffsetDays,
        direction
      )
      .pipe(
        finalize(() => {
          this.loading = false;
        })
      )
      .subscribe(() => {
        this.notificationService.scheduledSyncCreated();
        this.loadData();
      });
  }

  runNow(schedule: ScheduledSync): void {
    this.loading = true;

    this.workoutClient
      .runScheduleRequest(schedule.id)
      .pipe(
        finalize(() => {
          this.loading = false;
        })
      )
      .subscribe(response => {
        this.notificationService
          .copyCalendarToCalendarCompleted(
            response,
            Platform.getTitle(schedule.sourcePlatform),
            Platform.getTitle(schedule.targetPlatform)
          );

        this.loadData();
      });
  }

  deleteSchedule(schedule: ScheduledSync): void {
    const confirmed = window.confirm(
      `Delete scheduled sync ${this.directionTitle(schedule)}?`
    );

    if (!confirmed) {
      return;
    }

    this.loading = true;

    this.workoutClient
      .deleteScheduleRequest(schedule.id)
      .pipe(
        finalize(() => {
          this.loading = false;
        })
      )
      .subscribe(() => {
        this.notificationService.scheduledSyncDeleted();
        this.loadData();
      });
  }

  directionTitle(schedule: ScheduledSync): string {
    return `${Platform.getTitle(schedule.sourcePlatform)} → ` +
      `${Platform.getTitle(schedule.targetPlatform)}`;
  }

  schedulePeriodTitle(schedule: ScheduledSync): string {
    return this.periodTitle(
      schedule.startOffsetDays,
      schedule.endOffsetDays
    );
  }

  trainingTypeTitles(types: string[]): string {
    return types
      .map(type => TrainingTypes.getTitle(type))
      .join(', ');
  }

  executionDirectionTitle(execution: SyncExecution): string {
    return `${Platform.getTitle(execution.sourcePlatform)} → ` +
      `${Platform.getTitle(execution.targetPlatform)}`;
  }

  triggerTitle(
    trigger: SyncExecution['triggerType']
  ): string {
    switch (trigger) {
      case 'SCHEDULED':
        return 'Scheduled';

      case 'RUN_NOW':
        return 'Run now';

      case 'MANUAL':
        return 'Manual';
    }
  }

  statusTitle(
    status: SyncExecution['status']
  ): string {
    switch (status) {
      case 'RUNNING':
        return 'Running';

      case 'SUCCESS':
        return 'Success';

      case 'NO_CHANGES':
        return 'No changes';

      case 'PARTIAL_SUCCESS':
        return 'Partial success';

      case 'FAILED':
        return 'Failed';
    }
  }

  private periodTitle(
    startOffset: number,
    endOffset: number
  ): string {
    const start = this.offsetTitle(startOffset);
    const end = this.offsetTitle(endOffset);

    return startOffset === endOffset
      ? start
      : `${start} → ${end}`;
  }

  private offsetTitle(offset: number): string {
    if (offset === -1) {
      return 'Yesterday';
    }

    if (offset === 0) {
      return 'Today';
    }

    if (offset === 1) {
      return 'Tomorrow';
    }

    if (offset < 0) {
      return `${Math.abs(offset)} days before`;
    }

    return `${offset} days ahead`;
  }

  private loadData(): void {
    this.loading = true;

    forkJoin({
      schedules: this.workoutClient.getScheduleRequests(),
      executions: this.workoutClient.getSyncExecutions()
    })
      .pipe(
        finalize(() => {
          this.loading = false;
        })
      )
      .subscribe(({ schedules, executions }) => {
        this.schedules = schedules;
        this.executions = executions;
      });
  }
}
