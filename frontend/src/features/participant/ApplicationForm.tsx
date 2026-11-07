import { useState, type FormEvent } from 'react';
import { ApiError } from '../../api';
import Field from '../../components/Field/Field';
import Notice from '../../components/Notice/Notice';
import Select from '../../components/Select/Select';
import type { Application, Direction } from '../../types';
import { errorMessage, formatDateTime, isPast } from '../../utils/format';
import './ApplicationForm.css';

export interface ApplicationFormValues {
  directionId: string;
  title: string;
  abstractText: string;
  content: string;
}

interface ApplicationFormProps {
  directions: Direction[];
  initial: Application | null;
  onSubmit: (values: ApplicationFormValues) => Promise<void>;
  onCancel: () => void;
}

export default function ApplicationForm({
  directions,
  initial,
  onSubmit,
  onCancel,
}: ApplicationFormProps) {
  const isEdit = Boolean(initial);

  const [values, setValues] = useState<ApplicationFormValues>({
    directionId: initial ? String(initial.directionId) : '',
    title: initial?.title ?? '',
    abstractText: initial?.abstractText ?? '',
    content: initial?.content ?? '',
  });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  const setValue = (name: keyof ApplicationFormValues) => (value: string) => setValues((prev) => ({ ...prev, [name]: value }));

  const directionOptions = directions.map((direction) => {
    const closed = isPast(direction.deadline);
    return {
      value: String(direction.id),
      label: `${direction.name} - до ${formatDateTime(direction.deadline)}${
        closed ? ' (приём закрыт)' : ''
      }`,
      disabled: !isEdit && closed,
    };
  });

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitting(true);
    setError(null);
    setFieldErrors({});
    try {
      await onSubmit(values);
    } catch (err) {
      setError(errorMessage(err));
      setFieldErrors(err instanceof ApiError ? err.fields : {});
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form className="application-form" onSubmit={handleSubmit}>
      <h2>{isEdit ? 'Редактирование заявки' : 'Новая заявка'}</h2>

      <Select
        label="Направление"
        value={values.directionId}
        onChange={setValue('directionId')}
        options={directionOptions}
        placeholder="Выберите направление"
        required
        disabled={isEdit}
        error={fieldErrors.directionId}
      />
      <Field
        label="Название"
        value={values.title}
        onChange={setValue('title')}
        maxLength={300}
        required
        error={fieldErrors.title}
      />
      <Field
        label="Аннотация"
        value={values.abstractText}
        onChange={setValue('abstractText')}
        multiline
        rows={3}
        required
        error={fieldErrors.abstractText}
      />
      <Field
        label="Содержание"
        value={values.content}
        onChange={setValue('content')}
        multiline
        rows={6}
        required
        error={fieldErrors.content}
      />

      {error && <Notice type="error">{error}</Notice>}

      <div className="application-form__actions">
        <button type="submit" className="primary" disabled={submitting}>
          {isEdit ? 'Сохранить' : 'Подать заявку'}
        </button>
        {isEdit && (
          <button type="button" onClick={onCancel} disabled={submitting}>
            Отмена
          </button>
        )}
      </div>
    </form>
  );
}
