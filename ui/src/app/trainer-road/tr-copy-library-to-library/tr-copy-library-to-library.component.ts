import {Component, OnInit} from '@angular/core';
import {MatGridListModule} from "@angular/material/grid-list";
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {MatButtonModule} from "@angular/material/button";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {MatProgressBarModule} from "@angular/material/progress-bar";
import { AsyncPipe } from "@angular/common";
import {MatDatepickerModule} from "@angular/material/datepicker";
import {MatNativeDateModule} from "@angular/material/core";
import {MatSnackBarModule} from "@angular/material/snack-bar";
import {MatSelectModule} from "@angular/material/select";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {WorkoutClient} from "infrastructure/client/workout.client";
import {NotificationService} from "infrastructure/notification.service";
import {debounceTime, filter, finalize, map, Observable, switchMap, tap} from "rxjs";
import {LibraryClient} from "infrastructure/client/library-client.service";
import {Platform} from "infrastructure/platform";
import {MatAutocompleteModule} from "@angular/material/autocomplete";
import {LibraryContainer, WorkoutDetails} from 'infrastructure/api-models';

@Component({
    selector: 'tr-copy-library-to-library',
    imports: [
    MatGridListModule,
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
    AsyncPipe,
    MatAutocompleteModule
],
    templateUrl: './tr-copy-library-to-library.component.html',
    styleUrl: './tr-copy-library-to-library.component.scss'
})
export class TrCopyLibraryToLibraryComponent implements OnInit {

  formGroup: FormGroup = this.formBuilder.group({
    trWorkoutDetails: [null, [Validators.required, Validators.minLength(3)]],
    intervalsPlan: [null, Validators.required],
  });

  searchInProgress = false
  submitInProgress = false

  workouts: Observable<WorkoutDetails[]>;
  intervalsLibraryItem: Observable<{ name: string; value: LibraryContainer }[]>;

  private readonly direction = Platform.DIRECTION_TR_INT

  constructor(
    private formBuilder: FormBuilder,
    private workoutClient: WorkoutClient,
    private planClient: LibraryClient,
    private notificationService: NotificationService
  ) {
  }

  ngOnInit(): void {
    this.loadPlans();
    this.subscribeOnWorkoutNameChange();
  }

  copyWorkoutSubmit(): void {
    if (this.formGroup.invalid) {
      this.formGroup.markAllAsTouched();
      return;
    }

    this.submitInProgress = true
    const workoutDetails = this.formGroup.value.trWorkoutDetails as WorkoutDetails
    const intervalsPlan = this.formGroup.value.intervalsPlan as LibraryContainer
    this.workoutClient.copyLibraryToLibrary(workoutDetails.externalData, intervalsPlan, this.direction).pipe(
      finalize(() => this.submitInProgress = false)
    ).subscribe((response) => {
      this.notificationService.success(`Copied successfully`)
    })
  }

  getWorkoutDetailsName(details: WorkoutDetails | null): string {
    if (!details) {
      return ''
    }
    return `${details.name} (Duration: ${details.duration || '0'}, Load: ${details.load})`
  }

  private loadPlans(): void {
    this.formGroup.disable()
    this.intervalsLibraryItem = this.planClient.getLibraries(Platform.INTERVALS.key).pipe(
      map(plans => plans.map(plan => {
          return {name: plan.name, value: plan}
        })
      ),
      finalize(() => {
        this.formGroup.enable()
      })
    )
  }

  private subscribeOnWorkoutNameChange(): void {
    this.workouts = this.formGroup.controls['trWorkoutDetails'].valueChanges.pipe(
      debounceTime(500),
      filter(() => this.formGroup.controls['trWorkoutDetails'].valid),
      tap(() => {
        this.searchInProgress = true
      }),
      switchMap((value: string) => this.workoutClient.findWorkoutsByName(Platform.TRAINER_ROAD.key, value).pipe(
        finalize(() => {
          this.searchInProgress = false
        })
      ))
    )
  }
}
