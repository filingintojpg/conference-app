import type { ReactNode } from 'react';
import type { NoticeType } from '../../types';
import './Notice.css';

interface NoticeProps {
  type?: NoticeType;
  children: ReactNode;
}

/** Сообщение: type = 'error' | 'success' | 'info'. */
export default function Notice({ type = 'info', children }: NoticeProps) {
  return <div className={`notice notice--${type}`}>{children}</div>;
}
