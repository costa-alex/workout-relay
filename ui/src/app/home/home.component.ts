import { DatePipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { finalize, Observable } from 'rxjs';

import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';

import {
  ConfigurationClient,
  PlatformConnectionInfo,
  PlatformConnectionMap
} from 'infrastructure/client/configuration.client';
import { Platform } from 'infrastructure/platform';

interface PlatformCard {
  key: string;
  title: string;
  icon: string;
  route?: string;
}

interface SyncRoute {
  sourceKey: string;
  sourceTitle: string;
  targetKey: string;
  targetTitle: string;
  route: string;
  queryParams: {
    source: string;
    target: string;
  };
}

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    DatePipe,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatProgressBarModule
  ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent implements OnInit {

  readonly Platform = Platform;

  readonly platformCards: PlatformCard[] = [
    {
      key: Platform.TRAINER_ROAD.key,
      title: Platform.TRAINER_ROAD.title,
      icon: 'directions_bike',
      route: '/trainer-road'
    },
    {
      key: Platform.TRAINING_PEAKS.key,
      title: Platform.TRAINING_PEAKS.title,
      icon: 'timeline',
      route: '/training-peaks'
    },
    {
      key: Platform.INTERVALS.key,
      title: Platform.INTERVALS.title,
      icon: 'insights'
    }
  ];

  readonly syncRoutes: SyncRoute[] = [
    {
      sourceKey: Platform.TRAINER_ROAD.key,
      sourceTitle: Platform.TRAINER_ROAD.title,
      targetKey: Platform.TRAINING_PEAKS.key,
      targetTitle: Platform.TRAINING_PEAKS.title,
      route: '/trainer-road',
      queryParams: {
        source: Platform.TRAINER_ROAD.key,
        target: Platform.TRAINING_PEAKS.key
      }
    },
    {
      sourceKey: Platform.TRAINER_ROAD.key,
      sourceTitle: Platform.TRAINER_ROAD.title,
      targetKey: Platform.INTERVALS.key,
      targetTitle: Platform.INTERVALS.title,
      route: '/trainer-road',
      queryParams: {
        source: Platform.TRAINER_ROAD.key,
        target: Platform.INTERVALS.key
      }
    },
    {
      sourceKey: Platform.TRAINING_PEAKS.key,
      sourceTitle: Platform.TRAINING_PEAKS.title,
      targetKey: Platform.INTERVALS.key,
      targetTitle: Platform.INTERVALS.title,
      route: '/training-peaks',
      queryParams: {
        source: Platform.TRAINING_PEAKS.key,
        target: Platform.INTERVALS.key
      }
    },
    {
      sourceKey: Platform.INTERVALS.key,
      sourceTitle: Platform.INTERVALS.title,
      targetKey: Platform.TRAINING_PEAKS.key,
      targetTitle: Platform.TRAINING_PEAKS.title,
      route: '/training-peaks',
      queryParams: {
        source: Platform.INTERVALS.key,
        target: Platform.TRAINING_PEAKS.key
      }
    }
  ];

  platformInfo: PlatformConnectionMap = {};

  loading = false;
  loadFailed = false;
  lastChecked: Date | null = null;

  constructor(
    private configurationClient: ConfigurationClient
  ) {
  }

  ngOnInit(): void {
    this.loadConnections(false);
  }

  refreshConnections(): void {
    this.loadConnections(true);
  }

  isConnected(platformKey: string): boolean {
    return this.platformInfo[platformKey]?.isValid === true;
  }

  isSyncAvailable(syncRoute: SyncRoute): boolean {
    return this.isConnected(syncRoute.sourceKey) &&
      this.isConnected(syncRoute.targetKey);
  }

  statusText(platformKey: string): string {
    if (this.loading && !this.platformInfo[platformKey]) {
      return 'Checking connection...';
    }

    return this.isConnected(platformKey)
      ? 'Connected'
      : 'Not connected';
  }

  statusDescription(platformKey: string): string {
    const info = this.platformInfo[platformKey];

    if (!info?.isValid) {
      return 'Open Settings to verify the credentials for this platform.';
    }

    switch (platformKey) {
      case Platform.TRAINER_ROAD.key:
        return 'TrainerRoad authentication is valid.';

      case Platform.TRAINING_PEAKS.key:
        return this.trainingPeaksDescription(info);

      case Platform.INTERVALS.key:
        return 'Intervals.icu API access is valid.';

      default:
        return 'Connection is available.';
    }
  }

  private trainingPeaksDescription(
    info: PlatformConnectionInfo
  ): string {
    if (info.isAthlete === false) {
      return 'TrainingPeaks coach account connected.';
    }

    if (info.isAthlete === true && info.isPremium) {
      return 'TrainingPeaks athlete account connected · Premium.';
    }

    if (info.isAthlete === true) {
      return 'TrainingPeaks athlete account connected · Standard.';
    }

    return 'TrainingPeaks connection is valid.';
  }

  private loadConnections(forceRefresh: boolean): void {
    this.loading = true;
    this.loadFailed = false;

    const request$: Observable<PlatformConnectionMap> =
      forceRefresh
        ? this.configurationClient.refreshAllPlatformInfo()
        : this.configurationClient.getAllPlatformInfo();

    request$
      .pipe(
        finalize(() => {
          this.loading = false;
        })
      )
      .subscribe({
        next: response => {
          this.platformInfo = response;
          this.lastChecked = new Date();
        },
        error: () => {
          this.loadFailed = true;
        }
      });
  }
}
