import {
  Component,
  DestroyRef,
  OnInit,
  inject
} from '@angular/core';

import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import { Router } from '@angular/router';

import { finalize } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatOptionModule } from '@angular/material/core';
import { MatSelectModule } from '@angular/material/select';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatIconModule } from '@angular/material/icon';

import { ConfigData } from 'infrastructure/config-data';
import { NotificationService } from 'infrastructure/notification.service';
import {
  ConfigurationClient
} from 'infrastructure/client/configuration.client';
import { ThemeService } from 'infrastructure/theme.service';

@Component({
    selector: 'app-configuration',
    imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressBarModule,
    MatCheckboxModule,
    MatOptionModule,
    MatSelectModule,
    MatExpansionModule,
    MatTooltipModule,
    MatIconModule
],
    templateUrl: './configuration.component.html',
    styleUrl: './configuration.component.scss'
})
export class ConfigurationComponent implements OnInit {

  private readonly destroyRef = inject(DestroyRef);

  formGroup: FormGroup = this.formBuilder.group({
    'intervals.api-key': [
      null,
      Validators.required
    ],
    'intervals.athlete-id': [
      null,
      Validators.required
    ],
    'intervals.power-range': [
      null,
      [
        Validators.required,
        Validators.min(0),
        Validators.max(100)
      ]
    ],
    'intervals.hr-range': [
      null,
      [
        Validators.required,
        Validators.min(0),
        Validators.max(100)
      ]
    ],
    'intervals.pace-range': [
      null,
      [
        Validators.required,
        Validators.min(0),
        Validators.max(100)
      ]
    ],
    'training-peaks.auth-cookie': [
      null,
      [
        Validators.pattern(
          '^Production_tpAuth=[a-zA-Z0-9-_]*$'
        )
      ]
    ],
    'trainer-road.auth-cookie': [
      null,
      [
        Validators.pattern(
          '^SharedTrainerRoadAuth=.*$'
        )
      ]
    ],
    'trainer-road.remove-html-tags': [
      null,
      Validators.required
    ],
    'general.debug-mode': [
      null,
      Validators.required
    ]
  });

  inProgress = false;

  constructor(
    private readonly router: Router,
    private readonly formBuilder: FormBuilder,
    private readonly configClient: ConfigurationClient,
    private readonly notificationService: NotificationService,
    readonly themeService: ThemeService
  ) {}

  ngOnInit(): void {
    this.listenCookie(
      'training-peaks.auth-cookie',
      'Production_tpAuth='
    );

    this.listenCookie(
      'trainer-road.auth-cookie',
      'SharedTrainerRoadAuth='
    );

    this.loadConfiguration();
  }

  onSubmit(): void {
    if (this.formGroup.invalid || this.inProgress) {
      this.formGroup.markAllAsTouched();
      return;
    }

    this.inProgress = true;

    const newConfiguration =
      new ConfigData(this.formGroup.getRawValue());

    this.configClient
      .updateConfig(newConfiguration)
      .pipe(
        finalize(() => {
          this.inProgress = false;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: () => {
          this.notificationService.configurationSaved();
          this.router.navigate(['/home']);
        },
        error: () => {
          this.notificationService.error(
            'Unable to save settings.'
          );
        }
      });
  }

  private loadConfiguration(): void {
    this.inProgress = true;

    this.configClient
      .getConfig()
      .pipe(
        finalize(() => {
          this.inProgress = false;
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: config => {
          this.formGroup.patchValue(
            config.config,
            {
              emitEvent: false
            }
          );
        },
        error: () => {
          this.notificationService.error(
            'Unable to load settings.'
          );
        }
      });
  }

  private listenCookie(
    controlName: string,
    prefix: string
  ): void {
    const control =
      this.formGroup.controls[controlName];

    control.valueChanges
      .pipe(
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(value => {
        const rawValue =
          typeof value === 'string'
            ? value.trim()
            : '';

        const prefixIndex =
          rawValue.indexOf(prefix);

        if (prefixIndex < 0) {
          return;
        }

        const cookieValue = rawValue
          .substring(
            prefixIndex + prefix.length
          )
          .split(';')[0]
          .trim();

        const normalizedValue =
          `${prefix}${cookieValue}`;

        if (rawValue === normalizedValue) {
          return;
        }

        control.setValue(
          normalizedValue,
          {
            emitEvent: false
          }
        );
      });
  }
}