import { useState } from 'react';
import type { Direction } from '../../types';
import Select from '../../components/Select/Select';
import './TransferAction.css';

interface TransferActionProps {
  directions: Direction[];
  disabled?: boolean;
  onTransfer: (targetDirectionId: string) => Promise<void> | void;
}

/**
 * Кнопка "Передать на другое направление": по клику показывает выбор направления.
 * directions - направления, на которые можно передать заявку (текущее уже исключено).
 * onTransfer(targetDirectionId) вызывается после подтверждения.
 */
export default function TransferAction({
  directions,
  disabled = false,
  onTransfer,
}: TransferActionProps) {
  const [open, setOpen] = useState(false);
  const [targetId, setTargetId] = useState('');

  if (!open) {
    return (
      <button type="button" disabled={disabled} onClick={() => setOpen(true)}>
        Передать на другое направление
      </button>
    );
  }

  async function submit() {
    await onTransfer(targetId);
    setOpen(false);
    setTargetId('');
  }

  return (
    <div className="transfer-action">
      <Select
        label="Передать на направление"
        value={targetId}
        onChange={setTargetId}
        placeholder="Выберите направление"
        required
        options={directions.map((d) => ({
          value: String(d.id),
          label: `${d.name} (${d.organizerFullName})`,
        }))}
      />
      <div className="transfer-action__buttons">
        <button type="button" className="primary" disabled={disabled || !targetId} onClick={submit}>
          Передать
        </button>
        <button
          type="button"
          disabled={disabled}
          onClick={() => {
            setOpen(false);
            setTargetId('');
          }}
        >
          Отмена
        </button>
      </div>
    </div>
  );
}
