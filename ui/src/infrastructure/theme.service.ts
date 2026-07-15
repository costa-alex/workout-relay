import { DOCUMENT } from '@angular/common';
import {
  computed,
  effect,
  Inject,
  Injectable,
  OnDestroy,
  signal
} from '@angular/core';

export type ThemePreference = 'system' | 'light' | 'dark';
export type ActiveTheme = Exclude<ThemePreference, 'system'>;

@Injectable({
  providedIn: 'root'
})
export class ThemeService implements OnDestroy {
  private static readonly storageKey = 'workout-relay.theme';

  private readonly preferenceState = signal<ThemePreference>('system');
  private readonly systemDarkState = signal(false);
  private readonly view: Window | null;
  private readonly mediaQuery: MediaQueryList | null;

  readonly preference = this.preferenceState.asReadonly();
  readonly activeTheme = computed<ActiveTheme>(() => {
    const preference = this.preferenceState();

    if (preference === 'system') {
      return this.systemDarkState() ? 'dark' : 'light';
    }

    return preference;
  });

  private readonly mediaQueryListener = (event: MediaQueryListEvent): void => {
    this.systemDarkState.set(event.matches);
  };

  constructor(
    @Inject(DOCUMENT) private readonly document: Document
  ) {
    this.view = this.document.defaultView;
    this.mediaQuery = this.view?.matchMedia?.('(prefers-color-scheme: dark)') ?? null;

    this.systemDarkState.set(this.mediaQuery?.matches ?? false);
    this.preferenceState.set(this.readPreference());
    this.applyTheme(this.activeTheme());

    effect(() => {
      this.applyTheme(this.activeTheme());
    });

    this.mediaQuery?.addEventListener('change', this.mediaQueryListener);
  }

  setPreference(preference: ThemePreference): void {
    if (!this.isThemePreference(preference)) {
      return;
    }

    this.preferenceState.set(preference);

    try {
      this.view?.localStorage.setItem(ThemeService.storageKey, preference);
    } catch {
      // The theme still applies when browser storage is unavailable.
    }
  }

  ngOnDestroy(): void {
    this.mediaQuery?.removeEventListener('change', this.mediaQueryListener);
  }

  private readPreference(): ThemePreference {
    try {
      const storedPreference = this.view?.localStorage.getItem(ThemeService.storageKey);

      if (this.isThemePreference(storedPreference)) {
        return storedPreference;
      }
    } catch {
      // Fall back to the operating-system preference.
    }

    return 'system';
  }

  private isThemePreference(value: unknown): value is ThemePreference {
    return value === 'system' || value === 'light' || value === 'dark';
  }

  private applyTheme(theme: ActiveTheme): void {
    const root = this.document.documentElement;

    root.classList.toggle('app-theme-light', theme === 'light');
    root.classList.toggle('app-theme-dark', theme === 'dark');
    root.style.colorScheme = theme;
  }
}
