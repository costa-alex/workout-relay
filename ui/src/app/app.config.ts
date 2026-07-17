import {
  ApplicationConfig,
  importProvidersFrom
} from '@angular/core';
import {
  provideRouter,
  withHashLocation,
  withInMemoryScrolling
} from '@angular/router';
import { provideAnimations } from '@angular/platform-browser/animations';
import {
  provideHttpClient,
  withInterceptors
} from '@angular/common/http';
import { MatSnackBarModule } from '@angular/material/snack-bar';

import { routes } from './app.routes';
import { httpErrorInterceptor } from 'infrastructure/http.interceptors';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(
      routes,
      withHashLocation(),
      withInMemoryScrolling({
        scrollPositionRestoration: 'top',
        anchorScrolling: 'enabled'
      })
    ),
    provideAnimations(),
    provideHttpClient(
      withInterceptors([
        httpErrorInterceptor
      ])
    ),
    importProvidersFrom(MatSnackBarModule)
  ]
};