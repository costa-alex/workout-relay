import { Component, OnInit } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { forkJoin } from 'rxjs';
import * as semver from 'semver';
import { MatBadgeModule } from '@angular/material/badge';
import { MatTooltipModule } from '@angular/material/tooltip';

import { TopBarComponent } from 'app/top-bar/top-bar.component';
import {
  ApplicationInfoClient
} from 'infrastructure/client/application-info.client';
import { GitHubClient } from 'infrastructure/client/github.client';
import { ThemeService } from 'infrastructure/theme.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    RouterOutlet,
    RouterLink,
    TopBarComponent,
    MatSidenavModule,
    MatListModule,
    MatDividerModule,
    MatIconModule,
    MatBadgeModule,
    MatTooltipModule
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit {

  appVersion = '';
  updateAvailableBadgeHidden = true;
  githubLink = 'https://github.com/costa-alex/workout-relay';

  menuButtons = [
    { icon: 'home', name: 'Home', url: '/home' },
    {
      icon: 'directions_bike',
      name: 'TrainerRoad',
      url: '/trainer-road'
    },
    {
      icon: 'timeline',
      name: 'TrainingPeaks',
      url: '/training-peaks'
    },
    {
      icon: 'schedule',
      name: 'Sync Center',
      url: '/sync-center'
    },
    {
      icon: 'settings',
      name: 'Settings',
      url: '/settings'
    }
  ];

  constructor(
    protected router: Router,
    private githubClient: GitHubClient,
    private applicationInfoClient: ApplicationInfoClient,
    private themeService: ThemeService
  ) {
  }

  ngOnInit(): void {
    forkJoin([
      this.githubClient.getLatestRelease(),
      this.applicationInfoClient.getVersion()
    ]).subscribe(([latestRelease, appVersion]) => {
      this.appVersion = appVersion;

      if (semver.gt(latestRelease.version, this.appVersion)) {
        this.updateAvailableBadgeHidden = false;
        this.githubLink = latestRelease.url;
      }
    });
  }
}
