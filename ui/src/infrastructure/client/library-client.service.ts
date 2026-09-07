import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from 'rxjs';
import {CopyPlanResponse, LibraryContainer} from '../api-models';
import {PlatformDirection, PlatformKey} from '../platform';


@Injectable({
  providedIn: 'root'
})
export class LibraryClient {

  constructor(private httpClient: HttpClient) {
  }

  getLibraries(platform: PlatformKey): Observable<LibraryContainer[]> {
    return this.httpClient.get<LibraryContainer[]>(`/api/library-container`, {params: {platform}})
  }

  copyLibraryContainer(
    libraryContainer: LibraryContainer,
    newName: string,
    newStartDate: string,
    stepModifier: string,
    platformDirection: PlatformDirection,
  ): Observable<CopyPlanResponse> {
    return this.httpClient
      .post<CopyPlanResponse>(`/api/library-container/copy`, {
        libraryContainer,
        newName,
        newStartDate,
        stepModifier,
        ...platformDirection
      })
  }
}
