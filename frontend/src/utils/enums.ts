import { t, type MessageKey } from '../i18n'
import type { Test, TestInstance, Student, StudentTest } from '../types'

export const testStatusLabel = (status: Test['status']): string =>
  t((`label_${status.toLowerCase()}` as MessageKey))

export const testStatusColor = (status: Test['status']): string =>
  status === 'ACTIVE' ? 'success' : status === 'DRAFT' ? 'warning' : 'danger'

export const testTypeLabel = (type: Test['type']): string =>
  t(type === 'PUBLIC' ? 'label_public' : 'label_private')

export const testTypeColor = (type: Test['type']): string =>
  type === 'PUBLIC' ? 'success' : 'primary'

export const studentTestStatusLabel = (status: StudentTest['status']): string =>
  t((`label_${status.toLowerCase()}` as MessageKey))

export const studentTestStatusColor = (status: StudentTest['status']): string => {
  switch (status) {
    case 'FINISHED':
      return 'success'
    case 'IN_PROGRESS':
      return 'warning'
    case 'NOT_DONE':
      return 'danger'
    default:
      return 'secondary'
  }
}

export const instanceStatusLabel = (status: TestInstance['status']): string =>
  t(status === 'CLOSED' ? 'label_closed' : 'label_in_progress')

export const instanceStatusColor = (status: TestInstance['status']): string =>
  status === 'CLOSED' ? 'danger' : 'warning'

export const genderLabel = (gender: Student['gender']): string => {
  switch (gender) {
    case 'MALE':
      return t('label_male')
    case 'FEMALE':
      return t('label_female')
    case 'NOT_APPLICABLE':
      return t('label_not_applicable')
    default:
      return t('label_unknown')
  }
}

export const SORT_OPTIONS = [
  { value: 'NEWEST', label: t('label_newest') },
  { value: 'OLDEST', label: t('label_oldest') },
  { value: 'A_Z', label: t('label_a_z') },
  { value: 'Z_A', label: t('label_z_a') },
]
