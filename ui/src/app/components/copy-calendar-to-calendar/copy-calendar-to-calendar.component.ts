import {Component, Input, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {MatButtonModule} from "@angular/material/button";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {MatProgressBarModule} from "@angular/material/progress-bar";
import {MatDatepickerModule} from "@angular/material/datepicker";
import {MatNativeDateModule} from "@angular/material/core";
import {MatSnackBarModule} from "@angular/material/snack-bar";
import {MatSelectModule} from "@angular/material/select";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {Platform} from "infrastructure/platform";
import {formatDate} from "utils/date-formatter";

import {ConfigurationClient} from "infrastructure/client/configuration.client";
import {finalize} from "rxjs";
import {WorkoutClient} from "infrastructure/client/workout.client";
import {NotificationService} from "infrastructure/notification.service";
import {MatTooltipModule} from "@angular/material/tooltip";
import { MatIconModule } from '@angular/material/icon';
import { ActivatedRoute, RouterLink } from '@angular/router';

@Component({
    selector: 'copy-calendar-to-calendar',
    imports: [
    FormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    ReactiveFormsModule,
    MatProgressBarModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatSnackBarModule,
    MatSelectModule,
    MatCheckboxModule,
    MatTooltipModule,
    MatIconModule,
    RouterLink
],
    templateUrl: './copy-calendar-to-calendar.component.html',
    styleUrl: './copy-calendar-to-calendar.component.scss'
})
export class CopyCalendarToCalendarComponent implements OnInit {
  readonly Platform = Platform;
  readonly todayDate = new Date()
  readonly tomorrowDate = new Date(new Date().getTime() + 24 * 60 * 60 * 1000)

  @Input() trainingTypes: any[] = []
  @Input() selectedTrainingTypes = ['BIKE', 'VIRTUAL_BIKE']
  @Input() directions: any[] = []
  @Input() inProgress = false

  formGroup: FormGroup
  platformsInfo: any

  constructor(
    private activatedRoute: ActivatedRoute,
    private formBuilder: FormBuilder,
    private configurationClient: ConfigurationClient,
    private workoutClient: WorkoutClient,
    private notificationService: NotificationService
  ) {
  }

  ngOnInit(): void {
    this.configurationClient.getAllPlatformInfo().subscribe(value => {
      this.platformsInfo = value
    })
    this.formGroup = this.getFormGroup();
    this.applyDirectionFromQueryParams();
  }

  submit(): void {
    const startDate = formatDate(
      this.formGroup.controls['startDate'].value
    );

    const endDate = formatDate(
      this.formGroup.controls['endDate'].value
    );

    this.copyWorkouts(
      startDate,
      endDate,
      false
    );
  }

  today(): void {
    this.copyWorkoutsForOneDay(
      formatDate(this.todayDate)
    );
  }

  tomorrow(): void {
    this.copyWorkoutsForOneDay(
      formatDate(this.tomorrowDate)
    );
  }

  private copyWorkoutsForOneDay(date: string): void {
    this.copyWorkouts(
      date,
      date,
      true
    );
  }

  private copyWorkouts(
    startDate: string,
    endDate: string,
    reconcileChangedWorkouts = false
  ): void {
    const direction = this.formGroup.value.direction;
    const trainingTypes = this.formGroup.value.trainingTypes;
    const skipSynced = this.formGroup.value.skipSynced;

    const replaceChangedWorkouts =
      reconcileChangedWorkouts &&
      direction.sourcePlatform === Platform.TRAINER_ROAD.key &&
      direction.targetPlatform === Platform.TRAINING_PEAKS.key;

    this.inProgress = true;

    this.workoutClient
      .copyCalendarToCalendar(
        startDate,
        endDate,
        trainingTypes,
        skipSynced,
        direction,
        replaceChangedWorkouts
      )
      .pipe(
        finalize(() => this.inProgress = false)
      )
      .subscribe(response => {
        this.notificationService.copyCalendarToCalendarCompleted(
          response,
          Platform.getTitle(direction.sourcePlatform),
          Platform.getTitle(direction.targetPlatform)
        );
      });
  }

  private getFormGroup() {
    return this.formBuilder.group({
      direction: [this.directions[0].value, Validators.required],
      trainingTypes: [this.selectedTrainingTypes, Validators.required],
      startDate: [this.todayDate, Validators.required],
      endDate: [this.tomorrowDate, Validators.required],
      skipSynced: [true, Validators.required],
    })
  }

  private platformKey(platform: any): string {
    return typeof platform === 'string'
      ? platform
      : platform?.key;
  }

  private applyDirectionFromQueryParams(): void {
    this.activatedRoute.queryParamMap.subscribe(params => {
      const sourcePlatform = params.get('source');
      const targetPlatform = params.get('target');

      if (!sourcePlatform || !targetPlatform) {
        return;
      }

      const selectedDirection = this.directions.find(direction =>
        this.platformKey(direction.value.sourcePlatform) === sourcePlatform &&
        this.platformKey(direction.value.targetPlatform) === targetPlatform
      );

      if (selectedDirection) {
        this.formGroup.patchValue({
          direction: selectedDirection.value
        });
      }
    });
  }
}
