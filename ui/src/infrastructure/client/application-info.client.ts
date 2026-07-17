import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';

interface ApplicationInfoResponse {
  build: {
    version: string;
  };
}

@Injectable({
  providedIn: 'root'
})
export class ApplicationInfoClient {

  constructor(
    private readonly httpClient: HttpClient
  ) {
  }

  getVersion(): Observable<string> {
    return this.httpClient
      .get<ApplicationInfoResponse>('/actuator/info')
      .pipe(
        map(response => response.build.version)
      );
  }
}