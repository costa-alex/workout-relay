import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from 'rxjs';
import {CopyActivitiesResponse} from '../api-models';
import {PlatformDirection} from '../platform';


@Injectable({
  providedIn: 'root'
})
export class ActivityClient {

  constructor(private httpClient: HttpClient) {
  }

  copyActivities(
    startDate: string,
    endDate: string,
    types: string[],
    platformDirection: PlatformDirection,
  ): Observable<CopyActivitiesResponse> {
    return this.httpClient
      .post<CopyActivitiesResponse>(`/api/activities/copy`, {startDate, endDate, types, ...platformDirection})
  }
}
