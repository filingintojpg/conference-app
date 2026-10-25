import { useState } from 'react';
import Field from '../../components/Field/Field';
import Notice from '../../components/Notice/Notice';
import Select from '../../components/Select/Select';
import { formatDateTime, isPast } from '../../utils/format';
import './ApplicationForm.css';

/**
 * Форма подачи / редактирования заявки.
 * initial - заявка для редактирования (null - создание новой).
 * onSubmit(values) должен вернуть Promise; при ошибке форма покажет сообщение бэкенда.
 */
export default function ApplicationForm({ directions, initial, onSubmit, onCancel }) {
  const isEdit = Boolean(initial);

  const [values, setValues] = useState({
    directionId: initial ? String(initial.directionId) : '',
    title: initial?.title ?? '',
    abstractText: initial?.abstractText ?? '',
    content: initial?.content ?? '',
  });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [fieldErrors, setFieldErrors] = useState({});

  const setValue = (name) => (value) => setValues((prev) => ({ ...prev, [name]: value }));

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

  async function handleSubmit(event) {
    event.preventDefault();
    setSubmitting(true);
    setError(null);
    setFieldErrors({});
    try {
      await onSubmit(values);
    } catch (err) {
      setError(err.message);
      setFieldErrors(err.fields || {});
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
