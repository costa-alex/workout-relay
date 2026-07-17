import { Component, OnInit } from '@angular/core';

import { MatExpansionModule } from '@angular/material/expansion';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatIconModule } from '@angular/material/icon';

import { Platform } from 'infrastructure/platform';
import { ConfigurationClient } from 'infrastructure/client/configuration.client';

import {
  TrCopyCalendarToCalendarComponent
} from 'app/trainer-road/tr-copy-calendar-to-calendar/tr-copy-calendar-to-calendar.component';
import {
  TrCopyCalendarToLibraryComponent
} from 'app/trainer-road/tr-copy-calendar-to-library/tr-copy-calendar-to-library.component';
import {
  TrCopyLibraryToLibraryComponent
} from 'app/trainer-road/tr-copy-library-to-library/tr-copy-library-to-library.component';

@Component({
    selector: 'app-trainer-road',
    imports: [
    MatExpansionModule,
    MatProgressBarModule,
    MatIconModule,
    TrCopyLibraryToLibraryComponent,
    TrCopyCalendarToLibraryComponent,
    TrCopyCalendarToCalendarComponent
],
    templateUrl: './trainer-road.component.html',
    styleUrl: './trainer-road.component.scss'
})
export class TrainerRoadComponent implements OnInit {
  platformInfo: any = undefined;

  private readonly platform = Platform.TRAINER_ROAD;

  constructor(
    private configurationClient: ConfigurationClient
  ) {
  }

  ngOnInit(): void {
    this.configurationClient.platformInfo(this.platform.key).subscribe(value => {
      this.platformInfo = value;
    });
  }
}