import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';

import { ConfigData } from '../config-data';

export interface PlatformConnectionInfo {
  isValid: boolean;
  isAthlete?: boolean;
  isPremium?: boolean;
}

export interface PlatformConnectionMap {
  [platformKey: string]: PlatformConnectionInfo | undefined;
}

@Injectable({
  providedIn: 'root'
})
export class ConfigurationClient {

  constructor(
    private httpClient: HttpClient
  ) {
  }

  getConfig(): Observable<ConfigData> {
    return this.httpClient
      .get('/api/configuration')
      .pipe(
        map((response: any) =>
          new ConfigData(response?.config)
        )
      );
  }

  updateConfig(configData: ConfigData): Observable<any> {
    return this.httpClient.put(
      '/api/configuration',
      configData
    );
  }

  getAllPlatformInfo(): Observable<PlatformConnectionMap> {
    return this.httpClient
      .get('/api/configuration/platform')
      .pipe(
        map(response => this.unwrapPlatformInfo(response))
      );
  }

  refreshAllPlatformInfo(): Observable<PlatformConnectionMap> {
    return this.httpClient
      .post('/api/configuration/platform/refresh', {})
      .pipe(
        map(response => this.unwrapPlatformInfo(response))
      );
  }

  platformInfo(
    platform: string
  ): Observable<PlatformConnectionInfo> {
    return this.httpClient
      .get(`/api/configuration/${platform}`)
      .pipe(
        map((response: any) =>
          response?.infoMap ?? { isValid: false }
        )
      );
  }

  private unwrapPlatformInfo(
    response: any
  ): PlatformConnectionMap {
    const result: PlatformConnectionMap = {};

    Object.keys(response ?? {}).forEach(key => {
      result[key] =
        response[key]?.infoMap ?? { isValid: false };
    });

    return result;
  }
}