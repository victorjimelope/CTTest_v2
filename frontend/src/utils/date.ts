export function formatDate(value: string | null | undefined): string {
  if (!value) return '-'
  const [year, month, day] = value.split('T')[0].split('-')
  if (!year || !month || !day) return value
  return `${day}/${month}/${year}`
}
