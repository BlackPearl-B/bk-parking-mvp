import React from 'react';
import { AlertTriangle } from 'lucide-react';
import { Card } from '../../../../components/ui/Card';
import { AlertItem } from '../types';

interface AlertCardProps {
  alert: AlertItem;
}

export const AlertCard: React.FC<AlertCardProps> = ({ alert }) => {
  return (
    <Card headerTitle="Cảnh báo bảo vệ" headerRight="2 cảnh báo · còn 1" className="border-warning-border">
      <div className="flex items-center justify-between gap-3">
        <div className="flex items-center gap-2.5">
          <AlertTriangle className="w-5 h-5 text-warning-default shrink-0" />
          <div>
            <p className="text-sm font-bold text-text-primary">{alert.title}</p>
            <p className="text-xs text-text-secondary">
              {alert.time} · {alert.priorityNote}
            </p>
          </div>
        </div>
        <button
          type="button"
          className="min-h-touch px-5 rounded-full bg-surface-subtle border border-border-default text-sm font-bold text-text-primary hover:bg-surface-hover shrink-0"
        >
          Xem
        </button>
      </div>
    </Card>
  );
};

