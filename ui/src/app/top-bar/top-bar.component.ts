import { AsyncPipe } from '@angular/common';
import { BreakpointObserver } from '@angular/cdk/layout';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { map, shareReplay } from 'rxjs/operators';

@Component({
    selector: 'app-top-bar',
    imports: [
        AsyncPipe,
        RouterLink,
        MatButtonModule,
        MatIconModule,
        MatToolbarModule
    ],
    templateUrl: './top-bar.component.html',
    styleUrl: './top-bar.component.scss'
})
export class TopBarComponent {

  @Input() menuButtons: { icon: string; name: string; url: string }[] = [];
 
  @Output() menuClicked = new EventEmitter<void>();

  readonly isMobile$ = this.breakpointObserver
    .observe('(max-width: 768px)')
    .pipe(
      map(result => result.matches),
      shareReplay()
    );

  get currentPageTitle(): string {
    const currentUrl = this.router.url.split('?')[0].split('#')[0];

    const currentButton = this.menuButtons.find(button =>
      currentUrl === button.url || currentUrl.startsWith(`${button.url}/`)
    );

    return currentButton?.name ?? '';
  }

  constructor(
    protected router: Router,
    private breakpointObserver: BreakpointObserver
  ) {
  }
}