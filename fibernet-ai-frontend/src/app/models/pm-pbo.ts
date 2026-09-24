export interface PmPbo {
  id: number;
  type: string;
  capacity: number;
  location: string;
  latitude?: number;
  longitude?: number;
}

export interface PmPboWithCoordinates extends PmPbo {
  latitude: number;
  longitude: number;
}

export const PmPboTypeColors: { [key: string]: string } = {
  'PIO': '#3b82f6',
  'PBO': '#22c55e',
  'PBI': '#f97316',
  'PM': '#ef4444'
};

export const PmPboTypeNames: { [key: string]: string } = {
  'PIO': 'Point d\'Interconnexion Optique',
  'PBO': 'Point de Branchement Optique',
  'PBI': 'Point de Branchement Individuel',
  'PM': 'Point de Mutualisation'
};
