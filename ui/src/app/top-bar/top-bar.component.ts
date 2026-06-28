import { AsyncPipe } from '@angular/common';
import { BreakpointObserver } from '@angular/cdk/layout';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { MatBadgeModule } from '@angular/material/badge';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { map, shareReplay } from 'rxjs/operators';

@Component({
  selector: 'app-top-bar',
  standalone: true,
  imports: [
    AsyncPipe,
    RouterLink,
    MatBadgeModule,
    MatButtonModule,
    MatIconModule,
    MatToolbarModule,
    MatTooltipModule
  ],
  templateUrl: './top-bar.component.html',
  styleUrl: './top-bar.component.scss'
})
export class TopBarComponent {

  @Input() menuButtons: { icon: string; name: string; url: string }[] = [];
  @Input() appVersion = '';
  @Input() githubLink = 'https://github.com/costa-alex/tp2intervals';
  @Input() updateAvailableBadgeHidden = true;

  @Output() menuClicked = new EventEmitter<void>();

  readonly isMobile$ = this.breakpointObserver
    .observe('(max-width: 768px)')
    .pipe(
      map(result => result.matches),
      shareReplay()
    );

  constructor(
    protected router: Router,
    private breakpointObserver: BreakpointObserver
  ) {
  }
}