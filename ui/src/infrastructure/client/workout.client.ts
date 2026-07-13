import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from 'rxjs';

export interface ScheduledSync {
  id: number;
  types: string[];
  skipSynced: boolean;
  sourcePlatform: string;
  targetPlatform: string;
  startOffsetDays: number;
  endOffsetDays: number;
}

export interface SyncExecution {
  id: number;
  scheduleId?: number;
  triggerType: 'MANUAL' | 'SCHEDULED' | 'RUN_NOW';
  sourcePlatform: string;
  targetPlatform: string;
  startDate: string;
  endDate: string;
  startedAt: string;
  finishedAt?: string;
  status:
    | 'RUNNING'
    | 'SUCCESS'
    | 'NO_CHANGES'
    | 'PARTIAL_SUCCESS'
    | 'FAILED';
  copied: number;
  removed: number;
  skippedByType: number;
  skippedAlreadySynced: number;
  failed: number;
  failedToRemove: number;
  errorMessage?: string;
}

@Injectable({
  providedIn: 'root'
})
export class WorkoutClient {

  constructor(private httpClient: HttpClient) {
  }

  copyCalendarToCalendar(
    startDate,
    endDate,
    types,
    skipSynced,
    platformDirection,
    replaceChangedWorkouts = false
  ): Observable<any> {
    return this.httpClient.post(
      `/api/workout/copy-calendar-to-calendar`,
      {
        startDate,
        endDate,
        types,
        skipSynced,
        replaceChangedWorkouts,
        ...platformDirection
      }
    );
  }

  copyCalendarToLibrary(name, startDate, endDate, types, platformDirection, isPlan): Observable<any> {
    return this.httpClient
      .post(`/api/workout/copy-calendar-to-library`, {name, startDate, endDate, types, ...platformDirection, isPlan})
  }

  copyLibraryToLibrary(externalData, targetLibraryContainer, platformDirection): Observable<any> {
    return this.httpClient
      .post(`/api/workout/copy-library-to-library`, {
        workoutExternalData: externalData,
        targetLibraryContainer, ...platformDirection
      })
  }

  findWorkoutsByName(platform, name): Observable<any> {
    return this.httpClient.get(`/api/workout/find`, {params: {platform, name}})
  }

  scheduleCopyCalendarToCalendar(
    types: string[],
    skipSynced: boolean,
    startOffsetDays: number,
    endOffsetDays: number,
    platformDirection: {
      sourcePlatform: string;
      targetPlatform: string;
    }
  ): Observable<void> {
    return this.httpClient.post<void>(
      '/api/workout/copy-calendar-to-calendar/schedule',
      {
        types,
        skipSynced,
        startOffsetDays,
        endOffsetDays,
        ...platformDirection
      }
    );
  }

  getScheduleRequests(): Observable<ScheduledSync[]> {
    return this.httpClient.get<ScheduledSync[]>(
      '/api/workout/copy-calendar-to-calendar/schedule'
    );
  }

  runScheduleRequest(id: number): Observable<any> {
    return this.httpClient.post(
      `/api/workout/copy-calendar-to-calendar/schedule/${id}/run`,
      {}
    );
  }

  deleteScheduleRequest(id: number): Observable<void> {
    return this.httpClient.delete<void>(
      `/api/workout/copy-calendar-to-calendar/schedule/${id}`
    );
  }

  getSyncExecutions(): Observable<SyncExecution[]> {
    return this.httpClient.get<SyncExecution[]>(
      '/api/sync-executions'
    );
  }
}
