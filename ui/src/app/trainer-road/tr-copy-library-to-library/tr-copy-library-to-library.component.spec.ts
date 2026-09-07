import {FormBuilder} from '@angular/forms';
import {of} from 'rxjs';

import {LibraryClient} from 'infrastructure/client/library-client.service';
import {WorkoutClient} from 'infrastructure/client/workout.client';
import {NotificationService} from 'infrastructure/notification.service';
import {TrCopyLibraryToLibraryComponent} from './tr-copy-library-to-library.component';

describe('TrCopyLibraryToLibraryComponent', () => {
  it('does not submit an invalid form', () => {
    const workoutClient = jasmine.createSpyObj<WorkoutClient>(
      'WorkoutClient',
      {
        copyLibraryToLibrary: of(),
      }
    );
    const component = new TrCopyLibraryToLibraryComponent(
      new FormBuilder(),
      workoutClient,
      jasmine.createSpyObj<LibraryClient>('LibraryClient', ['getLibraries']),
      jasmine.createSpyObj<NotificationService>('NotificationService', ['success']),
    );

    component.copyWorkoutSubmit();

    expect(workoutClient.copyLibraryToLibrary).not.toHaveBeenCalled();
    expect(component.submitInProgress).toBeFalse();
  });
});