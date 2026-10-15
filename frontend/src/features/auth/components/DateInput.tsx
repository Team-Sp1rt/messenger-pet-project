import { useState, forwardRef } from 'react';
import type { InputHTMLAttributes } from 'react';
import DatePicker from 'react-datepicker';
import { IMaskInput } from 'react-imask';
import IMask from 'imask';
import 'react-datepicker/dist/react-datepicker.css';

type DateInputProps = {
    className?: string;
    name?: string;
    required?: boolean;
    onDateChange: (isoDate: string) => void;
};

type CustomInputProps = Omit<InputHTMLAttributes<HTMLInputElement>, 'onChange'> & {
    onChange?: unknown;
    onTextChange?: (text: string) => void;
};

const MaskedCustomInput = forwardRef<HTMLInputElement, CustomInputProps>(
    ({ value, onTextChange, ...rest }, ref) => {
        const current = (value as string) ?? '';

        return (
            <IMaskInput
                {...(rest as object)}
                inputRef={ref}
                value={current}
                mask="d.m.Y"
                blocks={{
                    d: { mask: IMask.MaskedRange, from: 1, to: 31, maxLength: 2, autofix: 'pad' },
                    m: { mask: IMask.MaskedRange, from: 1, to: 12, maxLength: 2, autofix: 'pad' },
                    Y: { mask: IMask.MaskedRange, from: 1900, to: new Date().getFullYear(), maxLength: 4 },
                }}
                onAccept={(val: string) => {
                    if (val === current) return;
                    onTextChange?.(val);
                }}
            />
        );
    }
);

MaskedCustomInput.displayName = 'MaskedCustomInput';

const MIN_DATE = new Date(1900, 0, 1);
const pad = (n: number) => String(n).padStart(2, '0');
const formatDate = (d: Date) =>
    `${pad(d.getDate())}.${pad(d.getMonth() + 1)}.${d.getFullYear()}`;

const parseDate = (text: string): Date | null => {
    const m = /^(\d{2})\.(\d{2})\.(\d{4})$/.exec(text);
    if (!m) return null;

    const [d, mo, y] = [Number(m[1]), Number(m[2]), Number(m[3])];
    const date = new Date(y, mo - 1, d);

    if (date.getFullYear() !== y || date.getMonth() !== mo - 1 || date.getDate() !== d) return null;
    if (date < MIN_DATE || date > new Date()) return null;

    return date;
};

export const DateInput = ({ className, name, required, onDateChange }: DateInputProps) => {
    const [text, setText] = useState('');
    const [selectedDate, setSelectedDate] = useState<Date | null>(null);

    const apply = (date: Date | null) => {
        setSelectedDate(date);
        onDateChange(date ? date.toLocaleDateString('sv-SE') : '');
    };

    const handleTextChange = (val: string) => {
        setText(val);
        apply(parseDate(val));
    };

    const handlePick = (date: Date | null) => {
        setText(date ? formatDate(date) : '');
        apply(date);
    };

    return (
        <DatePicker
            selected={selectedDate}
            onChange={handlePick}
            value={text}
            dateFormat="dd.MM.yyyy"
            placeholderText="DD.MM.YYYY"
            name={name}
            required={required}
            minDate={MIN_DATE}
            maxDate={new Date()}
            showYearDropdown
            showMonthDropdown
            dropdownMode="select"
            customInput={
                <MaskedCustomInput
                    onTextChange={handleTextChange}
                    className={`${className ?? ''} ${text ? 'has-input' : ''}`}
                />
            }
        />
    );
};