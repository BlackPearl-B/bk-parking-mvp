import React from 'react';
import { Car, Bike, Clock } from 'lucide-react';
import { ParkingLotPublicInfo } from '../api/publicApi';
import { ProgressBar } from '../../../components/ui/ProgressBar';
import { Badge } from '../../../components/ui/Badge';

interface ParkingLotCardProps {
  lot: ParkingLotPublicInfo;
}

export const ParkingLotCard: React.FC<ParkingLotCardProps> = ({ lot }) => {
  const isDanger = lot.occupancyRate >= 90;
  const isWarning = lot.occupancyRate >= 70 && lot.occupancyRate < 90;

  return (
    <div className="bg-surface-card border border-border-default rounded-3xl p-5 flex flex-col justify-between shadow-sm hover:shadow-md transition-all">
      <div className="flex flex-col gap-2">
        <div className="flex items-start justify-between">
          <div>
            <h3 className="text-lg font-bold text-text-primary leading-tight">{lot.name}</h3>
            <p className="text-xs text-text-secondary mt-0.5">{lot.location}</p>
          </div>
          <Badge variant={isDanger ? 'danger' : isWarning ? 'warning' : 'success'}>
            {isDanger ? 'Gần đầy' : isWarning ? 'Sắp đầy' : 'Còn nhiều'}
          </Badge>
        </div>

        <div className="flex items-baseline gap-2 mt-2">
          <span className="text-3xl font-extrabold text-text-primary">còn {lot.availableCount}</span>
          <span className="text-sm font-semibold text-text-secondary">chỗ trống</span>
        </div>

        <div className="flex items-center justify-between text-xs mt-1">
          <span className={`font-bold ${isDanger ? 'text-danger' : isWarning ? 'text-warning-text' : 'text-success-text'}`}>
            {lot.occupancyRate}% lấp đầy
          </span>
          <span className="text-text-secondary">{lot.occupiedCount} / {lot.capacity} xe</span>
        </div>

        <ProgressBar value={lot.occupancyRate} className="mt-1" />
      </div>

      <div className="border-t border-border-subtle pt-3 mt-4 flex items-center justify-between text-[11px] text-text-secondary">
        <div className="flex items-center gap-2">
          <span className="flex items-center gap-1"><Bike className="w-3.5 h-3.5 text-text-primary" /> Xe máy</span>
          {lot.isCarAllowed && (
            <span className="flex items-center gap-1 text-info font-bold">
              · <Car className="w-3.5 h-3.5" /> Có ô tô
            </span>
          )}
        </div>
        <span className="flex items-center gap-1">
          <Clock className="w-3 h-3" /> Cập nhật: {lot.updatedAt}
        </span>
      </div>
    </div>
  );
};
