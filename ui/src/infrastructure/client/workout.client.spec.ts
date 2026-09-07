import {provideHttpClient} from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import {TestBed} from '@angular/core/testing';

import {CopyWorkoutsResponse} from '../api-models';
import {Platform} from '../platform';
import {WorkoutClient} from './workout.client';

describe('WorkoutClient', () => {
  let client: WorkoutClient;
  let httpTestingController: HttpTestingController;

  const response: CopyWorkoutsResponse = {
    copied: 1,
    filteredOut: 0,
    skippedByType: 0,
    skippedAlreadySynced: 0,
    startDate: '2026-09-07',
    endDate: '2026-09-07',
    externalData: {},
    failed: 0,
    failedWorkouts: [],
    removed: 0,
    failedToRemove: 0,
    failedRemovals: [],
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        WorkoutClient,
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });

    client = TestBed.inject(WorkoutClient);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('posts a typed calendar synchronization request', () => {
    client.copyCalendarToCalendar(
      '2026-09-07',
      '2026-09-08',
      ['BIKE'],
      true,
      Platform.DIRECTION_TR_INT,
    ).subscribe(result => expect(result).toEqual(response));

    const request = httpTestingController.expectOne(
      '/api/workout/copy-calendar-to-calendar'
    );

    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      startDate: '2026-09-07',
      endDate: '2026-09-08',
      types: ['BIKE'],
      skipSynced: true,
      replaceChangedWorkouts: false,
      sourcePlatform: 'TRAINER_ROAD',
      targetPlatform: 'INTERVALS',
    });
    request.flush(response);
  });

  it('returns the typed result of a scheduled execution', () => {
    client.runScheduleRequest(12)
      .subscribe(result => expect(result.copied).toBe(1));

    const request = httpTestingController.expectOne(
      '/api/workout/copy-calendar-to-calendar/schedule/12/run'
    );

    expect(request.request.method).toBe('POST');
    request.flush(response);
  });
});