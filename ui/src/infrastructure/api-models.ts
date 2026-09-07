export interface ExternalData {
  trainingPeaksId?: string;
  intervalsId?: string;
  trainerRoadId?: string;
}

export interface LibraryContainer {
  name: string;
  startDate: string;
  isPlan: boolean;
  workoutsAmount: number;
  externalData: ExternalData;
}

export interface WorkoutDetails {
  name: string;
  duration?: string;
  load?: number;
  externalData: ExternalData;
}

export interface WorkoutSyncFailure {
  workoutName: string;
  workoutDate?: string;
  message: string;
}

export interface CopyWorkoutsResponse {
  copied: number;
  filteredOut: number;
  skippedByType: number;
  skippedAlreadySynced: number;
  startDate: string;
  endDate: string;
  externalData: ExternalData;
  failed: number;
  failedWorkouts: WorkoutSyncFailure[];
  removed: number;
  failedToRemove: number;
  failedRemovals: WorkoutSyncFailure[];
}

export interface CopyPlanResponse {
  planName: string;
  workouts: number;
  externalData: ExternalData;
}

export interface CopyActivitiesResponse {
  copied: number;
  filteredOut: number;
  startDate: string;
  endDate: string;
}