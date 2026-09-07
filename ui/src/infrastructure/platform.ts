const requiredConfigKeys = ['intervals.api-key', 'intervals.athlete-id']

export type PlatformKey = 'INTERVALS' | 'TRAINING_PEAKS' | 'TRAINER_ROAD';

export interface PlatformDefinition {
  key: PlatformKey;
  title: string;
}

export interface PlatformDirection {
  sourcePlatform: PlatformKey;
  targetPlatform: PlatformKey;
}

export class Platform {
  static readonly INTERVALS: PlatformDefinition = {key: 'INTERVALS', title: 'Intervals.icu'}
  static readonly TRAINING_PEAKS: PlatformDefinition = {key: 'TRAINING_PEAKS', title: 'TrainingPeaks'}
  static readonly TRAINER_ROAD: PlatformDefinition = {key: 'TRAINER_ROAD', title: 'TrainerRoad'}
  static readonly platforms: PlatformDefinition[] = [
    this.INTERVALS, this.TRAINING_PEAKS, this.TRAINER_ROAD
  ]

  static readonly DIRECTION_TP_INT: PlatformDirection = {
    sourcePlatform: this.TRAINING_PEAKS.key, targetPlatform: this.INTERVALS.key
  }
  static readonly DIRECTION_INT_TP: PlatformDirection = {
    sourcePlatform: this.INTERVALS.key, targetPlatform: this.TRAINING_PEAKS.key
  }
  static readonly DIRECTION_TR_INT: PlatformDirection = {
    sourcePlatform: this.TRAINER_ROAD.key, targetPlatform: this.INTERVALS.key
  }
  static readonly DIRECTION_TR_TP: PlatformDirection = {
    sourcePlatform: this.TRAINER_ROAD.key, targetPlatform: this.TRAINING_PEAKS.key
  }

  static getTitle(key: string): string | undefined {
    return this.platforms.find(platform => platform.key === key)?.title
  }
}
