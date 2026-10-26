function splitDatePart(value: string): { date: string; time: string | null } {
  const [datePart, timePart] = value.split('T')
  const time = timePart ? timePart.split('.')[0].slice(0, 5) : null
  return { date: datePart, time }
}

function toDdMmYyyy(datePart: string): string | null {
  const [year, month, day] = datePart.split('-')
  if (!year || !month || !day) return null
  return `${day}/${month}/${year}`
}

export function formatDate(value: string | null | undefined): string {
  if (!value) return '-'
  const { date } = splitDatePart(value)
  return toDdMmYyyy(date) ?? value
}

export function formatDateTime(value: string | null | undefined): string {
  if (!value) return '-'
  const { date, time } = splitDatePart(value)
  const formatted = toDdMmYyyy(date)
  if (!formatted) return value
  return time ? `${formatted} ${time}` : formatted
}
