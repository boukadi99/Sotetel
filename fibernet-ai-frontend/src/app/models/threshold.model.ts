export type ThresholdTechnology = 'GPON' | 'XGS-PON' | 'DEFAULT' | 'SPLITTER' | string;

export type ThresholdLevel = 'critical' | 'degraded' | 'warning' | 'good' | string;

export interface Threshold {
  id: number | string;
  name: string;
  value: number;
  unit?: string;
  description?: string;
  technology?: ThresholdTechnology;
  level?: ThresholdLevel;
  key?: string;
}

export interface ThresholdUpdatePayload {
  value: number;
}
